package com.erp.server.dmp.controller.api;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.WebAdvanceQuery;
import com.erp.model.dmp.dto.DmpCfgInputDTO;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.server.dmp.query.DmpCfgOutputQueryHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
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
import com.erp.server.dmp.service.DmpCfgOutputService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpCfgOutputDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;

/**
 * 推送配置
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@RestController
@LogSystemModule("推送配置")
@RequestMapping("/dmpCfgOutput")
public class DmpCfgOutputController extends BaseController {

    @Resource
    private DmpCfgOutputService dmpCfgOutputService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "推送配置新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpCfgOutputDTO.AddDTO dto) {
        return success(dmpCfgOutputService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "推送配置修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpCfgOutput:update",
        serviceClass = DmpCfgOutputService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpCfgOutputDTO.UpdateDTO dto) {
        dmpCfgOutputService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgOutput:paging",
            tableAlias = ""
    )
    public ApiResult<List<DmpCfgOutputDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(dmpCfgOutputService.tabList(dto));
    }

    /**
    * 列表查询
    * @author Jim
    * @date: 2025-10-23
    * @param dto
    * @return ApiResult<PagingVO<DmpCfgOutputDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgOutput:paging",
            tableAlias = ""
    )
    @WebAdvanceQuery(handler = DmpCfgOutputQueryHandler.class)
    public ApiResult<PagingVO<DmpCfgOutputDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<DmpCfgOutputDTO.PagingParamDTO> dto) {
        return success(dmpCfgOutputService.paging(dto));
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
            menuCode = "dmp:dmpCfgOutput:delete",
            serviceClass = DmpCfgOutputService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "推送配置删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// 数据查询放入外层，处理结果统一更新或单条更新
		List<DmpCfgOutputEntity> list = dmpCfgOutputService.lambdaQuery().in(DmpCfgOutputEntity::getId, ids).list();
		Map<String, DmpCfgOutputEntity> idEntityMap = list.stream().collect(Collectors.toMap(DmpCfgOutputEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            DmpCfgOutputEntity entity = idEntityMap.get(id);
            try {
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "推送配置不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = dmpCfgOutputService.delete(id);
            }catch (Exception e){
                log.error("推送配置删除失败",e);
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
    @LogAction(value = LogActionEnum.UPDATE, desc = "批量禁用推送配置", keyIdName = "ids")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgOutput:disabled",
            serviceClass = DmpCfgOutputService.class,
            keyIdName = "ids")
    public ApiResult<?> disabled(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<DmpCfgOutputEntity> list = dmpCfgOutputService.lambdaQuery().in(DmpCfgOutputEntity::getId, dto.getIds()).list();
        Map<String, DmpCfgOutputEntity> idEntityMap = list.stream().collect(Collectors.toMap(DmpCfgOutputEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            DmpCfgOutputEntity entity = idEntityMap.get(id);
            try {
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "推送配置不存在, 批量禁用失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = dmpCfgOutputService.disable(entity);
            }catch (Exception e){
                log.error("推送配置批量禁用失败",e);
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
    @LogAction(value = LogActionEnum.UPDATE, desc = "批量启用推送配置", keyIdName = "ids")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgOutput:enable",
            serviceClass = DmpCfgOutputService.class,
            keyIdName = "ids")
    public ApiResult<?> enable(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<DmpCfgOutputEntity> list = dmpCfgOutputService.lambdaQuery().in(DmpCfgOutputEntity::getId, dto.getIds()).list();
        Map<String, DmpCfgOutputEntity> idEntityMap = list.stream().collect(Collectors.toMap(DmpCfgOutputEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            DmpCfgOutputEntity entity = idEntityMap.get(id);
            try {
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "推送配置不存在, 启用失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = dmpCfgOutputService.enable(entity);
            }catch (Exception e){
                log.error("推送配置启用失败",e);
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
    * @return ApiResult<DmpCfgOutputDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgOutput:view",
            serviceClass = DmpCfgOutputService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<DmpCfgOutputDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(dmpCfgOutputService.view(id));
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
            menuCode = "dmp:dmpCfgOutput:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "推送配置导出Excel数据")
    public ApiResult<Boolean> exportList(@RequestBody @Validated DmpCfgOutputDTO.ExportDTO dto, HttpServletResponse response) {
        dmpCfgOutputService.exportList(dto, response);
        return success(true);
    }


    /**
     * 推送配置下拉列表
     * @author Jim
     * @date: 2025-10-23
     * @param dto
     * @return ApiResult<PagingVO<DmpCfgInputDTO.ListDTO>>
     */
    @PostMapping("/simplePaging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgOutput:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<DmpCfgOutputDTO.ListDmpCfgOutputDTO>> simplePaging(@RequestBody @Validated PagingDTO<DmpCfgOutputDTO.SimplePagingParamDTO> dto) {
        Page<DmpCfgOutputEntity> page = dmpCfgOutputService.lambdaQuery()
                .like(StringUtils.isNotBlank(dto.getParams().getSearchKey()), DmpCfgOutputEntity::getFlowName, dto.getParams().getSearchKey())
                .like(StringUtils.isNotBlank(dto.getParams().getSearchKey()), DmpCfgOutputEntity::getFlowCode, dto.getParams().getSearchKey())
                .page(new Page<>(dto.getCurrPage(), dto.getPageSize()));
        // 快速复制page.getRecords()到List<DmpCfgInputDTO.ListDmpCfgInputDTO> resultList
        List<DmpCfgOutputDTO.ListDmpCfgOutputDTO> resultList = page.getRecords().stream()
                .map(entity -> {
                    DmpCfgOutputDTO.ListDmpCfgOutputDTO dtoTemp = new DmpCfgOutputDTO.ListDmpCfgOutputDTO();
                    BeanUtils.copyProperties(entity, dtoTemp);
                    return dtoTemp;
                })
                .collect(Collectors.toList());
        return success(new PagingVO<>(resultList, (int) page.getTotal(), dto.getPageSize(), dto.getCurrPage()));
    }
}
