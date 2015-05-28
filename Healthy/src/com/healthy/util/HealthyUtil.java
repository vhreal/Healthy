package com.healthy.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.ReportedData.Row;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.search.UserSearch;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.healthy.logic.ExtensionalIQ;
import com.healthy.logic.ExtensionalIQProvider;

import static com.healthy.util.Constants.*;

/**
 * Healthy与服务器通讯的工具类,单例
 * 
 * @author Kunlun Zhao
 * */
public class HealthyUtil implements ConnectionListener, PacketListener,
		PacketFilter {

	private static String DEBUG_TAG = "HEALTHY";
	private XMPPConnection mConnection;
	private SimpleDateFormat mDateFormatter = new SimpleDateFormat(
			"yyyy-MM-dd kk:mm:ss");
	private static HealthyUtil mInstance;
	private List<SubscribePacketListener> mSubscribePacketListeners = new ArrayList<SubscribePacketListener>();

	private HealthyUtil() {
		config(ProviderManager.getInstance());// 在连接建立之前，进行一些必要的配置操作
	}

	public synchronized static HealthyUtil getInstance() {
		if (mInstance == null)
			mInstance = new HealthyUtil();
		return mInstance;
	}

	/**
	 * 获取已经连接到的服务器的名称
	 * 
	 * @throws HealthyException
	 * */
	public String getServerName() throws HealthyException {
		if (!isConnected())
			throw new HealthyException("无效网络连接");
		return mConnection.getServiceName();
	}

	/**
	 * 用户登录
	 * 
	 * @param name
	 *            用户名
	 * @param password
	 *            密码
	 * @throws HealthyException
	 * */
	public void login(String name, String password) throws HealthyException {
		if (!isConnected())
			throw new HealthyException("无效网络连接");
		if (mConnection.isAuthenticated())
			return;
		try {
			/** 登录 */
			mConnection.login(name, password);
		} catch (XMPPException e) {
			if (e.getXMPPError().getCode() == 401) {// not-authorized
				throw new HealthyException("用户名或密码错误，登录失败");
			}
			Log.e(DEBUG_TAG, "", e);
		}
	}

	/**
	 * 用户注册
	 * 
	 * @param name
	 *            用户名
	 * @param password
	 *            密码
	 * @throws HealthyException
	 * */
	public void register(String name, String password) throws HealthyException {
		if (!isConnected())
			throw new HealthyException("无效网络连接");
		Registration reg = new Registration();
		reg.setType(IQ.Type.SET);
		reg.setTo(mConnection.getServiceName());
		reg.setUsername(name);// 注意这里createAccount注册时，参数是username，不是jid，是“@”前面的部分。
		reg.setPassword(password);
		reg.addAttribute("android", "geolo_createUser_android");// 这边addAttribute不能为空，否则出错。所以做个标志是android手机创建的吧！！！！！
		PacketFilter filter = new AndFilter(new PacketIDFilter(
				reg.getPacketID()), new PacketTypeFilter(IQ.class));
		PacketCollector collector = mConnection.createPacketCollector(filter);
		mConnection.sendPacket(reg);
		IQ result = (IQ) collector.nextResult(SmackConfiguration
				.getPacketReplyTimeout());
		collector.cancel();// 停止请求results（是否成功的结果）
		if (result == null) {// 出现超时
			throw new HealthyException("请求超时");
		} else if (result.getType() == IQ.Type.RESULT) {// 注册成功
			return;
		} else {
			if (result.getError().getCode() == 409) {// 已存在相关用户
				throw new HealthyException("已存在相关用户");
			} else {
				throw new HealthyException("内部服务器错误");
			}
		}
	}

	/**
	 * 获取已登录的用户名称
	 * 
	 * @return the full XMPP address of the user logged in or null
	 * */
	public String getLoginedUser() {
		if (mConnection == null || !mConnection.isConnected())
			return null;
		return mConnection.getUser();
	}

	/**
	 * 添加好友
	 * 
	 * @param name
	 *            jid (e.g. johndoe@jabber.org)
	 * @param nick
	 *            the nickname of the user
	 * 
	 * @throws HealthyException
	 * */
	public void addFriend(String name, String nick) throws HealthyException {
		if (!isConnected())
			throw new HealthyException("无效网络连接");
		if (getLoginedUser() == null)
			throw new HealthyException("请先进行登录");
		try {
			mConnection.getRoster().createEntry(name+"@"+mConnection.getServiceName(), nick, null);
			// 申请订阅好友状态
			Presence subscription = new Presence(Presence.Type.subscribe);
			subscription.setTo(name);
			mConnection.sendPacket(subscription);
			Log.i("tag", "好友请求已发送");
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			throw new HealthyException(e.getXMPPError().getMessage());
		}
	}

	/**
	 * 删除好友
	 * 
	 * @param name
	 *            要删除的好友的jid (e.g. johndoe@jabber.org)
	 * */
	public void removeFriend(String name) {
		RosterEntry entry = mConnection.getRoster().getEntry(name);
		try {
			mConnection.getRoster().removeEntry(entry);
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			Log.e(DEBUG_TAG, "", e);
		}
		// 取消订阅
		Presence unsubscription = new Presence(Presence.Type.unsubscribe);
		unsubscription.setTo(name);
		mConnection.sendPacket(unsubscription);
	}

	/**
	 * 同意好友请求
	 * 
	 * @param name
	 *            申请人的jid (e.g. johndoe@jabber.org)
	 * */
	public void acceptFriendRequest(String name) {
		Presence presence = new Presence(Presence.Type.subscribed);
		presence.setTo(name+"@"+mConnection.getServiceName());
		mConnection.sendPacket(presence);
	}

	/**
	 * 拒绝好友请求
	 * 
	 * @param name
	 *            申请人的jid (e.g. johndoe@jabber.org)
	 * */
	public void rejectFriendRequest(String name) {
		Presence presence = new Presence(Presence.Type.unsubscribed);
		presence.setTo(name+"@"+mConnection.getServiceName());
		mConnection.sendPacket(presence);
	}
	
	/**
	 * 对方同意添加好友，确认好友关系
	 * 
	 * @param name	申请人的jid (e.g. johndoe@jabber.org)
	 */
	public void establishFriendRequest(String name)  {
		Presence presence = new Presence(Presence.Type.subscribed);
		presence.setTo(name+"@"+mConnection.getServiceName());
		mConnection.sendPacket(presence);
	}

	/**
	 * 登出当前用户
	 * */
	public void logout() {
		if (!isConnected())
			return;
		mConnection.disconnect();// 断开连接，即表示登出操作
	}

	/**
	 * 获取在最近一个月内好友的热量消耗排名
	 * 
	 * @param p
	 *            页码, 初始页码为0.
	 * @param psize
	 *            每页的大小
	 * @param calories
	 *            当前用户在当月的热量消耗
	 * 
	 * @return <b>String</b> 好友列表，已按照热量消耗的降序进行排名</br> 数据结构:</br> {</br>
	 *         "count":可获得当月热量消耗的好友总数量</br> "friends":好友列表</br> [{</br>
	 *         "name":好友用户名</br> "calories":当月消耗热量</br> "time":
	 *         记录最后一次更新时间</br>},...]</br> }
	 * @throws com.healthy.util.HealthyUtil.HealthyException
	 * */
	public String getFriendsByCalories(int p, int psize, float calories)
			throws HealthyException {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("category", RequestCode.GET_FRENDS_BY_CALORIES);// 任务种类
			jsonObject.put("p", p);
			jsonObject.put("psize", psize);
			jsonObject.put("calories", String.format("%.2f", calories));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e(DEBUG_TAG, "", e);
		}
		return sendAMessageToServer(jsonObject.toString());
	}

	/**
	 * 查找附近的人（不一定是好友）
	 * 
	 * @param longitude
	 *            经度
	 * @param latitude
	 *            纬度
	 * @param radius
	 *            搜索半径
	 * @param p
	 *            页码, 初始页码为0.
	 * @param psize
	 *            每页的大小
	 * 
	 * @return <b>String</b> 附近的人列表，已按照曼哈顿距离进行升序排序 </br> 数据结构:</br> {</br>
	 *         "count": 附近人数量</br> "persons": 附近人列表</br> [{</br> "name":
	 *         用户名</br> "longitude": 经度</br> "latitude": 纬度</br> "lastUpdate":
	 *         最后一次更新位置时间</br>},...]</br> }
	 * 
	 * @throws com.healthy.util.HealthyUtil.HealthyException
	 * */
	public String getPersonsNearby(long longitude, long latitude, long radius,
			int p, int psize) throws HealthyException {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("category", RequestCode.GET_PERSONS_NEARBY);// 任务种类
			jsonObject.put("p", p);
			jsonObject.put("psize", psize);
			jsonObject.put("longitude", longitude);
			jsonObject.put("latitude", latitude);
			jsonObject.put("radius", radius);
			jsonObject.put("time", mDateFormatter.format(new Date()));// 最后一次更新时间
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e("Healthy", "", e);
		}
		return sendAMessageToServer(jsonObject.toString());
	}

	/**
	 * 搜索用户
	 * 
	 * @param keyword
	 *            要搜索的关键字
	 * 
	 * @return 由包含keyword用户名所组成的String列表
	 * 
	 * @throws HealthyException
	 * 
	 * */
	public List<String> searchUser(String keyword) throws HealthyException {
		if (!isConnected())
			throw new HealthyException("无效网络连接");
		if (getLoginedUser() == null)
			throw new HealthyException("请先进行登录");
		List<String> users = new ArrayList<String>();
		try {
			UserSearchManager manager = new UserSearchManager(mConnection);
			Form searchForm = manager.getSearchForm("search."
					+ mConnection.getServiceName());
			Form answerForm = searchForm.createAnswerForm();
			answerForm.setAnswer("Username", true);
			answerForm.setAnswer("search", keyword);
			ReportedData data = manager.getSearchResults(answerForm, "search."
					+ mConnection.getServiceName());
			Iterator<Row> iterator = data.getRows();
			while (iterator.hasNext()) {
				Row row = iterator.next();
				String user = row.getValues("Username").next().toString();
				users.add(user);
			}
		} catch (XMPPException e) {
			throw new HealthyException(e.getXMPPError().getMessage());
		}
		return users;
	}

	/**
	 * 获取用户VCard信息，VCard是用来记录用户信息的结构体
	 * 
	 * @param name
	 *            用户jid (e.g. johndoe@jabber.org)
	 * 
	 * @return vcard 用户账户信息
	 * 
	 * @throws HealthyException
	 * 
	 * @throws XMPPException
	 *             If no vCard exists or the user does not exist. The error code
	 *             is service-unavailable (503).
	 * */
	public VCard getUserVCard(String name) throws HealthyException,
			XMPPException {
		if (!isConnected())
			throw new HealthyException("无效网络连接");
		if (getLoginedUser() == null)
			throw new HealthyException("请先进行登录");
		VCard vcard = new VCard();
		vcard.load(mConnection, name);
		return vcard;
	}

	/**
	 * 通过Vcard来获取用户头像信息
	 * 
	 * @param name
	 *            用户jid (e.g. johndoe@jabber.org)
	 * 请求格式为 name@域名--可由mConnection.getServiceName()
	 * @throws XMPPException
	 *             If no vCard exists or the user does not exist. The error code
	 *             is service-unavailable (503).
	 * @throws HealthyException
	 * */
	public Bitmap getUserAvatar(String name) throws XMPPException,
			HealthyException {
		if (!isConnected())
			throw new HealthyException("无效网络连接");
		if (getLoginedUser() == null)
			throw new HealthyException("请先进行登录");
		ByteArrayInputStream bais = null;
		VCard vcard = new VCard();
		vcard.load(mConnection, name+"@"+mConnection.getServiceName());
		if (vcard == null || vcard.getAvatar() == null)
			return null;
		bais = new ByteArrayInputStream(vcard.getAvatar());
		Bitmap avatar = BitmapFactory.decodeStream(bais);
		return avatar;
	}

	/**
	 * 上传用户头像信息
	 * 
	 * @param avatar
	 *            用户头像输入流,可以通过openRawResource(int id)来获取本地文件的输入流文件
	 * 
	 * @throws HealthyException
	 * @throws XMPPException
	 * @throws IOException
	 * 
	 * */
	public void uploadUserAvatra(InputStream avatar) throws HealthyException,
			XMPPException, IOException {
		if (!isConnected())
			throw new HealthyException("无效网络连接");
		if (getLoginedUser() == null)
			throw new HealthyException("请先进行登录");
		VCard vcard = new VCard();
		vcard.load(mConnection);
		byte[] bytes = getBytesFromInputStream(avatar);
		vcard.setAvatar(bytes);
		vcard.save(mConnection);
	}

	/*
	 * ====================================私有函数==================================
	 * ====
	 */

	/**
	 * 将输入流转换为byte数组
	 * */
	private byte[] getBytesFromInputStream(InputStream in) {
		byte[] buffer = new byte[1024];
		int len = -1;
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		try {
			while ((len = in.read(buffer)) != -1) {
				outStream.write(buffer, 0, len);
			}
			byte[] result = outStream.toByteArray();
			outStream.close();
			in.close();
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(DEBUG_TAG, "", e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 在初始化连接时，进行一些必要的配置操作
	 * 
	 * */
	private void config(ProviderManager pm) {
		// 自定义IQ
		pm.addIQProvider(ExtensionalIQ.ELEMENT, ExtensionalIQ.NAME_SPACE,
				new ExtensionalIQProvider());
		// User Search
		pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());
		// VCard
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());
		// Data Forms
		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());
	}

	/**
	 * 解析字符串某个节点内的信息
	 * 
	 * @param source
	 *            需要解析的字符串
	 * @param nodeName
	 *            节点名称
	 * */
	private String parseXMLByDOM(String source, String nodeName) {
		try {
			DocumentBuilderFactory domFac = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder domBuilder = domFac.newDocumentBuilder();
			Document doc = domBuilder.parse(new ByteArrayInputStream(source
					.getBytes()));
			Element root = doc.getDocumentElement();
			return root.getElementsByTagName(nodeName).item(0).getTextContent();
		} catch (Exception e) {
			Log.e(DEBUG_TAG, "", e);
		}
		return null;
	}

	private String sendAMessageToServer(String message) throws HealthyException {
		if (!isConnected())
			throw new HealthyException("无效网络连接");
		ExtensionalIQ iq = new ExtensionalIQ();
		iq.setMessage(message);
		iq.setType(IQ.Type.GET);
		iq.setTo(mConnection.getServiceName());
		mConnection.sendPacket(iq);
		PacketFilter filter = new AndFilter(
				new PacketIDFilter(iq.getPacketID()), new PacketTypeFilter(
						IQ.class));
		PacketCollector collector = mConnection.createPacketCollector(filter);
		IQ result = (IQ) collector.nextResult(SmackConfiguration
				.getPacketReplyTimeout());
		collector.cancel();
		if (result == null) {// 请求出现超时
			throw new HealthyException("请求超时");
		} else if (result.getType() == IQ.Type.RESULT) {// 请求执行成功
			return parseXMLByDOM(result.getChildElementXML(), "message");
		} else {
			throw new HealthyException("内部服务器错误");
		}
	}

	/**
	 * 连接到服务器
	 * */
	private boolean connectToServer() {
		ConnectionConfiguration config = new ConnectionConfiguration(
				"192.168.118.188", 5222);
		/* 是否启用安全验证 */
		config.setSASLAuthenticationEnabled(false);
		config.setReconnectionAllowed(true);// 允许自动进行重连
		/* 创建connection链接 */
		try {
			if (mConnection == null)
				mConnection = new XMPPConnection(config);
			mConnection.connect();/* 建立连接 */
			mConnection.addConnectionListener(this);
			mConnection.addPacketListener(this, this);
			Roster.setDefaultSubscriptionMode(SubscriptionMode.manual);// 设定默认好友添加模式，需要经过人工同意
			return true;
		} catch (XMPPException e) {
			Log.e("Healthy", "HealthyUtil.connectToServer():", e);
		}
		return false;
	}

	/**
	 * 判断是否已经建立连接，若没有则重新建立连接。
	 * */
	private boolean isConnected() {
		if (mConnection == null || !mConnection.isConnected()) {
			if (!connectToServer()) {
				return false;
			}
		}
		return true;
	}

	/*
	 * ====================================接口====================================
	 * ==
	 */

	/**
	 * Notification that the connection was closed normally or that the
	 * reconnection process has been aborted.
	 * */
	@Override
	public void connectionClosed() {
		// TODO Auto-generated method stub
		Log.i(DEBUG_TAG, "Healthy与服务端的连接已被正常关闭");
		mConnection = null;
	}

	/**
	 * Notification that the connection was closed due to an exception.
	 * */
	@Override
	public void connectionClosedOnError(Exception arg0) {
		// TODO Auto-generated method stub
		Log.e(DEBUG_TAG, "", arg0);
		connectToServer();// 重新连接服务器
	}

	/**
	 * The connection will retry to reconnect in the specified number of
	 * seconds.
	 * */
	@Override
	public void reconnectingIn(int arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * An attempt to connect to the server has failed.
	 * */
	@Override
	public void reconnectionFailed(Exception arg0) {
		// TODO Auto-generated method stub
		Log.e(DEBUG_TAG, "", arg0);
	}

	/**
	 * The connection has reconnected successfully to the server.
	 * */
	@Override
	public void reconnectionSuccessful() {
		// TODO Auto-generated method stub
		Log.i(DEBUG_TAG, "成功完成重连接");
	}

	/**
	 * 处理好友请求信息
	 * */
	@Override
	public void processPacket(Packet packet) {
		// TODO Auto-generated method stub
		Presence presence = (Presence) packet;
		if (presence.getType() == Presence.Type.subscribe) {// 好友添加申请
			for (int i = 0; i < mSubscribePacketListeners.size(); i++)
				mSubscribePacketListeners.get(i).processPacket(packet);
		} else if (presence.getType() == Presence.Type.unsubscribe) {// 好友删除申请
			RosterEntry entry = mConnection.getRoster().getEntry(
					packet.getFrom());
			if (entry != null) {
				try {
					mConnection.getRoster().removeEntry(entry);
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					Log.e(DEBUG_TAG, "", e);
				}
			}
			// 发送确认消息
			Presence unsubscription = new Presence(Presence.Type.unsubscribed);
			unsubscription.setTo(presence.getFrom());
			mConnection.sendPacket(unsubscription);
		} else if(presence.getType() == Presence.Type.subscribed){//对方已接受好友添加申请反馈信息
			Log.i("tag", "得到好友的确认信息");
			String[] messageFrom = packet.getFrom().split("@");
			String mMessageFrom = messageFrom[0];
			Log.i("tag", "得到好友的确认信息"+messageFrom[0]);
			establishFriendRequest(mMessageFrom);
			
		}
	}

	/**
	 * PacketFilter
	 * 
	 * 仅允许添加朋友的请求包通过
	 * */
	@Override
	public boolean accept(Packet packet) {
		// TODO Auto-generated method stub
		if (packet instanceof Presence) {
			Presence presence = (Presence) packet;
			// Request subscription to recipient's presence.
			if (presence.getType() == Presence.Type.subscribe || // 好友添加申请
					presence.getType() == Presence.Type.unsubscribe||// 好友删除申请
						presence.getType() == Presence.Type.subscribed)//已接受好友添加申请的反馈信息
				return true;
		}
		return false;
	}

	/**
	 * 处理好友请求接口</br>
	 * 
	 * 相关界面须实现该接口
	 * */
	public static interface SubscribePacketListener {
		public void processPacket(Packet packet);
	}
	
	public void addSubscribePacketListtener(SubscribePacketListener listener) {
		mSubscribePacketListeners.add(listener);
	}
}
