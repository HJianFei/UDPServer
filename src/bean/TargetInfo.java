package bean;

import utils.StringValidationUtils;

/**
 * 发送源的IP地址以及端口号
 * 
 * @author Administrator
 *
 */
public class TargetInfo {
	private String ip;
	private int port;

	public TargetInfo(String ip, int port) {
		this.ip = ip;
		this.port = port;
		check();
	}

	private void check() {
		if (!StringValidationUtils.validateRegex(port + "", StringValidationUtils.RegexPort)) {
			System.out.println("port 格式不合法");
		}
	}

	@Override
	public String toString() {
		return "TargetInfo{" + "ip='" + ip + '\'' + ", port='" + port + '\'' + '}';
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
