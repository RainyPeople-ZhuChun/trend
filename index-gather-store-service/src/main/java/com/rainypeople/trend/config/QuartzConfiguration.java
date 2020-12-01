package com.rainypeople.trend.config;

import com.rainypeople.trend.job.IndexDataSyncJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfiguration {
    //间隔一分钟执行一次，这儿定义的是常量，后面用来注入
    private static final int interval = 1;

    @Bean
    //定义一个JobDetail
    public JobDetail weatherDataSyncJobDetail() {
        //指定干活的类IndexDataSyncJob
        return JobBuilder.newJob(IndexDataSyncJob.class)
                //定义名称和所属的组（没有组）
                .withIdentity("indexDataSyncJob")
                .storeDurably().build();
    }

    @Bean
    //触发器
    public Trigger weatherDataSyncTrigger() {
        //每隔一分钟执行一次
        SimpleScheduleBuilder schedBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMinutes(interval).repeatForever();

        return TriggerBuilder.newTrigger().forJob(weatherDataSyncJobDetail())
                .withIdentity("indexDataSyncTrigger").withSchedule(schedBuilder).build();
    }
}
