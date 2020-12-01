package com.rainypeople.trend.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.rainypeople.trend.pojo.IndexData;
import com.rainypeople.trend.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@CacheConfig(cacheNames = "index_datas")
public class IndexDataService {

    @Autowired
    RestTemplate restTemplate;

    Map<String, List<IndexData>> indexDatas=new HashMap<>();

    @HystrixCommand(fallbackMethod = "third_part_not_connected")
    public List<IndexData> fresh(String code){
        List<IndexData> indexeDatas = fetch_indexes_from_third_part(code);

        indexDatas.put(code, indexeDatas);

        System.out.println("code:"+code);
        System.out.println("indexeDatas:"+indexDatas.get(code).size());

        IndexDataService indexDataService = SpringContextUtil.getBean(IndexDataService.class);

        indexDataService.remove(code);

        return indexDataService.store(code);
    }


    @CacheEvict(key = "'indexData-code-'+#p0")
    public void remove(String code){

    }

    @Cacheable(key = "'indexDate-code-'+#p0")
    public List<IndexData> get(String code){
        return CollUtil.toList();
    }

    @CachePut(key = "'indexData-code-'+#p0")
    public List<IndexData> store(String code){
        return indexDatas.get(code);
    }

    public List<IndexData> fetch_indexes_from_third_part(String code){
        List<Map<String,String>> temp = restTemplate.getForObject("http://127.0.0.1:8090/indexes/" + code + ".json", List.class);
        return map2IndexData(temp);
    }

    public List<IndexData> map2IndexData(List<Map<String, String>> temp) {
        List<IndexData> indexDatas=new ArrayList<>();
        for (Map<String,String> map:temp){
            String date = map.get("date").toString();
            float closePoint= Convert.toFloat(map.get("closePoint"));
            IndexData indexData=new IndexData();
            indexData.setDate(date);
            indexData.setClosePoint(closePoint);
            indexDatas.add(indexData);
        }
        return indexDatas;
    }

    public List<IndexData> third_part_not_connected(String code){
        System.out.println("third_part_not_connected()");
        IndexData indexData=new IndexData();
        indexData.setDate("n/a");
        indexData.setClosePoint(0);
        return CollectionUtil.toList(indexData);
    }
}
