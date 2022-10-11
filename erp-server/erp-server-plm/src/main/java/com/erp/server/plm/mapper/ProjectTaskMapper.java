package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.model.plm.dto.ProjectTaskDetailsDTO;
import com.erp.model.plm.dto.RefTaskInfoDTO;
import com.erp.model.plm.dto.TaskExcelDTO;
import com.erp.model.plm.dto.TaskSearchDTO;
import com.erp.model.plm.entity.ProjectTaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 产品任务表 Mapper 接口
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Mapper
public interface ProjectTaskMapper extends BaseMapper<ProjectTaskEntity> {


    IPage paging(Page query,@Param("productId") String productId ,@Param("phaseId") String phaseId, @Param("searchList") List<TaskSearchDTO> searchList,@Param("userId") String userId);

    List<TaskExcelDTO> getExportTask(@Param("productIds") List<String> productIds);

    /**
     * 获取任务详情
     * @param taskId
     * @return
     */
    ProjectTaskDetailsDTO getTaskDetails(@Param("taskId") String taskId);


    List<RefTaskInfoDTO> getRefTask(@Param("taskIds") List<String> taskIds);
}

