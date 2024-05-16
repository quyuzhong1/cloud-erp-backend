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
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.wms.feign.AliexpressDeliveryFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.wms.feign.WarehouseMappingFeign;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
    private SkuMappingFeign skuMappingFeign;

    @Resource
    private SoB2cDetailService soB2cDetailService;
    @Resource
    private SoB2cService soB2cService;

    @Override
    @Transactional(rollbackFor = Exception.class)
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
//        Boolean isWarehouseEmpty = resultDTO.getIsWarehouseEmpty();
        //已发货
        String shipped = SoB2cBillStatusEnum.ENUM_SHIPPED.getCode();
        String billStatus = mainEntity.getBillStatus();
        boolean isShipped = shipped.equals(billStatus);
        try {
            // 平台仓生成销售出库单
            // 非平台仓由发货单生成销售出库单
            if (isShipped && hasPlatformWarehouse) {
                this.aliExpressDeliveryQuery(dto, resultDTO, mainEntity);
            }
            return true;
        } catch (Exception e) {
            log.error("[速卖处理销售出库失败]:order={},msg={}", dto.getPlatformCode(), e.getMessage());
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
        String warehouseName = "";
        if (CollectionUtils.isNotEmpty(dto.getDetails())) {
            warehouseName = dto.getDetails().get(0).getWarehouseName();
        }
        //如果有发货时间
        List<PlatformOrderLogisticsDTO> logisticsDTOS = dto.getLogisticsList().stream().filter(req -> req.getDeliveryTime() != null).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(logisticsDTOS) && StringUtils.isNotBlank(warehouseName)) {
            if (resultDTO.getIsWarehouseEmpty()) {
                return;
            }
            //可能一个订单有多个发货单，并且sku 跟销售订单也不一致
            //根据仓库分组，同个仓库生成相同的销售出库单，销售出库单的sku和数量取速卖通返回的数据
            //根据平台sku查询Listing信息
            List<PlatformDeliveryDetailDTO> platformDeliveryDetailDTOList = dto.getDeliveryDetailDTOList();
            List<String> platformSkuIdList = platformDeliveryDetailDTOList.stream().map(PlatformDeliveryDetailDTO::getPlatformSkuId).collect(Collectors.toList());
            List<String> platformSpuList = platformDeliveryDetailDTOList.stream().map(PlatformDeliveryDetailDTO::getPlatformSpuNo).collect(Collectors.toList());
            Map<String, List<ListingInfoWithSkuMappingDTO>> skuMappingMap = soB2cDetailService.mapListingByPlatformSkuId(platformSkuIdList, platformSpuList, mainEntity.getDictPlatform(), mainEntity.getShopId(), null, null);

            List<WarehouseMappingDTO.MappingViewDTO> mappingViewDTOS =  warehouseMappingFeign.listMappingViewByDictPlatform(mainEntity.getDictPlatform());
            //设置销售订单id，在后面新增销售出库单时用到
            platformDeliveryDetailDTOList.forEach(v->v.setMainId(mainEntity.getId()));
            Map<String,List<PlatformDeliveryDetailDTO>> map = platformDeliveryDetailDTOList.stream().collect(Collectors.groupingBy(PlatformDeliveryDetailDTO::getWarehouseName));
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
                for (PlatformDeliveryDetailDTO deliveryDetailDTO : val) {
                    // 映射关系
                    List<ListingInfoWithSkuMappingDTO> mappingDTOList = skuMappingMap.get(deliveryDetailDTO.getPlatformSkuId());
                    // 检查和获取映射关系
                    ListingInfoWithSkuMappingDTO mappingDTO = soB2cDetailService.checkAndMappingDTO(mappingDTOList, deliveryDetailDTO.getPlatformSpuNo(), mainEntity.getDictPlatform());

                    if(Objects.isNull(mappingDTO) || StringUtils.isBlank(mappingDTO.getProductSkuId())){
                        SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
                        addError.setType(SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode());
                        addError.setParamJson("");
                        addError.setReturnJson("");
                        addError.setMainId(mainEntity.getId());
                        addError.setMessage(StrUtil.format("自动生成销售出库单失败：订单未匹配Sku映射关系,sku:【{}】", deliveryDetailDTO.getPlatformSkuNo()));
                        soB2cErrorService.add(addError);
                        return;
                    }
                    deliveryDetailDTO.setSkuId(mappingDTO.getProductSkuId());
                    deliveryDetailDTO.setSkuNo(mappingDTO.getProductSkuNo());
                    deliveryDetailDTO.setWarehouseId(mappingViewDTO.getWarehouseId());
                    deliveryDetailDTO.setWarehouseName(mappingViewDTO.getWarehouseName());
                    deliveryDetailDTO.setWarehouseOrgId(mappingViewDTO.getWarehouseOrgId());
                    deliveryDetailDTO.setWarehouseOrgName(mappingViewDTO.getWarehouseOrgName());
                }
                List<String> skuIdList = val.stream().map(PlatformDeliveryDetailDTO::getSkuId).distinct().collect(Collectors.toList());
                PlatformGenerateSoOutstockDTO platformGenerateSoOutstockDTO = PlatformGenerateSoOutstockDTO.builder()
                        .platformDeliveryDetailDTOList(platformDeliveryDetailDTOList)
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
            });
        } else {
            //如果仓库名称为空表示没找到速卖通发货单，记录异常订单，这里的WarehouseName是速卖通仓库名称
            if (StringUtils.isBlank(warehouseName)) {
                SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
                addError.setType(SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode());
                addError.setParamJson("");
                addError.setReturnJson("");
                addError.setMainId(mainEntity.getId());
                addError.setMessage("未查询到平台发货单或发货单未出库");
                soB2cErrorService.add(addError);
                return;
            }

            //是否匹配到仓库，是空表示未绑定速卖通仓库，记录异常订单
            if (resultDTO.getIsWarehouseEmpty()) {
                SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
                addError.setType(SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode());
                addError.setParamJson("");
                addError.setReturnJson("");
                addError.setMainId(mainEntity.getId());
                addError.setMessage(StrUtil.format("发货单仓库【{}】未匹配系统仓库", resultDTO.getWarehouseName()));
                soB2cErrorService.add(addError);
                return;
            }
        }
        return;
    }

    /**
     * 新增速卖通发货单
     * @param dto
     * @param mainEntity
     * @param logisticsDTOS
     */
    private void addAliExpressDelivery(PlatformOrderDTO dto, SoB2cEntity mainEntity, List<PlatformOrderLogisticsDTO> logisticsDTOS, String warehouseName,List<PlatformDeliveryDetailDTO> detailDTOList) {
        AliexpressDeliveryDTO.AddDTO addDTO = new AliexpressDeliveryDTO.AddDTO();
        addDTO.setOutBoundTime(logisticsDTOS.get(0).getDeliveryTime());
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
