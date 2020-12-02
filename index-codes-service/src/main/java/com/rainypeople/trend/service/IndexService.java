package com.rainypeople.trend.service;

import cn.hutool.core.collection.CollUtil;
import com.rainypeople.trend.pojo.Index;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames = "indexes")
public class IndexService {
    private List<Index> indexes;

    //直接从Redis获取，没有就返回自定义无效指数代码
    @Cacheable(key = "'all_codes'")
    public List<Index> get(){
        Index index=new Index();
        index.setName("无效指数代码");
        index.setCode("000000");
        indexes.add(index);

        return CollUtil.toList(index);
    }
}
