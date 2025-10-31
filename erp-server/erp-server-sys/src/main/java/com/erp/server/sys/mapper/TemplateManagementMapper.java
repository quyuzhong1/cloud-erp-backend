package com.erp.server.sys.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.sys.dto.CfgThirdNoticeDTO;
import com.erp.model.sys.dto.TemplateManagementDTO;
import com.erp.model.sys.entity.TemplateManagementEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * <p>
 * 合同模板主表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-07-24
 */
@Mapper
public interface TemplateManagementMapper extends BaseMapper<TemplateManagementEntity> {

    List<TemplateManagementDTO.TabListDTO> tabList(@Param("params") TemplateManagementDTO.PagingParamDTO searchParam);

    IPage<TemplateManagementDTO.ListDTO> paging(Page query,@Param("params") TemplateManagementDTO. PagingParamDTO params);

    List<TemplateManagementDTO.PageSelectDTO> pagingSelect(@Param("params") TemplateManagementDTO.SelectDTO params);
}
