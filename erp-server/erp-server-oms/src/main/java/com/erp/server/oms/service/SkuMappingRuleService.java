package com.erp.server.oms.service;
import com.erp.model.oms.entity.SkuMappingRuleEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.SkuMappingRuleDTO;

import java.util.List;

/**
 * <p>
 * sku对照表匹配规则 服务类
 * </p>
 *
 * @author lrp
 * @since 2023-12-21
 */
public interface SkuMappingRuleService extends SuperService<SkuMappingRuleEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2023-12-21
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SkuMappingRuleDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2023-12-21
    * @param dto
    * @return
    */
    Boolean update(SkuMappingRuleDTO.UpdateDTO dto);


    List<SkuMappingRuleEntity> listOrderByPriority();
}
