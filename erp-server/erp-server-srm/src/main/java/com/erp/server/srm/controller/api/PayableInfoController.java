package com.erp.server.srm.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.srm.dto.PayableInfoDTO;
import com.erp.model.srm.entity.PayableInfoEntity;
import com.erp.server.srm.service.PayableInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 
 *
 * @author will
 * @since 2025-09-25
 */
@Slf4j
@RestController
@LogSystemModule("")
@RequestMapping("/payableInfo")
public class PayableInfoController extends BaseController {

    @Resource
    private PayableInfoService payableInfoService;

    /**
    * 新增
    * @author will
    * @date:  2025-09-25
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated PayableInfoDTO.AddDTO dto) {
        return success(payableInfoService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2025-09-25
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "srm:payableInfo:update",
        serviceClass = PayableInfoService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated PayableInfoDTO.UpdateDTO dto) {
        payableInfoService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "srm:payableInfo:paging",
            tableAlias = ""
    )
    public ApiResult<List<PayableInfoDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(payableInfoService.tabList(dto));
    }

    /**
    * 列表查询
    * @author will
    * @date: 2025-09-25
    * @param dto
    * @return ApiResult<PagingVO<PayableInfoDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "srm:payableInfo:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<PayableInfoDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<PayableInfoDTO.PagingParamDTO> dto) {
        return success(payableInfoService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author will
    * @date:  2025-09-25
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated PayableInfoDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = payableInfoService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author will
    * @date:  2025-09-25
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "srm:payableInfo:updateAndSubmit",
            serviceClass = PayableInfoService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated PayableInfoDTO.UpdateDTO dto) {
        payableInfoService.updateAndSubmit(dto);
        return success();
    }


    /**
    * 审核
    * @author will
    * @date:  2025-09-25
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "srm:payableInfo:approve",
            serviceClass = PayableInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<PayableInfoEntity> list = payableInfoService.lambdaQuery().in(PayableInfoEntity::getId, ids).list();
		Map<String, PayableInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(PayableInfoEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = payableInfoService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("审核失败",e);
                PayableInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "不存在, 审核失败");
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
    * @author will
    * @date:  2025-09-25
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "srm:payableInfo:disApprove",
            serviceClass = PayableInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<PayableInfoEntity> list = payableInfoService.lambdaQuery().in(PayableInfoEntity::getId, ids).list();
		Map<String, PayableInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(PayableInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = payableInfoService.disApprove(id);
            }catch (Exception e){
                log.error("反审核失败",e);
                PayableInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "不存在, 反审核失败");
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
    * @author will
    * @date:  2025-09-25
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "srm:payableInfo:delete",
            serviceClass = PayableInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<PayableInfoEntity> list = payableInfoService.lambdaQuery().in(PayableInfoEntity::getId, ids).list();
		Map<String, PayableInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(PayableInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = payableInfoService.delete(id);
            }catch (Exception e){
                log.error("删除失败",e);
                PayableInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "不存在, 删除失败");
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
    * @author will
    * @date:  2025-09-25
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "srm:payableInfo:cancelProcess",
            serviceClass = PayableInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        // TODO 数据查询放入外层，处理结果统一更新或单条更新
        List<PayableInfoEntity> list = payableInfoService.lambdaQuery().in(PayableInfoEntity::getId, ids).list();
        Map<String, PayableInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(PayableInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = payableInfoService.cancelProcess(new ApproveDTO.CancelProcessDTO(id));
            }catch (Exception e){
                log.error("撤回流程失败",e);
                PayableInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "不存在, 撤回流程失败");
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
    * @author will
    * @date:  2025-09-25
    * @param id
    * @return ApiResult<PayableInfoDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "srm:payableInfo:view",
            serviceClass = PayableInfoService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<PayableInfoDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(payableInfoService.view(id));
    }


}
