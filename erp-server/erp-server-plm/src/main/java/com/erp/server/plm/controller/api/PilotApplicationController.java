package com.erp.server.plm.controller.api;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.dto.PilotApplicationDTO;
import com.erp.model.plm.dto.PilotApplicationRefTaskDTO;
import com.erp.model.plm.dto.ProductPackViewDTO;
import com.erp.model.plm.dto.ProductSearchDTO;
import com.erp.model.plm.entity.PilotApplicationDetailEntity;
import com.erp.model.plm.entity.PilotApplicationEntity;
import com.erp.server.plm.query.PilotApplicationQueryHandler;
import com.erp.server.plm.service.PilotApplicationDetailService;
import com.erp.server.plm.service.PilotApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 试产/量产申请
 *
 * @author tmj
 * @since 2024-08-27
 */
@Slf4j
@RestController
@LogSystemModule("试产/量产申请")
@RequestMapping("/pilotApplication")
public class PilotApplicationController extends BaseController {

    @Resource
    private PilotApplicationService pilotApplicationService;
    @Resource
    private PilotApplicationDetailService pilotApplicationDetailService;

    /**
    * 新增
    * @author tmj
    * @date:  2024-08-27
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "试产/量产申请新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated PilotApplicationDTO.AddDTO dto) {
        return success(pilotApplicationService.add(dto));
    }

    /**
    * 修改
    * @author tmj
    * @date:  2024-08-27
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "试产/量产申请修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "plm:pilotApplication:update",
        serviceClass = PilotApplicationService.class,
        keyIdName = "id")
    public ApiResult<Object> update(@RequestBody @Validated PilotApplicationDTO.UpdateDTO dto) {
        pilotApplicationService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:pilotApplication:paging",
            tableAlias = "pa"
    )
    public ApiResult<List<PilotApplicationDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(pilotApplicationService.tabList(dto));
    }

    /**
    * 列表查询
    * @author tmj
    * @date: 2024-08-27
    * @param dto
    * @return ApiResult<PagingVO<PilotApplicationDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:pilotApplication:paging",
            tableAlias = "pa"
    )
    @WebAdvanceQuery(handler = PilotApplicationQueryHandler.class)
    public ApiResult<PagingVO<PilotApplicationDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<PilotApplicationDTO.PagingParamDTO> dto) {
        return success(pilotApplicationService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author tmj
    * @date:  2024-08-27
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交审核")
    public ApiResult<BaseResultDTO.AddAndSubmmitDTO> addAndSubmit(@RequestBody @Validated PilotApplicationDTO.AddDTO dto) {
        //新增
        BaseResultDTO.AddDTO add ;
        try {
            add = pilotApplicationService.add(dto);
        } catch (ServiceException e) {
            log.error("新增失败，dto: {}", dto, e);
            return failure(e.getMessage(),new BaseResultDTO.AddAndSubmmitDTO("","",Boolean.FALSE));
        } catch (Exception e) {
            log.error("新增失败，dto: {}", dto, e);
            return failure(ApiError.ERROR_1019.msg,new BaseResultDTO.AddAndSubmmitDTO("","",Boolean.FALSE));
        }

        //提审
        try {
            pilotApplicationService.submit(add.getId());
        } catch (ServiceException e) {
            log.error("提交审批失败，ID: {}", add.getId(), e);
            return failure(e.getMessage(),new BaseResultDTO.AddAndSubmmitDTO(add.getId(),add.getCode(),Boolean.TRUE));
        } catch (Exception e) {
            log.error("提交审批失败，ID: {}", add.getId(), e);
            return failure(ApiError.RETRY_SUBMIT_ERROR.msg,new BaseResultDTO.AddAndSubmmitDTO(add.getId(),add.getCode(),Boolean.TRUE));
        }
        //发送消息
        try {
            pilotApplicationService.approvePilotApplicationNotice(add.getId());
        } catch (Exception e) {
            log.error("发送消息失败，ID: {}", add.getId(), e);
        }
        return success(new BaseResultDTO.AddAndSubmmitDTO(add.getId(), add.getCode(),Boolean.TRUE));
    }

    /**
    * 修改并提交审核
    * @author tmj
    * @date:  2024-08-27
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交审核")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:pilotApplication:updateAndSubmit",
            serviceClass = PilotApplicationService.class,
            keyIdName = "id")
    public ApiResult<BaseResultDTO.AddAndSubmmitDTO> updateAndSubmit(@RequestBody @Validated PilotApplicationDTO.UpdateDTO dto) {
        //更新
        try {
            pilotApplicationService.update(dto);
        } catch (ServiceException e) {
            log.error("更新失败，dto: {}", dto, e);
            return failure(e.getMessage(), new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.FALSE));
        } catch (Exception e) {
            log.error("更新失败，dto: {}", dto, e);
            return failure(ApiError.ERROR_1020.msg,new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.FALSE));
        }

        //提审
        try {
            pilotApplicationService.submit(dto.getId());
        } catch (ServiceException e) {
            log.error("提交审批失败，ID: {}", dto.getId(), e);
            return failure( e.getMessage(),new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.TRUE));
        } catch (Exception e) {
            log.error("提交审批失败，ID: {}", dto.getId(), e);
            return failure(ApiError.RETRY_SUBMIT_ERROR.msg,new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.TRUE));
        }
        //发送消息
        try {
            pilotApplicationService.approvePilotApplicationNotice(dto.getId());
        } catch (Exception e) {
            log.error("试产量产发送消息失败，ID: {}", dto.getId(), e);
        }
        return success(new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.TRUE));
    }

    /**
    * 提交审核
    * @author tmj
    * @date:  2024-08-27
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:pilotApplication:submit",
            serviceClass = PilotApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "试产申请提交审核")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<PilotApplicationEntity> list = pilotApplicationService.lambdaQuery().in(PilotApplicationEntity::getId, ids).list();
		Map<String, PilotApplicationEntity> idEntityMap = list.stream().collect(Collectors.toMap(PilotApplicationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = pilotApplicationService.submit(id);
                pilotApplicationService.approvePilotApplicationNotice(id);
            }catch (Exception e){
                log.error("试产申请 提交审核失败",e);
                PilotApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "试产申请不存在, 提交失败");
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
    * @author tmj
    * @date:  2024-08-27
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:pilotApplication:approve",
            serviceClass = PilotApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "试产申请审核")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated PilotApplicationDTO.ApproveDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<PilotApplicationEntity> list = pilotApplicationService.lambdaQuery().in(PilotApplicationEntity::getId, ids).list();
		Map<String, PilotApplicationEntity> idEntityMap = list.stream().collect(Collectors.toMap(PilotApplicationEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = pilotApplicationService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()), dto);
                //回写产品管理--采购信息--一级和二级供应商
                pilotApplicationService.writeProductPurchaseBack(id);
                pilotApplicationService.approvePilotApplicationNotice(id);
            }catch (Exception e){
                log.error("试产申请审核失败",e);
                PilotApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "试产申请不存在, 审核失败");
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
    * @author tmj
    * @date:  2024-08-27
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:pilotApplication:disApprove",
            serviceClass = PilotApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "试产申请反审核")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<PilotApplicationEntity> list = pilotApplicationService.lambdaQuery().in(PilotApplicationEntity::getId, ids).list();
		Map<String, PilotApplicationEntity> idEntityMap = list.stream().collect(Collectors.toMap(PilotApplicationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = pilotApplicationService.disApprove(id);
            }catch (Exception e){
                log.error("试产申请反审核失败",e);
                PilotApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "试产申请不存在, 反审核失败");
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
    * @author tmj
    * @date:  2024-08-27
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:pilotApplication:delete",
            serviceClass = PilotApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "试产申请删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<PilotApplicationEntity> list = pilotApplicationService.lambdaQuery().in(PilotApplicationEntity::getId, ids).list();
		Map<String, PilotApplicationEntity> idEntityMap = list.stream().collect(Collectors.toMap(PilotApplicationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = pilotApplicationService.delete(id);
            }catch (Exception e){
                log.error("试产申请删除失败",e);
                PilotApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "试产申请不存在, 删除失败");
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
    * @author tmj
    * @date:  2024-08-27
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:pilotApplication:cancelProcess",
            serviceClass = PilotApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "试产申请撤销")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<PilotApplicationEntity> list = pilotApplicationService.lambdaQuery().in(PilotApplicationEntity::getId, ids).list();
        Map<String, PilotApplicationEntity> idEntityMap = list.stream().collect(Collectors.toMap(PilotApplicationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = pilotApplicationService.cancelProcess(new ApproveDTO.CancelProcessDTO(id));
            }catch (Exception e){
                log.error("试产申请撤回流程失败",e);
                PilotApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "试产申请不存在, 撤回流程失败");
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
    * @author tmj
    * @date:  2024-08-27
    * @param id
    * @return ApiResult<PilotApplicationDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:pilotApplication:view",
            serviceClass = PilotApplicationService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<PilotApplicationDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(pilotApplicationService.view(id));
    }

    /**
    * 导出Excel数据
    * @author tmj
    * @date:  2024-08-27
    * @param dto
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:pilotApplication:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "试产申请导出Excel数据")
    @WebAdvanceQuery(handler = PilotApplicationQueryHandler.class)
    public ApiResult<Object> exportList(@RequestBody @Validated PilotApplicationDTO.ExportDTO dto) {
        pilotApplicationService.exportList(dto);
        return success();
    }

    /**
     * 下推采购申请
     * @author tmj
     */
    @PostMapping("/pushPurchaseApplication")
    public ApiResult<List<BatchResultDTO>> pushPurchaseApplication(@RequestBody @Validated List<PilotApplicationDTO.PushPurchaseApplicationDTO> dtoList){
        List<BatchResultDTO> resultList = pilotApplicationService.pushPurchaseApplication(dtoList);
        return resultList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultList) : failure(resultList);
    }

    /**
     * 下推并提交采购申请
     * @author tmj
     */
    @PostMapping("/pushAndSubmitPurchaseApplication")
    public ApiResult<List<BatchResultDTO>> pushAndSubmitPurchaseApplication(@RequestBody @Validated List<PilotApplicationDTO.PushPurchaseApplicationDTO> dtoList){
        List<BatchResultDTO> resultList = pilotApplicationService.pushAndSubmitPurchaseApplication(dtoList);
        return resultList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultList) : failure(resultList);
    }

    /**
     * 采购申请预览
     * @author tmj
     */
    @PostMapping("/viewPurchaseApplication")
    public ApiResult<List<PilotApplicationDTO.PushPurchaseApplicationDTO>> viewPurchaseApplication(@RequestBody @Validated PilotApplicationDTO.PurchaseApplicationParamDTO paramDTO){
        List<PilotApplicationDTO.PushPurchaseApplicationDTO> list = pilotApplicationService.viewPurchaseApplication(paramDTO);
        return success(list);
    }

    /**
     * 查询目的仓库：启用+已审核
     * @author tmj
     */
    @PostMapping("/listWarehouse")
    public ApiResult<List<PilotApplicationDTO.WarehouseDTO>> listWarehouse(){
        List<PilotApplicationDTO.WarehouseDTO> list = pilotApplicationService.listWarehouse();
        return success(list);
    }

    /**
     * 查询已启用的核算公司
     * @author tmj
     */
    @PostMapping("/listPurchaseOrg")
    public ApiResult<List<BaseIdDTO>> listPurchaseOrg(){
        List<BaseIdDTO> list = pilotApplicationService.listPurchaseOrg();
        return success(list);
    }

    /**
     * 保存关联任务
     */
    @PostMapping("/addRefTaskBatch")
    public ApiResult<Boolean> addRefTaskBatch(@RequestBody PilotApplicationDTO.RefTaskDTO dto){
        return success(pilotApplicationService.addRefTaskBatch(dto));
    }

    /**
     * 删除关联任务
     */
    @PostMapping("/deleteRefTaskBatch")
    public ApiResult<Boolean> deleteRefTaskBatch(@RequestBody PilotApplicationDTO.RefTaskDTO dto){
        return success(pilotApplicationService.deleteRefTaskBatch(dto));
    }

    /**
     * 移除产品
     */
    @PostMapping("/deleteProductBatch")
    public ApiResult<Boolean> deleteProductBatch(@RequestBody PilotApplicationDTO.ProductDTO dto){
        return success(pilotApplicationService.deleteProductBatch(dto));
    }

    /**
     * 查询关联任务
     * @param id 试产单ID
     */
    @GetMapping("/listRefTask")
    public ApiResult<List<PilotApplicationRefTaskDTO.SimpleListDTO>> listRefTask(@RequestParam String id){
        return success(pilotApplicationService.listRefTask(id));
    }

    /**
     * 快粘贴：根据sku编号查询
     * @param
     * @return
     * @date: 2024-09-02
     * @author: tanmujin
     */
    @PostMapping("/listSkuBySkuNos")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "plm:product:detail:list", tableAlias = "pd")
    public ApiResult<List<ProductSearchDTO.SkuListDTO>> listSkuBySkuNos(@RequestBody ProductSearchDTO.SkuParamDTO skuParamDTO) {
        List<ProductSearchDTO.SkuListDTO> list = pilotApplicationService.listSkuBySkuNos(skuParamDTO);
        return this.success(list);
    }

    /**
     * 根据skuIds获取产品包装尺寸明细,过滤包装数据完整的数据
     * @param dto 参数
     */
    @PostMapping("/listProductPackBySkuIds")
    public ApiResult<List<ProductPackViewDTO>> listProductPackBySkuIds(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<ProductPackViewDTO> productPackViewDTOS = pilotApplicationService.listProductPackBySkuIds(dto.getIds());
        return success(productPackViewDTOS);
    }

    /**
     * 作废
     * @author zdy
     * @date 2025/04/15 11:29
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/invalid")
    @LogAction(value = LogActionEnum.INVALID, desc = "作废")
    public ApiResult<List<BatchResultDTO>> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds().stream().filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        for (String id : ids) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = pilotApplicationService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("试产量产单作废失败",e);
                PilotApplicationEntity entity = pilotApplicationService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "试产量产单不存在, 作废失败");
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
     * 取消作废
     *
     * @param dto
     * @return ApiResult<List < BatchResultDTO>>
     * @author zdy
     * @date: 2025/04/15 11:29
     */
    @PostMapping("/unInvalid")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "取消作废")
    public ApiResult<List<BatchResultDTO>> unInvalid(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds().stream().filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        for (String id : ids) {
            BatchResultDTO unInvalidResult;
            try {
                unInvalidResult = pilotApplicationService.unInvalid(id);
            } catch (Exception e) {
                log.error("试产量产单取消作废失败", e);
                PilotApplicationEntity entity = pilotApplicationService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    unInvalidResult = BatchResultDTO.fail(id, id, "试产量产单不存在, 取消作废失败");
                    resultDTOS.add(unInvalidResult);
                    continue;
                }
                unInvalidResult = BatchResultDTO.fail(id, entity.getCode(), e.getMessage());
            }
            resultDTOS.add(unInvalidResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    /**
     * 更新备注
     * @author Will
     * @date: 2023/7/19 14:58
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "更新试产量产单:明细备注={remark}")
    @PostMapping("/updateRemark")
    public ApiResult<?> updateRemark(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<PilotApplicationDetailEntity> detailList = pilotApplicationDetailService.listByIds(dto.getIds());
        Map<String, PilotApplicationDetailEntity> detailMap = detailList.stream().collect(Collectors.toMap(BaseEntity::getId, e -> e));
        List<String> mainIds = detailList.stream().map(PilotApplicationDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<PilotApplicationEntity> entityList = pilotApplicationService.listByIds(mainIds);
        Map<String, PilotApplicationEntity> entityMap = entityList.stream().collect(Collectors.toMap(BaseEntity::getId, e -> e));
        for (String id : dto.getIds()) {
            PilotApplicationDetailEntity detailEntity = detailMap.get(id);
            if(Objects.isNull(detailEntity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"试产量产单明细不存在"));
                continue;
            }
            PilotApplicationEntity entity = entityMap.get(detailEntity.getMainId());
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"试产量产单不存在"));
                continue;
            }
            try {
                BaseIdsDTO.RemarkDTO remarkDTO = new BaseIdsDTO.RemarkDTO();
                remarkDTO.setRemark(dto.getRemark());
                remarkDTO.setIds(Collections.singletonList(id));
                Boolean flag = pilotApplicationDetailService.updateRemark(remarkDTO);
                if (flag){
                    resultDTOS.add(BatchResultDTO.success(id, entity.getCode(), "修更新备注试产量产单成功"));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(id, entity.getCode(), "修更新备注试产量产单失败"));
                }
            }catch (Exception e){
                log.error("试产量产单提交失败",e);
                resultDTOS.add(BatchResultDTO.fail(id, entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
