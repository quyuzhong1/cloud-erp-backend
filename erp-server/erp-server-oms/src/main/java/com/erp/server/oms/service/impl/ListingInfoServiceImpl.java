package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.erp.model.oms.dto.ListingInfoDTO;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.server.oms.mapper.ListingInfoMapper;
import com.erp.server.oms.service.ListingInfoService;
import com.common.business.service.impl.SuperServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * <p>
 * 对应平台sku 表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-18
 */
@Service
public class ListingInfoServiceImpl extends SuperServiceImpl<ListingInfoMapper, ListingInfoEntity> implements ListingInfoService {


    /**
     * 根据 sku 获取到listing 数据
     *
     * @param skuNo
     * @return com.erp.model.oms.entity.ListingInfoEntity
     * @author yl
     * @date 2023-08-18 16:35
     */
    @Override
    public ListingInfoEntity getBySkuNo(String skuNo, String type) {
        return lambdaQuery().eq(ListingInfoEntity::getSkuNo, skuNo).
                eq(ListingInfoEntity::getType, type).
                last("LIMIT 1").
                one();
    }


    /**
     * 添加库存sku
     *
     * @param skuNo
     * @param productName
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addWarehouseSku(String skuNo, String productName) {
        String id = IdWorker.getIdStr();
        ListingInfoEntity listingInfoEntity = new ListingInfoEntity();
        listingInfoEntity.setId(id);
        listingInfoEntity.setSkuNo(skuNo);
        listingInfoEntity.setProductName(productName);
        listingInfoEntity.setType(RuleTypeEnum.WAREHOUSE.getCode());
        if (this.save(listingInfoEntity)) {
            return id;
        } else {
            return "";
        }
    }


    /**
     * 根据平台sku ，平台 获取到对应的list
     * @author yl
     * @date 2023-08-21 11:57
     * @param platformSkuNo
     * @param platform
     * @return com.erp.model.oms.entity.ListingInfoEntity
     */
    @Override
    public ListingInfoEntity getByPlatformSkuNo(String platform, String platformSkuNo) {
        return lambdaQuery().eq(ListingInfoEntity::getPlatformSkuNo, platformSkuNo).
                eq(ListingInfoEntity::getPlatform, platform).
                last("LIMIT 1").
                one();
    }


    /**
     * 根据类型获取到对应数据
     * @author yl
     * @date 2023-08-24 14:54
     * @param type
     * @return java.util.List<com.erp.model.oms.dto.ListingInfoDTO.ListDTO>
     */
    @Override
    public List<ListingInfoDTO.ListDTO> listByType(String type) {
        return baseMapper.listByType(type);
    }

    @Override
    public List<ListingInfoEntity> findList(ListingInfoParamDTO dto) {
        return lambdaQuery()
                .eq(StringUtils.isNotBlank(dto.getPlatform()), ListingInfoEntity::getPlatform, dto.getPlatform())
                .eq(!CollectionUtils.isEmpty(dto.getPlatformSkuNoList()), ListingInfoEntity::getPlatformSkuNo, dto.getPlatformSkuNoList())
                .eq(null != dto.getMatchResult(), ListingInfoEntity::getMatchResult, dto.getMatchResult())
                .list();
    }
}
