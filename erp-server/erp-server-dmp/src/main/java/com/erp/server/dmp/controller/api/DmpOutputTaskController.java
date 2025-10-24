package com.erp.server.dmp.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.erp.server.dmp.query.DmpOutputTaskQueryHandler;
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
import com.erp.server.dmp.service.DmpOutputTaskService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpOutputTaskDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;

/**
 * 推送任务
 *
 * @author shukai
 * @since 2024-07-01
 */
@Slf4j
@RestController
@LogSystemModule("推送任务")
@RequestMapping("/dmpOutputTask")
public class DmpOutputTaskController extends BaseController {

    @Resource
    private DmpOutputTaskService dmpOutputTaskService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-07-01
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "推送任务新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpOutputTaskDTO.AddDTO dto) {
        return success(dmpOutputTaskService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-07-01
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "推送任务修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpOutputTask:update",
        serviceClass = DmpOutputTaskService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpOutputTaskDTO.UpdateDTO dto) {
        dmpOutputTaskService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpOutputTask:paging",
            tableAlias = ""
    )
    public ApiResult<List<DmpOutputTaskDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(dmpOutputTaskService.tabList(dto));
    }

    /**
    * 列表查询
    * @author Jim
    * @date: 2025-10-23
    * @param dto
    * @return ApiResult<PagingVO<DmpOutputTaskDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpOutputTask:paging",
            tableAlias = ""
    )
    @WebAdvanceQuery(handler = DmpOutputTaskQueryHandler.class)
    public ApiResult<PagingVO<DmpOutputTaskDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<DmpOutputTaskDTO.PagingParamDTO> dto) {
        return success(dmpOutputTaskService.paging(dto));
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
            menuCode = "dmp:dmpOutputTask:delete",
            serviceClass = DmpOutputTaskService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "推送任务删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// 数据查询放入外层，处理结果统一更新或单条更新
		List<DmpOutputTaskEntity> list = dmpOutputTaskService.lambdaQuery().in(DmpOutputTaskEntity::getId, ids).list();
		Map<String, DmpOutputTaskEntity> idEntityMap = list.stream().collect(Collectors.toMap(DmpOutputTaskEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            DmpOutputTaskEntity entity = idEntityMap.get(id);
            try {
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "推送任务不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = dmpOutputTaskService.delete(id);
            }catch (Exception e){
                log.error("推送任务删除失败",e);
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
    * @return ApiResult<DmpOutputTaskDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpOutputTask:view",
            serviceClass = DmpOutputTaskService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<DmpOutputTaskDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(dmpOutputTaskService.view(id));
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
            menuCode = "dmp:dmpOutputTask:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "推送任务导出Excel数据")
    public void exportList(@RequestBody @Validated DmpOutputTaskDTO.ExportDTO dto, HttpServletResponse response) {
        dmpOutputTaskService.exportList(dto, response);
    }


}
