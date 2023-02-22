package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.business.interceptor.CommonInterceptor;
import com.common.business.vo.LoginUser;
import com.erp.model.plm.dto.ProductCertificateDTO;
import com.erp.model.plm.dto.ProductCertificateShowDTO;
import com.erp.model.plm.entity.ProductCertificateEntity;

import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.SysLogEntity;
import com.erp.server.plm.mapper.ProductCertificateMapper;
import com.erp.server.plm.service.ProductCertificateService;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.SysLogService;
import org.apache.commons.lang3.StringUtils;
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

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private SysLogService sysLogService;

    private static final  String SKUCLASSPATH = String.valueOf(ProductDetailEntity.class);

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
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        if (ObjectUtils.isNotEmpty(loginUser)) {
            if (StringUtils.isBlank(productCertificateDTO.getId())) {
                certificateEntity.setCreateUserId(loginUser.getUid());
                certificateEntity.setCreateUserName(loginUser.getUserName());
            } else {
                certificateEntity.setUpdateUserId(loginUser.getUid());
                certificateEntity.setUpdateUserName(loginUser.getUserName());
            }
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
     * @Description 根据skuid删除产品证书信息
     * @Author Luo_WG
     * @Date 2022/9/26 18:42
     * @param skuId skuId
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean removeCertificate(String skuId) {
        LambdaQueryWrapper<ProductCertificateEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductCertificateEntity::getSkuId, skuId);
        Integer count = baseMapper.delete(queryWrapper);
        if (count > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean removeCertificateById(String id) {
        ProductCertificateEntity productCertificateEntity = this.getById(id);
        if (ObjectUtils.isNotEmpty(productCertificateEntity)) {
            ProductDetailEntity productDetailEntity = productDetailService.getById(productCertificateEntity.getSkuId());
            if (ObjectUtils.isNotEmpty(productDetailEntity)) {
                //操作日志
                SysLogEntity sysLogEntity =  new SysLogEntity().setClassPath(SKUCLASSPATH).setBusinessId(productDetailEntity.getId()).setPid(productDetailEntity.getProductId()).setOperation("删除证书信息").setContent("SKU["+productDetailEntity.getSkuNo()+"]删除了证书信息：["+productCertificateEntity.getCertificateImg()+"]");
                sysLogService.addSysLogByOther(sysLogEntity);
            }
        }
        return this.removeById(id);
    }
}




