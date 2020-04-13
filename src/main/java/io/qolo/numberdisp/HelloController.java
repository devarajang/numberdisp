package io.qolo.numberdisp;

import java.util.Random; 
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import static io.lettuce.core.ScriptOutputType.*;

import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.api.sync.RedisStringCommands;
import io.lettuce.core.api.StatefulRedisConnection;


@RestController
public class HelloController {

    @Autowired
    private StatefulRedisConnection<String, String> redisConnection;

	@RequestMapping("/")
	public String index() {
        RedisCommands<String, String> syncCommands = redisConnection.sync();

        String key = "appName";
        String value = "Number Dispenser";

        syncCommands.set(key, value);
        String response = syncCommands.get(key);
		return "Greetings from " +  response + "!";
	}

    @RequestMapping("/rand")
	public List<Long> rand() {
        RedisCommands<String, String> redis = redisConnection.sync();
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines( Paths.get("C:\\codebase\\research\\numberdisp.lua" ), StandardCharsets.UTF_8)) 
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        redis.scriptFlush();
        
        Random rand = new Random();
       
        String digest = redis.scriptLoad(contentBuilder.toString());
        //redis.evalsha(digest, INTEGER, "one", l)
        System.out.println(contentBuilder.toString());
        
        List<Long> randList = new ArrayList<Long>();
        new java.util.Timer().schedule( 
                new java.util.TimerTask() {
                    long t0 = System.currentTimeMillis();
                    @Override
                    public void run() {
                        if (System.currentTimeMillis() - t0 > 6 * 1000) {
                            cancel();
                        } else {
                            while(true && System.currentTimeMillis() - t0 < 5 * 1000) {
                                long l = rand.nextInt(99999999);
                                randList.add(redis.evalsha(digest, INTEGER, "one", String.valueOf(l)));
                            }
                        }
                        
                    }
                }, 1000);
        Map<String, Integer> map = new HashMap<String, Integer>();
       
       
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int cnt = 0;
         for (Long l : randList) {
            String str = String.valueOf(l);
            String substr = str.substring(0,2);
            Integer count = map.get(substr);
            if(count == null) {
                map.put(substr , 1);
            } else {
                map.put(substr , count+1);
            }
            cnt++;
        }
        System.out.println("Total numbers generated is " + cnt);
        System.out.println(map);
        return randList;
	}

}