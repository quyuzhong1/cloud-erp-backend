package com.erp.server.oms.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformOrderDetailDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.CalculateRuleEnum;
import com.erp.model.oms.enums.CalculateSizeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
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
                if (PlatformDictEnum.ALI_EXPRESS.getCode().equals(mainEntity.getDictPlatform()) && hasPlatformWarehouse) {
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
            //根据sku进行计算
            calculateSize(detailList, skuList, skuIds, maxLength, maxWidth, totalHeight);
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

    /**
     * 计算长宽高
     * @param detailList
     * @param skuList
     * @param skuIds
     * @param maxLength
     * @param maxWidth
     * @param totalHeight
     */
    private void calculateSize(List<SoB2cDetailEntity> detailList,List<SkuVO> skuList, List<String> skuIds, BigDecimal maxLength, BigDecimal maxWidth, BigDecimal totalHeight) {
        if (CollectionUtils.isEmpty(skuIds) || CollectionUtils.isEmpty(skuList) || CollectionUtils.isEmpty(detailList)){
            return;
        }
        Boolean isCombination = Boolean.FALSE;
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listHistoryBomChildBySkuIds(skuIds);
        List<String> parentSkuIds = null;
        if (CollectionUtils.isNotEmpty(bomChildrenSkuDTOS)){
            isCombination = true;
            parentSkuIds = bomChildrenSkuDTOS.stream().map(BomChildrenSkuDTO::getParentSkuId).distinct().collect(Collectors.toList());
            //计算sku尺寸
            buildProductSize(bomChildrenSkuDTOS);
        }
        List<String> keyList = new ArrayList<>();
        keyList.add(CalculateSizeEnum.LENGTH.getCode());
        keyList.add(CalculateSizeEnum.WIDTH.getCode());
        keyList.add(CalculateSizeEnum.HEIGHT.getCode());
        List<DictBasicEntity> byKeyList = dictBasicService.getByKeyList(keyList);
        if (isCombination){
            Map<String, Integer> skuQty = detailList.stream()
                    .filter(e -> StringUtils.isNotEmpty(e.getSkuId())).distinct()
                    .collect(Collectors.toMap(SoB2cDetailEntity::getSkuId, SoB2cDetailEntity::getQty,Integer::sum));
            List<String> finalParentSkuIds = parentSkuIds;
            //组合时 计算需要排除存在父sku数据
            Map<String, String> collect = byKeyList.stream().collect(Collectors.toMap(DictBasicEntity::getType, DictBasicEntity::getValue));
            String length = collect.get(CalculateSizeEnum.LENGTH.getCode());
            if (CalculateRuleEnum.SUM.getCode().equalsIgnoreCase(length)){
                BigDecimal maxLength1 = skuList.stream().filter(e -> !finalParentSkuIds.contains(e.getSkuId()))
                        .map(e -> e.getLength().multiply(BigDecimal.valueOf(skuQty.get(e.getSkuId())))).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                BigDecimal maxLength2 = bomChildrenSkuDTOS.stream().filter(e -> Objects.nonNull(e.getLength()))
                        .map(e -> e.getLength().multiply(BigDecimal.valueOf(e.getQuantity())).multiply(BigDecimal.valueOf(skuQty.get(e.getParentSkuId()))))
                        .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                maxLength = maxLength1.add(maxLength2);
            }else {
                BigDecimal maxLength1  = skuList.stream().filter(e -> !finalParentSkuIds.contains(e.getSkuId()) && Objects.nonNull(e.getLength()))
                        .map(SkuVO::getLength)
                        .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                BigDecimal maxLength2  = bomChildrenSkuDTOS.stream()
                        .map(BomChildrenSkuDTO::getLength)
                        .filter(Objects::nonNull)
                        .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                if (CalculateRuleEnum.MIN.getCode().equalsIgnoreCase(length)){
                    maxLength = maxLength1.compareTo(maxLength2) > 0 ?  maxLength2: maxLength1;
                }else {
                    maxLength = maxLength1.compareTo(maxLength2) > 0 ? maxLength1: maxLength2;
                }
            }
            String width = collect.get(CalculateSizeEnum.WIDTH.getCode());
            if (CalculateRuleEnum.SUM.getCode().equalsIgnoreCase(width)){
                BigDecimal maxWidth1 = skuList.stream().filter(e -> !finalParentSkuIds.contains(e.getSkuId()))
                        .map(e -> e.getWidth().multiply(BigDecimal.valueOf(skuQty.get(e.getSkuId())))).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                BigDecimal maxWidth2 = bomChildrenSkuDTOS.stream().filter(e -> Objects.nonNull(e.getWidth()))
                        .map(e -> e.getWidth().multiply(BigDecimal.valueOf(e.getQuantity())).multiply(BigDecimal.valueOf(skuQty.get(e.getParentSkuId()))))
                        .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                maxWidth = maxWidth1.add(maxWidth2);
            }else {
                BigDecimal maxWidth1  = skuList.stream().filter(e -> !finalParentSkuIds.contains(e.getSkuId()) && Objects.nonNull(e.getWidth()))
                        .map(SkuVO::getWidth)
                        .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                BigDecimal maxWidth2  = bomChildrenSkuDTOS.stream().map(BomChildrenSkuDTO::getWidth)
                        .filter(Objects::nonNull)
                        .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                if (CalculateRuleEnum.MIN.getCode().equalsIgnoreCase(length)){
                    maxWidth = maxWidth1.compareTo(maxWidth2) > 0 ?  maxWidth2: maxWidth1;
                }else {
                    maxWidth = maxWidth1.compareTo(maxWidth2) > 0 ? maxWidth1: maxWidth2;
                }
            }
            String height = collect.get(CalculateSizeEnum.HEIGHT.getCode());
            if (CalculateRuleEnum.SUM.getCode().equalsIgnoreCase(height)){
                BigDecimal totalHeight1 = skuList.stream().filter(e -> !finalParentSkuIds.contains(e.getSkuId()))
                        .map(e -> e.getHeight().multiply(BigDecimal.valueOf(skuQty.get(e.getSkuId())))).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                BigDecimal totalHeight2 = bomChildrenSkuDTOS.stream().filter(e -> Objects.nonNull(e.getHeight()))
                        .map(e -> e.getHeight().multiply(BigDecimal.valueOf(e.getQuantity())).multiply(BigDecimal.valueOf(skuQty.get(e.getParentSkuId()))))
                        .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                totalHeight = totalHeight1.add(totalHeight2);
            }else {
                BigDecimal totalHeight1  = skuList.stream().filter(e -> !finalParentSkuIds.contains(e.getSkuId()) && Objects.nonNull(e.getHeight()))
                        .map(SkuVO::getHeight)
                        .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                BigDecimal totalHeight2  = bomChildrenSkuDTOS.stream()
                        .map(BomChildrenSkuDTO::getHeight)
                        .filter(Objects::nonNull)
                        .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                if (CalculateRuleEnum.MIN.getCode().equalsIgnoreCase(length)){
                    totalHeight = totalHeight1.compareTo(totalHeight2) > 0 ?  totalHeight2: totalHeight1;
                }else {
                    totalHeight = totalHeight1.compareTo(totalHeight2) > 0 ? totalHeight1: totalHeight2;
                }
            }
        }else {
            Map<String, String> collect = byKeyList.stream().collect(Collectors.toMap(DictBasicEntity::getType, DictBasicEntity::getValue));
            //长
            String length = collect.get(CalculateSizeEnum.LENGTH.getCode());
            if (StringUtils.isNotEmpty(length) && CalculateRuleEnum.SUM.getCode().equalsIgnoreCase(length)){
                maxLength = skuList.stream().map(SkuVO::getLength)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            }else if (StringUtils.isNotEmpty(length) && CalculateRuleEnum.MIN.getCode().equalsIgnoreCase(length)){
                maxLength = skuList.stream().map(SkuVO::getLength)
                        .filter(Objects::nonNull)
                        .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            }else {
                maxLength = skuList.stream().map(SkuVO::getLength)
                        .filter(Objects::nonNull)
                        .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            }
            //宽
            String width = collect.get(CalculateSizeEnum.WIDTH.getCode());
            if (StringUtils.isNotEmpty(width) && CalculateRuleEnum.SUM.getCode().equalsIgnoreCase(width)){
                maxWidth = skuList.stream().map(SkuVO::getWidth)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            }else if (StringUtils.isNotEmpty(width) && CalculateRuleEnum.MIN.getCode().equalsIgnoreCase(width)){
                maxWidth = skuList.stream().map(SkuVO::getWidth)
                        .filter(Objects::nonNull)
                        .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            }else {
                maxWidth = skuList.stream().map(SkuVO::getWidth)
                        .filter(Objects::nonNull)
                        .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            }
            //高
            String height = collect.get(CalculateSizeEnum.HEIGHT.getCode());
            if (StringUtils.isNotEmpty(height) && CalculateRuleEnum.MAX.getCode().equalsIgnoreCase(height)){
                totalHeight  = skuList.stream().map(SkuVO::getHeight)
                        .filter(Objects::nonNull)
                        .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            }else if (StringUtils.isNotEmpty(height) && CalculateRuleEnum.MIN.getCode().equalsIgnoreCase(height)){
                totalHeight  = skuList.stream().map(SkuVO::getHeight)
                        .filter(Objects::nonNull)
                        .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            }else {
                totalHeight  = skuList.stream().map(SkuVO::getHeight)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            }
        }
    }

    private void buildProductSize(List<BomChildrenSkuDTO> bomChildrenSkuDTOS) {
        bomChildrenSkuDTOS.forEach(bomChildrenSkuDTO -> {
            String productSize = bomChildrenSkuDTO.getProductSize();
            if (StringUtils.isEmpty(productSize)){
                bomChildrenSkuDTO.setLength(BigDecimal.ZERO);
                bomChildrenSkuDTO.setWidth(BigDecimal.ZERO);
                bomChildrenSkuDTO.setHeight(BigDecimal.ZERO);
            }else {
                String[] xes = productSize.split("X");
                if (xes.length > 2){
                    bomChildrenSkuDTO.setLength(new BigDecimal(xes[0]));
                    bomChildrenSkuDTO.setWidth(new BigDecimal(xes[1]));
                    bomChildrenSkuDTO.setHeight(new BigDecimal(xes[2]));
                }else if (xes.length > 1){
                    bomChildrenSkuDTO.setLength(new BigDecimal(xes[0]));
                    bomChildrenSkuDTO.setWidth(new BigDecimal(xes[1]));
                    bomChildrenSkuDTO.setHeight(BigDecimal.ZERO);
                }else if (xes.length > 0){
                    bomChildrenSkuDTO.setLength(new BigDecimal(xes[0]));
                    bomChildrenSkuDTO.setWidth(BigDecimal.ZERO);
                    bomChildrenSkuDTO.setHeight(BigDecimal.ZERO);
                }else {
                    bomChildrenSkuDTO.setLength(BigDecimal.ZERO);
                    bomChildrenSkuDTO.setWidth(BigDecimal.ZERO);
                    bomChildrenSkuDTO.setHeight(BigDecimal.ZERO);
                }
            }
        });
    }
}
