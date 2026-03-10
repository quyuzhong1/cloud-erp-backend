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
import com.erp.model.wms.dto.SampleTransferInfoDTO;
import com.erp.model.wms.entity.SampleTransferInfoEntity;
import com.erp.server.wms.query.SampleTransferInfoQueryHandler;
import com.erp.server.wms.service.SampleTransferInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 样品转移单app端
 *
 * @author wuhaotian
 * @since 2025-10-28
 */
@Slf4j
@RestController
@LogSystemModule("样品转移单app端")
@RequestMapping("/feign/sampleTransferInfo")
public class SampleTransferInfoFeignController extends BaseController {

    @Resource
    private SampleTransferInfoService sampleTransferInfoService;

    /**
     * 新增
     * @author wuhaotian
     * @date: 2025-10-28
     * @param dto
     * @return ApiResult<BaseResultDTO.AddDTO>
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "样品转移单app端新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SampleTransferInfoDTO.AddDTO dto) {
        return success(sampleTransferInfoService.add(dto));
    }

    /**
     * 修改
     * @author wuhaotian
     * @date: 2025-10-28
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "样品转移单app端修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleTransferInfo:update",
            serviceClass = SampleTransferInfoService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SampleTransferInfoDTO.UpdateDTO dto) {
        sampleTransferInfoService.update(dto);
        return success();
    }

    /**
     * APP端标签页列表
     * @author wuhaotian
     * @date: 2025-10-28
     * @param param 权限参数对象，用于控制数据访问权限
     * @return 标签页列表
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleTransferInfo:paging",
            tableAlias = "sti"
    )
    public ApiResult<List<SampleTransferInfoDTO.TabListDTO>> tabListApp(@RequestBody PermissionsDTO param) {
        return success(sampleTransferInfoService.tabListApp(param));
    }

    /**
     * APP端列表查询
     * @author wuhaotian
     * @date: 2025-10-28
     * @param dto
     * @return ApiResult<PagingVO<SampleTransferInfoDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleTransferInfo:paging",
            tableAlias = "sti"
    )
    @WebAdvanceQuery(handler = SampleTransferInfoQueryHandler.class)
    public ApiResult<PagingVO<SampleTransferInfoDTO.ListDTO>> pagingApp(@RequestBody @Validated PagingDTO<SampleTransferInfoDTO.PagingParamDTO> dto) {
        return success(sampleTransferInfoService.pagingApp(dto));
    }

    /**
     * 新增并提交审核
     * @author wuhaotian
     * @date: 2025-10-28
     * @param dto
     * @return ApiResult<BaseResultDTO.AddDTO>
     */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated SampleTransferInfoDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = sampleTransferInfoService.addAndSubmit(dto);
        return success(result);
    }

    /**
     * 修改并提交审核
     * @author wuhaotian
     * @date: 2025-10-28
     * @param dto
     * @return ApiResult<Void>
     */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleTransferInfo:updateAndSubmit",
            serviceClass = SampleTransferInfoService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated SampleTransferInfoDTO.UpdateDTO dto) {
        sampleTransferInfoService.updateAndSubmit(dto);
        return success();
    }

    /**
     * 提交审核
     * @author wuhaotian
     * @date: 2025-10-28
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleTransferInfo:submit",
            serviceClass = SampleTransferInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "样品转移单app端提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SampleTransferInfoEntity> list = sampleTransferInfoService.lambdaQuery().in(SampleTransferInfoEntity::getId, ids).list();
        Map<String, SampleTransferInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleTransferInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = sampleTransferInfoService.submit(id, ClientTypeEnum.APP);
            }catch (Exception e){
                log.error("样品转移单app端 提交审核失败",e);
                SampleTransferInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "样品转移单不存在, 提交失败");
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
     * @date: 2025-10-28
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleTransferInfo:approve",
            serviceClass = SampleTransferInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "样品转移单app端审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SampleTransferInfoEntity> list = sampleTransferInfoService.lambdaQuery().in(SampleTransferInfoEntity::getId, ids).list();
        Map<String, SampleTransferInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleTransferInfoEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = sampleTransferInfoService.approve(new ApproveOneDTO(id, dto.getType(), dto.getComment()), ClientTypeEnum.APP);
            }catch (Exception e){
                log.error("样品转移单app端审核失败",e);
                SampleTransferInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "样品转移单不存在, 审核失败");
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
     * @date: 2025-10-28
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleTransferInfo:disApprove",
            serviceClass = SampleTransferInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "样品转移单app端反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SampleTransferInfoEntity> list = sampleTransferInfoService.lambdaQuery().in(SampleTransferInfoEntity::getId, ids).list();
        Map<String, SampleTransferInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleTransferInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = sampleTransferInfoService.disApprove(id, ClientTypeEnum.APP);
            }catch (Exception e){
                log.error("样品转移单app端反审核失败",e);
                SampleTransferInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "样品转移单不存在, 反审核失败");
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
     * @date: 2025-10-28
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleTransferInfo:delete",
            serviceClass = SampleTransferInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "样品转移单app端删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SampleTransferInfoEntity> list = sampleTransferInfoService.lambdaQuery().in(SampleTransferInfoEntity::getId, ids).list();
        Map<String, SampleTransferInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleTransferInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = sampleTransferInfoService.delete(id, ClientTypeEnum.APP);
            }catch (Exception e){
                log.error("样品转移单app端删除失败",e);
                SampleTransferInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "样品转移单不存在, 删除失败");
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
     * @date: 2025-10-28
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleTransferInfo:invalid",
            serviceClass = SampleTransferInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "样品转移单app端作废")
    public ApiResult<List<BatchResultDTO>> batchInvalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SampleTransferInfoEntity> list = sampleTransferInfoService.lambdaQuery().in(SampleTransferInfoEntity::getId, ids).list();
        Map<String, SampleTransferInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleTransferInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = sampleTransferInfoService.invalid(id, dto.getRemark());
            }catch (Exception e){
                log.error("样品转移单app端作废失败",e);
                SampleTransferInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, "样品转移单不存在, 作废失败");
                    resultDTOS.add(invalidResult);
                    continue;
                }
                invalidResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(invalidResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 撤销
     * @author wuhaotian
     * @date: 2025-10-28
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleTransferInfo:cancelProcess",
            serviceClass = SampleTransferInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "样品转移单app端撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SampleTransferInfoEntity> list = sampleTransferInfoService.lambdaQuery().in(SampleTransferInfoEntity::getId, ids).list();
        Map<String, SampleTransferInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleTransferInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = sampleTransferInfoService.cancelProcess(id);
            }catch (Exception e){
                log.error("样品转移单app端撤回流程失败",e);
                SampleTransferInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "样品转移单不存在, 撤回流程失败");
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
     * @author wuhaotian
     * @date: 2025-10-28
     * @param id
     * @return ApiResult<SampleTransferInfoDTO.ViewDTO>>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<SampleTransferInfoDTO.ViewDTO> viewApp(@RequestParam("id") String id) {
        return success(sampleTransferInfoService.view(id));
    }
}

