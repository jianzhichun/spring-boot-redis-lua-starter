package org.chun.spring.boot.redis.query.config;


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
import org.chun.spring.boot.redis.query.RedisQuery;
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
@EnableConfigurationProperties(RedisQueryConfig.class)
public class RedisQueryAutoConfiguration extends AbstractPointcutAdvisor {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private Pointcut pointcut;
    private Advice advice;
	
	@Autowired
    private RedisQueryConfig redisQueryConfig;
    
	@Autowired
    private RedisTemplate redisTemplate;
    
    @PostConstruct
    public void init() {
        logger.info("init RedisQueryAutoConfiguration");
        this.pointcut = new AnnotationMatchingPointcut(null, RedisQuery.class);
        this.advice = new CloudsQueryMethodInterceptor();
        logger.info("finish RedisQueryAutoConfiguration");
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
			RedisQuery redisQuery = method.getDeclaredAnnotation(RedisQuery.class);
			if(!redisQuery.replace()){
				//TODO
			}
			Class<?> resultType = method.getReturnType();
			Object[] arguments = invocation.getArguments();
			//keys
			int keysCount = redisQuery.keysCount();
			List<Object> keys = new ArrayList<Object>(keysCount);
			Collections.addAll(keys, Arrays.copyOfRange(arguments, 0, keysCount));
			//args
			int argsCount = redisQuery.argsCount();
			Object[] args = ( argsCount == 0 && keysCount < arguments.length -1 ) ? 
					null : Arrays.copyOfRange(arguments, keysCount + 1, argsCount + keysCount + 1);
			Object result = redisTemplate.execute(new DefaultRedisScript(redisQuery.lua(), resultType), keys, args);
	        return result;
		}
		
	}
}
