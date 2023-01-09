package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.vo.SkuCategoryVO;
import com.erp.model.dmp.entity.DmpSkuInfoEntity;

import java.util.List;

/**
 * @Classname BiSkuInfoService
 * @Description TODO
 * @Date 2022-12-26 16:34
 * @Created by yl
 */
public interface BiSkuInfoService  extends IService<DmpSkuInfoEntity> {

    List<SkuCategoryVO> getSkuCategoryList();

    List<SkuCategoryVO> getSkuBrandList();

    List<SkuCategoryVO> getSkuPropertyList();
}
