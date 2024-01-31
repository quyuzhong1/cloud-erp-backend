package com.erp.server.oms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformOrderDetailDTO;
import com.common.business.dto.PlatformOrderLogisticsDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SplitSkuDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.CalculateSizeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.wms.dto.AliexpressDeliveryDTO;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.wms.feign.AliexpressDeliveryFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.wms.feign.WarehouseMappingFeign;
import com.erp.server.oms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 平台消费处理接口
 *
 * @param
 * @author Jim
 * @date 2023/12/18 16:42
 * @Return
 */
@Slf4j
@Service
public class PlatformOrderConsumerHandleServiceImpl implements PlatformOrderConsumerHandleService {

    @Resource
    private SoB2cService soB2cService;
    @Resource
    private SoB2cDetailService soB2cDetailService;
    @Resource
    private SoB2cLogisticsService soB2cLogisticsService;
    @Resource
    private SoB2cReceiverService soB2cReceiverService;
    @Resource
    private SoB2cFinanceService soB2cFinanceService;

    @Resource
    private CustomerB2cService customerB2cService;
    @Resource
    private CustomerB2cAddressService customerB2cAddressService;
    @Resource
    private CustomerB2cContactService customerB2cContactService;

    @Resource
    private ShopInfoService shopInfoService;
    @Resource
    private SysDictFeign sysDictFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Resource
    private DmpMongoDbFeign dmpMongoDbFeign;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private ShopAuthService shopAuthService;

    @Resource
    private AliexpressDeliveryFeign aliexpressDeliveryFeign;

    @Resource
    private WarehouseMappingFeign warehouseMappingFeign;

