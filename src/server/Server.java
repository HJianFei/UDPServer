package server;

import bean.BaseMsg.MsgType;
import bean.UdpMsg;
import listener.UdpServerListener;
import udp.UdpServerConfig;
import udp.UdpUtils;

public class Server {

	private static UdpUtils udpUtils = null;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (udpUtils != null && udpUtils.isUdpServerRuning()) {
			udpUtils.stopUdpServer();
		} else {
			if (udpUtils == null) {
				udpUtils = UdpUtils.getUdpClient();
				udpUtils.config(new UdpServerConfig.Builder().setLocalPort(8989).create());
				udpUtils.addUdpClientListener(new UdpServerListener() {

					@Override
					public void onStoped(UdpUtils XUdp) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onStarted(UdpUtils XUdp) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onSended(UdpUtils XUdp, UdpMsg udpMsg) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onReceive(UdpUtils client, UdpMsg udpMsg) {
						// TODO Auto-generated method stub
						System.out.println(udpMsg.toString());
						
						client.sendMsg(new UdpMsg("信息已收到：" + udpMsg.getSourceDataString(), udpMsg.getTarget(), MsgType.Send));
					}

					@Override
					public void onError(UdpUtils client, String msg, Exception e) {
						// TODO Auto-generated method stub

					}
				});
			}
			udpUtils.startUdpServer();
		}

	}

}
