package com.rainypeople.trend;

import brave.sampler.Sampler;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.tomcat.jni.Thread;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@SpringBootApplication
@EnableEurekaClient
public class TrendTradingBackTestViewApplication {

    public static void main(String[] args) {
        int port=0;
        int defaultPort=8041;
        int eurekaServerPort=8761;
        int configServerPort = 8060;

        if(NetUtil.isUsableLocalPort(configServerPort)) {
            System.err.printf("检查到端口%d 未启用，判断 配置服务器 没有启动，本服务无法使用，故退出%n", configServerPort );
            System.exit(1);
        }
        if (NetUtil.isUsableLocalPort(eurekaServerPort)){
            System.err.printf("检查到端口%d 未启用，判断 eureka 服务器没有启动，本服务无法使用，故退出%n", eurekaServerPort);
            System.exit(1);
        }

        if (null!=args&&0!=args.length){
            for (String arg:args){
                if (arg.startsWith("port=")){
                    String strPort = StrUtil.subAfter(arg, "port=", true);
                    if (NumberUtil.isNumber(strPort)){
                        port= Convert.toInt(strPort);
                    }
                }
            }
        }

        if (0==port){
            Future<Integer> future= ThreadUtil.execAsync(()->{
                int p=0;
                System.out.printf("请于5秒钟内输入端口号, 推荐  %d ,超过5秒将默认使用 %d %n ",defaultPort,defaultPort);
                Scanner scanner=new Scanner(System.in);
                while(true){
                    String strPort = scanner.nextLine();
                    if (!NumberUtil.isNumber(strPort)){
                        System.out.println("只能输入数字");
                        continue;
                    }else {
                        p=Convert.toInt(strPort);
                        scanner.close();
                        break;
                    }
                }
                return p;
            });
            try {
                port=future.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e){
                port=defaultPort;
            }
        }
        new SpringApplicationBuilder(TrendTradingBackTestViewApplication.class).properties("server.port="+port).run(args);
    }
    @Bean
    public Sampler defaultSampler() {
        return Sampler.ALWAYS_SAMPLE;
    }
}
