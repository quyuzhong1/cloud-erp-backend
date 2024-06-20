package com.erp.server.wms.service;
import com.erp.model.wms.entity.CfgRuleWaveEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.CfgRuleWaveDTO;

/**
 * <p>
 * 波次规则 服务类
 * </p>
 *
 * @author will
 * @since 2024-06-20
 */
public interface CfgRuleWaveService extends SuperService<CfgRuleWaveEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-06-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgRuleWaveDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-06-20
    * @param dto
    * @return
    */
    Boolean update(CfgRuleWaveDTO.UpdateDTO dto);


}
