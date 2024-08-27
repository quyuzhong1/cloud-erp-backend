package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.PackingTaskDTO;
import com.erp.model.wms.dto.WmsCartonSpecDTO;
import com.erp.model.wms.dto.OverseasWarehouseInboundDTO;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.model.wms.entity.PackingTaskEntity;
import com.erp.server.wms.query.FirstMileDeliveryQueryHandler;
import com.erp.server.wms.service.FirstMileDeliveryDetailService;
import com.erp.server.wms.service.PackingTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.FirstMileDeliveryService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.function.Function;
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

    @Autowired
    private FirstMileDeliveryService firstMileDeliveryService;

    @Autowired
    private FirstMileDeliveryDetailService firstMileDeliveryDetailService;

    @Resource
    private PackingTaskService packingTaskService;

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
    public ApiResult update(@RequestBody @Validated FirstMileDeliveryDTO.UpdateDTO dto) {
        firstMileDeliveryService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
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
            menuCode = "wms:fbaDelivery:paging",
            tableAlias = "fd"
    )
    @WebAdvanceQuery(handler = FirstMileDeliveryQueryHandler.class)
    public ApiResult<PagingVO<FirstMileDeliveryDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<FirstMileDeliveryDTO.PagingParamDTO> dto) {
        return success(firstMileDeliveryService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author Luo_WG
    * @date:  2023-10-30
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated FirstMileDeliveryDTO.AddDTO dto) {
        BaseResultDTO.AddDTO addDTO = firstMileDeliveryService.addAndSubmit(dto);
        return success(addDTO);
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
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated FirstMileDeliveryDTO.UpdateDTO dto) {
        firstMileDeliveryService.updateAndSubmit(dto);
        return success();
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
                submit = firstMileDeliveryService.submit(id);
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
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = firstMileDeliveryService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
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
                cancelResult = firstMileDeliveryService.cancelProcess(id);
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
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:fbaDelivery:export",
            tableAlias = "fd"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "头程发货单导出Excel数据")
    @WebAdvanceQuery(handler = FirstMileDeliveryQueryHandler.class)
    public void exportList(@RequestBody @Validated FirstMileDeliveryDTO.PagingParamDTO dto, HttpServletResponse response) {
        firstMileDeliveryService.exportList(dto, response);
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
}
