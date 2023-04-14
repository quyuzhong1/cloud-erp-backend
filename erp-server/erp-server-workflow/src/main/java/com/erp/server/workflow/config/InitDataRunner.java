package com.erp.server.workflow.config;

import com.common.core.utils.EnumCacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @Classname InitDataRunner
 * @Description 初始化数据
 * @Date 2023-04-14 11:46
 * @Created by zhangchunlin
 */
@Slf4j
@Component
public class InitDataRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //枚举（下拉等等）
        EnumCacheUtils.getInstance().setServiceEnumPackagePath("com.erp.model.workflow.enums");
        try {
            EnumCacheUtils.getInstance().getData();
        } catch (Exception e) {
            log.error("初始化数据异常",e);
        }
    }

}
