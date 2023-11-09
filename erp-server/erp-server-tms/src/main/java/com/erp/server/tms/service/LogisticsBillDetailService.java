package com.erp.server.tms.service;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsBillDetailDTO;

/**
 * <p>
 * 物流单明细表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
 */
public interface LogisticsBillDetailService extends SuperService<LogisticsBillDetailEntity> {

    /**
    * 新增
    * @author lambda
    * @date: 2023-11-09
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsBillDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author lambda
    * @date: 2023-11-09
    * @param dto
    * @return
    */
    Boolean update(LogisticsBillDetailDTO.UpdateDTO dto);


}
