package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.OtherInstockDTO;
import com.erp.model.wms.entity.OtherInstockEntity;
import com.erp.server.wms.query.OtherInstockQueryHandler;
import com.erp.server.wms.service.OtherInstockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *  其他入库单
 *
 * @author will
 * @since 2023-05-10
 */
@RestController
@LogSystemModule("其他入库单")
@RequestMapping("/otherInstock")
@Slf4j
public class OtherInstockController extends BaseController {

    @Resource
    private OtherInstockService otherInstockService;


    /**
     * 列表查询
     * @author Will
     * @date: 2023/5/10 19:56
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherInstock:paging",
            tableAlias = "oi"
    )
    @WebAdvanceQuery(handler = OtherInstockQueryHandler.class)
    public ApiResult<PagingVO<OtherInstockDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<OtherInstockDTO.SearchParamDTO> dto) {
        PagingVO<OtherInstockDTO.ListDTO> pagingVO = otherInstockService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 列表数量
     * @author Will
     * @date: 2023/5/10 20:08
     * @param dto
     * @return ApiResult<List<ListStatusCountDTO>>
     */
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherInstock:paging",
            tableAlias = "oi"
    )
    public ApiResult<List<OtherInstockDTO.ListStatusCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<OtherInstockDTO.ListStatusCountDTO> list = otherInstockService.listCount(dto);
        return success(list);
    }

    /**
     * 新增
     * @author Will
     * @date: 2023/5/10 19:58
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增其他入库单")
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherInstock:add",
            serviceClass = OtherInstockService.class,
            keyIdName = "id")
    public ApiResult add(@RequestBody @Validated OtherInstockDTO.AddDTO dto) {
        String id = otherInstockService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 新增并提交
     * @author Will
     * @date: 2023/5/10 19:59
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交其他入库单")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherInstock:add",
            serviceClass = OtherInstockService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated OtherInstockDTO.AddDTO dto) {
        String id = otherInstockService.addAndSubmit(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改
     * @author Will
     * @date: 2023/5/10 20:02
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改其他入库单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherInstock:update",
            serviceClass = OtherInstockService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated OtherInstockDTO.UpdateDTO dto) {
        Boolean flag = otherInstockService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改并提交
     * @author Will
     * @date: 2023/5/10 20:02
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交其他入库单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherInstock:update",
            serviceClass = OtherInstockService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated OtherInstockDTO.UpdateDTO dto) {
        Boolean flag = otherInstockService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 提交
     * @author Will
     * @date: 2023/5/10 20:00
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交其他入库单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherInstock:submit",
            serviceClass = OtherInstockService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = otherInstockService.submit(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 查看详情
     * @author Will
     * @date: 2023/5/10 20:10
     * @param id
     * @return ApiResult
     */
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherInstock:view",
            serviceClass = OtherInstockService.class,
            keyIdName = "id")
    public ApiResult<OtherInstockDTO.ViewDTO> view(@RequestParam("id") String id) {
        OtherInstockDTO.ViewDTO dto = otherInstockService.view(id);
        return success(dto);
    }


    /**
     * 删除
     * @author Will
     * @date: 2023/5/10 20:09
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除其他入库单")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherInstock:delete",
            serviceClass = OtherInstockService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = otherInstockService.delete(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 作废
     * @author Will
     * @date: 2023/5/10 20:11
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INVALID, desc = "作废其他入库单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherInstock:invalid",
            serviceClass = OtherInstockService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean flag = otherInstockService.invalid(dto.getIds(),dto.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @author Will
     * @date: 2023/5/10 20:11
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核其他入库单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherInstock:approve",
            serviceClass = OtherInstockService.class,
            keyIdName = "ids")
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<String> ids = dto.getIds();
        for (String id : ids) {
            BatchResultDTO resultDTO;
            String flagCode = id;
            try {
                OtherInstockEntity entity = otherInstockService.getById(id);
                if (Objects.isNull(entity)) {
                    resultDTO = BatchResultDTO.fail(id,flagCode, "其他入库单不存在");
                } else {
                    flagCode = entity.getCode();
                    resultDTO = otherInstockService.approve(id,dto.getType(),dto.getComment(), true);
                }
            } catch (Exception e) {
                log.error("其他入库单审核失败>>>>{}", e);
                resultDTO = BatchResultDTO.fail(id,flagCode, e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量反审核
     * @author Will
     * @date: 2023/5/10 20:12
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核其他入库单")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherInstock:disApprove",
            serviceClass = OtherInstockService.class,
            keyIdName = "ids")
    public ApiResult disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<String> ids = dto.getIds();
        for (String id : ids) {
            BatchResultDTO resultDTO;
            String flagCode = id;
            try {
                OtherInstockEntity entity = otherInstockService.getById(id);
                if (Objects.isNull(entity)) {
                    resultDTO = BatchResultDTO.fail(id,flagCode, "其他入库单不存在");
                } else {
                    flagCode = entity.getCode();
                    resultDTO = otherInstockService.disApprove(id, true);
                }
            } catch (Exception e) {
                log.error("其他入库单反审核失败>>>>{}", e);
                resultDTO = BatchResultDTO.fail(id,flagCode, e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 取消流程
     * @author Will
     * @date: 2023/5/10 20:24
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销其他入库单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherInstock:cancelProcess",
            serviceClass = OtherInstockService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = otherInstockService.cancelProcess(dto.getIds());
        return result == true ? success() : failure();
    }

    /**
     * 导出
     * @author Will
     * @date: 2023/5/10 20:25
     * @param dto
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出其他入库单")
    @PostMapping(value = "/exportExcel")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherInstock:paging",
            tableAlias = "oi"
    )
    @WebAdvanceQuery(handler = OtherInstockQueryHandler.class)
    public ApiResult exportExcel(@RequestBody OtherInstockDTO.SearchParamDTO dto, HttpServletResponse response) {
        Boolean flag = otherInstockService.exportExcel(dto, response);
        return flag == true ? success() : failure();
    }

    /**
     * 下载导入模板
     *
     * @author Jim
     * {@code @date:} 2024/03/21
     */
    @GetMapping("/downloadTemplate")
    public ApiResult<?> downloadTemplate(HttpServletResponse response) {
        otherInstockService.downloadTemplate(response);
        return success();
    }

    /**
     * 导入
     * @author Jim
     * {@code @date:} 2024/03/21
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入其他入库单")
    @PostMapping("/import")
    public ApiResult<?> exportWarehouse(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = otherInstockService.importFile(excelFile, response);
        return result ? success() : failure();
    }
}
