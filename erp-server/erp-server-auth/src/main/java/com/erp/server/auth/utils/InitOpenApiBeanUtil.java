package com.erp.server.auth.utils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import com.erp.server.auth.config.OpenApi;

import lombok.Data;


@Component
public class InitOpenApiBeanUtil implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(InitOpenApiBeanUtil.class);

    protected ApplicationContext applicationContext;

    private final ConcurrentHashMap<String, GatewayBaseInfo> gatewayMap = new ConcurrentHashMap<>();


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        applicationContext=event.getApplicationContext();
        //索引请求路由
        initGateway();
    }

    private void initGateway(){
        try{
            Collection<Object> collection = applicationContext.getBeansWithAnnotation(OpenApi.class).values();
            for (Object object : collection){
                Class<?> clz = object.getClass();
                Class<?> userType = ClassUtils.getUserClass(clz);
                // 只对当前类bean处理，父类忽略
                Method[] methodArr=userType.getDeclaredMethods();
                for(Method method : methodArr){
                    OpenApi openApi = AnnotationUtils.findAnnotation(method, OpenApi.class);
                    if (openApi!=null && StringUtils.isNoneBlank(openApi.value())){
                        GatewayBaseInfo value =gatewayMap.get(openApi.value());
                        if (null == value){
                            // 避免检查
                            method.setAccessible(true);
                            gatewayMap.put(openApi.value(), new GatewayBaseInfo(method, object));
                        }
                    }
                }
            }
        }catch (Exception e){
            logger.error("",e);
        }
    }

    public Map<String, GatewayBaseInfo> getGatewayMap() {
        return gatewayMap;
    }


    public <T> T getBeanByName(String beanName, Class<T> requiredType) {
        return applicationContext.getBean(beanName, requiredType);
    }



    @Data
    public static class GatewayBaseInfo{
        private Method method;
        private Object gatewayClass;
        public GatewayBaseInfo(Method method, Object gatewayClass){
            this.method =method;
            this.gatewayClass =gatewayClass;
        }

    }

}
