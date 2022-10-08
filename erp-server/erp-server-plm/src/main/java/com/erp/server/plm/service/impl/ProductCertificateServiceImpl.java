package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.common.vo.LoginUser;
import com.erp.model.plm.dto.ProductCertificateDTO;
import com.erp.model.plm.dto.ProductCertificateShowDTO;
import com.erp.model.plm.entity.ProductCertificateEntity;
import com.erp.model.plm.entity.ProductCostEntity;
import com.erp.model.plm.entity.ProductLogisticsEntity;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.ProductCertificateMapper;
import com.erp.server.plm.service.ProductCertificateService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 *
 */
@Service
public class ProductCertificateServiceImpl extends ServiceImpl<ProductCertificateMapper, ProductCertificateEntity>
    implements ProductCertificateService {

    @Resource
    private ProductCertificateMapper productCertificateMapper;

    /**
     * @Description 产品证书信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductCertificateShowDTO>
     **/
    @Override
    public List<ProductCertificateShowDTO> list(String productId) {
        return productCertificateMapper.list(productId);
    }

    /**
     * @Description 保存/修改产品证书信息
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productCertificateDTO 产品证书信息表
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdate(ProductCertificateDTO productCertificateDTO) {
        ProductCertificateEntity certificateEntity = new ProductCertificateEntity();
        BeanMapper.copy(productCertificateDTO, certificateEntity);
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        if (StringUtils.isBlank(productCertificateDTO.getId())) {
            certificateEntity.setCreateUserId(loginUser.getUid());
            certificateEntity.setCreateUserName(loginUser.getUserName());
        } else {
            certificateEntity.setUpdateUserId(loginUser.getUid());
            certificateEntity.setUpdateUserName(loginUser.getUserName());
        }
        return this.saveOrUpdate(certificateEntity);
    }


    /**
     * @Description 保存/修改产品证书信息-批量操作
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productCertificateList 产品证书信息表
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdateBatch(List<ProductCertificateDTO> productCertificateList) {
        List<ProductCertificateEntity> list = BeanMapper.copyList(productCertificateList, ProductCertificateEntity.class);
        return this.saveOrUpdateBatch(list);
    }

    /**
     * @Description 删除产品证书信息
     * @Author Luo_WG
     * @Date 2022/9/26 18:42
     * @param skuId 产品sku明细表id
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean remove(String skuId) {
        LambdaQueryWrapper<ProductCertificateEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductCertificateEntity:: getSkuId, skuId);
        return this.remove(queryWrapper);
    }
}




