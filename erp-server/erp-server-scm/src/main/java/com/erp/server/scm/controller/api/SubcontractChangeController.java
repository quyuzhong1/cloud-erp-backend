package com.erp.server.scm.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.scm.dto.SubcontractChangeDTO;
import com.erp.server.scm.query.SubcontractChangeQueryHandler;
import com.erp.server.scm.service.SubcontractChangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 委外变更单
 *
 * @author will
 * @since 2023-06-08
 */
@RestController
@LogSystemModule("委外变更订单")
@RequestMapping("/subcontractChangeOrder")
public class SubcontractChangeController extends BaseController {

    @Autowired
    private SubcontractChangeService subcontractChangeService;

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:subcontractChangeOrder:paging",
            tableAlias = "sc"
    )
    public ApiResult<List<SubcontractChangeDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(subcontractChangeService.tabList(dto));
    }

    /**
    * 列表查询
    * @author will
    * @date: 2023-06-08
    * @param dto
    * @return ApiResult<PagingVO<SubcontractChangeOrderDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:subcontractChangeOrder:paging",
            tableAlias = "sc"
    )
    @WebAdvanceQuery(handler = SubcontractChangeQueryHandler.class)
    public ApiResult<PagingVO<SubcontractChangeDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SubcontractChangeDTO.PagingParamDTO> dto) {
        return success(subcontractChangeService.paging(dto));
    }

   /**
   * 新增
   * @author will
   * @date:  2023-06-08
   * @param dto
   * @return ApiResult<Void>
   */
   @LogAction(value = LogActionEnum.INSERT, desc = "新增委外变更订单")
   @PostMapping("/add")
   public ApiResult<Void> add(@RequestBody @Validated SubcontractChangeDTO.AddDTO dto) {
      subcontractChangeService.add(dto);
      return success();
   }

    /**
    * 修改
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改委外变更订单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractChangeOrder:update",
            serviceClass = SubcontractChangeService.class,
            keyIdName = "id")
    public ApiResult<Void> update(@RequestBody @Validated SubcontractChangeDTO.UpdateDTO dto) {
        subcontractChangeService.update(dto);
        return success();
    }

    /**
    * 新增并提交审核
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交委外变更订单")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractChangeOrder:submit",
            serviceClass = SubcontractChangeService.class,
            keyIdName = "id")
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated SubcontractChangeDTO.AddDTO dto) {
        subcontractChangeService.addAndSubmit(dto);
        return success();
    }

    /**
    * 修改并提交审核
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交委外变更订单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractChangeOrder:submit",
            serviceClass = SubcontractChangeService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated SubcontractChangeDTO.UpdateDTO dto) {
        subcontractChangeService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交委外变更订单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractChangeOrder:submit",
            serviceClass = SubcontractChangeService.class,
            keyIdName = "ids")
    public ApiResult<Void> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        subcontractChangeService.submit(dto.getIds());
        return success();
    }

    /**
    * 审核
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核委外变更订单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractChangeOrder:approve",
            serviceClass = SubcontractChangeService.class,
            keyIdName = "ids")
    public ApiResult<Void> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        subcontractChangeService.approve(dto);
        return success();
    }

    /**
     * 批量作废
     * @author Will
     * @date: 2023-06-08
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INVALID, desc = "批量作废委外变更订单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractChangeOrder:invalid",
            serviceClass = SubcontractChangeService.class,
            keyIdName = "ids")
    public ApiResult<Void> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        subcontractChangeService.invalid(dto.getIds(),dto.getRemark());
        return success();
    }

    /**
    * 撤销
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销委外变更订单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractChangeOrder:cancelProcess",
            serviceClass = SubcontractChangeService.class,
            keyIdName = "ids")
    public ApiResult<Void> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        subcontractChangeService.cancelProcess(new ApproveDTO.BatchCancelProcessDTO(dto.getIds()));
        return success();
    }

    /**
    * 详情
    * @author will
    * @date:  2023-06-08
    * @param id
    * @return ApiResult<SubcontractChangeOrderDTO.ViewDTO>>
    */
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractChangeOrder:view",
            serviceClass = SubcontractChangeService.class,
            keyIdName = "id")
    public ApiResult<SubcontractChangeDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(subcontractChangeService.view(id));
    }

    /**
    * 导出Excel数据
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return
    */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出委外变更订单")
    @PostMapping("/export")
    public ApiResult<Boolean> exportList(@RequestBody @Validated SubcontractChangeDTO.PagingParamDTO dto) {
        subcontractChangeService.exportList(dto);
        return success(true);
    }

}
