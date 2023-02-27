package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.ChangeTaskScheduleDTO;
import com.erp.model.plm.dto.HandleTaskScheduleDTO;
import com.erp.model.plm.dto.ProjectPlanTaskDTO;
import com.erp.model.plm.dto.SearchPagingDTO;
import com.erp.model.plm.entity.ProjectPlanEntity;
import com.erp.model.plm.vo.ProjectPlanDetailsVO;
import com.erp.model.plm.vo.ProjectTaskPlanAutoVO;
import com.erp.model.plm.vo.SchedulePagingVO;
import com.erp.model.workflow.dto.ProcessPassDTO;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import com.erp.server.plm.controller.AuditParamDTO;

import java.util.List;
import java.util.Map;

/**
 * 项目计划表(ProjectPlan)表服务接口
 *
 * @author yl
 * @since 2023-02-03 15:14:29
 */
public interface ProjectPlanService  extends IService<ProjectPlanEntity> {



    Boolean submitSchedule(HandleTaskScheduleDTO dto);

    /**
     * 启动流程
     * @param id
     */
    void startScheduleTaskProcess(String id);

    /**
     * 审核不通过
     * @param dto
     */
    Boolean approvalNoPass(AuditParamDTO dto);

    /**
     * 审核通过
     * @param dto
     */
    Boolean approvalPass(AuditParamDTO dto);

    /**
     * 审核情况
     *
     */
    List<ApproveNodeRecordVO> auditInfo(String id);

    Boolean cancelSchedule(List<String> ids);

    Boolean restartSchedule(String id);

    Boolean changeSchedule(String productId,List<ChangeTaskScheduleDTO> list);

    List<ProjectPlanEntity> getByIds(List<String> projectPlanIds);

    ProjectPlanDetailsVO details(String id);

    /**
     * 排期审核分页
     * @author yl
     * @date 2023-02-10 9:15
     * @param dto
     * @return com.erp.common.vo.PagingVO<java.util.List<com.erp.model.plm.vo.SchedulePagingVO>>
     */
    PagingVO<List<SchedulePagingVO>> paging(PagingDTO<SearchPagingDTO> dto);

    /**
     * 最终审核通过
     * @author yl
     * @date 2023-02-11 11:11
     * @param dto
     * @return void
     */
    void processPass(ProcessPassDTO dto);

    void checkAuditor();

    List<Map<String, Object>> getSubmitSchedule();


    /**
     * 自动排期服务
     * @param dto
     * @return
     */
    ProjectTaskPlanAutoVO autoSchedule(List<ProjectPlanTaskDTO.AutoDTO> dto);
}
