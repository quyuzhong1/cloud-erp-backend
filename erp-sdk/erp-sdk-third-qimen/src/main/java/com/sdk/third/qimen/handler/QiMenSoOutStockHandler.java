package com.sdk.third.qimen.handler;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.WdtSoOutStockDTO;
import com.common.business.dto.WdtSoOutStockDetailDTO;
import com.common.business.enums.*;
import com.common.business.handler.AbstractSoOutStockHandler;
import com.common.core.enums.CurrencyEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.qimencloud.api.scene3ldsmu02o9.request.WdtWmsStockoutSalesQuerywithdetailRequest;
import com.qimencloud.api.scene3ldsmu02o9.response.WdtWmsStockoutSalesQuerywithdetailResponse;
import com.sdk.third.qimen.QiMenClientService;
import com.sdk.third.qimen.config.QiMenUtils;
import com.sdk.third.qimen.entity.QiMenSoOutStockEntity;
import com.taobao.api.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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

import static com.common.core.enums.CountrySiteEnum.CHINA;

/**
 * 奇门销售出库单处理器
 * @date 2024-06-07
 * @author tanmujin
 */
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.QI_MEN)
@BusinessType(BusinessTypeEnum.QIMEN_SO_OUT_STOCK)
public class QiMenSoOutStockHandler extends AbstractSoOutStockHandler<QiMenSoOutStockEntity, WdtSoOutStockDTO> {

    @Resource
    private QiMenClientService qimenService;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<QiMenSoOutStockEntity> download(JobTaskDTO dto) {
        List<WdtWmsStockoutSalesQuerywithdetailResponse.Order> orderList = pullData(dto);
        return orderList.stream()
                .map(e -> new QiMenSoOutStockEntity(e, dto))
                .collect(Collectors.toList());
    }

