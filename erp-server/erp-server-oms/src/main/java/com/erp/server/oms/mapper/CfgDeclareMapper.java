package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.CfgDeclareDTO;
import com.erp.model.oms.entity.CfgDeclareEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 申报规则表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2024-05-08
 */
@Mapper
public interface CfgDeclareMapper extends BaseMapper<CfgDeclareEntity> {

    IPage<CfgDeclareDTO.PagingViewDTO> paging(Page query, @Param("params") CfgDeclareDTO.PagingParamDTO params);
}
