package org.chun.spring.boot.redis.lua.test.domin;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, String> {
	User findByName(String name);
}
