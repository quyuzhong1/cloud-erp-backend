package com.erp.server.sys.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.sys.dto.CfgQueryConditionDTO;
import com.erp.model.sys.dto.CfgQueryOptionDTO;
import com.erp.model.sys.entity.CfgQueryOptionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;


/**
 * <p>
 * 查询option配置表 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2024-01-04
 */
@Mapper
public interface CfgQueryOptionMapper extends BaseMapper<CfgQueryOptionEntity> {

    IPage<CfgQueryOptionDTO.ListDTO> paging(Page query, CfgQueryOptionDTO.ParamDTO params);
}
