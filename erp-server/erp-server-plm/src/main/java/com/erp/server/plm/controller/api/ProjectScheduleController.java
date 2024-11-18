package com.erp.server.plm.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.AuditParamDTO;
import com.erp.model.plm.dto.HandleTaskScheduleDTO;
import com.erp.model.plm.dto.ProjectPlanTaskDTO;
import com.erp.model.plm.dto.SearchPagingDTO;
import com.erp.model.plm.vo.ProjectPlanDetailsVO;
import com.erp.model.plm.vo.ProjectTaskPlanAutoVO;
import com.erp.model.plm.vo.SchedulePagingVO;
import com.erp.model.workflow.dto.ProcessPassDTO;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import com.erp.server.plm.service.ProjectPlanService;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 产品排期
 *
 * @author yl
 * @since 2023-02-03 15:14:29
 */
@RestController
@LogSystemModule("产品排期审核")
@RequestMapping("product/schedule")
@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
public class ProjectScheduleController extends BaseController {
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
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:product:schedule:paging",
            tableAlias = "pp"
    )
    public ApiResult<PagingVO<SchedulePagingVO>> queryByPage(@RequestBody @Validated PagingDTO<SearchPagingDTO> dto) {
        PagingVO<SchedulePagingVO> pagingVO = projectPlanService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 提交排期
     *
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交排期", keyIdName = "productId")
    @PostMapping("/submit")
    public ApiResult<Object> submitSchedule(@RequestBody @Validated HandleTaskScheduleDTO dto) {
        Boolean result = projectPlanService.submitSchedule(dto);
        return result == true ? success() : failure();
    }


    /**
     * 获取审核状态
     *
     * @return
     */
    @GetMapping("/scheduleStatus")
    public ApiResult<Object> submitSchedule() {
        List<Map<String,Object>> list=projectPlanService.getSubmitSchedule();
        return success(list);
    }


    /**
     * 取消排期
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "取消排期")
    @PostMapping("/cancel")
    public ApiResult<Object> cancelSchedule(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = projectPlanService.cancelSchedule(Arrays.asList(dto.getId()));
        return result == true ? success() : failure();
    }

    /**
     * 重启排期
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "重启排期：id={id}")
    @PostMapping("/restart")
    public ApiResult<Object> restartSchedule(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = projectPlanService.restartSchedule(dto.getId());
        return result == true ? success() : failure();
    }

    /**
     * 详情
     */
    @LogViewService
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
    @LogAction(value = LogActionEnum.APPROVE, desc = "排期审核通过")
    @PostMapping("/approvalPass")
    public ApiResult<Object> approvalPass(@RequestBody @Validated AuditParamDTO dto) {
       Boolean flag= projectPlanService.approvalPass(dto);
        return flag==true?success():failure();
    }


    /**
     * 排期  审核 不通过
     *
     * @param
     * @return 新增结果
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "排期审核不通过")
    @PostMapping("/approvalNoPass")
    public ApiResult<Object> approvalNoPass(@RequestBody @Validated AuditParamDTO dto) {
        Boolean flag= projectPlanService.approvalNoPass(dto);
        return flag==true?success():failure();
    }


    /**
     * 排期 审核通过后改变  状态
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "排期审核通过后改变状态:流程id={processId},具体业务表id={businessTableId}")
    @PostMapping("/workflow/pass")
    public ApiResult<Object> processPass(@RequestBody ProcessPassDTO dto) {
        projectPlanService.processPass(dto);
        return success();
    }

    /**
     * 审核情况
     *
     * @return
     */
    @PostMapping("/auditInfo")
    public ApiResult<Object> auditInfo(@RequestBody @Validated BaseIdDTO dto) {
        List<ApproveNodeRecordVO> list=  projectPlanService.auditInfo(dto.getId());
        return success(list);
    }


    /**
     * 自动排期
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "自动排期:入口类型={type},需要排期列表={list}")
    @PostMapping("/auto")
    public ApiResult<ProjectTaskPlanAutoVO> autoSchedule(@RequestBody @Validated ProjectPlanTaskDTO.AutoDTo dto){
        ProjectTaskPlanAutoVO resultVO = projectPlanService.autoSchedule(dto);
        return success(resultVO);
    }

    /**
     * 导入project文件
     * @author Will
     * @date: 2023/6/2 9:13
     * @param excelFile
     * @param productId
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导入project文件")
    @PostMapping(value = "/importProjectSchedule")
    public ApiResult<Object> importProjectSchedule(@RequestParam("excelFile") MultipartFile excelFile, @RequestParam(value = "productId") String  productId) {
        Boolean flag = projectPlanService.importProjectSchedule(excelFile,productId);
        return flag == true ? success() : failure();
    }


}

