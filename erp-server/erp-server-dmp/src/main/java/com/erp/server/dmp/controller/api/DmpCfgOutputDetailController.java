package com.erp.server.dmp.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.enums.OperationTypeEnum;
import com.erp.model.dmp.dto.DmpCfgInputDetailDTO;
import com.erp.model.dmp.entity.DmpCfgOutputDetailEntity;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.query.DmpCfgInputDetailQueryHandler;
import com.erp.server.dmp.service.DmpCfgInputDetailService;
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
import com.erp.server.dmp.service.DmpCfgOutputDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpCfgOutputDetailDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.dmp.entity.DmpCfgOutputDetailEntity;

/**
 * 推送调度
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@RestController
@LogSystemModule("推送调度")
@RequestMapping("/dmpCfgOutputDetail")
public class DmpCfgOutputDetailController extends BaseController {

    @Resource
    private DmpCfgOutputDetailService dmpCfgOutputDetailService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "推送调度新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpCfgOutputDetailDTO.AddDTO dto) {
        return success(dmpCfgOutputDetailService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "推送调度修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpCfgOutputDetail:update",
        serviceClass = DmpCfgOutputDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpCfgOutputDetailDTO.UpdateDTO dto) {
        dmpCfgOutputDetailService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgOutputDetail:paging",
            tableAlias = ""
    )
    public ApiResult<List<DmpCfgOutputDetailDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(dmpCfgOutputDetailService.tabList(dto));
    }

    /**
    * 列表查询
    * @author Jim
    * @date: 2025-10-23
    * @param dto
    * @return ApiResult<PagingVO<DmpCfgOutputDetailDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgOutputDetail:paging",
            tableAlias = ""
    )
    @WebAdvanceQuery(handler = DmpCfgInputDetailQueryHandler.class)
    public ApiResult<PagingVO<DmpCfgOutputDetailDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<DmpCfgOutputDetailDTO.PagingParamDTO> dto) {
        return success(dmpCfgOutputDetailService.paging(dto));
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
            menuCode = "dmp:dmpCfgOutputDetail:delete",
            serviceClass = DmpCfgOutputDetailService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "推送调度删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// 数据查询放入外层，处理结果统一更新或单条更新
		List<DmpCfgOutputDetailEntity> list = dmpCfgOutputDetailService.lambdaQuery().in(DmpCfgOutputDetailEntity::getId, ids).list();
		Map<String, DmpCfgOutputDetailEntity> idEntityMap = list.stream().collect(Collectors.toMap(DmpCfgOutputDetailEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            DmpCfgOutputDetailEntity entity = idEntityMap.get(id);
            try {
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "推送调度不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = dmpCfgOutputDetailService.delete(id);
            }catch (Exception e){
                log.error("推送调度删除失败",e);
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
    @LogAction(value = LogActionEnum.UPDATE, desc = "批量禁用推送调度", keyIdName = "ids")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgOutputDetail:disabled",
            serviceClass = DmpCfgOutputDetailService.class,
            keyIdName = "ids")
    public ApiResult<?> disabled(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<DmpCfgOutputDetailEntity> list = dmpCfgOutputDetailService.lambdaQuery().in(DmpCfgOutputDetailEntity::getId, dto.getIds()).list();
        Map<String, DmpCfgOutputDetailEntity> idEntityMap = list.stream().collect(Collectors.toMap(DmpCfgOutputDetailEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            DmpCfgOutputDetailEntity entity = idEntityMap.get(id);
            try {
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "推送调度不存在, 批量禁用失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = dmpCfgOutputDetailService.disable(entity);
            }catch (Exception e){
                log.error("推送调度批量禁用失败",e);
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
    @LogAction(value = LogActionEnum.UPDATE, desc = "批量启用推送调度", keyIdName = "ids")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgOutputDetail:enable",
            serviceClass = DmpCfgOutputDetailService.class,
            keyIdName = "ids")
    public ApiResult<?> enable(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<DmpCfgOutputDetailEntity> list = dmpCfgOutputDetailService.lambdaQuery().in(DmpCfgOutputDetailEntity::getId, dto.getIds()).list();
        Map<String, DmpCfgOutputDetailEntity> idEntityMap = list.stream().collect(Collectors.toMap(DmpCfgOutputDetailEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            DmpCfgOutputDetailEntity entity = idEntityMap.get(id);
            try {
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "推送调度不存在, 启用失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = dmpCfgOutputDetailService.enable(entity);
            }catch (Exception e){
                log.error("推送调度启用失败",e);
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
    * @return ApiResult<DmpCfgOutputDetailDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgOutputDetail:view",
            serviceClass = DmpCfgOutputDetailService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<DmpCfgOutputDetailDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(dmpCfgOutputDetailService.view(id));
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
            menuCode = "dmp:dmpCfgOutputDetail:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "推送调度导出Excel数据")
    public ApiResult<Boolean> exportList(@RequestBody @Validated DmpCfgOutputDetailDTO.ExportDTO dto, HttpServletResponse response) {
        dmpCfgOutputDetailService.exportList(dto, response);
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
            menuCode = "dmp:dmpCfgOutputDetail:doTask",
            serviceClass = DmpCfgInputDetailService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INSERT, desc = "推送调度生成任务")
    public ApiResult<List<BatchResultDTO>> createTask(@RequestBody @Validated DmpCfgOutputDetailDTO.DoTaskDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        // 数据查询放入外层，处理结果统一更新或单条更新
        List<DmpCfgOutputDetailEntity> list = dmpCfgOutputDetailService.lambdaQuery().in(DmpCfgOutputDetailEntity::getId, ids).list();
        Map<String, DmpCfgOutputDetailEntity> idEntityMap = list.stream().collect(Collectors.toMap(DmpCfgOutputDetailEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            DmpCfgOutputDetailEntity entity = idEntityMap.get(id);
            if (ObjectUtil.isEmpty(entity)) {
                result = BatchResultDTO.fail(id, id, "推送调度不存在, 删除失败");
                resultDTOS.add(result);
                continue;
            }
            try {
                // TODO
                DmpInputHotfixCreateRequest dmpInputHotfixCreateRequest = new DmpInputHotfixCreateRequest();
//                dmpInputHotfixCreateRequest.setCfgInputId("1938157629872296175");
//                dmpInputCreateFactory.doHotfixInputTask(dmpInputHotfixCreateRequest);
                result = new BatchResultDTO();
            }catch (Exception e){
                log.error("推送调度生成任务失败",e);
                result = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
