package com.example.mydemo.Controller;

import com.example.mydemo.Model.ServConf;
import com.example.mydemo.Until.CommunicationTool;
import com.example.mydemo.Until.FileTool;
import com.example.mydemo.Until.NetTool;
import com.example.mydemo.Until.SecureTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 */
@RestController
public class demoController {

    @Autowired
    private ServConf conf;
    @PostMapping("/httpSend")
    public String httpSend(String nodeid,String singType,String message){
        byte[] header=CreateHeader(nodeid,singType,message.length());
        byte[] messageBody=encrypt(message);
        try {
            //boolean flag= CommunicationTool.SendAndRecv(conf.serverIP, conf.serverPort, conf.serverHost, conf.serverUrl,conf.nodeid,messageBody);
            byte[] flag = CommunicationTool.httpSendAndRecv(conf.serverIP, conf.serverPort, conf.serverHost, conf.serverUrl, header, messageBody);
            return "success";
        }
        catch (Exception ex){
            return ex.getMessage();
        }
    }
    @PostMapping("/socketSend")
    public String socketSend(String nodeid,String singType,String message)
    {
        try {
            return CommunicationTool.SendAndRecv(conf.serverIP, conf.serverPort, conf.serverHost, conf.serverUrl,conf.nodeid,message);
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

    private byte[] encrypt(String message){
        SecureTool secureTool = new SecureTool();
        try {
            return secureTool.encrypt(message.getBytes());
        }
        catch (Exception ex)
        {
            return null;
        }
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
