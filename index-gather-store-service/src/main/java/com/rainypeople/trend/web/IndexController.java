package com.rainypeople.trend.web;

import com.rainypeople.trend.pojo.Index;
import com.rainypeople.trend.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class IndexController {

    @Autowired
    IndexService indexService;

    @GetMapping("getCodes")
    public List<Index> get(){
        return  indexService.fecth_indexes_from_third_part();
    }

}
