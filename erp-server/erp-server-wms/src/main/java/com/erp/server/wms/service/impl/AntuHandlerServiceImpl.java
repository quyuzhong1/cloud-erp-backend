package com.erp.server.wms.service.impl;

import com.common.business.enums.OmsPlatformEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:51
 *

 */
@Slf4j
@Service
@Validated
public class AntuHandlerServiceImpl extends EccangHandlerServiceImpl {
    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.OMS_ANTU;
    }
}
