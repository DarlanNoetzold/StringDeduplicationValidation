package com.crawler.web.crawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EnableAsync
public class CrawlerApplication {

	private static ConcurrentHashMap<String,String>crawlerMap = new ConcurrentHashMap<>();

	private static ConcurrentHashMap<String, Future> list = new ConcurrentHashMap<>();


	public static void main(String[] args) {
		SpringApplication.run(CrawlerApplication.class, args);
	}

	public static Map<String, String> getCrawlerMap() {
		return crawlerMap;
	}

	public static ConcurrentHashMap<String,Future> getCrawlerTask() {
		return list;
	}


}
