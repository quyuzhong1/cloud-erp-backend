package com.erp.server.workflow.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.workflow.dto.ThirdProcessDefinitionDTO;
import com.erp.model.workflow.entity.ThirdProcessDefinitionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


/**
 * <p>
 * 三方审批定义 Mapper 接口
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
 */
@Mapper
public interface ThirdProcessDefinitionMapper extends BaseMapper<ThirdProcessDefinitionEntity> {

    IPage<ThirdProcessDefinitionDTO.ListDTO> paging(Page query, ThirdProcessDefinitionDTO.@NotNull(message = "参数不能为空") @Valid PagingParamDTO params);
}
