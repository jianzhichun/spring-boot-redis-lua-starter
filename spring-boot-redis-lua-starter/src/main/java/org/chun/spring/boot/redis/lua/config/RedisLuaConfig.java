package org.chun.spring.boot.redis.lua.config;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "redis.lua")
public class RedisLuaConfig {
	private String scanPackage;

	private String[] packageArr;

	@PostConstruct
	public void init() {
		this.packageArr = StringUtils.split(scanPackage, ";");
	}
	

	public String getScanPackage() {
		return scanPackage;
	}

	public void setScanPackages(String scanPackage) {
		this.scanPackage = scanPackage;
	}

	public String[] getPackageArr() {
		return packageArr;
	}

}
