package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpCfgInputChildEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpCfgInputChildDTO;

/**
 * <p>
 * 父子任务关系 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-21
 */
public interface DmpCfgInputChildService extends SuperService<DmpCfgInputChildEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-21
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpCfgInputChildDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-21
    * @param dto
    * @return
    */
    Boolean update(DmpCfgInputChildDTO.UpdateDTO dto);


}
