package listener;

import bean.UdpMsg;
import udp.UdpUtils;

/**
 * UDP服务端监听器
 * 
 * @author Administrator
 *
 */
public interface UdpServerListener {

	// 服务启动
	void onStarted(UdpUtils XUdp);

	// 服务停止
	void onStoped(UdpUtils XUdp);

	// 发送消息
	void onSended(UdpUtils XUdp, UdpMsg udpMsg);

	// 接收信息
	void onReceive(UdpUtils client, UdpMsg udpMsg);

	// 系统出错
	void onError(UdpUtils client, String msg, Exception e);

}
