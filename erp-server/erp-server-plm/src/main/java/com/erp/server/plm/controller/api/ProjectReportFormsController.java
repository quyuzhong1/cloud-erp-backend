package com.erp.server.plm.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.ProjectReportFormsDTO;
import com.erp.server.plm.service.ProductInfoService;
import com.erp.server.plm.service.ProjectReportFormsService;
import com.erp.server.plm.service.ProjectTaskService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 项目报表
 * @Author Luo_WG
 * @Date 2023/6/12 19:10
 **/
@RestController
@RequestMapping("/ProjectReportForms")
public class ProjectReportFormsController extends BaseController {
    /**
     * 项目报表
     * @Author Luo_WG
     * @Date 2023/6/12 18:24
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.scm.dto.PurchaseBusinessGatherTableDTO.PagingViewDTO>>
     **/
    @PostMapping(value = "/purchaseBusinessGatherTable")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:ProjectReportForms:purchaseBusinessGatherTable",
            tableAlias = "pod")
    public ApiResult<List<ProjectReportFormsDTO.PagingView>> projectReportFormsPaging(@RequestBody ProjectReportFormsDTO.PagingParam dto) {
        return null;
    }

    /**
     * 任务详情
     * @Author Luo_WG
     * @Date 2023/6/12 18:24
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.scm.dto.PurchaseBusinessGatherTableDTO.PagingViewDTO>>
     **/
    @PostMapping(value = "/taskDetailView")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:ProjectReportForms:taskDetailView",
            serviceClass = ProductInfoService.class,
            keyIdName = "id")
    public ApiResult<List<ProjectReportFormsDTO.TaskDetailParam>> taskDetailView(@RequestBody ProjectReportFormsDTO.TaskDetailParam dto) {
        return null;
    }

    /**
     * 导出项目报表
     * @Author Luo_WG
     * @Date 2023/6/12 18:27
     * @param dto
     * @param response
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping(value = "/exportExcelPurchaseBusiness")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:ProjectReportForms:exportExcelProjectReportForms",
            tableAlias = "pod")
    public ApiResult exportExcelProjectReportForms(@RequestBody ProjectReportFormsDTO.PagingParam dto, HttpServletResponse response) {
        Boolean flag = Boolean.TRUE;
        return flag == true ? success() : failure();
    }

    /**
     * 导出项目任务明细
     * @Author Luo_WG
     * @Date 2023/6/12 18:27
     * @param dto
     * @param response
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping(value = "/exportExcelTaskDetail")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:ProjectReportForms:exportExcelProjectReportForms",
            tableAlias = "pod")
    public ApiResult exportExcelTaskDetail(@RequestBody ProjectReportFormsDTO.PagingParam dto, HttpServletResponse response) {
        Boolean flag = Boolean.TRUE;
        return flag == true ? success() : failure();
    }
}
