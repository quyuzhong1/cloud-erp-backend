package com.erp.server.wms.service;
import com.erp.model.wms.entity.CfgVirtualTransRulesEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.CfgVirtualTransRulesDTO;

/**
 * <p>
 * 虚拟库存交易规则表 服务类
 * </p>
 *
 * @author will
 * @since 2024-06-03
 */
public interface CfgVirtualTransRulesService extends SuperService<CfgVirtualTransRulesEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-06-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgVirtualTransRulesDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-06-03
    * @param dto
    * @return
    */
    Boolean update(CfgVirtualTransRulesDTO.UpdateDTO dto);


}
