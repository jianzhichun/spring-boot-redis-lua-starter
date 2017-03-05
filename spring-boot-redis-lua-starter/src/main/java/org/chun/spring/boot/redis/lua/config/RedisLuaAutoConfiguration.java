package org.chun.spring.boot.redis.lua.config;


import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.chun.spring.boot.redis.lua.RedisLua;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.ScriptExecutor;


@SuppressWarnings({"serial","rawtypes","unchecked","unused"})
@Configuration
@EnableConfigurationProperties(RedisLuaConfig.class)
public class RedisLuaAutoConfiguration extends AbstractPointcutAdvisor {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private Pointcut pointcut;
    private Advice advice;
	
	@Autowired
    private RedisLuaConfig redisLuaConfig;
    
	@Autowired
    private RedisTemplate redisTemplate;
    
    @PostConstruct
    public void init() {
        this.pointcut = new AnnotationMatchingPointcut(null, RedisLua.class);
        this.advice = new CloudsQueryMethodInterceptor();
    }
    
    @Override
	public Pointcut getPointcut() {
		return this.pointcut;
	}

	@Override
	public Advice getAdvice() {
		return this.advice;
	}
	
	
	class CloudsQueryMethodInterceptor implements MethodInterceptor{
		
		@Override
		public Object invoke(MethodInvocation invocation) throws Throwable {
			if(null == redisTemplate){
				logger.warn("RedisTemplate is not exist, will do method logic");
				return invocation.proceed();
			}
			Method method = invocation.getMethod();
			RedisLua redisLua = method.getDeclaredAnnotation(RedisLua.class);
			if(!redisLua.replace()){
				//TODO
			}
			Class<?> resultType = method.getReturnType();
			Object[] arguments = invocation.getArguments();
			//keys
			int keysCount = redisLua.keysCount();
			List<Object> keys = new ArrayList<Object>(keysCount);
			Collections.addAll(keys, Arrays.copyOfRange(arguments, 0, keysCount));
			//args
			int argsCount = redisLua.argsCount();
			Object[] args = ( argsCount > 0 && keysCount + 1 < arguments.length ) ? 
					Arrays.copyOfRange(arguments, keysCount + 1, argsCount + keysCount + 1) : new Object[]{};
			Object result = redisTemplate.execute(new DefaultRedisScript(redisLua.lua(), resultType), keys, args);
			return result;
		}
		
	}
}
