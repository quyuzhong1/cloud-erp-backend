package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpInputFileMongoRelationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpInputFileMongoRelationDTO;

/**
 * <p>
 * file与mongo关联表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-19
 */
public interface DmpInputFileMongoRelationService extends SuperService<DmpInputFileMongoRelationEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-19
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpInputFileMongoRelationDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-19
    * @param dto
    * @return
    */
    Boolean update(DmpInputFileMongoRelationDTO.UpdateDTO dto);


}
