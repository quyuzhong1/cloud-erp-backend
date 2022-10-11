package com.erp.server.plm.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.ProductOperateRecordDTO;
import com.erp.model.plm.dto.ProductPurchaseRemarkDTO;
import com.erp.model.plm.entity.ProductOperateRecordEntity;
import com.erp.model.plm.entity.ProductPurchaseRemarkEntity;

import java.util.List;

/**
 *
 */
public interface ProductOperateRecordService extends IService<ProductOperateRecordEntity> {
    /**
     * @Description 产品采购操作日志查询列表
     * @Author Luo_WG
     * @Date 2022/9/23 14:06
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.entity.ProductPurchaseRemarkEntity>
     **/
    List<ProductOperateRecordEntity> list(String productId);

    /**
     * @Description 保存/修改产品操作日志信息
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param dto 产品采购备注信息表请求参数
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdate(ProductOperateRecordDTO dto);

    /**
     * @Description 保存/修改产品操作日志信息-批量
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param dto 产品采购备注信息表请求参数
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdateBatch(List<ProductOperateRecordDTO> dto);
}
