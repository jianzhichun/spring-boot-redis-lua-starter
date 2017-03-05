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
	 * <p>default is true
	 * <p>false logic need TODO
	 * @return
	 */
	boolean replace() default true;
	
	/**
	 * The KEYS for lua script
	 * <p> KEYS from method-params[0:0+keysCount()]
	 * @return
	 */
	int keysCount() default 0;
	
	/**
	 * The ARGS for lua script
	 * <p> ARGS from method-params[keys()+1:keys()+1+argsCount()]
	 * <p> None if args() == 0
	 * @return
	 */
	int argsCount() default 0;
	
	
}
