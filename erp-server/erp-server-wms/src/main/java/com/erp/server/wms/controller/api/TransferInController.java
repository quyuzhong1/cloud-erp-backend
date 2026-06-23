package com.erp.server.wms.controller.api;


import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.ApproveDTO;
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
import com.erp.model.wms.dto.TransferInDTO;
import com.erp.model.wms.entity.TransferInEntity;
import com.erp.server.wms.query.TransferInQueryHandler;
import com.erp.server.wms.service.TransferInService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 调拨管理-分布式调入
 *
 * @author lambda
 * @since 2023-05-10
 */
@RestController
@LogSystemModule("分布式调入单")
@RequestMapping("/transfer/in")
@Slf4j
public class TransferInController extends BaseController {

    @Resource
    private TransferInService transferInService;


    /**
     * 获取 tab列表
     *
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id,warehouse_keeper_id",
            warehouseTableField = "ti.out_warehouse_id,ti.in_warehouse_id",
            menuCode = "wms:transfer:in:paging",
            tableAlias = "ti"
    )
    public ApiResult<List<TransferInDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<TransferInDTO.TabListDTO> tabList = transferInService.tabList(dto);
        return success(tabList);
    }

    /**
     * 分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id,warehouse_keeper_id",
            warehouseTableField = "ti.out_warehouse_id,ti.in_warehouse_id",
            menuCode = "wms:transfer:in:paging",
            tableAlias = "ti"
    )
    @WebAdvanceQuery(handler = TransferInQueryHandler.class)
    public ApiResult<PagingVO<TransferInDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<TransferInDTO.PagingParamDTO> dto) {
        PagingVO<TransferInDTO.PagingViewDTO> pagingVO = transferInService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 分布式调出单下推 分布式调入
     *
     * @param list
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-05-23 15:15
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "分布式调出单下推")
    @PostMapping("/generateTransferIn")
    public ApiResult generateTransferIn(@RequestBody @Valid ValidList<TransferInDTO.ViewGenerateTransferInDTO> list) {
        Boolean result = transferInService.generateTransferIn(list);
        return result ? success() : failure();

    }


    /**
     * 提交
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交分布式调入单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:transfer:in:submit",
            serviceClass = TransferInService.class,
            keyIdName = "ids"
    )
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = transferInService.submit(dto.getIds());
        return result ? success() : failure();
    }


    /**
     * 详情
     *
     * @param dto
     * @return
     */
    @LogViewService
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:transfer:in:view",
            serviceClass = TransferInService.class,
            keyIdName = "id"
    )
    public ApiResult<TransferInDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        TransferInDTO.ViewDTO result = transferInService.view(dto.getId());
        return success(result);
    }

    /**
     * 修改
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改分布式调入单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:transfer:in:update",
            serviceClass = TransferInService.class,
            keyIdName = "id"
    )
    public ApiResult update(@RequestBody @Validated TransferInDTO.UpdateDTO dto) {
        String id = transferInService.updateTransferIn(dto);
        return CharSequenceUtil.isNotBlank(id) ? success() : failure();

    }

    /**
     * 修改并提交
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交分布式调入单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:transfer:in:update",
            serviceClass = TransferInService.class,
            keyIdName = "id"
    )
    public ApiResult updateAndSubmit(@RequestBody @Validated TransferInDTO.UpdateDTO dto) {
        Boolean result = transferInService.updateAndSubmit(dto);
        return result ? success() : failure();

    }

    /**
     * 审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核分布式调入单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:transfer:in:approve",
            serviceClass = TransferInService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<TransferInEntity> entityList = transferInService.listByIds(ids);
        for (String id : ids) {
            TransferInEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"分布式调入单不存在"));
                continue;
            }
            try {
                resultDTOS.add(transferInService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()), entity));
            }catch (Exception e){
                log.error("分布式调入单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 撤销流程
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销分布式调入单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:transfer:in:cancelProcess",
            serviceClass = TransferInService.class,
            keyIdName = "ids"
    )
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = transferInService.cancelProcess(new ApproveDTO.BatchCancelProcessDTO(dto.getIds()));
        return result ? success() : failure();
    }

    /**
     * 反审核
     */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核分布式调入单")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:transfer:in:disApprove",
            serviceClass = TransferInService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<TransferInEntity> entityList = transferInService.listByIds(ids);
        for (String id : ids) {
            TransferInEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"分布式调出单不存在"));
                continue;
            }
            try {
                resultDTOS.add(transferInService.disApprove(entity));
            }catch (Exception e){
                log.error("分布式调入单反审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 删除分布式调入单
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除记录")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:transfer:in:delete",
            serviceClass = TransferInService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOList = transferInService.deleteByIds(dto.getIds(), true);
        return  resultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOList) : failure(resultDTOList);
    }

    /**
     * 作废
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2023/5/10 20:11
     */
    @LogAction(value = LogActionEnum.INVALID, desc = "作废分布式调入单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:transfer:in:invalid",
            serviceClass = TransferInService.class,
            keyIdName = "ids"
    )
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean result = transferInService.invalid(dto.getIds(), dto.getRemark());
        return result ? success() : failure();
    }

    /**
     * 导出
     * 数据
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出分布式调入单")
    @PostMapping("/export")
    public ApiResult exportWarehouse(@RequestBody @Valid TransferInDTO.ExportDTO dto) {
        Boolean result = transferInService.exportExcel(dto);
        return result ? success() : failure();
    }


}
