package com.erp.server.dmp.mapper;
import com.erp.model.dmp.entity.DmpEtlTaskEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.dmp.dto.DmpEtlTaskDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * etl任务 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2025-07-21
 */
@Mapper
public interface DmpEtlTaskMapper extends BaseMapper<DmpEtlTaskEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<DmpEtlTaskDTO.ListDTO> paging(Page query, @Param("params") DmpEtlTaskDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") DmpEtlTaskDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<DmpEtlTaskDTO.ListDTO> listExport(@Param("params") DmpEtlTaskDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<DmpEtlTaskDTO.TabListDTO> tabList(@Param("params") DmpEtlTaskDTO.PagingParamDTO searchParam);
}
