package com.erp.server.wms.controller.api;


import com.common.business.validator.ValidList;
import com.erp.server.wms.service.SoDeliveryNoticeService;
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
import com.erp.server.wms.service.OverseasDeliveryPlanService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.OverseasDeliveryPlanDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import com.erp.model.wms.entity.OverseasDeliveryPlanEntity;

/**
 * 发货计划
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@RestController
@LogSystemModule("发货计划")
@RequestMapping("/overseasDeliveryPlan")
public class OverseasDeliveryPlanController extends BaseController {

    @Resource
    private OverseasDeliveryPlanService overseasDeliveryPlanService;

    /**
    * 新增
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "发货计划新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated OverseasDeliveryPlanDTO.AddDTO dto) {
        return success(overseasDeliveryPlanService.add(dto));
    }

    /**
    * 修改
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "发货计划修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:overseasDeliveryPlan:update",
        serviceClass = OverseasDeliveryPlanService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated OverseasDeliveryPlanDTO.UpdateDTO dto) {
        overseasDeliveryPlanService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:overseasDeliveryPlan:paging",
            tableAlias = "odp"
    )
    public ApiResult<List<OverseasDeliveryPlanDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(overseasDeliveryPlanService.tabList(dto));
    }

    /**
    * 列表查询
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return ApiResult<PagingVO<OverseasDeliveryPlanDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:overseasDeliveryPlan:paging",
            tableAlias = "odp"
    )
    public ApiResult<PagingVO<OverseasDeliveryPlanDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<OverseasDeliveryPlanDTO.PagingParamDTO> dto) {
        return success(overseasDeliveryPlanService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult<Void>
    */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交发货计划")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasDeliveryPlan:add",
            serviceClass = OverseasDeliveryPlanService.class,
            keyIdName = "id")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated OverseasDeliveryPlanDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = overseasDeliveryPlanService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult<Void>
    */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交发货计划")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasDeliveryPlan:updateAndSubmit",
            serviceClass = OverseasDeliveryPlanService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated OverseasDeliveryPlanDTO.UpdateDTO dto) {
        overseasDeliveryPlanService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasDeliveryPlan:submit",
            serviceClass = OverseasDeliveryPlanService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "发货计划提交审核")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = overseasDeliveryPlanService.submit(id);
            }catch (Exception e){
                log.error("发货计划 提交审核失败",e);
                OverseasDeliveryPlanEntity entity = overseasDeliveryPlanService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "发货计划不存在, 提交失败");
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
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasDeliveryPlan:approve",
            serviceClass = OverseasDeliveryPlanService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "发货计划审核")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = overseasDeliveryPlanService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("发货计划审核失败",e);
                OverseasDeliveryPlanEntity entity = overseasDeliveryPlanService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "发货计划不存在, 审核失败");
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
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasDeliveryPlan:disApprove",
            serviceClass = OverseasDeliveryPlanService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "发货计划反审核")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = overseasDeliveryPlanService.disApprove(id);
            }catch (Exception e){
                log.error("发货计划反审核失败",e);
                OverseasDeliveryPlanEntity entity = overseasDeliveryPlanService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "发货计划不存在, 反审核失败");
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
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasDeliveryPlan:delete",
            serviceClass = OverseasDeliveryPlanService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "发货计划删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = overseasDeliveryPlanService.delete(id);
            }catch (Exception e){
                log.error("发货计划删除失败",e);
                OverseasDeliveryPlanEntity entity = overseasDeliveryPlanService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "发货计划不存在, 删除失败");
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
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasDeliveryPlan:invalid",
            serviceClass = OverseasDeliveryPlanService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "发货计划作废")
    public ApiResult<List<BatchResultDTO>> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = overseasDeliveryPlanService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("发货计划作废失败",e);
                OverseasDeliveryPlanEntity entity = overseasDeliveryPlanService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, "发货计划不存在, 作废失败");
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
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasDeliveryPlan:cancel",
            serviceClass = OverseasDeliveryPlanService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "发货计划撤销")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = overseasDeliveryPlanService.cancelProcess(id);
            }catch (Exception e){
                log.error("发货计划撤回流程失败",e);
                OverseasDeliveryPlanEntity entity = overseasDeliveryPlanService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "发货计划不存在, 撤回流程失败");
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
    * @author Luo_WG
    * @date:  2023-11-16
    * @param id
    * @return ApiResult<OverseasDeliveryPlanDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasDeliveryPlan:view",
            serviceClass = OverseasDeliveryPlanService.class,
            keyIdName = "id")
    public ApiResult<OverseasDeliveryPlanDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(overseasDeliveryPlanService.view(id));
    }

    /**
    * 导出Excel数据
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:overseasDeliveryPlan:export",
            tableAlias = "odp"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "发货计划导出Excel数据")
    public void exportList(@RequestBody @Validated OverseasDeliveryPlanDTO.ExportDTO dto, HttpServletResponse response) {
        overseasDeliveryPlanService.exportList(dto, response);
    }

    /**
     * 查询发货记录
     * @Author Luo_WG
     * @Date 2023/11/16 17:21
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<OverseasDeliveryPlanDTO.DeliverRecordDTO>>
     **/
    @GetMapping("/listDeliverRecord")
    public ApiResult<List<OverseasDeliveryPlanDTO.DeliverRecordDTO>> listDeliverRecord(@RequestParam("id") String id) {
        List<OverseasDeliveryPlanDTO.DeliverRecordDTO> result = overseasDeliveryPlanService.listDeliverRecord(id);
        return success(result);
    }

    /**
     * 下推要货申请列表查询
     * @Author Luo_WG
     * @Date 2023/11/16 18:07
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO>>
     **/
    @PostMapping("/generateRequisitionApplicationView")
    public ApiResult<List<OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO>> generateRequisitionApplicationView(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> result = overseasDeliveryPlanService.generateRequisitionApplicationView(dto.getIds());
        return success(result);
    }

    /**
     * 发货计划下推要货申请保存
     * @Author Luo_WG
     * @Date 2023/11/16 18:06
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/generateRequisitionApplicationSave")
    @LogAction(value = LogActionEnum.INSERT, desc = "发货计划下推要货申请保存")
    public ApiResult generateRequisitionApplicationSave(@RequestBody @Validated ValidList<OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> dto) {
        Boolean flag = overseasDeliveryPlanService.generateRequisitionApplicationSave(dto.getList());
        return flag ? success() : failure();
    }

    /**
     * 下推发货单列表查询
     * @Author Luo_WG
     * @Date 2023/11/16 18:06
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.OverseasDeliveryPlanDTO.GenerateDeliverViewDTO>>
     **/
    @PostMapping("/generateDeliverView")
    public ApiResult<List<OverseasDeliveryPlanDTO.GenerateDeliverViewDTO>> generateDeliverView(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<OverseasDeliveryPlanDTO.GenerateDeliverViewDTO> result = overseasDeliveryPlanService.generateDeliverView(dto.getIds());
        return success(result);
    }

    /**
     * 下推发货单保存
     * @Author Luo_WG
     * @Date 2023/11/16 18:06
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/generateDeliverSave")
    @LogAction(value = LogActionEnum.INSERT, desc = "下推发货单保存")
    public ApiResult generateDeliverSave(@RequestBody @Validated ValidList<OverseasDeliveryPlanDTO.GenerateDeliverViewDTO> dto) {
        Boolean flag = overseasDeliveryPlanService.generateDeliverSave(dto.getList());
        return flag ? success() : failure();
    }
}
