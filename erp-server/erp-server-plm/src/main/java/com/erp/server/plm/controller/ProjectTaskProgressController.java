package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.plm.dto.ProductMilepostDTO;
import com.erp.model.plm.dto.ProductMilepostDateDTO;
import com.erp.model.plm.dto.ProductMilepostParamDTO;
import com.erp.model.plm.dto.ProductPhaseProgressDTO;
import com.erp.server.plm.service.ProjectTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *  产品管理
 *
 * @author Will
 * @version 1.0
 * @date 2022/11/18 15:01
 */
@RestController
@RequestMapping("/plm/task/progress")
public class ProjectTaskProgressController extends BaseController {

    @Autowired
    private ProjectTaskService projectTaskService;

    /**
     * 任务进度-产品里程碑
     *
     * @author Will
     * @date: 2022/11/18 15:06
     * @param productId
     * @return ApiResult
     */
    @GetMapping("/milepostList")
    public ApiResult<List<ProductMilepostDTO>> milepostList(@RequestParam("productId") String productId) {
        List<ProductMilepostDTO> list = projectTaskService.getMilepostTaskListByProductId(productId);
        return success(list);
    }

    /**
     * 任务进度-查询里程碑结束时间
     *
     * @author Will
     * @date: 2022/11/18 16:48
     * @param dto
     * @return ApiResult<ProductMilepostDateDTO>
     */
    @PostMapping("/getMilepostDate")
    public ApiResult<ProductMilepostDateDTO> getMilepostDate(@RequestBody @Validated ProductMilepostParamDTO dto) {
        ProductMilepostDateDTO dateDto = projectTaskService.getMilepostDate(dto);
        return success(dateDto);
    }


    /**
     * 任务进度-任务完成进度
     *
     * @author Will
     * @date: 2022/11/21 9:25
     * @param productId
     * @return ApiResult<List<ProductPhaseProgressDTO>>
     */
    @GetMapping("/getFinishProgressList")
    public ApiResult<List<ProductPhaseProgressDTO>> getFinishProgressList(@RequestParam("productId") String productId) {
        List<ProductPhaseProgressDTO> list = projectTaskService.getFinishProgressList(productId);
        return success(list);
    }

}
