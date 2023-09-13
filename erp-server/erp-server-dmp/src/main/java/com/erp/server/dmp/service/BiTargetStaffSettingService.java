package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.BiTargetStaffSettingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.BiTargetStaffSettingDTO;

/**
 * <p>
 * 人员目标设置表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
public interface BiTargetStaffSettingService extends SuperService<BiTargetStaffSettingEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    String add(BiTargetStaffSettingDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    Boolean update(BiTargetStaffSettingDTO.UpdateDTO dto);


}
