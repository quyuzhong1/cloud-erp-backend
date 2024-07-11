package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpInputMongoDmpRelationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpInputMongoDmpRelationDTO;

/**
 * <p>
 * mongo与dmp关联表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-19
 */
public interface DmpInputMongoDmpRelationService extends SuperService<DmpInputMongoDmpRelationEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-19
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpInputMongoDmpRelationDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-19
    * @param dto
    * @return
    */
    Boolean update(DmpInputMongoDmpRelationDTO.UpdateDTO dto);


}
