package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.ProductCostDTO;
import com.erp.model.plm.dto.ProductCostShowDTO;
import com.erp.model.plm.dto.ProductLogisticsDTO;
import com.erp.model.plm.dto.ProductLogisticsShowDTO;
import com.erp.model.plm.entity.ProductLogisticsEntity;

import java.util.List;

/**
 * @Description 产品物流信息服务类
 * @Author Luo_WG
 * @Date 2022/9/23 15:35
 **/
public interface ProductLogisticsService extends IService<ProductLogisticsEntity> {
    /**
     * @Description 产品物流信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductLogisticsShowDTO>
     **/
    List<ProductLogisticsShowDTO> list(String productId);

    /**
     * @Description 保存/修改产品物流信息
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productLogisticsDTO 产品物流信息表
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdate(ProductLogisticsDTO productLogisticsDTO);
}
