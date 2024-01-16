package com.erp.server.sys.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.sys.dto.CfgQueryConditionDTO;
import com.erp.model.sys.entity.CfgQueryConditionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 查询条件配置表 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2024-01-04
 */
@Mapper
public interface CfgQueryConditionMapper extends BaseMapper<CfgQueryConditionEntity> {
    List<CfgQueryConditionDTO.ViewDTO> getQueryConditionByCode(@Param("code") String code);

    IPage<CfgQueryConditionDTO.ListDTO> paging(Page query,@Param("params")  CfgQueryConditionDTO.SearchParamDTO params);

    IPage<CfgQueryConditionDTO.MenuDTO> menuPaging(Page query,@Param("params") CfgQueryConditionDTO.MenuSearchParamDTO params);
}
