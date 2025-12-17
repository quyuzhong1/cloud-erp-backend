package com.erp.server.oms.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.ReportDTO;
import com.erp.server.oms.service.ReportManagerService;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 报表管理
 *
 * @Description
 * @Author yl
 * @Date 2023-09-01 9:58
 */
@Slf4j
@RestController
@RequestMapping("/reportManager")
@LogSystemModule("报表管理")
public class ReportManagerController extends BaseController {

    @Resource
    private ReportManagerService reportManagerService;

    /**
     * 产品销售分页查询
     *
     * @return
     */
    @PostMapping("/productSalesPaging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            shopTableField = "sb.shop_id",
            menuCode = "oms:reportManager:productSalesPaging"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<ReportDTO.ProductSalesPagingViewDTO>> queryProductSalesByPage(@RequestBody @Validated PagingDTO<ReportDTO.ProductSalesPagingParamDTO> dto) {
        PagingVO<ReportDTO.ProductSalesPagingViewDTO> pagingVO = reportManagerService.productSalesPaging(dto);
        return success(pagingVO);
    }

    /**
     * 产品销售分页导出
     *
     * @return
     */
    @PostMapping("/productSalesExport")
    @LogAction(value = LogActionEnum.EXPORT, desc = "产品销售分页导出")
    public ApiResult<Object> productSalesExport(@RequestBody @Validated ReportDTO.ProductSalesPagingParamDTO dto) {
        Boolean result = reportManagerService.productSalesExport(dto);
        return Boolean.TRUE.equals(result) ? success() : failure();
    }
}
