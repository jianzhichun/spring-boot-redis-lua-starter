package org.chun.spring.boot.redis.query;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RedisQuery {
	
	/**
	 * The lua code to redis
	 * @return
	 */
	String lua() default "";
	
	/**
	 * True will not invoke java method
	 * <p>default is false
	 * @return
	 */
	boolean replace() default false;
	
}
