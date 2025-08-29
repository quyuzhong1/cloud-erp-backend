package com.erp.server.oms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.erp.server.oms.service.CustomerCreditApplyService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.CustomerCreditApplyDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.oms.entity.CustomerCreditApplyEntity;

/**
 * 客户授信
 *
 * @author lrp
 * @since 2025-08-28
 */
@Slf4j
@RestController
@LogSystemModule("客户授信")
@RequestMapping("/customerCreditApply")
public class CustomerCreditApplyController extends BaseController {

    @Resource
    private CustomerCreditApplyService customerCreditApplyService;

    /**
    * 新增
    * @author lrp
    * @date:  2025-08-28
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "客户授信新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CustomerCreditApplyDTO.AddDTO dto) {
        return success(customerCreditApplyService.add(dto));
    }

    /**
    * 修改
    * @author lrp
    * @date:  2025-08-28
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "客户授信修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:customerCreditApply:update",
        serviceClass = CustomerCreditApplyService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CustomerCreditApplyDTO.UpdateDTO dto) {
        customerCreditApplyService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:customerCreditApply:paging",
            tableAlias = ""
    )
    public ApiResult<List<CustomerCreditApplyDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(customerCreditApplyService.tabList(dto));
    }

    /**
    * 列表查询
    * @author lrp
    * @date: 2025-08-28
    * @param dto
    * @return ApiResult<PagingVO<CustomerCreditApplyDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:customerCreditApply:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<CustomerCreditApplyDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CustomerCreditApplyDTO.PagingParamDTO> dto) {
        return success(customerCreditApplyService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author lrp
    * @date:  2025-08-28
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated CustomerCreditApplyDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = customerCreditApplyService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author lrp
    * @date:  2025-08-28
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:customerCreditApply:updateAndSubmit",
            serviceClass = CustomerCreditApplyService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated CustomerCreditApplyDTO.UpdateDTO dto) {
        customerCreditApplyService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author lrp
    * @date:  2025-08-28
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:customerCreditApply:submit",
            serviceClass = CustomerCreditApplyService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "客户授信提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<CustomerCreditApplyEntity> list = customerCreditApplyService.lambdaQuery().in(CustomerCreditApplyEntity::getId, ids).list();
		Map<String, CustomerCreditApplyEntity> idEntityMap = list.stream().collect(Collectors.toMap(CustomerCreditApplyEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = customerCreditApplyService.submit(id);
            }catch (Exception e){
                log.error("客户授信 提交审核失败",e);
                CustomerCreditApplyEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "客户授信不存在, 提交失败");
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
    * @author lrp
    * @date:  2025-08-28
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:customerCreditApply:approve",
            serviceClass = CustomerCreditApplyService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "客户授信审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<CustomerCreditApplyEntity> list = customerCreditApplyService.lambdaQuery().in(CustomerCreditApplyEntity::getId, ids).list();
		Map<String, CustomerCreditApplyEntity> idEntityMap = list.stream().collect(Collectors.toMap(CustomerCreditApplyEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = customerCreditApplyService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("客户授信审核失败",e);
                CustomerCreditApplyEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "客户授信不存在, 审核失败");
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
    * @author lrp
    * @date:  2025-08-28
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:customerCreditApply:disApprove",
            serviceClass = CustomerCreditApplyService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "客户授信反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<CustomerCreditApplyEntity> list = customerCreditApplyService.lambdaQuery().in(CustomerCreditApplyEntity::getId, ids).list();
		Map<String, CustomerCreditApplyEntity> idEntityMap = list.stream().collect(Collectors.toMap(CustomerCreditApplyEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = customerCreditApplyService.disApprove(id);
            }catch (Exception e){
                log.error("客户授信反审核失败",e);
                CustomerCreditApplyEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "客户授信不存在, 反审核失败");
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
    * @author lrp
    * @date:  2025-08-28
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:customerCreditApply:delete",
            serviceClass = CustomerCreditApplyService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "客户授信删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<CustomerCreditApplyEntity> list = customerCreditApplyService.lambdaQuery().in(CustomerCreditApplyEntity::getId, ids).list();
		Map<String, CustomerCreditApplyEntity> idEntityMap = list.stream().collect(Collectors.toMap(CustomerCreditApplyEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = customerCreditApplyService.delete(id);
            }catch (Exception e){
                log.error("客户授信删除失败",e);
                CustomerCreditApplyEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "客户授信不存在, 删除失败");
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
    * @author lrp
    * @date:  2025-08-28
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:customerCreditApply:cancelProcess",
            serviceClass = CustomerCreditApplyService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "客户授信撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        // TODO 数据查询放入外层，处理结果统一更新或单条更新
        List<CustomerCreditApplyEntity> list = customerCreditApplyService.lambdaQuery().in(CustomerCreditApplyEntity::getId, ids).list();
        Map<String, CustomerCreditApplyEntity> idEntityMap = list.stream().collect(Collectors.toMap(CustomerCreditApplyEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = customerCreditApplyService.cancelProcess(id);
            }catch (Exception e){
                log.error("客户授信撤回流程失败",e);
                CustomerCreditApplyEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "客户授信不存在, 撤回流程失败");
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
    * @author lrp
    * @date:  2025-08-28
    * @param id
    * @return ApiResult<CustomerCreditApplyDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:customerCreditApply:view",
            serviceClass = CustomerCreditApplyService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<CustomerCreditApplyDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(customerCreditApplyService.view(id));
    }

    /**
    * 导出Excel数据
    * @author lrp
    * @date:  2025-08-28
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:customerCreditApply:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "客户授信导出Excel数据")
    public void exportList(@RequestBody @Validated CustomerCreditApplyDTO.ExportDTO dto, HttpServletResponse response) {
        customerCreditApplyService.exportList(dto, response);
    }


}
