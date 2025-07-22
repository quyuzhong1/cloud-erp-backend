package com.erp.server.wms.controller.api;


import cn.hutool.core.text.CharSequenceUtil;
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
import com.common.core.utils.MathUtil;
import com.erp.model.wms.dto.PickingDetailDTO;
import com.erp.model.wms.dto.SingleApproveParamDTO;
import com.erp.model.wms.dto.TransferApplicationDTO;
import com.erp.model.wms.entity.TransferApplicationEntity;
import com.erp.server.wms.query.TransferApplicationQueryHandler;
import com.erp.server.wms.service.TransferApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *  调拨申请单
 *
 * @author will
 * @since 2023-05-10
 */
@Slf4j
@RestController
@LogSystemModule("调拨申请单")
@RequestMapping("/transferApplication")
public class TransferApplicationController extends BaseController {

    @Resource
    private TransferApplicationService transferApplicationService;


    /**
     * 列表查询
     * @author Will
     * @date: 2023/5/10 19:56
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "apply_user_id,create_user_id",
            warehouseTableField = "ta.in_warehouse_id,ta.out_warehouse_id",
            menuCode = "wms:transferApplication:paging",
            tableAlias = "ta"
    )
    @WebAdvanceQuery(handler = TransferApplicationQueryHandler.class)
    public ApiResult<PagingVO<TransferApplicationDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<TransferApplicationDTO.SearchParamDTO> dto) {
        PagingVO<TransferApplicationDTO.ListDTO> pagingVO = transferApplicationService.paging(dto);
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
            tableField = "apply_user_id,create_user_id",
            warehouseTableField = "ta.in_warehouse_id,ta.out_warehouse_id",
            menuCode = "wms:transferApplication:paging",
            tableAlias = "ta"
    )
    public ApiResult<List<TransferApplicationDTO.ListStatusCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<TransferApplicationDTO.ListStatusCountDTO> list = transferApplicationService.listCount(dto);
        return success(list);
    }

    /**
     * 新增
     * @author Will
     * @date: 2023/5/10 19:58
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增调拨申请单")
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id,create_user_id",
            menuCode = "wms:transferApplication:add",
            serviceClass = TransferApplicationService.class,
            keyIdName = "id")
    public ApiResult add(@RequestBody @Validated TransferApplicationDTO.AddDTO dto) {
        String id = transferApplicationService.add(dto);
        return CharSequenceUtil.isNotBlank(id) ? success() : failure();
    }

    /**
     * 新增并提交
     * @author Will
     * @date: 2023/5/10 19:59
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交调拨申请单")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id,create_user_id",
            menuCode = "wms:transferApplication:add",
            serviceClass = TransferApplicationService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated TransferApplicationDTO.AddDTO dto) {
        String id = transferApplicationService.addAndSubmit(dto);
        return CharSequenceUtil.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改
     * @author Will
     * @date: 2023/5/10 20:02
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改调拨申请单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id,create_user_id",
            menuCode = "wms:transferApplication:update",
            serviceClass = TransferApplicationService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated TransferApplicationDTO.UpdateDTO dto) {
        Boolean flag = transferApplicationService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改并提交
     * @author Will
     * @date: 2023/5/10 20:02
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交调拨申请单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id,create_user_id",
            menuCode = "wms:transferApplication:update",
            serviceClass = TransferApplicationService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated TransferApplicationDTO.UpdateDTO dto) {
        Boolean flag = transferApplicationService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 提交
     * @author Will
     * @date: 2023/5/10 20:00
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交调拨申请单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id,create_user_id",
            menuCode = "wms:transferApplication:submit",
            serviceClass = TransferApplicationService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, TransferApplicationEntity> entityMap = transferApplicationService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            TransferApplicationEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"调拨申请单不存在"));
                continue;
            }
            try {
                resultDTOS.add(transferApplicationService.submit(entity));
            }catch (Exception e){
                log.error("调拨申请单提交失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
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
            tableField = "apply_user_id,create_user_id",
            menuCode = "wms:transferApplication:view",
            serviceClass = TransferApplicationService.class,
            keyIdName = "id")
    public ApiResult<TransferApplicationDTO.ViewDTO> view(@RequestParam("id") String id) {
        TransferApplicationDTO.ViewDTO dto = transferApplicationService.view(id);
        return success(dto);
    }


    /**
     * 删除
     * @author Will
     * @date: 2023/5/10 20:09
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除调拨申请单")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id,create_user_id",
            menuCode = "wms:transferApplication:delete",
            serviceClass = TransferApplicationService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = transferApplicationService.delete(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 作废
     * @author Will
     * @date: 2023/5/10 20:11
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INVALID, desc = "作废调拨申请单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id,create_user_id",
            menuCode = "wms:transferApplication:invalid",
            serviceClass = TransferApplicationService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean flag = transferApplicationService.invalid(dto.getIds(),dto.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @author Will
     * @date: 2023/5/10 20:11
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核调拨申请单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id,create_user_id",
            menuCode = "wms:transferApplication:approve",
            serviceClass = TransferApplicationService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<TransferApplicationEntity> entityList = transferApplicationService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            TransferApplicationEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"调拨申请单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(transferApplicationService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess()));
            }catch (Exception e){
                log.error("调拨申请单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 单个审核审核
     * @Author Luo_WG
     * @Date 2023/6/30 10:22
     * @param singleApproveParamDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.APPROVE, desc = "单审核调拨申请单")
    @PostMapping("/singleApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id,create_user_id",
            menuCode = "wms:transferApplication:approve",
            serviceClass = TransferApplicationService.class,
            keyIdName = "ids")
    public ApiResult singleApprove(@RequestBody @Validated SingleApproveParamDTO singleApproveParamDTO) {
        transferApplicationService.singleApprove(singleApproveParamDTO);
        return success();
    }

    /**
     * 批量反审核
     * @author Will
     * @date: 2023/5/10 20:12
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核调拨申请单")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id,create_user_id",
            menuCode = "wms:transferApplication:disApprove",
            serviceClass = TransferApplicationService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<TransferApplicationEntity> entityList = transferApplicationService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            TransferApplicationEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"调拨申请单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(transferApplicationService.disApprove(entity));
            }catch (Exception e){
                log.error("调拨申请单反审核失败",e);
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
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销调拨申请单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id,create_user_id",
            menuCode = "wms:transferApplication:cancelProcess",
            serviceClass = TransferApplicationService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = transferApplicationService.cancelProcess(dto.getIds());
        return result == true ? success() : failure();
    }

    /**
     * 导出
     * @author Will
     * @date: 2023/5/10 20:25
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出调拨申请单")
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody TransferApplicationDTO.SearchParamDTO dto) {
        Boolean flag = transferApplicationService.exportExcel(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 下推直接调拨单数据显示
     * @author Will
     * @date: 2023/5/10 18:39
     * @param dto
     * @return ApiResult<List<ViewGenerateTransferInfoDTO>>
     */
    @PostMapping(value = "/viewGenerateTransferInfo")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "apply_user_id,create_user_id",
            menuCode = "wms:transferApplication:viewGenerateTransferInfo",
            tableAlias = "ta"
    )
    public ApiResult<List<TransferApplicationDTO.ViewGenerateTransferInfoDTO>> viewGenerateTransferInfo(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<TransferApplicationDTO.ViewGenerateTransferInfoDTO> list = transferApplicationService.viewGenerateTransferInfo(dto.getIds());
        return success(list);
    }

    /**
     * 下推直接调拨单保存
     * @author Will
     * @date: 2023/5/12 10:52
     * @param validList
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "下推直接调拨单保存")
    @PostMapping("/generateTransferInfo")
    public ApiResult generateTransferInfo(@RequestBody @Valid ValidList<TransferApplicationDTO.GenerateTransferInfoDTO> validList) {
        Boolean flag = transferApplicationService.generateTransferInfo(validList);
        return flag == true ? success() : failure();
    }

    /**
     * 下推分布式调出数据显示
     * @author Will
     * @date: 2023/5/10 18:39
     * @param dto
     * @return ApiResult<List<ViewGenerateTransferInfoDTO>>
     */
    @PostMapping(value = "/viewGenerateTransferOut")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "apply_user_id,create_user_id",
            menuCode = "wms:transferApplication:viewGenerateTransferOut",
            tableAlias = "ta"
    )
    public ApiResult<List<TransferApplicationDTO.ViewGenerateTransferInfoDTO>> viewGenerateTransferOut(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<TransferApplicationDTO.ViewGenerateTransferInfoDTO> list = transferApplicationService.viewGenerateTransferOut(dto.getIds());
        return success(list);
    }

    /**
     * 下推分布式调出保存
     * @author Will
     * @date: 2023/5/12 10:52
     * @param validList
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "下推分布式调出保存")
    @PostMapping("/generateTransferOut")
    public ApiResult generateTransferOut(@RequestBody @Valid ValidList<TransferApplicationDTO.GenerateTransferInfoDTO> validList) {
        Boolean flag = transferApplicationService.generateTransferOut(validList);
        return flag == true ? success() : failure();
    }

    /**
     * 查询拣货明细
     * @author Will
     * @date: 2023/5/16 12:09
     * @param dto
     * @return ApiResult<List<CommonDTO>>
     */
    @PostMapping("/listPickingDetail")
    public ApiResult<List<PickingDetailDTO.ListDTO>> listPickingDetail(@RequestBody @Valid PickingDetailDTO.SearchParamDTO dto) {
        List<PickingDetailDTO.ListDTO> list = transferApplicationService.listPickingDetail(dto);
        return success(list);
    }

    /**
     * 下推加工单-列表查询
     * @Author Luo_WG
     * @Date 2023/6/29 16:49
     * @param dto dto
     * @return java.util.List<com.erp.model.wms.dto.TransferApplicationDTO.generateMachineInfoView>
     **/
    @PostMapping("/viewGenerateMachineInfo")
    public ApiResult<List<TransferApplicationDTO.ViewGenerateMachineInfo>> viewGenerateMachineInfo(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<TransferApplicationDTO.ViewGenerateMachineInfo> viewGenerateMachineInfoList = transferApplicationService.viewGenerateMachineInfo(dto.getIds(), Boolean.FALSE, MathUtil.ZERO);
        return success(viewGenerateMachineInfoList);
    }

    /**
     * 下推加工单-保存
     * @Author Luo_WG
     * @Date 2023/6/29 19:18
     * @param validList
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.PickingDetailDTO.ListDTO>>
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "下推加工单")
    @PostMapping("/saveGenerateMachineInfo")
    public ApiResult<List<PickingDetailDTO.ListDTO>> saveGenerateMachineInfo(@RequestBody ValidList<TransferApplicationDTO.ViewGenerateMachineInfo> validList) {
        Boolean flag = transferApplicationService.saveGenerateMachineInfo(validList.getList());
        return flag == true ? success() : failure();
    }
}
