package com.erp.sdk.oms.amz.spapi.handler;

import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractOrderHandler;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonListingDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 亚马逊产品处理器
 *
 * @Author Cloud
 * @Date 2023/8/31 15:48
 **/
@Component
@PlatformCategoryType(PlatformCategoryEnum.OMS)
@PlatformType(PlatformDictEnum.AMAZON)
@BusinessType(BusinessTypeEnum.ORDER)
public class AmazonListingHandler extends AbstractOrderHandler<PlatformAmazonListingDTO, PlatformProductDTO> {

    @Override
    public List<PlatformAmazonListingDTO> download(JobTaskDTO data) {

        // TODO: 亚马逊订单下载
        System.out.println("data = " + data);
        // 返回下载源数据
        return null;
    }


    @Override
    public List<PlatformProductDTO> convert(List<PlatformAmazonListingDTO> sourceDataList) {
        System.out.println("sourceDataList = " + sourceDataList);
        // TODO: 亚马逊订单转换为发送mq数据
        // 包含数据过滤数据 数据转换 数据合并拆分等操作
        return null;
    }

    @Override
    public String getTargetPlatform() {
        return PlatformDictEnum.AMAZON.getCode();
    }
}
