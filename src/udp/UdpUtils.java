package udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import bean.BaseMsg.MsgType;
import bean.TargetInfo;
import bean.UdpMsg;
import listener.UdpServerListener;
import utils.CharsetUtil;

/**
 * UDP工具类
 * 
 * @author Administrator
 *
 */
public class UdpUtils {
	// UDP配置
	protected UdpServerConfig mUdpServerConfig;
	protected List<UdpServerListener> mUdpServerListeners;
	private DatagramSocket datagramSocket;
	private SendThread sendThread;// 发送线程
	private ReceiveThread receiverThread;// 接收线程
	private static UdpUtils client;

	private UdpUtils() {
		super();
	}

	public static UdpUtils getUdpClient() {

		if (client == null) {
			client = new UdpUtils();
			client.init();
		}
		return client;
	}

	/**
	 * 配置初始化
	 */
	private void init() {
		mUdpServerListeners = new ArrayList<>();
		mUdpServerConfig = new UdpServerConfig.Builder().create();
	}

	/**
	 * 关闭Socket
	 */
	public void closeSocket() {
		if (datagramSocket != null && datagramSocket.isConnected()) {
			datagramSocket.disconnect();
			datagramSocket = null;
		}
	}

	/**
	 * 开启服务Socket，等待接收信息
	 */
	public void startUdpServer() {
		if (!getReceiveThread().isAlive()) {
			getReceiveThread().start();
			System.out.println("服务开启");
		}
	}

	/**
	 * 停止Socket服务
	 */
	public void stopUdpServer() {
		getReceiveThread().interrupt();
	}

	/**
	 * Socket线程是否开启
	 * 
	 * @return
	 */
	public boolean isUdpServerRuning() {
		return getReceiveThread().isAlive();
	}

	/**
	 * 发送信息
	 * 
	 * @param msg
	 */
	public void sendMsg(UdpMsg msg) {
		if (!getSendThread().isAlive()) {// 开启发送线程
			getSendThread().start();
		}
		getSendThread().enqueueUdpMsg(msg);
	}

	/**
	 * 获取发送线程
	 * 
	 * @return
	 */
	private SendThread getSendThread() {
		if (sendThread == null || !sendThread.isAlive()) {
			sendThread = new SendThread();
		}
		return sendThread;
	}

	/**
	 * 获取接收线程
	 * 
	 * @return
	 */
	private ReceiveThread getReceiveThread() {
		if (receiverThread == null || !receiverThread.isAlive()) {
			receiverThread = new ReceiveThread();
		}
		return receiverThread;
	}

	/**
	 * 获取UDP Socket
	 * 
	 * @return
	 */
	private DatagramSocket getDatagramSocket() {
		if (datagramSocket != null) {
			return datagramSocket;
		}
		synchronized (new Object()) {// 同步
			if (datagramSocket != null) {
				return datagramSocket;
			}
			int localPort = mUdpServerConfig.getLocalPort();
			try {
				if (localPort > 0) {
					datagramSocket = UdpSocketManager.getUdpSocket(localPort);// 从Map集合中查找
					if (datagramSocket == null) {
						datagramSocket = new DatagramSocket(localPort);
						UdpSocketManager.putUdpSocket(datagramSocket);
					}
				} else {
					datagramSocket = new DatagramSocket();
				}
				datagramSocket.setSoTimeout((int) mUdpServerConfig.getReceiveTimeout());
			} catch (SocketException e) {
				e.printStackTrace();
				datagramSocket = null;
			}
			return datagramSocket;
		}
	}

	/**
	 * 消息发送线程
	 * 
	 * @author Administrator
	 *
	 */
	private class SendThread extends Thread {

		// 消息发送阻塞队列， 是线程安全的，实现了先进先出等特性
		private LinkedBlockingQueue<UdpMsg> msgQueue;
		private UdpMsg sendingMsg;

		// 获取阻塞线程队列
		protected LinkedBlockingQueue<UdpMsg> getMsgQueue() {
			if (msgQueue == null) {
				msgQueue = new LinkedBlockingQueue<>();
			}
			return msgQueue;
		}

		protected SendThread setSendingMsg(UdpMsg sendingMsg) {
			this.sendingMsg = sendingMsg;
			return this;
		}

		public UdpMsg getSendingMsg() {
			return this.sendingMsg;
		}

		// 消息排队
		public boolean enqueueUdpMsg(final UdpMsg tcpMsg) {
			if (tcpMsg == null || getSendingMsg() == tcpMsg || getMsgQueue().contains(tcpMsg)) {
				return false;
			}
			try {
				getMsgQueue().put(tcpMsg);
				return true;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		public void run() {
			UdpMsg msg;
			if (getDatagramSocket() == null) {
				return;
			}
			try {
				while (!Thread.interrupted() && (msg = getMsgQueue().take()) != null) {
					setSendingMsg(msg);// 设置正在发送的
					byte[] data = msg.getSourceDataBytes();
					if (data == null) {// 根据编码转换消息
						data = CharsetUtil.stringToData(msg.getSourceDataString(), mUdpServerConfig.getCharsetName());
					}
					if (data != null && data.length > 0) {
						TargetInfo mTargetInfo = msg.getTarget();
						DatagramPacket packet = new DatagramPacket(data, data.length,
								new InetSocketAddress(mTargetInfo.getIp(), mTargetInfo.getPort()));
						try {
							msg.setTime();
							datagramSocket.send(packet);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 接收信息线程
	 * 
	 * @author Administrator
	 *
	 */
	private class ReceiveThread extends Thread {
		@Override
		public void run() {
			if (getDatagramSocket() == null) {
				return;
			}
			byte[] buff = new byte[1024];
			DatagramPacket pack = new DatagramPacket(buff, buff.length);
			while (!Thread.interrupted()) {
				try {
					getDatagramSocket().receive(pack);
					byte[] res = Arrays.copyOf(buff, pack.getLength());
					UdpMsg udpMsg = new UdpMsg(res, new TargetInfo(pack.getAddress().getHostAddress(), pack.getPort()),
							MsgType.Receive);
					udpMsg.setTime();
					String msgstr = CharsetUtil.dataToString(res, mUdpServerConfig.getCharsetName());
					udpMsg.setSourceDataString(msgstr);
					// 接收完成产生通知
					notifyReceiveListener(udpMsg);
				} catch (IOException e) {
					if (!(e instanceof SocketTimeoutException)) {// 不是超时报错

					}
				}
			}
		}
	}

	/**
	 * 接收完成通知
	 * 
	 * @param msg
	 */
	private void notifyReceiveListener(final UdpMsg msg) {
		for (UdpServerListener l : mUdpServerListeners) {
			final UdpServerListener listener = l;
			if (listener != null) {
				listener.onReceive(UdpUtils.this, msg);
			}

		}
	}

	public void config(UdpServerConfig udpClientConfig) {
		mUdpServerConfig = udpClientConfig;
	}

	public void addUdpClientListener(UdpServerListener listener) {
		if (mUdpServerListeners.contains(listener)) {
			return;
		}
		this.mUdpServerListeners.add(listener);
	}

	public void removeUdpClientListener(UdpServerListener listener) {
		this.mUdpServerListeners.remove(listener);
	}

	@Override
	public String toString() {
		return "XUdp{" + "datagramSocket=" + datagramSocket + '}';
	}
}
