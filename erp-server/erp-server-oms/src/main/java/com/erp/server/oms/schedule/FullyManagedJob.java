package com.erp.server.oms.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.server.oms.service.SoB2cService;
import com.sdk.oms.tiktok.dto.tiktok.order.FullyDeliveryOrderDTO;
import com.sdk.oms.tiktok.service.TikTokFullService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zdy
 * @ClassName MsgWarningJob
 * @description: 全托管订单任务
 * @date 2025年03月26日
 * @version: 1.0
 */
@Component
@Slf4j
public class FullyManagedJob {
    @Resource
    private SoB2cService soB2cService;
    @Resource
    private TikTokFullService tikTokFullService;
    /**
     * 查询送货单信息
     */
    @XxlJob("GetDeliveryOrderByParam")
    public void GetDeliveryOrderByParam() {
        XxlJobHelper.log("查询送货单信息开始执行");
        String jobParam = XxlJobHelper.getJobParam();
        //已发货 且 INBOUND: 已入库上架/INVAILD-已作废  中间查询【未发货不需要查或者已入库或已作废不需要查询】
        List<String> billStatusList = new ArrayList<>();
        billStatusList.add(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
        List<String> platformStatusList = new ArrayList<>();
        platformStatusList.add("INBOUND");
        platformStatusList.add("INVAILD");
        List<String> platformList = new ArrayList<>();
        platformList.add(PlatformDictEnum.TIK_TOK_FULLY.getCode());
        List<SoB2cDTO.DeliveryDTO> deliveryDTOS = soB2cService.listDeliveryOrderByParam(billStatusList, platformStatusList,platformList);
        //根据店铺进行分组
        if (CollUtil.isEmpty(deliveryDTOS)) {
            XxlJobHelper.log("查询送货单信息执行完成 没有需要查询的订单");
            return; // 没有配置则不执行
        }
        Map<String, List<SoB2cDTO.DeliveryDTO>> shopMap = deliveryDTOS.stream().collect(Collectors.groupingBy(SoB2cDTO.DeliveryDTO::getShopId));
        //循环查询
        for (Map.Entry<String, List<SoB2cDTO.DeliveryDTO>> entry : shopMap.entrySet()) {
            String shopId = entry.getKey();
            List<SoB2cDTO.DeliveryDTO> deliveryDTOList = entry.getValue();
            List<String> deliveryNoList = deliveryDTOList.stream().map(SoB2cDTO.DeliveryDTO::getTransportNo).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            List<FullyDeliveryOrderDTO.DataDTO.DeliveryOrdersDTO> deliveryOrdersDTOS = tikTokFullService.listDeliveryOrderByParam(shopId, deliveryNoList);
        }

    }
}
