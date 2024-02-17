package com.example.wiki.service;

import com.example.wiki.domain.Test;
import com.example.wiki.mapper.TestMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TestService {
    /**
     * Resource是JDK自带的
     * Autowired是Spring自带的
     * 两者都可以
     */
    @Resource
    private TestMapper testMapper;

    public List<Test> list(){
        return testMapper.list();
    }
}
