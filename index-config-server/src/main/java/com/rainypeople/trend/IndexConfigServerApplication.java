package com.rainypeople.trend;

import cn.hutool.core.util.NetUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient
@EnableConfigServer
public class IndexConfigServerApplication {
    public static void main(String[] args) {
        int port=8060;

        int eurekaServerPort=8761;
        //检测端口8761是否可用，如果可用表示eurekaserver未启用
        if (NetUtil.isUsableLocalPort(eurekaServerPort)){
            System.err.printf("检查到端口%d 未启用，判断 eureka 服务器没有启动，本服务无法使用，故退出%n", eurekaServerPort);
            //1或者其他正整数表示非正常退出
            System.exit(1);
        }

        if (!NetUtil.isUsableLocalPort(port)){
            System.err.printf("端口 %d 被占用，故退出%n",port);
            System.exit(1);
        }
        new SpringApplicationBuilder(IndexConfigServerApplication.class).properties("server.port="+port).run(args);
    }
}
