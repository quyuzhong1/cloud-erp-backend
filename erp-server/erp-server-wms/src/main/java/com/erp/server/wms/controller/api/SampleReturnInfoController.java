package com.erp.server.wms.controller.api;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.enums.ClientTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.SampleBorrowInfoDTO;
import com.erp.model.wms.dto.SampleReturnDetailDTO;
import com.erp.server.wms.query.SampleReturnInfoQueryHandler;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;
import com.erp.server.wms.service.SampleReturnInfoService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.SampleReturnInfoDTO;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.wms.entity.SampleReturnInfoEntity;

/**
 * 样品归还单
 *
 * @author jack
 * @since 2025-08-20
 */
@Slf4j
@RestController
@LogSystemModule("样品归还单")
@RequestMapping("/sampleReturnInfo")
public class SampleReturnInfoController extends BaseController {

    @Resource
    private SampleReturnInfoService sampleReturnInfoService;

    /**
    * 新增
    * @author jack
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "样品归还单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SampleReturnInfoDTO.AddDTO dto) {
        return success(sampleReturnInfoService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "样品归还单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:sampleReturnInfo:update",
        serviceClass = SampleReturnInfoService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SampleReturnInfoDTO.UpdateDTO dto) {
        sampleReturnInfoService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleReturnInfo:paging",
            tableAlias = "sri"
    )
    public ApiResult<List<SampleReturnInfoDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(sampleReturnInfoService.tabList(dto));
    }

    /**
    * 列表查询
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return ApiResult<PagingVO<SampleReturnInfoDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleReturnInfo:paging",
            tableAlias = "sri"
    )
    @WebAdvanceQuery(handler = SampleReturnInfoQueryHandler.class )
    public ApiResult<PagingVO<SampleReturnInfoDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SampleReturnInfoDTO.PagingParamDTO> dto) {
        return success(sampleReturnInfoService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author jack
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated SampleReturnInfoDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = sampleReturnInfoService.addAndSubmit(dto);
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
            menuCode = "wms:sampleReturnInfo:update",
            serviceClass = SampleReturnInfoService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated SampleReturnInfoDTO.UpdateDTO dto) {
        sampleReturnInfoService.updateAndSubmit(dto);
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
            menuCode = "wms:sampleReturnInfo:submit",
            serviceClass = SampleReturnInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "样品归还单提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<SampleReturnInfoEntity> list = sampleReturnInfoService.lambdaQuery().in(SampleReturnInfoEntity::getId, ids).list();
		Map<String, SampleReturnInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleReturnInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = sampleReturnInfoService.submit(id, ClientTypeEnum.WEB);
            }catch (Exception e){
                log.error("样品归还单 提交审核失败",e);
                SampleReturnInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "样品归还单不存在, 提交失败");
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
            menuCode = "wms:sampleReturnInfo:approve",
            serviceClass = SampleReturnInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "样品归还单审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<SampleReturnInfoEntity> list = sampleReturnInfoService.lambdaQuery().in(SampleReturnInfoEntity::getId, ids).list();
		Map<String, SampleReturnInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleReturnInfoEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = sampleReturnInfoService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()), ClientTypeEnum.WEB);
            }catch (Exception e){
                log.error("样品归还单审核失败",e);
                SampleReturnInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "样品归还单不存在, 审核失败");
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
            menuCode = "wms:sampleReturnInfo:disApprove",
            serviceClass = SampleReturnInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "样品归还单反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<SampleReturnInfoEntity> list = sampleReturnInfoService.lambdaQuery().in(SampleReturnInfoEntity::getId, ids).list();
		Map<String, SampleReturnInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleReturnInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = sampleReturnInfoService.disApprove(id, ClientTypeEnum.WEB);
            }catch (Exception e){
                log.error("样品归还单反审核失败",e);
                SampleReturnInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "样品归还单不存在, 反审核失败");
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
            menuCode = "wms:sampleReturnInfo:delete",
            serviceClass = SampleReturnInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "样品归还单删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<SampleReturnInfoEntity> list = sampleReturnInfoService.lambdaQuery().in(SampleReturnInfoEntity::getId, ids).list();
		Map<String, SampleReturnInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleReturnInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = sampleReturnInfoService.delete(id, ClientTypeEnum.WEB);
            }catch (Exception e){
                log.error("样品归还单删除失败",e);
                SampleReturnInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "样品归还单不存在, 删除失败");
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
            menuCode = "wms:sampleReturnInfo:invalid",
            serviceClass = SampleReturnInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "样品归还单作废")
    public ApiResult<List<BatchResultDTO>> batchInvalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<SampleReturnInfoEntity> list = sampleReturnInfoService.lambdaQuery().in(SampleReturnInfoEntity::getId, ids).list();
		Map<String, SampleReturnInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleReturnInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = sampleReturnInfoService.invalid(id,dto.getRemark(), ClientTypeEnum.WEB);
            }catch (Exception e){
                log.error("样品归还单作废失败",e);
                SampleReturnInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "样品归还单不存在, 作废失败");
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
            menuCode = "wms:sampleReturnInfo:cancelProcess",
            serviceClass = SampleReturnInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "样品归还单撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SampleReturnInfoEntity> list = sampleReturnInfoService.lambdaQuery().in(SampleReturnInfoEntity::getId, ids).list();
        Map<String, SampleReturnInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleReturnInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = sampleReturnInfoService.cancelProcess(id, ClientTypeEnum.WEB);
            }catch (Exception e){
                log.error("样品归还单撤回流程失败",e);
                SampleReturnInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "样品归还单不存在, 撤回流程失败");
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
    * @return ApiResult<SampleReturnInfoDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<SampleReturnInfoDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(sampleReturnInfoService.view(id));
    }

    /**
    * 导出Excel数据
    * @author jack
    * @date:  2025-08-20
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleReturnInfo:export",
            tableAlias = "sri"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "样品归还单导出Excel数据")
    @WebAdvanceQuery(handler = SampleReturnInfoQueryHandler.class )
    public ApiResult<Object> exportList(@RequestBody @Validated SampleReturnInfoDTO.PagingParamDTO dto, HttpServletResponse response) {
        sampleReturnInfoService.exportList(dto, response);
        return success();
    }

    /**
     * 生成样品归还单
     * <p>
     * 该接口接收一个样品借用信息列表，按照 sourceId 和 returnDate 进行分组，
     * 每组生成一条样品归还单记录，并调用服务进行保存。
     * 最终返回每条记录的处理结果（成功或失败）。
     *
     * @param list 样品归还信息列表，不能为空且每个元素需通过校验规则
     * @return ApiResult<List<BatchResultDTO>> 批量处理结果：
     *         - 如果所有记录都处理成功，则返回成功状态；
     *         - 如果存在处理失败的记录，则返回失败状态；
     *         - 每个 BatchResultDTO 表示一条记录的处理结果
     * @author jack
     * @date:  2025-08-27
     */
    @PostMapping("/generateSampleReturn")
    public ApiResult<List<BatchResultDTO>> generateSampleReturn(@RequestBody @Validated ValidList<SampleBorrowInfoDTO.SampleReturnView> list) {
        List<BatchResultDTO> resultDTOS = sampleReturnInfoService.generateSampleReturn(list);
        // 判断是否全部成功，决定返回成功还是部分失败
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

}
