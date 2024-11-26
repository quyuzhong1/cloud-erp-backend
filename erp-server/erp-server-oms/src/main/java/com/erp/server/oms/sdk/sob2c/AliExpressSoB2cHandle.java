package com.erp.server.oms.sdk.sob2c;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.annotation.PlatformSoB2cAnnotate;
import com.common.business.dto.PlatformDeliveryDetailDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformOrderLogisticsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.dto.DmpInoutDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.wms.dto.AliexpressDeliveryDTO;
import com.erp.model.wms.dto.AliexpressDeliveryDetailDTO;
import com.erp.model.wms.dto.WarehouseMappingDTO;
import com.erp.model.wms.enums.AliexpressDeliveryOrderStatusEnum;
import com.erp.oms.aliexpress.dto.response.AliExpressDeliveryDetail;
import com.erp.oms.aliexpress.service.AliExpressDliveryOrderService;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.wms.feign.AliexpressDeliveryFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.wms.feign.WarehouseMappingFeign;
import com.erp.server.oms.service.*;
import jnr.ffi.annotations.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 速卖通B2C订单处理
 *
 * @Author Jim
 * @Date 2024/03/21
 **/
@Slf4j
@Component
@PlatformSoB2cAnnotate(method = PlatformDictEnum.ALI_EXPRESS)
public class AliExpressSoB2cHandle extends AbstractSoB2cHandle {

    public static final String HISTORY = "history";
    @Resource
    private SoOutstockFeign soOutstockFeign;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private ShopAuthService shopAuthService;
    @Resource
    private AliexpressDeliveryFeign aliexpressDeliveryFeign;
    @Resource
    private SoB2cErrorService soB2cErrorService;
    @Resource
    private ShopInfoService shopInfoService;
    @Resource
    private PlatformOrderConsumerHandleService platformOrderConsumerHandleService;

    @Resource
    private WarehouseMappingFeign warehouseMappingFeign;

    @Resource
    private SkuMappingService skuMappingService;

    @Resource
    private SoB2cDetailService soB2cDetailService;
    @Resource
    private SoB2cService soB2cService;
    @Resource
    private AliExpressDliveryOrderService aliExpressDliveryOrderService;

    @Override
    public Boolean handleRule(SoB2cEntity mainEntity) {
        //平台仓订单不走任何规则
        if (Boolean.TRUE.equals(mainEntity.hasPlatformWarehouseOrder())) {
            return false;
        }
        try {
            platformOrderConsumerHandleService.handleRule(mainEntity);
        } catch (Exception e) {
            log.error("[速卖通订单规则处理失败]:order={},msg={}", mainEntity.getPlatformCode(), e.getMessage());
        }
        return true;
    }

    @Override
    public Boolean handleSoOutStock(PlatformOrderDTO dto, SoB2cDTO.PullOrderResultDTO resultDTO, SoB2cEntity mainEntity) {
        //重置发货明细
        dto.setDeliveryDetailDTOList(resetDeliveryDetail(dto.getDeliveryDetailDTOList()));
        if (CollUtil.isEmpty(dto.getDeliveryDetailDTOList())){
            return Boolean.TRUE;
        }
        //平台仓订单
        Boolean hasPlatformWarehouse = mainEntity.hasPlatformWarehouseOrder();
        // 非平台仓由发货单生成销售出库单--不用实现
        if (!Boolean.TRUE.equals(hasPlatformWarehouse)) {
            return Boolean.TRUE;
        }
        try {
            // 平台仓生成销售出库单
            this.aliExpressDeliveryQuery(dto, mainEntity);
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("[速卖处理销售出库失败]:order={},msg={}", dto.getPlatformCode(), e.getMessage(), e);
            SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
            addError.setType(SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode());
            addError.setParamJson("");
            addError.setReturnJson("");
            addError.setMainId(mainEntity.getId());
            addError.setMessage(e.getMessage());
            soB2cErrorService.add(addError);
            return Boolean.FALSE;
        }
    }

