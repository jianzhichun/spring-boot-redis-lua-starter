package org.chun.spring.boot.redis.lua.config;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.chun.spring.boot.redis.lua.RedisLua;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cglib.core.SpringNamingPolicy;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.cglib.proxy.Proxy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.util.Assert;

@Configuration
@EnableConfigurationProperties(RedisLuaConfig.class)
public class RedisLuaBeanFactoryPostProcessor implements BeanFactoryPostProcessor, ApplicationContextAware {

	private static Logger logger = LoggerFactory.getLogger(RedisLuaBeanFactoryPostProcessor.class);

	private ApplicationContext applicationContext;
	
	@SuppressWarnings("rawtypes")
	private static RedisTemplate redisTemplate;
	
    private static RedisLuaConfig redisLuaConfig;

	@SuppressWarnings("rawtypes")
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		redisTemplate = (RedisTemplate) applicationContext.getBean("redisTemplate");
		redisLuaConfig = applicationContext.getBean(RedisLuaConfig.class);
//		Assert.notEmpty(redisLuaConfig.getPackageArr());
		Assert.notNull(redisTemplate);
		this.applicationContext = applicationContext;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		Scanner scanner = new Scanner((BeanDefinitionRegistry) beanFactory);
		scanner.setResourceLoader(this.applicationContext);
		scanner.scan("org.chun.spring.boot.redis.lua");
	}

	public final static class Scanner extends ClassPathBeanDefinitionScanner {
		public Scanner(BeanDefinitionRegistry registry) {
			super(registry);
		}

		public void registerDefaultFilters() {
			this.addIncludeFilter(new AnnotationTypeFilter(RedisLua.class));
		}

		public Set<BeanDefinitionHolder> doScan(String... basePackages) {
			Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
			for (BeanDefinitionHolder holder : beanDefinitions) {
				GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();
				definition.getPropertyValues().add("innerClassName", definition.getBeanClassName());
				definition.setBeanClass(RedisLuaFactoryBean.class);
			}
			return beanDefinitions;
		}

		public boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
			return beanDefinition.getMetadata().hasAnnotation(RedisLua.class.getName());
		}
	}

	public static class RedisLuaFactoryBean<T> implements InitializingBean, FactoryBean<T> {

		private String innerClassName;

		public void setInnerClassName(String innerClassName) {
			this.innerClassName = innerClassName;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public T getObject() throws Exception {
			Class innerClass = Class.forName(innerClassName);
			if (innerClass.isInterface()) {
				return (T) InterfaceProxy.newInstance(innerClass);
			} else {
				Enhancer enhancer = new Enhancer();
				enhancer.setSuperclass(innerClass);
				enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
				enhancer.setCallback(new MethodInterceptorImpl());
				return (T) enhancer.create();
			}
		}

		@Override
		public Class<?> getObjectType() {
			try {
				if(null == innerClassName)
					return null;
				return Class.forName(innerClassName);
			} catch (ClassNotFoundException e) {
				logger.debug(e.getMessage());
			}
			return null;
		}

		@Override
		public boolean isSingleton() {
			return true;
		}

		@Override
		public void afterPropertiesSet() throws Exception {
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static Object invocation(Object proxy, Method method, Object[] arguments, MethodProxy methodProxy)
			throws Throwable {
		if (!method.isAnnotationPresent(RedisLua.class)) {
			if(Modifier.isAbstract(method.getModifiers()))
				return null;
			return null != methodProxy ? methodProxy.invokeSuper(proxy, arguments) : method.invoke(proxy, arguments);
		}
		RedisLua redisLua = method.getDeclaredAnnotation(RedisLua.class);
		if (!redisLua.replace()) {
			// TODO
		}
		Class<?> resultType = method.getReturnType();
		// keys
		int keysCount = redisLua.keysCount();
		List<Object> keys = new ArrayList<Object>(keysCount);
		Collections.addAll(keys, Arrays.copyOfRange(arguments, 0, keysCount));
		// args
		int argsCount = redisLua.argsCount();
		Object[] args = (argsCount > 0 && keysCount + 1 < arguments.length)
				? Arrays.copyOfRange(arguments, keysCount + 1, argsCount + keysCount + 1) : new Object[] {};
		Object result = redisTemplate.execute(new DefaultRedisScript(redisLua.lua(), resultType), keys, args);
		return result;
	}

	public static class InterfaceProxy implements InvocationHandler {

		public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
			return invocation(proxy, method, arguments, null);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public static <T> T newInstance(Class<T> innerInterface) {
			ClassLoader classLoader = innerInterface.getClassLoader();
			Class[] interfaces = new Class[] { innerInterface };
			InterfaceProxy proxy = new InterfaceProxy();
			return (T) Proxy.newProxyInstance(classLoader, interfaces, proxy);
		}
	}

	public static class MethodInterceptorImpl implements MethodInterceptor {
		public Object intercept(Object proxy, Method method, Object[] objects, MethodProxy methodProxy)
				throws Throwable {
			return invocation(proxy, method, objects, methodProxy);
		}
	}
}
