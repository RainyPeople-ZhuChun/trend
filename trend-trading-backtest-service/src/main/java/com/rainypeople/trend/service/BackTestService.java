package com.rainypeople.trend.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.rainypeople.trend.client.IndexDataClient;
import com.rainypeople.trend.pojo.AnnualProfit;
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

        //盈利交易次数
        int winCount=0;
        //总盈利比率
        float totalWinRate=0;
        //平均盈利比率
        float avgWinRate=0;
        //亏损交易次数
        int lossCount=0;
        //总亏损比率
        float totalLossRate=0;
        //平均亏损比率
        float avgLossRate=0;

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

                        //获取总盈利比率和盈利交易次数
                        if (trade.getSellClosePoint()>trade.getBuyClosePoint()){
                            totalWinRate += (trade.getSellClosePoint()-trade.getBuyClosePoint())/trade.getBuyClosePoint();
                            winCount++;

                        //获取总亏损比率和亏损次数
                        }else {
                            totalLossRate += (trade.getSellClosePoint()-trade.getBuyClosePoint())/trade.getBuyClosePoint();
                            lossCount++;
                        }
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

        avgWinRate=totalWinRate/winCount;
        avgLossRate=totalLossRate/lossCount;

        //获取相关年收益类的信息
        List<AnnualProfit> annualProfits = caculateAnnualProfits(allIndexDatas, profits);

        Map<String,Object> result=new HashMap<>();
        result.put("profits",profits);
        result.put("trades",trades);

        result.put("winCount", winCount);
        result.put("lossCount", lossCount);
        result.put("avgWinRate", avgWinRate);
        result.put("avgLossRate", avgLossRate);

        result.put("annualProfits", annualProfits);
        return result;
    }

    private List<AnnualProfit> caculateAnnualProfits(List<IndexData> allIndexDatas, List<Profit> profits) {
        List<AnnualProfit> result=new ArrayList<>();
        String strStatDate = allIndexDatas.get(0).getDate();
        String strEndDate = allIndexDatas.get(allIndexDatas.size()-1).getDate();
        Date startDate=DateUtil.parse(strStatDate);
        DateTime endDate = DateUtil.parse(strEndDate);
        int startYear = DateUtil.year(startDate);
        int endYear = DateUtil.year(endDate);
        for (int year=startYear;year<=endYear;year++){
            AnnualProfit annualProfit=new AnnualProfit();
            annualProfit.setYear(year);
            float indexIncome = getIndexIncome(year,allIndexDatas);
            float trendIncome = getTrendIncome(year,profits);
            annualProfit.setIndexIncome(indexIncome);
            annualProfit.setTrendIncome(trendIncome);
            result.add(annualProfit);
        }
        return result;
    }

    private float getTrendIncome(int year, List<Profit> profits) {
        Profit first=null;
        Profit last=null;
        for (Profit profit:profits){
            String strDate = profit.getDate();
            int currentYear = getYear(strDate);
            if (year==currentYear){
                if (null==first){
                    first=profit;
                }
                last=profit;
            }
        }
        return (last.getValue()-first.getValue())/first.getValue();
    }


    private float getIndexIncome(int year, List<IndexData> allIndexDatas) {
        //获取数据中年份开始的第一天数据
        IndexData first=null;
        //数据中年份结束的最后一天
        IndexData last=null;
        for (IndexData indexData:allIndexDatas){
            String strDate = indexData.getDate();
            Date date=DateUtil.parse(strDate);
            //取得当前指数数据的年份
            int currentYear=getYear(strDate);
            if(currentYear==year){
                if (null==first){
                    //取得数据中年份开始的一天指数数据
                    first=indexData;
                }
                //结尾的一天指数数据
                last=indexData;
            }
        }
        return (last.getClosePoint()-first.getClosePoint())/first.getClosePoint();
    }

    private int getYear(String strDate) {
        String strYear= StrUtil.subBefore(strDate,"-",false);
        return Convert.toInt(strYear);
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
