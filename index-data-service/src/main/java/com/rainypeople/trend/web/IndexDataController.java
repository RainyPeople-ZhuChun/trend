package com.rainypeople.trend.web;

import com.rainypeople.trend.pojo.IndexData;
import com.rainypeople.trend.service.IndexDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class IndexDataController {

    @Autowired
    IndexDataService indexDataService;

    //http://127.0.0.1:8021/data/000300

    @GetMapping("data/{code}")
    public List<IndexData> get(@PathVariable(name = "code")String code)throws Exception{
        return indexDataService.get(code);
    }
}
