package com.sdk.wms.goodcang.handle;

import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformInboundDTO;
import com.common.business.dto.PlatformWarehouseDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.OverseasInstockStatusEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractThirdWarehouseHandler;
import com.common.business.utils.MD5Util;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.wms.enums.InstockTypeEnum;
import com.erp.rpc.wms.feign.WmsFbaOverseasFeign;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.sdk.wms.goodcang.convert.GoodCangConverter;
import com.sdk.wms.goodcang.dto.response.GoodCangReceiptBatchResp;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import com.sdk.wms.goodcang.dto.response.GoodCangWarehouseResp;
import com.sdk.wms.goodcang.service.GoodCangService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 谷仓拉取入库数据
 **/
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.GOOD_CANG)
@BusinessType(BusinessTypeEnum.INBOUND)
public class GoodCangInboundHandler extends AbstractThirdWarehouseHandler<GoodCangReceiptBatchResp, PlatformInboundDTO> {

    @Resource
    private GoodCangService goodCangService;

    @Resource
    private WmsOverseasWarehouseFeign overseasWarehouseFeign;

    @Override
    public List<GoodCangReceiptBatchResp> download(JobTaskDTO data) {
        List<GoodCangReceiptBatchResp> respList = new ArrayList<>();
        //查询待签收、部分签收状态的入库单
        List<String> receiveCodeList = overseasWarehouseFeign.getReceiptNumbersForStatus(Arrays.asList(OverseasInstockStatusEnum.TO_BE_SIGNED.getCode(),OverseasInstockStatusEnum.PARTIAL_SIGNED.getCode()));
        for(String receiveCode : receiveCodeList){
            GoodCangResponse<GoodCangReceiptBatchResp> response = goodCangService.getReceiptBatch(receiveCode);
            checkResponse(response);
            respList.add(response.getData());
        }
        return respList;
    }

    private void checkResponse(GoodCangResponse<?> response) {
        if (!isSuccess(response.getAsk())) {
            log.error("谷仓查询入库数据失败," + response.getMessage());
            throw new ServiceException("谷仓查询入库数据失败," + response.getMessage());
        }
    }

    @Override
    public List<PlatformInboundDTO> convert(List<GoodCangReceiptBatchResp> sourceDataList) {
        List<PlatformInboundDTO> list = GoodCangConverter.INSTANCE.inboundConversion(sourceDataList);
        //封装明细数据
        for(PlatformInboundDTO dto : list){
            this.groupBySku(dto);
        }
        return list;
    }


    private void groupBySku(PlatformInboundDTO dto) {
        Map<String, Integer> receivedQuantityMap = dto.getReceivingDataList().stream()
                .collect(Collectors.groupingBy(PlatformInboundDTO.Receiving::getProductSku, Collectors.summingInt(PlatformInboundDTO.Receiving::getReceiveQty)));

        List<PlatformInboundDTO.Item> items = receivedQuantityMap.entrySet().stream()
                .map(entry -> new PlatformInboundDTO.Item(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        dto.setItems(items);
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP_WMS.getDesc();
    }

    public boolean isSuccess(String ask){
        return "Success".equals(ask);
    }
}
