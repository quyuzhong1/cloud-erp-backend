package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.PoInstockDTO;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.server.wms.service.PoInstockService;
import com.erp.server.wms.service.PurchaseReturnOrderService;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 采购退货单
 * @author LUO_WG
 * @since 2023-04-07
 */
@RestController
@RequestMapping("/purchaseReturnOrder")
public class PurchaseReturnOrderController extends BaseController {
    @Resource
    private PurchaseReturnOrderService purchaseReturnOrderService;

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.WarehouseReceiveDTO.PagingViewDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "return_user_id",
            menuCode = "wms:purchaseReturnOrder:paging",
            tableAlias = "pro"
    )
    public ApiResult<PagingVO<PurchaseReturnOrderDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<PurchaseReturnOrderDTO.PagingParamDTO> dto) {
        PagingVO<PurchaseReturnOrderDTO.PagingViewDTO> pagingVO = purchaseReturnOrderService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/4/17 13:14
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.WarehouseReceiveDTO.WarehouseReceiveCountDTO>>
     **/
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "return_user_id",
            menuCode = "wms:purchaseReturnOrder:paging",
            tableAlias = "pro")
    public ApiResult<List<PurchaseReturnOrderDTO.ReturnOrderCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<PurchaseReturnOrderDTO.ReturnOrderCountDTO> warehouseReceiveCountDTOS = purchaseReturnOrderService.listCount(dto);
        return success(warehouseReceiveCountDTOS);
    }

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated PurchaseReturnOrderDTO.AddDTO dto) {
        String id = purchaseReturnOrderService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,return_user_id",
            menuCode = "wms:purchaseReturnOrder:update",
            serviceClass = PurchaseReturnOrderService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated PurchaseReturnOrderDTO.UpdateDTO dto) {
        Boolean flag = purchaseReturnOrderService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/4/6 18:57
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.WarehouseReceiveDTO.ViewDTO>
     **/
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "return_user_id",
            menuCode = "wms:purchaseReturnOrder:view",
            serviceClass = PurchaseReturnOrderService.class,
            keyIdName = "id")
    public ApiResult<PurchaseReturnOrderDTO.ViewDTO> view(@Param("id") String id) {
        PurchaseReturnOrderDTO.ViewDTO dto = purchaseReturnOrderService.view(id);
        return success(dto);
    }

    /**
     * 提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,return_user_id",
            menuCode = "wms:purchaseReturnOrder:submit",
            serviceClass = PurchaseReturnOrderService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = purchaseReturnOrderService.submit(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 新增提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,return_user_id",
            menuCode = "wms:purchaseReturnOrder:addAndSubmit",
            serviceClass = PurchaseReturnOrderService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated PurchaseReturnOrderDTO.AddDTO dto) {
        Boolean flag = purchaseReturnOrderService.addAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,return_user_id",
            menuCode = "wms:purchaseReturnOrder:updateAndSubmit",
            serviceClass = PurchaseReturnOrderService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated PurchaseReturnOrderDTO.UpdateDTO dto) {
        Boolean flag = purchaseReturnOrderService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param baseApproveParamDTO baseApproveParamDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,return_user_id",
            menuCode = "wms:purchaseReturnOrder:approve",
            serviceClass = PurchaseReturnOrderService.class,
            keyIdName = "ids")
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        Boolean flag = purchaseReturnOrderService.approve(baseApproveParamDTO);
        return flag == true ? success() : failure();
    }

    /**
     * 批量反审核审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,return_user_id",
            menuCode = "wms:purchaseReturnOrder:disApprove",
            serviceClass = PurchaseReturnOrderService.class,
            keyIdName = "ids")
    public ApiResult disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = purchaseReturnOrderService.disApprove(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 取消流程
     * @Author Luo_WG
     * @Date 2023/4/13 18:58
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,return_user_id",
            menuCode = "wms:purchaseReturnOrder:cancelProcess",
            serviceClass = PurchaseReturnOrderService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = purchaseReturnOrderService.cancelProcess(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 批量作废
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param remarkDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,return_user_id",
            menuCode = "wms:purchaseReturnOrder:invalid",
            serviceClass = PurchaseReturnOrderService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO remarkDTO) {
        Boolean flag = purchaseReturnOrderService.invalid(remarkDTO.getIds(), remarkDTO.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 批量删除
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param idsDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,return_user_id",
            menuCode = "wms:purchaseReturnOrder:delete",
            serviceClass = PurchaseReturnOrderService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        Boolean flag = purchaseReturnOrderService.delete(idsDTO.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 采购订单-关联的退货单据
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param purchaseOrderId purchaseOrderId
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping(value = "/purchaseOrderRefReturn")
    public ApiResult<List<PurchaseReturnOrderDTO.OrderRefReceiveDTO>> purchaseOrderRefReturn(@RequestBody @RequestParam("purchaseOrderId") String purchaseOrderId) {
        List<PurchaseReturnOrderDTO.OrderRefReceiveDTO> orderRefReceiveDTOS = purchaseReturnOrderService.purchaseOrderRefReturn(purchaseOrderId);
        return success(orderRefReceiveDTOS);
    }

    /**
     * 导出
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param dto dto
     * @param response response
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping(value = "/exportExcel")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id",
            menuCode = "scm:purchaseOrder:paging",
            tableAlias = "po")
    public ApiResult exportExcel(@RequestBody PurchaseReturnOrderDTO.PagingParamDTO dto, HttpServletResponse response) {
        Boolean flag = purchaseReturnOrderService.exportExcel(dto, response);
        return flag == true ? success() : failure();
    }


    /**
     * 采购订单下推退货单
     * @author yl
     * @date 2023-05-08 11:03
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     */
    @PostMapping("/generatePurchaseReturnOrder")
    public ApiResult generatePurchaseReturnOrder(@RequestBody @Validated PoInstockDTO.ListGeneratePurchaseReturnOrderDTO dto) {
        Boolean  flag = purchaseReturnOrderService.generatePurchaseReturnOrder(dto);
        return flag?success():failure();
    }

}