    private List<WdtWmsStockoutSalesQuerywithdetailResponse.Order> pullData(JobTaskDTO dto) {
        WdtWmsStockoutSalesQuerywithdetailRequest.Pager pager = new WdtWmsStockoutSalesQuerywithdetailRequest.Pager();
        Long pageSize = 200L;
        pager.setPageNo(1L);
        pager.setPageSize(pageSize);

        WdtWmsStockoutSalesQuerywithdetailRequest.Params params = new WdtWmsStockoutSalesQuerywithdetailRequest.Params();
        params.setStatus("110");
        params.setStatusType(3L);
        params.setStartTime(dto.getLastTime().minusMinutes(5).format(timeFormatter));
        params.setEndTime(dto.getNextTime().format(timeFormatter));

        WdtWmsStockoutSalesQuerywithdetailRequest request = new WdtWmsStockoutSalesQuerywithdetailRequest();
        request.setPager(pager);
        request.setParams(params);
        request.setTargetAppKey(qimenService.getTargetAppKey());
        request.setWdtAppkey(qimenService.getWdtAppKey());
        request.setWdtSalt(qimenService.getWdtSalt());
        request.putOtherTextParam(qimenService.getCustomerIdKey(), qimenService.getCustomerIdValue());

        List<WdtWmsStockoutSalesQuerywithdetailResponse.Order> result = new ArrayList<>();
        boolean hasNext = true;
        while (hasNext) {
            request.setDatetime(qimenService.format(new Date()));
            request.setWdtSign(QiMenUtils.getQimenCustomWdtSign(request, qimenService.getWdtSecret()));
            WdtWmsStockoutSalesQuerywithdetailResponse response;
            try {
                response = qimenService.execute(request);
                log.info("奇门销售出库单响应参数：{}", JSONUtil.toJsonStr(response));
            } catch (ApiException e) {
                log.error("拉取奇门销售出库单异常：{}", e);
                return result;
            }

            if(response.getStatus() != 0L){
                log.error("拉取奇门销售出库单失败，request：{}，response：{}", JSONUtil.toJsonStr(request), JSONUtil.toJsonStr(response));
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
    public List<WdtSoOutStockDTO> convert(List<QiMenSoOutStockEntity> sourceDataList) {
        if (CollectionUtils.isEmpty(sourceDataList)){
            return Collections.emptyList();
        }
        return convertWdtSoOutStock(sourceDataList);
    }

    private List<WdtSoOutStockDTO> convertWdtSoOutStock(List<QiMenSoOutStockEntity> sourceDataList) {
        List<WdtSoOutStockDTO> soOutStockList = new ArrayList<>();
        for (QiMenSoOutStockEntity orderEntity : sourceDataList) {
            WdtWmsStockoutSalesQuerywithdetailResponse.Order order = orderEntity.getOrderInfoDto();
            WdtSoOutStockDTO dto = new WdtSoOutStockDTO();
            //单据编号
            dto.setCode(order.getOrderNo());
            //单据状态
            dto.setApproveStatus(ApproveStatusEnum.APPROVE);
            //是否作废
            dto.setInvalidStatus(false);
            //销售订单code
            dto.setSoCode(order.getSrcTradeNo());
            //店铺id
            dto.setShopId(String.valueOf(order.getShopId()));
            dto.setShopName(order.getShopName());
            dto.setShopNo(order.getShopNo());
            //仓库id
            dto.setWarehouseId(String.valueOf(order.getWarehouseId()));
            dto.setWarehouseName(order.getWarehouseName());
            //出库时间
            LocalDateTime outStockTime = LocalDateTime.parse(order.getConsignTime(), timeFormatter);
            dto.setPlanDeliveryDate(outStockTime.toLocalDate());
            dto.setPackDate(outStockTime.toLocalDate());
            dto.setActualDeliveryDate(outStockTime);
            // 出库日期
            dto.setBillDate(outStockTime.toLocalDate());
            //优惠金额
            dto.setTotalDiscountAmount(BigDecimal.valueOf(Double.parseDouble(order.getDiscount())));
            //运输单号
            dto.setTrackNo(order.getLogisticsNo());
            //来源信息
            dto.setSourceId(String.valueOf(order.getStockoutId()));
            dto.setSourceType(SourceTypeEnum.SO_OUTSTOCK.getCode());
            dto.setSourceCode(order.getTradeNo());
            dto.setOrderType(OrderTypeEnum.B2C.getCode());
            //审核时间
            dto.setApproveTime(outStockTime);
            dto.setCreated(LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(order.getCreated())), ZoneId.systemDefault()));
            dto.setCreateUserName("qimen");
            dto.setCountry(CHINA.getSite());
            //第三方单据编号
            dto.setThirdCode(order.getSrcOrderNo());

            List<WdtSoOutStockDetailDTO> detailList = new ArrayList<>();
            for (WdtWmsStockoutSalesQuerywithdetailResponse.DetailsList detailItem : order.getDetailsList()) {
                WdtSoOutStockDetailDTO detail = new WdtSoOutStockDetailDTO();
                detail.setSkuNo(detailItem.getSpecNo());
                //实发
                String realQty = detailItem.getNum();
                Integer actualQty = Double.valueOf(realQty).intValue();
                detail.setActualQty(actualQty);
                detail.setPlanQty(actualQty);
                //单价
                detail.setPrice(BigDecimal.valueOf(Double.parseDouble(detailItem.getMarketPrice())));
                //税率
                detail.setTaxRate(BigDecimal.valueOf(Double.parseDouble(detailItem.getTaxRate())));
                //成交价
                detail.setAmount(BigDecimal.valueOf(Double.parseDouble(detailItem.getSellPrice())));
                detail.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
                detail.setCurrencySymbol(CurrencyEnum.CNY.getCurrencySymbol());
                detail.setAllAmountLocalCurrency(BigDecimal.valueOf(Double.parseDouble(detailItem.getSellPrice())));
                detail.setExchangeRate(new BigDecimal(1));
                detail.setSoDetailId(String.valueOf(detailItem.getSrcOrderDetailId()));
                detail.setRemark(detailItem.getRemark());
                detail.setSourceDetailId(String.valueOf(detailItem.getSrcOrderDetailId()));
                detail.setInvalidStatus(false);
                detail.setSuiteNo(detailItem.getSuiteNo());
                detail.setSuiteQty(Integer.valueOf(detailItem.getSuiteNum()));

                List<WdtWmsStockoutSalesQuerywithdetailResponse.PositionDetailsList> list = detailItem.getPositionDetailsList();
                List<WdtSoOutStockDetailDTO.PositionDetailsList> detailsLists = list.stream()
                        .map(v -> {
                            WdtSoOutStockDetailDTO.PositionDetailsList detailDTO = new WdtSoOutStockDetailDTO.PositionDetailsList();
                            BeanUtils.copyProperties(v, detailDTO);
                            detailDTO.setPositionGoodsCount(Double.valueOf(v.getPositionGoodsCount()).intValue());
                            return detailDTO;
                        }).collect(Collectors.toList());
                detail.setPositionDetailsList(detailsLists);
                detailList.add(detail);
            }
            dto.setDetailList(detailList);
            // 平台类型
            dto.setPlatform(orderEntity.getPlatform());
            // 唯一ID
            dto.setUniqueId(orderEntity.getUniqueId());
            // 同步任务ID
            dto.setDmpSyncTaskId(orderEntity.getDmpSyncTaskId());
            soOutStockList.add(dto);
        }
        return soOutStockList;
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP.getDesc();
    }


}
