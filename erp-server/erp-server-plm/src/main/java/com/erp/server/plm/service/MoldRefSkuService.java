package com.erp.server.plm.service;
import com.erp.model.plm.entity.MoldRefSkuEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.MoldRefSkuDTO;

/**
 * <p>
 * 模具关联sku 服务类
 * </p>
 *
 * @author jack
 * @since 2025-10-10
 */
public interface MoldRefSkuService extends SuperService<MoldRefSkuEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-10-10
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(MoldRefSkuDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-10-10
    * @param dto
    * @return
    */
    Boolean update(MoldRefSkuDTO.UpdateDTO dto);


}
