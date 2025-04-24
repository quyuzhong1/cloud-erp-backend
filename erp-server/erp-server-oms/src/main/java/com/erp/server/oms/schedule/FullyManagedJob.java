package com.erp.server.oms.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.enums.FullyManagedPlatformStatusEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.server.oms.service.SoB2cDetailService;
import com.erp.server.oms.service.SoB2cService;
import com.sdk.oms.tiktok.dto.tiktok.order.FullyDeliveryOrderDTO;
import com.sdk.oms.tiktok.service.TikTokFullService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
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
    private SoB2cDetailService soB2cDetailService;
    @Resource
    private TikTokFullService tikTokFullService;
    /**
     * 查询送货单信息
     */
    @XxlJob("getDeliveryOrderByParam")
    public void getDeliveryOrderByParam() {
        XxlJobHelper.log("查询送货单信息开始执行");
        String jobParam = XxlJobHelper.getJobParam();
        //已发货 且 INBOUND: 已入库上架/INVAILD-已作废  中间查询【未发货不需要查或者已入库或已作废不需要查询】
        List<String> billStatusList = new ArrayList<>();
        billStatusList.add(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
        List<String> platformStatusList = new ArrayList<>();
        platformStatusList.add(FullyManagedPlatformStatusEnum.TikTokStatusEnum.INBOUND.getErpEnum().getCode());
        platformStatusList.add(FullyManagedPlatformStatusEnum.TikTokStatusEnum.INVAILD.getErpEnum().getCode());
        List<String> platformList = new ArrayList<>();
        platformList.add(PlatformDictEnum.TIK_TOK_FULLY.getCode());
        List<String> codeList = new ArrayList<>();
        if (CharSequenceUtil.isNotBlank(jobParam)) {
            codeList = Arrays.asList(jobParam.split("//,"));
        }
        List<SoB2cDTO.DeliveryDTO> deliveryDTOS = soB2cService.listDeliveryOrderByParam(billStatusList, platformStatusList,platformList,codeList);
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
            List<FullyDeliveryOrderDTO.DataDTO.DeliveryOrdersDTO> deliveryOrdersDTOS;
            try {
                deliveryOrdersDTOS = tikTokFullService.listDeliveryOrderByParam(shopId, deliveryNoList);
                XxlJobHelper.log("查询送货单信息执行完成 店铺：" + shopId + "查询结果：" + JSONUtil.toJsonStr(deliveryOrdersDTOS));
            }catch (Exception e){
                XxlJobHelper.log("查询送货单信息执行完成 店铺：" + shopId + "查询失败" + e.getMessage());
                continue;
            }
            if (CollUtil.isEmpty(deliveryOrdersDTOS)) {
                XxlJobHelper.log("查询送货单信息执行完成 店铺：" + shopId + "没有需要查询的订单");
                continue;
            }
            //根据订单号进行分组
            Map<String, FullyDeliveryOrderDTO.DataDTO.DeliveryOrdersDTO> orderMap = deliveryOrdersDTOS.stream().collect(Collectors.toMap(FullyDeliveryOrderDTO.DataDTO.DeliveryOrdersDTO::getCode, Function.identity()));
            for (Map.Entry<String, FullyDeliveryOrderDTO.DataDTO.DeliveryOrdersDTO> orderEntry : orderMap.entrySet()) {
                String transportNo = orderEntry.getKey();
                SoB2cDTO.DeliveryDTO deliveryDTO1 = deliveryDTOS.stream().filter(e -> e.getTransportNo().equals(transportNo)).findFirst().orElse(null);
                if (Objects.isNull(deliveryDTO1)) {
                    XxlJobHelper.log("查询送货单信息执行完成 运单号：" + transportNo + "没有对应的订单信息");
                    continue;
                }
                String soId = deliveryDTO1.getId();
                String code = deliveryDTO1.getCode();
                if (CharSequenceUtil.isBlank(soId)) {
                    XxlJobHelper.log("查询送货单信息执行完成 订单：" + code + "没有对应的全托管单");
                    continue;
                }
                List<SoB2cDTO.DeliveryDTO> detailList = deliveryDTOList.stream().filter(e -> e.getId().equals(soId)).collect(Collectors.toList());
                if (CollUtil.isEmpty(detailList)){
                    XxlJobHelper.log("查询送货单信息执行完成 订单：" + code + "没有对应的订单信息");
                    continue;
                }
                FullyDeliveryOrderDTO.DataDTO.DeliveryOrdersDTO deliveryOrdersDTO = orderEntry.getValue();
                List<FullyDeliveryOrderDTO.DataDTO.DeliveryOrdersDTO.SkusDTO> skus = deliveryOrdersDTO.getSkus();
                if (CollUtil.isEmpty(skus)) {
                    XxlJobHelper.log("查询送货单信息执行完成 订单：" + code + "没有对应的商品信息");
                    continue;
                }
                //订单明细扩展字段
                for (FullyDeliveryOrderDTO.DataDTO.DeliveryOrdersDTO.SkusDTO skusDTO : skus){
                    SoB2cDTO.DeliveryDTO deliveryDTO = detailList.stream().filter(e -> e.getId().equals(soId) && e.getPlatformSpuNo().equals(skusDTO.getPlatformSkuCode())).findFirst().orElse(null);
                    if (Objects.isNull(deliveryDTO)) {
                        XxlJobHelper.log("查询送货单信息执行完成 订单：" + code + "没有对应的" + skusDTO.getPlatformSkuCode() + "商品信息");
                        continue;
                    }
                    SoB2cDTO.ExtendDataDTO extendDataDTO = JSONUtil.toBean(deliveryDTO.getExtendDetailData(), SoB2cDTO.ExtendDataDTO.class);
                    extendDataDTO.setDeliveryQty(Objects.nonNull(skusDTO.getDeliveredQuantity()) ? skusDTO.getDeliveredQuantity() : 0);
                    extendDataDTO.setInstockQty(Objects.nonNull(skusDTO.getInboundQuantity()) ? skusDTO.getInboundQuantity() : 0);
                    extendDataDTO.setReceiveQty(Objects.nonNull(skusDTO.getReceivedQuantity()) ? skusDTO.getReceivedQuantity() : 0);
                    extendDataDTO.setReturnQty(Objects.nonNull(skusDTO.getReturnedQuantity()) ? skusDTO.getReturnedQuantity() : 0);
                    //根据平台sku和订单id更新扩展信息
                    soB2cDetailService.updateExtendData(deliveryDTO.getDetailId(), extendDataDTO);
                }
                //订单主表扩展字段
                SoB2cDTO.ExtendDataDTO extendDataDTO = JSONUtil.toBean(detailList.get(0).getExtendData(), SoB2cDTO.ExtendDataDTO.class);
                extendDataDTO.setDeliveryQty(Objects.nonNull(deliveryOrdersDTO.getDeliveredQuantity()) ? deliveryOrdersDTO.getDeliveredQuantity() : 0);
                extendDataDTO.setInstockQty(Objects.nonNull(deliveryOrdersDTO.getInboundQuantity()) ? deliveryOrdersDTO.getInboundQuantity() : 0);
                extendDataDTO.setReceiveQty(Objects.nonNull(deliveryOrdersDTO.getReceivedQuantity()) ? deliveryOrdersDTO.getReceivedQuantity() : 0);
                extendDataDTO.setReturnQty(Objects.nonNull(deliveryOrdersDTO.getReturnedQuantity()) ? deliveryOrdersDTO.getReturnedQuantity() : 0);
                //根据平台sku和订单id更新扩展信息
                soB2cService.updateExtendData(detailList.get(0).getId(), extendDataDTO);
            }
        }

    }
}
