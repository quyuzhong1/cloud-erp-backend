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
import com.erp.server.oms.service.SoReceiptService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.SoReceiptDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.oms.entity.SoReceiptEntity;

/**
 * 收款单
 *
 * @author lrp
 * @since 2025-08-28
 */
@Slf4j
@RestController
@LogSystemModule("收款单")
@RequestMapping("/soReceipt")
public class SoReceiptController extends BaseController {

    @Resource
    private SoReceiptService soReceiptService;

    /**
    * 新增
    * @author lrp
    * @date:  2025-08-28
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "收款单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SoReceiptDTO.AddDTO dto) {
        return success(soReceiptService.add(dto));
    }

    /**
    * 修改
    * @author lrp
    * @date:  2025-08-28
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "收款单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:soReceipt:update",
        serviceClass = SoReceiptService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SoReceiptDTO.UpdateDTO dto) {
        soReceiptService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:soReceipt:paging",
            tableAlias = ""
    )
    public ApiResult<List<SoReceiptDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(soReceiptService.tabList(dto));
    }

    /**
    * 列表查询
    * @author lrp
    * @date: 2025-08-28
    * @param dto
    * @return ApiResult<PagingVO<SoReceiptDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:soReceipt:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<SoReceiptDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SoReceiptDTO.PagingParamDTO> dto) {
        return success(soReceiptService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author lrp
    * @date:  2025-08-28
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated SoReceiptDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = soReceiptService.addAndSubmit(dto);
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
            menuCode = "oms:soReceipt:updateAndSubmit",
            serviceClass = SoReceiptService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated SoReceiptDTO.UpdateDTO dto) {
        soReceiptService.updateAndSubmit(dto);
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
            menuCode = "oms:soReceipt:submit",
            serviceClass = SoReceiptService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "收款单提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SoReceiptEntity> list = soReceiptService.lambdaQuery().in(SoReceiptEntity::getId, ids).list();
		Map<String, SoReceiptEntity> idEntityMap = list.stream().collect(Collectors.toMap(SoReceiptEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = soReceiptService.submit(id);
            }catch (Exception e){
                log.error("收款单 提交审核失败",e);
                SoReceiptEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "收款单不存在, 提交失败");
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
            menuCode = "oms:soReceipt:approve",
            serviceClass = SoReceiptService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "收款单审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SoReceiptEntity> list = soReceiptService.lambdaQuery().in(SoReceiptEntity::getId, ids).list();
		Map<String, SoReceiptEntity> idEntityMap = list.stream().collect(Collectors.toMap(SoReceiptEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = soReceiptService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("收款单审核失败",e);
                SoReceiptEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "收款单不存在, 审核失败");
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
            menuCode = "oms:soReceipt:disApprove",
            serviceClass = SoReceiptService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "收款单反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SoReceiptEntity> list = soReceiptService.lambdaQuery().in(SoReceiptEntity::getId, ids).list();
		Map<String, SoReceiptEntity> idEntityMap = list.stream().collect(Collectors.toMap(SoReceiptEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = soReceiptService.disApprove(id);
            }catch (Exception e){
                log.error("收款单反审核失败",e);
                SoReceiptEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "收款单不存在, 反审核失败");
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
            menuCode = "oms:soReceipt:delete",
            serviceClass = SoReceiptService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "收款单删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SoReceiptEntity> list = soReceiptService.lambdaQuery().in(SoReceiptEntity::getId, ids).list();
		Map<String, SoReceiptEntity> idEntityMap = list.stream().collect(Collectors.toMap(SoReceiptEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = soReceiptService.delete(id);
            }catch (Exception e){
                log.error("收款单删除失败",e);
                SoReceiptEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "收款单不存在, 删除失败");
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
            menuCode = "oms:soReceipt:cancelProcess",
            serviceClass = SoReceiptService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "收款单撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        // TODO 数据查询放入外层，处理结果统一更新或单条更新
        List<SoReceiptEntity> list = soReceiptService.lambdaQuery().in(SoReceiptEntity::getId, ids).list();
        Map<String, SoReceiptEntity> idEntityMap = list.stream().collect(Collectors.toMap(SoReceiptEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = soReceiptService.cancelProcess(id);
            }catch (Exception e){
                log.error("收款单撤回流程失败",e);
                SoReceiptEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "收款单不存在, 撤回流程失败");
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
    * @return ApiResult<SoReceiptDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soReceipt:view",
            serviceClass = SoReceiptService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<SoReceiptDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(soReceiptService.view(id));
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
            menuCode = "oms:soReceipt:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "收款单导出Excel数据")
    public void exportList(@RequestBody @Validated SoReceiptDTO.ExportDTO dto, HttpServletResponse response) {
        soReceiptService.exportList(dto, response);
    }

    /**
     * 通过销售订单查订单收款信息
     */
    @PostMapping("/listSoReceiptBySoCode")
    public List<SoReceiptDTO.SoInfoAndReceiptDTO> listSoReceiptBySoCode(@RequestBody @Validated SoReceiptDTO.SoSearchDTO dto) {
       return soReceiptService.listSoReceiptBySoCode(dto);
    }


}
