package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.TransferOutDTO;
import com.erp.model.wms.entity.TransferOutEntity;
import com.erp.server.wms.query.TransferOutQueryHandler;
import com.erp.server.wms.service.TransferOutService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 调拨管理-分布式调出
 * @author lambda
 * @since 2023-05-10
 */
@AllArgsConstructor
@RestController
@LogSystemModule("分布式调出单")
@RequestMapping("/transfer/out")
@Slf4j
public class TransferOutController extends BaseController {

    private final TransferOutService transferOutService;

    /**
     * 获取状态统计
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id,warehouse_keeper_id",
            warehouseTableField = "tfo.out_warehouse_id",
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
            tableField = "create_user_id,warehouse_keeper_id",
            warehouseTableField = "tfo.out_warehouse_id",
            menuCode = "wms:transfer:out:paging",
            tableAlias = "tfo"
    )
    @WebAdvanceQuery(handler = TransferOutQueryHandler.class)
    public ApiResult<PagingVO<TransferOutDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<TransferOutDTO.PagingParamDTO> dto) {
        return success(transferOutService.paging(dto));
    }


    /**
     * 提交
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交分布式调出单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
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
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
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
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改分布式调出单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
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
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交分布式调出单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
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
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核分布式调出单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:transfer:out:approve",
            serviceClass = TransferOutService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<TransferOutEntity> entityList = transferOutService.listByIds(ids);
        for (String id : ids) {
            TransferOutEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"分布式调出单不存在"));
                continue;
            }
            try {
                resultDTOS.add(transferOutService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()), entity));
            }catch (Exception e){
                log.error("分布式调出单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 反审核
     *
     */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核分布式调出单")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:transfer:out:disApprove",
            serviceClass = TransferOutService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<TransferOutEntity> entityList = transferOutService.listByIds(ids);
        for (String id : ids) {
            TransferOutEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"分布式调出单不存在"));
                continue;
            }
            try {
                resultDTOS.add(transferOutService.disApprove(entity));
            }catch (Exception e){
                log.error("分布式调出单反审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 删除
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除记录")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:transfer:out:delete",
            serviceClass = TransferOutService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        try {
            List<BatchResultDTO> resultDTOS = transferOutService.deleteByIds(dto.getIds(), true);
            return success(resultDTOS);
        } catch (Exception e) {
            log.error("批量删除分布式调出单失败", e);
            return failure(e.getMessage());
        }
    }

    /**
     * 作废
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INVALID, desc = "作废分布式调出单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
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
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销分布式调出单")
    @PostMapping("/cancel")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
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
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出分布式调出单")
    @PostMapping("/export")
    public ApiResult<Boolean> exportList(@RequestBody @Valid TransferOutDTO.ExportDTO dto) {
        transferOutService.exportList(dto);
        return success(true);
    }


    /**
     * 下推分布式调入数据显示
     * @param dto
     * @return
     */
    @PostMapping(value = "/viewGenerateTransferIn")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id,warehouse_keeper_id",
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
    @LogAction(value = LogActionEnum.INSERT, desc = "下推分布式调入数据保存")
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

    /**
     *上架详情弹窗
     * @return
     */
    @GetMapping("/listPutawayDetail")
    public ApiResult<List<TransferOutDTO.PutawayDetailDTO>> listPutawayDetail(@RequestParam("detailId") String detailId) {
        return success(transferOutService.listPutawayDetail(detailId));
    }

}
