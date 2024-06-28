package com.erp.server.wms.service;
import com.erp.model.wms.entity.CfgRuleOutEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.CfgRuleOutDTO;

/**
 * <p>
 * 出库配置规则 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-06-28
 */
public interface CfgRuleOutService extends SuperService<CfgRuleOutEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-06-28
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgRuleOutDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2024-06-28
    * @param dto
    * @return
    */
    Boolean update(CfgRuleOutDTO.UpdateDTO dto);


}
