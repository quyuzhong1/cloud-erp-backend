package com.erp.server.wms.controller;


import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.server.wms.service.PurchaseReturnOrderService;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.common.core.controller.BaseController;

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
    public ApiResult exportExcel(@RequestBody PurchaseReturnOrderDTO.PagingParamDTO dto, HttpServletResponse response) {
        Boolean flag = purchaseReturnOrderService.exportExcel(dto, response);
        return flag == true ? success() : failure();
    }
}
