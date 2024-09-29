package com.erp.server.oms.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.common.core.utils.LengthConverterUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SplitSkuDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.CalculateSizeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuInfoSimpleVO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.SysRefererConfigEntity;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
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
    private DmpMongoDbFeign dmpMongoDbFeign;
    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private SkuMappingService skuMappingService;
    @Lazy
    @Resource
    private PlatformOrderConsumerHandleService platformOrderConsumerHandleService;




    @Override
    public void handleAll(PlatformOrderDTO dto) {
        // 已有出库详情/不保存订单
        // 2024-03-25允许所有来源订单处理
//        Boolean hasDeliveryDetail = Boolean.FALSE;
//        if (StringUtils.isNotEmpty(dto.getPlatformCode()) && StringUtils.isNotEmpty(dto.getDictPlatform())){
//            hasDeliveryDetail = dmpMongoDbFeign.checkHasDeliveryDetail(dto.getPlatformCode(), dto.getDictPlatform());
//        }
//        if (hasDeliveryDetail){
//            log.warn("已存在对应销售出库单不新增：单号={}", dto.getPlatformCode());
//            return;
//        }
        // 跳过未作废的自发货无地址的订单
        if ( notPlatformOrderNotExistAddress(dto)
                && null != dto.getInvalidStatus()
                && !dto.getInvalidStatus()
        ) {
            log.warn("卖家自发货订单无地址暂不新增：单号={}", dto.getPlatformCode());
            return;
        }

        SoB2cDTO.PullOrderResultDTO resultDTO = platformOrderConsumerHandleService.checkAndSaveAll(dto);
        SoB2cEntity mainEntity = resultDTO.getSoB2cEntity();
        //平台仓订单
        Boolean hasPlatformWarehouse = mainEntity.hasPlatformWarehouseOrder();
        Boolean isWarehouseEmpty = resultDTO.getIsWarehouseEmpty();
        //已发货
        String shipped = SoB2cBillStatusEnum.ENUM_SHIPPED.getCode();
        String billStatus = mainEntity.getBillStatus();
        Boolean isShipped = shipped.equals(billStatus);
        //如果已发货且仓库为空且是平台仓订单
        if (isShipped && isWarehouseEmpty && hasPlatformWarehouse && !PlatformDictEnum.ALI_EXPRESS.getCode().equals(mainEntity.getDictPlatform())) {
            String warehouseId = resultDTO.getShopWarehouseId();
            if(StringUtils.isNotBlank(warehouseId)){
              soB2cDetailService.updateWarehouseIdByMainId(mainEntity.getId(),warehouseId,true);
            }
        }
        // 平台仓订单不走任何规则
        // 取消订单不走规则
        if (!mainEntity.hasPlatformWarehouseOrder()
                && !mainEntity.getIsCancel()
                && !ApproveStatusEnum.REJECT.equals(mainEntity.getApproveStatus())
        ) {
            // 已审核过的订单不走规则
            Integer count = operateLogService.lambdaQuery()
                    .eq(OperateLogEntity::getBusinessId, mainEntity.getId())
                    .eq(OperateLogEntity::getOperation, "审核操作")
                    .count();
            if (0 == count){
                // 规则处理(分平台)
                SoB2cHandler.handleRule(mainEntity);
            }
        }

        // 销售出库单处理(分平台)
        SoB2cHandler.handleSoOutStock(dto, resultDTO, mainEntity);

        //平台取消订单后自动取消预报
        if(Objects.nonNull(mainEntity.getIsCancel()) && mainEntity.getIsCancel()){
            soB2cService.autoCancelOrderForecast(mainEntity);
        }

        // 非平台
        if (!mainEntity.hasPlatformWarehouseOrder()
                && resultDTO.isUpdateCancel()
                && SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode().equalsIgnoreCase(mainEntity.getBillStatus())
                && !mainEntity.getIsIntercept()
        ){
            soB2cService.deliveryIntercept(mainEntity.getId(), "平台取消");
        }


        //走过订单规则审核的不需要重复推送DMP，规则审核时已经推送过
        Integer count = operateLogService.lambdaQuery()
                .eq(OperateLogEntity::getBusinessId, mainEntity.getId())
                .eq(OperateLogEntity::getOperation, "审核操作")
                .count();
        if (0 == count) {
            //推送到DMP
            soB2cService.syncOrderToDmp(mainEntity.getId(), SyncOperateEnum.OPERATE_UPDATE.getCode());
        }

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
//    @Transactional(rollbackFor = Exception.class)
//    @GlobalTransactional(rollbackFor = Exception.class)
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
    public SoB2cDTO.PullOrderResultDTO checkAndSaveAll(PlatformOrderDTO dto) {
        // 查询关联关系
        List<String> platformSkuList = dto.convertPlatformSkuList();

        // 速卖通同店铺存在相同SkuNo需要配合平台产ID/SPU查询
        List<String> platformSpuList = new LinkedList<>();
        if (PlatformDictEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(dto.getPlatform())
                || PlatformDictEnum.MERCADOLIBRE.getCode().equalsIgnoreCase(dto.getPlatform())
                || PlatformDictEnum.SHOPIFY.getCode().equalsIgnoreCase(dto.getPlatform())
                || PlatformDictEnum.TIK_TOK.getCode().equalsIgnoreCase(dto.getPlatform())){
            platformSpuList = dto.convertPlatformSpuList();
        }

        Map<String, List<ListingInfoWithSkuMappingDTO>> listingInfoWithSkuMappingDTOMap = skuMappingService.mapListingByPlatformSkuNo(platformSkuList, platformSpuList, dto.getDictPlatform(), dto.getShopId(), dto.getPlatformOrderCreateTime(), null);

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
        List<DictCountryEntity> countryList = sysDictFeign.listCountryByIdsOrAlpha3(countryIds);

        List<String> skuIds = listingInfoWithSkuMappingDTOMap.values().stream()
                .flatMap(List::stream)
                .map(ListingInfoWithSkuMappingDTO::getProductSkuId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<SkuInfoSimpleVO> skuList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(skuIds)) {
            skuList = plmTaskFeign.getSimpleSkuInfoByIds(skuIds);
        }

        // 主表更新或保存
        SoB2cDTO.PullOrderResultDTO resultDTO = soB2cService.saveOrUpdateEntity(dto, shopInfo);
        SoB2cEntity mainEntity = resultDTO.getSoB2cEntity();
        resultDTO.setShopWarehouseId(shopInfo.getWarehouseId());
        // 详情更新或保存
        List<SoB2cDetailEntity> detailList = soB2cDetailService.saveOrUpdateEntity(dto, mainEntity, listingInfoWithSkuMappingDTOMap, shopInfo, skuList);

        Boolean isWarehouseEmpty = detailList.stream().filter(d -> StringUtils.isBlank(d.getWarehouseId())).count() > 0;
        resultDTO.setIsWarehouseEmpty(isWarehouseEmpty);
        resultDTO.setWarehouseName(detailList.get(MathUtil.ZERO).getWarehouseName());

        if (CollectionUtils.isEmpty(detailList)){
            // 拆分后无平台来源明细不更新
            log.warn("[B2C订单消费] 平台订单【{}】：拆分后无平台来源明细不更新", dto.getPlatformCode());
            return resultDTO;
        }

        // 毛重(捆绑商品按拆分后计算)
        BigDecimal allNetWeight = BigDecimal.ZERO;
        //长宽高计算
        BigDecimal maxLength = BigDecimal.ZERO;
        BigDecimal maxWidth = BigDecimal.ZERO;
        BigDecimal totalHeight = BigDecimal.ZERO;
        if (CollectionUtils.isNotEmpty(skuList)){
            //拆分明细
            List<SplitSkuDTO> splitSkuDTOS = soB2cService.splitBySoDetail(detailList, skuIds, mainEntity.getCode(), true);
            //根据sku进行计算
            List<String> keyList = new ArrayList<>();
            keyList.add(CalculateSizeEnum.LENGTH.getCode());
            keyList.add(CalculateSizeEnum.WIDTH.getCode());
            keyList.add(CalculateSizeEnum.HEIGHT.getCode());
            keyList.add(CalculateSizeEnum.GROSS_WEIGHT.getCode());
            List<DictBasicEntity> byKeyList = dictBasicService.getByKeyList(keyList);
            Map<String, String> collect = byKeyList.stream().collect(Collectors.toMap(DictBasicEntity::getType, DictBasicEntity::getValue));
            maxLength = SplitSkuDTO.calculateSplitSkuDTOLength(splitSkuDTOS,collect.get(CalculateSizeEnum.LENGTH.getCode()));
            maxWidth = SplitSkuDTO.calculateSplitSkuDTOWidth(splitSkuDTOS,collect.get(CalculateSizeEnum.WIDTH.getCode()));
            totalHeight = SplitSkuDTO.calculateSplitSkuDTOHeight(splitSkuDTOS,collect.get(CalculateSizeEnum.HEIGHT.getCode()));
            allNetWeight = SplitSkuDTO.calculateSplitSkuDTOGrossWeight(splitSkuDTOS, collect.get(CalculateSizeEnum.GROSS_WEIGHT.getCode()));
        }
        //物流信息更新保存
        SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsService.saveOrUpdateEntity(dto, mainEntity, allNetWeight,maxLength,maxWidth,totalHeight);
        //买家信息更新保存
        SoB2cReceiverEntity receiverEntity = soB2cReceiverService.saveOrUpdateEntity(dto, mainEntity, countryList);

        //财务信息更新保存
        soB2cFinanceService.saveOrUpdateEntity(dto, mainEntity, logisticsEntity, detailList);

        //客户信息
        CustomerB2cEntity customerB2cEntity = customerB2cService.saveOrUpdateEntity(dto, mainEntity, receiverEntity, shopInfo.getDictCountryCode(), countryList);

        customerB2cAddressService.saveOrUpdateEntity(dto, customerB2cEntity, receiverEntity);

        customerB2cContactService.saveOrUpdateEntity(dto, customerB2cEntity, receiverEntity);

        receiverEntity.setCustomerId(customerB2cEntity.getId());
        soB2cReceiverService.saveOrUpdate(receiverEntity);
//        if (!soB2cReceiverService.saveOrUpdate(receiverEntity)) {
//            throw new ServiceException("[SoB2cReceiverEntity] 保存失败");
//        }
        return resultDTO;
    }

    private List<SplitSkuDTO> splitBySoDetail(List<SoB2cDetailEntity> detailList, List<SkuInfoSimpleVO> skuList) {
        if (CollectionUtils.isEmpty(detailList)){
            return Collections.emptyList();
        }
        Map<String, SkuInfoSimpleVO> sourceSkuMap = skuList.stream().collect(Collectors.toMap(SkuInfoSimpleVO::getSkuId, Function.identity()));

        List<String> skuIds = detailList.stream().map(SoB2cDetailEntity::getSkuId).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);
        List<SplitSkuDTO> splitSkuDTOS = new ArrayList<>();
        detailList.forEach(addDTO -> {
            BomChildrenSkuDTO skuVO = bomChildrenSkuDTOS.stream().filter(e -> StrUtil.isNotEmpty(e.getParentSkuId()) && StrUtil.isNotEmpty(e.getParentSkuNo()) && e.getParentSkuId().equals(addDTO.getSkuId()))
                    .findFirst().orElse(null);
            if (Objects.nonNull(skuVO) && StringUtils.isNotEmpty(skuVO.getType()) && BomTypeEnum.COMBINATION.getType().equals(skuVO.getType())){
                //组合品时进行拆分
                List<BomChildrenSkuDTO> childrenSkuDTOS = bomChildrenSkuDTOS.stream().filter(e -> Objects.nonNull(e.getParentSkuId()) && addDTO.getSkuId().equals(e.getParentSkuId()))
                        .collect(Collectors.toList());

                //子sku数量需要乘订单数量
                childrenSkuDTOS.forEach(bomChildrenSkuDTO -> {
                    splitSkuDTOS.add(SplitSkuDTO.builder().skuId(addDTO.getSkuId()).qty(addDTO.getQty() * bomChildrenSkuDTO.getQuantity())
                            .skuNo( StrUtil.isNotEmpty(bomChildrenSkuDTO.getSkuNo()) ? bomChildrenSkuDTO.getSkuNo() : "")
                            .length( Objects.nonNull(bomChildrenSkuDTO.getLength()) ? LengthConverterUtil.mmToCm(bomChildrenSkuDTO.getLength()) : BigDecimal.ZERO)
                            .width( Objects.nonNull(bomChildrenSkuDTO.getWidth()) ? LengthConverterUtil.mmToCm(bomChildrenSkuDTO.getWidth()) : BigDecimal.ZERO)
                            .height( Objects.nonNull(bomChildrenSkuDTO.getHeight()) ? LengthConverterUtil.mmToCm(bomChildrenSkuDTO.getHeight()) : BigDecimal.ZERO)
                            .build());
                });
            }else {

                SkuInfoSimpleVO simpleSkuVO = sourceSkuMap.get(addDTO.getSkuId());
                if (null == simpleSkuVO){
                    // 部分无映射关系设置为空
                    splitSkuDTOS.add(new SplitSkuDTO(addDTO.getSkuId(), addDTO.getSkuNo()));
                } else {
                    skuVO = BomChildrenSkuDTO.builder()
                            .skuId(simpleSkuVO.getSkuId())
                            .length(simpleSkuVO.getProductLength())
                            .width(simpleSkuVO.getProductWidth())
                            .height(simpleSkuVO.getProductHeight())
                            .build();
                    splitSkuDTOS.add(SplitSkuDTO.builder().skuId(addDTO.getSkuId()).qty(addDTO.getQty())
                            .skuNo(Objects.nonNull(skuVO) && StrUtil.isNotEmpty(skuVO.getSkuNo()) ? skuVO.getSkuNo() : "")
                            .length(Objects.nonNull(skuVO) && Objects.nonNull(skuVO.getLength()) ? LengthConverterUtil.mmToCm(skuVO.getLength()) : BigDecimal.ZERO)
                            .width(Objects.nonNull(skuVO) && Objects.nonNull(skuVO.getWidth()) ? LengthConverterUtil.mmToCm(skuVO.getWidth()) : BigDecimal.ZERO)
                            .height(Objects.nonNull(skuVO) && Objects.nonNull(skuVO.getHeight()) ? LengthConverterUtil.mmToCm(skuVO.getHeight()) : BigDecimal.ZERO)
                            .build());
                }
            }
        });
        return splitSkuDTOS;
    }

    /**
     * 自发货订单不存在地址
     */
    private boolean notPlatformOrderNotExistAddress(PlatformOrderDTO dto) {
        if (PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(dto.getDictPlatform())) {
            return this.checkHasMfnOrderAndNoAddress(dto);
        }
        if (PlatformDictEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(dto.getDictPlatform())) {
            return this.aliExpressNotPlatformOrderNotExistAddress(dto);
        }
        return false;
    }

    /**
     * 速卖通自发货订单未解密地址
     */
    private boolean aliExpressNotPlatformOrderNotExistAddress(PlatformOrderDTO dto) {
        if (StrUtil.isNotBlank(dto.getLabelJson())) {
            SoB2cDTO.LabelDTO labelJsonDTO = JSONUtil.toBean(dto.getLabelJson(), SoB2cDTO.LabelDTO.class);
            Boolean isAliexpressPlatformWarehouseOrder = labelJsonDTO.getIsPlatformWarehouseOrder();
            if (isAliexpressPlatformWarehouseOrder){
                return false;
            }
            if (null == dto.getReceiver()){
                return false;
            }
            return StringUtils.isNotBlank(dto.getReceiver().getFullAddress()) && dto.getReceiver().getFullAddress().contains("***");
        }
        return false;
    }
}
