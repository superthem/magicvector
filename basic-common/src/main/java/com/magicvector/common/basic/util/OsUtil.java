package com.magicvector.common.basic.util;

import java.net.InetAddress;
import java.net.NetworkInterface;

public class OsUtil { 

	public static String getMacAddress() {

		try{
			InetAddress ia = InetAddress.getLocalHost();
			byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < mac.length; i++) {
				if (i != 0) {
					sb.append("-");
				}
				String s = Integer.toHexString(mac[i] & 0xFF);
				sb.append(s.length() == 1 ? 0 + s : s);
			}
			return sb.toString().toUpperCase();
		}
		catch (Exception e){
			return "unknown";
		}

	}
}
