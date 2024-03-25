package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.SkuMappingRuleDTO;
import com.erp.model.oms.entity.SkuMappingRuleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * sku对照表匹配规则 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2023-12-21
 */
@Mapper
public interface SkuMappingRuleMapper extends BaseMapper<SkuMappingRuleEntity> {

    IPage<SkuMappingRuleDTO.ListDTO> paging(Page query,@Param("params") SkuMappingRuleDTO.ParamsDTO params);
}
