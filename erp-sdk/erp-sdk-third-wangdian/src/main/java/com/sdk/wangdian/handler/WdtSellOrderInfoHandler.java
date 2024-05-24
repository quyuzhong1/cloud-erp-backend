package com.sdk.wangdian.handler;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.WdtSoOutStockDTO;
import com.common.business.dto.WdtSoOutStockDetailDTO;
import com.common.business.enums.*;
import com.common.business.handler.AbstractSoOutStockHandler;
import com.common.core.enums.CurrencyEnum;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.enums.PlatformEnum;
import com.sdk.wangdian.dto.WangDianOrderEntity;
import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.wms.stockout.StockoutAPI;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.SalesStockoutRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.SalesStockoutResponse;
import com.sdk.wangdian.server.WangDianClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.WDT)
@BusinessType(BusinessTypeEnum.WDT_SO_OUT_STOCK)
public class WdtSellOrderInfoHandler extends AbstractSoOutStockHandler<WangDianOrderEntity, WdtSoOutStockDTO> {
    @Resource
    private WangDianClientService wangDianClientService;
    @Override
    public List<WangDianOrderEntity> download(JobTaskDTO data) {
        return pullData(data);
    }

    @Override
    public List<WdtSoOutStockDTO> convert(List<WangDianOrderEntity> sourceDataList) {
        return convertWdtSoOutStock(sourceDataList);
    }

    private List<WdtSoOutStockDTO> convertWdtSoOutStock(List<WangDianOrderEntity> sourceDataList) {
        List<WdtSoOutStockDTO> soOutStockList = new ArrayList<>();
        for (WangDianOrderEntity order : sourceDataList) {
            WdtSoOutStockDTO soOutStock = new WdtSoOutStockDTO();
            //单据编号
            soOutStock.setCode(order.getOrderNo());
            //单据状态
            soOutStock.setApproveStatus(ApproveStatusEnum.APPROVE);
            //是否作废
            soOutStock.setInvalidStatus(false);
            //销售订单code
            soOutStock.setSoCode(order.getSrcOrderNo());
            //出库时间
            LocalDateTime outStockTime = LocalDateTime.parse(order.getConsignTime(), DateTimeFormatter.ofPattern(DateUtil.fmt));
            soOutStock.setPlanDeliveryDate(outStockTime.toLocalDate());
            soOutStock.setPackDate(outStockTime.toLocalDate());
            soOutStock.setActualDeliveryDate(outStockTime);
            // 出库日期
            soOutStock.setBillDate(outStockTime.toLocalDate());
            //优惠金额
            soOutStock.setTotalDiscountAmount(order.getDiscount());
            //运输单号
            soOutStock.setTrackNo(order.getLogisticsNo());
            //来源信息
            soOutStock.setSourceId(order.getStockoutId());
            soOutStock.setSourceType(SourceTypeEnum.WDT_OUT_STOCK.getCode());
            soOutStock.setSourceCode(order.getTradeNo());
            soOutStock.setOrderType(OrderTypeEnum.B2C.getCode());
            //审核时间
            soOutStock.setApproveTime(outStockTime);

            soOutStock.setCountry(order.getReceiverCountry());
            //第三方单据编号
            soOutStock.setThirdCode(order.getSrcOrderNo());

            List<WdtSoOutStockDetailDTO> detailList = new ArrayList<>();
            for (WangDianOrderEntity.DetailItem detailItem : order.getDetailsList()) {
                WdtSoOutStockDetailDTO detail = new WdtSoOutStockDetailDTO();
                String skuNo = detailItem.getSpecNo();
                detail.setSkuNo(skuNo);
                //实发
                BigDecimal realQty = detailItem.getNum();
                Integer actualQty = realQty.intValue();
                detail.setActualQty(actualQty);
                detail.setPlanQty(actualQty);
                //单价
                detail.setPrice(detailItem.getMarketPrice());
                //税率
                detail.setTaxRate(detailItem.getTaxRate());
                //成交价
                detail.setAmount(detailItem.getSellPrice());
                detail.setCurrency(CurrencyEnum.RMB.getCurrencyCode());
                detail.setCurrencySymbol(CurrencyEnum.RMB.getCurrencySymbol());
                detail.setAllAmountLocalCurrency(detailItem.getSellPrice());
                detail.setExchangeRate(new BigDecimal(1));
                detail.setSoDetailId(detailItem.getSrcOrderDetailId());
                detail.setRemark(detailItem.getRemark());
                detail.setSourceDetailId(detailItem.getSrcOrderDetailId());
                detail.setApproveStatus(ApproveStatusEnum.APPROVE.getStatus());
                detail.setInvalidStatus(false);
                detailList.add(detail);
            }
            soOutStock.setDetailList(detailList);
            soOutStockList.add(soOutStock);
        }
        return soOutStockList;
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP.getDesc();
    }

    private List<WangDianOrderEntity> pullData(JobTaskDTO dto) {
        List<SalesStockoutResponse.OrderInfoDto> result = new ArrayList<>();
        StockoutAPI stockoutAPI = wangDianClientService.get(StockoutAPI.class);
        SalesStockoutRequest request = new SalesStockoutRequest();
        request.setStatusType(SalesStockoutRequest.STATUS_TYPE_CONSIGNED);
        request.setStatus("110");
        request.setStartTime(dto.getLastTime().minusMinutes(15).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        request.setEndTime(dto.getNextTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        Pager pager = new Pager();
        int pageSize = 200;
        pager.setPageSize(pageSize);
        pager.setPageNo(0);
        boolean hasNext = true;
        while (hasNext) {
            SalesStockoutResponse response;
            try {
                response = stockoutAPI.querySales(request, pager);
            } catch (WdtErpException e) {
                log.error("拉取旺店通销售出库单失败，原因【{}】", e.getMessage(), e);
                return BeanMapperUtils.copyList(WangDianOrderEntity.class, result);
            }
            if (ObjectUtil.isEmpty(response) || ObjectUtil.isEmpty(response.getOrderList())) {
                return BeanMapperUtils.copyList(WangDianOrderEntity.class, result);
            }
            result.addAll(response.getOrderList());
            Integer totalCount = response.getTotal();
            if (totalCount <= pager.getPageNo() * pageSize) {
                hasNext = false;
            }
            pager.setPageNo(pager.getPageNo() + 1);
        }
        return JSON.parseObject(JSON.toJSONString(result), new TypeReference<List<WangDianOrderEntity>>() {
        });
    }
}
