package com.erp.server.plm.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.ProductMilepostShowDTO;
import com.erp.model.plm.dto.ProductProgressShowDTO;
import com.erp.server.plm.service.ProjectTaskProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 *  任务进度
 *
 * @author Will
 * @version 1.0
 * @date 2022/11/18 15:01
 */
@RestController
@RequestMapping("task/progress")
public class ProjectTaskProgressController extends BaseController {

    @Autowired
    private ProjectTaskProgressService projectTaskProgressService;

    /**
     * 任务进度-产品里程碑
     *
     * @author Will
     * @date: 2022/11/18 15:06
     * @param productId
     * @return ApiResult
     */
    @GetMapping("/milepostList")
    public ApiResult<ProductMilepostShowDTO> milepostList(@RequestParam("productId") String productId) {
        ProductMilepostShowDTO dto = projectTaskProgressService.getMilepostTaskListByProductId(productId);
        return success(dto);
    }

    /**
     * 任务进度-任务完成进度
     *
     * @author Will
     * @date: 2022/11/21 9:25
     * @param productId
     * @return ApiResult<productProgressShowDTO>
     */
    @GetMapping("/getFinishProgressList")
    public ApiResult<ProductProgressShowDTO> getFinishProgressList(@RequestParam("productId") String productId) {
        ProductProgressShowDTO dto = projectTaskProgressService.getFinishProgressList(productId);
        return success(dto);
    }

}
