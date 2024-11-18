package com.erp.server.plm.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.dto.ProductRefLabelDTO;
import com.erp.model.plm.entity.ProductRefLabelEntity;
import com.erp.model.plm.vo.ProductRefLabelVO;
import com.erp.server.plm.mapper.ProductRefLabelMapper;
import com.erp.server.plm.service.ProductRefLabelService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * <p>
 * 产品便签关系表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@Service
public class ProductRefLabelServiceImpl extends SuperServiceImpl<ProductRefLabelMapper, ProductRefLabelEntity> implements ProductRefLabelService {

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchAdd(ProductRefLabelDTO.BatchAddDTO batchAddDTO) {
        if (CollectionUtils.isEmpty(batchAddDTO.getLabelIds())) throw new ServiceException(ApiError.DEFAULT);
        if (CollectionUtils.isEmpty(batchAddDTO.getProjectDTOs())) throw new ServiceException(ApiError.DEFAULT);
        List<ProductRefLabelEntity> productRefLabelEntities = new ArrayList<>();
        // 数据处理
        handleData(batchAddDTO, productRefLabelEntities);
        log.info("开始新增产品便签关系单");
        if (CollectionUtils.isNotEmpty(productRefLabelEntities)) {
            boolean save = super.saveBatch(productRefLabelEntities);
            if (!save) throw new ServiceException(ApiError.ERROR_SAVE_PRODUCT_BASIC_LABEL);
        }
        log.info("结束新增产品便签关系单");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeProductRef(ProductRefLabelDTO.RemoveDTO dto) {
        if (CollectionUtils.isNotEmpty(dto.getIds())) this.removeByIds(dto.getIds());
    }

    @Override
    public List<ProductRefLabelVO> getLabelList(String productId, String labelId, String skuId) {
        String userId = null;
        LoginUser user = UserContext.getDefaultLoginUser();
        if(Objects.nonNull(user)){
            userId = user.getUid();
        }
        return baseMapper.getLabelList(productId, labelId, skuId,userId);
    }

    @Override
    public List<ProductRefLabelVO> getLabelListByIds(Set<String> productIds, Set<String> labelIds, Set<String> skuIds) {
        String userId = null;
        LoginUser user = UserContext.getDefaultLoginUser();
        if(Objects.nonNull(user)){
            userId = user.getUid();
        }
        return baseMapper.getLabelListByIds(productIds, labelIds, skuIds, userId);
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(ProductRefLabelDTO.BatchAddDTO batchAddDTO, List<ProductRefLabelEntity> productRefLabelEntities) {
        //检查数据是否已存在,存在则过滤，不存在则新增
        batchAddDTO.getLabelIds().forEach(labelId -> {
            batchAddDTO.getProjectDTOs().forEach(projectDTO -> {
                if (!isExistRef(labelId, projectDTO.getSkuId(), projectDTO.getProductId())) {
                    productRefLabelEntities.add(new ProductRefLabelEntity().setProductId(projectDTO.getProductId()).setLabelId(labelId).setSkuId(projectDTO.getSkuId()));
                }
            });
        });
    }

    /**
     * 是否存在记录 ture 是, false 否
     *
     * @param labelId
     * @param skuId
     * @param productId
     * @return
     */
    private boolean isExistRef(String labelId, String skuId, String productId) {
        LambdaQueryWrapper<ProductRefLabelEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductRefLabelEntity::getProductId, productId);
        queryWrapper.eq(ProductRefLabelEntity::getSkuId, skuId);
        queryWrapper.eq(ProductRefLabelEntity::getLabelId, labelId);
        int count = this.count(queryWrapper);
        return count != 0;
    }
}
