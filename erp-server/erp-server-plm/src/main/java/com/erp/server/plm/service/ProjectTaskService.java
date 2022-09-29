package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProjectTaskEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 产品任务表 服务类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
public interface ProjectTaskService extends IService<ProjectTaskEntity> {


    void addSysTask(String id);

    void removeTaskByProductId(String productId);

    List<ProjectTaskEntity> getByProductIds(List<String> productIds);

    List<ProjectTaskEntity> getByProductId(String productId);

    void copyTaskByProject(String saveProductId, String saveProjectId,String  flagProjectId);

    void copyTaskByTemplate(String saveProductId, String saveProjectId, String flagTemplateId);

    void copyTaskBySys(String productId, String projectId);

    PagingVO paging(PagingDTO<TaskPagingDTO> dto);

    Boolean save(ProjectTaskDTO dto);

    List<Map<String, Object>> getTaskListByProductId(BasicProductIdDTO dto);

    Boolean removeTask(String id);

    Boolean setPreTask(setPreTaskDTO dto);


    List<TaskConductDTO> getTaskConductList(String projectId);
    TaskConductDTO getTaskConduct(String productId);
    List<TaskExcelDTO> getExportTask(List<String> productIds);
}
