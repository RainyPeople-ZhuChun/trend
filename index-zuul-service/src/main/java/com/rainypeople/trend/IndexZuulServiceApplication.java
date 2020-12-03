package com.rainypeople.trend;

import cn.hutool.core.util.NetUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication
@EnableEurekaClient
@EnableZuulProxy   //开启Zuul的API网关服务功能
@EnableDiscoveryClient  //@EnableDiscoveryClient 可以是其他注册中心。
public class IndexZuulServiceApplication {

    //http://127.0.0.1:8031/api-codes/codes
    public static void main(String[] args) {
        int port=8031;
        if (!NetUtil.isUsableLocalPort(port)){
            System.err.printf("端口%d被占用了，无法启动%n", port );
            //1或者其他的正数表示非正常退出程序，0表示正常退出程序
            System.exit(1);
        }
        new SpringApplicationBuilder(IndexZuulServiceApplication.class).properties("server.port=" + port).run(args);
    }
}
