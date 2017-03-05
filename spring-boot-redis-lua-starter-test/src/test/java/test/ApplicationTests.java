package test;

import org.chun.spring.boot.redis.lua.test.Application;
import org.chun.spring.boot.redis.lua.test.domin.Address;
import org.chun.spring.boot.redis.lua.test.domin.User;
import org.chun.spring.boot.redis.lua.test.domin.UserRepository;
import org.chun.spring.boot.redis.lua.test.service.TestService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;



@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {Application.class})
@SuppressWarnings("unchecked")
public class ApplicationTests {
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	@Test
	public void test() throws Exception {
		stringRedisTemplate.opsForValue().set("aaa", "111");
		Assert.assertEquals("111", stringRedisTemplate.opsForValue().get("aaa"));
    }
	
	@Autowired
	private UserRepository userRepository;
	@Test
	public void test1() throws Exception {
		User u = new User();
		u.setAge(10);
		u.setId("dlb");
		u.setName("dlbbbasdfb");
		Address address = new Address();
		address.setName("a");
//		address.setCity("hahah");
		u.setAddress(address);
		userRepository.save(u);
		Assert.assertEquals(userRepository.findByName("dlbbbasdfb"),u);
	}
	
	@Autowired
	private TestService testService;
	
	@SuppressWarnings("rawtypes")
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Test
	public void test2() throws Exception {
		User u = new User();
		u.setAge(13);
		u.setId("dlb3");
		u.setName("dlb3");
		redisTemplate.opsForValue().set("u:dlb4", u);
		Assert.assertEquals(testService.say("dlb4"),u);
		System.out.println(testService.say1("aaa"));
	}
}