    @Override
    public void handleAll(PlatformOrderDTO dto) {
        // 已有出库详情/不保存订单
        Boolean hasDeliveryDetail = Boolean.FALSE;
        if (StringUtils.isNotEmpty(dto.getPlatformCode()) && StringUtils.isNotEmpty(dto.getDictPlatform())){
            hasDeliveryDetail = dmpMongoDbFeign.checkHasDeliveryDetail(dto.getPlatformCode(), dto.getDictPlatform());
        }
        if (hasDeliveryDetail){
            log.warn("已存在对应销售出库单不新增：单号={}", dto.getPlatformCode());
            return;
        }
        // 亚马逊, 跳过MFN时，地址为空的订单
        if (PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(dto.getDictPlatform()) && this.checkHasMfnOrderAndNoAddress(dto) ) {
            log.warn("亚马逊卖家自发货订单无地址暂不新增：单号={}", dto.getPlatformCode());
            return;
        }

        SoB2cDTO.PullOrderResultDTO resultDTO = this.checkAndSaveAll(dto);
        SoB2cEntity mainEntity = resultDTO.getSoB2cEntity();
        //平台仓订单
        Boolean hasPlatformWarehouse = mainEntity.hasPlatformWarehouseOrder();
        Boolean isWarehouseEmpty = resultDTO.getIsWarehouseEmpty();
        //已发货
        String shipped = SoB2cBillStatusEnum.ENUM_SHIPPED.getCode();
        String billStatus = mainEntity.getBillStatus();
        Boolean isShipped = shipped.equals(billStatus);
        //如果已发货且仓库为空且是平台仓订单
        if (isShipped && isWarehouseEmpty && hasPlatformWarehouse) {
            String warehouseId = resultDTO.getShopWarehouseId();
            if(StringUtils.isNotBlank(warehouseId)){
              soB2cDetailService.updateWarehouseIdByMainId(mainEntity.getId(),warehouseId,true);
            }
        }
        try {
            if (Objects.nonNull(mainEntity)) {

                //速卖通平台仓订单不走任何规则
                if (PlatformDictEnum.ALI_EXPRESS.getCode().equals(mainEntity.getDictPlatform()) && hasPlatformWarehouse && isShipped) {
                    //如果有发货时间
                    List<PlatformOrderLogisticsDTO> logisticsDTOS = dto.getLogisticsList().stream().filter(req -> req.getDeliveryTime() != null).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(logisticsDTOS)) {
                        //生成销售出库单
                        soOutstockFeign.generateB2cSoOutstock(mainEntity.getId());

                        //生成速卖通发货单
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
                        addDTO.setTradeCreateTime(dto.getOrderCreateTime());
                        aliexpressDeliveryFeign.add(addDTO);
                    } else {
                        //查询速卖通仓库名称是否映射ERP仓库
                        String warehouseName = dto.getDetails().get(0).getWarehouseName();

                    }
                    return;
                }

                handleRule(mainEntity);
                //如果是已发货且是平台仓订单 就生成销售出库单
                if (isShipped && hasPlatformWarehouse) {
                    soOutstockFeign.generateB2cSoOutstock(mainEntity.getId());
                }
            }

        } catch (Exception e) {
            log.error("[订单规则处理失败]:order={},msg={}", dto.getPlatformCode(), e.getMessage());
        }

    }

    private void test (SoB2cEntity soB2cEntity) {


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

    /**
     * 检查亚马逊卖家自发货订单无地址
     */
    private Boolean checkHasMfnOrderAndNoAddress(PlatformOrderDTO dto) {
        if (StrUtil.isNotBlank(dto.getLabelJson())) {
            SoB2cDTO.LabelDTO labelJsonDTO = JSONUtil.toBean(dto.getLabelJson(), SoB2cDTO.LabelDTO.class);
            //FBA
            if ("AFN".equalsIgnoreCase(labelJsonDTO.getFulfillmentChannel())){
                return false;
            }
        }
        if (null == dto.getReceiver()){
            return true;
        }
        return StringUtils.isBlank(dto.getReceiver().getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void handleRule(SoB2cEntity mainEntity) {
        //订单状态
        String billStatus = mainEntity.getBillStatus();

        String id = mainEntity.getId();
        //是否是平台仓订单 true 是
        Boolean isPlatformWarehouseOrder = mainEntity.hasPlatformWarehouseOrder();

        //付款状态
        String payStatus = mainEntity.getPayStatus();
        //已付款
        String paid = SoB2cPayStatusEnum.ENUM_PAID.getCode();
        //自动匹配订单规则 待配貨和已付款 就要订单规则
        if (SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equalsIgnoreCase(billStatus)
                && paid.equalsIgnoreCase(payStatus)
                && !mainEntity.getInvalidStatus()) {
            List<SoB2cDetailEntity> detailList = soB2cDetailService.listByMainId(id);
            Map<String, Object> map = soB2cService.handleMatchJson(id, detailList, new HashMap<>());
            if (isPlatformWarehouseOrder) {
                soB2cService.platformWarehouseOrderHandle(id, map);
            } else {
                //拉取订单正常处理
                soB2cService.pullOrderHandle(id, detailList, map);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public SoB2cDTO.PullOrderResultDTO checkAndSaveAll(PlatformOrderDTO dto) {
        // 查询关联关系
        List<String> platformSkuList = dto.getDetails()
                .stream()
                .map(PlatformOrderDetailDTO::getPlatformSkuNo)
                .distinct().collect(Collectors.toList());
        Map<String, List<ListingInfoWithSkuMappingDTO>> listingInfoWithSkuMappingDTOMap = soB2cDetailService.mapListingByPlatformSkuNo(platformSkuList, dto.getDictPlatform(), dto.getShopId());

        // 查询当前店铺信息
        ShopInfoEntity shopInfo = shopInfoService.getById(dto.getShopId());
        if (null == shopInfo) {
            throw new ServiceException("未找到订单的店铺" + dto.getShopId());
        }
        // 查询国家信息
        List<String> countryIds;
        if (Objects.nonNull(dto.getReceiver())){
            countryIds = Stream.of(shopInfo.getDictCountryCode(), dto.getReceiver().getCountry()).distinct().collect(Collectors.toList());
        }else {
            countryIds = Stream.of(shopInfo.getDictCountryCode()).collect(Collectors.toList());
        }
        List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(countryIds);

        List<String> skuIds = listingInfoWithSkuMappingDTOMap.values().stream()
                .flatMap(List::stream)
                .map(ListingInfoWithSkuMappingDTO::getProductSkuId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<SkuVO> skuList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(skuIds)) {
            skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        }

        // 主表更新或保存
        SoB2cDTO.PullOrderResultDTO resultDTO = soB2cService.saveOrUpdateEntity(dto);
        SoB2cEntity mainEntity = resultDTO.getSoB2cEntity();
        resultDTO.setShopWarehouseId(shopInfo.getWarehouseId());
        // 详情更新或保存
        List<SoB2cDetailEntity> detailList = soB2cDetailService.saveOrUpdateEntity(dto, mainEntity, listingInfoWithSkuMappingDTOMap, shopInfo, skuList);
        Boolean isWarehouseEmpty = detailList.stream().filter(d -> StringUtils.isBlank(d.getWarehouseId())).count() > 0;
        resultDTO.setIsWarehouseEmpty(isWarehouseEmpty);
        // 净重
        BigDecimal allNetWeight = detailList.stream().map(SoB2cDetailEntity::getCurrentNetWeight).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        //长宽高计算
        BigDecimal maxLength = BigDecimal.ZERO;
        BigDecimal maxWidth = BigDecimal.ZERO;
        BigDecimal totalHeight = BigDecimal.ZERO;
        if (CollectionUtils.isNotEmpty(skuList)){
            //拆分明细
            List<SplitSkuDTO> splitSkuDTOS = splitBySoDetail(detailList,skuList,skuIds);
            //根据sku进行计算
            List<String> keyList = new ArrayList<>();
            keyList.add(CalculateSizeEnum.LENGTH.getCode());
            keyList.add(CalculateSizeEnum.WIDTH.getCode());
            keyList.add(CalculateSizeEnum.HEIGHT.getCode());
            List<DictBasicEntity> byKeyList = dictBasicService.getByKeyList(keyList);
            Map<String, String> collect = byKeyList.stream().collect(Collectors.toMap(DictBasicEntity::getType, DictBasicEntity::getValue));
            maxLength = soB2cService.calculateSplitSkuDTOLength(splitSkuDTOS,collect.get(CalculateSizeEnum.LENGTH.getCode()));
            maxWidth = soB2cService.calculateSplitSkuDTOWidth(splitSkuDTOS,collect.get(CalculateSizeEnum.WIDTH.getCode()));
            totalHeight = soB2cService.calculateSplitSkuDTOHeight(splitSkuDTOS,collect.get(CalculateSizeEnum.HEIGHT.getCode()));
        }
        //物流信息更新保存
        SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsService.saveOrUpdateEntity(dto, mainEntity, allNetWeight,maxLength,maxWidth,totalHeight);
        //买家信息更新保存
        SoB2cReceiverEntity receiverEntity = soB2cReceiverService.saveOrUpdateEntity(dto, mainEntity, countryList);

        //财务信息更新保存
        soB2cFinanceService.saveOrUpdateEntity(dto, mainEntity, logisticsEntity, detailList);

        //客户信息
        // 根据平台和名称判断
        CustomerB2cEntity customerB2cEntity = customerB2cService.findByPlatformAndName(dto.getDictPlatform(), receiverEntity.getName(), SourceTypeEnum.SO_B2C.getCode());

        customerB2cEntity = customerB2cService.saveOrUpdateEntity(customerB2cEntity, dto, mainEntity, receiverEntity, shopInfo.getDictCountryCode(), countryList);

        customerB2cAddressService.saveOrUpdateEntity(dto, customerB2cEntity, receiverEntity);

        customerB2cContactService.saveOrUpdateEntity(dto, customerB2cEntity, receiverEntity);

        receiverEntity.setCustomerId(customerB2cEntity.getId());
        if (!soB2cReceiverService.saveOrUpdate(receiverEntity)) {
            throw new ServiceException("[SoB2cReceiverEntity] 保存失败");
        }
        return resultDTO;
    }

    private List<SplitSkuDTO> splitBySoDetail(List<SoB2cDetailEntity> detailList, List<SkuVO> skuVOList, List<String> skuIds) {
        if (CollectionUtils.isEmpty(detailList)){
            return Collections.emptyList();
        }
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);
        List<SplitSkuDTO> splitSkuDTOS = new ArrayList<>();
        if(CollectionUtils.isEmpty(bomChildrenSkuDTOS)){
            //不存在拆分sku
            detailList.forEach(addDTO -> {
                SkuVO skuVO = skuVOList.stream().filter(e -> StrUtil.isNotEmpty(e.getSkuId()) && StrUtil.isNotEmpty(e.getSkuNo()) && e.getSkuId().equals(addDTO.getSkuId()))
                        .findFirst().orElse(null);
                splitSkuDTOS.add(SplitSkuDTO.builder().skuId(addDTO.getSkuId()).qty(addDTO.getQty())
                        .skuNo(Objects.nonNull(skuVO) && StrUtil.isNotEmpty(skuVO.getSkuNo()) ? skuVO.getSkuNo() : "")
                        .length(Objects.nonNull(skuVO) && Objects.nonNull(skuVO.getLength()) ? skuVO.getLength() : BigDecimal.ZERO)
                        .width(Objects.nonNull(skuVO) && Objects.nonNull(skuVO.getWidth()) ? skuVO.getWidth() : BigDecimal.ZERO)
                        .height(Objects.nonNull(skuVO) && Objects.nonNull(skuVO.getHeight()) ? skuVO.getHeight() : BigDecimal.ZERO)
                        .build());
            });
        }else {
            soB2cService.buildProductSize(bomChildrenSkuDTOS);
            detailList.forEach(addDTO -> {
                List<BomChildrenSkuDTO> childrenSkuDTOS = bomChildrenSkuDTOS.stream().filter(e -> Objects.nonNull(e.getParentSkuId()) && addDTO.getSkuId().equals(e.getParentSkuId())
                        && BomTypeEnum.COMBINATION.getType().equals(e.getType()))
                        .collect(Collectors.toList());
                if (CollectionUtils.isEmpty(childrenSkuDTOS)){
                    //不存在子sku
                    SkuVO skuVO = skuVOList.stream().filter(e -> StrUtil.isNotEmpty(e.getSkuId()) && StrUtil.isNotEmpty(e.getSkuNo()) && e.getSkuId().equals(addDTO.getSkuId()))
                            .findFirst().orElse(null);
                    splitSkuDTOS.add(SplitSkuDTO.builder().skuId(addDTO.getSkuId()).qty(addDTO.getQty())
                            .skuNo(Objects.nonNull(skuVO) && StrUtil.isNotEmpty(skuVO.getSkuNo()) ? skuVO.getSkuNo() : "")
                            .length(Objects.nonNull(skuVO) && Objects.nonNull(skuVO.getLength()) ? skuVO.getLength() : BigDecimal.ZERO)
                            .width(Objects.nonNull(skuVO) && Objects.nonNull(skuVO.getWidth()) ? skuVO.getWidth() : BigDecimal.ZERO)
                            .height(Objects.nonNull(skuVO) && Objects.nonNull(skuVO.getHeight()) ? skuVO.getHeight() : BigDecimal.ZERO)
                            .build());
                }else {
                    //子sku数量需要乘订单数量
                    childrenSkuDTOS.forEach(bomChildrenSkuDTO -> {
                        splitSkuDTOS.add(SplitSkuDTO.builder().skuId(addDTO.getSkuId()).qty(addDTO.getQty() * bomChildrenSkuDTO.getQuantity())
                                .skuNo( StrUtil.isNotEmpty(bomChildrenSkuDTO.getSkuNo()) ? bomChildrenSkuDTO.getSkuNo() : "")
                                .length( Objects.nonNull(bomChildrenSkuDTO.getLength()) ? bomChildrenSkuDTO.getLength() : BigDecimal.ZERO)
                                .width( Objects.nonNull(bomChildrenSkuDTO.getWidth()) ? bomChildrenSkuDTO.getWidth() : BigDecimal.ZERO)
                                .height( Objects.nonNull(bomChildrenSkuDTO.getHeight()) ? bomChildrenSkuDTO.getHeight() : BigDecimal.ZERO)
                                .build());
                    });
                }
            });
        }
        return splitSkuDTOS;
    }
}
