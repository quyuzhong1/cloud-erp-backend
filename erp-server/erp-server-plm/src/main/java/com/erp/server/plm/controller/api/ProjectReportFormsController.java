package com.erp.server.plm.controller.api;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.ProjectReportFormsDTO;
import com.erp.server.plm.service.ProjectReportFormsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 项目报表
 * @Author Luo_WG
 * @Date 2023/6/12 19:10
 **/
@RestController
@LogSystemModule("项目报表")
@RequestMapping("/ProjectReportForms")
public class ProjectReportFormsController extends BaseController {
    @Resource
    private ProjectReportFormsService projectReportFormsService;

    /**
     * 项目报表-分页查询
     * @Author Luo_WG
     * @Date 2023/6/12 18:24
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.scm.dto.PurchaseBusinessGatherTableDTO.PagingViewDTO>>
     **/
    @PostMapping(value = "/purchaseBusinessGatherTable")
/*    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:ProjectReportForms:purchaseBusinessGatherTable",
            tableAlias = "pod")*/
    public ApiResult<PagingVO<List<ProjectReportFormsDTO.PagingView>>> projectReportFormsPaging(@RequestBody PagingDTO<ProjectReportFormsDTO.PagingParam> dto) {
        PagingVO<List<ProjectReportFormsDTO.PagingView>> listPagingVO = projectReportFormsService.projectReportFormsPaging(dto);
        return success(listPagingVO);
    }

    /**
     * 任务详情
     * @Author Luo_WG
     * @Date 2023/6/12 18:24
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.scm.dto.PurchaseBusinessGatherTableDTO.PagingViewDTO>>
     **/
    @PostMapping(value = "/taskDetailView")
/*    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:ProjectReportForms:taskDetailView",
            serviceClass = ProductInfoService.class,
            keyIdName = "id")*/
    public ApiResult<List<ProjectReportFormsDTO.TaskDetail>> taskDetailView(@RequestBody ProjectReportFormsDTO.TaskDetailParam dto) {
        List<ProjectReportFormsDTO.TaskDetail> list = projectReportFormsService.taskDetailView(dto);
        return success(list);
    }

    /**
     * 导出项目报表
     * @Author Luo_WG
     * @Date 2023/6/12 18:27
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出项目报表")
    @PostMapping(value = "/exportExcelPurchaseBusiness")
/*    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:ProjectReportForms:exportExcelProjectReportForms",
            tableAlias = "pod")*/
    public ApiResult exportExcelProjectReportForms(@RequestBody ProjectReportFormsDTO.PagingParam dto) {
        Boolean flag = projectReportFormsService.exportExcelProjectReportForms(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 导出项目任务明细
     * @Author Luo_WG
     * @Date 2023/6/12 18:27
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出项目任务明细")
    @PostMapping(value = "/exportExcelTaskDetail")
/*    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:ProjectReportForms:exportExcelProjectReportForms",
            tableAlias = "pod")*/
    public ApiResult exportExcelTaskDetail(@RequestBody ProjectReportFormsDTO.TaskDetailParam dto) {
        Boolean flag = projectReportFormsService.exportExcelTaskDetail(dto);
        return flag == true ? success() : failure();
    }
}
