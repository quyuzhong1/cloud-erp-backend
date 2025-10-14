package com.sdk.wangdian.handler;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.WdtReturnOrderDTO;
import com.common.business.dto.WdtReturnOrderDetailDTO;
import com.common.business.enums.*;
import com.common.business.handler.AbstractSoOutStockHandler;
import com.erp.model.dmp.enums.PlatformEnum;
import com.sdk.wangdian.dto.WangDianReturnOrderEntity;
import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.api.wms.stockin.StockinAPI;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.RefundStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.RefundStockinResponse;
import com.sdk.wangdian.server.WangDianClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        List<RefundStockinResponse.OrderInfoDto> dtos = pullData(data);
        return dtos.stream()
                .map(e -> new WangDianReturnOrderEntity(e, data))
                .collect(Collectors.toList());
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


    private List<RefundStockinResponse.OrderInfoDto> pullData(JobTaskDTO dto) {
        List<RefundStockinResponse.OrderInfoDto> result = new ArrayList<>();
        StockinAPI stockinAPI = wangDianClientService.get(StockinAPI.class);
        RefundStockinRequest request = new RefundStockinRequest();
        request.setStatus("80");
        request.setStartTime(dto.getLastTime().minusMinutes(15).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        request.setEndTime(dto.getNextTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        Pager pager = new Pager();
        int pageSize = 200;
        pager.setCalcTotal(true);
        pager.setPageSize(pageSize);
        pager.setPageNo(0);
        boolean hasNext = true;
        while (hasNext) {
            RefundStockinResponse response;
            try {
                response = stockinAPI.searchRefund(request, pager);
            } catch (Exception e) {
                log.error("拉取旺店通销售出库单失败，原因【{}】", e.getMessage(), e);
                return result;
            }
            if (ObjectUtil.isEmpty(response) || ObjectUtil.isEmpty(response.getOrders())) {
                return result;
            }
            result.addAll(response.getOrders());
            Integer totalCount = response.getTotal();
            if (totalCount <= pager.getPageNo() * pageSize) {
                hasNext = false;
            }
            pager.setPageNo(pager.getPageNo() + 1);
        }
        return result;
    }

    private List<WdtReturnOrderDTO> convertWdtReturnOrder(List<WangDianReturnOrderEntity> sourceDataList) {

        List<WdtReturnOrderDTO> dtoList = new ArrayList<>();
        for (WangDianReturnOrderEntity entity : sourceDataList) {
            RefundStockinResponse.OrderInfoDto orderEntity = entity.getOrderInfoDto();
            WdtReturnOrderDTO dto = new WdtReturnOrderDTO();
            dto.setThirdCode(orderEntity.getOrderNo());
            dto.setWarehouseId(orderEntity.getWarehouseId());
            dto.setApproveStatus(ApproveStatusEnum.APPROVE.getCode());
            dto.setType(OrderTypeEnum.B2C.getCode());
            LocalDateTime approveTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(orderEntity.getCheckTime())), ZoneId.systemDefault());
            dto.setBillDate(approveTime.toLocalDate());
            dto.setInvalidStatus(false);
            dto.setApproveUserName("wangdiantong");
            dto.setCreateUserName("wangdiantong");
            dto.setCreateUserId("1977926579534028802");
            dto.setApproveUserId("1977926579534028802");
            dto.setApproveTime(approveTime);
            //店铺id
            dto.setShopId(orderEntity.getShopId());
            dto.setShopName(orderEntity.getShopName());
            dto.setShopNo(orderEntity.getShopNo());
            dto.setCreated(LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(orderEntity.getCreatedTime())), ZoneId.systemDefault()));
            //仓库id
            dto.setWarehouseId(orderEntity.getWarehouseId());
            dto.setWarehouseName(orderEntity.getWarehouseName());
            dto.setSourceType(SourceTypeEnum.SO_RETURN_INSTOCK.getCode());
            dto.setSourceId(orderEntity.getTidList());
            dto.setSourceCode(orderEntity.getTradeNoList());
            List<WdtReturnOrderDetailDTO> detailList = new ArrayList<>();
            for (RefundStockinResponse.OrderDetailInfoDto infoDto : orderEntity.getDetailList()) {
                WdtReturnOrderDetailDTO detail = getDetail(orderEntity, infoDto);
                detailList.add(detail);
            }
            dto.setDetailList(detailList);
            // 平台类型
            dto.setPlatform(entity.getPlatform());
            // 唯一ID
            dto.setUniqueId(entity.getUniqueId());
            // 同步任务ID
            dto.setDmpSyncTaskId(entity.getDmpSyncTaskId());
            dtoList.add(dto);
        }
        return dtoList;
    }

    private static WdtReturnOrderDetailDTO getDetail(RefundStockinResponse.OrderInfoDto orderEntity, RefundStockinResponse.OrderDetailInfoDto infoDto) {
        WdtReturnOrderDetailDTO detail = new WdtReturnOrderDetailDTO();
        detail.setSkuNo(infoDto.getSpecNo());
        detail.setMustQty(infoDto.getExpectNum().intValue());
        detail.setReceiveQty(infoDto.getStockinNum().intValue());
        detail.setRealQty(infoDto.getStockinNum().intValue());
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
