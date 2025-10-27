package com.erp.server.dmp.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.erp.server.dmp.query.DmpCfgInputQueryHandler;
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
import com.erp.server.dmp.service.DmpCfgInputService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpCfgInputDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.dmp.entity.DmpCfgInputEntity;

/**
 * 拉取配置
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@RestController
@LogSystemModule("拉取配置")
@RequestMapping("/dmpCfgInput")
public class DmpCfgInputController extends BaseController {

    @Resource
    private DmpCfgInputService dmpCfgInputService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "拉取配置新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpCfgInputDTO.AddDTO dto) {
        return success(dmpCfgInputService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "拉取配置修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpCfgInput:update",
        serviceClass = DmpCfgInputService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpCfgInputDTO.UpdateDTO dto) {
        dmpCfgInputService.update(dto);
        return success();
    }

    /**
     * 查询系统单据
     * @Author Luo_WG
     * @Date 2024/9/5 19:28
     * @param id 系统id
     * @return com.common.core.controller.vo.ApiResult<?>
     **/
    @GetMapping("/listDmpCfgInput")
    public ApiResult<List<DmpCfgInputDTO.ListDmpCfgInputDTO>> listDmpCfgInput(@RequestParam("id") String id) {
        List<DmpCfgInputDTO.ListDmpCfgInputDTO> resultList = dmpCfgInputService.listDmpCfgInput(id);
        return success(resultList);
    }

    /**
     * 查询所有输入配置
     * @Author Luo_WG
     * @Date 2024/9/5 19:28
     * @return com.common.core.controller.vo.ApiResult<?>
     **/
    @GetMapping("/allDmpCfgInput")
    public ApiResult<List<DmpCfgInputDTO.ListDmpCfgInputDTO>> allDmpCfgInput() {
        List<DmpCfgInputDTO.ListDmpCfgInputDTO> resultList = dmpCfgInputService.allDmpCfgInput();
        return success(resultList);
    }

    /**
     * 获取状态统计
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgInput:paging",
            tableAlias = ""
    )
    public ApiResult<List<DmpCfgInputDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(dmpCfgInputService.tabList(dto));
    }

    /**
     * 列表查询
     * @author Jim
     * @date: 2025-10-23
     * @param dto
     * @return ApiResult<PagingVO<DmpCfgInputDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgInput:paging",
            tableAlias = ""
    )
    @WebAdvanceQuery(handler = DmpCfgInputQueryHandler.class)
    public ApiResult<PagingVO<DmpCfgInputDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<DmpCfgInputDTO.PagingParamDTO> dto) {
        return success(dmpCfgInputService.paging(dto));
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
            menuCode = "dmp:dmpCfgInput:delete",
            serviceClass = DmpCfgInputService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "拉取配置删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        // 数据查询放入外层，处理结果统一更新或单条更新
        List<DmpCfgInputEntity> list = dmpCfgInputService.lambdaQuery().in(DmpCfgInputEntity::getId, ids).list();
        Map<String, DmpCfgInputEntity> idEntityMap = list.stream().collect(Collectors.toMap(DmpCfgInputEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            DmpCfgInputEntity entity = idEntityMap.get(id);
            try {
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "拉取配置不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = dmpCfgInputService.delete(id);
            }catch (Exception e){
                log.error("拉取配置删除失败",e);

                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量禁用
     */
    @PostMapping("/disabled")
    @LogAction(value = LogActionEnum.UPDATE, desc = "批量禁用拉取配置", keyIdName = "ids")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgInput:disabled",
            serviceClass = DmpCfgInputService.class,
            keyIdName = "ids")
    public ApiResult<?> disabled(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<DmpCfgInputEntity> list = dmpCfgInputService.lambdaQuery().in(DmpCfgInputEntity::getId, dto.getIds()).list();
        Map<String, DmpCfgInputEntity> idEntityMap = list.stream().collect(Collectors.toMap(DmpCfgInputEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            DmpCfgInputEntity entity = idEntityMap.get(id);
            try {
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "拉取配置不存在, 批量禁用失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = dmpCfgInputService.disable(entity);
            }catch (Exception e){
                log.error("拉取配置批量禁用失败",e);
                result = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量启用
     */
    @PostMapping("/enable")
    @LogAction(value = LogActionEnum.UPDATE, desc = "批量启用拉取配置", keyIdName = "ids")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgInput:enable",
            serviceClass = DmpCfgInputService.class,
            keyIdName = "ids")
    public ApiResult<?> enable(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<DmpCfgInputEntity> list = dmpCfgInputService.lambdaQuery().in(DmpCfgInputEntity::getId, dto.getIds()).list();
        Map<String, DmpCfgInputEntity> idEntityMap = list.stream().collect(Collectors.toMap(DmpCfgInputEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            DmpCfgInputEntity entity = idEntityMap.get(id);
            try {
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "拉取配置不存在, 启用失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = dmpCfgInputService.enable(entity);
            }catch (Exception e){
                log.error("拉取配置启用失败",e);
                result = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
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
     * @return ApiResult<DmpCfgInputDTO.ViewDTO>>
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:dmpCfgInput:view",
            serviceClass = DmpCfgInputService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<DmpCfgInputDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(dmpCfgInputService.view(id));
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
            menuCode = "dmp:dmpCfgInput:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "拉取配置导出Excel数据")
    public ApiResult<Boolean> exportList(@RequestBody @Validated DmpCfgInputDTO.ExportDTO dto, HttpServletResponse response) {
        dmpCfgInputService.exportList(dto, response);
        return success(true);
    }

}
