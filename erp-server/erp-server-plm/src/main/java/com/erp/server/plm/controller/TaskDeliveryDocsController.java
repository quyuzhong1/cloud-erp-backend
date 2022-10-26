package com.erp.server.plm.controller;

import com.erp.common.annotation.RequestPermissions;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.DeliveryDocsDTO;
import com.erp.model.plm.dto.SetDocsPowerDTO;
import com.erp.server.plm.service.TaskDeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**  产品开发管理
 *  TaskDocsController
 * @Description TODO
 * @Date 2022-09-22 11:36
 * @Created by yl
 */
@RestController
@RequestMapping("/plm/taskDocs")
public class TaskDeliveryDocsController extends BaseController {

    @Autowired
    private TaskDeliveryService taskDeliveryService;

    /**
     * 输出物-输出物列表
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    //  @RequestPermissions("plm:taskDocs:paging")
    public ApiResult<PagingVO<List<DeliveryDocsDTO>>> paging(@RequestBody @Validated PagingDTO<BaseSearchDTO> dto) {
        PagingVO<List<DeliveryDocsDTO>> pagingVO = taskDeliveryService.paging(dto);
        return success(pagingVO);
    }

    @GetMapping("/list")
    //  @RequestPermissions("plm:taskDocs:list")
    public ApiResult list(@RequestBody @Validated BaseIdDTO dto) {
        List<DeliveryDocsDTO> list = taskDeliveryService.getByTaskId(dto.getId());
        return success(list);
    }

    /**
     * 输出物-权限
     * @param dto
     * @return
     */
    @PostMapping("/setPower")
    // @RequestPermissions("plm:taskDocs:setPower")
    public ApiResult setPower(@RequestBody @Validated SetDocsPowerDTO dto) {
        taskDeliveryService.setPower(dto);
        return success();
    }
}
