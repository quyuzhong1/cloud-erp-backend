package com.erp.server.wms.pull.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.BaseDropDownDTO;
import com.erp.model.plm.entity.ProductInfoEntity;

import java.util.List;

public interface ProductInfoService extends IService<ProductInfoEntity> {
/*
    */
/**
     * 更新PLM同步过来的数据
     * @Author zhangchunlin
     * @Date 2023-05-11 19:18
     **//*

    void saveOrUpdateProductInfo(List<ProductInfoEntity> productInfoEntities);
*/

    /**
     * 根据主键Id查询产品表信息
     * @Author Luo_WG
     * @Date 2023/4/19 16:25
     * @param ids
     * @return com.erp.model.plm.entity.ProductInfoEntity
     **/
    List<ProductInfoEntity> ListProductInfoByIds(List<String> ids);

    /**
     * 获取所有非空的spu no
     * @return
     */
    List<BaseDropDownDTO.CommonDTO> getNotEmptySpuNos();
}
