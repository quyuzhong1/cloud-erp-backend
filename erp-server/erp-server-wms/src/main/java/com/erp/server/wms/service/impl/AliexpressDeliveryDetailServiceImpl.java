package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.common.business.dto.PlatformDeliveryDTO;
import com.common.business.dto.PlatformDeliveryDetailDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.AliexpressDeliveryDetailDTO;
import com.erp.model.wms.entity.AliexpressDeliveryDetailEntity;
import com.erp.model.wms.entity.AliexpressDeliveryEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.AliexpressDeliveryDetailMapper;
import com.erp.server.wms.service.AliexpressDeliveryDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 速卖通发货单详情 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-05-06
 */
@Slf4j
@Service
public class AliexpressDeliveryDetailServiceImpl extends SuperServiceImpl<AliexpressDeliveryDetailMapper, AliexpressDeliveryDetailEntity> implements AliexpressDeliveryDetailService {


    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean addOrUpdate(List<AliexpressDeliveryDetailDTO.AddDTO> addDTO, AliexpressDeliveryEntity mainEntity, List<PlatformDeliveryDTO> sourceAllDeliveryList) {
        if(CollectionUtils.isEmpty(addDTO)){
            return true;
        }
        if (addDTO.stream().anyMatch(e-> StringUtils.isBlank(e.getUniqueId()))){
            ServiceException.runError("速卖通发货明细唯一ID为空,平台单号【{}】", mainEntity.getPlatformCode());
        }
        if (addDTO.stream().anyMatch(e-> StringUtils.isBlank(e.getPlatformSpuNo()))){
            ServiceException.runError("速卖通发货明细产品ID为空,平台单号【{}】", mainEntity.getPlatformCode());
        }
        //先删除后新增
//        this.removeByMainId(addDTO.get(0).getMainId());
        // 补充明细ID
        List<AliexpressDeliveryDetailEntity> aliexpressDeliveryDetailEntityList = fillData(addDTO, mainEntity, sourceAllDeliveryList);

        log.info("开始新增/更新速卖通发货单详情");
        boolean save = super.saveOrUpdateBatch(aliexpressDeliveryDetailEntityList);
        if(!save) {
            throw new ServiceException("速卖通发货单详情新增/更新失败");
        }
        return true;
    }

    /**
     * 补充信息
     */
    private List<AliexpressDeliveryDetailEntity> fillData(List<AliexpressDeliveryDetailDTO.AddDTO> addDTO,
                                                          AliexpressDeliveryEntity mainEntity,
                                                          List<PlatformDeliveryDTO> sourceAllDeliveryList) {
        List<AliexpressDeliveryDetailEntity> aliexpressDeliveryDetailEntityList = BeanUtil.copyToList(addDTO,AliexpressDeliveryDetailEntity.class);
        String mainId = mainEntity.getId();
        String platformCode = mainEntity.getPlatformCode();
        // 查询历史已存在明细
        List<String> detailUniqueIds = addDTO.stream().map(AliexpressDeliveryDetailDTO.AddDTO::getUniqueId).distinct().collect(Collectors.toList());
        Map<String, AliexpressDeliveryDetailEntity> existDetailMap = lambdaQuery()
                .eq(AliexpressDeliveryDetailEntity::getMainId, mainId)
                .in(AliexpressDeliveryDetailEntity::getUniqueId, detailUniqueIds)
                .list()
                .stream()
                .collect(Collectors.toMap(AliexpressDeliveryDetailEntity::getUniqueId, e -> e));
        if (!existDetailMap.isEmpty()) {
            // 根据唯一ID设置已存在ID
            for (AliexpressDeliveryDetailEntity aliexpressDeliveryDetailEntity : aliexpressDeliveryDetailEntityList) {
                AliexpressDeliveryDetailEntity existDetailEntity = existDetailMap.get(aliexpressDeliveryDetailEntity.getUniqueId());
                if(null != existDetailEntity){
                    aliexpressDeliveryDetailEntity.setId(existDetailEntity.getId());
                    aliexpressDeliveryDetailEntity.setCreateTime(existDetailEntity.getCreateTime());
                }
            }
        }
        // 补充发货价格信息和来源明细ID
        // 查询主单
        List<SoB2cEntity> soB2cEntityList = FeignQuery.create(SoB2cEntity.class)
                .eq(SoB2cEntity::getPlatformCode, platformCode)
                .list();
        if (CollectionUtils.isEmpty(soB2cEntityList)) {
            ServiceException.runError("【速卖通发货生成】:未找到B2C销售订单：平台单号【{}】", platformCode);
        }
        List<String> soIds = soB2cEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<SoB2cDetailEntity> soB2cDetailEntityList = FeignQuery.create(SoB2cDetailEntity.class)
                .in(SoB2cDetailEntity::getMainId, soIds)
                .list();
        if (CollectionUtils.isEmpty(soB2cDetailEntityList)) {
            ServiceException.runError("【速卖通发货生成】:未找到B2C销售订单明细：平台单号【{}】", platformCode);
        }
        //产品信息
        List<String> skuNos = aliexpressDeliveryDetailEntityList.stream().map(AliexpressDeliveryDetailEntity::getSkuNo).distinct().collect(Collectors.toList());
        skuNos.addAll(soB2cDetailEntityList.stream().map(SoB2cDetailEntity::getSkuNo).distinct().collect(Collectors.toList()));
        // 重新分摊价格
        return convertAllAliExpressDeliveryDetailPrice(aliexpressDeliveryDetailEntityList, soB2cDetailEntityList, sourceAllDeliveryList, mainEntity);
    }


