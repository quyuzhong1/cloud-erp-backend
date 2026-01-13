package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.core.anno.LogViewService;
import com.erp.server.wms.query.AwdOutStockQueryHandler;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;
import com.erp.server.wms.service.AwdOutstockService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.AwdOutstockDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 
 *
 * @author wtr
 * @since 2025-12-22
 */
@Slf4j
@RestController
@LogSystemModule("awd出库货件")
@RequestMapping("/awdOutstock")
public class AwdOutstockController extends BaseController {

    @Resource
    private AwdOutstockService awdOutstockService;

    /**
    * 新增
    * @author wtr
    * @date:  2025-12-22
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AwdOutstockDTO.AddDTO dto) {
        return success(awdOutstockService.add(dto));
    }

    /**
    * 更新发货时间
    * @author wtr
    * @date:  2025-12-22
    * @param dtoList
    * @return ApiResult
    */
    @PostMapping("/batchUpdateBillDate")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:awdOutstock:update",
        serviceClass = AwdOutstockService.class,
        keyIdName = "id")
    public ApiResult<?> batchUpdateBillDate(@RequestBody @Validated List<AwdOutstockDTO.UpdateDTO> dtoList) {
        Boolean flag = awdOutstockService.batchUpdateBillDate(dtoList);
        return flag == Boolean.TRUE ? success() : failure();
    }

    /**
     * 更新发货时间弹窗
     * @author wtr
     * @date:  2025-12-22
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/batchUpdateBillDateView")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:awdOutstock:batchUpdateBillDate",
            serviceClass = AwdOutstockService.class,
            keyIdName = "id")
    public ApiResult<List<AwdOutstockDTO.BatchUpdateBillDateViewDTO>> batchUpdateBillDateView(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(awdOutstockService.batchUpdateBillDateView(dto));
    }

    /**
     * 下推头程发货单
     * @author wtr
     * @date:  2025-12-22
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/generateFirstMileDelivery")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:awdOutstock:update",
            serviceClass = AwdOutstockService.class,
            keyIdName = "id")
    public ApiResult<?> generateFirstMileDelivery(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        boolean flag = awdOutstockService.generateFirstMileDelivery(dto);
        return flag == true ? success() : failure();
    }

    /**
    * 列表查询
    * @author wtr
    * @date: 2025-12-22
    * @param dto
    * @return ApiResult<PagingVO<AwdOutstockDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:awdOutstock:paging",
            tableAlias = "ao"
    )
    @WebAdvanceQuery(handler = AwdOutStockQueryHandler.class)
    public ApiResult<PagingVO<AwdOutstockDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<AwdOutstockDTO.PagingParamDTO> dto) {
        return success(awdOutstockService.paging(dto));
    }

    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:awdOutstock:view",
            serviceClass = AwdOutstockService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<AwdOutstockDTO.ViewDTO> view(@RequestParam("id") String id) {
        AwdOutstockDTO.ViewDTO result = awdOutstockService.view(id);
        return success(result);
    }

    /**
    * 导出Excel数据
    * @author wtr
    * @date:  2025-12-22
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:awdOutstock:export",
            tableAlias = "ao"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出Excel数据")
    public ApiResult exportList(@RequestBody @Validated AwdOutstockDTO.ExportDTO dto, HttpServletResponse response) {
        awdOutstockService.exportList(dto, response);
        return success();
    }


}
