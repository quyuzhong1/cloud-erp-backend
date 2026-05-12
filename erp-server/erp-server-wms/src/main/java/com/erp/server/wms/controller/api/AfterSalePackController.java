package com.erp.server.wms.controller.api;

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
import com.erp.server.wms.query.AfterSalePackQueryHandler;
import com.erp.server.wms.service.AfterSalePackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
     * 箱码申请
     *
     * @param dto AfterSalePackDTO.BoxCodeApplicationDTO
     * @return ApiResult<List < String>>
     * @author lei.nie
     * @date: 2026-05-12
     */
    @PostMapping("/boxCodeApplication")
    @LogAction(value = LogActionEnum.INSERT, desc = "箱码申请")
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
            tableAlias = ""
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
     * 导出Excel数据
     *
     * @param dto      AfterSalePackDTO.ExportDTO
     * @param response HttpServletResponse
     * @author lei.nie
     * @date: 2026-05-12
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:afterSalePack:export",
            tableAlias = ""
    )
    @WebAdvanceQuery(handler = AfterSalePackQueryHandler.class)
    @LogAction(value = LogActionEnum.EXPORT, desc = "售后装箱表导出Excel数据")
    public void exportList(@RequestBody @Validated AfterSalePackDTO.ExportDTO dto, HttpServletResponse response) {
        afterSalePackService.exportList(dto, response);
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

}
