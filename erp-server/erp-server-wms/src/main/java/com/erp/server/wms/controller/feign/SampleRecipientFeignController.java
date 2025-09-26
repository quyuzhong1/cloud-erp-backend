package com.erp.server.wms.controller.feign;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
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
import com.erp.model.wms.dto.SampleRecipientDTO;
import com.erp.model.wms.entity.SampleRecipientEntity;
import com.erp.server.wms.query.SampleRecipientQueryHandler;
import com.erp.server.wms.service.SampleRecipientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 样品领用单app端
 *
 * @author wuhaotian
 * @since 2025-09-15
 */
@Slf4j
@RestController
@LogSystemModule("样品领用单app端")
@RequestMapping("/feign/sampleRecipient")
public class SampleRecipientFeignController extends BaseController {

    @Resource
    private SampleRecipientService sampleRecipientService;

    /**
     * 新增
     * @author wuhaotian
     * @date: 2025-09-15
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "样品领用单app端新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SampleRecipientDTO.AddDTO dto) {
        dto.setClientType(ClientTypeEnum.APP);
        return success(sampleRecipientService.add(dto));
    }

    /**
     * 修改
     * @author wuhaotian
     * @date: 2025-09-15
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "样品领用单app端修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleRecipient:update",
            serviceClass = SampleRecipientService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SampleRecipientDTO.UpdateDTO dto) {
        dto.setClientType(ClientTypeEnum.APP);
        sampleRecipientService.update(dto);
        return success();
    }

    /**
     * APP端标签页列表
     * @author wuhaotian
     * @date: 2025-09-15
     * @param param 权限参数对象，用于控制数据访问权限
     * @return 标签页列表，包含待提交/不通过、审核中、待出库、已出库四个标签页的统计信息
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleRecipient:paging",
            tableAlias = "sr"
    )
    public ApiResult<List<SampleRecipientDTO.TabListDTO>> tabListApp(@RequestBody PermissionsDTO param) {
        return success(sampleRecipientService.tabListApp(param));
    }

    /**
     * APP端列表查询
     * @author wuhaotian
     * @date: 2025-09-15
     * @param dto
     * @return ApiResult<PagingVO<SampleRecipientDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleRecipient:paging",
            tableAlias = "sr"
    )
    @WebAdvanceQuery(handler = SampleRecipientQueryHandler.class)
    public ApiResult<PagingVO<SampleRecipientDTO.ListDTO>> pagingApp(@RequestBody @Validated PagingDTO<SampleRecipientDTO.PagingParamDTO> dto) {
        return success(sampleRecipientService.pagingApp(dto));
    }

    /**
     * 新增并提交审核
     * @author wuhaotian
     * @date: 2025-09-15
     * @param dto
     * @return ApiResult<Void>
     */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated SampleRecipientDTO.AddDTO dto) {
        dto.setClientType(ClientTypeEnum.APP);
        BaseResultDTO.AddDTO result = sampleRecipientService.addAndSubmit(dto);
        return success(result);
    }

    /**
     * 修改并提交审核
     * @author wuhaotian
     * @date: 2025-09-15
     * @param dto
     * @return ApiResult<Void>
     */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleRecipient:updateAndSubmit",
            serviceClass = SampleRecipientService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated SampleRecipientDTO.UpdateDTO dto) {
        dto.setClientType(ClientTypeEnum.APP);
        sampleRecipientService.updateAndSubmit(dto);
        return success();
    }

    /**
     * 提交审核
     * @author wuhaotian
     * @date: 2025-09-15
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleRecipient:submit",
            serviceClass = SampleRecipientService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "样品领用单app端提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SampleRecipientEntity> list = sampleRecipientService.lambdaQuery().in(SampleRecipientEntity::getId, ids).list();
        Map<String, SampleRecipientEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleRecipientEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = sampleRecipientService.submit(id, ClientTypeEnum.APP);
            }catch (Exception e){
                log.error("样品领用单app端 提交审核失败",e);
                SampleRecipientEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "样品领用单不存在, 提交失败");
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
     * @author wuhaotian
     * @date: 2025-09-15
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleRecipient:approve",
            serviceClass = SampleRecipientService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "样品领用单app端审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SampleRecipientEntity> list = sampleRecipientService.lambdaQuery().in(SampleRecipientEntity::getId, ids).list();
        Map<String, SampleRecipientEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleRecipientEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = sampleRecipientService.approve(new ApproveOneDTO(id, dto.getType(), dto.getComment()), ClientTypeEnum.APP);
            }catch (Exception e){
                log.error("样品领用单app端审核失败",e);
                SampleRecipientEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "样品领用单不存在, 审核失败");
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
     * @author wuhaotian
     * @date: 2025-09-15
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleRecipient:disApprove",
            serviceClass = SampleRecipientService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "样品领用单app端反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SampleRecipientEntity> list = sampleRecipientService.lambdaQuery().in(SampleRecipientEntity::getId, ids).list();
        Map<String, SampleRecipientEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleRecipientEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = sampleRecipientService.disApprove(id, ClientTypeEnum.APP);
            }catch (Exception e){
                log.error("样品领用单app端反审核失败",e);
                SampleRecipientEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "样品领用单不存在, 反审核失败");
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
     * @author wuhaotian
     * @date: 2025-09-15
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleRecipient:delete",
            serviceClass = SampleRecipientService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "样品领用单app端删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SampleRecipientEntity> list = sampleRecipientService.lambdaQuery().in(SampleRecipientEntity::getId, ids).list();
        Map<String, SampleRecipientEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleRecipientEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = sampleRecipientService.delete(id, ClientTypeEnum.APP);
            }catch (Exception e){
                log.error("样品领用单app端删除失败",e);
                SampleRecipientEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "样品领用单不存在, 删除失败");
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
     * @author wuhaotian
     * @date: 2025-09-15
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleRecipient:invalid",
            serviceClass = SampleRecipientService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "样品领用单app端作废")
    public ApiResult<List<BatchResultDTO>> batchInvalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SampleRecipientEntity> list = sampleRecipientService.lambdaQuery().in(SampleRecipientEntity::getId, ids).list();
        Map<String, SampleRecipientEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleRecipientEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = sampleRecipientService.invalid(id, dto.getRemark(), ClientTypeEnum.APP);
            }catch (Exception e){
                log.error("样品领用单app端作废失败",e);
                SampleRecipientEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "样品领用单不存在, 作废失败");
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
     * @author wuhaotian
     * @date: 2025-09-15
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleRecipient:cancelProcess",
            serviceClass = SampleRecipientService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "样品领用单app端撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SampleRecipientEntity> list = sampleRecipientService.lambdaQuery().in(SampleRecipientEntity::getId, ids).list();
        Map<String, SampleRecipientEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleRecipientEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = sampleRecipientService.cancelProcess(id, ClientTypeEnum.APP);
            }catch (Exception e){
                log.error("样品领用单app端撤回流程失败",e);
                SampleRecipientEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "样品领用单不存在, 撤回流程失败");
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
     * 结束领用
     * @author wuhaotian
     * @date: 2025-09-15
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/finishRecipient")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleRecipient:finishRecipient",
            serviceClass = SampleRecipientService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.UPDATE, desc = "样品领用单app端结束领用")
    public ApiResult<List<BatchResultDTO>> batchFinishRecipient(@RequestBody @Validated SampleRecipientDTO.FinishRecipientDTO dto) {
        List<BatchResultDTO> resultDTOS = sampleRecipientService.finishRecipient(dto);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * APP端详情
     * @author wuhaotian
     * @date: 2025-09-15
     * @param id
     * @return ApiResult<SampleRecipientDTO.ViewDTO>>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<SampleRecipientDTO.ViewDTO> viewApp(@RequestParam("id") String id) {
        return success(sampleRecipientService.view(id));
    }

    /**
     * 获取SKU列表（用于选择商品）
     * @author wuhaotian
     * @date: 2025-09-15
     * @param dto
     * @return ApiResult<PagingVO<SampleRecipientDTO.SkuListResponseDTO>>
     */
    @PostMapping("/listSku")
    public ApiResult<PagingVO<SampleRecipientDTO.SkuListResponseDTO>> listSku(@RequestBody @Validated SampleRecipientDTO.SkuListQueryDTO dto) {
        return success(sampleRecipientService.getSkuList(dto));
    }

    /**
     * 获取SKU可领用库存
     * @author wuhaotian
     * @date: 2025-09-15
     * @param dto
     * @return ApiResult<List<SampleRecipientDTO.SkuAvailableStockDTO>>
     */
    @PostMapping("/skuAvailableStock")
    public ApiResult<List<SampleRecipientDTO.SkuAvailableStockDTO>> skuAvailableStock(@RequestBody @Validated SampleRecipientDTO.SkuAvailableStockQueryDTO dto) {
        return success(sampleRecipientService.querySkuAvailableStock(dto));
    }

    /**
     * 查询SKU成本
     * @author wuhaotian
     * @date: 2025-09-15
     * @param dto
     * @return ApiResult<List<SampleRecipientDTO.SkuDTO>>
     */
    @PostMapping("/skuCost")
    public ApiResult<List<SampleRecipientDTO.SkuDTO>> skuCost(@RequestBody @Validated SampleRecipientDTO.SkuCostQueryDTO dto) {
        return success(sampleRecipientService.querySkuCost(dto));
    }


}
