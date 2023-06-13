package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.ValidList;
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
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transfer:out:paging",
            tableAlias = "tfo"
    )
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
            menuCode = "wms:transfer:out:paging",
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
            menuCode = "wms:transfer:out:submit",
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
            menuCode = "wms:transfer:out:view",
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
            menuCode = "wms:transfer:out:update",
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
            menuCode = "wms:transfer:out:approve",
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transfer:out:disApprove",
            serviceClass = TransferOutService.class,
            keyIdName = "ids")
    public ApiResult<Void> disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        transferOutService.disApprove(dto.getIds());
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
            menuCode = "wms:transfer:out:delete",
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
            menuCode = "wms:transfer:out:invalid",
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
            menuCode = "wms:transfer:out:cancel",
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
            menuCode = "wms:transfer:out:export",
            tableAlias = "tfo"
    )
    public void exportList(@RequestBody @Valid TransferOutDTO.ExportDTO dto, HttpServletResponse response) {
        transferOutService.exportList(dto, response);
    }


    /**
     * 下推分布式调入数据显示
     * @param dto
     * @return
     */
    @PostMapping(value = "/viewGenerateTransferIn")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transfer:out:viewGenerateTransferIn",
            tableAlias = "tfo"
    )
    public ApiResult<List<TransferOutDTO.ViewGenerateTransferInDTO>> viewGenerateTransferIn(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<TransferOutDTO.ViewGenerateTransferInDTO> list = transferOutService.viewGenerateTransferIn(dto.getIds());
        return success(list);
    }

    /**
     * 下推分布式调入数据保存
     * @param dto
     * @return
     */
    @PostMapping(value = "/generateTransferIn")
    public ApiResult<Void> generateTransferIn(@RequestBody @Validated ValidList<TransferOutDTO.GenerateTransferInDTO> dto) {
        transferOutService.generateTransferIn(dto);
        return success();
    }

    /**
     * 下推分布式调入单修改页面选择产品信息
     * @param param
     * @return
     */
    @PostMapping("/listTransferOut")
    public ApiResult<List<TransferOutDTO.ChooseListDTO>> listTransferOut(@RequestBody @Valid TransferOutDTO.SearchParamDTO param) {
        return success(transferOutService.listTransferOut(param));
    }

}
