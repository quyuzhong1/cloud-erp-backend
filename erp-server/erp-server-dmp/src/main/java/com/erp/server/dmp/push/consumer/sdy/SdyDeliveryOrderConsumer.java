package com.erp.server.dmp.push.consumer.sdy;

import com.alibaba.fastjson.JSON;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.erp.server.dmp.push.service.sdy.SdyPushCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 速递云
 */

@Service
@Slf4j
public class SdyDeliveryOrderConsumer {
    @Resource
    private SdyPushCommonService sdyPushCommonService;


    public ApiResult handle(Object ext) {

        ShudiyunB2cOrderDTO shudiyunB2cOrderDTOList = JSON.parseObject(ext.toString(), ShudiyunB2cOrderDTO.class);

        return sdyPushCommonService.executeConsumer(shudiyunB2cOrderDTOList);
    }
}
