package com.example.mydemo;

import java.io.Console;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

public class CommunicationTool {
    private static String secureType = "guomiSM";

    public static boolean SendAndRecv(String serverIP,String serverPort,String serverHost,String serverUrl,String nodeid,String messageFile) throws Exception
    {
        //获取会话密钥，设置加解密参数
        SecureTool secureTool = new SecureTool();
        if(secureTool.updateKeyInfo(secureType,nodeid) == false) return false;
        secureTool.printSessionKey();
        if(messageFile.equalsIgnoreCase("overall")) return true;//如果只是打印密钥，则直接返回

        //交易类型标记 0:正常 1:签到
        String singType = secureTool.getSignType(messageFile);
        System.out.printf("secureType=[%s] serverIP=[%s] serverPort=[%s] nodeId=[%s]\n",secureType,serverIP,serverPort,nodeid);

        //原始报文，直接从文件读取
        byte[] messageBody = null;
        if(singType.equalsIgnoreCase("0")) //需要加密
        {
            byte[] messageByte = FileTool.File2Bytes(messageFile);
            System.out.printf("原始报文message=[%d] [%s]\n",messageByte.length,new String(messageByte,"UTF-8"));
            messageBody = secureTool.encrypt(messageByte); //加密
            NetTool.printEncMessage("加密后的报文",messageBody,messageBody.length);
        }
        else
        {
            String signMessage = MessageTool.getSignReqMessage();
            System.out.printf("原始报文message=[%d] [%s]\n",signMessage.length(),signMessage);
            messageBody = signMessage.getBytes();
        }
        byte[] messageHead = NetTool.getMessageHead(nodeid,singType,messageBody.length);

        //调用发送
        byte[] recvByte = httpSendAndRecv(serverIP,serverPort,serverHost,serverUrl,messageHead,messageBody);
        if(recvByte==null) return false;

        //重新获取加解密标志
        singType = NetTool.getSingTypeFromHead(messageHead);
        int recvLen = NetTool.getRecvLenFromHead(messageHead);
        System.out.printf("\n接收报文长度[%d]\n", recvLen);

        //解析返回报文
        String messageFile1 = String.format("%s.%s",messageFile,Long.toString(new Date().getTime()));
        if(singType.equalsIgnoreCase("0")) //需要解密
        {
            NetTool.printEncMessage("接收到的密文",recvByte,recvByte.length);
            messageBody = secureTool.decrypt(recvByte); //解密

            String recvMessage = new String(messageBody, "UTF-8");
            System.out.printf("已解密报文[%d]=[%s]\n", recvMessage.length(), recvMessage);
            FileTool.String2File(messageFile1,recvMessage);
            System.out.printf("已解密报文已写入文件[%s]\n\n",messageFile1);
        }
        else
        {
            messageBody = recvByte;
            String recvMessage = new String(messageBody, "UTF-8");
            System.out.printf("未加密报文[%d]=[%s]\n", recvLen, recvMessage);
            FileTool.String2File(messageFile1,recvMessage);
            System.out.printf("未加密报文已写入文件[%s]\n\n",messageFile1);

            //如果为明文，但是不是签到报文则直接返回
            if(secureTool.isSignType(messageFile)==false) return true;
            boolean saveFlag = secureTool.saveHandKey(recvMessage);
            if (saveFlag)
                System.out.printf("会话密钥更新成功\n\n");
            else
                System.out.printf("会话密钥更新失败\n\n");
        }
        return true;
    }

    //HTTP方式发送和接收数据
    public static byte[] httpSendAndRecv(String serverIP,String serverPort,String serverHost,String serverUrl,byte[] messageHead,byte[] messageBody) throws Exception
    {
        String httpurl = String.format("http://%s:%s%s",serverIP,serverPort,serverUrl);
        URL url = new URL(httpurl);
        // 建立连接
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");//取消http头部设置值的限制
        URLConnection conn = url.openConnection();
        // 设置通用的请求头
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("User-Agent", "P5HPC");
        conn.setRequestProperty("Content-Type", "text/html");
        conn.setRequestProperty("Content-Length", String.format("%d", messageHead.length+messageBody.length));
        conn.setRequestProperty("Host", serverHost);
        conn.setRequestProperty("Cache-Control", "no-cache");
        conn.setRequestProperty("Connection", "close");
        // 发送POST请求必须设置
        conn.setDoOutput(true);
        conn.setDoInput(true);

        OutputStream out = conn.getOutputStream();
        out.write(messageHead); //发送报文头
        out.write(messageBody); //发送报文体
        out.flush();
        out.close();

		/*//判断是否是chunked编码数据
		boolean isCkunked = false;
	    Map<String, List<String>> map = conn.getHeaderFields();
		for (String key : map.keySet())
		{
		    for(String value:map.get(key))
		    {
		    	//if(key == null)
		    	//	System.out.println(value);
		    	//else
		    	//	System.out.println(key+": "+value);

		    	if("Transfer-Encoding".equalsIgnoreCase(key))
		    		isCkunked = value.equalsIgnoreCase("chunked");
		    }
		}*/

        //接收http报文体数据 先接收20位报文头
        InputStream in = conn.getInputStream();
        int totalBytesRcvd = in.read(messageHead, 0, 20);//接收
        if(totalBytesRcvd<20) throw new Exception("接收长度不足20位: " + totalBytesRcvd);

        int onceLen = 0;
        int recvLen = NetTool.getRecvLenFromHead(messageHead);
        byte[] recvByte = new byte[recvLen];//接收数据缓冲

        totalBytesRcvd = 0;
        while(totalBytesRcvd!=recvLen)
        {
            onceLen = in.read(recvByte, totalBytesRcvd, recvLen-totalBytesRcvd);//分段接收
            if(onceLen==-1) break;
            totalBytesRcvd += onceLen;
        }
        in.close();

        //报文接收不全
        if(totalBytesRcvd!=recvLen)
        {
            System.out.printf(String.format("报文实际返回长度[%d]与指定长度[%d]不符\n\n", totalBytesRcvd,recvLen));
            return null;
        }
        return recvByte;
    }
}
