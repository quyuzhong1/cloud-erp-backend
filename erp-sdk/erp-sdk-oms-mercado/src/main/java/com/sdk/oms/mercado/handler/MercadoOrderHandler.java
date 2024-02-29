package com.sdk.oms.mercado.handler;

import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractOrderHandler;
import com.sdk.oms.mercado.dto.MercadoOrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.SHOPEE)
@BusinessType(BusinessTypeEnum.ORDER)
public class MercadoOrderHandler extends AbstractOrderHandler<MercadoOrderDTO, PlatformOrderDTO> {

    @Override
    public List<MercadoOrderDTO> download(JobTaskDTO data) {
        return null;
    }

    @Override
    public List<PlatformOrderDTO> convert(List<MercadoOrderDTO> sourceDataList) {
        return null;
    }

    @Override
    public String getTargetPlatform() {
        return null;
    }
}
