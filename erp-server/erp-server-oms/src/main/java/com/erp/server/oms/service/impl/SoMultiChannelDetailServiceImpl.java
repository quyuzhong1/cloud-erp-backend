package com.erp.server.oms.service.impl;


import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoMultiChannelDetailEntity;
import com.erp.model.oms.entity.SoMultiChannelEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.plm.vo.SkuInfoSimpleVO;
import com.erp.model.wms.dto.WarehouseMappingDTO;
import com.erp.rpc.wms.feign.WarehouseMappingFeign;
import com.erp.server.oms.convert.SoMultiChannelConsumerConverter;
import com.erp.server.oms.mapper.SoMultiChannelDetailMapper;
import com.erp.server.oms.service.SkuMappingService;
import com.erp.server.oms.service.SoMultiChannelDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 多渠道订单明细表 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2024-05-30
 */
@Slf4j
@Service
public class SoMultiChannelDetailServiceImpl extends SuperServiceImpl<SoMultiChannelDetailMapper, SoMultiChannelDetailEntity> implements SoMultiChannelDetailService {

    @Resource
    private SkuMappingService skuMappingService;
    @Resource
    private WarehouseMappingFeign warehouseMappingFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SoMultiChannelDetailEntity> saveOrUpdateEntity(PlatformOrderDTO dto, SoMultiChannelEntity mainEntity, Map<String, List<ListingInfoWithSkuMappingDTO>> listingInfoWithSkuMappingDTOMap, ShopInfoEntity shopInfo, List<SkuInfoSimpleVO> skuList) {
        // 订单明细
        List<SoMultiChannelDetailEntity> oldDetailEntityList = this.listByMainId(mainEntity.getId());
        // 来源为空
        if (CollectionUtils.isEmpty(dto.getDetails())) {
            if (CollectionUtils.isEmpty(oldDetailEntityList)) {
                // 新建空
                SoMultiChannelDetailEntity detailEntity = SoMultiChannelConsumerConverter.INSTANCE.convertNewDetail(null, mainEntity.getId(), "", "", "", "");
                if (!this.save(detailEntity)) {
                    throw new ServiceException("[SoMultiChannelDetailEntity] 保存失败");
                }
                return Collections.singletonList(detailEntity);
            }
            return oldDetailEntityList;
        }

        // 来源不为空
        // 历史map
        Map<String, SoMultiChannelDetailEntity> oldDetailMap = oldDetailEntityList.stream()
                .filter(e -> StringUtils.isNotEmpty(e.getSourceDetailId()))
                .collect(Collectors.toMap(SoMultiChannelDetailEntity::getSourceDetailId, Function.identity()));

        //查询速卖通仓库名称是否映射ERP仓库
        List<WarehouseMappingDTO.MappingViewDTO> mappingViewDTOS = new ArrayList<>();
        if (LogisticsPlatformEnum.ALI_EXPRESS.getCode().equals(mainEntity.getDictPlatform())) {
            mappingViewDTOS = warehouseMappingFeign.listMappingViewByDictPlatform(mainEntity.getDictPlatform());
        }
        //已发货
        String shipped = SoB2cBillStatusEnum.ENUM_SHIPPED.getCode();
        String billStatus = mainEntity.getBillStatus();
        boolean isShipped = shipped.equals(billStatus);
        // 新增或更新列表
        List<WarehouseMappingDTO.MappingViewDTO> finalMappingViewDTOS = mappingViewDTOS;
        List<SoMultiChannelDetailEntity> saveOrUpdateList = dto.getDetails().stream().map(detailDTO -> {
            // 历史记录
            SoMultiChannelDetailEntity oldEntity = oldDetailMap.get(detailDTO.getSourceDetailId());
            // 映射关系
            List<ListingInfoWithSkuMappingDTO> mappingDTOList = listingInfoWithSkuMappingDTOMap.get(detailDTO.getPlatformSkuNo());
            // 检查和获取映射关系
            ListingInfoWithSkuMappingDTO mappingDTO = skuMappingService.checkAndMappingDTO(mappingDTOList, detailDTO.getPlatformSpuNo(), mainEntity.getDictPlatform());

            String skuId = null == oldEntity ? "" : oldEntity.getSkuId();
            String skuNO = null == oldEntity ? "" : oldEntity.getSkuNo();
            String imageUrl = null == oldEntity ? "" : oldEntity.getImageUrl();
            String platformSpuNo = null == oldEntity ? detailDTO.getPlatformSpuNo() : oldEntity.getPlatformSpuNo();
            // 历史不为空不更新
            if (null != mappingDTO && StringUtils.isBlank(skuNO) && StringUtils.isBlank(skuId)) {
                skuId = mappingDTO.checkAndGetProductSkuId();
                skuNO = mappingDTO.checkAndGetProductSkuNo();
                imageUrl = mappingDTO.checkAndGetProductImageUrl();
            }
            if (null != mappingDTO && StringUtils.isBlank(platformSpuNo)) {
                platformSpuNo = mappingDTO.getPlatformSpuNo();
            }

            SoMultiChannelDetailEntity saveOrUpdateEntity;
            if (null != oldEntity) {
                // 更新指定内容
                saveOrUpdateEntity = SoMultiChannelConsumerConverter.INSTANCE.convertUpdateDetail(oldEntity, detailDTO, skuId, skuNO, imageUrl, platformSpuNo);
            } else {
                // 新记录
                saveOrUpdateEntity = SoMultiChannelConsumerConverter.INSTANCE.convertNewDetail(detailDTO, mainEntity.getId(), skuId, skuNO, imageUrl, platformSpuNo);
            }

            if (PlatformDictEnum.ALI_EXPRESS.getCode().equals(mainEntity.getDictPlatform()) && isShipped) {
                //查询映射的仓库信息
                WarehouseMappingDTO.MappingViewDTO mappingViewDTO = finalMappingViewDTOS.stream().filter(req -> detailDTO.getWarehouseName().equals(req.getThirdWarehouseName())).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(mappingViewDTO)) {
                    saveOrUpdateEntity.setWarehouseId(mappingViewDTO.getWarehouseId());
                    saveOrUpdateEntity.setWarehouseName(mappingViewDTO.getWarehouseName());
                    saveOrUpdateEntity.setWarehouseOrgId(mappingViewDTO.getWarehouseOrgId());
                    saveOrUpdateEntity.setWarehouseOrgName(mappingViewDTO.getWarehouseOrgName());
                } else {
                    saveOrUpdateEntity.setWarehouseId("");
                    saveOrUpdateEntity.setWarehouseName("");
                    saveOrUpdateEntity.setWarehouseOrgId("");
                    saveOrUpdateEntity.setWarehouseOrgName("");
                }
            }

            return saveOrUpdateEntity;
        }).collect(Collectors.toList());

