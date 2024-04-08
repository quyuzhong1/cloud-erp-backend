package com.erp.server.sys.controller.api;

import java.time.Duration;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.business.utils.RedisUtil;
import com.common.core.controller.BaseController;
import com.erp.model.sys.entity.DictBasicEntity;
import com.erp.server.sys.service.DictBasicService;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;


@Slf4j
@RestController
@RequestMapping("/webVersion")
public class SysWebVersionController extends BaseController {

	private final static String WEB_VERSION_REDISKEY = "web:version:package";
	
	private final static String WEB_VERSION_TYPE = "webVersionPackage";
	
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private DictBasicService dictBasicService;

    @CrossOrigin
	@GetMapping(value = "/update")
    public String update() throws Exception {
    	int count = 0;
		while(!dealUpdate() || count < 30) {
			count = count + 1;
			Thread.sleep(1000);
		}
        return "update web version success";
    }
    
    @CrossOrigin
	@GetMapping(value = "/sse")
    public Flux<ServerSentEvent<String>> getEvents() {
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
    		
    		redisUtil.set(WEB_VERSION_REDISKEY, version.toString());
    	}
    	
    	String versionStr = version.toString();
    	
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> ServerSentEvent.<String> builder()
                        .id(String.valueOf(sequence))
                        .event("message")
                        .data(versionStr)
                        .build());
    }
    
    private boolean dealUpdate() {
    	try {
			redisUtil.del(WEB_VERSION_REDISKEY);
			dictBasicService.lambdaUpdate().eq(DictBasicEntity::getType, WEB_VERSION_TYPE).setSql(" value = value::int + 1 ").update();
			redisUtil.del(WEB_VERSION_REDISKEY);
		} catch (Exception e) {
			log.error("更新前端打包版本失败" , e);
			return false;
		}
    	return true;
    }
    
}
