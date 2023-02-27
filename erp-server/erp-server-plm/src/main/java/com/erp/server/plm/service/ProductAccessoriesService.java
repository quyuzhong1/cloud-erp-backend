package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.ProductAccessoriesDTO;
import com.erp.model.plm.entity.ProductAccessoriesEntity;

import java.util.List;

/**
 * 产品包装/辅料信息(ProductAccessories)表服务接口
 *
 * @author yl
 * @since 2023-02-25 12:58:11
 */
public interface ProductAccessoriesService  extends IService<ProductAccessoriesEntity> {


    /**
     * 批量保存或者修改包装辅料的信息
     * @param productAccessoriesList
     */
    Boolean saveOrUpdateBatchAccessories(List<ProductAccessoriesDTO> productAccessoriesList);


    /**
     * 根据产品id 获取辅料信息
     * @param productId
     * @return
     */
    List<ProductAccessoriesDTO> getByProductId(String productId);
}
