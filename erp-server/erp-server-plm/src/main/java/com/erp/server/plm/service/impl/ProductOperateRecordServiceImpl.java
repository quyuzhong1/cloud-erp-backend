package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.common.vo.LoginUser;
import com.erp.model.plm.dto.ProductOperateRecordDTO;
import com.erp.model.plm.dto.ProductPurchaseRemarkDTO;
import com.erp.model.plm.entity.ProductOperateRecordEntity;
import com.erp.model.plm.entity.ProductPurchaseRemarkEntity;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.ProductOperateRecordMapper;
import com.erp.server.plm.service.ProductOperateRecordService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 产品操作日志
 */
@Service
public class ProductOperateRecordServiceImpl extends ServiceImpl<ProductOperateRecordMapper, ProductOperateRecordEntity>
    implements ProductOperateRecordService {

    /**
     * @Description 产品采购备注信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/23 14:06
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductPurchaseRemarkShowDTO>
     **/
    @Override
    public List<ProductOperateRecordEntity> list(String productId) {
        LambdaQueryWrapper<ProductOperateRecordEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductOperateRecordEntity::getProductId, productId);
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
    public Boolean saveOrUpdate(ProductOperateRecordDTO dto) {
        ProductOperateRecordEntity recordEntity = new ProductOperateRecordEntity();
        BeanMapper.copy(dto, recordEntity);
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        if (ObjectUtils.isNotEmpty(loginUser)) {
            if (StringUtils.isBlank(dto.getId())) {
                recordEntity.setCreateUserId(loginUser.getUid());
                recordEntity.setCreateUserName(loginUser.getUserName());
            } else {
                recordEntity.setUpdateUserId(loginUser.getUid());
                recordEntity.setUpdateUserName(loginUser.getUserName());
            }
        }
        return this.saveOrUpdate(recordEntity);
    }

    /**
     * @Description 保存/修改产品采购备注信息-批量
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param dto 产品采购备注信息表请求参数
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdateBatch(List<ProductOperateRecordDTO> dto) {
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        List<ProductOperateRecordEntity> productOperateRecordEntities = BeanMapper.copyList(dto, ProductOperateRecordEntity.class);
        if (ObjectUtils.isNotEmpty(loginUser)) {
            for (ProductOperateRecordEntity productOperateRecordEntity : productOperateRecordEntities) {
                if (StringUtils.isBlank(productOperateRecordEntity.getId())) {
                    productOperateRecordEntity.setCreateUserId(loginUser.getUid());
                    productOperateRecordEntity.setCreateUserName(loginUser.getUserName());
                } else {
                    productOperateRecordEntity.setUpdateUserId(loginUser.getUid());
                    productOperateRecordEntity.setUpdateUserName(loginUser.getUserName());
                }
            }
        }
        return this.saveOrUpdateBatch(productOperateRecordEntities);
    }

}




