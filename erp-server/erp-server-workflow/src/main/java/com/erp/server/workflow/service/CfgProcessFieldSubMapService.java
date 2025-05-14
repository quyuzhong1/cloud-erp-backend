package com.erp.server.workflow.service;
import com.erp.model.workflow.entity.CfgProcessFieldSubMapEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.CfgProcessFieldSubMapDTO;

import java.util.List;

/**
 * <p>
 * fieldList 明细字段映射 服务类
 * </p>
 *
 * @author hcg
 * @since 2025-05-14
 */
public interface CfgProcessFieldSubMapService extends SuperService<CfgProcessFieldSubMapEntity> {

    /**
    * 新增
    * @author hcg
    * @date: 2025-05-14
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO addOrUpdate(String parentId, List<CfgProcessFieldSubMapDTO.AddOrUpdateDTO> dto);

    /**
    * 修改
    * @author hcg
    * @date: 2025-05-14
    * @param dto
    * @return
    */
//    Boolean update(CfgProcessFieldSubMapDTO.UpdateDTO dto);


}