    /**
     * 根据状态重置明细记录
     * @param deliveryDetailDTOList
     * @return
     */
    private List<PlatformDeliveryDetailDTO> resetDeliveryDetail(List<PlatformDeliveryDetailDTO> deliveryDetailDTOList) {
        if (CollUtil.isEmpty(deliveryDetailDTOList)){
            return Collections.emptyList();
        }
        //速卖通 已发货/已签收创建销售出库单
        List<String> deliveryStatusNameList = new ArrayList<>();
        deliveryStatusNameList.add(AliexpressDeliveryOrderStatusEnum.SHIPPED.getName());
        deliveryStatusNameList.add(AliexpressDeliveryOrderStatusEnum.SIGNED.getName());
        return deliveryDetailDTOList.stream().filter(e -> deliveryStatusNameList.contains(e.getDeliveryStatusName())).collect(Collectors.toList());
    }

    /**
     * 速卖通发货查询，生成销售出库单
     * @param dto
     * @param mainEntity
     */
    public void aliExpressDeliveryQuery(PlatformOrderDTO dto, SoB2cEntity mainEntity) {
        //发货时间是否存在
        List<PlatformOrderLogisticsDTO> logisticsDTOS = dto.getLogisticsList().stream().filter(req -> req.getDeliveryTime() != null).collect(Collectors.toList());
        //没有发货时间不进行下一步操作
        if (CollUtil.isEmpty(logisticsDTOS)) {
            return;
        }
        //可能一个订单有多个发货单，并且sku 跟销售订单也不一致
        //根据仓库分组，同个仓库生成相同的销售出库单，销售出库单的sku和数量取速卖通返回的数据
        //根据平台sku查询Listing信息
        List<PlatformDeliveryDetailDTO> platformDeliveryDetailDTOList = dto.getDeliveryDetailDTOList();
        platformDeliveryDetailDTOList = platformDeliveryDetailDTOList.stream().filter(v -> StringUtils.isNotBlank(v.getPlatformWarehouseName())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(platformDeliveryDetailDTOList)) {
            return;
        }

        List<WarehouseMappingDTO.MappingViewDTO> mappingViewDTOS = warehouseMappingFeign.listMappingViewByDictPlatform(mainEntity.getDictPlatform());
        //设置销售订单id，在后面新增销售出库单时用到
        platformDeliveryDetailDTOList.forEach(v -> v.setMainId(mainEntity.getId()));
        Map<String, List<PlatformDeliveryDetailDTO>> map = platformDeliveryDetailDTOList.stream().collect(Collectors.groupingBy(PlatformDeliveryDetailDTO::getPlatformWarehouseName));
        map.forEach((key, val) -> autoGenerateSalesDelivery(dto, mainEntity, key, val, mappingViewDTOS, logisticsDTOS));
    }

