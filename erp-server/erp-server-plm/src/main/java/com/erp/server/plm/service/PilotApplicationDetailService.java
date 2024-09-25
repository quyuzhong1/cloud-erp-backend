package com.erp.server.plm.service;
import com.erp.model.plm.entity.PilotApplicationDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.PilotApplicationDetailDTO;

/**
 * <p>
 * 试产/量产 明细 服务类
 * </p>
 *
 * @author tmj
 * @since 2024-08-27
 */
public interface PilotApplicationDetailService extends SuperService<PilotApplicationDetailEntity> {

    /**
    * 新增
    * @author tmj
    * @date: 2024-08-27
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PilotApplicationDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author tmj
    * @date: 2024-08-27
    * @param dto
    * @return
    */
    Boolean update(PilotApplicationDetailDTO.UpdateDTO dto);


}
