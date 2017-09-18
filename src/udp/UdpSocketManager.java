 package udp;

import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;

/**
 * UDP Socket管理工具
 * 
 * @author Administrator
 *
 */
public class UdpSocketManager {

	private static Map<Integer, DatagramSocket> sDatagramSockets = new HashMap<>();

	/**
	 * 把连接放到Map集合中去
	 * 
	 * @param socket
	 */
	public static void putUdpSocket(DatagramSocket socket) {
		sDatagramSockets.put(socket.getLocalPort(), socket);
	}

	/**
	 * 根据端口号获取一个Socket
	 * 
	 * @param port
	 * @return
	 */
	public static DatagramSocket getUdpSocket(int port) {
		return sDatagramSockets.get(port);
	}
}
