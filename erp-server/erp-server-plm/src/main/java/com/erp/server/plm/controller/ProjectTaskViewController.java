package com.erp.server.plm.controller;

import com.common.business.annotation.DataPermission;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.plm.dto.*;
import com.erp.server.plm.service.ProjectTaskViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 任务视图
 *
 * @author Will
 * @version 1.0
 * @date 2022/11/22 18:06
 */
@RestController
@RequestMapping("/plm/task/view")
public class ProjectTaskViewController extends BaseController {

    @Autowired
    private ProjectTaskViewService projectTaskViewService;

    /**
     *
     * 任务视图-按人员查看
     * @author Will
     * @date: 2022/11/23 11:32
     * @param dto
     * @return ApiResult<List<ProductTaskPersonnelChildDTO>>
     */
    @PostMapping("/getPersonnelView")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "plm:task:view:getPersonnelView", tableAlias = "t")
    public ApiResult<List<ProductTaskPersonnelChildDTO>> getPersonnelView(@RequestBody ProductTaskViewSearchDTO dto) {
        List<ProductTaskPersonnelChildDTO> list = projectTaskViewService.getPersonnelView(dto);
        return success(list);
    }

    /**
     *
     * 任务视图-按产品查看
     * @author Will
     * @date: 2022/11/23 11:33
     * @param dto
     * @return ApiResult<List<ProductTaskProductChildDTO>>
     */
    @PostMapping("/getProductView")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "plm:task:view:getProductView", tableAlias = "t")
    public ApiResult<List<ProductTaskProductChildDTO>> getProductView(@RequestBody ProductTaskViewSearchDTO dto) {
        List<ProductTaskProductChildDTO> list = projectTaskViewService.getProductView(dto);
        return success(list);
    }

    /**
     *
     * 任务视图-按阶段查看
     * @author Will
     * @date: 2022/11/23 11:34
     * @param dto
     * @return ApiResult<List<ProductTaskPhaseChildDTO>>
     */
    @PostMapping("/getPhaseView")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "plm:task:view:getPhaseView", tableAlias = "t")
    public ApiResult<List<ProductTaskPhaseChildDTO>> getPhaseView(@RequestBody ProductTaskViewSearchDTO dto) {
        List<ProductTaskPhaseChildDTO> list = projectTaskViewService.getPhaseView(dto);
        return success(list);
    }

    /**
     *
     * 任务视图-按量产入库时间查看
     * @author Will
     * @date: 2022/11/23 11:34
     * @param dto
     * @return ApiResult<List<ProductTaskInWarehouseTimeChildDTO>>
     */
    @PostMapping("/getInWarehouseTimeView")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "plm:task:view:getInWarehouseTimeView", tableAlias = "t")
    public ApiResult<List<ProductTaskInWarehouseTimeChildDTO>> getInWarehouseTimeView(@RequestBody ProductTaskViewSearchDTO dto) {
        List<ProductTaskInWarehouseTimeChildDTO> list = projectTaskViewService.getInWarehouseTimeView(dto);
        return success(list);
    }

    /**
     *  任务视图-导出
     * @author Will
     * @date: 2022/11/23 18:45
     * @param dto
     * @param response
     */
    @PostMapping(value = "/exportExcel")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "plm:task:view:exportExcel", tableAlias = "t")
    public void exportProduct(@RequestBody ProductTaskViewSearchDTO dto, HttpServletResponse response) {
        projectTaskViewService.exportExcel(dto, response);
    }
}
