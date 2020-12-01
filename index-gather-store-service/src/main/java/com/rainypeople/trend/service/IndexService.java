package com.rainypeople.trend.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.rainypeople.trend.pojo.Index;
import com.rainypeople.trend.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@CacheConfig(cacheNames = "indexes")
public class IndexService {

    private List<Index> indexes;

    @Autowired
    RestTemplate restTemplate;

    //Redis刷新数据
    @HystrixCommand(fallbackMethod = "third_part_not_connected")
    public List<Index> fresh() {
        //从第三方获取数据
        indexes =fecth_indexes_from_third_part();
        IndexService indexService = SpringContextUtil.getBean(IndexService.class);
        //将redis数据库原有数据删除
        indexService.remove();
        //将从第三方获取的数据保存到redis数据库中
        return indexService.store();
    }

    //Redis清空数据
    @CacheEvict(allEntries=true)
    public void remove(){

    }

    //Redis保存数据
    @Cacheable(key="'all_codes'")
    public List<Index> store(){
        System.out.println(this);
        return indexes;
    }

    //从Redis获取数据
    @Cacheable(key="'all_codes'")
    public List<Index> get(){
        return CollUtil.toList();
    }


    public List<Index> fecth_indexes_from_third_part(){
        List<Map<String,String>> temp= restTemplate.getForObject("http://127.0.0.1:8090/indexes/codes.json", List.class);
        return map2Index(temp);
    }

    private List<Index> map2Index(List<Map<String, String>> temp) {
        List<Index> indexes=new ArrayList<>();
        for (Map<String, String> map:temp){
            String code = map.get("code").toString();
            String name = map.get("name").toString();
            Index index=new Index();
            index.setCode(code);
            index.setName(name);
            indexes.add(index);
        }
        return indexes;
    }

    public List<Index> third_part_not_connected(){
        System.out.println("third_part_not_connected()");
        Index index= new Index();
        index.setCode("000000");
        index.setName("无效指数代码");
        return CollectionUtil.toList(index);
    }
}
