package org.chun.spring.boot.redis.lua.test.service;

import org.chun.spring.boot.redis.lua.RedisLua;
import org.chun.spring.boot.redis.lua.test.domin.User;
import org.springframework.stereotype.Service;

@Service
public class TestService {
	@RedisLua(lua="return redis.call('get','u:'..KEYS[1])",keysCount=1)
	public User say(String id){
		return null;
	}
}
