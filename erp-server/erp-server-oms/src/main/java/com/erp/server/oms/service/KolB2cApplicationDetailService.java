package com.erp.server.oms.service;
import com.erp.model.oms.entity.KolB2cApplicationDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.KolB2cApplicationDetailDTO;

/**
 * <p>
 * B2C寄样申请单明细 服务类
 * </p>
 *
 * @author jack
 * @since 2025-12-04
 */
public interface KolB2cApplicationDetailService extends SuperService<KolB2cApplicationDetailEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-12-04
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(KolB2cApplicationDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-12-04
    * @param dto
    * @return
    */
    Boolean update(KolB2cApplicationDetailDTO.UpdateDTO dto);


}
