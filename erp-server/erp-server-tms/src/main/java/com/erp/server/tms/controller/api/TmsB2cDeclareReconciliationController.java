package com.erp.server.tms.controller.api;


import cn.hutool.core.util.ObjectUtil;
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
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationDTO;
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationDetailDTO;
import com.erp.model.tms.entity.TmsB2cDeclareReconciliationEntity;
import com.erp.server.tms.query.TmsB2cDeclareReconciliationQueryHandler;
import com.erp.server.tms.service.TmsB2cDeclareReconciliationDetailService;
import com.erp.server.tms.service.TmsB2cDeclareReconciliationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * b2c报关对账单
 *
 * @author will
 * @since 2024-03-19
 */
@Slf4j
@RestController
@LogSystemModule("b2c报关对账单")
@RequestMapping("/tmsB2cDeclareReconciliation")
public class TmsB2cDeclareReconciliationController extends BaseController {

    @Resource
    private TmsB2cDeclareReconciliationService tmsB2cDeclareReconciliationService;

    @Resource
    private TmsB2cDeclareReconciliationDetailService tmsB2cDeclareReconciliationDetailService;


    /**
    * 修改
    * @author will
    * @date:  2024-03-19
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "b2c报关对账单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:tmsB2cDeclareReconciliation:update",
        serviceClass = TmsB2cDeclareReconciliationService.class,
        keyIdName = "id")
    public ApiResult<Object> update(@RequestBody @Validated TmsB2cDeclareReconciliationDTO.UpdateDTO dto) {
        tmsB2cDeclareReconciliationService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsB2cDeclareReconciliation:paging",
            tableAlias = "tbdr"
    )
    public ApiResult<List<TmsB2cDeclareReconciliationDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(tmsB2cDeclareReconciliationService.tabList(dto));
    }

    /**
    * 列表查询
    * @author will
    * @date: 2024-03-19
    * @param dto
    * @return ApiResult<PagingVO<TmsB2cDeclareReconciliationDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsB2cDeclareReconciliation:paging",
            tableAlias = "tbdr"
    )
    @WebAdvanceQuery(handler = TmsB2cDeclareReconciliationQueryHandler.class)
    public ApiResult<PagingVO<TmsB2cDeclareReconciliationDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<TmsB2cDeclareReconciliationDTO.PagingParamDTO> dto) {
        return success(tmsB2cDeclareReconciliationService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author will
    * @date:  2024-03-19
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交审核")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated TmsB2cDeclareReconciliationDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = tmsB2cDeclareReconciliationService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author will
    * @date:  2024-03-19
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsB2cDeclareReconciliation:updateAndSubmit",
            serviceClass = TmsB2cDeclareReconciliationService.class,
            keyIdName = "id")
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交审核")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated TmsB2cDeclareReconciliationDTO.UpdateDTO dto) {
        tmsB2cDeclareReconciliationService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author will
    * @date:  2024-03-19
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsB2cDeclareReconciliation:submit",
            serviceClass = TmsB2cDeclareReconciliationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "b2c报关对账单提交审核")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = tmsB2cDeclareReconciliationService.submit(id);
            }catch (Exception e){
                log.error("b2c报关对账单 提交审核失败",e);
                TmsB2cDeclareReconciliationEntity entity = tmsB2cDeclareReconciliationService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "b2c报关对账单不存在, 提交失败");
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
     * 修改支付状态
     * @author will
     * @date:  2024-03-19
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/updatePayStatus")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
    tableField = "create_user_id",
    menuCode = "tms:tmsB2cDeclareReconciliation:updatePayStatus",
    serviceClass = TmsB2cDeclareReconciliationService.class,
    keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "b2c报关对账单提交审核")
    public ApiResult<List<BatchResultDTO>> updatePayStatus(@RequestBody @Validated TmsB2cDeclareReconciliationDTO.PayStatusUpdateDTO dto) {
    	List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
    	for (String id : dto.getIds()) {
    		BatchResultDTO submit;
    		try {
    			submit = tmsB2cDeclareReconciliationService.updatePayStatus(id , dto.getPayStatus() , dto.getPayTime());
    		}catch (Exception e){
    			log.error("b2c报关对账单不存在, 不允许修改支付状态",e);
    			TmsB2cDeclareReconciliationEntity entity = tmsB2cDeclareReconciliationService.getById(id);
    			if (ObjectUtil.isEmpty(entity)) {
    				submit = BatchResultDTO.fail(id, id, "b2c报关对账单不存在, 不允许修改支付状态");
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
    * @author will
    * @date:  2024-03-19
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsB2cDeclareReconciliation:approve",
            serviceClass = TmsB2cDeclareReconciliationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "b2c报关对账单审核")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = tmsB2cDeclareReconciliationService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("b2c报关对账单审核失败",e);
                TmsB2cDeclareReconciliationEntity entity = tmsB2cDeclareReconciliationService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "b2c报关对账单不存在, 审核失败");
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
    * @date:  2024-03-19
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsB2cDeclareReconciliation:disApprove",
            serviceClass = TmsB2cDeclareReconciliationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "b2c报关对账单反审核")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = tmsB2cDeclareReconciliationService.disApprove(id);
            }catch (Exception e){
                log.error("b2c报关对账单反审核失败",e);
                TmsB2cDeclareReconciliationEntity entity = tmsB2cDeclareReconciliationService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "b2c报关对账单不存在, 反审核失败");
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
    * @date:  2024-03-19
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsB2cDeclareReconciliation:delete",
            serviceClass = TmsB2cDeclareReconciliationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "b2c报关对账单删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = tmsB2cDeclareReconciliationService.delete(id);
            }catch (Exception e){
                log.error("b2c报关对账单删除失败",e);
                TmsB2cDeclareReconciliationEntity entity = tmsB2cDeclareReconciliationService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "b2c报关对账单不存在, 删除失败");
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
    * @date:  2024-03-19
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsB2cDeclareReconciliation:cancelProcess",
            serviceClass = TmsB2cDeclareReconciliationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "b2c报关对账单撤销")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = tmsB2cDeclareReconciliationService.cancelProcess(id);
            }catch (Exception e){
                log.error("b2c报关对账单撤回流程失败",e);
                TmsB2cDeclareReconciliationEntity entity = tmsB2cDeclareReconciliationService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "b2c报关对账单不存在, 撤回流程失败");
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
    * @date:  2024-03-19
    * @param id
    * @return ApiResult<TmsB2cDeclareReconciliationDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsB2cDeclareReconciliation:view",
            serviceClass = TmsB2cDeclareReconciliationService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<TmsB2cDeclareReconciliationDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(tmsB2cDeclareReconciliationService.view(id));
    }

    /**
    * 导出Excel数据
    * @author will
    * @date:  2024-03-19
    * @param dto
    * @return
    */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "b2c报关对账单导出Excel数据")
    public ApiResult<Boolean> exportList(@RequestBody @Validated TmsB2cDeclareReconciliationDTO.ExportDTO dto) {
        tmsB2cDeclareReconciliationService.exportList(dto);
        return success(true);
    }


    /**
     * 导出明细Excel数据
     * @author Will
     * @date: 2024/3/26 9:56
     * @param dto
     */
    @PostMapping("/exportDetail")
    @LogAction(value = LogActionEnum.EXPORT, desc = "b2c报关对账单明细导出Excel数据")
    public ApiResult<Boolean> exportDetailList(@RequestBody @Validated TmsB2cDeclareReconciliationDetailDTO.ExportDTO dto) {
        tmsB2cDeclareReconciliationDetailService.exportDetailList(dto);
        return success(true);
    }

}
