package com.erp.server.dmp.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.dmp.dto.RulePromptWordDTO;
import com.erp.model.dmp.entity.RulePromptWordEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * <p>
 * 汉化管理规则表 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2025-01-17
 */
@Mapper
public interface RulePromptWordMapper extends BaseMapper<RulePromptWordEntity> {

    IPage<RulePromptWordDTO.ListDTO> paging(Page query, RulePromptWordDTO.PagingParamDTO params);

    List<RulePromptWordEntity> list();
}
