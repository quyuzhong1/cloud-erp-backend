package com.erp.server.wms.controller.api;


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
import com.erp.model.wms.dto.TransferInfoDTO;
import com.erp.model.wms.entity.TransferInfoEntity;
import com.erp.server.wms.query.TransferInfoQueryHandler;
import com.erp.server.wms.service.TransferInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 直接调拨单主表
 *
 * @author will
 * @since 2023-05-10
 */
@Slf4j
@RestController
@LogSystemModule("直接调拨单")
@RequestMapping("/transferInfo")
public class TransferInfoController extends BaseController {

    @Resource
    private TransferInfoService transferInfoService;

    /**
     * 列表查询
     * @author Will
     * @date: 2023/5/10 19:56
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferInfo:paging",
            tableAlias = "ti"
    )
    @WebAdvanceQuery(handler = TransferInfoQueryHandler.class)
    public ApiResult<PagingVO<TransferInfoDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<TransferInfoDTO.SearchParamDTO> dto) {
        PagingVO<TransferInfoDTO.ListDTO> pagingVO = transferInfoService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 列表数量
     * @author Will
     * @date: 2023/5/10 20:08
     * @param dto
     * @return ApiResult<List<ListStatusCountDTO>>
     */
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferInfo:paging",
            tableAlias = "ti"
    )
    public ApiResult<List<TransferInfoDTO.ListStatusCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<TransferInfoDTO.ListStatusCountDTO> list = transferInfoService.listCount(dto);
        return success(list);
    }

    /**
     * 新增
     * @author Will
     * @date: 2023/5/10 19:58
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增直接调拨单")
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferInfo:add",
            serviceClass = TransferInfoService.class,
            keyIdName = "id")
    public ApiResult add(@RequestBody @Validated TransferInfoDTO.AddDTO dto) {
        String id = transferInfoService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 新增并提交
     * @author Will
     * @date: 2023/5/10 19:59
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交直接调拨单")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferInfo:add",
            serviceClass = TransferInfoService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated TransferInfoDTO.AddDTO dto) {
        String id = transferInfoService.addAndSubmit(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改
     * @author Will
     * @date: 2023/5/10 20:02
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改直接调拨单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferInfo:update",
            serviceClass = TransferInfoService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated TransferInfoDTO.UpdateDTO dto) {
        Boolean flag = transferInfoService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改并提交
     * @author Will
     * @date: 2023/5/10 20:02
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交直接调拨单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferInfo:update",
            serviceClass = TransferInfoService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated TransferInfoDTO.UpdateDTO dto) {
        Boolean flag = transferInfoService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 提交
     * @author Will
     * @date: 2023/5/10 20:00
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交直接调拨单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferInfo:submit",
            serviceClass = TransferInfoService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = transferInfoService.submit(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 查看详情
     * @author Will
     * @date: 2023/5/10 20:10
     * @param id
     * @return ApiResult
     */
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferInfo:view",
            serviceClass = TransferInfoService.class,
            keyIdName = "id")
    public ApiResult<TransferInfoDTO.ViewDTO> view(@RequestParam("id") String id) {
        TransferInfoDTO.ViewDTO dto = transferInfoService.view(id);
        return success(dto);
    }


    /**
     * 删除
     * @author Will
     * @date: 2023/5/10 20:09
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除直接调拨单")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferInfo:delete",
            serviceClass = TransferInfoService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = transferInfoService.delete(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 作废
     * @author Will
     * @date: 2023/5/10 20:11
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INVALID, desc = "作废直接调拨单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferInfo:invalid",
            serviceClass = TransferInfoService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean flag = transferInfoService.invalid(dto.getIds(),dto.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @author Will
     * @date: 2023/5/10 20:11
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核直接调拨单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferInfo:approve",
            serviceClass = TransferInfoService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<TransferInfoEntity> entityList = transferInfoService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            TransferInfoEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"直接调拨单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(transferInfoService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess(),Boolean.TRUE));
            }catch (Exception e){
                log.error("直接调拨单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量反审核
     * @author Will
     * @date: 2023/5/10 20:12
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核直接调拨单")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferInfo:disApprove",
            serviceClass = TransferInfoService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<TransferInfoEntity> entityList = transferInfoService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            TransferInfoEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"直接调拨单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(transferInfoService.disApprove(entity,Boolean.TRUE,Boolean.TRUE));
            }catch (Exception e){
                log.error("直接调拨单反审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 取消流程
     * @author Will
     * @date: 2023/5/10 20:24
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销直接调拨单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferInfo:cancelProcess",
            serviceClass = TransferInfoService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = transferInfoService.cancelProcess(dto.getIds());
        return result == true ? success() : failure();
    }

    /**
     * 导出
     * @author Will
     * @date: 2023/5/10 20:25
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出直接调拨单")
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody TransferInfoDTO.SearchParamDTO dto) {
        Boolean flag = transferInfoService.exportExcel(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 检测 sku 是否缺货
     * @param dto
     * @return
     */
    @PostMapping("/checkSkuInventory")
    public ApiResult<String> checkSkuInventory(@RequestBody @Validated() TransferInfoDTO.AddDTO dto) {
        String msg = transferInfoService.checkSkuInventory(dto, dto.getDetailList());
        return success( "", msg);
    }

}
