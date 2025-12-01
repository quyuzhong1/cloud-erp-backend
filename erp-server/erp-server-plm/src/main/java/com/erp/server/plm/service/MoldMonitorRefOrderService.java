package com.erp.server.plm.service;
import com.erp.model.plm.entity.MoldMonitorRefOrderEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.MoldMonitorRefOrderDTO;

/**
 * <p>
 * 模具监控关联单据 服务类
 * </p>
 *
 * @author jack
 * @since 2025-10-24
 */
public interface MoldMonitorRefOrderService extends SuperService<MoldMonitorRefOrderEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-10-24
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(MoldMonitorRefOrderDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-10-24
    * @param dto
    * @return
    */
    Boolean update(MoldMonitorRefOrderDTO.UpdateDTO dto);


}
