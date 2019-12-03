package com.example.mydemo.Model;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "servconf") //绑定配置文件的值

@Getter
@Setter
public class ServConf {
    private String serverIP;
    private String serverPort;
    private String serverHost ;
    private String serverUrl ;
    private String nodeid ;
    private String publicKeyPath;
    private String sessionKeyPath;
}
