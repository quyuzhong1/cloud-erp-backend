package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.BiTargetYearEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.BiTargetYearDTO;

/**
 * <p>
 * 年度目标表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
public interface BiTargetYearService extends SuperService<BiTargetYearEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    String add(BiTargetYearDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    Boolean update(BiTargetYearDTO.UpdateDTO dto);


}
