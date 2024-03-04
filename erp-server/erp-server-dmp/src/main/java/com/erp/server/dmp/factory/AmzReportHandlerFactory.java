package com.erp.server.dmp.factory;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.core.exception.ServiceException;
import com.erp.sdk.oms.amz.spapi.enums.AmazonReportRecordTypeEnum;
import com.erp.server.dmp.handler.report.AmzReportBusinessHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 亚马逊报告处理工厂
 *
 * @author Jim
 * @date 2024/1/24
 */
@Component
public class AmzReportHandlerFactory {


    private static ApplicationContext applicationContext;

    @Resource
    public void setApplicationContext(ApplicationContext applicationContext) {
        AmzReportHandlerFactory.applicationContext = applicationContext;
    }


    /**
     * 根据报告类型创建Handler
     */
    public static AmzReportBusinessHandler createHandler(String recordType) {
        AmazonReportRecordTypeEnum recordTypeEnum = AmazonReportRecordTypeEnum.checkAndGetByRecordType(recordType);
        String amzReportBusinessHandlerName = recordTypeEnum.getAmzReportBusinessHandlerName();
        if (StringUtils.isBlank(amzReportBusinessHandlerName)) {
            throw new ServiceException("枚举配置异常,业务处理类名称amzReportBusinessHandlerName不能为空,recordTypeEnum=" + recordTypeEnum.name());
        }
        return applicationContext.getBean(amzReportBusinessHandlerName, AmzReportBusinessHandler.class);
    }
}