    private void autoGenerateSalesDelivery(PlatformOrderDTO dto, SoB2cEntity mainEntity, String key, List<PlatformDeliveryDetailDTO> val, List<WarehouseMappingDTO.MappingViewDTO> mappingViewDTOS, List<PlatformOrderLogisticsDTO> logisticsDTOS) {
        //校验仓库是否匹配到
        WarehouseMappingDTO.MappingViewDTO mappingViewDTO = mappingViewDTOS.stream().filter(req -> key.equals(req.getThirdWarehouseName())).findFirst().orElse(null);
        if (Objects.isNull(mappingViewDTO)) {
            String msg = CharSequenceUtil.format("发货单仓库【{}】未匹配系统仓库", key);
            throw new ServiceException(msg);
        }
        List<String> platformSkuIdList = val.stream().map(PlatformDeliveryDetailDTO::getScItemId).distinct().collect(Collectors.toList());
        List<SkuMappingDTO.WarehouseSkuDTO> warehouseSkuDTOList = skuMappingService.listByWarehouseAndPlatformSku(mappingViewDTO.getWarehouseId(), platformSkuIdList);
        List<String> notMatchSkuNoList = new ArrayList<>();
        for (PlatformDeliveryDetailDTO deliveryDetailDTO : val) {
            SkuMappingDTO.WarehouseSkuDTO warehouseSkuDTO = warehouseSkuDTOList.stream().filter(v -> v.getPlatformSkuNo().equals(deliveryDetailDTO.getScItemId())).findFirst().orElse(new SkuMappingDTO.WarehouseSkuDTO());
            if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(warehouseSkuDTO.getProductSkuId())) {
                notMatchSkuNoList.add(deliveryDetailDTO.getScItemId());
                continue;
            }
            deliveryDetailDTO.setSkuId(warehouseSkuDTO.getProductSkuId());
            deliveryDetailDTO.setSkuNo(warehouseSkuDTO.getProductSkuNo());
            deliveryDetailDTO.setWarehouseId(mappingViewDTO.getWarehouseId());
            deliveryDetailDTO.setWarehouseName(mappingViewDTO.getWarehouseName());
            deliveryDetailDTO.setWarehouseOrgId(mappingViewDTO.getWarehouseOrgId());
            deliveryDetailDTO.setWarehouseOrgName(mappingViewDTO.getWarehouseOrgName());
        }
        val = val.stream().filter(v -> StringUtils.isNotBlank(v.getSkuId())).collect(Collectors.toList());
        List<String> skuIdList = val.stream().map(PlatformDeliveryDetailDTO::getSkuId).distinct().collect(Collectors.toList());
        PlatformGenerateSoOutstockDTO platformGenerateSoOutstockDTO = PlatformGenerateSoOutstockDTO.builder()
                .platformDeliveryDetailDTOList(val)
                .generateB2cDTO(soB2cService.getSoOutstockByIdAndWarehouseId(mainEntity.getId(), mappingViewDTO.getWarehouseId()))
                .build();
        //生成速卖通发货单
        addAliExpressDelivery(dto, mainEntity, logisticsDTOS, key, val);
        //生成销售出库单
        Boolean generateSoOutstockResult = soOutstockFeign.generateB2cSoOutstockByPlatformData(platformGenerateSoOutstockDTO);
        if (Boolean.TRUE.equals(generateSoOutstockResult)) {
            //封装映射的仓库信息
            soB2cDetailService.updateWarehouseByMapping(mappingViewDTO, mainEntity.getId(), skuIdList);
        }
        if (CollectionUtils.isNotEmpty(notMatchSkuNoList)) {
            String msg = CharSequenceUtil.format("自动生成销售出库单失败：存在速卖通货品id未映射sku，货品id:【{}】", notMatchSkuNoList);
            throw new ServiceException(msg);
        } else if (Boolean.TRUE.equals(generateSoOutstockResult)) {
            soB2cErrorService.deleteByCodeAndType(mainEntity.getCode(), SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode());
            soB2cService.removeSignError(mainEntity.getId(), SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode());
        }
    }

    /**
     * 新增速卖通发货单
     * @param dto
     * @param mainEntity
     * @param logisticsDTOS
     */
    private void addAliExpressDelivery(PlatformOrderDTO dto, SoB2cEntity mainEntity, List<PlatformOrderLogisticsDTO> logisticsDTOS, String warehouseName,List<PlatformDeliveryDetailDTO> detailDTOList) {
        AliexpressDeliveryDTO.AddDTO addDTO = new AliexpressDeliveryDTO.AddDTO();
        // 转化系统时区
        PlatformOrderLogisticsDTO logisticsDTO = logisticsDTOS.stream().findFirst().orElse(null);
        if (null == logisticsDTO){
            throw new ServiceException("发货时间为空");
        }
        LocalDateTime sourceDeliveryTime = logisticsDTO.getDeliveryTime();
        // 速卖通GMT时区转北京时区
        LocalDateTime targetDeliveryTime = DateUtil.convertZoneTime(sourceDeliveryTime,
                ZoneId.of("America/Los_Angeles"),
                ZoneId.of("Asia/Shanghai"));
        addDTO.setOutBoundTime(targetDeliveryTime);
        addDTO.setPlatformCode(mainEntity.getPlatformCode());
        addDTO.setSoId(mainEntity.getId());
        addDTO.setSoCode(mainEntity.getCode());
        addDTO.setShopId(mainEntity.getShopId());
        if (StringUtils.isNotBlank(mainEntity.getShopId())) {
            addDTO.setShopId(mainEntity.getShopId());
            ShopInfoEntity shopInfoEntity = shopInfoService.getById(mainEntity.getShopId());
            if (ObjectUtil.isNotEmpty(shopInfoEntity)) {
                addDTO.setShopName(shopInfoEntity.getName());
            }
        }
        addDTO.setTrackNo(logisticsDTOS.get(0).getCode());
        addDTO.setTradeCreateTime(dto.getPlatformOrderCreateTime());
        addDTO.setWarehouseName(warehouseName);
        addDTO.setPlatformDeliveryStatus(AliexpressDeliveryOrderStatusEnum.getCode(detailDTOList.get(0).getDeliveryStatusName()));
        List<AliexpressDeliveryDetailDTO.AddDTO> detailAddList = new ArrayList<>();
        for (PlatformDeliveryDetailDTO detailDTO : detailDTOList) {
            AliexpressDeliveryDetailDTO.AddDTO detailAddDTO = new AliexpressDeliveryDetailDTO.AddDTO();
            detailAddDTO.setOrderLineQty(detailDTO.getQty());
            detailAddDTO.setPlatformSku(detailDTO.getPlatformSkuNo());
            detailAddDTO.setSkuId(detailDTO.getSkuId());
            detailAddDTO.setSkuNo(detailDTO.getSkuNo());
            detailAddDTO.setPlatformDeliveryStatus(AliexpressDeliveryOrderStatusEnum.getCode(detailDTO.getDeliveryStatusName()));
            detailAddList.add(detailAddDTO);
        }
        addDTO.setDetailList(detailAddList);
        aliexpressDeliveryFeign.add(addDTO);
    }

    /**
     * 转换新中台刷新订单请求参数
     */
    @Override
    public List<DmpInoutDTO.CreateInputDTO> convertCreateInputDTOList(List orderEntityList){
        Map<String, List<SoB2cEntity>> shopGroupMap = ((List<SoB2cEntity>) orderEntityList).stream().collect(Collectors.groupingBy(SoB2cEntity::getShopId));
        return shopGroupMap.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(this::createAliExpressInputDTO))
                .collect(Collectors.toCollection(LinkedList::new));

    }


    /**
     * 转换速卖通订单拉取任务
     */
    private DmpInoutDTO.CreateInputDTO createAliExpressInputDTO(SoB2cEntity e) {
        DmpInoutDTO.CreateInputDTO dto = new DmpInoutDTO.CreateInputDTO();
        dto.setNextLevelId(e.getShopId());
        dto.setSystemCode(e.getDictPlatform());
        dto.setBillType(BusinessTypeEnum.ORDER.getCode());
        //  DmpInputTaskTaskTypeEnum	HISTORY("history", "历史任务"),
        dto.setTaskType(HISTORY);

        // 速卖通GMT时区转北京时区
        LocalDateTime targetOrderCreateTime = DateUtil.convertZoneTime(e.getPlatformOrderCreateTime(),
                ZoneId.of("America/Los_Angeles"),
                ZoneId.of("Asia/Shanghai"));
        LocalDateTime startTime = targetOrderCreateTime.minusSeconds(1);
        LocalDateTime endTime = targetOrderCreateTime.plusSeconds(1);
        dto.setStartTime(startTime);
        dto.setEndTime(endTime);

        // 构建 orderIdList 并封装为 JSON
        Map<String, List<String>> map = Collections.singletonMap(
                ORDER_ID_LIST,
                Collections.singletonList(e.getPlatformCode())
        );
        dto.setDetailExtendJson(JSON.toJSONString(map));
        return dto;
    }
}
