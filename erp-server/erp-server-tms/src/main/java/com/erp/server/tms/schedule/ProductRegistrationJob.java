package com.erp.server.tms.schedule;

import com.erp.server.tms.service.ProductRegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @author Lambda
 * @Classname ProductRegistrationJob
 * @Description 备案产品 job
 * @Date 2024-01-19 12:22
 * @Created by yl
 */
@Component
@Slf4j
@EnableScheduling
public class ProductRegistrationJob {

    @Resource
    private ProductRegistrationService productRegistrationService;


    /**
     * 同步产品备案信息
     */
    public void syncProductRegistrationInfo() {
        //todo
        List<String> skuNoList= Arrays.asList();
        productRegistrationService.listBySkuNoList(skuNoList);
    }
}
