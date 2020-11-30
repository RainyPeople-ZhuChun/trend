package com.rainypeople.trend.service;

import com.rainypeople.trend.pojo.Index;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class IndexService {
    private List<Index> indexes;

    @Autowired
    RestTemplate restTemplate;

    public List<Index> fecth_indexes_from_third_part(){
        List<Map<String,String>> temp= restTemplate.getForObject("http://127.0.0.1:8090/indexes/codes.json", List.class);
        return map2Index(temp);
    }

    private List<Index> map2Index(List<Map<String, String>> temp) {
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
}
