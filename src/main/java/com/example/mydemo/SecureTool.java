package com.example.mydemo;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

public class SecureTool {
	private String secureType = null;
	private String sessionKeyPath = null;
	private byte[] sessionkey = null;

	private static BouncyCastleProvider bouncyCastleProvider = null;

    public static synchronized BouncyCastleProvider getInstance() {
        if (bouncyCastleProvider == null) {
            bouncyCastleProvider = new BouncyCastleProvider();
            Security.addProvider(bouncyCastleProvider);
        }
        return bouncyCastleProvider;
    }

    /**
     * 加载sessionkey等信息
     * @param secureType
     * @param nodeid
     * @return
     * @throws Exception
     */
    public boolean updateKeyInfo(String secureType,String nodeid) throws Exception{
    	this.secureType = secureType;

    	//确定密钥文件的路径
    	String publicKeyPath  = String.format("%s/key",nodeid);
		String sessionKeyPath = String.format("%s/sessionKey",nodeid);
		if(new File(publicKeyPath).exists()==false)
		{
			publicKeyPath = String.format("%s\\conf\\key\\%s\\320300M1.publickey",System.getenv("JAVA_HOME"),nodeid);
			sessionKeyPath = String.format("%s\\conf\\key\\%s\\sessionKey",System.getenv("JAVA_HOME"),nodeid);
			if(new File(publicKeyPath).exists()==false)
			{
				System.out.println("无法找到可用的公钥文件："+String.format("%s/key",nodeid));
				return false;
			}
		}

    	//公钥
    	RSAPublicKey publickey = loadPublicKey(publicKeyPath);
    	if(new File(sessionKeyPath).exists())
    	{
    		//加密后的会话密钥
			byte[] sm4EncryptByte = getRSAKeyByte(sessionKeyPath);
	    	//公钥解密
	    	sessionkey = rsaDecrypt(publickey,sm4EncryptByte);
    	}

    	this.sessionKeyPath = sessionKeyPath;
    	return true;
    }


	/**
	 * 获取RSA公钥的keysize
	 * @param publicKey
	 * @return
	 * @throws Exception
	 */
	private int getKeySize(RSAPublicKey publicKey) throws Exception {
		String algorithm = publicKey.getAlgorithm(); // 获取算法
		KeyFactory keyFact = KeyFactory.getInstance(algorithm);
		RSAPublicKeySpec keySpec = (RSAPublicKeySpec) keyFact.getKeySpec(publicKey, RSAPublicKeySpec.class);
		BigInteger prime = keySpec.getModulus();
		return prime.toString(2).length(); // 转换为二进制，获取公钥长度
	}



	/**
	 * 获取RSA密钥的内容
	 * @param key
	 * @return
	 * @throws Exception
	 */
	private byte[] getRSAKeyByte(String key) throws Exception {
		InputStream in = new FileInputStream(key);
		InputStreamReader is = new InputStreamReader(in);
		BufferedReader br = new BufferedReader(is);
		String readLine = null;
		StringBuilder sb = new StringBuilder();
		while ((readLine = br.readLine()) != null) {
			if (readLine.charAt(0) == '-') {
				continue;
			} else {
				sb.append(readLine);
				sb.append('\r');
			}
		}
		br.close();

		return Base64.decode(sb.toString());
	}

	/**
	 * 加载RSA的公钥
	 * @param publicKey
	 * @return
	 * @throws Exception
	 */
	private RSAPublicKey loadPublicKey(String publicKey) throws Exception {
		try{
			byte[] buffer = getRSAKeyByte(publicKey);

			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
			return (RSAPublicKey) keyFactory.generatePublic(keySpec);

		} catch (NoSuchAlgorithmException e) {
			throw new Exception("无此算法");
		} catch (InvalidKeySpecException e) {
			throw new Exception("公钥非法");
		} catch (IOException e) {
			throw new Exception("公钥数据内容读取错误");
		} catch (NullPointerException e) {
			throw new Exception("公钥数据为空");
		}
	}

