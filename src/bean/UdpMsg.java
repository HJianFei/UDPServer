package bean;

/**
 * UDP数据接收，发送实体类
 * 
 * @author Administrator
 *
 */
public class UdpMsg extends BaseMsg {

	public UdpMsg(byte[] data, TargetInfo target, MsgType type) {
		super(data, target, type);
	}

	public UdpMsg(String data, TargetInfo target, MsgType type) {
		super(data, target, type);
	}
}
