package com.erp.server.dmp.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.erp.server.dmp.query.DmpInputTaskQueryHandler;
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
import com.erp.server.dmp.service.DmpInputTaskService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpInputTaskDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.dmp.entity.DmpInputTaskEntity;

/**
 * 拉取任务
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@RestController
@LogSystemModule("拉取任务")
@RequestMapping("/dmpInputTask")
public class DmpInputTaskController extends BaseController {

    @Resource
    private DmpInputTaskService dmpInputTaskService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "拉取任务新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpInputTaskDTO.AddDTO dto) {
        return success(dmpInputTaskService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "拉取任务修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpInputTask:update",
        serviceClass = DmpInputTaskService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpInputTaskDTO.UpdateDTO dto) {
        dmpInputTaskService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpInputTask:paging",
            tableAlias = ""
    )
    public ApiResult<List<DmpInputTaskDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(dmpInputTaskService.tabList(dto));
    }

    /**
    * 列表查询
    * @author Jim
    * @date: 2025-10-23
    * @param dto
    * @return ApiResult<PagingVO<DmpInputTaskDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpInputTask:paging",
            tableAlias = ""
    )
    @WebAdvanceQuery(handler = DmpInputTaskQueryHandler.class)
    public ApiResult<PagingVO<DmpInputTaskDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<DmpInputTaskDTO.PagingParamDTO> dto) {
        return success(dmpInputTaskService.paging(dto));
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
            menuCode = "dmp:dmpInputTask:delete",
            serviceClass = DmpInputTaskService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "拉取任务删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery().in(DmpInputTaskEntity::getId, ids).list();
		Map<String, DmpInputTaskEntity> idEntityMap = list.stream().collect(Collectors.toMap(DmpInputTaskEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = dmpInputTaskService.delete(id);
            }catch (Exception e){
                log.error("拉取任务删除失败",e);
                DmpInputTaskEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "拉取任务不存在, 删除失败");
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
    * @return ApiResult<DmpInputTaskDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpInputTask:view",
            serviceClass = DmpInputTaskService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<DmpInputTaskDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(dmpInputTaskService.view(id));
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
            menuCode = "dmp:dmpInputTask:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "拉取任务导出Excel数据")
    public void exportList(@RequestBody @Validated DmpInputTaskDTO.ExportDTO dto, HttpServletResponse response) {
        dmpInputTaskService.exportList(dto, response);
    }

}
