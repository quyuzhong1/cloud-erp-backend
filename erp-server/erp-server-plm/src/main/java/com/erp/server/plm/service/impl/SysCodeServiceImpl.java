package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.modules.sys.dto.SysCodeDTO;
import com.erp.model.plm.entity.BasicCategoryEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.enums.SysNoEnum;
import com.erp.server.plm.enums.VariantColorEnum;
import com.erp.server.plm.service.BasicCategoryService;
import com.erp.server.plm.service.ProductInfoService;
import com.erp.server.plm.service.SysCodeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        SysCodeDTO dto = new SysCodeDTO();
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
        dto.setType(SysNoEnum.SKU_NO.getCode());
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
        String sysNo = sysUserFeign.getSysCode(dto);
        return sysNo;
    }
}
