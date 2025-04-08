package com.erp.server.sys.service;
import com.erp.model.sys.entity.SysEventTrackingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.SysEventTrackingDTO;

/**
 * <p>
 * 前端埋点事件记录 服务类
 * </p>
 *
 * @author Jim
 * @since 2025-02-19
 */
public interface SysEventTrackingService extends SuperService<SysEventTrackingEntity> {

    /**
    * 新增
    * @author Jim
    * @date: 2025-02-19
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SysEventTrackingDTO.AddDTO dto);



}
