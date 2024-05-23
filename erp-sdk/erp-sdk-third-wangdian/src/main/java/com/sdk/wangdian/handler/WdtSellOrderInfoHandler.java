package com.sdk.wangdian.handler;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractSoOutStockHandler;
import com.common.core.utils.BeanMapperUtils;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.WDT)
@BusinessType(BusinessTypeEnum.WDT_SO_OUT_STOCK)
public class WdtSellOrderInfoHandler extends AbstractSoOutStockHandler<WangDianOrderEntity, WangDianOrderEntity> {
    @Resource
    private WangDianClientService wangDianClientService;
    @Override
    public List<WangDianOrderEntity> download(JobTaskDTO data) {
        return pullData(data);
    }

    @Override
    public List<WangDianOrderEntity> convert(List<WangDianOrderEntity> sourceDataList) {
        return sourceDataList;
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP_WMS.getDesc();
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
