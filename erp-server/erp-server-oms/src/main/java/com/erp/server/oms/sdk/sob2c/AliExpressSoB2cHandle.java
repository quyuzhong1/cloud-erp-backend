package com.erp.server.oms.sdk.sob2c;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.PlatformSoB2cAnnotate;
import com.common.business.dto.PlatformDeliveryDetailDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformOrderLogisticsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.CfgAppClientDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
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
public class AliExpressSoB2cHandle implements ISoB2cHandleService {

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
    @Autowired
    private AliExpressDliveryOrderService aliExpressDliveryOrderService;

    @Override
    public Boolean handleRule(SoB2cEntity mainEntity) {
        //平台仓订单不走任何规则
        if (mainEntity.hasPlatformWarehouseOrder()) {
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
    @Transactional(rollbackFor = Exception.class)
    public Boolean handleSoOutStock(PlatformOrderDTO dto, SoB2cDTO.PullOrderResultDTO resultDTO, SoB2cEntity mainEntity) {
        //平台仓订单
        Boolean hasPlatformWarehouse = mainEntity.hasPlatformWarehouseOrder();
        try {
            // 平台仓生成销售出库单
            // 非平台仓由发货单生成销售出库单
            if (hasPlatformWarehouse) {
                this.aliExpressDeliveryQuery(dto, resultDTO, mainEntity);
            }
            return true;
        } catch (Exception e) {
            log.error("[速卖处理销售出库失败]:order={},msg={}", dto.getPlatformCode(), e.getMessage() , e);
            SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
            addError.setType(SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode());
            addError.setParamJson("");
            addError.setReturnJson("");
            addError.setMainId(mainEntity.getId());
            addError.setMessage(e.getMessage());
            soB2cErrorService.add(addError);
        }
        return false;
    }

    /**
     * 速卖通发货查询，生成销售出库单
     * @param dto
     * @param resultDTO
     * @param mainEntity
     */
    public void aliExpressDeliveryQuery(PlatformOrderDTO dto, SoB2cDTO.PullOrderResultDTO resultDTO, SoB2cEntity mainEntity) {
        //如果有发货时间
        List<PlatformOrderLogisticsDTO> logisticsDTOS = dto.getLogisticsList().stream().filter(req -> req.getDeliveryTime() != null).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(logisticsDTOS)) {

            //可能一个订单有多个发货单，并且sku 跟销售订单也不一致
            //根据仓库分组，同个仓库生成相同的销售出库单，销售出库单的sku和数量取速卖通返回的数据
            //根据平台sku查询Listing信息
            List<PlatformDeliveryDetailDTO> platformDeliveryDetailDTOList = dto.getDeliveryDetailDTOList();
            platformDeliveryDetailDTOList = platformDeliveryDetailDTOList.stream().filter(v->StringUtils.isNotBlank(v.getPlatformWarehouseName())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(platformDeliveryDetailDTOList)){
                return;
            }

            List<WarehouseMappingDTO.MappingViewDTO> mappingViewDTOS =  warehouseMappingFeign.listMappingViewByDictPlatform(mainEntity.getDictPlatform());
            //设置销售订单id，在后面新增销售出库单时用到
            platformDeliveryDetailDTOList.forEach(v->v.setMainId(mainEntity.getId()));
            Map<String,List<PlatformDeliveryDetailDTO>> map = platformDeliveryDetailDTOList.stream().collect(Collectors.groupingBy(PlatformDeliveryDetailDTO::getPlatformWarehouseName));
            map.forEach((key,val)->{
                //校验仓库是否匹配到
                WarehouseMappingDTO.MappingViewDTO mappingViewDTO = mappingViewDTOS.stream().filter(req -> key.equals(req.getThirdWarehouseName())).findFirst().orElse(null);
                if(Objects.isNull(mappingViewDTO)){
                    SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
                    addError.setType(SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode());
                    addError.setParamJson("");
                    addError.setReturnJson("");
                    addError.setMainId(mainEntity.getId());
                    addError.setMessage(StrUtil.format("发货单仓库【{}】未匹配系统仓库", key));
                    soB2cErrorService.add(addError);
                    return;
                }
                List<String> platformSkuIdList = val.stream().map(PlatformDeliveryDetailDTO::getScItemId).distinct().collect(Collectors.toList());
                List<SkuMappingDTO.WarehouseSkuDTO> warehouseSkuDTOList = skuMappingService.listByWarehouseAndPlatformSku(mappingViewDTO.getWarehouseId(),platformSkuIdList);
                List<String> notMatchSkuNoList = new ArrayList<>();
                for (PlatformDeliveryDetailDTO deliveryDetailDTO : val) {
                    SkuMappingDTO.WarehouseSkuDTO warehouseSkuDTO = warehouseSkuDTOList.stream().filter(v->v.getPlatformSkuNo().equals(deliveryDetailDTO.getScItemId())).findFirst().orElse(new SkuMappingDTO.WarehouseSkuDTO());
                    if(com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(warehouseSkuDTO.getProductSkuId())){
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
                val = val.stream().filter(v->StringUtils.isNotBlank(v.getSkuId())).collect(Collectors.toList());
                List<String> skuIdList = val.stream().map(PlatformDeliveryDetailDTO::getSkuId).distinct().collect(Collectors.toList());
                PlatformGenerateSoOutstockDTO platformGenerateSoOutstockDTO = PlatformGenerateSoOutstockDTO.builder()
                        .platformDeliveryDetailDTOList(val)
                        .generateB2cDTO(soB2cService.getSoOutstockByIdAndWarehouseId(mainEntity.getId(),mappingViewDTO.getWarehouseId()))
                        .build();
                //生成销售出库单
                Boolean generateSoOutstockResult = soOutstockFeign.generateB2cSoOutstockByPlatformData(platformGenerateSoOutstockDTO);
                if(generateSoOutstockResult){
                    //封装映射的仓库信息
                    soB2cDetailService.updateWarehouseByMapping(mappingViewDTO,mainEntity.getId(),skuIdList);
                    //生成速卖通发货单
                    addAliExpressDelivery(dto, mainEntity, logisticsDTOS, key,val);
                }
                if(CollectionUtils.isNotEmpty(notMatchSkuNoList)){
                    SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
                    addError.setType(SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode());
                    addError.setParamJson("");
                    addError.setReturnJson("");
                    addError.setMainId(mainEntity.getId());
                    addError.setMessage(StrUtil.format("自动生成销售出库单失败：存在速卖通货品id未映射sku，货品id:【{}】", notMatchSkuNoList));
                    soB2cErrorService.add(addError);
                }else if (generateSoOutstockResult){
                    soB2cErrorService.deleteByCodeAndType(mainEntity.getCode(),SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode());
                    soB2cService.removeSignError(mainEntity.getId(),SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode());
                }
            });
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
//        addDTO.setOutBoundTime(logisticsDTOS.get(0).getDeliveryTime());
        // 转化系统时区
        PlatformOrderLogisticsDTO logisticsDTO = logisticsDTOS.stream().findFirst().orElse(null);
        if (null == logisticsDTO){
            ServiceException.runError("发货时间为空");
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
        List<AliexpressDeliveryDetailDTO.AddDTO> detailAddList = new ArrayList<>();
        for (PlatformDeliveryDetailDTO detailDTO : detailDTOList) {
            AliexpressDeliveryDetailDTO.AddDTO detailAddDTO = new AliexpressDeliveryDetailDTO.AddDTO();
            detailAddDTO.setOrderLineQty(detailDTO.getQty());
            detailAddDTO.setPlatformSku(detailDTO.getPlatformSkuNo());
            detailAddDTO.setSkuId(detailDTO.getSkuId());
            detailAddDTO.setSkuNo(detailDTO.getSkuNo());
            detailAddList.add(detailAddDTO);
        }
        addDTO.setDetailList(detailAddList);
        aliexpressDeliveryFeign.add(addDTO);
    }

    /**
     * 获取速卖通平台店铺授权信息+
     * @param shopId
     * @return
     */
    private Map<String, String> getAliExpressCfgClientMap(String shopId) {
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.ALI_EXPRESS_LOGISTICS;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient = null;
        try {
            cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        } catch (Exception e) {
            log.error("erp-dmp服务dmpTaskFeign.getCfgAppClient接口异常：{}", e.getMessage());
            return new HashMap<>();
        }
        if (Objects.isNull(cfgAppClient)) return new HashMap<>();
        Map<String, String> map = new HashMap<>();
        map.put("clientSecret", cfgAppClient.getClientSecret());
        map.put("clientId", cfgAppClient.getClientId());
        map.put("url", cfgAppClient.getUrl());
        if (StringUtils.isNotBlank(shopId)) {
            ShopAuthEntity shopAuth = shopAuthService.getByShopId(shopId);
            if (Objects.nonNull(shopAuth)) {
                map.put("shopId", shopAuth.getShopId());
                map.put("token", shopAuth.getAccessToken());
            }
        }
        return map;
    }
}
