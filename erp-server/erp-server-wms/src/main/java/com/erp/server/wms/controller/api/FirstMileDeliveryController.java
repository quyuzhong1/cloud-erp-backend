package com.erp.server.wms.controller.api;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.OverseasWarehouseInboundDTO;
import com.erp.model.wms.dto.PackingTaskDTO;
import com.erp.model.wms.dto.WmsCartonSpecDTO;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.model.wms.entity.PackingTaskEntity;
import com.erp.server.wms.query.FirstMileDeliveryQueryHandler;
import com.erp.server.wms.service.FirstMileDeliveryDetailService;
import com.erp.server.wms.service.FirstMileDeliveryService;
import com.erp.server.wms.service.PackingTaskService;
import com.erp.server.wms.service.RequisitionApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 头程发货单
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Slf4j
@RestController
@LogSystemModule("头程发货单")
@RequestMapping("/fbaDelivery")
public class FirstMileDeliveryController extends BaseController {

    @Resource
    private FirstMileDeliveryService firstMileDeliveryService;

    @Resource
    private FirstMileDeliveryDetailService firstMileDeliveryDetailService;

    @Resource
    private PackingTaskService packingTaskService;
    @Resource
    private RequisitionApplicationService requisitionApplicationService;

    /**
    * 新增
    * @author Luo_WG
    * @date:  2023-10-30
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "头程发货单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated FirstMileDeliveryDTO.AddDTO dto) {
        return success(firstMileDeliveryService.add(dto));
    }

    /**
    * 修改
    * @author Luo_WG
    * @date:  2023-10-30
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
    tableField = "create_user_id",
    menuCode = "wms:fbaDelivery:update",
    serviceClass = FirstMileDeliveryService.class,
    keyIdName = "id")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程发货单修改")
    public ApiResult update(@RequestBody @Validated FirstMileDeliveryDTO.UpdateDTO dto) {
        firstMileDeliveryService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            shopTableField = "fd.shop_id",
            warehouseTableField = "fd.delivery_warehouse_id,fd.dest_warehouse_id",
            menuCode = "wms:fbaDelivery:paging",
            tableAlias = "fd"
    )
    public ApiResult<List<FirstMileDeliveryDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(firstMileDeliveryService.tabList(dto));
    }

    /**
    * 列表查询
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @return ApiResult<PagingVO<FbaDeliveryDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            shopTableField = "fd.shop_id",
            warehouseTableField = "fd.delivery_warehouse_id,fd.dest_warehouse_id",
            menuCode = "wms:fbaDelivery:paging",
            tableAlias = "fd"
    )
    @WebAdvanceQuery(handler = FirstMileDeliveryQueryHandler.class)
    public ApiResult<PagingVO<FirstMileDeliveryDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<FirstMileDeliveryDTO.PagingParamDTO> dto) {
        return success(firstMileDeliveryService.paging(dto));
    }

    /**
     * 根据头程发货单ids获取国家下拉。
     */
    @PostMapping("/countryDropDownByIds")
    public ApiResult<List<BaseDropDownDTO.DisabledDTO>> countryDropDownByIds(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(firstMileDeliveryService.countryDropDownByIds(dto.getIds()));
    }

