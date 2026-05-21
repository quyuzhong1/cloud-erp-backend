package com.common.business.controller;


import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.common.business.cache.LocalCache;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;

import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;


/**
 * 本地缓存
 *
 * @author Administrator
 */
@Slf4j
@RestController
@RequestMapping("/localCache")
public class LocalCacheController extends BaseController {

	@Autowired
	private LocalCache localCache;
	
	@PostMapping("getCache")
    public ApiResult<?> getCache(@RequestParam(required = false) String f) {
        Map<String, Object> typeCacheMap = new HashMap<>();
        if(StringUtils.isNotBlank(f)) {
        	try {
				Field field = localCache.getClass().getDeclaredField(f);
				field.setAccessible(true);
				Object object = field.get(localCache);
				field.setAccessible(false);
				typeCacheMap.put(f, object);
			} catch (Exception e) {
				log.error("获取本地缓存错误" , e);
				return ApiResult.error("获取本地缓存错误：" + ExceptionUtil.stacktraceToString(e));
			}
        }
        return success(typeCacheMap);
    }
	
    @PostMapping("initCache")
    public ApiResult<?> initCache() {
        localCache.initCache(false);
        return success();
    }
}
