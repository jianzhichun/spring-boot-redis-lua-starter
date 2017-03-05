package org.chun.spring.boot.redis.query.config;


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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("serial")
@Configuration
@EnableConfigurationProperties(RedisQueryConfig.class)
public class RedisQueryAutoConfiguration extends AbstractPointcutAdvisor {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private Pointcut pointcut;
    private Advice advice;
	
    @Autowired
    private RedisQueryConfig redisQueryConfig;
    
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
			System.out.println(redisQueryConfig);
			return null;
		}
		
	}
}
