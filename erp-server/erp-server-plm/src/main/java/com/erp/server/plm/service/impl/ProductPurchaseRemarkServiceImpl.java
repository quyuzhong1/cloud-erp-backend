package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.common.business.interceptor.CommonInterceptor;
import com.erp.common.vo.LoginUser;
import com.erp.model.plm.dto.ProductPurchaseRemarkDTO;
import com.erp.model.plm.entity.ProductPurchaseRemarkEntity;
import com.erp.server.plm.mapper.ProductPurchaseRemarkMapper;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.ProductPurchaseRemarkService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 *
 */
@Service
public class ProductPurchaseRemarkServiceImpl extends ServiceImpl<ProductPurchaseRemarkMapper, ProductPurchaseRemarkEntity>
    implements ProductPurchaseRemarkService {

    @Resource
    private CommonService commonService;

    /**
     * @Description 产品采购备注信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/23 14:06
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductPurchaseRemarkShowDTO>
     **/
    @Override
    public List<ProductPurchaseRemarkEntity> list(String productId) {
        LambdaQueryWrapper<ProductPurchaseRemarkEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductPurchaseRemarkEntity::getProductId, productId);
        return this.list(queryWrapper);
    }

    /**
     * @Description 保存/修改产品采购备注信息
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param dto 产品采购备注信息表请求参数
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdate(ProductPurchaseRemarkDTO dto) {
        ProductPurchaseRemarkEntity remarkEntity = new ProductPurchaseRemarkEntity();
        BeanMapper.copy(dto, remarkEntity);

        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        if (ObjectUtils.isNotEmpty(loginUser)) {
            if (StringUtils.isBlank(dto.getId())) {
                remarkEntity.setCreateUserId(loginUser.getUid());
                remarkEntity.setCreateUserName(loginUser.getUserName());
            } else {
                remarkEntity.setUpdateUserId(loginUser.getUid());
                remarkEntity.setUpdateUserName(loginUser.getUserName());
            }
        }
        return this.saveOrUpdate(remarkEntity);
    }

    /**
     * @Description 保存/修改产品采购备注信息-批量
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param dto 产品采购备注信息表请求参数
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdateBatch(List<ProductPurchaseRemarkDTO> dto) {
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        List<ProductPurchaseRemarkEntity> productPurchaseRemarkEntities = BeanMapper.copyList(dto, ProductPurchaseRemarkEntity.class);
        if (ObjectUtils.isNotEmpty(loginUser)) {
            for (ProductPurchaseRemarkEntity productPurchaseRemarkEntity : productPurchaseRemarkEntities) {
                if (StringUtils.isBlank(productPurchaseRemarkEntity.getId())) {
                    productPurchaseRemarkEntity.setCreateUserId(loginUser.getUid());
                    productPurchaseRemarkEntity.setCreateUserName(loginUser.getUserName());
                } else {
                    productPurchaseRemarkEntity.setUpdateUserId(loginUser.getUid());
                    productPurchaseRemarkEntity.setUpdateUserName(loginUser.getUserName());
                }
            }
        }
        return this.saveOrUpdateBatch(productPurchaseRemarkEntities);
    }
}




