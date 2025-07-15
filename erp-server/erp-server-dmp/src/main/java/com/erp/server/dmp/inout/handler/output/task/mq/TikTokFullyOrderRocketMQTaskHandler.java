package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.*;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpSoDetailEntity;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.dmp.entity.DmpSoReceiverEntity;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.utils.DmpMappingUtils;
import com.erp.server.dmp.service.DmpCfgOutputConvertMappingService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
public class TikTokFullyOrderRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {
    @Resource
    private DmpCfgOutputConvertMappingService dmpCfgOutputConvertMappingService;

    @Resource
    private DmpMappingUtils dmpMappingUtils;

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpSoInfoEntity> dmpSoInfoEntityMap = new HashMap<>();
        Map<String, List<DmpSoDetailEntity>> dmpSoDetailEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_so_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoInfoEntity dmpSoInfoEntity = (DmpSoInfoEntity) v;
                        dmpSoInfoEntityMap.put(dmpSoInfoEntity.getId(), dmpSoInfoEntity);
                    }
                } else if ("dmp_so_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoDetailEntity dmpSoDetailEntity = (DmpSoDetailEntity) v;
                        String mainId = dmpSoDetailEntity.getMainId();
                        List<DmpSoDetailEntity> list = dmpSoDetailEntityMap.get(mainId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(dmpSoDetailEntity);
                        dmpSoDetailEntityMap.put(mainId, list);
                    }
                }
            }
        }

        Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
        Set<String> changeIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMap : changeConvertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = changeConvertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = changeConvertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_so_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                } else if ("dmp_so_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoDetailEntity dmpSoReturnDetailEntity = (DmpSoDetailEntity) v;
                        changeIds.add(dmpSoReturnDetailEntity.getMainId());
                    }
                }
            }
        }

        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for(String changId : changeIds) {
            PlatformOrderDTO orderDTO = this.convert(dmpSoInfoEntityMap.get(changId), dmpSoDetailEntityMap.get(changId)
                    , cfgOutputId);
            if(orderDTO != null) {
                map.put(changId, JSON.toJSONString(orderDTO));
            }
        }
        return map;
    }

    /**
     * 解析订单数据
     **/
    public PlatformOrderDTO convert(DmpSoInfoEntity dmpSoInfoEntity , List<DmpSoDetailEntity> dmpSoDetailEntityList , String cfgOutputId) {
        if(this.validateDataBlack(dmpSoInfoEntity, cfgOutputId)) {
            return null;
        }
        if(CollUtil.isEmpty(dmpSoDetailEntityList)) {
        	return null;
        }
        //设置对应关系
        PlatformOrderDTO orderDTO = new PlatformOrderDTO();

        //平台订单号
        orderDTO.setPlatformCode(dmpSoInfoEntity.getThirdCode());
        orderDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAID.getCode());
        orderDTO.setPayTime(dmpSoInfoEntity.getPlatformCreateTime());
        //销售平台
        orderDTO.setDictPlatform(PlatformDictEnum.TIK_TOK_FULLY.getCode());
        orderDTO.setPlatform(PlatformDictEnum.TIK_TOK_FULLY.getCode());
        // 店铺ID
        orderDTO.setShopId(dmpSoInfoEntity.getNextLevelId());

        //订单金额
        orderDTO.setAmount(dmpSoInfoEntity.getPayAmount());

        //币别
        orderDTO.setCurrency(dmpSoInfoEntity.getCurrencyCode());

        //买家备注
        orderDTO.setBuyerRemark(dmpSoInfoEntity.getBuyerRemark());

        // 是否拦截
        orderDTO.setIsIntercept(false);

        // 拦截备注
        orderDTO.setInterceptRemark("");

        // 来源类型
        orderDTO.setSourceType(SourceTypeEnum.SO_B2C.getCode());

        // 来源id
        orderDTO.setSourceId(dmpSoInfoEntity.getThirdCode());

        // 来源编码
        orderDTO.setSourceCode(dmpSoInfoEntity.getThirdCode());

        // 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
        orderDTO.setAbnormalType("");

        // 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
        orderDTO.setSyncKingdeeStatus("0");

        //付款状态
        if (dmpSoInfoEntity.getPayTime() != null) {
            orderDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAID.getCode());
        }

        buildExtend(dmpSoInfoEntity, orderDTO);

        orderDTO.setApproveStatusStr(dmpSoInfoEntity.getOrderStatus());
        orderDTO.setBillStatus(dmpSoInfoEntity.getDeliveryStatus());
        orderDTO.setInvalidStatus(dmpSoInfoEntity.getInvalidStatus());

        orderDTO.setIsCancel(dmpSoInfoEntity.getIsCancel());
        // 平台订单原始状态
        orderDTO.setPlatformOrderStatus(dmpSoInfoEntity.getPlatformOriginalStatus());

        //创建时间
        orderDTO.setPlatformOrderCreateTime(dmpSoInfoEntity.getPlatformCreateTime());

        // 订单明细
        List<PlatformOrderDetailDTO> details = parseDetailDto(dmpSoInfoEntity, dmpSoDetailEntityList);
        orderDTO.setDetails(details);

        //B2C销售订单物流信息表
        orderDTO.setLogisticsList(parseLogistics(dmpSoInfoEntity));
        return orderDTO;
    }

    private void buildExtend(DmpSoInfoEntity dmpSoInfoEntity, PlatformOrderDTO orderDTO) {
        JSONObject extendDataJson = JSONObject.parseObject(dmpSoInfoEntity.getExtendData());
        JSONObject label = new JSONObject();

        if (extendDataJson.containsKey("deliveryType")) {
            label.put("deliveryType", extendDataJson.get("deliveryType"));
        }
        if (extendDataJson.containsKey("emergencyLevel")) {
            label.put("priorityLevel", extendDataJson.get("emergencyLevel"));
        }

        if (extendDataJson.containsKey("isDeliver")) {
            label.put("isDeliver", extendDataJson.get("isDeliver"));
        }
        orderDTO.setLabelJson(label.toJSONString());

        JSONObject orderExtendJson = new JSONObject();
        orderExtendJson.put("deliveryQty", extendDataJson.containsKey("deliveryQty") && extendDataJson.get("deliveryQty") != null?extendDataJson.get("deliveryQty"):0);
        orderExtendJson.put("receiveQty", extendDataJson.containsKey("receiveQty") && extendDataJson.get("receiveQty") != null?extendDataJson.get("receiveQty"):0);
        orderExtendJson.put("instockQty", extendDataJson.containsKey("instockQty") && extendDataJson.get("instockQty") != null?extendDataJson.get("instockQty"):0);
        orderExtendJson.put("returnQty", extendDataJson.containsKey("returnQty") && extendDataJson.get("returnQty") != null?extendDataJson.get("returnQty"):0);
        orderExtendJson.put("orderQty", extendDataJson.containsKey("stockupQty") && extendDataJson.get("stockupQty") != null?extendDataJson.get("stockupQty"):0);
        orderDTO.setExtendData(orderExtendJson.toJSONString());

        PlatformOrderExtendDTO platformOrderExtendDTO = new PlatformOrderExtendDTO();
        if (extendDataJson.containsKey("requiredDeliveryTime") && Long.parseLong(extendDataJson.get("requiredDeliveryTime") + "") != 0) {
            LocalDateTime requiredDeliveryTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(extendDataJson.get("requiredDeliveryTime") + "")), ZoneId.systemDefault());

            platformOrderExtendDTO.setRequiredDeliveryTime(requiredDeliveryTime);
        }
        if (extendDataJson.containsKey("requiredReceiveTime") && Long.parseLong(extendDataJson.get("requiredReceiveTime") + "") != 0) {
            LocalDateTime requiredReceiveTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(extendDataJson.get("requiredReceiveTime") + "")), ZoneId.systemDefault());

            platformOrderExtendDTO.setRequiredReceiveTime(requiredReceiveTime);
        }
        if (extendDataJson.containsKey("orderSourceType")) {
            platformOrderExtendDTO.setOrderSourceType(extendDataJson.getString("orderSourceType"));
        }
        orderDTO.setExtend(platformOrderExtendDTO);
    }

    /**
     * 批量转换明细
     */
    public static List<PlatformOrderDetailDTO> parseDetailDto(DmpSoInfoEntity dmpSoInfoEntity, List<DmpSoDetailEntity> dmpSoDetailEntities) {
        //相同的sku和packageId合并去重
        Map<String, List<DmpSoDetailEntity>> collect = dmpSoDetailEntities.stream().collect(Collectors.groupingBy(DmpSoDetailEntity::getPlatformDetailId));

        return collect.entrySet().stream()
                .map(e -> intPlatformOrderDetailDTO(dmpSoInfoEntity, e.getValue(), e.getKey()))
                .collect(Collectors.toList());
    }


    /**
     * 转换明细
     */
    private static PlatformOrderDetailDTO intPlatformOrderDetailDTO(DmpSoInfoEntity dmpSoInfoEntity, List<DmpSoDetailEntity> soDetailEntityList, String sourceDetailId) {
        PlatformOrderDetailDTO detailDTO = new PlatformOrderDetailDTO();
        if (CollectionUtil.isEmpty(soDetailEntityList)) {
            return detailDTO;
        }
        DmpSoDetailEntity soDetailEntity = soDetailEntityList.get(0);
        // 图片URL
        detailDTO.setImageUrl("");
        // skuId
        detailDTO.setSkuId("");
        // skuNo
        detailDTO.setSkuNo("");

        //平台明细行号
        detailDTO.setPlatformLineNumber("");

        // 平台sku编号
        detailDTO.setPlatformSkuNo(soDetailEntity.getPlatformSku());

        //平台产品id
        detailDTO.setPlatformSpuNo(soDetailEntity.getThirdDetailId());

        // 库存sku编号
        detailDTO.setWarehouseName("");
        // 仓库名称
        // 库存是否扣除
        detailDTO.setWarehouseId("");
        // 数量
        detailDTO.setQty(soDetailEntity.getQty());

        // 金额
        BigDecimal salePrice = soDetailEntityList.stream().map(req -> req.getAfterAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
        detailDTO.setAmount(salePrice);
        // 单价
        detailDTO.setPrice(NumberUtil.toBigDecimal(soDetailEntity.getSellPriceOrigin()));
        // 币别（原币）
        detailDTO.setCurrency(dmpSoInfoEntity.getCurrencyCode());
        // 汇率
        detailDTO.setExchangeRate(BigDecimal.ZERO);
        // 建议售价（本位币）
        detailDTO.setAdvicePrice(BigDecimal.ZERO);
        // 含税成本（本位币）
        detailDTO.setTaxCost(BigDecimal.ZERO);

        // 来源明细id
        detailDTO.setSourceDetailId(sourceDetailId);

        List<String> thirdDetailIdList = soDetailEntityList.stream()
                .map(DmpSoDetailEntity::getThirdDetailId)
                .sorted()
                .collect(Collectors.toList());

        // 平台明细行
        detailDTO.setPlatformLineNumber(String.join(",", thirdDetailIdList));

        // 标签json
        detailDTO.setLabelJson("");
        detailDTO.setExtendData(soDetailEntity.getExtendData());
        // 库存组织id
        detailDTO.setWarehouseOrgId("");
        // 库存组织名称
        detailDTO.setWarehouseOrgName("");
        // 库位
        detailDTO.setWarehouseLocation("");
        //包裹号
        detailDTO.setPlatformPackageId(soDetailEntity.getPlatformPackageId());
        return detailDTO;
    }

    /**
     * 物流信息字段处理
     * @Author Luo_WG
     * @Date 2023/12/4 14:07
     * @param dmpSoInfoEntity
     * @return java.util.List<com.common.business.dto.PlatformOrderLogisticsDTO>
     **/
    private static List<PlatformOrderLogisticsDTO> parseLogistics(DmpSoInfoEntity dmpSoInfoEntity) {
        if (ObjectUtil.isEmpty(dmpSoInfoEntity)) {
            return Collections.emptyList();
        }

        String name = "";
        BigDecimal cost = BigDecimal.ZERO;


        List<PlatformOrderLogisticsDTO> logisticsDTOS = new ArrayList<>();
        PlatformOrderLogisticsDTO dto = PlatformOrderLogisticsDTO.builder()
//                .code(dmpSoInfoEntity.getLogisticsCode())
                .name(name)
                .deliveryTime(dmpSoInfoEntity.getDeliveryTime())
                .estimatedShippingCost(cost)
                .actualShippingCost(BigDecimal.ZERO)
                .accessoriesCostCurrency("")
                .actualShippingCurrency("")
                .estimatedShippingCurrency("")
                .logisticType(dmpSoInfoEntity.getLogisticType())
                .build();
        logisticsDTOS.add(dto);
        return logisticsDTOS;
    }



    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("platformCode");
    }
}
