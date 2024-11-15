package com.erp.server.wms.controller.api;


import cn.hutool.core.text.CharSequenceUtil;
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
import com.erp.model.wms.dto.OtherOutstockDTO;
import com.erp.model.wms.entity.OtherOutstockEntity;
import com.erp.server.wms.query.OtherOutstockQueryHandler;
import com.erp.server.wms.service.OtherOutstockService;
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
 *  其他出库单
 *
 * @author will
 * @since 2023-05-10
 */
@RestController
@LogSystemModule("其他出库单")
@RequestMapping("/otherOutstock")
@Slf4j
public class OtherOutstockController extends BaseController {


    @Resource
    private OtherOutstockService otherOutstockService;
    
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
            menuCode = "wms:otherOutstock:paging",
            tableAlias = "oo"
    )
    @WebAdvanceQuery(handler = OtherOutstockQueryHandler.class)
    public ApiResult<PagingVO<OtherOutstockDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<OtherOutstockDTO.SearchParamDTO> dto) {
        PagingVO<OtherOutstockDTO.ListDTO> pagingVO = otherOutstockService.paging(dto);
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
            menuCode = "wms:otherOutstock:paging",
            tableAlias = "oo"
    )
    public ApiResult<List<OtherOutstockDTO.ListStatusCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<OtherOutstockDTO.ListStatusCountDTO> list = otherOutstockService.listCount(dto);
        return success(list);
    }

    /**
     * 新增
     * @author Will
     * @date: 2023/5/10 19:58
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增其他出库单")
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherOutstock:add",
            serviceClass = OtherOutstockService.class,
            keyIdName = "id")
    public ApiResult add(@RequestBody @Validated OtherOutstockDTO.AddDTO dto) {
        String id = otherOutstockService.add(dto);
        return CharSequenceUtil.isNotBlank(id) ? success() : failure();
    }

    /**
     * 新增并提交
     * @author Will
     * @date: 2023/5/10 19:59
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交其他出库单")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherOutstock:add",
            serviceClass = OtherOutstockService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated OtherOutstockDTO.AddDTO dto) {
        String id = otherOutstockService.addAndSubmit(dto);
        return CharSequenceUtil.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改
     * @author Will
     * @date: 2023/5/10 20:02
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改其他出库单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherOutstock:update",
            serviceClass = OtherOutstockService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated OtherOutstockDTO.UpdateDTO dto) {
        Boolean flag = otherOutstockService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改并提交
     * @author Will
     * @date: 2023/5/10 20:02
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交其他出库单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherOutstock:update",
            serviceClass = OtherOutstockService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated OtherOutstockDTO.UpdateDTO dto) {
        Boolean flag = otherOutstockService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 提交
     * @author Will
     * @date: 2023/5/10 20:00
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交其他出库单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherOutstock:submit",
            serviceClass = OtherOutstockService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = otherOutstockService.submit(dto.getIds());
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
            menuCode = "wms:otherOutstock:view",
            serviceClass = OtherOutstockService.class,
            keyIdName = "id")
    public ApiResult<OtherOutstockDTO.ViewDTO> view(@RequestParam("id") String id) {
        OtherOutstockDTO.ViewDTO dto = otherOutstockService.view(id);
        return success(dto);
    }


    /**
     * 删除
     * @author Will
     * @date: 2023/5/10 20:09
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除其他出库单")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherOutstock:delete",
            serviceClass = OtherOutstockService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = otherOutstockService.delete(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 作废
     * @author Will
     * @date: 2023/5/10 20:11
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INVALID, desc = "作废其他出库单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherOutstock:invalid",
            serviceClass = OtherOutstockService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean flag = otherOutstockService.invalid(dto.getIds(),dto.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @author Will
     * @date: 2023/5/10 20:11
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核其他出库单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherOutstock:approve",
            serviceClass = OtherOutstockService.class,
            keyIdName = "ids")
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<String> ids = dto.getIds();
        for (String id : ids) {
            BatchResultDTO resultDTO;
            String flagCode = id;
            try {
                OtherOutstockEntity entity = otherOutstockService.getById(id);
                if (Objects.isNull(entity)) {
                    resultDTO = BatchResultDTO.fail(id,flagCode, "其他出库单不存在");
                } else {
                    flagCode = entity.getCode();
                    resultDTO = otherOutstockService.approve(id,dto.getType(),dto.getComment());
                }
            } catch (Exception e) {
                log.error("其他出库单审核失败>>>>{}", e);
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
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核其他出库单")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherOutstock:disApprove",
            serviceClass = OtherOutstockService.class,
            keyIdName = "ids")
    public ApiResult disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<String> ids = dto.getIds();
        for (String id : ids) {
            BatchResultDTO resultDTO;
            String flagCode = id;
            try {
                OtherOutstockEntity entity = otherOutstockService.getById(id);
                if (Objects.isNull(entity)) {
                    resultDTO = BatchResultDTO.fail(id,flagCode, "其他出库单不存在");
                } else {
                    flagCode = entity.getCode();
                    resultDTO = otherOutstockService.disApprove(id);
                }
            } catch (Exception e) {
                log.error("其他出库单反审核失败>>>>{}", e);
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
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销其他出库单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherOutstock:cancelProcess",
            serviceClass = OtherOutstockService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = otherOutstockService.cancelProcess(dto.getIds());
        return result == true ? success() : failure();
    }

    /**
     * 导出
     * @author Will
     * @date: 2023/5/10 20:25
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出其他出库单")
    @PostMapping(value = "/exportExcel")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherOutstock:paging",
            tableAlias = "oo"
    )
    public ApiResult exportExcel(@RequestBody OtherOutstockDTO.SearchParamDTO dto) {
        Boolean flag = otherOutstockService.exportExcel(dto);
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
        otherOutstockService.downloadTemplate(response);
        return success();
    }

    /**
     * 导入
     * @author Jim
     * {@code @date:} 2024/03/21
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入其他出库单")
    @PostMapping("/import")
    public ApiResult<?> exportWarehouse(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = otherOutstockService.importFile(excelFile, response);
        return result ? success() : failure();
    }
}
