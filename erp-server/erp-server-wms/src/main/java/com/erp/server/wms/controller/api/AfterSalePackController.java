package com.erp.server.wms.controller.api;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.AfterSalePackDTO;
import com.erp.model.wms.entity.AfterSalePackEntity;
import com.erp.server.wms.query.AfterSalePackQueryHandler;
import com.erp.server.wms.service.AfterSalePackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 售后装箱表
 *
 * @author lei.nie
 * @since 2026-05-12
 */
@Slf4j
@RestController
@LogSystemModule("售后装箱表")
@RequestMapping("/afterSalePack")
public class AfterSalePackController extends BaseController {

    @Resource
    private AfterSalePackService afterSalePackService;

    /**
     * 申请箱唛
     *
     * @param dto AfterSalePackDTO.BoxCodeApplicationDTO
     * @return ApiResult<List < String>>
     * @author lei.nie
     * @date: 2026-05-12
     */
    @PostMapping("/boxCodeApplication")
    @LogAction(value = LogActionEnum.INSERT, desc = "申请箱唛")
    public ApiResult<List<String>> boxCodeApplication(@RequestBody @Validated AfterSalePackDTO.BoxCodeApplicationDTO dto) {
        return success(afterSalePackService.boxCodeApplication(dto));
    }

    /**
     * 新增
     *
     * @param dto AfterSalePackDTO.AddDTO
     * @return ApiResult<String>
     * @author lei.nie
     * @date: 2026-05-12
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "售后装箱表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AfterSalePackDTO.AddDTO dto) {
        return success(afterSalePackService.add(dto));
    }

    /**
     * 修改
     *
     * @param dto AfterSalePackDTO.UpdateDTO
     * @return ApiResult
     * @author lei.nie
     * @date: 2026-05-12
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "售后装箱表修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:afterSalePack:update",
            serviceClass = AfterSalePackService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated AfterSalePackDTO.UpdateDTO dto) {
        afterSalePackService.update(dto);
        return success();
    }

    /**
     * 确定提审
     *
     * @param dto AfterSalePackDTO.UpdateDTO
     * @return ApiResult
     * @author lei.nie
     * @date: 2026-05-12
     */
    @PostMapping("/submit")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "售后装箱表确定提审")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:afterSalePack:submit",
            serviceClass = AfterSalePackService.class,
            keyIdName = "id")
    public ApiResult<?> submit(@RequestBody @Validated AfterSalePackDTO.UpdateDTO dto) {
        afterSalePackService.submit(dto);
        return success();
    }

    /**
     * 复核驳回
     *
     * @param dto AfterSalePackDTO.UpdateDTO
     * @return ApiResult
     * @author lei.nie
     * @date: 2026-05-12
     */
    @PostMapping("/reject")
    @LogAction(value = LogActionEnum.REJECT, desc = "售后装箱表复核驳回")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:afterSalePack:reject",
            serviceClass = AfterSalePackService.class,
            keyIdName = "id")
    public ApiResult<?> reject(@RequestBody @Validated AfterSalePackDTO.UpdateDTO dto) {
        afterSalePackService.reject(dto);
        return success();
    }

    /**
     * 确认并封箱
     *
     * @param dto AfterSalePackDTO.UpdateDTO
     * @return ApiResult
     * @author lei.nie
     * @date: 2026-05-12
     */
    @PostMapping("/confirm")
    @LogAction(value = LogActionEnum.CONFIRM, desc = "售后装箱表确认并封箱")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:afterSalePack:confirm",
            serviceClass = AfterSalePackService.class,
            keyIdName = "id")
    public ApiResult<?> confirm(@RequestBody @Validated AfterSalePackDTO.UpdateDTO dto) {
        afterSalePackService.confirm(dto);
        return success();
    }

    /**
     * 列表查询
     *
     * @param dto AfterSalePackDTO.PagingParamDTO
     * @return ApiResult<PagingVO < AfterSalePackingDTO.ListDTO>>
     * @author lei.nie
     * @date: 2026-05-12
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:afterSalePack:paging",
            tableAlias = "t"
    )
    @WebAdvanceQuery(handler = AfterSalePackQueryHandler.class)
    public ApiResult<PagingVO<AfterSalePackDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<AfterSalePackDTO.PagingParamDTO> dto) {
        return success(afterSalePackService.paging(dto));
    }

    /**
     * 详情
     *
     * @param id String
     * @return ApiResult<AfterSalePackDTO.ViewDTO>>
     * @author lei.nie
     * @date: 2026-05-12
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:afterSalePack:view",
            serviceClass = AfterSalePackService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<AfterSalePackDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(afterSalePackService.view(id));
    }

    /**
     * 删除
     *
     * @param dto BaseIdsDTO.IdsDTO
     * @return ApiResult<List < BatchResultDTO>>
     * @author lei.nie
     * @date: 2026-05-12
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:afterSalePack:delete",
            serviceClass = AfterSalePackService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "售后装箱删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = afterSalePackService.delete(dto);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 箱唛作废
     */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:afterSalePack:invalid",
            serviceClass = AfterSalePackService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "售后装箱箱唛作废")
    public ApiResult<List<BatchResultDTO>> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<AfterSalePackEntity> list = afterSalePackService.lambdaQuery().in(AfterSalePackEntity::getId, ids).list();
        Map<String, AfterSalePackEntity> idEntityMap = list.stream().collect(Collectors.toMap(AfterSalePackEntity::getId, item -> item));
        String remark = dto.getRemark();
        for (String id : ids) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = afterSalePackService.invalid(id, remark);
            } catch (Exception e) {
                log.warn("售后装箱箱唛作废失败，id={}", id, e);
                AfterSalePackEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, BatchResultDTO.resolveFailMsg(e));
                } else {
                    invalidResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), BatchResultDTO.resolveFailMsg(e));
                }
            }
            resultDTOS.add(invalidResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 导出
     */
    @PostMapping("/exportExcel")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:afterSalePack:exportExcel",
            tableAlias = "t")
    @WebAdvanceQuery(handler = AfterSalePackQueryHandler.class)
    @LogAction(value = LogActionEnum.EXPORT, desc = "售后装箱导出")
    public ApiResult<Boolean> exportExcel(@RequestBody @Validated AfterSalePackDTO.ExportDTO dto) {
        return success(afterSalePackService.exportExcel(dto));
    }

    /**
     * 根据code查询详情
     *
     * @param code String
     * @return ApiResult<AfterSalePackDTO.ViewDTO>>
     * @author lei.nie
     * @date: 2026-05-12
     */
    @GetMapping("/viewByCode")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:afterSalePack:view",
            serviceClass = AfterSalePackService.class,
            keyIdName = "code")
    @LogViewService
    public ApiResult<AfterSalePackDTO.ViewDTO> viewByCode(@RequestParam("code") String code) {
        return success(afterSalePackService.viewByCode(code));
    }

}
