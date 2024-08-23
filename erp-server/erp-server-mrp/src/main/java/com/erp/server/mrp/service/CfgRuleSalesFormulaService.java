package com.erp.server.mrp.service;
import com.erp.model.mrp.entity.CfgRuleSalesFormulaEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.mrp.dto.CfgRuleSalesFormulaDTO;

/**
 * <p>
 * 销量公式（规则设置） 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
public interface CfgRuleSalesFormulaService extends SuperService<CfgRuleSalesFormulaEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-08-23
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgRuleSalesFormulaDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-08-23
    * @param dto
    * @return
    */
    Boolean update(CfgRuleSalesFormulaDTO.UpdateDTO dto);


}
