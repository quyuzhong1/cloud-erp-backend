package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
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
 * 产品管理
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
     * @return ApiResult<List<ProductTaskPersonnelViewDTO>>
     */
    @PostMapping("/getPersonnelView")
    public ApiResult<List<ProductTaskPersonnelViewDTO>> getPersonnelView(@RequestBody ProductTaskViewSearchDTO dto) {
        List<ProductTaskPersonnelViewDTO> list = projectTaskViewService.getPersonnelView(dto);
        return success(list);
    }

    /**
     *
     * 任务视图-按产品查看
     * @author Will
     * @date: 2022/11/23 11:33
     * @param dto
     * @return ApiResult<List<ProductTaskProductViewDTO>>
     */
    @PostMapping("/getProductView")
    public ApiResult<List<ProductTaskProductViewDTO>> getProductView(@RequestBody ProductTaskViewSearchDTO dto) {
        List<ProductTaskProductViewDTO> list = projectTaskViewService.getProductView(dto);
        return success(list);
    }

    /**
     *
     * 任务视图-按阶段查看
     * @author Will
     * @date: 2022/11/23 11:34
     * @param dto
     * @return ApiResult<List<ProductTaskPhaseViewDTO>>
     */
    @PostMapping("/getPhaseView")
    public ApiResult<List<ProductTaskPhaseViewDTO>> getPhaseView(@RequestBody ProductTaskViewSearchDTO dto) {
        List<ProductTaskPhaseViewDTO> list = projectTaskViewService.getPhaseView(dto);
        return success(list);
    }

    /**
     *
     * 任务视图-按量产入库时间查看
     * @author Will
     * @date: 2022/11/23 11:34
     * @param dto
     * @return ApiResult<List<ProductTaskInWarehouseTimeViewDTO>>
     */
    @PostMapping("/getInWarehouseTimeView")
    public ApiResult<List<ProductTaskInWarehouseTimeViewDTO>> getInWarehouseTimeView(@RequestBody ProductTaskViewSearchDTO dto) {
        List<ProductTaskInWarehouseTimeViewDTO> list = projectTaskViewService.getInWarehouseTimeView(dto);
        return success(list);
    }

    /**
     * @description: 任务视图导出
     * @author Will
     * @date: 2022/11/23 18:45
     * @param dto
     * @param response

     */
    @PostMapping(value = "/exportExcel")
    public void exportProduct(@RequestBody ProductTaskViewSearchDTO dto, HttpServletResponse response) {
        projectTaskViewService.exportExcel(dto, response);
    }
}
