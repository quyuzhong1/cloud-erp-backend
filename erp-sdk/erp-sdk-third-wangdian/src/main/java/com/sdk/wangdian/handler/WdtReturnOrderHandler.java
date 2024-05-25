package com.sdk.wangdian.handler;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.WdtReturnOrderDTO;
import com.common.business.dto.WdtReturnOrderDetailDTO;
import com.common.business.enums.*;
import com.common.business.handler.AbstractSoOutStockHandler;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.wdt.WangDianReturnOrderEntity;
import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.api.wms.stockin.StockinAPI;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.RefundStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.RefundStockinResponse;
import com.sdk.wangdian.server.WangDianClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.WDT)
@BusinessType(BusinessTypeEnum.WDT_RETURN_ORDER)
public class WdtReturnOrderHandler  extends AbstractSoOutStockHandler<WangDianReturnOrderEntity, WdtReturnOrderDTO> {

    @Resource
    private WangDianClientService wangDianClientService;

    @Override
    public List<WangDianReturnOrderEntity> download(JobTaskDTO data) {
        return pullData(data);
    }

    @Override
    public List<WdtReturnOrderDTO> convert(List<WangDianReturnOrderEntity> sourceDataList) {
        if (CollectionUtils.isEmpty(sourceDataList)){
            return Collections.emptyList();
        }
        return convertWdtReturnOrder(sourceDataList);
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP.getDesc();
    }


    private List<WangDianReturnOrderEntity> pullData(JobTaskDTO dto) {
        List<RefundStockinResponse.OrderInfoDto> result = new ArrayList<>();
        StockinAPI stockinAPI = wangDianClientService.get(StockinAPI.class);
        RefundStockinRequest request = new RefundStockinRequest();

        request.setStatus(80);
        request.setStartTime(dto.getLastTime().minusMinutes(15).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        request.setEndTime(dto.getNextTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        Pager pager = new Pager();
        int pageSize = 200;
        pager.setPageSize(pageSize);
        pager.setPageNo(0);
        boolean hasNext = true;
        while (hasNext) {
            RefundStockinResponse response;
            try {
                response = stockinAPI.searchRefund(request, pager);
            } catch (Exception e) {
                log.error("拉取旺店通销售出库单失败，原因【{}】", e.getMessage(), e);
                return BeanMapperUtils.copyList(WangDianReturnOrderEntity.class, result);
            }
            if (ObjectUtil.isEmpty(response) || ObjectUtil.isEmpty(response.getOrders())) {
                return BeanMapperUtils.copyList(WangDianReturnOrderEntity.class, result);
            }
            result.addAll(response.getOrders());
            Integer totalCount = response.getTotal();
            if (totalCount <= pager.getPageNo() * pageSize) {
                hasNext = false;
            }
            pager.setPageNo(pager.getPageNo() + 1);
        }
        return JSON.parseObject(JSON.toJSONString(result), new TypeReference<List<WangDianReturnOrderEntity>>() {
        });
    }

    private List<WdtReturnOrderDTO> convertWdtReturnOrder(List<WangDianReturnOrderEntity> sourceDataList) {

        List<WdtReturnOrderDTO> dtoList = new ArrayList<>();
        for (WangDianReturnOrderEntity orderEntity : sourceDataList) {
            WdtReturnOrderDTO dto = new WdtReturnOrderDTO();
            dto.setThirdCode(orderEntity.getOrderNo());
            dto.setWarehouseId(orderEntity.getWarehouseId());
            dto.setApproveStatus(ApproveStatusEnum.APPROVE.getCode());
            dto.setType(OrderTypeEnum.B2C.getCode());
            dto.setBillDate(LocalDateTime.parse(orderEntity.getCheckTime(), DateTimeFormatter.ofPattern(DateUtil.fmt)).toLocalDate());
            dto.setInvalidStatus(false);
            dto.setApproveUserName("wangdaintong");
            dto.setCreateUserName("wangdaintong");
            dto.setApproveTime(LocalDateTime.parse(orderEntity.getCheckTime(), DateTimeFormatter.ofPattern(DateUtil.fmt)));
            dto.setWarehouseId(orderEntity.getWarehouseId());
            dto.setShopId(orderEntity.getShopId());
            dto.setSourceType(SourceTypeEnum.WDT_RETURN_ORDER.getCode());
            dto.setSourceId(orderEntity.getTidList());
            dto.setSourceCode(orderEntity.getTradeNoList());
            List<WdtReturnOrderDetailDTO> detailList = new ArrayList<>();
            for (WangDianReturnOrderEntity.OrderDetailInfoDto infoDto : orderEntity.getDetailList()) {
                WdtReturnOrderDetailDTO detail = getDetail(orderEntity, infoDto);
                detailList.add(detail);
            }
            dto.setDetailList(detailList);
            dtoList.add(dto);
        }
        return dtoList;
    }

    private static WdtReturnOrderDetailDTO getDetail(WangDianReturnOrderEntity orderEntity, WangDianReturnOrderEntity.OrderDetailInfoDto infoDto) {
        WdtReturnOrderDetailDTO detail = new WdtReturnOrderDetailDTO();
        detail.setSkuNo(infoDto.getSpecNo());
        detail.setMustQty(infoDto.getExpectNum().intValue());
        detail.setReceiveQty(infoDto.getStockinNum().intValue());
        detail.setReturnReasonDict(orderEntity.getReason());
        detail.setWarehouseLocation(infoDto.getPositionNo());
        detail.setIsSubContract(false);
        detail.setApproveStatus(ApproveStatusEnum.APPROVE.getCode());
        detail.setWarehouseId(orderEntity.getWarehouseId());
        detail.setSoReturnDetailId(infoDto.getRefundDetailId());
        detail.setSourceDetailId(infoDto.getRecId());
        return detail;
    }
}
