package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.CfgRuleDeclareDTO;
import com.erp.model.oms.entity.CfgRuleDeclareEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.ss.formula.functions.T;


/**
 * <p>
 * 申报规则表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2024-05-08
 */
@Mapper
public interface CfgRuleDeclareMapper extends BaseMapper<CfgRuleDeclareEntity> {

    IPage<CfgRuleDeclareDTO.PagingViewDTO> paging(Page<T> query, @Param("params") CfgRuleDeclareDTO.PagingParamDTO params);
}
