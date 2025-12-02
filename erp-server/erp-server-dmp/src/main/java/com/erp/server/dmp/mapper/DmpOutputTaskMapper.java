package com.erp.server.dmp.mapper;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.dmp.dto.DmpOutputTaskDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 推送任务 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2024-07-01
 */
@Mapper
public interface DmpOutputTaskMapper extends BaseMapper<DmpOutputTaskEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<DmpOutputTaskDTO.ListDTO> paging(Page query, @Param("params") DmpOutputTaskDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") DmpOutputTaskDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<DmpOutputTaskDTO.ListDTO> listExport(@Param("params") DmpOutputTaskDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<DmpOutputTaskDTO.TabListDTO> tabList(@Param("params") DmpOutputTaskDTO.PagingParamDTO searchParam);
}
