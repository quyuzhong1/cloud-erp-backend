package com.erp.server.wms.controller.api;


import cn.hutool.core.text.CharSequenceUtil;
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
import com.erp.model.tms.entity.TmsFirstMileReconciliationEntity;
import com.erp.model.wms.dto.MachineInfoDTO;
import com.erp.model.wms.dto.MachineSubComponentsDTO;
import com.erp.model.wms.entity.MachineInfoEntity;
import com.erp.server.wms.query.MachineInfoQueryHandler;
import com.erp.server.wms.service.MachineInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.lang.reflect.Array;
import java.util.*;

/**
 * 加工单 
 *
 * @author will
 * @since 2023-05-10
 */
@Slf4j
@RestController
@LogSystemModule("加工单")
@RequestMapping("/machineInfo")
public class MachineInfoController extends BaseController {

    @Resource
    private MachineInfoService machineInfoService;


    /**
     * 列表查询
     * @author Will
     * @date: 2023/5/10 19:56
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id",
            warehouseTableField = "mi.warehouse_id",
            menuCode = "wms:machineInfo:paging",
            tableAlias = "mi"
    )
    @WebAdvanceQuery(handler = MachineInfoQueryHandler.class)
    public ApiResult<PagingVO<MachineInfoDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<MachineInfoDTO.SearchParamDTO> dto) {
        PagingVO<MachineInfoDTO.ListDTO> pagingVO = machineInfoService.paging(dto);
        return success(pagingVO);
    }
    /**
     * 列表数量
     * @author Will
     * @date: 2023/5/10 20:08
     * @param dto
     * @return ApiResult<List<ListStatusCountDTO>>
     */
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id",
            warehouseTableField = "mi.warehouse_id",
            menuCode = "wms:machineInfo:paging",
            tableAlias = "mi"
    )
    public ApiResult<List<MachineInfoDTO.ListStatusCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<MachineInfoDTO.ListStatusCountDTO> list = machineInfoService.listCount(dto);
        return success(list);
    }

    /**
     * 新增
     * @author Will
     * @date: 2023/5/10 19:58
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增加工单")
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:machineInfo:add",
            serviceClass = MachineInfoService.class,
            keyIdName = "id")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated MachineInfoDTO.AddDTO dto) {
        MachineInfoEntity entity = machineInfoService.add(dto);
        return success(new BaseResultDTO.AddDTO(entity.getId(), entity.getCode()));
    }

    /**
     * 新增并提交
     * @author Will
     * @date: 2023/5/10 19:59
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交加工单")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:machineInfo:add",
            serviceClass = MachineInfoService.class,
            keyIdName = "id")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated MachineInfoDTO.AddDTO dto) {
        MachineInfoEntity entity = machineInfoService.addAndSubmit(dto);
        return success(new BaseResultDTO.AddDTO(entity.getId(), entity.getCode()));
    }

    /**
     * 修改
     * @author Will
     * @date: 2023/5/10 20:02
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改加工单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:machineInfo:update",
            serviceClass = MachineInfoService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated MachineInfoDTO.UpdateDTO dto) {
        Boolean flag = machineInfoService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改并提交
     * @author Will
     * @date: 2023/5/10 20:02
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交加工单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:machineInfo:update",
            serviceClass = MachineInfoService.class,
            keyIdName = "id")
    public ApiResult<?> updateAndSubmit(@RequestBody @Validated MachineInfoDTO.UpdateDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(1);
        String id = dto.getId();
        BatchResultDTO submit;
            try {
                submit =  machineInfoService.updateAndSubmit(dto);
            } catch (Exception e) {
                log.error("修改并提交加工单失败", e);
                MachineInfoEntity entity = machineInfoService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "提交加工单不存在, 修改并提交加工单");
                    resultDTOS.add(submit);
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 提交
     * @author Will
     * @date: 2023/5/10 20:00
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交加工单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:machineInfo:submit",
            serviceClass = MachineInfoService.class,
            keyIdName = "ids")
    public ApiResult<?> submit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, MachineInfoEntity> entityMap = machineInfoService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            MachineInfoEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"加工单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(machineInfoService.submitEntity(entity));
            }catch (Exception e){
                log.error("加工单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 查看详情
     * @author Will
     * @date: 2023/5/10 20:10
     * @param id
     * @return ApiResult
     */
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:machineInfo:view",
            serviceClass = MachineInfoService.class,
            keyIdName = "id")
    public ApiResult<MachineInfoDTO.ViewDTO> view(@RequestParam("id") String id) {
        MachineInfoDTO.ViewDTO dto = machineInfoService.view(id);
        return success(dto);
    }

    /**
     * 根据明细id查询子件明细数据
     * @author Will
     * @date: 2023/5/16 19:40
     * @param detailId
     * @return ApiResult<List<ViewDTO>>
     */
    @GetMapping("/viewSubComponents")
    public ApiResult<List<MachineSubComponentsDTO.ViewDTO>> viewSubComponents(@RequestParam("detailId") String detailId) {
        List<MachineSubComponentsDTO.ViewDTO> dto = machineInfoService.viewSubComponents(detailId);
        return success(dto);
    }
    
    /**
     * 通过SKU查询BOM子集
     * @author Will
     * @date: 2023/5/16 19:40
     * @param dto
     * @return ApiResult<List<ViewDTO>> 
     */
    @PostMapping("/viewBomSubComponents")
    public ApiResult<List<MachineSubComponentsDTO.ViewDTO>> viewBomSubComponents(@RequestBody @Validated MachineSubComponentsDTO.ViewBomParamDTO dto) {
        List<MachineSubComponentsDTO.ViewDTO> list = machineInfoService.viewBomSubComponents(dto);
        return success(list);
    }


    /**
     * 删除
     * @author Will
     * @date: 2023/5/10 20:09
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除加工单")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:machineInfo:delete",
            serviceClass = MachineInfoService.class,
            keyIdName = "ids")
    public ApiResult<?> delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, MachineInfoEntity> entityMap = machineInfoService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            MachineInfoEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"加工单记录不存在"));
                continue;
            }
            try {
                Boolean flag = machineInfoService.delete(Collections.singletonList(id));
                if (flag){
                    resultDTOS.add(BatchResultDTO.success(entity.getId(), entity.getCode(), "删除成功"));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), "删除失败"));
                }
            }catch (Exception e){
                log.error("加工单删除失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 作废
     * @author Will
     * @date: 2023/5/10 20:11
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INVALID, desc = "作废加工单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:machineInfo:invalid",
            serviceClass = MachineInfoService.class,
            keyIdName = "ids")
    public ApiResult<?> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, MachineInfoEntity> entityMap = machineInfoService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            MachineInfoEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"加工单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(machineInfoService.invalid(id, dto.getRemark()));
            }catch (Exception e){
                log.error("加工单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量审核
     * @author Will
     * @date: 2023/5/10 20:11
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核加工单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:machineInfo:approve",
            serviceClass = MachineInfoService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<MachineInfoEntity> entityList = machineInfoService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            MachineInfoEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"加工单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(machineInfoService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess()));
            }catch (Exception e){
                log.error("加工单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量反审核
     * @author Will
     * @date: 2023/5/10 20:12
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核加工单")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:machineInfo:disApprove",
            serviceClass = MachineInfoService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<MachineInfoEntity> entityList = machineInfoService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            MachineInfoEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"加工单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(machineInfoService.disApprove(entity));
            }catch (Exception e){
                log.error("加工单反审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 取消流程
     * @author Will
     * @date: 2023/5/10 20:24
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销加工单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:machineInfo:cancelProcess",
            serviceClass = MachineInfoService.class,
            keyIdName = "ids")
    public ApiResult<?> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<MachineInfoEntity> entityList = machineInfoService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            MachineInfoEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"加工单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(machineInfoService.cancelProcessEntity(entity));
            }catch (Exception e){
                log.error("加工单撤销失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 导出
     * @author Will
     * @date: 2023/5/10 20:25
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出加工单")
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody MachineInfoDTO.SearchParamDTO dto) {
        Boolean flag = machineInfoService.exportExcel(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 处理数据
     * @author will
     * @date 2024/11/27 11:02
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/handleErrorData")
    public ApiResult<List<BatchResultDTO>> handleErrorData(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<String> ids = dto.getIds();
        for (String id : ids) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = machineInfoService.handleErrorData(id);
            } catch (Exception e) {
                log.error("加工单 处理数据失败", e);
                MachineInfoEntity entity = machineInfoService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "加工单不存在, 处理数据失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
