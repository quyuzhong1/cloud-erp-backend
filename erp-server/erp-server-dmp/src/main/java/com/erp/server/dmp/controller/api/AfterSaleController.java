package com.erp.server.dmp.controller.api;


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
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.dmp.dto.AfterSaleProgressDTO;
import com.erp.model.dmp.entity.AfterSaleEntity;
import com.erp.server.dmp.query.AfterSaleQueryHandler;
import com.erp.server.dmp.service.AfterSaleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 售后申请表
 *
 * @author jack
 * @since 2025-04-06
 */
@Slf4j
@RestController
@LogSystemModule("售后申请表")
@RequestMapping("/afterSale")
public class AfterSaleController extends BaseController {

    @Resource
    private AfterSaleService afterSaleService;


    /**
    * 新增
    * @author jack
    * @date:  2025-04-06
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "售后申请表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AfterSaleDTO.AddDTO dto) {
        dto.setType("selfAdd");
        return success(afterSaleService.addAndSubmit(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-04-06
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "售后申请表修改")
//        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//        tableField = "create_user_id",
//        menuCode = "dmp:afterSale:update",
//        serviceClass = AfterSaleService.class,
//        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated AfterSaleDTO.UpdateDTO dto) {
        afterSaleService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "create_user_id",
//            menuCode = "dmp:afterSale:paging",
//            tableAlias = "afs"
//    )
    public ApiResult<List<AfterSaleDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(afterSaleService.tabList(dto));
    }

    /**
    * 列表查询
    * @author jack
    * @date: 2025-04-06
    * @param dto
    * @return ApiResult<PagingVO<AfterSaleDTO.ListDTO>>
    */
    @PostMapping("/paging")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "create_user_id",
//            menuCode = "dmp:afterSale:paging",
//            tableAlias = "afs"
//    )
    @WebAdvanceQuery(handler = AfterSaleQueryHandler.class)
    public ApiResult<PagingVO<AfterSaleDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<AfterSaleDTO.PagingParamDTO> dto) {
        return success(afterSaleService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author jack
    * @date:  2025-04-06
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交审核")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated AfterSaleDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = afterSaleService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author jack
    * @date:  2025-04-06
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
//    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//            tableField = "create_user_id",
//            menuCode = "dmp:afterSale:updateAndSubmit",
//            serviceClass = AfterSaleService.class,
//            keyIdName = "id")
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "售后申请表修改并提交审核")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated AfterSaleDTO.UpdateDTO dto) {
        afterSaleService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author jack
    * @date:  2025-04-06
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
//    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//            tableField = "create_user_id",
//            menuCode = "dmp:afterSale:submit",
//            serviceClass = AfterSaleService.class,
//            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "售后申请表提交审核")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<AfterSaleEntity> list = afterSaleService.lambdaQuery().in(AfterSaleEntity::getId, ids).list();
		Map<String, AfterSaleEntity> idEntityMap = list.stream().collect(Collectors.toMap(AfterSaleEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = afterSaleService.submit(id);
            }catch (Exception e){
                log.error("售后申请单 提交审核失败",e);
                AfterSaleEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "售后申请单不存在, 提交失败");
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
    * @date:  2025-04-06
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
//    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//            tableField = "create_user_id",
//            menuCode = "dmp:afterSale:approve",
//            serviceClass = AfterSaleService.class,
//            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "售后申请表审核")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<AfterSaleEntity> list = afterSaleService.lambdaQuery().in(AfterSaleEntity::getId, ids).list();
		Map<String, AfterSaleEntity> idEntityMap = list.stream().collect(Collectors.toMap(AfterSaleEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = afterSaleService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("售后申请单审核失败",e);
                AfterSaleEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "售后申请单不存在, 审核失败");
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
    * @date:  2025-04-06
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
//    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//            tableField = "create_user_id",
//            menuCode = "dmp:afterSale:disApprove",
//            serviceClass = AfterSaleService.class,
//            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "售后申请表反审核")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<AfterSaleEntity> list = afterSaleService.lambdaQuery().in(AfterSaleEntity::getId, ids).list();
		Map<String, AfterSaleEntity> idEntityMap = list.stream().collect(Collectors.toMap(AfterSaleEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = afterSaleService.disApprove(id);
            }catch (Exception e){
                log.error("售后申请单反审核失败",e);
                AfterSaleEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "售后申请单不存在, 反审核失败");
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
    * @date:  2025-04-06
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
//    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//            tableField = "create_user_id",
//            menuCode = "dmp:afterSale:delete",
//            serviceClass = AfterSaleService.class,
//            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "售后申请表删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<AfterSaleEntity> list = afterSaleService.lambdaQuery().in(AfterSaleEntity::getId, ids).list();
		Map<String, AfterSaleEntity> idEntityMap = list.stream().collect(Collectors.toMap(AfterSaleEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = afterSaleService.delete(id);
            }catch (Exception e){
                log.error("售后申请单删除失败",e);
                AfterSaleEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "售后申请单不存在, 删除失败");
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
    * @date:  2025-04-06
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/invalid")
//    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//            tableField = "create_user_id",
//            menuCode = "dmp:afterSale:invalid",
//            serviceClass = AfterSaleService.class,
//            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "售后申请表作废")
    public ApiResult<List<BatchResultDTO>> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<AfterSaleEntity> list = afterSaleService.lambdaQuery().in(AfterSaleEntity::getId, ids).list();
		Map<String, AfterSaleEntity> idEntityMap = list.stream().collect(Collectors.toMap(AfterSaleEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = afterSaleService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("售后申请单作废失败",e);
                AfterSaleEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, "售后申请单不存在, 作废失败");
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
    * @author jack
    * @date:  2025-04-06
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
//    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//            tableField = "create_user_id",
//            menuCode = "dmp:afterSale:cancelProcess",
//            serviceClass = AfterSaleService.class,
//            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "售后申请表撤销")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<AfterSaleEntity> list = afterSaleService.lambdaQuery().in(AfterSaleEntity::getId, ids).list();
        Map<String, AfterSaleEntity> idEntityMap = list.stream().collect(Collectors.toMap(AfterSaleEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = afterSaleService.cancelProcess(id);
            }catch (Exception e){
                log.error("售后申请单撤回流程失败",e);
                AfterSaleEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "售后申请单不存在, 撤回流程失败");
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
    * @date:  2025-04-06
    * @param id
    * @return ApiResult<AfterSaleDTO.ViewDTO>>
    */
    @GetMapping("/view")
//    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//            tableField = "create_user_id",
//            menuCode = "dmp:afterSale:view",
//            serviceClass = AfterSaleService.class,
//            keyIdName = "id")
    @LogViewService
    public ApiResult<AfterSaleDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(afterSaleService.view(id));
    }

    /**
    * 导出Excel数据
    * @author jack
    * @date:  2025-04-06
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "create_user_id",
//            menuCode = "dmp:afterSale:export",
//            tableAlias = "afs"
//    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "售后申请表导出Excel数据")
    @WebAdvanceQuery(handler = AfterSaleQueryHandler.class)
    public ApiResult<Object> exportList(@RequestBody @Validated AfterSaleDTO.ExportDTO dto, HttpServletResponse response) {
        afterSaleService.exportList(dto, response);
        return success();
    }

    /**
     *
     * @author jack
     * @date:  2025-04-06
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/changeStatus")
//    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//            tableField = "create_user_id",
//            menuCode = "dmp:afterSale:changeStatus",
//            serviceClass = AfterSaleService.class,
//            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "售后申请状态变更")
    public ApiResult<List<BatchResultDTO>> changeStatus(@RequestBody @Validated AfterSaleDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = afterSaleService.changeStatus(dto);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 获取寄修进度
     * @author jack
     * @date:  2025-04-06
     * @return ApiResult
     */
    @PostMapping("/getRepairRecord")
    public ApiResult<AfterSaleProgressDTO.RepairRecordDTO> getRepairRecord(@RequestBody @Validated AfterSaleDTO.ProgressDTO dto) {
        return success(afterSaleService.getRepairProgress(dto));
    }


    /**
     * 获取寄修历史
     * @author jack
     * @date:  2025-04-06
     * @return ApiResultwo
     */
    @PostMapping("/getRepairHistory")
    public ApiResult<List<AfterSaleProgressDTO.RepairHistoryListDTO>> getRepairHistory(@RequestBody @Validated AfterSaleDTO.ThridUserDTO dto) {
        return success(afterSaleService.getRepairHistory(dto));
    }

    /**
     * 根据订单编号查询明细
     * @Author jack
     * @since 2025-04-07
     */
    @GetMapping("/getDetailByPlatformCode")
    ApiResult<List<AfterSaleDTO.DropDownDTO>> getDetailByPlatformCode(@RequestParam("platformCode") String platformCode){
        return success(afterSaleService.getDetailByPlatformCode(platformCode));
    }

    /**
     * 获取节点配置信息
     * @Author jack
     * @since 2025-04-07
     */
    @GetMapping("/getNodeList")
    ApiResult<List<AfterSaleDTO.NodeDTO>> getNodeList(){
        return success(afterSaleService.getNodeList());
    }
    /**
     * 获取节点配置信息
     * @Author jack
     * @since 2025-04-07
     */
    @GetMapping("/syncWdtToAfterSale")
    ApiResult<Object> syncWdtToAfterSale(){
        afterSaleService.syncWdtToAfterSale();
        return success();
    }

}
