package com.rainypeople.trend.service;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.rainypeople.trend.client.IndexDataClient;
import com.rainypeople.trend.pojo.IndexData;
import com.rainypeople.trend.pojo.Profit;
import com.rainypeople.trend.pojo.Trade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BackTestService {

    @Autowired
    IndexDataClient indexDataClient;

    public List<IndexData> listIndexData(String code){
        List<IndexData> result = indexDataClient.getIndexData(code);
        Collections.reverse(result);

        /*for (IndexData indexData:result){
            System.out.println(indexData.getDate());
        }*/

        return result;
    }

    public Map<String,Object> simulate(int ma, float sellSate, float buySate, float serviceCharge, List<IndexData> allIndexDatas) {
        List<Profit> profits=new ArrayList<>();
        List<Trade> trades=new ArrayList<>();
        //初始资金
        float initCash= 1000;
        //抛出获得的金额(在买入卖出后的资金)
        float cash= initCash;
        //份额
        float share=0;
        //购买的股票现在的总价值
        float value=0;

        //初始值每股价格
        float init=0;
        if (!allIndexDatas.isEmpty()){
            init=allIndexDatas.get(0).getClosePoint();
        }
        for (int i=0;i<allIndexDatas.size();i++){
            IndexData indexData = allIndexDatas.get(i);
            float closePoint = indexData.getClosePoint();
            //取得平均值
            float avg=getMA(i,ma,allIndexDatas);
            //取得最高点值
            float max=getMax(i,ma,allIndexDatas);
            //上升率
            float increase_sate;
            //回跌率
            float decrease_sate;

            if (0==avg){
                increase_sate=0;
            }
            increase_sate=closePoint/avg;
            if (0==max){
                decrease_sate=0;
            }
            decrease_sate=closePoint/max;

            if (0!=avg){
                //如果上涨率超过购买阈值，买入最大量
                if (increase_sate>buySate){
                    if (0==share){
                        share=cash/closePoint;
                        cash=0;

                        //购买的交易记录
                        Trade trade=new Trade();
                        trade.setBuyDate(indexData.getDate());
                        trade.setBuyClosePoint(indexData.getClosePoint());
                        trade.setSellDate("n/a");
                        trade.setSellClosePoint(0);
                        trades.add(trade);
                    }
                //如果下跌率低于了抛出阈值，全部抛出
                }else if(decrease_sate<sellSate){
                    if (0!=share){
                        cash=share*closePoint*(1-serviceCharge);
                        share=0;

                        //抛出的交易记录
                        //只需要修改前一个创建的交易对象
                        Trade trade=trades.get(trades.size()-1);
                        trade.setSellDate(indexData.getDate());
                        trade.setSellClosePoint(indexData.getClosePoint());
                        //回报率=交易之后的资金/初始资金
                        float rate=cash/initCash;
                        trade.setRate(rate);
                    }
                //如果在两者中间，什么都不做
                }else {

                }
            }

            if (0!=share){
                value=share*closePoint;
            }else {
                value=cash;
            }

            //回报率
            float rate=value/initCash;

            Profit profit=new Profit();
            profit.setDate(indexData.getDate());
            profit.setValue(rate*init);

            System.out.println("profit.value="+profit.getValue());
            profits.add(profit);
        }

        Map<String,Object> result=new HashMap<>();
        result.put("profits",profits);
        result.put("trades",trades);
        return result;
    }

    public float getMax(int i, int ma, List<IndexData> allIndexDatas) {
        //moving average移动均线的天数开始日期，比如ma=20，那么前20天都不会有值，知道21从0开始
        int start=i-1-ma;
        if (start<0){
            start=0;
        }
        //到前一天
        int now=i-1;

        if (start<0){
            return 0;
        }

        float max=0;
        for (int j=start;j<now;j++){
            IndexData indexData = allIndexDatas.get(j);
            float closePoint = indexData.getClosePoint();
            if (closePoint>max){
                max=closePoint;
            }
        }
        return max;
    }

    public float getMA(int i, int ma, List<IndexData> allIndexDatas) {
        //moving average移动均线的天数开始日期，比如ma=20，那么前20天都不会有值，知道21从0开始
        int start=i-1-ma;
        //到前一天
        int now=i-1;

        if (start<0){
            return 0;
        }

        float sum=0;
        float avg=0;
        for (int j=start;j<now;j++){
            IndexData indexData = allIndexDatas.get(j);
            sum += indexData.getClosePoint();
        }
        int dateCount=now-start;
        if (0==dateCount){
            avg=0;
        }
        avg=sum/dateCount;
        return avg;
    }

    public float getYears(List<IndexData> allIndexDatas){
        float years;

        String sDateStart=allIndexDatas.get(0).getDate();
        String sDateEnd=allIndexDatas.get(allIndexDatas.size()-1).getDate();

        Date startDate= DateUtil.parse(sDateStart);
        Date endDate=DateUtil.parse(sDateEnd);

        long days=DateUtil.between(startDate,endDate, DateUnit.DAY);

        years=days/365f;

        return years;
    }
}
