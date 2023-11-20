package com.sdk.wms.goodcang.handle;

import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.ThirdWarehouseProductDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractOrderHandler;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.sdk.wms.goodcang.dto.response.GoodCangSkuResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 谷仓拉取产品数据
 **/
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.GOOD_CANG)
@BusinessType(BusinessTypeEnum.THIRD_WAREHOUSE_PRODUCT)
public class GoodCangProductHandler extends AbstractOrderHandler<GoodCangSkuResp, ThirdWarehouseProductDTO> {

    @Override
    public List<GoodCangSkuResp> download(JobTaskDTO data) {
        LocalDateTime lastTime = data.getLastTime();
        long timeFrom = Timestamp.valueOf(lastTime).getTime() / 1000;
        LocalDateTime nextTime = data.getNextTime();
        if (lastTime.isEqual(nextTime)){
            //nextTime +1天
            nextTime = lastTime.plusDays(1);
        }
        long timeTo = Timestamp.valueOf(nextTime).getTime() / 1000;
        //获取授权信息
        //设置threadlocal
        //查询数据
        // 返回下载源数据
        return null;
    }

    @Override
    public List<ThirdWarehouseProductDTO> convert(List<GoodCangSkuResp> sourceDataList) {
        return null;
    }

    @Override
    public String getTargetPlatform() {
        return PlatformDictEnum.GOOD_CANG.getCode();
    }
}
