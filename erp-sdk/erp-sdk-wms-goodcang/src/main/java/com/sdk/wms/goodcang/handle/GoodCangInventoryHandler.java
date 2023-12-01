package com.sdk.wms.goodcang.handle;

import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformInventoryDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractPullThirdWarehouseHandler;
import com.common.business.utils.MD5Util;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.PlatformEnum;
import com.sdk.wms.goodcang.convert.GoodCangConverter;
import com.sdk.wms.goodcang.dto.request.GoodCangGetInventoryReq;
import com.sdk.wms.goodcang.dto.response.GoodCangInventoryResp;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import com.sdk.wms.goodcang.service.GoodCangService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 谷仓拉取库存数据
 **/
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.GOOD_CANG)
@BusinessType(BusinessTypeEnum.INVENTORY)
public class GoodCangInventoryHandler extends AbstractPullThirdWarehouseHandler<GoodCangInventoryResp, PlatformInventoryDTO> {

    @Resource
    private GoodCangService goodCangService;

    @Override
    public List<GoodCangInventoryResp> download(JobTaskDTO data) {

        //查询数据
        GoodCangGetInventoryReq req = new GoodCangGetInventoryReq();
        //最大页码100，从第一页开始查询
        req.setPageSize(100);

        List<GoodCangInventoryResp> respList = new ArrayList<>();
        int page = 1;
        while (true) {
            req.setPage(page);
            GoodCangResponse<List<GoodCangInventoryResp>> goodCangResponse = goodCangService.getProductInventory(req);
            checkResponse(goodCangResponse);
            respList.addAll(goodCangResponse.getData());
            if (goodCangResponse.getCount() <= page * 100) {
                break;
            }
            page++;
        }
        respList.forEach(v->{
            v.setUniqueId(MD5Util.toMD5(getTargetPlatform()+BusinessTypeEnum.INVENTORY.getCode()+v.getWarehouseCode()+v.getProductSku()));
            v.setAuthId(data.getShopId());
        });
        return respList;
    }

    private void checkResponse(GoodCangResponse<?> response) {
        if (!isSuccess(response.getAsk())) {
            log.error("谷仓查询库存数据失败," + response.getMessage());
            throw new ServiceException("谷仓查询库存数据失败," + response.getMessage());
        }
    }

    @Override
    public List<PlatformInventoryDTO> convert(List<GoodCangInventoryResp> sourceDataList) {
        return GoodCangConverter.INSTANCE.inventoryConversion(sourceDataList);
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP_WMS.getDesc();
    }

    public boolean isSuccess(String ask){
        return "Success".equals(ask);
    }
}
