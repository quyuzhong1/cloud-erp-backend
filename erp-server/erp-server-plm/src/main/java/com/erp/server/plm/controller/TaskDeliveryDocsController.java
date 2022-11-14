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
import com.erp.model.plm.entity.TaskDeliveryDocsEntity;
import com.erp.server.plm.service.DocsPermissionService;
import com.erp.server.plm.service.TaskDeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 产品开发管理
 * TaskDocsController
 *
 * @Description TODO
 * @Date 2022-09-22 11:36
 * @Created by yl
 */
@RestController
@RequestMapping("/plm/taskDocs")
public class TaskDeliveryDocsController extends BaseController {

    @Autowired
    private TaskDeliveryService taskDeliveryService;

    @Autowired
    private DocsPermissionService docsPermissionService;

    /**
     * 输出物-输出物列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    //  @RequestPermissions("plm:taskDocs:paging")
    public ApiResult<PagingVO<List<DeliveryDocsDTO>>> paging(@RequestBody @Validated PagingDTO<BaseSearchDTO> dto) {
        PagingVO<List<DeliveryDocsDTO>> pagingVO = taskDeliveryService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 根据任务id 获取 交付的文档列表
     * @author yl
     * @date 2022-11-10 17:48
     * @param dto
     * @return com.erp.common.dto.base.ApiResult
     */
    @PostMapping("/list")
    //  @RequestPermissions("plm:taskDocs:list")
    public ApiResult<List<DeliveryDocsDTO>> list(@RequestBody @Validated BaseIdDTO dto) {
        List<DeliveryDocsDTO> list = taskDeliveryService.getByTaskId(dto.getId());
        return success(list);
    }


    @PostMapping("/deliveryDocsList")
    //  @RequestPermissions("plm:taskDocs:list")
    public ApiResult<List<DeliveryDocsDTO>> getDeliveryDocsByTaskId(@RequestBody @Validated BaseIdDTO dto) {
        List<DeliveryDocsDTO> list = taskDeliveryService.getByTaskId(dto.getId());
        return success(list);
    }





    /**
     * 输出物-设置权限
     *
     * @param dto
     * @return
     */
    @PostMapping("/setPower")
    // @RequestPermissions("plm:taskDocs:setPower")
    public ApiResult setPower(@RequestBody @Validated SetDocsPowerDTO dto) {
        taskDeliveryService.setPower(dto);
        return success();
    }


    /**
     * 输出物-查看权限
     *
     * @param
     * @return
     */
    @GetMapping("/getPower")
    // @RequestPermissions("plm:taskDocs:setPower")
    public ApiResult<SetDocsPowerDTO> getPower(@RequestParam(value = "id") String id) {
        SetDocsPowerDTO docsPower = docsPermissionService.getDocsPower(id);
        return success(docsPower);
    }


}
