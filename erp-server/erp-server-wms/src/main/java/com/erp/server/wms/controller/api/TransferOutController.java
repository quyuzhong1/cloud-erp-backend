package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.TransferOutDTO;
import com.erp.server.wms.service.TransferOutService;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 调拨管理-分布式调出
 * @author lambda
 * @since 2023-05-10
 */
@AllArgsConstructor
@RestController
@RequestMapping("/transfer/out")
public class TransferOutController extends BaseController {

    private final TransferOutService transferOutService;

    /**
     * 获取状态统计
     * @return
     */
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferOut:paging",
            tableAlias = "tfo"
    )
    @PostMapping("/tabList")
    public ApiResult<List<TransferOutDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(transferOutService.listCount(dto));
    }


    /**
     * 分页列表
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferOut:paging",
            tableAlias = "tfo"
    )
    public ApiResult<PagingVO<TransferOutDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<TransferOutDTO.PagingParamDTO> dto) {
        return success(transferOutService.paging(dto));
    }


    /**
     * 提交
     * @param dto
     * @return
     */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferOut:submit",
            serviceClass = TransferOutService.class,
            keyIdName = "ids")
    public ApiResult<Void> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        transferOutService.submit(dto.getIds());
        return  success();
    }


    /**
     * 详情
     * @param id
     * @return
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferOut:view",
            serviceClass = TransferOutService.class,
            keyIdName = "id")
    public ApiResult<TransferOutDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(transferOutService.view(id));
    }

    /**
     * 修改
     * @param dto
     * @return
     */
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferOut:update",
            serviceClass = TransferOutService.class,
            keyIdName = "id")
    public ApiResult<Void> update(@RequestBody @Validated TransferOutDTO.UpdateDTO dto) {
        transferOutService.update(dto);
        return success();
    }

    /**
     * 修改并提交
     * @param dto
     * @return
     */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferOut:update",
            serviceClass = TransferOutService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated TransferOutDTO.UpdateDTO dto) {
        transferOutService.updateAndSubmit(dto);
        return success();
    }


    /**
     * 审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferOut:approve",
            serviceClass = TransferOutService.class,
            keyIdName = "ids")
    public ApiResult<Void> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        transferOutService.approve(dto);
        return success();
    }

    /**
     * 反审核
     *
     */
    @PostMapping("/disApprove")
    public ApiResult<Void> disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        return  success();
    }

    /**
     * 删除
     *
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferOut:delete",
            serviceClass = TransferOutService.class,
            keyIdName = "ids")
    public ApiResult<Void> delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        transferOutService.delete(dto.getIds());
        return success();
    }

    /**
     * 作废
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferOut:invalid",
            serviceClass = TransferOutService.class,
            keyIdName = "ids")
    public ApiResult<Void> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        transferOutService.invalid(dto.getIds(), dto.getRemark());
        return  success();
    }

    /**
     * 撤销
     * @param dto
     * @return
     */
    @PostMapping("/cancel")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferOut:cancel",
            serviceClass = TransferOutService.class,
            keyIdName = "ids")
    public ApiResult cancel(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        transferOutService.cancel(dto.getIds());
        return  success();
    }

    /**
     * 导出数据
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferOut:paging",
            tableAlias = "tfo"
    )
    public void exportList(@RequestBody @Valid TransferOutDTO.ExportDTO dto, HttpServletResponse response) {
        transferOutService.exportList(dto, response);
    }

}
