package com.sdk.third.qimen.handler;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.WdtReturnOrderDTO;
import com.common.business.dto.WdtReturnOrderDetailDTO;
import com.common.business.enums.*;
import com.common.business.handler.AbstractSoOutStockHandler;
import com.erp.model.dmp.enums.PlatformEnum;
import com.qimencloud.api.scene3ldsmu02o9.request.WdtWmsStockinRefundQuerywithdetailRequest;
import com.qimencloud.api.scene3ldsmu02o9.response.WdtWmsStockinRefundQuerywithdetailResponse;
import com.sdk.third.qimen.QiMenClientService;
import com.sdk.third.qimen.config.QiMenUtils;
import com.sdk.third.qimen.entity.QiMenReturnOrderEntity;
import com.taobao.api.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 * @date 2024-06-11
 * @author tanmujin
 */
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.QI_MEN)
@BusinessType(BusinessTypeEnum.QIMEN_RETURN_ORDER)
public class QiMenReturnOrderHandler extends AbstractSoOutStockHandler<QiMenReturnOrderEntity, WdtReturnOrderDTO> {

    @Resource
    private QiMenClientService qimenService;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<QiMenReturnOrderEntity> download(JobTaskDTO dto) {
        List<WdtWmsStockinRefundQuerywithdetailResponse.Order> dtos = pullData(dto);
        return dtos.stream()
                .map(e -> new QiMenReturnOrderEntity(e, dto))
                .collect(Collectors.toList());
    }

    private List<WdtWmsStockinRefundQuerywithdetailResponse.Order> pullData(JobTaskDTO dto) {
        WdtWmsStockinRefundQuerywithdetailRequest.Pager pager = new WdtWmsStockinRefundQuerywithdetailRequest.Pager();
        long pageSize = 200L;
        pager.setPageNo(1L);
        pager.setPageSize(pageSize);

        WdtWmsStockinRefundQuerywithdetailRequest.Params params = new WdtWmsStockinRefundQuerywithdetailRequest.Params();
        params.setStatus("80");
        params.setStartTime(dto.getLastTime().minusMinutes(15).format(timeFormatter));
        params.setEndTime(dto.getNextTime().format(timeFormatter));

        WdtWmsStockinRefundQuerywithdetailRequest request = new WdtWmsStockinRefundQuerywithdetailRequest();
        request.setPager(pager);
        request.setParams(params);
        request.setTargetAppKey(qimenService.getTargetAppKey());
        request.setWdtAppkey(qimenService.getWdtAppKey());
        request.setWdtSalt(qimenService.getWdtSalt());
        request.putOtherTextParam(qimenService.getCustomerIdKey(), qimenService.getCustomerIdValue());

        List<WdtWmsStockinRefundQuerywithdetailResponse.Order> result = new ArrayList<>();
        boolean hasNext = true;
        while (hasNext) {
            request.setDatetime(qimenService.format(new Date()));
            request.setWdtSign(QiMenUtils.getQimenCustomWdtSign(request, qimenService.getWdtSecret()));
            WdtWmsStockinRefundQuerywithdetailResponse response;
            try {
                response = qimenService.execute(request);
            } catch (ApiException e) {
                log.error("拉取奇门销售退货入库单异常：{}", e);
                return result;
            }
            if(response.getStatus() != 0L){
                log.error("拉取奇门销售退货入库单失败，request：{}，response：{}", JSONUtil.toJsonStr(request), JSONUtil.toJsonStr(response));
                return result;
            }
            if (response.getData().getTotalCount() == 0L) {
                return result;
            }
            result.addAll(response.getData().getOrder());
            Long totalCount = response.getData().getTotalCount();
            if (totalCount <= pager.getPageNo() * pageSize) {
                hasNext = false;
            }
            pager.setPageNo(pager.getPageNo() + 1);
            request.setPager(pager);
        }
        return result;
    }

    @Override
    public List<WdtReturnOrderDTO> convert(List<QiMenReturnOrderEntity> sourceDataList) {
        if (CollectionUtils.isEmpty(sourceDataList)){
            return Collections.emptyList();
        }
        return convertWdtReturnOrder(sourceDataList);
    }

    private List<WdtReturnOrderDTO> convertWdtReturnOrder(List<QiMenReturnOrderEntity> sourceDataList) {
        List<WdtReturnOrderDTO> dtoList = new ArrayList<>();
        for (QiMenReturnOrderEntity entity : sourceDataList) {
            WdtWmsStockinRefundQuerywithdetailResponse.Order orderEntity = entity.getOrderInfoDto();
            WdtReturnOrderDTO dto = new WdtReturnOrderDTO();
            dto.setThirdCode(orderEntity.getOrderNo());
            dto.setWarehouseId(String.valueOf(orderEntity.getWarehouseId()));
            dto.setApproveStatus(ApproveStatusEnum.APPROVE.getCode());
            dto.setType(OrderTypeEnum.B2C.getCode());
            LocalDateTime approveTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(orderEntity.getCheckTime())), ZoneId.systemDefault());
            dto.setBillDate(approveTime.toLocalDate());
            dto.setInvalidStatus(false);
            dto.setApproveUserName("qimen");
            dto.setApproveUserId("1808810116456153089");
            dto.setCreateUserName("qimen");
            dto.setCreateUserId("1808810116456153089");
            dto.setApproveTime(approveTime);
            //店铺id
            dto.setShopId(String.valueOf(orderEntity.getShopId()));
            dto.setShopName(orderEntity.getShopName());
            dto.setShopNo(orderEntity.getShopNo());
            dto.setCreated(LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(orderEntity.getCreatedTime())), ZoneId.systemDefault()));
            //仓库id
            dto.setWarehouseId(String.valueOf(orderEntity.getWarehouseId()));
            dto.setWarehouseName(orderEntity.getWarehouseName());
            dto.setSourceType(SourceTypeEnum.SO_RETURN_INSTOCK.getCode());
            dto.setSourceId(orderEntity.getTidList());
            dto.setSourceCode(orderEntity.getTradeNoList());
            List<WdtReturnOrderDetailDTO> detailList = new ArrayList<>();
            for (WdtWmsStockinRefundQuerywithdetailResponse.DetailsList detailDto : orderEntity.getDetailsList()) {
                WdtReturnOrderDetailDTO detail = getDetail(orderEntity, detailDto);
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

    private WdtReturnOrderDetailDTO getDetail(WdtWmsStockinRefundQuerywithdetailResponse.Order orderEntity, WdtWmsStockinRefundQuerywithdetailResponse.DetailsList detailDto) {
        WdtReturnOrderDetailDTO detail = new WdtReturnOrderDetailDTO();
        detail.setSkuNo(detailDto.getSpecNo());
        detail.setMustQty(new BigDecimal(detailDto.getExpectNum()).intValue());
        detail.setReceiveQty(new BigDecimal(detailDto.getNum()).intValue());
        detail.setRealQty(new BigDecimal(detailDto.getNum()).intValue());
        detail.setReturnReasonDict(orderEntity.getReason());
        detail.setWarehouseLocation(detailDto.getPositionNo());
        detail.setIsSubContract(false);
        detail.setApproveStatus(ApproveStatusEnum.APPROVE.getCode());
        detail.setWarehouseId(String.valueOf(orderEntity.getWarehouseId()));
        detail.setSoReturnDetailId(detailDto.getRefundDetailId());
        detail.setSourceDetailId(String.valueOf(detailDto.getRecId()));
        return detail;
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP.getDesc();
    }
}
