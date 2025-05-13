package com.erp.server.workflow.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;
import com.erp.model.workflow.entity.CfgApproveSyncEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * <p>
 * ERP审批同步配置 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-05-12
 */
@Mapper
public interface CfgApproveSyncMapper extends BaseMapper<CfgApproveSyncEntity> {

    List<CfgApproveSyncDTO.TabListDTO> tabList(@Param("params") CfgApproveSyncDTO.PagingParamDTO searchParam);

    IPage<CfgApproveSyncDTO.ListDTO> paging(Page query, @Param("params")  CfgApproveSyncDTO.PagingParamDTO params);
}
