package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.RefundOrderDTO;
import com.erp.server.oms.service.RefundOrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import javax.annotation.Resource;

/**
 * <p>
 * 退款订单 前端控制器
 * </p>
 *
 * @author Lambda
 * @since 2024-09-12
 */
@RestController
@RequestMapping("/refundOrder")
public class RefundOrderController extends BaseController {


    @Resource
    private RefundOrderService refundOrderService;

    /**
     * 退款订单分页
     *
     * @return
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<RefundOrderDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<RefundOrderDTO.PagingParamDTO> dto) {
        PagingVO<RefundOrderDTO.PagingViewDTO> pagingVO = refundOrderService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 退款订单导出
     *
     * @return
     */
    @PostMapping("/export")
    @WebAdvanceQuery
    public ApiResult<Object> export(@RequestBody @Validated RefundOrderDTO.PagingParamDTO dto) {
        refundOrderService.exportExcel(dto);
        return success();
    }
}
