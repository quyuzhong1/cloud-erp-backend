package com.erp.server.tms.controller.api;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.CfgFileParseDTO;
import com.erp.model.tms.entity.CfgFileParseEntity;
import com.erp.server.tms.query.CfgFileParseQueryHandler;
import com.erp.server.tms.service.CfgFileParseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 月结文件解析配置接口。
 *
 * @author jack
 * @since 2026-06-29
 */
@Slf4j
@RestController
@LogSystemModule("月结文件解析配置")
@RequestMapping("/cfgFileParse")
public class CfgFileParseController extends BaseController {

    @Resource
    private CfgFileParseService cfgFileParseService;

    /**
     * 新增月结文件解析配置。
     *
     * @param dto 新增参数
     * @return 新增结果
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "月结文件解析配置新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgFileParseDTO.AddDTO dto) {
        return success(cfgFileParseService.add(dto));
    }

    /**
     * 修改月结文件解析配置。
     *
     * @param dto 修改参数
     * @return 操作结果
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "月结文件解析配置修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:cfgFileParse:update",
            serviceClass = CfgFileParseService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgFileParseDTO.UpdateDTO dto) {
        cfgFileParseService.update(dto);
        return success();
    }

    /**
     * 获取启用状态页签数量。
     *
     * @param dto 权限参数
     * @return 页签数量
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:cfgFileParse:paging",
            tableAlias = "cfp"
    )
    public ApiResult<List<CfgFileParseDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(cfgFileParseService.tabList(dto));
    }

    /**
     * 分页查询月结文件解析配置。
     *
     * @param dto 分页查询参数
     * @return 分页结果
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = CfgFileParseQueryHandler.class)
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:cfgFileParse:paging",
            tableAlias = "cfp"
    )
    public ApiResult<PagingVO<CfgFileParseDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CfgFileParseDTO.PagingParamDTO> dto) {
        return success(cfgFileParseService.paging(dto));
    }

    /**
     * 查询月结文件解析配置详情。
     *
     * @param id 配置 ID
     * @return 配置详情
     */
    @GetMapping("/view")
    @LogViewService
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:cfgFileParse:view",
            serviceClass = CfgFileParseService.class,
            keyIdName = "id")
    public ApiResult<CfgFileParseDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(cfgFileParseService.view(id));
    }

    /**
     * 创建异步导出任务。
     *
     * @param dto 导出参数
     * @return 是否创建成功
     */
    @PostMapping("/export")
    @WebAdvanceQuery(handler = CfgFileParseQueryHandler.class)
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:cfgFileParse:export",
            tableAlias = "cfp"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "月结文件解析配置导出")
    public ApiResult<Boolean> exportList(@RequestBody @Validated CfgFileParseDTO.ExportDTO dto) {
        return success(cfgFileParseService.exportList(dto));
    }


    /**
     * 批量删除月结文件解析配置。
     *
     * @param dto ID 集合
     * @return 每条数据的处理结果
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:cfgFileParse:delete",
            serviceClass = CfgFileParseService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "月结文件解析配置批量删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<CfgFileParseEntity> list = cfgFileParseService.lambdaQuery().in(CfgFileParseEntity::getId, ids).list();
        Map<String, CfgFileParseEntity> idEntityMap = list.stream().collect(Collectors.toMap(CfgFileParseEntity::getId, item -> item));
        for (String id : ids) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = cfgFileParseService.delete(id);
            } catch (Exception e) {
                log.error("月结文件解析配置删除失败", e);
                CfgFileParseEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "月结文件解析配置不存在，删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e);
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量更新月结文件解析配置启用状态。
     *
     * @param dto ID 集合和启用状态
     * @return 每条数据的处理结果
     */
    @PostMapping("/updateDisabled")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:cfgFileParse:updateDisabled",
            serviceClass = CfgFileParseService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.UPDATE, desc = "月结文件解析配置批量更新启用状态")
    public ApiResult<List<BatchResultDTO>> batchUpdateDisabled(@RequestBody @Validated CfgFileParseDTO.UpdateDisabledDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<CfgFileParseEntity> list = cfgFileParseService.lambdaQuery().in(CfgFileParseEntity::getId, ids).list();
        Map<String, CfgFileParseEntity> idEntityMap = list.stream().collect(Collectors.toMap(CfgFileParseEntity::getId, item -> item));
        for (String id : ids) {
            BatchResultDTO updateResult;
            try {
                updateResult = cfgFileParseService.updateDisabled(id, dto.getDisabled());
            } catch (Exception e) {
                log.error("月结文件解析配置启用状态更新失败", e);
                CfgFileParseEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    updateResult = BatchResultDTO.fail(id, id, "月结文件解析配置不存在，启用状态更新失败");
                    resultDTOS.add(updateResult);
                    continue;
                }
                updateResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e);
            }
            resultDTOS.add(updateResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
