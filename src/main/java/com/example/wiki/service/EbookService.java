package com.example.wiki.service;

import com.example.wiki.domain.Ebook;
import com.example.wiki.mapper.EbookMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class EbookService {
    /**
     * Resource是JDK自带的
     * Autowired是Spring自带的
     * 两者都可以
     */
    @Resource
    private EbookMapper ebookMapper;

    public List<Ebook> list(){
        return ebookMapper.selectByExample(null);
    }
}
