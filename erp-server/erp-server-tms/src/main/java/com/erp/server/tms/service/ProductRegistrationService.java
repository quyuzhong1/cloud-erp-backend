package com.erp.server.tms.service;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.ProductRegistrationDTO;

import java.util.List;

/**
 * <p>
 * 产品备案表 服务类
 * </p>
 *
 * @author lambda
 * @since 2024-01-19
 */
public interface ProductRegistrationService extends SuperService<ProductRegistrationEntity> {

    /**
    * 新增
    * @author lambda
    * @date: 2024-01-19
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ProductRegistrationDTO.AddDTO dto);

    /**
    * 修改
    * @author lambda
    * @date: 2024-01-19
    * @param dto
    * @return
    */
    Boolean update(ProductRegistrationDTO.UpdateDTO dto);


    /**
     * 根据sku 查询
     * @param skuNoList
     * @return
     */
    List<ProductRegistrationEntity> listBySkuNoList(List<String> skuNoList);
}
