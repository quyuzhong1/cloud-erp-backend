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
import com.erp.model.wms.dto.SampleScrapInfoDTO;
import com.erp.model.wms.entity.SampleScrapInfoEntity;
import com.erp.server.wms.query.SampleScrapInfoQueryHandler;
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
 * 样品报废单app端
 *
 * @author jack
 * @since 2025-08-20
 */
@Slf4j
@RestController
@LogSystemModule("样品报废单app端app端")
@RequestMapping("/feign/sampleScrapInfo")
public class SampleScrapInfoFeignController extends BaseController {

    @Resource
    private SampleScrapInfoService sampleScrapInfoService;

    /**
    * 新增
    * @author jack
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "样品报废单app端新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SampleScrapInfoDTO.AddDTO dto) {
        dto.setClientType(ClientTypeEnum.APP);
        return success(sampleScrapInfoService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "样品报废单app端修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:sampleScrapInfo:update",
        serviceClass = SampleScrapInfoService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SampleScrapInfoDTO.UpdateDTO dto) {
        dto.setClientType(ClientTypeEnum.APP);
        sampleScrapInfoService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleScrapInfo:paging",
            tableAlias = "ssi"
    )
    public ApiResult<List<SampleScrapInfoDTO.TabListDTO>> tabListApp(@RequestBody PermissionsDTO dto) {
       return success(sampleScrapInfoService.tabListApp(dto));
    }

    /**
    * 列表查询
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return ApiResult<PagingVO<SampleScrapInfoDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleScrapInfo:paging",
            tableAlias = "ssi"
    )
    @WebAdvanceQuery(handler = SampleScrapInfoQueryHandler.class)
    public ApiResult<PagingVO<SampleScrapInfoDTO.ListDTO>> pagingApp(@RequestBody @Validated PagingDTO<SampleScrapInfoDTO.PagingParamDTO> dto) {
        return success(sampleScrapInfoService.pagingApp(dto));
    }

    /**
    * 新增并提交审核
    * @author jack
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated SampleScrapInfoDTO.AddDTO dto) {
        dto.setClientType(ClientTypeEnum.APP);
        BaseResultDTO.AddDTO result = sampleScrapInfoService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author jack
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleScrapInfo:updateAndSubmit",
            serviceClass = SampleScrapInfoService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated SampleScrapInfoDTO.UpdateDTO dto) {
        dto.setClientType(ClientTypeEnum.APP);
        sampleScrapInfoService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author jack
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleScrapInfo:submit",
            serviceClass = SampleScrapInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "样品报废单app端提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<SampleScrapInfoEntity> list = sampleScrapInfoService.lambdaQuery().in(SampleScrapInfoEntity::getId, ids).list();
		Map<String, SampleScrapInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleScrapInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = sampleScrapInfoService.submit(id,ClientTypeEnum.APP);
            }catch (Exception e){
                log.error("样品报废单app端 提交审核失败",e);
                SampleScrapInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "样品报废单不存在, 提交失败");
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
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleScrapInfo:approve",
            serviceClass = SampleScrapInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "样品报废单app端审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<SampleScrapInfoEntity> list = sampleScrapInfoService.lambdaQuery().in(SampleScrapInfoEntity::getId, ids).list();
		Map<String, SampleScrapInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleScrapInfoEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = sampleScrapInfoService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()),ClientTypeEnum.APP);
            }catch (Exception e){
                log.error("样品报废单app端审核失败",e);
                SampleScrapInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "样品报废单不存在, 审核失败");
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
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleScrapInfo:disApprove",
            serviceClass = SampleScrapInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "样品报废单app端反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<SampleScrapInfoEntity> list = sampleScrapInfoService.lambdaQuery().in(SampleScrapInfoEntity::getId, ids).list();
		Map<String, SampleScrapInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleScrapInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = sampleScrapInfoService.disApprove(id,ClientTypeEnum.APP);
            }catch (Exception e){
                log.error("样品报废单app端反审核失败",e);
                SampleScrapInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "样品报废单不存在, 反审核失败");
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
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleScrapInfo:delete",
            serviceClass = SampleScrapInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "样品报废单app端删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<SampleScrapInfoEntity> list = sampleScrapInfoService.lambdaQuery().in(SampleScrapInfoEntity::getId, ids).list();
		Map<String, SampleScrapInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleScrapInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = sampleScrapInfoService.delete(id,ClientTypeEnum.APP);
            }catch (Exception e){
                log.error("样品报废单app端删除失败",e);
                SampleScrapInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "样品报废单不存在, 删除失败");
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
     * @date:  2025-08-20
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleScrapInfo:invalid",
            serviceClass = SampleScrapInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "样品报废单app端作废")
    public ApiResult<List<BatchResultDTO>> batchInvalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SampleScrapInfoEntity> list = sampleScrapInfoService.lambdaQuery().in(SampleScrapInfoEntity::getId, ids).list();
        Map<String, SampleScrapInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleScrapInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = sampleScrapInfoService.invalid(id,dto.getRemark(),ClientTypeEnum.APP);
            }catch (Exception e){
                log.error("样品报废单app端作废失败",e);
                SampleScrapInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "样品报废单不存在, 作废失败");
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
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleScrapInfo:cancelProcess",
            serviceClass = SampleScrapInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "样品报废单app端撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SampleScrapInfoEntity> list = sampleScrapInfoService.lambdaQuery().in(SampleScrapInfoEntity::getId, ids).list();
        Map<String, SampleScrapInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleScrapInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = sampleScrapInfoService.cancelProcess(id,ClientTypeEnum.APP);
            }catch (Exception e){
                log.error("样品报废单app端撤回流程失败",e);
                SampleScrapInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "样品报废单不存在, 撤回流程失败");
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
    * 详情
    * @author jack
    * @date:  2025-08-20
    * @param id
    * @return ApiResult<SampleScrapInfoDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<SampleScrapInfoDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(sampleScrapInfoService.view(id));
    }
}