    public boolean removeByMainId(String mainId){
        return this.lambdaUpdate().eq(AliexpressDeliveryDetailEntity::getMainId, mainId).remove();
    }

    /**
     * 计算自发发货单明细单价
     *
     * @param deliveryDetailList    速卖通发货单明细
     * @param soB2cDetailEntityList 速卖通销售订单明细
     * @param sourceAllDeliveryList
     * @param mainEntity
     * @return Map<速卖通明细ID, Pair < 发货明细sku单价, 发货明细sku总价>
     */
    public List<AliexpressDeliveryDetailEntity> convertAllAliExpressDeliveryDetailPrice(List<AliexpressDeliveryDetailEntity> deliveryDetailList,
                                                                                        List<SoB2cDetailEntity> soB2cDetailEntityList,
                                                                                        List<PlatformDeliveryDTO> sourceAllDeliveryList,
                                                                                        AliexpressDeliveryEntity mainEntity) {
        List<AliexpressDeliveryDetailEntity> deliveryDetailResultList = new LinkedList<>();

        // 所有发货明细
        List<PlatformDeliveryDetailDTO> allSourceDeliveryDetailList = sourceAllDeliveryList.stream().map(PlatformDeliveryDTO::getDetailDTOList).flatMap(List::stream).collect(Collectors.toList());


        // 平台产品ID 分组
        Map<String, List<AliexpressDeliveryDetailEntity>> deliveryMap = deliveryDetailList
                .stream()
                .collect(Collectors.groupingBy(AliexpressDeliveryDetailEntity::getPlatformSkuId));

        for (Map.Entry<String, List<AliexpressDeliveryDetailEntity>> entry : deliveryMap.entrySet()) {
            // 未拆分
            if (1 == entry.getValue().size()){
                String platformSkuId = entry.getValue().get(0).getPlatformSkuId();
                // 所有发货明细只有一个平台skuID
                if (1 == allSourceDeliveryDetailList.stream().filter(e-> e.getPlatformSkuId().equals(platformSkuId)).count()){
                    for (AliexpressDeliveryDetailEntity aliExpressDetailEntity : entry.getValue()) {
                        SoB2cDetailEntity detailEntity = checkAndGetSoB2cDetailEntity(soB2cDetailEntityList, aliExpressDetailEntity);
                        // 根据发货数量和订单明细数量判断单价和发货明细总价
                        // Map<速卖通明细ID, Pair<发货明细单价, 发货明细总价>>
                        Pair<BigDecimal, BigDecimal> pircePair = checkQtyGetPrice(aliExpressDetailEntity, detailEntity);
                        aliExpressDetailEntity.setPlatformDetailId(detailEntity.getSourceDetailId());
                        aliExpressDetailEntity.setProratedUnitPrice(pircePair.getFirst());
                        aliExpressDetailEntity.setProratedAmount(pircePair.getSecond());
                        deliveryDetailResultList.add(aliExpressDetailEntity);
                    }
                    continue;
                }
            }
            // 平台库存产品ID一样 = 未拆分
            // TODO
            if (1 == allSourceDeliveryDetailList.stream().map(PlatformDeliveryDetailDTO::getScItemId).distinct().count()){
                for (AliexpressDeliveryDetailEntity aliExpressDetailEntity : entry.getValue()) {
                    SoB2cDetailEntity detailEntity = checkAndGetSoB2cDetailEntity(soB2cDetailEntityList, aliExpressDetailEntity);
                    // 根据发货数量和订单明细数量判断单价和发货明细总价
                    aliExpressDetailEntity.setPlatformDetailId(detailEntity.getSourceDetailId());
                    aliExpressDetailEntity.setProratedUnitPrice(aliExpressDetailEntity.getPrice());
                    aliExpressDetailEntity.setProratedAmount(detailEntity.getAmount());
                    deliveryDetailResultList.add(aliExpressDetailEntity);
                }
                continue;
            }

            // 子件拆分
            for (AliexpressDeliveryDetailEntity deliveryDetailEntity : entry.getValue()) {
                // 根据产品ID匹配, 目前速卖通明细产品ID唯一
                SoB2cDetailEntity detailEntity = checkAndGetSoB2cDetailEntity(soB2cDetailEntityList, deliveryDetailEntity);
                deliveryDetailEntity.setPlatformDetailId(detailEntity.getSourceDetailId());
                // 当前明细分摊的金额 = 订单总金额 * 买家视角订单总金额 / 发货明细实际金额
                BigDecimal prorateAmount = mainEntity.getOrderAmount().multiply(mainEntity.getActualAmount()).divide(deliveryDetailEntity.getPayAmount(), 4, RoundingMode.DOWN);
                // 当前明细分摊的单价 = 当前明细分摊的金额 / 发货明细数量
                BigDecimal proratedUnitPrice = prorateAmount.divide(BigDecimal.valueOf(deliveryDetailEntity.getOrderLineQty()), 4, RoundingMode.DOWN);

                deliveryDetailEntity.setProratedUnitPrice(proratedUnitPrice);
                deliveryDetailEntity.setProratedAmount(prorateAmount);
                deliveryDetailResultList.add(deliveryDetailEntity);
            }
        }

        return deliveryDetailResultList;
    }

