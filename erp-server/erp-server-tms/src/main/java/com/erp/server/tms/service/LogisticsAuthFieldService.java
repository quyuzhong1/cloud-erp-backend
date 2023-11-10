package com.erp.server.tms.service;
import com.erp.model.tms.entity.LogisticsAuthFieldEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsAuthFieldDTO;

import java.util.List;

/**
 * <p>
 * 物流授权字段值表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
 */
public interface LogisticsAuthFieldService extends SuperService<LogisticsAuthFieldEntity> {

    /**
    * 新增
    * @author lambda
    * @date: 2023-11-09
    * @param logisticsAuthId
    * @return
    */
    Boolean add(String logisticsAuthId, List<LogisticsAuthFieldDTO.AddDTO> list);

    /**
    * 修改
    * @author lambda
    * @date: 2023-11-09
    * @param dto
    * @return
    */
    Boolean update(LogisticsAuthFieldDTO.UpdateDTO dto);


}
