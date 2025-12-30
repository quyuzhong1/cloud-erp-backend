package com.erp.server.dmp.factory;

import com.common.core.exception.ServiceException;
import com.erp.server.dmp.enums.DmpMongoHandleTypeEnum;
import com.erp.server.dmp.handler.DmpMongoHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Mongo业务处理工厂
 *
 * @author Jim
 * @date 2024/1/24
 */
@Component
public class DmpMongoHandlerFactory {


    private static ApplicationContext applicationContext;

    @Resource
    public void setApplicationContext(ApplicationContext applicationContext) {
        DmpMongoHandlerFactory.applicationContext = applicationContext;
    }


    /**
     * 根据处理类型创建Handler
     */
    public static DmpMongoHandler createHandler(DmpMongoHandleTypeEnum handleTypeEnum) {
        String handlerName = handleTypeEnum.getHandlerName();
        if (StringUtils.isBlank(handlerName)) {
            throw new ServiceException("DmpMongoHandleTypeEnum枚举配置异常,业务处理类名称handlerName不能为空,recordTypeEnum=" + handleTypeEnum.name());
        }
        return applicationContext.getBean(handlerName, DmpMongoHandler.class);
    }
}