package com.erp.server.wms.service;
import com.erp.model.wms.entity.CfgRuleWaveRecordEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.CfgRuleWaveRecordDTO;

/**
 * <p>
 * 波次规则执行记录表 服务类
 * </p>
 *
 * @author will
 * @since 2024-07-01
 */
public interface CfgRuleWaveRecordService extends SuperService<CfgRuleWaveRecordEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-07-01
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgRuleWaveRecordDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-07-01
    * @param dto
    * @return
    */
    Boolean update(CfgRuleWaveRecordDTO.UpdateDTO dto);


}
