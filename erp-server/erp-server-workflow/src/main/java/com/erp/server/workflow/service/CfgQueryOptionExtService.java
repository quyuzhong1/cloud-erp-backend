package com.erp.server.workflow.service;
import com.erp.model.workflow.entity.CfgQueryOptionExtEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.CfgQueryOptionExtDTO;

/**
 * <p>
 * cfg_query_option拓展表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-06-04
 */
public interface CfgQueryOptionExtService extends SuperService<CfgQueryOptionExtEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-06-04
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgQueryOptionExtDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-06-04
    * @param dto
    * @return
    */
    Boolean update(CfgQueryOptionExtDTO.UpdateDTO dto);


}
