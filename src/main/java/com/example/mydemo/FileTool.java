package com.example.mydemo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FileTool {
	// 给一个文本文件路径,返回对应的字节数组
	public static byte[] File2Bytes(String filePath) throws Exception {
		File file = new File(filePath);
		FileInputStream fis = new FileInputStream(file);

		byte[] bytes = new byte[fis.available()];
		fis.read(bytes);
		fis.close();
		return bytes;
	}

	// 给一个文本文件路径,返回对应的字符串
	public static String File2String(String filePath) throws Exception {
		byte[] bytes = File2Bytes(filePath);
		return new String(bytes, "UTF-8");
	}

	// 给一个文件路径和一段字符串,将字符串写入路径指定的文件
	public static boolean String2File(String filePath, String content) throws Exception {
		File file = new File(filePath);
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(content.getBytes("UTF-8"));
		fos.close();
		return true;
	}
}