        // 其他处理
        consumerHandleDetailList(saveOrUpdateList, mainEntity, skuList);

        // 批量保存和更新
        if (!this.saveOrUpdateBatch(saveOrUpdateList)) {
            throw new ServiceException(" [SoMultiChannelDetailEntity] 订单明细批量更新或保存失败");
        }
        return saveOrUpdateList;
    }

    @Override
    public List<SoMultiChannelDetailEntity> listByMainId(String mainId) {
        return lambdaQuery()
                .eq(SoMultiChannelDetailEntity::getIsDeleted, false)
                .eq(SoMultiChannelDetailEntity::getMainId, mainId)
                .list();
    }

    public void consumerHandleDetailList(List<SoMultiChannelDetailEntity> list, SoMultiChannelEntity mainEntity, List<SkuInfoSimpleVO> skuList) {
        for (SoMultiChannelDetailEntity detailEntity : list) {
            //产品信息
            SkuInfoSimpleVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
            detailEntity.setCurrency(mainEntity.getCurrency());
            detailEntity.setExchangeRate(mainEntity.getExchangeRate());

            //建议售价
            BigDecimal advicePrice = null == skuVO ? BigDecimal.ZERO : skuVO.getRetailPrice();
            detailEntity.setAdvicePrice(advicePrice);
            //含税单价
            BigDecimal costPrice = null == skuVO ? BigDecimal.ZERO : ObjectUtils.isEmpty(skuVO.getActualTaxCost()) ? skuVO.getTargetTaxCost() : skuVO.getActualTaxCost();
            detailEntity.setTaxCost(costPrice);
            detailEntity.setAmount(MathUtil.multiply(detailEntity.getPrice(), detailEntity.getQty()));
            // 产品图片
            detailEntity.setImageUrl(null == skuVO ? "" : skuVO.getSkuImagesUrl());
        }
    }

}
