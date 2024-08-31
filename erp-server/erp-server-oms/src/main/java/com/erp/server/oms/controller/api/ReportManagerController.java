package com.erp.server.oms.controller.api;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.ReportDTO;
import com.erp.model.oms.dto.RuleOrderApprovalDTO;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

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
public class ReportManagerController extends BaseController {

    @Autowired
    private SoB2cService soB2cService;

    /**
     * 产品销售分页查询
     *
     * @return
     */
    @PostMapping("/productSalesPaging")
    public ApiResult<PagingVO<ReportDTO.ProductSalesPagingViewDTO>> queryProductSalesByPage(@RequestBody @Validated PagingDTO<ReportDTO.ProductSalesPagingParamDTO> dto) {
        PagingVO<ReportDTO.ProductSalesPagingViewDTO> pagingVO = soB2cService.productSalesPaging(dto);
        return success(pagingVO);
    }

    /**
     * 产品销售分页导出
     *
     * @return
     */
    @PostMapping("/productSalesExport")
    public ApiResult productSalesExport(@RequestBody @Validated ReportDTO.ProductSalesPagingParamDTO dto) {
        Boolean result = soB2cService.productSalesExport(dto);
        return result ? success() : failure();

    }
}
