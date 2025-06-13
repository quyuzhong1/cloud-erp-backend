package com.erp.server.scm.service;
import com.erp.model.scm.entity.SalesSharingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.scm.dto.SalesSharingDTO;

/**
 * <p>
 * 销量共享表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-06-13
 */
public interface SalesSharingService extends SuperService<SalesSharingEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-06-13
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SalesSharingDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-06-13
    * @param dto
    * @return
    */
    Boolean update(SalesSharingDTO.UpdateDTO dto);


}