    /**
     * 检查匹配订单明细
     */
    private static SoB2cDetailEntity checkAndGetSoB2cDetailEntity(List<SoB2cDetailEntity> soB2cDetailEntityList, AliexpressDeliveryDetailEntity deliveryDetailEntity) {
        // 平台skuId 优先
        SoB2cDetailEntity detailEntity = soB2cDetailEntityList.stream()
                .filter(e -> e.getPlatformSkuId().equalsIgnoreCase(deliveryDetailEntity.getPlatformSkuId()))
                .findFirst()
                .orElse(null);
        if (null == detailEntity){
            // 平台产品ID匹配
            String platformSpuNo = deliveryDetailEntity.getPlatformSpuNo();
            detailEntity= soB2cDetailEntityList.stream()
                    .filter(e -> e.getPlatformSpuNo().equalsIgnoreCase(platformSpuNo))
                    .findFirst()
                    .orElse(null);
        }
        if (null == detailEntity){
           throw  new ServiceException("未找对应明细:产品sku ID={}", deliveryDetailEntity.getId());
        }
        return detailEntity;
    }


    /**
     * 根据发货数量和订单明细数量判断单价
     * @param aliExpressDetailEntity 速卖通发货单明细
     * @param detailEntity 订单明细
     * @return 计算后发货sku单价
     */
    private Pair<BigDecimal, BigDecimal> checkQtyGetPrice(AliexpressDeliveryDetailEntity aliExpressDetailEntity, SoB2cDetailEntity detailEntity) {
        if (aliExpressDetailEntity.getOrderLineQty() > detailEntity.getQty()){
            // 速卖通发货单数量大于订单明细数量
            // 速卖通发货单价格 * 订单明细数量 / 速卖通发货单发货数量
            BigDecimal price = aliExpressDetailEntity.getPrice()
                    .multiply(BigDecimal.valueOf(detailEntity.getQty()))
                    .divide(BigDecimal.valueOf(aliExpressDetailEntity.getOrderLineQty()), 4, RoundingMode.DOWN);
            return new Pair<>(price, detailEntity.getAmount());
        } else {
            //  速卖通发货单数量小于等于订单明细数量
            return new Pair<>(aliExpressDetailEntity.getPrice(), detailEntity.getAmount());
        }
    }
}
