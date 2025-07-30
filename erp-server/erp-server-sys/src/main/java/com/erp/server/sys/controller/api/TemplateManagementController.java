package com.erp.server.sys.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.entity.TemplateManagementEntity;
import com.erp.server.sys.query.TemplateManagementQueryHandler;
import lombok.extern.slf4j.Slf4j;
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
import com.erp.server.sys.service.TemplateManagementService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.TemplateManagementDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 合同模板主表
 *
 * @author jack
 * @since 2025-07-24
 */
@Slf4j
@RestController
@LogSystemModule("合同模板主表")
@RequestMapping("/templateManagement")
public class TemplateManagementController extends BaseController {

    @Resource
    private TemplateManagementService templateManagementService;

    /**
    * 新增
    * @author jack
    * @date:  2025-07-24
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "合同模板主表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated TemplateManagementDTO.AddDTO dto) {
        return success(templateManagementService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-07-24
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "合同模板主表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "sys:templateManagement:update",
        serviceClass = TemplateManagementService.class,
        keyIdName = "id")
    public ApiResult<Object> update(@RequestBody @Validated TemplateManagementDTO.UpdateDTO dto) {
        templateManagementService.update(dto);
        return success();
    }


    /**
     * 获取状态统计
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "sys:templateManagement:paging",
            tableAlias = "tm"
    )
    public ApiResult<List<TemplateManagementDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(templateManagementService.tabList(dto));
    }

    /**
     * 列表查询
     * @author jack
     * @date: 2025-07-28
     * @param dto
     * @return ApiResult<PagingVO<TemplateManagementDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "sys:templateManagement:paging",
            tableAlias = "tm"
    )
    @WebAdvanceQuery(handler = TemplateManagementQueryHandler.class)
    public ApiResult<PagingVO<TemplateManagementDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<TemplateManagementDTO.PagingParamDTO> dto) {
        return success(templateManagementService.paging(dto));
    }

    /**
     * 详情
     * @author jack
     * @date: 2025-07-28
     * @param id
     * @return ApiResult<AfterSaleDTO.ViewDTO>>
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "sys:templateManagement:view",
            serviceClass = TemplateManagementService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<TemplateManagementDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(templateManagementService.view(id));
    }


    /**
     * 删除
     * @author jack
     * @date: 2025-07-28
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "sys:templateManagement:delete",
            serviceClass = TemplateManagementService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "模板删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<TemplateManagementEntity> list = templateManagementService.lambdaQuery().in(TemplateManagementEntity::getId, ids).list();
        Map<String, TemplateManagementEntity> idEntityMap = list.stream().collect(Collectors.toMap(TemplateManagementEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = templateManagementService.delete(id);
            }catch (Exception e){
                log.error("模板删除失败",e);
                TemplateManagementEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "模板不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 启用/停用
     * @author jack
     * @date: 2025-07-28
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/disabled")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "sys:templateManagement:disabled",
            serviceClass = TemplateManagementService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.UPDATE, desc = "模板启用/停用")
    public ApiResult<List<BatchResultDTO>> disabled(@RequestBody @Validated TemplateManagementDTO.DisabledDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<TemplateManagementEntity> list = templateManagementService.lambdaQuery().in(TemplateManagementEntity::getId, ids).list();
        Map<String, TemplateManagementEntity> idEntityMap = list.stream().collect(Collectors.toMap(TemplateManagementEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = templateManagementService.setDisabled(id,dto.getDisabledStatus());
            }catch (Exception e){
                log.error("模板更新失败",e);
                TemplateManagementEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "模板不存在, 更新失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 设置默认
     * @author jack
     * @date: 2025-07-28
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/setDefault")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "sys:templateManagement:setDefault",
            serviceClass = TemplateManagementService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.UPDATE, desc = "设置默认模板")
    public ApiResult<List<BatchResultDTO>> setDefault(@RequestBody @Validated  BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<TemplateManagementEntity> list = templateManagementService.lambdaQuery().in(TemplateManagementEntity::getId, ids).list();
        Map<String, TemplateManagementEntity> idEntityMap = list.stream().collect(Collectors.toMap(TemplateManagementEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = templateManagementService.setDefault(id);
            }catch (Exception e){
                log.error("模板设置默认失败",e);
                TemplateManagementEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "模板不存在, 设置默认失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 模糊搜索 （启用 未删除，已发布）
     * @author jack
     * @date: 2025-07-28
     * @param dto
     * @return ApiResult<PagingVO<TemplateManagementDTO.PageSelectDTO>>
     */
    @PostMapping("/pagingSelect")
    public ApiResult<List<TemplateManagementDTO.PageSelectDTO>> pagingSelect(@RequestBody @Validated TemplateManagementDTO.SelectDTO dto) {
        return success(templateManagementService.pagingSelect(dto));
    }

}
