package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.config.DocNoGenHelper;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.entity.BasicCategoryEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysCodeSkuDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.service.BasicCategoryService;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.ProductInfoService;
import com.erp.server.plm.service.SysCodeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Will
 * @version 1.0

 * @date 2022/11/23 19:14
 */
@Service
public class SysCodeServiceImpl implements SysCodeService {

    @Autowired
    private ProductInfoService productInfoService;

    @Autowired
    private BasicCategoryService basicCategoryService;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private ProductDetailService productDetailService;
    @Resource
    private DocNoGenHelper docNoGenHelper;

    /**
     * @description: 根据产品id和颜色生产sku编号
     * @author Will
     * @date: 2022/11/23 10:01
     * @param productId
     * @param variantColorProperty
     * @return String
     */
    @Override
    @Transactional
    public String getSkuNo(String productId,String variantColorProperty){
        //产品信息
        ProductInfoEntity entity = productInfoService.getById(productId);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_95010);
        }
        SysCodeSkuDTO dto = new SysCodeSkuDTO();
        //查询产品分类代码
        BasicCategoryEntity bestEntity = new BasicCategoryEntity();
        basicCategoryService.getBestEntity(entity.getCategoryId(),bestEntity);

        if (ObjectUtils.isEmpty(bestEntity) || StringUtils.isBlank(bestEntity.getCode())) {
            throw new ServiceException(ApiError.ERROR_95069);
        }
        //产品类目
        dto.setCategory(bestEntity.getCode());
        dto.setType(BusinessNoTypeEnum.SKU_NO.getCode());
        String sysNo = sysUserFeign.getSkuNo(dto);
        //已存在则获取下一个
        String existSKuNo = isExistSKuNo(sysNo, dto);
        return existSKuNo;
    }


    @Override
    @Transactional
    public String getSpuNo(String categoryId){
        //spuNo生成规则：一级品类代码+二级品类代码+顺序码（两位数以内自动填充0到两位，例如：01）
        //父级品类
        List<BasicCategoryEntity> categoryList = basicCategoryService.listParentEntity(categoryId);
        if (CollectionUtils.isEmpty(categoryList)) {
            throw new ServiceException(ApiError.ERROR_95091);
        }
        //一级品类
        BasicCategoryEntity bestEntity = categoryList.stream().filter(obj -> "0".equals(obj.getPid())).findFirst().orElse(null);
        if (ObjectUtils.isEmpty(bestEntity) || StringUtils.isBlank(bestEntity.getCode())) {
            throw new ServiceException(ApiError.ERROR_95091);
        }
        //二级品类
        BasicCategoryEntity secondEntity = categoryList.stream().filter(obj -> bestEntity.getId().equals(obj.getPid())).findFirst().orElse(null);
        if (ObjectUtils.isEmpty(secondEntity) || StringUtils.isBlank(secondEntity.getCode())) {
            throw new ServiceException(ApiError.ERROR_95092);
        }
        SysCodeDTO dto = new SysCodeDTO();
        //分类组合
        String category = bestEntity.getCode() + secondEntity.getCode();
        dto.setCategory(category);
        dto.setType(BusinessNoTypeEnum.SPU_NO.getCode());
        String sysNo = sysUserFeign.getSpuNo(dto);
        isExistSpuNo(sysNo,dto);
        return sysNo;
    }



    @Override
    public String getBusinessNo(String businessHead, BusinessNoTypeEnum businessNoTypeEnum) {
        SysCodeDTO dto = new SysCodeDTO();
        dto.setCategory(businessHead);
        dto.setType(businessNoTypeEnum.getCode());
//        String sysNo = sysUserFeign.getBusinessNo(dto);
        String sysNo = docNoGenHelper.generateCode(businessNoTypeEnum);
        return sysNo;
    }


    /**
     * 判断spu编号是否存在
     */
    private void isExistSpuNo(String sysNo,SysCodeDTO dto) {
        //判断spu编码系统中是否已经存在，存在则获取下一个编码
        ProductInfoEntity productInfoEntity = productInfoService.getBySpuNo(sysNo);
        if (ObjectUtils.isNotEmpty(productInfoEntity)) {
             sysNo = sysUserFeign.getSpuNo(dto);
             //再次判断是否存在
             isExistSpuNo(sysNo,dto);
        } else {
            return;
        }
    }

    /**
     * 判断sku编号是否存在
     */
    private String isExistSKuNo(String sysNo,SysCodeSkuDTO dto) {
        //判断spu编码系统中是否已经存在，存在则获取下一个编码
        ProductDetailEntity productDetailEntity = productDetailService.getProductIdBySku(sysNo);
        if (ObjectUtils.isNotEmpty(productDetailEntity)) {
            sysNo = sysUserFeign.getSkuNo(dto);
            //再次判断是否存在
           return isExistSKuNo(sysNo,dto);
        } else {
            return sysNo;
        }
    }
}
