package com.erp.server.mrp.service;
import com.erp.model.mrp.entity.CfgRuleCommonEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.mrp.dto.CfgRuleCommonDTO;

/**
 * <p>
 * 公共配置（规则设置） 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
public interface CfgRuleCommonService extends SuperService<CfgRuleCommonEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-08-23
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgRuleCommonDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-08-23
    * @param dto
    * @return
    */
    Boolean update(CfgRuleCommonDTO.UpdateDTO dto);


}
