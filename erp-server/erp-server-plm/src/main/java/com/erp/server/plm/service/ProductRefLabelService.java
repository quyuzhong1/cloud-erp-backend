package com.erp.server.plm.service;
import com.erp.model.plm.entity.ProductRefLabelEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.ProductRefLabelDTO;

/**
 * <p>
 * 产品便签关系表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
public interface ProductRefLabelService extends SuperService<ProductRefLabelEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    String add(ProductRefLabelDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    Boolean update(ProductRefLabelDTO.UpdateDTO dto);


}
