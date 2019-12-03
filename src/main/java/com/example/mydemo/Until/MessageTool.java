package com.example.mydemo.Until;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MessageTool {
	public static String getSignReqMessage(){
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		Calendar calendar = Calendar.getInstance();
		
		String signReqMessage = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
				"<message>\n"+
				"  <head>\n"+
				"    <LH_VERSION>100</LH_VERSION>\n"+
				"    <LH_TYPE>0200</LH_TYPE>\n"+
				"    <LH_REQ_SYS_NO>000000M1</LH_REQ_SYS_NO>\n"+ //实际分配的接入系统编码
				"    <LH_RES_SYS_NO>A3031</LH_RES_SYS_NO>\n"+
				"    <LH_FORWARD_FLAG>0</LH_FORWARD_FLAG>\n"+
				"    <LH_REQUEST_DATE>"+df.format(calendar.getTime()).substring(0,8)+"</LH_REQUEST_DATE>\n"+
				"    <LH_REQUEST_TIME>"+df.format(calendar.getTime()).substring(8,14)+"</LH_REQUEST_TIME>\n"+
				"    <LH_REQUEST_FLOW_NO>A3038BDC"+Long.toString(new Date().getTime())+"</LH_REQUEST_FLOW_NO>\n"+
				"    <LH_TRAN_CD>A3038BDC</LH_TRAN_CD>\n"+
				"    <LH_PROV_NO>999999</LH_PROV_NO>\n"+  //实际省的行政区划代码
				"    <LH_CITY_NO>999999</LH_CITY_NO>\n"+  //实际城市的行政区划代码
				"  </head>\n"+
				"</message>\n";

		if(System.getProperty("os.name").indexOf("windows")>=0)
			return signReqMessage.replaceAll("\n", "\r\n");
		else
			return signReqMessage;
	}
	
	public static String getSignRetMessage(){
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		Calendar calendar = Calendar.getInstance();

		String signRetMessage = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
				"<message>\n"+
				"  <head>\n"+
				"    <LH_VERSION>100</LH_VERSION>\n"+
				"    <LH_TYPE>0210</LH_TYPE>\n"+
				"    <LH_REQ_SYS_NO>000000M1</LH_REQ_SYS_NO>\n"+  //实际分配的接入系统编码
				"    <LH_RES_SYS_NO>A3031</LH_RES_SYS_NO>\n"+
				"    <LH_FORWARD_FLAG>0</LH_FORWARD_FLAG>\n"+
				"    <LH_REQUEST_DATE>"+df.format(calendar.getTime()).substring(0,8)+"</LH_REQUEST_DATE>\n"+      //实际取请求报文中的字段值返回
				"    <LH_REQUEST_TIME>"+df.format(calendar.getTime()).substring(8,14)+"</LH_REQUEST_TIME>\n"+     //实际取请求报文中的字段值返回
				"    <LH_REQUEST_FLOW_NO>A3038BDC"+Long.toString(new Date().getTime())+"</LH_REQUEST_FLOW_NO>\n"+ //实际取请求报文中的字段值返回
				"    <LH_RESPONSE_STATUS>00</LH_RESPONSE_STATUS>\n"+
				"    <LH_RESPONSE_CODE>000000000000</LH_RESPONSE_CODE>\n"+
				"    <LH_RESPONSE_MSG>签到成功</LH_RESPONSE_MSG>\n"+
				"  </head>\n"+
				"  <body>\n"+
				"    <HandKey>%s</HandKey>\n"+
				"  </body>\n"+
				"</message>\n";

		if(System.getProperty("os.name").indexOf("windows")>=0)
			return signRetMessage.replaceAll("\n", "\r\n");
		else
			return signRetMessage;
	}
}
