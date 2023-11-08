package com.erp.server.tms.service;
import com.erp.model.tms.entity.LogisticsOrderOperateLogEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsOrderOperateLogDTO;

/**
 * <p>
 * 物流平台订单操作记录 服务类
 * </p>
 *
 * @author zdy
 * @since 2023-11-08
 */
public interface LogisticsOrderOperateLogService extends SuperService<LogisticsOrderOperateLogEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2023-11-08
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsOrderOperateLogDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2023-11-08
    * @param dto
    * @return
    */
    Boolean update(LogisticsOrderOperateLogDTO.UpdateDTO dto);


}
