package utils;

import java.io.UnsupportedEncodingException;

public class CharsetUtil {
	public static final String UTF_8 = "UTF-8";
	public static final String GBK = "GBK";

	/**
	 * 数据转字节
	 * 
	 * @param string
	 * @param charsetName
	 * @return
	 */
	public static byte[] stringToData(String string, String charsetName) {
		if (string != null) {
			try {
				return string.getBytes(charsetName);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 数据转字符串
	 * 
	 * @param data
	 * @param charsetName
	 * @return
	 */
	public static String dataToString(byte[] data, String charsetName) {
		if (data != null) {
			try {
				return new String(data, charsetName);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}