package com.erp.server.wms.service.impl;

import com.common.business.enums.OmsPlatformEnum;
import com.sdk.wms.antu.service.AntuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

/**
 * @author jack
 * @date 2025-02-26
 * @description 速派通处理器
 *
 * 速派通海外仓 -- 来源易仓平台，统一继承易仓处理器
 */
@Slf4j
@Service
@Validated
public class SptHandlerServiceImpl extends EccangHandlerServiceImpl {

    @Resource
    private AntuService antuService;

    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.OMS_SPT;
    }
}