	/**
	 * 会话密钥解密
	 * @param publicKey
	 * @param cipherByte
	 * @return
	 * @throws Exception
	 */
	private byte[] rsaDecrypt(RSAPublicKey publicKey, byte[] cipherByte) throws Exception {
		if (publicKey == null)
			throw new Exception("解密公钥为空, 请设置");

		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("RSA/ECB/NOPadding",SecureTool.getInstance());
			cipher.init(Cipher.DECRYPT_MODE, publicKey);
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			int keysize = getKeySize(publicKey);
			int blocksize = keysize / 8;

			byte[] cache;
			int inputLen = cipherByte.length;
			for (int index = 0; index < inputLen; index = index + blocksize) {
				if (index + blocksize > inputLen)
					cache = cipher.doFinal(cipherByte, index, inputLen - index);
				else
					cache = cipher.doFinal(cipherByte, index, blocksize);

				out.write(cache, 0, cache.length);
			}
			byte[] decryptedData = out.toByteArray();
			out.close();

			return decryptedData;
		} catch (NoSuchAlgorithmException e) {
			throw new Exception("无此解密算法");
		} catch (NoSuchPaddingException e) {
			System.out.println(e.getMessage());
			return null;
		} catch (InvalidKeyException e) {
			throw new Exception("解密私钥非法,请检查");
		} catch (IllegalBlockSizeException e) {
			throw new Exception("密文长度非法");
		} catch (BadPaddingException e) {
			throw new Exception("密文数据已损坏");
		}
	}

	/**
	 * 获取解密后的sessionKey
	 * @return
	 */
	public void printSessionKey(){
		byte[] sslKey = null;
		if(sessionkey==null) return;

		if(secureType.equalsIgnoreCase("guomiSM"))
		{
			sslKey = new byte[16];
			System.arraycopy(sessionkey, 0, sslKey, 0, sslKey.length);
		}
		else return;

		NetTool.printEncMessage("sessionKey",sslKey,sslKey.length);
	}

	/**
	 * 获取报文类型
	 * @param messageFile
	 * @return
	 */
	public String getSignType(String messageFile){
		String singType = "1";
		if(secureType.equalsIgnoreCase("guomiSM"))
			singType = messageFile.contains("BDC") ? "1" : "0";
		return singType;
	}

	/**
	 * 判断是不是签到报文
	 * @param messageFile
	 * @return
	 */
	public boolean isSignType(String messageFile){
		if(secureType.equalsIgnoreCase("guomiSM") && messageFile.contains("BDC")) return true;
		return false;
	}

	public byte[] encrypt(byte[] plainByte) throws Exception {
		return sm4Encrypt(plainByte);
	}

	public byte[] decrypt(byte[] cipherByte) throws Exception {
		return sm4Decrypt(cipherByte);
	}

	/**
	 * SM4加密
	 * @param plainByte
	 * @return
	 * @throws Exception
	 */
	private byte[] sm4Encrypt(byte[] plainByte) throws Exception {
		byte[] sm4Key = new byte[16];
		System.arraycopy(sessionkey, 0, sm4Key, 0, sm4Key.length);

		//SM4补位过程
		byte[] message = new byte[(plainByte.length/16+1)*16];
		System.arraycopy(plainByte, 0, message, 0, plainByte.length);

		//SM4加密过程
		SecretKeySpec key = new SecretKeySpec(sm4Key, "SM4");
		Cipher cipher = Cipher.getInstance("SM4/ECB/NOPadding",SecureTool.getInstance());
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(message);
	}

	/**
	 * SM4解密
	 * @param cipherByte
	 * @return
	 * @throws Exception
	 */
	private byte[] sm4Decrypt(byte[] cipherByte) throws Exception {
		byte[] sm4Key = new byte[16];
		System.arraycopy(sessionkey, 0, sm4Key, 0, sm4Key.length);

		//SM4解密
		SecretKeySpec key = new SecretKeySpec(sm4Key, "SM4");
		Cipher cipher = Cipher.getInstance("SM4/ECB/NOPadding",SecureTool.getInstance());
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(cipherByte);
	}

	/**
	 * 写入sessionKey
	 * @param handKey
	 * @return
	 * @throws Exception
	 */
	public boolean saveHandKey(String recvMessage) throws Exception
	{
		int index1=0,index2=0;
		index1 = recvMessage.indexOf("<HandKey>") + "<HandKey>".length();
		index2 = recvMessage.indexOf("</HandKey>",index1);

		if(index1>0 && index2>index1)
		{
			String handKey = recvMessage.substring(index1, index2);
			return FileTool.String2File(sessionKeyPath, handKey);
		}
		return false;
	}

	//////////////////////////////////////私钥相关操作代码//////////////////////////////////////
	/**
	 * 获取RSA私钥的keysize
	 *
	 * @param privateKey
	 * @return
	 * @throws Exception
	 */
	private int getKeySize(RSAPrivateKey privateKey) throws Exception {
		String algorithm = privateKey.getAlgorithm(); // 获取算法
		KeyFactory keyFact = KeyFactory.getInstance(algorithm);
		RSAPrivateKeySpec keySpec = (RSAPrivateKeySpec) keyFact.getKeySpec(privateKey, RSAPrivateKeySpec.class);
		BigInteger prime = keySpec.getModulus();
		return prime.toString(2).length(); // 转换为二进制，获取公钥长度
	}

	/**
	 * 加载RSA的私钥
	 *
	 * @param privateKey
	 * @return
	 * @throws Exception
	 */
	private RSAPrivateKey loadPrivateKey(String privateKey) throws Exception {
		try {
			byte[] buffer = getRSAKeyByte(privateKey);

			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
		} catch (NoSuchAlgorithmException e) {
			throw new Exception("无此算法");
		} catch (InvalidKeySpecException e) {
			throw new Exception("私钥非法");
		} catch (IOException e) {
			throw new Exception("私钥数据内容读取错误");
		} catch (NullPointerException e) {
			throw new Exception("私钥数据为空");
		}
	}

	/**
	 * RSA私钥加密
	 *
	 * @param privateKey
	 * @param plainText
	 * @return
	 * @throws Exception
	 */
	private byte[] rsaEncrypt(RSAPrivateKey privateKey, byte[] plainByte) throws Exception {
		if (privateKey == null)
			throw new Exception("加密私钥为空, 请设置");

		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding",SecureTool.getInstance());
			cipher.init(Cipher.ENCRYPT_MODE, privateKey);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int keysize = getKeySize(privateKey);
			int blocksize = keysize / 8 - 11;

			// 对数据分段加密
			byte[] cache;
			int inputLen = plainByte.length;
			for (int index = 0; index < inputLen; index = index + blocksize) {
				if (index + blocksize > inputLen)
					cache = cipher.doFinal(plainByte, index, inputLen - index);
				else
					cache = cipher.doFinal(plainByte, index, blocksize);
				out.write(cache, 0, cache.length);
			}

			byte[] encryptedData = out.toByteArray();
			out.close();

			return Base64.encode(encryptedData);
		} catch (NoSuchAlgorithmException e) {
			throw new Exception("无此加密算法");
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			return null;
		} catch (InvalidKeyException e) {
			throw new Exception("加密公钥非法,请检查");
		} catch (IllegalBlockSizeException e) {
			throw new Exception("明文长度非法");
		} catch (BadPaddingException e) {
			throw new Exception("明文数据已损坏");
		}
	}

	/**
	 * 获取SM4的加密密钥
	 * @return
	 * @throws Exception
	 */
	private byte[] sm4GenKey() throws Exception {
		char basecode[] = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890~;<@#:>%^".toCharArray();
        String key = "";
        Random random = new Random();
        for (int index = 0; index < 16; index++) {
        	key = key + basecode[random.nextInt(basecode.length)];
        }
        return key.getBytes();
	}

	/**
	 * 获取加密后的会话密钥
	 * @param nodeid
	 * @return
	 * @throws Exception
	 */
	public String getHandKey(String secureType,String nodeid) throws Exception {
    	//确定密钥文件的路径
    	String privateKeyPath = String.format("%s/privateKey",nodeid);
    	String sessionKeyPath = String.format("%s/sessionKey",nodeid);
		if(new File(privateKeyPath).exists()==false)
		{
			privateKeyPath = String.format("%s/conf/key/%s/privateKey",System.getenv("HOME"),nodeid);
			sessionKeyPath = String.format("%s/conf/key/%s/sessionKey",System.getenv("HOME"),nodeid);
			if(new File(privateKeyPath).exists()==false)
			{
				System.out.println("无法找到可用的公钥文件："+String.format("%s/privateKey",nodeid));
				return null;
			}
		}

		//加载rsa的私钥
		RSAPrivateKey privatekey = loadPrivateKey(privateKeyPath);
		byte[] sm4Key = rsaEncrypt(privatekey,sm4GenKey());

		//会话密钥加密返回
		String handKey = new String(Base64.encode(sm4Key));
		FileTool.String2File(sessionKeyPath, handKey);

		return handKey;
	}
}