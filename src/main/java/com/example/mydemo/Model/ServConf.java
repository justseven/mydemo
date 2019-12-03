package com.example.mydemo.Model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component//将Person类注册到容器中
@ConfigurationProperties(prefix = "servconf") //绑定配置文件的值
public class ServConf {
    public String serverIP;
    public String serverPort;
    public String serverHost ;
    public String serverUrl ;
    public String nodeid ;
    public String filePath;
}
