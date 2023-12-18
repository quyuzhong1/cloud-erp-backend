package com.erp.oms.aliexpress.handler;/**
 * @author Lambda
 * @Classname AliExpressOrderHandler
 * @Description TODO
 * @Date 2023-11-29 10:11
 * @Created by yl
 */

import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractOrderHandler;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.oms.aliexpress.dto.PlatformAliExpressOrderDTO;
import com.erp.oms.aliexpress.dto.request.OrderRequest;
import com.erp.oms.aliexpress.dto.response.AliExpressOrder;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description TODO
 * @Author yl
 * @Date 2023-11-29 10:11
 */
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.ALI_EXPRESS)
@BusinessType(BusinessTypeEnum.ORDER)
public class AliExpressOrderHandler extends AbstractOrderHandler<PlatformAliExpressOrderDTO, PlatformOrderDTO> {


    @Resource
    private AliExpressOrderService aliExpressOrderService;

    /**
     * 下载数据
     *
     * @param data
     * @return
     */
    @Override
    public List<PlatformAliExpressOrderDTO> download(JobTaskDTO data) {
        String apiName = AliexpressConstants.LIST_ORDER;
        AliExpressShopInfoDTO shopInfoDTO = aliExpressOrderService.getShopInfoByShopId(data.getShopId());
        if (null == shopInfoDTO) {
            log.error("[速卖通订单下载]  获取 token 失败: shopId={}", data.getShopId());
            return Collections.emptyList();
        }
        // 上次执行时间
        LocalDateTime lastTime = data.getLastTime();
        // 下次执行时间
        LocalDateTime nextTime = data.getNextTime();
        String formatStr = DateUtil.fmt;
        OrderRequest orderRequest = OrderRequest.builder().
                clientId(shopInfoDTO.getClientId()).
                clientSecret(shopInfoDTO.getClientSecret()).
                startTime(LocalDateUtil.formatTime(lastTime, formatStr)).
                endTime(LocalDateUtil.formatTime(nextTime, formatStr)).
                baseUrl(shopInfoDTO.getBaseUrl()).
                apiName(apiName).
                currentPage(1).
                token(shopInfoDTO.getToken()).build();
        List<AliExpressOrder> orderList = new ArrayList<>(20);
        try {
            aliExpressOrderService.listOrder(orderRequest,orderList);
        } catch (Exception e) {
            log.error("获取速卖通订单数据异常:{}", e.getMessage());
        }
        if (CollectionUtils.isEmpty(orderList)) {
            return Collections.emptyList();
        }
        return orderList.stream()
                .map(e -> new PlatformAliExpressOrderDTO(data, e, shopInfoDTO))
                .collect(Collectors.toList());

    }

    @Override
    public List<PlatformOrderDTO> convert(List<PlatformAliExpressOrderDTO> sourceDataList) {
        return sourceDataList.stream()
                // 组装
                .map(PlatformAliExpressOrderDTO::convertDTO)
                .collect(Collectors.toList());
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP.getDesc();
    }
}
