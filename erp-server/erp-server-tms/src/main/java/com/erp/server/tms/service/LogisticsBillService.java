package com.erp.server.tms.service;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsBillDTO;

/**
 * <p>
 * 物流单 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
 */
public interface LogisticsBillService extends SuperService<LogisticsBillEntity> {

    /**
    * 新增
    * @author lambda
    * @date: 2023-11-09
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsBillDTO.AddDTO dto);

    /**
    * 修改
    * @author lambda
    * @date: 2023-11-09
    * @param dto
    * @return
    */
    Boolean update(LogisticsBillDTO.UpdateDTO dto);


}
