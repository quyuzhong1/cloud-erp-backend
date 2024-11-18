package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.SysTaskPagingDTO;
import com.erp.model.plm.dto.SysTaskPagingSearchDTO;
import com.erp.model.plm.entity.ProjectTaskSysEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 系统任务 Mapper 接口
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Mapper
public interface ProjectTaskSysMapper extends BaseMapper<ProjectTaskSysEntity> {

    IPage<SysTaskPagingDTO> paging(Page<SysTaskPagingSearchDTO> query, @Param("params") SysTaskPagingSearchDTO params);

    List<SysTaskPagingDTO> getSysTaskDocsNames();
}
