package com.example.wiki.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    /**
     * GET,POST,PUT,DELETE
     * 查询，新增，修改，删除
     */
    @RequestMapping("/hello")
    public String hello(){
        return "hello world!";
    }
}
