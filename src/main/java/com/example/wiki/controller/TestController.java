package com.example.wiki.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    /**
     * GET,POST,PUT,DELETE
     * 查询，新增，修改，删除
     */
    @GetMapping("/hello")
    public String hello(){
        return "hello world!";
    }

    @PostMapping("/hello2")
    public String hello2(String name){
        return "hello world! post " + name;
    }
}
