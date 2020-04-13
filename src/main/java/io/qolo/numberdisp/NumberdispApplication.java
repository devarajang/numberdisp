package io.qolo.numberdisp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;


@SpringBootApplication
public class NumberdispApplication {

	public static void main(String[] args) {
		SpringApplication.run(NumberdispApplication.class, args);
	}

	@Bean
	public StatefulRedisConnection<String, String> redisConnection() {
		RedisClient redisClient = RedisClient.create("redis://localhost:6379/");
        StatefulRedisConnection<String, String> redisConnection = redisClient.connect();
		return redisConnection;
	}

	// @Bean
	// public LettuceConnectionFactory redisConnectionFactory() {
	// 	return new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 6379));
	// }

	// @Bean
	// public RedisTemplate<String, String> appRedisTemplate() {
	// 	RedisTemplate<String, String> template = new RedisTemplate<>();
	// 	template.setConnectionFactory(redisConnectionFactory());
	// 	return template;
	// }

	// @Bean
	// public RedisScript<Integer> script() {
	// 	DefaultRedisScript redisScript = new DefaultRedisScript<>();
	// 	redisScript.setScriptSource(new ResourceScriptSource(new FileSystemResource("C:/codebase/research/numberdisp.lua")));
	// 	redisScript.setResultType(Integer.class);
	// 	return redisScript;			
	// }

}
