package com.erp.server.plm.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.FindUserDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.erp.model.plm.dto.*;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.query.PilotApplicationQueryHandler;
import com.erp.server.plm.service.ProductDetailService;
import com.sdk.wangdian.sdk.impl.Api;
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
import com.erp.server.plm.service.PilotApplicationService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.plm.entity.PilotApplicationEntity;

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
    private SysUserFeign sysUserFeign;
    @Resource
    private ProductDetailService productDetailService;

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
    public ApiResult<?> update(@RequestBody @Validated PilotApplicationDTO.UpdateDTO dto) {
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
            tableAlias = ""
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
            tableAlias = ""
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
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated PilotApplicationDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = pilotApplicationService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author tmj
    * @date:  2024-08-27
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:pilotApplication:updateAndSubmit",
            serviceClass = PilotApplicationService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated PilotApplicationDTO.UpdateDTO dto) {
        pilotApplicationService.updateAndSubmit(dto);
        return success();
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
                cancelResult = pilotApplicationService.cancelProcess(id);
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
    public ApiResult<?> exportList(@RequestBody @Validated PilotApplicationDTO.ExportDTO dto) {
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
}
