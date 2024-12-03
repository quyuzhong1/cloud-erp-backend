package com.erp.server.srm.controller.api;

import com.common.business.annotation.Idempotent;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.SortParamDTO;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.scm.dto.ListStatusCountDTO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.dto.PurchaseOrderSrmDTO;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import com.erp.rpc.scm.feign.PurchaseOrderFeign;
import com.erp.server.srm.query.WaitDeliveryQueryHandler;
import com.erp.server.srm.service.DeliveryOrderService;
import com.erp.server.srm.service.PurchaseOrderDetailService;
import com.erp.server.srm.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

/**
 * 待发货
 * @author zdy
 * @ClassName WaitDeliveryController
 * @description: 待发货管理
 * @date 2024年01月19日
 * @version: 1.0
 */
@Slf4j
@RestController
@LogSystemModule("待发货管理")
@RequestMapping("/waitDelivery")
public class WaitDeliveryController extends BaseController {

    @Resource
    private UserService userService;
    @Resource
    private PurchaseOrderFeign purchaseOrderFeign;
    @Resource
    private DeliveryOrderService deliveryOrderService;
    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    /**
     * srm待发货列表统计
     * @author zdy
     * @date: 2024/1/16 17:34
     * @return ApiResult
     */
    @PostMapping("/srmWaitDeliveryCount")
    public ApiResult<List<DeliveryOrderDTO.WaitDeliveryCountDTO>> srmWaitDeliveryCount() {
        PurchaseOrderSrmDTO.WaitDeliveryParamDTO dto = new PurchaseOrderSrmDTO.WaitDeliveryParamDTO();
        dto.setSupplierId(userService.getSupplierId());
        List<DeliveryOrderDTO.WaitDeliveryCountDTO> countDTOS = purchaseOrderFeign.srmWaitDeliveryCount(dto);
        //数据转换
//        List<DeliveryOrderDTO.WaitDeliveryCountDTO> countDTOS = deliveryOrderService.buildSrmWaitDeliveryCount(waitDeliveryCountDTO);
        return success(countDTOS);
//        List<DeliveryOrderDTO.WaitDeliveryCountDTO> countDTOS = deliveryOrderService.buildSrmWaitDeliveryCount();
    }


    /**
     * srm待发货分页查询
     * @author Will
     * @date: 2023/3/15 16:47
     * @param dto
     * @return ApiResult<PagingVO<PurchaseOrderDTO.listDTO>>
     */
    @PostMapping("/srmWaitDeliveryPaging")
    @WebAdvanceQuery(handler = WaitDeliveryQueryHandler.class)
    public ApiResult<PagingVO<PurchaseOrderDTO.ListDTO>> srmWaitDeliveryPaging(@RequestBody @Validated PagingDTO<PurchaseOrderDTO.SrmSearchParamDTO> dto) {
//        dto.getParams().setSupplierId(userService.getSupplierId());
//        PagingVO<PurchaseOrderDTO.ListDTO> pagingVO = purchaseOrderDetailService.srmWaitDeliveryPaging(dto);
        dto.getParams().setSupplierId(userService.getSupplierId());
        PagingVO<PurchaseOrderDTO.ListDTO> pagingVO = purchaseOrderFeign.srmWaitDeliveryPaging(dto);

        return success(pagingVO);
    }

    /**
     * 生成送货单列表
     * @author zdy
     * @date: 2023/3/15 16:47
     * @param dto
     * @return ApiResult<PagingVO<PurchaseOrderDTO.listDTO>>
     */
    @PostMapping("/generateDeliveryList")
    public ApiResult<List<PurchaseOrderDTO.ListDTO>> generateDeliveryList(@RequestBody @Validated PurchaseOrderSrmDTO.GenerateDeliveryParamDTO dto) {
        dto.setSupplierId(userService.getSupplierId());
        List<PurchaseOrderDTO.ListDTO> pagingVO = purchaseOrderFeign.generateDeliveryList(dto);
        return success(pagingVO);
    }
    /**
     * srm待发货列表合计
     * @author zdy
     * @date: 2024/1/15 17:34
     * @param dto
     * @return ApiResult<PagingVO<PurchaseOrderDTO.listDTO>>
     */
    @PostMapping("/srmWaitDeliveryTotal")
    @WebAdvanceQuery(handler = WaitDeliveryQueryHandler.class)
    public ApiResult<PurchaseOrderDTO.ListDTO> srmWaitDeliveryTotal(@RequestBody @Validated PurchaseOrderDTO.SrmSearchParamDTO dto) {
        dto.setSupplierId(userService.getSupplierId());
//        PurchaseOrderDTO.ListDTO listDTO = purchaseOrderDetailService.srmWaitDeliveryTotal(dto);
        PurchaseOrderDTO.ListDTO listDTO = purchaseOrderFeign.srmWaitDeliveryTotal(dto);
        return success(listDTO);
    }

    /**
     * 生成送货单
     * @author lrp
     * @date:  2024-01-12
     * @param dtos
     * @return ApiResult<String>
     */
    @PostMapping("/generateDeliveryOrder")
    @LogAction(value = LogActionEnum.INSERT, desc = "生成送货单")
    public ApiResult<List<BatchResultDTO>> addDeliveryOrder(@RequestBody @Valid ValidList<DeliveryOrderDTO.AddDeliveryDTO> dtos) {
        List<BatchResultDTO> resultDTOS = deliveryOrderService.addDeliveryOrder(dtos);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
