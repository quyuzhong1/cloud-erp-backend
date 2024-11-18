package com.erp.server.oms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SkuMappingRuleDTO;
import com.erp.model.oms.entity.SkuMappingRuleEntity;

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


    List<SkuMappingRuleEntity> listOrderByPriorityAndUpdateTime();

    Boolean enableOrDisable(SkuMappingRuleDTO.StatusDTO dto);

    SkuMappingRuleDTO.ViewDTO view(String id);

    List<String> getSkuRuleTest(SkuMappingRuleDTO.RuleTestDTO dto);

    void handleSkuMapping(List<String> skuMappingIds);

    PagingVO<SkuMappingRuleDTO.ListDTO> paging(PagingDTO<SkuMappingRuleDTO.ParamsDTO> dto);
}
