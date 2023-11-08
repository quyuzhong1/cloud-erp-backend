package com.erp.server.plm.service;
import com.erp.model.plm.entity.ProductPropertiesEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.ProductPropertiesDTO;

/**
 * <p>
 * sku与配置字段关系表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-08
 */
public interface ProductPropertiesService extends SuperService<ProductPropertiesEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-11-08
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ProductPropertiesDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-11-08
    * @param dto
    * @return
    */
    Boolean update(ProductPropertiesDTO.UpdateDTO dto);


}
