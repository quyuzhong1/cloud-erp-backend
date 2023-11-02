package com.erp.server.tms.service;
import com.erp.model.tms.entity.LogisticsPrintTypeEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsPrintTypeDTO;

/**
 * <p>
 * 面板打印设置表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
public interface LogisticsPrintTypeService extends SuperService<LogisticsPrintTypeEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsPrintTypeDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    Boolean update(LogisticsPrintTypeDTO.UpdateDTO dto);


}
