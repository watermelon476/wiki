package com.example.wiki.config;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

//@ComponentScan({"com.example.wiki","com.test"}) //扫描多个包的写法
@ComponentScan("com.example.wiki")
@SpringBootApplication
@MapperScan("com.example.wiki.mapper")
@EnableScheduling
public class WikiApplication {

	private static final Logger LOG = LoggerFactory.getLogger(WikiApplication.class);

	public static void main(String[] args) {
//		SpringApplication.run(WikiApplication.class, args);
		SpringApplication app = new SpringApplication(WikiApplication.class);
		Environment env = app.run(args).getEnvironment();
		LOG.info("启动成功");
		LOG.info("地址: \thttp://127.0.0.1:{}",env.getProperty("server.port"));
	}


}
