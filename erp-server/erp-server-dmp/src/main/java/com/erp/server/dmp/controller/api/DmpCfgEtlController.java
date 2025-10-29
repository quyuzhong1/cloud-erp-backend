package com.erp.server.dmp.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.erp.model.dmp.dto.DmpCfgInputDetailDTO;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.model.dmp.entity.DmpCfgEtlEntity;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.query.DmpCfgEtlQueryHandler;
import com.erp.server.dmp.service.DmpBasicSystemService;
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
import com.erp.server.dmp.service.DmpCfgEtlService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpCfgEtlDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.dmp.entity.DmpCfgEtlEntity;

/**
 * 清洗调度
 *
 * @author shukai
 * @since 2025-07-21
 */
@Slf4j
@RestController
@LogSystemModule("清洗调度")
@RequestMapping("/dmpCfgEtl")
public class DmpCfgEtlController extends BaseController {

    @Resource
    private DmpCfgEtlService dmpCfgEtlService;

    /**
    * 新增
    * @author shukai
    * @date:  2025-07-21
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "清洗调度新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpCfgEtlDTO.AddDTO dto) {
        return success(dmpCfgEtlService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2025-07-21
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "清洗调度修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpCfgEtl:update",
        serviceClass = DmpCfgEtlService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpCfgEtlDTO.UpdateDTO dto) {
        dmpCfgEtlService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgEtl:paging",
            tableAlias = ""
    )
    public ApiResult<List<DmpCfgEtlDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(dmpCfgEtlService.tabList(dto));
    }

    /**
    * 列表查询
    * @author Jim
    * @date: 2025-10-23
    * @param dto
    * @return ApiResult<PagingVO<DmpCfgEtlDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgEtl:paging",
            tableAlias = ""
    )
    @WebAdvanceQuery(handler = DmpCfgEtlQueryHandler.class)
    public ApiResult<PagingVO<DmpCfgEtlDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<DmpCfgEtlDTO.PagingParamDTO> dto) {
        return success(dmpCfgEtlService.paging(dto));
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
            menuCode = "dmp:dmpCfgEtl:delete",
            serviceClass = DmpCfgEtlService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "清洗调度删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// 数据查询放入外层，处理结果统一更新或单条更新
		List<DmpCfgEtlEntity> list = dmpCfgEtlService.lambdaQuery().in(DmpCfgEtlEntity::getId, ids).list();
		Map<String, DmpCfgEtlEntity> idEntityMap = list.stream().collect(Collectors.toMap(DmpCfgEtlEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            DmpCfgEtlEntity entity = idEntityMap.get(id);
            try {
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "清洗调度不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = dmpCfgEtlService.delete(id);
            }catch (Exception e){
                log.error("清洗调度删除失败",e);

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
    @LogAction(value = LogActionEnum.UPDATE, desc = "批量禁用清洗调度", keyIdName = "ids")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgEtl:disabled",
            serviceClass = DmpCfgEtlService.class,
            keyIdName = "ids")
    public ApiResult<?> disabled(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<DmpCfgEtlEntity> list = dmpCfgEtlService.lambdaQuery().in(DmpCfgEtlEntity::getId, dto.getIds()).list();
        Map<String, DmpCfgEtlEntity> idEntityMap = list.stream().collect(Collectors.toMap(DmpCfgEtlEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            DmpCfgEtlEntity entity = idEntityMap.get(id);
            try {
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "清洗调度不存在, 批量禁用失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = dmpCfgEtlService.disable(entity);
            }catch (Exception e){
                log.error("清洗调度批量禁用失败",e);
                result = BatchResultDTO.fail(entity.getId(), entity.getExecUrl(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量启用
     */
    @PostMapping("/enable")
    @LogAction(value = LogActionEnum.UPDATE, desc = "批量启用清洗调度", keyIdName = "ids")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgEtl:enable",
            serviceClass = DmpCfgEtlService.class,
            keyIdName = "ids")
    public ApiResult<?> enable(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<DmpCfgEtlEntity> list = dmpCfgEtlService.lambdaQuery().in(DmpCfgEtlEntity::getId, dto.getIds()).list();
        Map<String, DmpCfgEtlEntity> idEntityMap = list.stream().collect(Collectors.toMap(DmpCfgEtlEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            DmpCfgEtlEntity entity = idEntityMap.get(id);
            try {
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "清洗调度不存在, 启用失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = dmpCfgEtlService.enable(entity);
            }catch (Exception e){
                log.error("清洗调度启用失败",e);
                result = BatchResultDTO.fail(entity.getId(), entity.getExecUrl(), e.getMessage());
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
    * @return ApiResult<DmpCfgEtlDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgEtl:view",
            serviceClass = DmpCfgEtlService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<DmpCfgEtlDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(dmpCfgEtlService.view(id));
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
            menuCode = "dmp:dmpCfgEtl:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "清洗调度导出Excel数据")
    public ApiResult<Boolean> exportList(@RequestBody @Validated DmpCfgEtlDTO.ExportDTO dto, HttpServletResponse response) {
        dmpCfgEtlService.exportList(dto, response);
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
            menuCode = "dmp:dmpCfgEtl:doTask",
            serviceClass = DmpCfgInputDetailService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INSERT, desc = "清洗调度生成任务")
    public ApiResult<List<BatchResultDTO>> createTask(@RequestBody @Validated DmpCfgEtlDTO.DoTaskDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        // 数据查询放入外层，处理结果统一更新或单条更新
        List<DmpCfgEtlEntity> list = dmpCfgEtlService.lambdaQuery().in(DmpCfgEtlEntity::getId, ids).list();
        Map<String, DmpCfgEtlEntity> idEntityMap = list.stream().collect(Collectors.toMap(DmpCfgEtlEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            DmpCfgEtlEntity entity = idEntityMap.get(id);
            if (ObjectUtil.isEmpty(entity)) {
                result = BatchResultDTO.fail(id, id, "清洗调度不存在, 删除失败");
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
                log.error("清洗调度生成任务失败",e);
                result = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
