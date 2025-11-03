package com.erp.server.dmp.controller.api;


import cn.hutool.core.collection.CollectionUtil;
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
import com.common.core.entity.BaseEntity;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.DmpCfgInputDetailDTO;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.enums.DmpCfgInputExecSystemEnum;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.inout.handler.factory.DmpInputCreateFactory;
import com.erp.server.dmp.query.DmpCfgOutputDetailQueryHandler;
import com.erp.server.dmp.service.DmpCfgInputDetailService;
import com.erp.server.dmp.service.DmpCfgInputService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 拉取调度
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@RestController
@LogSystemModule("拉取调度")
@RequestMapping("/dmpCfgInputDetail")
public class DmpCfgInputDetailController extends BaseController {

    @Resource
    private DmpCfgInputDetailService dmpCfgInputDetailService;
    @Resource
    private DmpCfgInputService dmpCfgInputService;
    @Resource
    private DmpInputCreateFactory dmpInputCreateFactory;

    /**
    * 新增
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "拉取调度新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpCfgInputDetailDTO.AddDTO dto) {
        return success(dmpCfgInputDetailService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "拉取调度修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpCfgInputDetail:update",
        serviceClass = DmpCfgInputDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpCfgInputDetailDTO.UpdateDTO dto) {
        dmpCfgInputDetailService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgInputDetail:paging",
            tableAlias = ""
    )
    public ApiResult<List<DmpCfgInputDetailDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(dmpCfgInputDetailService.tabList(dto));
    }

    /**
    * 列表查询
    * @author Jim
    * @date: 2025-10-23
    * @param dto
    * @return ApiResult<PagingVO<DmpCfgInputDetailDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgInputDetail:paging",
            tableAlias = ""
    )
    @WebAdvanceQuery(handler = DmpCfgOutputDetailQueryHandler.class)
    public ApiResult<PagingVO<DmpCfgInputDetailDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<DmpCfgInputDetailDTO.PagingParamDTO> dto) {
        return success(dmpCfgInputDetailService.paging(dto));
    }


    /**
    * 删除
    * @author Jim
    * @date:  2025-10-23
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgInputDetail:delete",
            serviceClass = DmpCfgInputDetailService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "拉取调度删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// 数据查询放入外层，处理结果统一更新或单条更新
		List<DmpCfgInputDetailEntity> list = dmpCfgInputDetailService.lambdaQuery().in(DmpCfgInputDetailEntity::getId, ids).list();
		Map<String, DmpCfgInputDetailEntity> idEntityMap = list.stream().collect(Collectors.toMap(DmpCfgInputDetailEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            DmpCfgInputDetailEntity entity = idEntityMap.get(id);
            if (ObjectUtil.isEmpty(entity)) {
                deleteResult = BatchResultDTO.fail(id, id, "拉取调度不存在, 删除失败");
                resultDTOS.add(deleteResult);
                continue;
            }
            try {
                deleteResult = dmpCfgInputDetailService.delete(id);
            }catch (Exception e){
                log.error("拉取调度删除失败",e);
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 批量禁用
     */
    @PostMapping("/disabled")
    @LogAction(value = LogActionEnum.UPDATE, desc = "批量禁用拉取调度", keyIdName = "ids")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgInputDetail:disabled",
            serviceClass = DmpCfgInputDetailService.class,
            keyIdName = "ids")
    public ApiResult<?> disabled(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<DmpCfgInputDetailEntity> list = dmpCfgInputDetailService.lambdaQuery().in(DmpCfgInputDetailEntity::getId, dto.getIds()).list();
        Map<String, DmpCfgInputDetailEntity> idEntityMap = list.stream().collect(Collectors.toMap(DmpCfgInputDetailEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            DmpCfgInputDetailEntity entity = idEntityMap.get(id);
            try {
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "拉取调度不存在, 批量禁用失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = dmpCfgInputDetailService.disable(entity);
            }catch (Exception e){
                log.error("拉取调度批量禁用失败",e);
                result = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量启用
     */
    @PostMapping("/enable")
    @LogAction(value = LogActionEnum.UPDATE, desc = "批量启用拉取调度", keyIdName = "ids")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgInputDetail:enable",
            serviceClass = DmpCfgInputDetailService.class,
            keyIdName = "ids")
    public ApiResult<?> enable(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<DmpCfgInputDetailEntity> list = dmpCfgInputDetailService.lambdaQuery().in(DmpCfgInputDetailEntity::getId, dto.getIds()).list();
        Map<String, DmpCfgInputDetailEntity> idEntityMap = list.stream().collect(Collectors.toMap(DmpCfgInputDetailEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            DmpCfgInputDetailEntity entity = idEntityMap.get(id);
            try {
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "拉取调度不存在, 启用失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = dmpCfgInputDetailService.enable(entity);
            }catch (Exception e){
                log.error("拉取调度启用失败",e);
                result = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
    * 详情
    * @author Jim
    * @date:  2025-10-23
    * @param id
    * @return ApiResult<DmpCfgInputDetailDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgInputDetail:view",
            serviceClass = DmpCfgInputDetailService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<DmpCfgInputDetailDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(dmpCfgInputDetailService.view(id));
    }

    /**
    * 导出Excel数据
    * @author Jim
    * @date:  2025-10-23
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgInputDetail:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "拉取调度导出Excel数据")
    public ApiResult<Boolean> exportList(@RequestBody @Validated DmpCfgInputDetailDTO.ExportDTO dto, HttpServletResponse response) {
        dmpCfgInputDetailService.exportList(dto, response);
        return success(true);
    }

    /**
     * 生成任务
     * @author Jim
     * @date:  2025-10-23
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/doTask")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgInputDetail:doTask",
            serviceClass = DmpCfgInputDetailService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INSERT, desc = "拉取调度生成任务")
    public ApiResult<List<BatchResultDTO>> doTask(@RequestBody @Validated DmpCfgInputDetailDTO.DoTaskDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        // 数据查询放入外层，处理结果统一更新或单条更新
        List<DmpCfgInputDetailEntity> list = dmpCfgInputDetailService.lambdaQuery().in(DmpCfgInputDetailEntity::getId, ids).list();
        Map<String, DmpCfgInputDetailEntity> idEntityMap = list.stream().collect(Collectors.toMap(DmpCfgInputDetailEntity::getId, w -> w));
        List<String> mainIds = list.stream().map(DmpCfgInputDetailEntity::getMainId).distinct().collect(Collectors.toList());
        Map<String, DmpCfgInputEntity> mainIdEntityMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(mainIds)){
            List<DmpCfgInputEntity> mainList = dmpCfgInputService.lambdaQuery().in(DmpCfgInputEntity::getId, mainIds).list();
            mainIdEntityMap = mainList.stream().collect(Collectors.toMap(DmpCfgInputEntity::getId, w -> w));
        }
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            DmpCfgInputDetailEntity entity = idEntityMap.get(id);
            if (ObjectUtil.isEmpty(entity)) {
                result = BatchResultDTO.fail(id, id, "拉取调度不存在, 删除失败");
                resultDTOS.add(result);
                continue;
            }
            DmpCfgInputEntity dmpCfgInputEntity = mainIdEntityMap.get(entity.getMainId());
            if (ObjectUtil.isEmpty(dmpCfgInputEntity)) {
                result = BatchResultDTO.fail(id, id, "拉取调度配置不存在, 生成拉取任务失败");
                resultDTOS.add(result);
                continue;
            }
            try {
                if (DmpCfgInputExecSystemEnum.DMP.getCode().equals(dmpCfgInputEntity.getExecSystem())){
                    // 中台执行
                    DmpInputHotfixCreateRequest dmpInputHotfixCreateRequest = buildDmpInputHotfixCreateRequest(dto, dmpCfgInputEntity, entity);
                    dmpInputCreateFactory.doHotfixInputTask(dmpInputHotfixCreateRequest);
                } else if (DmpCfgInputExecSystemEnum.REST_CLOUD.getCode().equals(dmpCfgInputEntity.getExecSystem())){
                    // RestCloud执行
                    boolean restCloudCanRun = Arrays.asList(DmpInputTaskTaskTypeEnum.NORMAL.getCode(), DmpInputTaskTaskTypeEnum.HISTORY.getCode()).contains(dto.getTaskType());
                    if (!restCloudCanRun){
                        result = BatchResultDTO.fail(id, id, "RestCloud执行系统只支持普通任务和历史任务，当前任务类型：" + DmpInputTaskTaskTypeEnum.getName(dto.getTaskType()));
                        resultDTOS.add(result);
                        continue;
                    }
                    DmpInputHotfixCreateRequest dmpInputHotfixCreateRequest = buildDmpInputHotfixCreateRequest(dto, dmpCfgInputEntity, entity);
                    dmpInputCreateFactory.createHotfixInputTask(dmpInputHotfixCreateRequest);
                } else {
                    ServiceException.runError("不支持的执行系统类型：" + dmpCfgInputEntity.getExecSystem());
                }
                result = BatchResultDTO.success(id, id, "生成拉取任务成功");
            }catch (Exception e){
                log.error("拉取调度生成任务失败",e);
                result = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    private static DmpInputHotfixCreateRequest buildDmpInputHotfixCreateRequest(DmpCfgInputDetailDTO.DoTaskDTO dto,
                                                                                DmpCfgInputEntity dmpCfgInputEntity,
                                                                                DmpCfgInputDetailEntity entity) {
        DmpInputHotfixCreateRequest dmpInputHotfixCreateRequest = new DmpInputHotfixCreateRequest();
        dmpInputHotfixCreateRequest.setCfgInputId(dmpCfgInputEntity.getId());
        dmpInputHotfixCreateRequest.setCfgInputDetailIdList(Collections.singletonList(entity.getId()));
        dmpInputHotfixCreateRequest.setTaskType(dto.getTaskType());
        dmpInputHotfixCreateRequest.setDetailExtendJson(dto.getCheckAndDetailExtendJson());
        dmpInputHotfixCreateRequest.setStartTime(dto.getStartTime());
        dmpInputHotfixCreateRequest.setEndTime(dto.getEndTime());
        dmpInputHotfixCreateRequest.setExecTimeout(null == entity.getExecTimeout() ? dto.getExecTimeout() : entity.getExecTimeout());
        dmpInputHotfixCreateRequest.setSplitFlag(dto.isSplitFlag());
        dmpInputHotfixCreateRequest.setNextExecTime(dto.getNextExecTime());
        return dmpInputHotfixCreateRequest;
    }
}
