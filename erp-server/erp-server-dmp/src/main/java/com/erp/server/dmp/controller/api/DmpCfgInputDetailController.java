package com.erp.server.dmp.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.AdvanceQueryDTO;
import com.erp.server.dmp.query.DmpCfgOutputDetailQueryHandler;
import lombok.Data;
import lombok.NoArgsConstructor;
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
import com.erp.server.dmp.service.DmpCfgInputDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpCfgInputDetailDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;

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

}
