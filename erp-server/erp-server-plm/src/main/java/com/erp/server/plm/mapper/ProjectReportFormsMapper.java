package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.ProjectReportFormsDTO;
import com.erp.model.plm.entity.ProductInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProjectReportFormsMapper extends BaseMapper<ProductInfoEntity> {
    /**
     * 项目报表-分页查询
     * @Author Luo_WG
     * @Date 2023/6/16 16:25
     * @param query
     * @param param
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.plm.dto.ProjectReportFormsDTO.PagingView>
     **/
    IPage<ProjectReportFormsDTO.PagingView> projectReportFormsPaging(Page query, @Param("params") ProjectReportFormsDTO.PagingParam param);

    /**
     * 项目报表-导出excel
     * @Author Luo_WG
     * @Date 2023/6/17 13:15
     * @param param
     * @return java.util.List<com.erp.model.plm.dto.ProjectReportFormsDTO.PagingView>
     **/
    List<ProjectReportFormsDTO.PagingView> projectReportFormsExportExcel(@Param("params") ProjectReportFormsDTO.PagingParam param);
    Page<ProjectReportFormsDTO.PagingView> projectReportFormsExportExcel(@Param("page") Page<ProjectReportFormsDTO.PagingView> page,  @Param("params") ProjectReportFormsDTO.PagingParam param);

    /**
     * 任务详情
     * @Author Luo_WG
     * @Date 2023/6/19 10:29
     * @param dto
     * @return java.util.List<com.erp.model.plm.dto.ProjectReportFormsDTO.TaskDetailParam>
     **/
    List<ProjectReportFormsDTO.TaskDetail> taskDetailView(@Param("params") ProjectReportFormsDTO.TaskDetailParam dto);
    Page<ProjectReportFormsDTO.TaskDetail> taskDetailView(@Param("page") Page<ProjectReportFormsDTO.TaskDetail> page, @Param("params") ProjectReportFormsDTO.TaskDetailParam dto);
}
