package com.erp.server.oms.service;
import com.erp.model.oms.entity.KolSubB2cApplicationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.KolSubB2cApplicationDTO;

/**
 * <p>
 * B2C寄样申请单拆分单 服务类
 * </p>
 *
 * @author jack
 * @since 2025-12-04
 */
public interface KolSubB2cApplicationService extends SuperService<KolSubB2cApplicationEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-12-04
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(KolSubB2cApplicationDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-12-04
    * @param dto
    * @return
    */
    Boolean update(KolSubB2cApplicationDTO.UpdateDTO dto);


}
