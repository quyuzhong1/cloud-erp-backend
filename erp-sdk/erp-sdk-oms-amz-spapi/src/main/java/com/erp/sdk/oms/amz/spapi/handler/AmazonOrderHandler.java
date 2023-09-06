package com.erp.sdk.oms.amz.spapi.handler;

import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractOrderHandler;
import com.common.business.dto.PlatformOrderDTO;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonOrderDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 亚马逊订单处理器
 *
 * @Author Cloud
 * @Date 2023/8/31 15:48
 **/
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.AMAZON)
@BusinessType(BusinessTypeEnum.ORDER)
public class AmazonOrderHandler extends AbstractOrderHandler<PlatformAmazonOrderDTO, PlatformOrderDTO> {

    @Override
    public List<PlatformAmazonOrderDTO> download(JobTaskDTO data) {

        // TODO: 亚马逊订单下载
        System.out.println("data = " + data);
        // 返回下载源数据
        return null;
    }


    @Override
    public List<PlatformOrderDTO> convert(List<PlatformAmazonOrderDTO> sourceDataList) {

        // TODO: 亚马逊订单转换为发送mq数据
        System.out.println("convert = " + sourceDataList);
        // 包含数据过滤数据 数据转换 数据合并拆分等操作
        return null;
    }

    @Override
    public String getTargetPlatform() {
        return PlatformDictEnum.AMAZON.getCode();
    }
}