    /**
    * 新增并提交审核
    * @author Luo_WG
    * @date:  2023-10-30
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交审核")
    public ApiResult<BaseResultDTO.AddAndSubmmitDTO> addAndSubmit(@RequestBody @Validated FirstMileDeliveryDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO resultAdd;
        try {
            resultAdd = firstMileDeliveryService.add(dto);
        } catch (ServiceException e) {
            log.error("新增失败，dto: {}", dto, e);
            return failure(e.getMessage(),new BaseResultDTO.AddAndSubmmitDTO("","",Boolean.FALSE));
        } catch (Exception e) {
            log.error("新增失败，dto: {}", dto, e);
            return failure(ApiError.BILL_SAVE_FAILED.getMsg(),new BaseResultDTO.AddAndSubmmitDTO("","",Boolean.FALSE));
        }
        //提审
        try {
            firstMileDeliveryService.submit(resultAdd.getId(),Boolean.TRUE);;
        } catch (ServiceException e) {
            log.error("提交审批失败，ID: {}", resultAdd.getId(), e);
            return failure(e.getMessage(),new BaseResultDTO.AddAndSubmmitDTO(resultAdd.getId(),resultAdd.getCode(),Boolean.TRUE));
        } catch (Exception e) {
            log.error("提交审批失败，ID: {}", resultAdd.getId(), e);
            return failure(ApiError.BILL_APPROVE_SUBMIT_RETRY.getMsg(),new BaseResultDTO.AddAndSubmmitDTO(resultAdd.getId(),resultAdd.getCode(),Boolean.TRUE));
        }

        return success(new BaseResultDTO.AddAndSubmmitDTO(resultAdd.getId(), resultAdd.getCode(),Boolean.TRUE));
    }

    /**
    * 修改并提交审核
    * @author Luo_WG
    * @date:  2023-10-30
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:fbaDelivery:updateAndSubmit",
            serviceClass = FirstMileDeliveryService.class,
            keyIdName = "id")
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交审核")
    public ApiResult<BaseResultDTO.AddAndSubmmitDTO> updateAndSubmit(@RequestBody @Validated FirstMileDeliveryDTO.UpdateDTO dto) {
        try {
            firstMileDeliveryService.update(dto);
        } catch (ServiceException e) {
            log.error("更新失败，dto: {}", dto, e);
            return failure(e.getMessage(), new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.FALSE));
        } catch (Exception e) {
            log.error("更新失败，dto: {}", dto, e);
            return failure(ApiError.BILL_UPDATE_FAILED.getMsg(),new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.FALSE));
        }
        //提审
        try {
            firstMileDeliveryService.submit(dto.getId(),Boolean.TRUE);
        } catch (ServiceException e) {
            log.error("提交审批失败，ID: {}", dto.getId(), e);
            return failure( e.getMessage(),new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.TRUE));
        } catch (Exception e) {
            log.error("提交审批失败，ID: {}", dto.getId(), e);
            return failure(ApiError.BILL_APPROVE_SUBMIT_RETRY.getMsg(),new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.TRUE));
        }

        return success(new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.TRUE));
    }

    /**
    * 提交审核
    * @author Luo_WG
    * @date:  2023-10-30
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:fbaDelivery:submit",
            serviceClass = FirstMileDeliveryService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "头程发货单提交审核")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = firstMileDeliveryService.submit(id,Boolean.TRUE);
            }catch (Exception e){
                log.error("发货单 提交审核失败",e);
                FirstMileDeliveryEntity entity = firstMileDeliveryService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "发货单不存在, 提交失败");
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
    * @date:  2023-10-30
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:fbaDelivery:approve",
            serviceClass = FirstMileDeliveryService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "头程发货单审核")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds().stream().filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = firstMileDeliveryService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment(),dto.getDeliveryDate()));
            }catch (Exception e){
                log.error("发货单审核失败",e);
                FirstMileDeliveryEntity entity = firstMileDeliveryService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "发货单不存在, 审核失败");
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
    * @date:  2023-10-30
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:fbaDelivery:disApprove",
            serviceClass = FirstMileDeliveryService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "头程发货单反审核")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = firstMileDeliveryService.disApprove(id);
            }catch (Exception e){
                log.error("发货单反审核失败",e);
                FirstMileDeliveryEntity entity = firstMileDeliveryService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "发货单不存在, 反审核失败");
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
    * @date:  2023-10-30
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:fbaDelivery:delete",
            serviceClass = FirstMileDeliveryService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "头程发货单删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<FirstMileDeliveryEntity> entityList = firstMileDeliveryService.listByIds(dto.getIds());
        List<String> sourceCodeList = entityList.stream().map(FirstMileDeliveryEntity::getCode).distinct().collect(Collectors.toList());
        List<PackingTaskEntity> packingTaskEntityList = packingTaskService.listBySourceCodes(sourceCodeList);
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            FirstMileDeliveryEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                invalidResult = BatchResultDTO.fail(id, id, "发货单不存在, 删除失败");
            }else{
                try {
                    PackingTaskEntity packingTaskEntity = packingTaskEntityList.stream().filter(v->v.getSourceCode().equals(entity.getCode())).findFirst().orElse(null);
                    invalidResult = firstMileDeliveryService.delete(entity, packingTaskEntity);
                    requisitionApplicationService.writeBackRequisitionDeliveryPushDownStatus(entity.getSourceId());
                }catch (Exception e){
                    log.error("发货单删除失败",e);
                    invalidResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
                }
            }
            resultDTOS.add(invalidResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    /**
    * 作废
    * @author Luo_WG
    * @date:  2023-10-30
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:fbaDelivery:invalid",
            serviceClass = FirstMileDeliveryService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "头程发货单作废")
    public ApiResult<List<BatchResultDTO>> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<FirstMileDeliveryEntity> entityList = firstMileDeliveryService.listByIds(dto.getIds());
        List<String> sourceCodeList = entityList.stream().map(FirstMileDeliveryEntity::getCode).distinct().collect(Collectors.toList());
        List<PackingTaskEntity> packingTaskEntityList = packingTaskService.listBySourceCodes(sourceCodeList);
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            FirstMileDeliveryEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                invalidResult = BatchResultDTO.fail(id, id, "发货单不存在, 作废失败");
            }else{
                try {
                    PackingTaskEntity packingTaskEntity = packingTaskEntityList.stream().filter(v->v.getSourceCode().equals(entity.getCode())).findFirst().orElse(null);
                    invalidResult = firstMileDeliveryService.invalid(entity,dto.getRemark(), packingTaskEntity);
                    requisitionApplicationService.writeBackRequisitionDeliveryPushDownStatus(entity.getSourceId());

                }catch (Exception e){
                    log.error("发货单作废失败",e);
                    invalidResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
                }
            }
            resultDTOS.add(invalidResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 撤销
    * @author Luo_WG
    * @date:  2023-10-30
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:fbaDelivery:cancelProcess",
            serviceClass = FirstMileDeliveryService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "头程发货单撤销")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = firstMileDeliveryService.cancelProcess(new ApproveDTO.CancelProcessDTO(id));
            }catch (Exception e){
                log.error("发货单撤回流程失败",e);
                FirstMileDeliveryEntity entity = firstMileDeliveryService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "发货单不存在, 撤回流程失败");
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
    * @date:  2023-10-30
    * @param id
    * @return ApiResult<FbaDeliveryDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:fbaDelivery:view",
            serviceClass = FirstMileDeliveryService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<FirstMileDeliveryDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(firstMileDeliveryService.view(id));
    }

    /**
    * 导出Excel数据
    * @author Luo_WG
    * @date:  2023-10-30
    * @param dto
    * @return
    */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "头程发货单导出Excel数据")
    public ApiResult<Boolean> exportList(@RequestBody @Validated FirstMileDeliveryDTO.PagingParamDTO dto) {
        firstMileDeliveryService.exportList(dto);
        return success(true);
    }

    /**
     * 下推装箱任务
     **/
    @PostMapping("/generatePackingTask")
    public ApiResult<List<BatchResultDTO>> generatePackingTask(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        List<FirstMileDeliveryEntity> entityList = firstMileDeliveryService.listByIds(ids);
        List<String> sourceCodes = entityList.stream().map(FirstMileDeliveryEntity::getCode).distinct().collect(Collectors.toList());
        List<PackingTaskEntity> packingTaskEntityList = packingTaskService.listBySourceCodes(sourceCodes);
        List<BatchResultDTO> result = new ArrayList<>();
        for (String id : ids) {
            FirstMileDeliveryEntity firstMileDeliveryEntity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(firstMileDeliveryEntity)){
                result.add(BatchResultDTO.fail(id,id,"发货单为空"));
                continue;
            }
            try {
                PackingTaskEntity packingTaskEntity = packingTaskEntityList.stream().filter(v->v.getSourceCode().equals(firstMileDeliveryEntity.getCode())).findFirst().orElse(null);
                if(Objects.nonNull(packingTaskEntity)){
                    result.add(BatchResultDTO.fail(id,firstMileDeliveryEntity.getCode(),"已生成装箱任务不可重复生成"));
                    continue;
                }
                result.add(firstMileDeliveryService.generatePackingTask(firstMileDeliveryEntity));
            }catch (Exception e){
                log.error("头程发货单下推装箱任务失败>>>>>", e);
                result.add(BatchResultDTO.fail(firstMileDeliveryEntity.getId(),firstMileDeliveryEntity.getCode(),e.getMessage()));
            }
        }
        return result.stream().allMatch(BatchResultDTO::getSuccess) ? success(result) : failure(result);
    }

    /**
     * 下推加工单列表查询
     * @Author Luo_WG
     * @Date 2023/10/31 9:44
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaDeliveryDTO.GenerateGenerateMachineView>>
     **/
    @PostMapping("/generateMachineView")
    public ApiResult<List<FirstMileDeliveryDTO.GenerateMachineView>> generateMachineView(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<FirstMileDeliveryDTO.GenerateMachineView> result = firstMileDeliveryService.generateMachineView(dto.getIds());
        return success(result);
    }

    /**
     * 下推加工单保存
     * @Author Luo_WG
     * @Date 2023/10/31 9:57
     * @param list
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/fbaDeliveryGenerateMachineSave")
    public ApiResult fbaDeliveryGenerateMachineSave(@RequestBody @Validated List<FirstMileDeliveryDTO.GenerateMachineView> list) {
        Boolean flag = firstMileDeliveryService.fbaDeliveryGenerateMachineSave(list);
        return flag ? success() : failure();
    }

    /**
     * 下推加工单提交
     * @Author Luo_WG
     * @Date 2023/10/31 9:57
     * @param list
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/fbaDeliveryGenerateMachineSubmit")
    public ApiResult fbaDeliveryGenerateMachineSubmit(@RequestBody @Validated List<FirstMileDeliveryDTO.GenerateMachineView> list) {
        Boolean flag = firstMileDeliveryService.fbaDeliveryGenerateMachineSubmit(list);
        return flag ? success() : failure();
    }

    /**
     * 下推加工单提交并审核
     * @Author Luo_WG
     * @Date 2023/10/31 9:57
     * @param list
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/fbaDeliveryGenerateMachineSubmitAndApprove")
    public ApiResult<List<BatchResultDTO>> fbaDeliveryGenerateMachineSubmitAndApprove(@RequestBody @Validated List<FirstMileDeliveryDTO.GenerateMachineView> list) {
        List<BatchResultDTO> resultDTOS = firstMileDeliveryService.fbaDeliveryGenerateMachineSubmitAndApprove(list);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 打印子件明细查询
     * @Author Luo_WG
     * @Date 2023/10/31 10:12
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaDeliveryDTO.PrintSonItem>>
     **/
    @PostMapping("/printSonItemDetail")
    public ApiResult<List<FirstMileDeliveryDTO.PrintSonItem>> printSonItemDetail(@RequestBody @Validated List<FirstMileDeliveryDTO.GenerateMachineView> dto) {
        List<FirstMileDeliveryDTO.PrintSonItem> list = firstMileDeliveryService.printSonItemDetail(dto);
        return success(list);
    }

    /**
     * 根据版本号重新获取下推加工单的子件详情
     * @Author Luo_WG
     * @Date 2023/11/8 11:18
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaDeliveryDTO.SonItem>>
     **/
    @PostMapping("/sonItemDetailByVersion")
    public ApiResult<List<FirstMileDeliveryDTO.SonItem>> sonItemDetailByVersion(@RequestBody @Validated FirstMileDeliveryDTO.SonItemDetailByVersion dto) {
        List<FirstMileDeliveryDTO.SonItem> result = firstMileDeliveryService.sonItemDetailByVersion(dto);
        return success(result);
    }

    /**
     * 下推海外仓入库单单个查询
     * @author Luo_WG
     * @date 2023-10-30
     * @param id 发货单id
     */
    @GetMapping("/getGenerateOverseasWarehouseInboundView")
    public ApiResult<OverseasWarehouseInboundDTO.ViewDTO> getGenerateOverseasWarehouseInboundView(@RequestParam("id") String id) {
        OverseasWarehouseInboundDTO.ViewDTO result = firstMileDeliveryService.getGenerateOverseasWarehouseInboundView(id);
        return success(result);
    }
    /**
     * 生成状态更新为无需生成
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "生成状态更新")
    @PostMapping("/generateStatusUpdate")
    public ApiResult generateStatusUpdate(@RequestBody FirstMileDeliveryDTO.GenerateStatusUpdateDTO dto) {
        Boolean result = firstMileDeliveryService.generateStatusUpdate(dto);
        return result == true ? success() : failure();
    }

    /**
     * 期初明细分页列表
     * @author zdy
     * @date: 2024-8-15
     * @param dto
     * @return ApiResult<PagingVO<FirstMileDeliveryDTO.ListDTO>>
     */
    @PostMapping("/pagingFirstMile")
    @WebAdvanceQuery(handler = FirstMileDeliveryQueryHandler.class)
    public ApiResult<PagingVO<FirstMileDeliveryDTO.ListFirstMileDTO>> pagingFirstMile(@RequestBody @Validated PagingDTO<FirstMileDeliveryDTO.PagingParamDTO> dto) {
        return success(firstMileDeliveryService.pagingFirstMile(dto));
    }

    /**
     * 导出装箱清单Excel
     */
    @PostMapping("/exportPackingDetail")
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出装箱任务Excel")
    public ApiResult exportPackingDetail(@RequestBody @Validated PackingTaskDTO.ExportDTO dto) {
        firstMileDeliveryService.exportPackingDetail(dto);
        return success();
    }


    /**
     * 查看装箱
     */
    @PostMapping("/listPacking")
    public ApiResult<WmsCartonSpecDTO.ListPackingDTO> listPacking(@RequestBody @Validated BaseIdDTO dto) {
        return success(firstMileDeliveryService.listPacking(dto.getId()));
    }

    /**
     * 下载箱号对照表excel
     */
    @PostMapping("/exportBox")
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出箱号对照表Excel")
    public ApiResult exportBox(@RequestBody @Validated PackingTaskDTO.ExportDTO dto) {
        firstMileDeliveryService.exportBox(dto);
        return success();
    }

    /**
     * 批量修改中转仓库
     * @author zdy
     * @date:  2024-10-24
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/updateTransferWarehouse")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "批量修改中转仓库")
    public ApiResult<List<BatchResultDTO>> updateTransferWarehouse(@RequestBody @Validated BaseIdsDTO.ChangeDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<FirstMileDeliveryEntity> entityList = firstMileDeliveryService.listByIds(ids);
        for (String id : ids) {
            BatchResultDTO resultDTO;
            FirstMileDeliveryEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"头程发货单记录不存在"));
                continue;
            }
            try {
                resultDTO = firstMileDeliveryService.updateTransferWarehouse(entity, dto.getChangeIds());
            }catch (Exception e){
                log.error("头程发货单修改中转仓库失败",e);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "发货单不存在, 修改中转仓库失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }



    /**
     * 重新出库
     * @author will
     * @date 2025/08/06 15:47
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/retryOutstock")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "批量重新出库")
    public ApiResult<List<BatchResultDTO>> retryOutstock(@RequestBody @Validated BaseIdsDTO.IdsDTO dto){
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = firstMileDeliveryService.retryOutstock(id);
            }catch (Exception e){
                log.error("重新出库失败",e);
                FirstMileDeliveryEntity entity = firstMileDeliveryService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "头程发货单不存在, 重新出库失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 取消发货列表
     * @author will
     * @date 2026/1/23 15:18
     * @param dto
     * @return ApiResult<PagingVO<CancelDeliveryListDTO>>
     */
    @PostMapping("/cancelDeliveryPaging")
    @WebAdvanceQuery
    @DataPermission(operationType = DataAttributeEnum.LIST,
            shopTableField = "fmd.shop_id",
            warehouseTableField = "fmd.delivery_warehouse_id,fmd.dest_warehouse_id",
            menuCode = "wms:fbaDelivery:paging"
    )
    public ApiResult<PagingVO<FirstMileDeliveryDTO.CancelDeliveryListDTO>> cancelDeliveryPaging(@RequestBody @Valid PagingDTO<FirstMileDeliveryDTO.CancelDeliveryParamDTO> dto) {
        PagingVO<FirstMileDeliveryDTO.CancelDeliveryListDTO> pagingVO = firstMileDeliveryService.cancelDeliveryPaging(dto);
        return success(pagingVO);
    }

    /**
     * 批量取消发货
     * @author will
     * @date 2026/1/23 18:49
     * @param list
     * @return ApiResult<Object>
     */
    @PostMapping("/batchCancelDelivery")
    public ApiResult<Object> batchCancelDelivery(@RequestBody @Valid ValidList<FirstMileDeliveryDTO.CancelDeliveryDTO> list) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(list.size());
        List<String> logisticsBillIds = new ArrayList<>();
        for (FirstMileDeliveryDTO.CancelDeliveryDTO cancelDeliveryDTO :list) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = firstMileDeliveryService.cancelDelivery(cancelDeliveryDTO);
                if(resultDTO.getSuccess()){
                    logisticsBillIds.add(cancelDeliveryDTO.getLogisticsBillId());
                }
            }catch (Exception e){
                resultDTO = BatchResultDTO.fail(cancelDeliveryDTO.getPackingTaskId(), cancelDeliveryDTO.getPackingTaskId(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        if(CollUtil.isNotEmpty(logisticsBillIds)){
            firstMileDeliveryService.sendMsg(logisticsBillIds);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 添加产品明细（查询未生成的头程发货明细信息）
     * @author will
     * @date 2026/4/21 19:09
     * @return com.common.core.controller.vo.ApiResult<java.lang.Object>
     */
    @PostMapping("/listNotGenerateFmDetailPaging")
    @LogAction(value = LogActionEnum.INSERT, desc = "添加产品明细")
    @WebAdvanceQuery
    public ApiResult<PagingVO<TmsDeclareBillDTO.NotGenerateDetailDTO>> listNotGenerateFmDetailPaging(@RequestBody @Valid PagingDTO<TmsDeclareBillDTO.NotGenerateParamDTO> dto)  {
        PagingVO<TmsDeclareBillDTO.NotGenerateDetailDTO> pagingVO = firstMileDeliveryService.listNotGenerateFmDetailPaging(dto);
        return success(pagingVO);
    }

    /**
     * 下推头程报关单（合并前）
     * @author will
     * @date 2026/4/23 18:00
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.lang.Object>
     */
    @PostMapping("/listBeforePushFmDeclare")
    public ApiResult<List<TmsDeclareBillDTO.SourceDeliveryDetailDTO>> listBeforePushFmDeclare(@RequestBody @Valid TmsDeclareBillDTO.PushDeclareBeforeParamDTO dto)  {
        return success(firstMileDeliveryService.listBeforePushFmDeclare(dto));
    }


    /**
     * 下推头程报关单（合并后）
     * @author will
     * @date 2026/4/23 18:00
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<List<TmsDeclareBillDTO.SourceDeliveryDetailDTO>>
     */
    @PostMapping("/listAfterPushFmDeclare")
    public ApiResult<List<TmsDeclareBillDTO.MergeDeclareBillDTO>> listAfterPushFmDeclare(@RequestBody @Valid TmsDeclareBillDTO.PushDeclareBeforeParamDTO dto)  {
        return success(firstMileDeliveryService.listAfterPushFmDeclare(dto));
    }

    /**
     * 头程报关单（BOM拆分后不合并，按来源明细最小维度返回）
     * @author will
     * @date 2026/5/9 15:00
     * @param list
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.MergeDeclareBillDTO>>
     */
    @PostMapping("/listAfterPushFmDeclareNoMerge")
    public ApiResult<TmsDeclareBillDTO.MergeDeclareBillDTO> listAfterPushFmDeclareNoMerge(@RequestBody @Valid ValidList<TmsDeclareBillDTO.PushDeclareNoMergeDTO> list)  {
        return success(firstMileDeliveryService.listAfterPushFmDeclareNoMerge(list.getList()));
    }
}
