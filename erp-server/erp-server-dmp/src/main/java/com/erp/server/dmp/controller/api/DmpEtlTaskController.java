package com.erp.server.dmp.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.erp.model.dmp.entity.DmpEtlTaskEntity;
import com.erp.server.dmp.query.DmpEtlTaskQueryHandler;
import com.erp.server.dmp.service.DmpInputTaskService;
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
import com.erp.server.dmp.service.DmpEtlTaskService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpEtlTaskDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.dmp.entity.DmpEtlTaskEntity;

/**
 * 清洗任务
 *
 * @author shukai
 * @since 2025-07-21
 */
@Slf4j
@RestController
@LogSystemModule("清洗任务")
@RequestMapping("/dmpEtlTask")
public class DmpEtlTaskController extends BaseController {

    @Resource
    private DmpEtlTaskService dmpEtlTaskService;

    /**
    * 新增
    * @author shukai
    * @date:  2025-07-21
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "清洗任务新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpEtlTaskDTO.AddDTO dto) {
        return success(dmpEtlTaskService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2025-07-21
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "清洗任务修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpEtlTask:update",
        serviceClass = DmpEtlTaskService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpEtlTaskDTO.UpdateDTO dto) {
        dmpEtlTaskService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpEtlTask:paging",
            tableAlias = ""
    )
    public ApiResult<List<DmpEtlTaskDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(dmpEtlTaskService.tabList(dto));
    }

    /**
    * 列表查询
    * @author Jim
    * @date: 2025-10-23
    * @param dto
    * @return ApiResult<PagingVO<DmpEtlTaskDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpEtlTask:paging",
            tableAlias = ""
    )
    @WebAdvanceQuery(handler = DmpEtlTaskQueryHandler.class)
    public ApiResult<PagingVO<DmpEtlTaskDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<DmpEtlTaskDTO.PagingParamDTO> dto) {
        return success(dmpEtlTaskService.paging(dto));
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
            menuCode = "dmp:dmpEtlTask:delete",
            serviceClass = DmpEtlTaskService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "清洗任务删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<DmpEtlTaskEntity> list = dmpEtlTaskService.lambdaQuery().in(DmpEtlTaskEntity::getId, ids).list();
		Map<String, DmpEtlTaskEntity> idEntityMap = list.stream().collect(Collectors.toMap(DmpEtlTaskEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = dmpEtlTaskService.delete(id);
            }catch (Exception e){
                log.error("清洗任务删除失败",e);
                DmpEtlTaskEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "清洗任务不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    

    /**
    * 详情
    * @author Jim
    * @date:  2025-10-23
    * @param id
    * @return ApiResult<DmpEtlTaskDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpEtlTask:view",
            serviceClass = DmpEtlTaskService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<DmpEtlTaskDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(dmpEtlTaskService.view(id));
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
            menuCode = "dmp:dmpEtlTask:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "清洗任务导出Excel数据")
    public ApiResult<Boolean> exportList(@RequestBody @Validated DmpEtlTaskDTO.ExportDTO dto, HttpServletResponse response) {
        dmpEtlTaskService.exportList(dto, response);
        return success(true);
    }

    /**
     * 重试
     * @author Jim
     * @date:  2025-10-23
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/retry")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpEtlTask:retry",
            serviceClass = DmpInputTaskService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.UPDATE, desc = "清洗任务重试")
    public ApiResult<List<BatchResultDTO>> batchRetry(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        // 数据查询放入外层，处理结果统一更新或单条更新
        List<DmpEtlTaskEntity> list = dmpEtlTaskService.lambdaQuery().in(DmpEtlTaskEntity::getId, ids).list();
        Map<String, DmpEtlTaskEntity> idEntityMap = list.stream().collect(Collectors.toMap(DmpEtlTaskEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            DmpEtlTaskEntity entity = idEntityMap.get(id);
            try {
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "清洗任务不存在, 重试失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = dmpEtlTaskService.retry(entity);
            }catch (Exception e){
                log.error("清洗任务重试失败",e);
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
