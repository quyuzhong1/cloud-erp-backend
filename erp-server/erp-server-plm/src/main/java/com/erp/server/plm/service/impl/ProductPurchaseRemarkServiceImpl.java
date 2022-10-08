package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.common.vo.LoginUser;
import com.erp.model.plm.dto.ProductPurchaseRemarkDTO;
import com.erp.model.plm.dto.ProductPurchaseRemarkShowDTO;
import com.erp.model.plm.entity.ProductPackEntity;
import com.erp.model.plm.entity.ProductPurchaseRemarkEntity;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.ProductPurchaseRemarkMapper;
import com.erp.server.plm.service.ProductPurchaseRemarkService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 */
@Service
public class ProductPurchaseRemarkServiceImpl extends ServiceImpl<ProductPurchaseRemarkMapper, ProductPurchaseRemarkEntity>
    implements ProductPurchaseRemarkService {

    /**
     * @Description 产品采购备注信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/23 14:06
     * @param purchaseId:产品采购信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductPurchaseRemarkShowDTO>
     **/
    @Override
    public List<ProductPurchaseRemarkEntity> list(String purchaseId) {
        LambdaQueryWrapper<ProductPurchaseRemarkEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductPurchaseRemarkEntity::getPurchaseId, purchaseId);
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
        ProductPurchaseRemarkEntity packEntity = new ProductPurchaseRemarkEntity();
        BeanMapper.copy(dto, packEntity);
        return this.saveOrUpdate(packEntity);
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
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
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




