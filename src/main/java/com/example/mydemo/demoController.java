package com.example.mydemo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 */
@RestController
public class demoController {

    String serverIP    = "127.0.0.1";
    String serverPort  = "9000";
    String serverHost  = "localhost";
    String serverUrl   ="/P5HPC";
    String nodeid      = "JSXZM1";
    private String filePath="F:\\MessageDemo\\A3038HY01_REQ.xml";

    @GetMapping("/deal")
    public String deal(){
        String messageBody=getMessageBody(filePath);
        byte[] header=CreateHeader(nodeid,"0",messageBody.length());

        try {
            boolean flag=CommunicationTool.SendAndRecv(serverIP, serverPort, serverHost, serverUrl,nodeid,messageBody);
            //byte[] flag = CommunicationTool.httpSendAndRecv(serverIP, serverPort, serverHost, serverUrl, header, messageBody);
            return "success";
        }
        catch (Exception ex){
            return ex.getMessage();
        }
    }
    @GetMapping("/singIn")
    public String singIn()
    {
        String messageBody=getMessageBody(filePath);
        byte[] header=CreateHeader(nodeid,"1",messageBody.length());

        try {
            boolean flag=CommunicationTool.SendAndRecv(serverIP, serverPort, serverHost, serverUrl,nodeid,messageBody);
            //byte[] flag = CommunicationTool.httpSendAndRecv(serverIP, serverPort, serverHost, serverUrl, header, messageBody);
            return "success";
        }
        catch (Exception ex){
            return ex.getMessage();
        }
    }

    private byte[] CreateHeader(String nodeid,String singType,int messageLen){
        byte[] header=new byte[20];
        String str=nodeid+singType+"000000000";
        byte[] bytes=str.getBytes();
        System.arraycopy(bytes,0,header,0,bytes.length);
        byte[] mesLength=intToByte(messageLen);
        System.arraycopy(mesLength,0,header,16,4);
        return header;
    }

    private String formatNum(int number)
    {
        String str = String.format("%04d",number);
        return str;
    }

    private byte[] intToByte(int val) {
        byte[] b = new byte[4];

        NetTool.DATA2NET(val,b);

        return b;
    }

    private String getMessageBody(String filePath){
        try
        {
            return FileTool.File2String(filePath);
        }
        catch (Exception e)
        {
            return null;
        }

    }
}
