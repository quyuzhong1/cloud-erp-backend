package com.erp.server.plm.controller;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.common.business.dto.base.BaseIdDTO;
import com.erp.common.business.dto.base.PagingDTO;
import com.erp.model.workflow.dto.ProcessPassDTO;
import com.erp.common.business.vo.PagingVO;
import com.erp.model.plm.dto.HandleTaskScheduleDTO;
import com.erp.model.plm.dto.SearchPagingDTO;
import com.erp.model.plm.vo.ProjectPlanDetailsVO;
import com.erp.model.plm.vo.SchedulePagingVO;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import com.erp.server.plm.service.ProjectPlanService;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 产品排期
 *
 * @author yl
 * @since 2023-02-03 15:14:29
 */
@RestController
@RequestMapping("plm/product/schedule")
@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
public class ProjectPlanController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private ProjectPlanService projectPlanService;


    /**
     * 产品排期 分页
     *
     * @param
     * @return 查询结果
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<List<SchedulePagingVO>>> queryByPage(@RequestBody @Validated PagingDTO<SearchPagingDTO> dto) {
        PagingVO<List<SchedulePagingVO>> pagingVO = projectPlanService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 提交排期
     *
     * @return
     */
    @PostMapping("/submit")
    public ApiResult submitSchedule(@RequestBody @Validated HandleTaskScheduleDTO dto) {
        Boolean result = projectPlanService.submitSchedule(dto);
        return result == true ? success() : failure();
    }


    /**
     * 获取审核状态
     *
     * @return
     */
    @GetMapping("/scheduleStatus")
    public ApiResult submitSchedule() {
        List<Map<String,Object>> list=projectPlanService.getSubmitSchedule();
        return success(list);
    }


    /**
     * 取消排期
     */
    @PostMapping("/cancel")
    public ApiResult cancelSchedule(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = projectPlanService.cancelSchedule(dto.getId());
        return result == true ? success() : failure();
    }

    /**
     * 重启排期
     */
    @PostMapping("/restart")
    public ApiResult restartSchedule(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = projectPlanService.restartSchedule(dto.getId());
        return result == true ? success() : failure();
    }

    /**
     * 详情
     */
    @PostMapping("/view")
    public ApiResult<ProjectPlanDetailsVO> details(@RequestBody @Validated BaseIdDTO dto) {
        ProjectPlanDetailsVO resultVO = projectPlanService.details(dto.getId());
        return success(resultVO);
    }


    /**
     * 排期 审核 通过
     *
     * @param
     * @return 新增结果
     */
    @PostMapping("/approvalPass")
    public ApiResult approvalPass(@RequestBody @Validated AuditParamDTO dto) {
       Boolean flag= projectPlanService.approvalPass(dto);
        return flag==true?success():failure();
    }


    /**
     * 排期  审核 不通过
     *
     * @param
     * @return 新增结果
     */
    @PostMapping("/approvalNoPass")
    public ApiResult approvalNoPass(@RequestBody @Validated AuditParamDTO dto) {
        Boolean flag= projectPlanService.approvalNoPass(dto);
        return flag==true?success():failure();
    }


    /**
     * 排期 审核通过后改变  状态
     */
    @PostMapping("/workflow/pass")
    public ApiResult processPass(@RequestBody ProcessPassDTO dto) {
        projectPlanService.processPass(dto);
        return success();
    }

    /**
     * 审核情况
     *
     * @return
     */
    @PostMapping("/auditInfo")
    public ApiResult auditInfo(@RequestBody @Validated BaseIdDTO dto) {
        List<ApproveNodeRecordVO> list=  projectPlanService.auditInfo(dto.getId());
        return success(list);
    }

}

