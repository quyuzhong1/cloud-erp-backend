package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpInputDataDmpRelationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpInputDataDmpRelationDTO;

/**
 * <p>
 * data表与dmp关联表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-17
 */
public interface DmpInputDataDmpRelationService extends SuperService<DmpInputDataDmpRelationEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-17
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpInputDataDmpRelationDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-17
    * @param dto
    * @return
    */
    Boolean update(DmpInputDataDmpRelationDTO.UpdateDTO dto);


}
