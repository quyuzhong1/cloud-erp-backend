package com.erp.server.tms.service;
import com.erp.model.tms.entity.TransferDeclareDeadlineSettingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TransferDeclareDeadlineSettingDTO;

/**
 * <p>
 * 截单设置 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-24
 */
public interface TransferDeclareDeadlineSettingService extends SuperService<TransferDeclareDeadlineSettingEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2024-01-24
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TransferDeclareDeadlineSettingDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2024-01-24
    * @param dto
    * @return
    */
    Boolean update(TransferDeclareDeadlineSettingDTO.UpdateDTO dto);


}
