package com.erp.server.scm.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.scm.dto.SupplierPhaseDTO;
import com.erp.server.scm.query.SupplierPhaseQueryHandler;
import com.erp.server.scm.service.SupplierPhaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 供应商阶段管理
 *
 * @author admin
 * @since 2023-03-15
 */
@RestController
@LogSystemModule("供应商阶段审核")
@RequestMapping("/supplier/phase")
public class SupplierPhaseController extends BaseController {


    @Resource
    private SupplierPhaseService supplierPhaseService;

    /**
     * tab列表
     *
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:supplier:phase:paging",
            tableAlias = "sp"
    )
    public ApiResult<List<SupplierPhaseDTO.TabFlagDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<SupplierPhaseDTO.TabFlagDTO> tabList = supplierPhaseService.tabList(dto);
        return success(tabList);
    }

    /**
     * 供应商阶段分页列表
     *
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:supplier:phase:paging",
            tableAlias = "sp"
    )
    @WebAdvanceQuery(handler = SupplierPhaseQueryHandler.class)
    public ApiResult<PagingVO<SupplierPhaseDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<SupplierPhaseDTO.PagingParamDTO> dto) {
        PagingVO<SupplierPhaseDTO.PagingViewDTO> pagingVO = supplierPhaseService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 添加供应商阶段
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "添加供应商阶段")
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated SupplierPhaseDTO.AddDTO dto) {
        String id = supplierPhaseService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改供应商阶段
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改供应商阶段")
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated SupplierPhaseDTO.UpdateDTO dto) {
        String id = supplierPhaseService.updateSupplierPhase(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改并审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并审核供应商阶段")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:supplier:phase:submit",
            serviceClass = SupplierPhaseService.class,
            keyIdName = "id"
    )
    public ApiResult updateAndSubmit(@RequestBody @Validated SupplierPhaseDTO.UpdateDTO dto) {
        Boolean result = supplierPhaseService.updateAndSubmit(dto);
        return result == true ? success() : failure();
    }

    /**
     * 提交并审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交供应商阶段")
    @PostMapping("/addAndSubmit")
    public ApiResult addAndSubmit(@RequestBody @Validated SupplierPhaseDTO.AddDTO dto) {
        Boolean result = supplierPhaseService.addAndSubmit(dto);
        return result == true ? success() : failure();
    }

    /**
     * 供应商阶段提交审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "供应商阶段提交审核")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:supplier:phase:submit",
            serviceClass = SupplierPhaseService.class,
            keyIdName = "ids"
    )
    public ApiResult submit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = supplierPhaseService.submit(dto.getIds());
        return result == true ? success() : failure();
    }

    /**
     * 供应商阶段 详情
     *
     * @param dto
     * @return
     */
    @LogViewService
    @PostMapping("/view")
    public ApiResult<SupplierPhaseDTO.UpdateDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        SupplierPhaseDTO.UpdateDTO supplierPhase = supplierPhaseService.view(dto.getId());
        return success(supplierPhase);
    }


    /**
     * 供应商阶段审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "供应商阶段审核")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:supplier:phase:approve",
            serviceClass = SupplierPhaseService.class,
            keyIdName = "ids"
    )
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        Boolean result = supplierPhaseService.approve(dto);
        return result == true ? success() : failure();
    }


    /**
     * 取消流程
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-03-23 17:57
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销供应商阶段")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:supplier:phase:cancelProcess",
            serviceClass = SupplierPhaseService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = supplierPhaseService.cancelProcess(dto.getIds());
        return result == true ? success() : failure();
    }


    /**
     * 删除供应商阶段
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除供应商阶段")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:supplier:phase:delete",
            serviceClass = SupplierPhaseService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = supplierPhaseService.deleteByIds(dto.getIds());
        return result == true ? success() : failure();
    }

    /**
     * 导出供应商阶段审核
     * @author will
     * @date 2025/7/28 10:30
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出供应商阶段审核")
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:supplier:phase:paging",
            tableAlias = "sp"
    )
    @WebAdvanceQuery(handler = SupplierPhaseQueryHandler.class)
    public ApiResult export(@RequestBody @Valid SupplierPhaseDTO.PagingParamDTO dto) {
        supplierPhaseService.export(dto);
        return success();
    }
}
