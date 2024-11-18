package com.erp.server.sys.controller.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.utils.RedisUtil;
import com.common.core.controller.BaseController;
import com.erp.model.sys.entity.DictBasicEntity;
import com.erp.server.sys.service.DictBasicService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Slf4j
@RestController
@RequestMapping("/webVersion")
public class SysWebVersionController extends BaseController implements CommandLineRunner {

	private final static String WEB_VERSION_REDISKEY = "web:version:package";
	
	private final static String WEB_VERSION_TYPE = "webVersionPackage";
	
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private DictBasicService dictBasicService;
    
    private volatile String webVersion = "";
    
    @CrossOrigin
	@GetMapping(value = "/update")
    public String update(@RequestParam(value = "isDeteleToken" , required = false) Boolean isDeteleToken) throws Exception {
    	int count = 0;
		while(!dealUpdate(isDeteleToken)) {
			if(count >= 30) {
				return "update web version fail";
			}
			count = count + 1;
			Thread.sleep(1000);
		}
        return "update web version success";
    }
    
    @CrossOrigin
	@RequestMapping(value = "/sse" , produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> getEvents() {
        return Flux.interval(Duration.ofSeconds(2))
                .map(sequence -> ServerSentEvent.<String> builder()
                        .id(String.valueOf(sequence))
                        .event("message")
                        .data(getWebVersion())
                        .build());
    }
    
    private boolean dealUpdate(Boolean isDeteleToken) {
    	try {
			redisUtil.del(WEB_VERSION_REDISKEY);
			dictBasicService.lambdaUpdate().eq(DictBasicEntity::getType, WEB_VERSION_TYPE).setSql(" value = value::int + 1 ").update();
			redisUtil.del(WEB_VERSION_REDISKEY);
			if(isDeteleToken != null && isDeteleToken) {
				Collection<String> keys = redisUtil.keys(RedisCacheConstants.LOGIN_TOKEN_KEY + "*");
				if(CollUtil.isNotEmpty(keys)) {
					redisUtil.del(keys.toArray(new String[] {}));
				}
			}
			webVersion = "";
		} catch (Exception e) {
			log.error("更新前端打包版本失败" , e);
			return false;
		}
    	return true;
    }
    
    private String getWebVersion() {
    	int min = 0;
    	int max = 31;
    	int randomNumber = RandomUtil.randomInt(min, max);
    	while(StringUtils.isBlank(webVersion) && min < randomNumber) {
    		min = min + 1;
    		for(int i = 0;i < max - randomNumber;i++);
    	}
    	if(StringUtils.isBlank(webVersion)) {
    		Integer version = 1;
        	Object object = redisUtil.get(WEB_VERSION_REDISKEY);
        	if(object != null) {
        		version = Integer.valueOf(object.toString());
        	}else {
        		List<DictBasicEntity> list = dictBasicService.lambdaQuery().eq(DictBasicEntity::getType, WEB_VERSION_TYPE).select(DictBasicEntity::getValue).list();
        		if(CollUtil.isNotEmpty(list)) {
        			DictBasicEntity dictBasicEntity = list.get(0);
        			String value = dictBasicEntity.getValue();
        			if(StringUtils.isNotBlank(value)) {
        				try {
    						version = Integer.valueOf(value);
    					} catch (NumberFormatException e) {
    						dictBasicService.lambdaUpdate().eq(DictBasicEntity::getType, WEB_VERSION_TYPE).set(DictBasicEntity::getValue, version.toString()).update();	
    					}
        			}else {
        				dictBasicService.lambdaUpdate().eq(DictBasicEntity::getType, WEB_VERSION_TYPE).set(DictBasicEntity::getValue, version.toString()).update();	
        			}
        		}else {
        			DictBasicEntity dictBasicEntity = new DictBasicEntity();
        			dictBasicEntity.setType(WEB_VERSION_TYPE);
        			dictBasicEntity.setName("当前版本");
        			dictBasicEntity.setValue(version.toString());
        			dictBasicService.save(dictBasicEntity);
        		}
        		redisUtil.set(WEB_VERSION_REDISKEY, version.toString() , 120L + randomNumber);
        	}
        	webVersion = version.toString();
    	}
    	return webVersion;
    }

	@Override
	public void run(String... args) throws Exception {
		Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> webVersion = "", 0, 5, TimeUnit.SECONDS);
	}
}
