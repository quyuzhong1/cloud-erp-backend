package com.erp.server.workflow.service;
import com.erp.model.workflow.entity.DictCfgSysFieldEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.DictCfgSysFieldDTO;

/**
 * <p>
 * 数大臣单据字段 服务类
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
 */
public interface DictCfgSysFieldService extends SuperService<DictCfgSysFieldEntity> {

    /**
    * 新增
    * @author hcg
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DictCfgSysFieldDTO.AddDTO dto);

    /**
    * 修改
    * @author hcg
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    Boolean update(DictCfgSysFieldDTO.UpdateDTO dto);


}
