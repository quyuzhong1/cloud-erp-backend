package com.erp.server.oms.service;
import com.erp.model.oms.entity.KolSubB2cApplicationDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.KolSubB2cApplicationDetailDTO;

/**
 * <p>
 * B2C寄样申请单拆分单明细 服务类
 * </p>
 *
 * @author jack
 * @since 2025-12-04
 */
public interface KolSubB2cApplicationDetailService extends SuperService<KolSubB2cApplicationDetailEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-12-04
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(KolSubB2cApplicationDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-12-04
    * @param dto
    * @return
    */
    Boolean update(KolSubB2cApplicationDetailDTO.UpdateDTO dto);


}
