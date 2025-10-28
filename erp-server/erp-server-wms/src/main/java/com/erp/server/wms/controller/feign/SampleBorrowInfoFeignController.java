package com.erp.server.wms.controller.feign;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.ClientTypeEnum;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.SampleBorrowInfoDTO;
import com.erp.model.wms.entity.SampleBorrowInfoEntity;
import com.erp.server.wms.query.SampleBorrowInfoQueryHandler;
import com.erp.server.wms.service.SampleBorrowInfoService;
import com.erp.server.wms.service.SampleScrapInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 样品借用单app端
 *
 * @author jack
 * @since 2025-09-11
 */
@Slf4j
@RestController
@LogSystemModule("样品借用单app端")
@RequestMapping("/feign/sampleBorrowInfo")
public class SampleBorrowInfoFeignController extends BaseController {

    @Resource
    private SampleBorrowInfoService sampleBorrowInfoService;

    /**
     * 新增
     * @author jack
     * @date:  2025-09-11
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "样品借用单app端新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SampleBorrowInfoDTO.AddDTO dto) {
        dto.setClientType(ClientTypeEnum.APP);
        return success(sampleBorrowInfoService.add(dto));
    }

    /**
     * 修改
     * @author jack
     * @date:  2025-09-11
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "样品借用单app端修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleBorrowInfo:update",
            serviceClass = SampleBorrowInfoService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SampleBorrowInfoDTO.UpdateDTO dto) {
        dto.setClientType(ClientTypeEnum.APP);
        sampleBorrowInfoService.update(dto);
        return success();
    }

    /**
     * APP端标签页列表
     * @author jack
     * @date: 2025-09-11
     * @param param 权限参数对象，用于控制数据访问权限
     * @return 标签页列表，包含待提交/不通过、审核中、待归还三个标签页的统计信息
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleBorrowInfo:paging",
            tableAlias = "sbi"
    )
    public ApiResult<List<SampleBorrowInfoDTO.TabListDTO>> tabListApp(@RequestBody PermissionsDTO param) {
        return success(sampleBorrowInfoService.tabListApp(param));
    }

    /**
     * APP端列表查询
     * @author jack
     * @date: 2025-09-11
     * @param dto
     * @return ApiResult<PagingVO<SampleBorrowInfoDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleBorrowInfo:paging",
            tableAlias = "sbi"
    )
    @WebAdvanceQuery(handler = SampleBorrowInfoQueryHandler.class)
    public ApiResult<PagingVO<SampleBorrowInfoDTO.ListDTO>> pagingApp(@RequestBody @Validated PagingDTO<SampleBorrowInfoDTO.PagingParamDTO> dto) {
        return success(sampleBorrowInfoService.pagingApp(dto));
    }

    /**
     * 新增并提交审核
     * @author jack
     * @date:  2025-09-11
     * @param dto
     * @return ApiResult<Void>
     */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated SampleBorrowInfoDTO.AddDTO dto) {
        dto.setClientType(ClientTypeEnum.APP);
        BaseResultDTO.AddDTO result = sampleBorrowInfoService.addAndSubmit(dto);
        return success(result);
    }

    /**
     * 修改并提交审核
     * @author jack
     * @date:  2025-09-11
     * @param dto
     * @return ApiResult<Void>
     */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleBorrowInfo:updateAndSubmit",
            serviceClass = SampleBorrowInfoService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated SampleBorrowInfoDTO.UpdateDTO dto) {
        dto.setClientType(ClientTypeEnum.APP);
        sampleBorrowInfoService.updateAndSubmit(dto);
        return success();
    }


    /**
     * 提交审核
     * @author jack
     * @date:  2025-09-11
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleBorrowInfo:submit",
            serviceClass = SampleBorrowInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "样品借用单app端提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SampleBorrowInfoEntity> list = sampleBorrowInfoService.lambdaQuery().in(SampleBorrowInfoEntity::getId, ids).list();
        Map<String, SampleBorrowInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleBorrowInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = sampleBorrowInfoService.submit(id,ClientTypeEnum.APP);
            }catch (Exception e){
                log.error("样品借用单app端 提交审核失败",e);
                SampleBorrowInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "样品借用单不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 审核
     * @author jack
     * @date:  2025-09-11
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleBorrowInfo:approve",
            serviceClass = SampleBorrowInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "样品借用单app端审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SampleBorrowInfoEntity> list = sampleBorrowInfoService.lambdaQuery().in(SampleBorrowInfoEntity::getId, ids).list();
        Map<String, SampleBorrowInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleBorrowInfoEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = sampleBorrowInfoService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()),ClientTypeEnum.APP);
            }catch (Exception e){
                log.error("样品借用单app端审核失败",e);
                SampleBorrowInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "样品借用单不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(approveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 反审核
     * @author jack
     * @date:  2025-09-11
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleBorrowInfo:disApprove",
            serviceClass = SampleBorrowInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "样品借用单app端反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SampleBorrowInfoEntity> list = sampleBorrowInfoService.lambdaQuery().in(SampleBorrowInfoEntity::getId, ids).list();
        Map<String, SampleBorrowInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleBorrowInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = sampleBorrowInfoService.disApprove(id,ClientTypeEnum.APP);
            }catch (Exception e){
                log.error("样品借用单app端反审核失败",e);
                SampleBorrowInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "样品借用单不存在, 反审核失败");
                    resultDTOS.add(disApproveResult);
                    continue;
                }
                disApproveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(disApproveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 删除
     * @author jack
     * @date:  2025-09-11
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleBorrowInfo:delete",
            serviceClass = SampleBorrowInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "样品借用单app端删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SampleBorrowInfoEntity> list = sampleBorrowInfoService.lambdaQuery().in(SampleBorrowInfoEntity::getId, ids).list();
        Map<String, SampleBorrowInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleBorrowInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = sampleBorrowInfoService.delete(id,ClientTypeEnum.APP);
            }catch (Exception e){
                log.error("样品借用单app端删除失败",e);
                SampleBorrowInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "样品借用单不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 作废
     * @author jack
     * @date:  2025-09-11
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleBorrowInfo:invalid",
            serviceClass = SampleScrapInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "样品借用单app端作废")
    public ApiResult<List<BatchResultDTO>> batchInvalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SampleBorrowInfoEntity> list = sampleBorrowInfoService.lambdaQuery().in(SampleBorrowInfoEntity::getId, ids).list();
        Map<String, SampleBorrowInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleBorrowInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = sampleBorrowInfoService.invalid(id,dto.getRemark(),ClientTypeEnum.APP);
            }catch (Exception e){
                log.error("样品借用单app端作废失败",e);
                SampleBorrowInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "样品借用单不存在, 作废失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 撤销
     * @author jack
     * @date:  2025-09-11
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleBorrowInfo:cancelProcess",
            serviceClass = SampleBorrowInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "样品借用单app端撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SampleBorrowInfoEntity> list = sampleBorrowInfoService.lambdaQuery().in(SampleBorrowInfoEntity::getId, ids).list();
        Map<String, SampleBorrowInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleBorrowInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = sampleBorrowInfoService.cancelProcess(new ApproveDTO.CancelProcessDTO(id),ClientTypeEnum.APP);
            }catch (Exception e){
                log.error("样品借用单app端撤回流程失败",e);
                SampleBorrowInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "样品借用单不存在, 撤回流程失败");
                    resultDTOS.add(cancelResult);
                    continue;
                }
                cancelResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(cancelResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * APP端详情
     * @author jack
     * @date:  2025-09-11
     * @param id
     * @return ApiResult<SampleBorrowInfoDTO.ViewDTO>>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<SampleBorrowInfoDTO.ViewDTO> viewApp(@RequestParam("id") String id) {
        return success(sampleBorrowInfoService.view(id));
    }

    /**
     * 归还单添加产品
     * @author jack
     * @date: 2025-09-11
     * @param dto
     * @return ApiResult<List<SampleBorrowInfoDTO.SkuAvailableQtyDTO>>
     */
    @PostMapping("/listSku")
    public ApiResult<PagingVO<SampleBorrowInfoDTO.SkuAvailableQtyDTO>> listSku(@RequestBody @Validated PagingDTO<SampleBorrowInfoDTO.SearchDTO> dto) {
        return success(sampleBorrowInfoService.listSku(dto));
    }

    /**
     * 借用单编号下拉
     * @author jack
     * @date: 2025-09-11
     * @return ApiResult<PagingVO<SampleLedgerDTO.SkuAvailableQtyDTO>>
     */
    @PostMapping("/drop/down")
    public ApiResult<List<SampleBorrowInfoDTO.DropDownDTO>> dropDown(@RequestBody SampleBorrowInfoDTO.SelectDTO dto) {
        return success(sampleBorrowInfoService.dropDown(dto));
    }


}
