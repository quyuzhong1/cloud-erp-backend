package com.erp.server.srm.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.SortParamDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.scm.dto.ListStatusCountDTO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.dto.PurchaseOrderSrmDTO;
import com.erp.rpc.scm.feign.PurchaseOrderFeign;
import com.erp.server.srm.query.OrderConfirmQueryHandler;
import com.erp.server.srm.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 采购订单管理
 *
 * @author zdy
 * @ClassName PurchaseOrderController
 * @description: 采购订单管理
 * @date 2024年01月15日
 * @version: 1.0
 */
@Slf4j
@RestController
@LogSystemModule("采购订单管理")
@RequestMapping("/purchaseOrder")
public class PurchaseOrderController extends BaseController {

    @Resource
    private UserService userService;
    @Resource
    private PurchaseOrderFeign purchaseOrderFeign;
    /**
     * srm订单确认列表统计
     * @author zdy
     * @date: 2024/1/16 17:34
     * @return ApiResult
     */
    @PostMapping("/srmOrderConfirmCount")
    public ApiResult<List<ListStatusCountDTO.PurchaseOrderConfirmCountDTO>> srmOrderConfirmCount() {
        PurchaseOrderSrmDTO.SearchParamDTO dto = new PurchaseOrderSrmDTO.SearchParamDTO();
        dto.setSupplierId(userService.getSupplierId());
        List<ListStatusCountDTO.PurchaseOrderConfirmCountDTO> countDTOS = purchaseOrderFeign.srmOrderConfirmCount(dto);
        return success(countDTOS);
    }


    /**
     * srm订单确认列表分页查询
     * @author Will
     * @date: 2023/3/15 16:47
     * @param dto
     * @return ApiResult<PagingVO<PurchaseOrderDTO.listDTO>>
     */
    @PostMapping("/srmOrderConfirmPaging")
    @WebAdvanceQuery(handler = OrderConfirmQueryHandler.class)
    public ApiResult<PagingVO<PurchaseOrderDTO.ListDTO>> srmOrderConfirmPaging(@RequestBody @Validated PagingDTO<PurchaseOrderDTO.SrmSearchParamDTO> dto) {
        dto.getParams().setSupplierId(userService.getSupplierId());
        PagingVO<PurchaseOrderDTO.ListDTO> pagingVO = purchaseOrderFeign.srmOrderConfirmPaging(dto);
        return success(pagingVO);
    }

    /**
     * srm订单确认列表合计
     * @author zdy
     * @date: 2024/1/15 17:34
     * @param dto
     * @return ApiResult<PagingVO<PurchaseOrderDTO.listDTO>>
     */
    @PostMapping("/srmOrderConfirmTotal")
    @WebAdvanceQuery(handler = OrderConfirmQueryHandler.class)
    public ApiResult<PurchaseOrderDTO.ListDTO> srmOrderConfirmTotal(@RequestBody @Validated PurchaseOrderDTO.SrmSearchParamDTO dto) {
        dto.setSupplierId(userService.getSupplierId());
        PurchaseOrderDTO.ListDTO total = purchaseOrderFeign.srmOrderConfirmTotal(dto);
        return success(total);
    }

    /**
     * srm订单确认整单处理
     * @author zdy
     * @date: 2024/1/15 17:34
     * @param dto
     * @return ApiResult<PagingVO<PurchaseOrderDTO.listDTO>>
     */
    @PostMapping("/srmOrderConfirmStatus")
    public ApiResult<List<BatchResultDTO>> srmOrderConfirmStatus(@RequestBody @Validated PurchaseOrderDTO.ConfirmDTO dto) {
        dto.setSupplierId(userService.getSupplierId());
        List<BatchResultDTO> resultDTOS = purchaseOrderFeign.srmOrderConfirmStatus(dto);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 订单明细
     * @param id
     * @return
     */
    @LogViewService
    @GetMapping("/srmOrderView")
    public ApiResult<PurchaseOrderDTO.ViewDTO> srmOrderView(@RequestParam("id") String id) {
        return success(purchaseOrderFeign.srmOrderView(id));
    }
}
