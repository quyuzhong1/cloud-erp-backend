package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.CfgConditionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.CfgConditionDTO;

/**
 * <p>
 * 条件配置表 服务类
 * </p>
 *
 * @author lrp
 * @since 2025-01-20
 */
public interface CfgConditionService extends SuperService<CfgConditionEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2025-01-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgConditionDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2025-01-20
    * @param dto
    * @return
    */
    Boolean update(CfgConditionDTO.UpdateDTO dto);


}
