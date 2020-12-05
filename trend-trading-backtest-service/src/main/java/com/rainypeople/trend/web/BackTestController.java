package com.rainypeople.trend.web;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.rainypeople.trend.pojo.IndexData;
import com.rainypeople.trend.pojo.Profit;
import com.rainypeople.trend.service.BackTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class BackTestController {

    @Autowired
    BackTestService backTestService;

    @GetMapping("/simulate/{code}/{startDate}/{endDate}")
    @CrossOrigin
    public Map<String,Object> backtest(@PathVariable("code")String code,
                                       @PathVariable("startDate")String strStartDate,
                                       @PathVariable("endDate")String strEndDate)throws Exception{
        List<IndexData> allIndexDatas = backTestService.listIndexData(code);

        String indexStartDate = allIndexDatas.get(0).getDate();
        String indexEndDate = allIndexDatas.get(allIndexDatas.size() - 1).getDate();

        //过滤日期，只需要大于等于开始日期，小于等于结束日期的数据
        allIndexDatas = filterByDateRange(allIndexDatas,strStartDate, strEndDate);

        //moving average移动均线,20天的
        int ma=20;
        //抛出阈值
        float sellSate=0.95f;
        //购买阈值
        float buySate=1.05f;
        //手续费
        float serviceCharge=0f;

        //模拟服务
        Map<String,?> simulateResult=backTestService.simulate(ma,sellSate,buySate,serviceCharge,allIndexDatas);
        //从map集合中取出所有的利润类profits
        List<Profit> profits= (List<Profit>) simulateResult.get("profits");

        Map<String,Object> result=new HashMap<>();
        result.put("indexDatas",allIndexDatas);
        result.put("indexStartDate",indexStartDate);
        result.put("indexEndDate",indexEndDate);
        result.put("profits",profits);
        return result;
    }

    private List<IndexData> filterByDateRange(List<IndexData> allIndexDatas, String strStartDate, String strEndDate) {
        if (StrUtil.isBlankOrUndefined(strStartDate)&&StrUtil.isBlankOrUndefined(strEndDate)){
            return allIndexDatas;
        }
        List<IndexData> result=new ArrayList<>();
        Date startDate = DateUtil.parse(strStartDate);
        Date endDate = DateUtil.parse(strEndDate);

        for (IndexData indexData:allIndexDatas){
            Date date=DateUtil.parse(indexData.getDate());
            if (date.getTime()>=startDate.getTime()&&date.getTime()<=endDate.getTime()){
                result.add(indexData);
            }
        }

        return result;
    }


}
