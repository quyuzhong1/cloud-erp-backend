package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.entity.BasicCategoryEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.enums.BusinessNoTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysCodeSkuDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.service.BasicCategoryService;
import com.erp.server.plm.service.ProductInfoService;
import com.erp.server.plm.service.SysCodeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
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
        //产品颜色
        dto.setColorCode(variantColorProperty);
        dto.setType(BusinessNoTypeEnum.SKU_NO.getCode());
        //产品销售渠道
        if (StringUtils.isBlank(entity.getSalesChannel())) {
            throw new ServiceException(ApiError.ERROR_95073);
        }
        dto.setSalesChannel(entity.getSalesChannel());
        //产品是否是客户定制
        if (entity.getIsCustomized().equals(IsConstant.YES)) {
            dto.setCustomized("DZ");
        } else {
            dto.setCustomized("");
        }
        //产品的版本 1-9，A-Z
        int version = entity.getVersion().intValue();
        if (9 >= version ) {
            dto.setVersion(entity.getVersion().toString());
        } else {
            //version为10以上时转换成大写英文字母
            //大写字母A到Z的ascii码是从65到90
            int j = version - 10;
            char c = 65;
            if ((65 + j) > 90) {
                //如果版本超出字母范围则恒定为Z
                c = (char)90;
            } else {
                c = (char) (65 + j);
            }
            if (String.valueOf(c).equals("I") || String.valueOf(c).equals("O")) {
                c = (char) (65 + j + 1); //当版本为I或者O时取下一个字母
            }
            dto.setVersion(String.valueOf(c));
        }

        String sysNo = sysUserFeign.getSkuNo(dto);
        return sysNo;
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
        String sysNo = sysUserFeign.getBusinessNo(dto);
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


}
