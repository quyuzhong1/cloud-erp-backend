package com.erp.server.wms.controller;


import com.common.business.dto.base.*;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.PurchaseStockInDTO;
import com.erp.server.wms.service.PurchaseStockInService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 采购入库单
 *
 * @author will
 * @since 2023-04-10
 */
@RestController
@RequestMapping("/purchaseStockIn")
public class PurchaseStockInController extends BaseController {

    @Resource
    private PurchaseStockInService purchaseStorageService;


    /**
     * 列表查询
     * @author Will
     * @date: 2023/4/11 19:56
     * @param dto 
     * @return ApiResult<PagingVO<ListDTO>> 
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<PurchaseStockInDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<PurchaseStockInDTO.SearchParamDTO> dto) {
        PagingVO<PurchaseStockInDTO.ListDTO> pagingVO = purchaseStorageService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 列表数量
     * @author Will
     * @date: 2023/4/11 20:08
     * @param dto
     * @return ApiResult<List<ListStatusCountDTO>>
     */
    @PostMapping("/listCount")
    public ApiResult<List<PurchaseStockInDTO.ListStatusCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<PurchaseStockInDTO.ListStatusCountDTO> list = purchaseStorageService.listCount(dto);
        return success(list);
    }

   /**
    * 新增
    * @author Will
    * @date: 2023/4/11 19:58
    * @param dto 
    * @return ApiResult 
    */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated PurchaseStockInDTO.AddDTO dto) {
        String id = purchaseStorageService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

   /**
    * 新增并提交
    * @author Will
    * @date: 2023/4/11 19:59
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/addAndSubmit")
    public ApiResult addAndSubmit(@RequestBody @Validated PurchaseStockInDTO.AddDTO dto) {
        Boolean flag = purchaseStorageService.addAndSubmit(dto);
        return flag == true ? success() : failure();
    }
    
    /**
     * 修改
     * @author Will
     * @date: 2023/4/11 20:02
     * @param dto 
     * @return ApiResult 
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated PurchaseStockInDTO.UpdateDTO dto) {
        Boolean flag = purchaseStorageService.update(dto);
        return flag == true ? success() : failure();
    }
    
    /**
     * 修改并提交
     * @author Will
     * @date: 2023/4/11 20:02
     * @param dto 
     * @return ApiResult 
     */
    @PostMapping("/updateAndSubmit")
    public ApiResult updateAndSubmit(@RequestBody @Validated PurchaseStockInDTO.UpdateDTO dto) {
        Boolean flag = purchaseStorageService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 提交
     * @author Will
     * @date: 2023/4/11 20:00
     * @param dto 
     * @return ApiResult 
     */
    @PostMapping("/submit")
    public ApiResult submit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = purchaseStorageService.submit(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 查看详情
     * @author Will
     * @date: 2023/4/11 20:10
     * @param id
     * @return ApiResult
     */
    @GetMapping("/view")
    public ApiResult<PurchaseStockInDTO.ViewDTO> view(@RequestParam("id") String id) {
        PurchaseStockInDTO.ViewDTO dto = purchaseStorageService.view(id);
        return success(dto);
    }


    /**
     * 删除
     * @author Will
     * @date: 2023/4/11 20:09
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = purchaseStorageService.delete(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 作废
     * @author Will
     * @date: 2023/4/11 20:11
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/invalid")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean flag = purchaseStorageService.invalid(dto.getIds(),dto.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @author Will
     * @date: 2023/4/11 20:11
     * @param baseApproveParamDTO
     * @return ApiResult
     */
    @PostMapping("/approve")
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        purchaseStorageService.approve(baseApproveParamDTO);
        return success();
    }

    /**
     * 批量反审核
     * @author Will
     * @date: 2023/4/11 20:12
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/disApprove")
    public ApiResult disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = purchaseStorageService.disApprove(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 取消流程
     * @author Will
     * @date: 2023/4/11 20:24
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/cancelProcess")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = purchaseStorageService.cancelProcess(dto.getIds());
        return result == true ? success() : failure();
    }

    /**
     * 导出
     * @author Will
     * @date: 2023/4/11 20:25
     * @param dto
     * @param response
     * @return ApiResult
     */
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody PurchaseStockInDTO.SearchParamDTO dto, HttpServletResponse response) {
        Boolean flag = purchaseStorageService.exportExcel(dto, response);
        return flag == true ? success() : failure();
    }

    /**
     * 下推退货单数据显示
     * @author Will
     * @date: 2023/4/11 20:30
     * @param dto
     * @return ApiResult<ViewGeneratePurchaseReturnOrderDTO>
     */
    @PostMapping("/viewGeneratePurchaseReturnOrder")
    public ApiResult<List<PurchaseStockInDTO.ViewGeneratePurchaseReturnOrderDTO>> viewGeneratePurchaseReturnOrder(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<PurchaseStockInDTO.ViewGeneratePurchaseReturnOrderDTO> list = purchaseStorageService.viewGeneratePurchaseReturnOrder(dto.getIds());
        return success(list);
    }

    /**
     * 下推退货单数据保存
     * @author Will
     * @date: 2023/4/11 20:33
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/generatePurchaseReturnOrder")
    public ApiResult generatePurchaseReturnOrder(@RequestBody @Validated PurchaseStockInDTO.ListGeneratePurchaseReturnOrderDTO dto) {
        Boolean flag = purchaseStorageService.generatePurchaseReturnOrder(dto);
        return flag == true ? success() : failure();
    }

   /**
    * 采购订单-关联入库单
    * @author Will
    * @date: 2023/4/19 16:28
    * @param dto
    * @return ApiResult<List<OrderRefStockInDTO>>
    */
    @PostMapping(value = "/purchaseOrderRefStockIn")
    public ApiResult<List<PurchaseStockInDTO.OrderRefStockInDTO>> purchaseOrderRefStockIn(@RequestBody @Validated BaseIdDTO dto) {
        List<PurchaseStockInDTO.OrderRefStockInDTO> list = purchaseStorageService.purchaseOrderRefStockIn(dto.getId());
        return success(list);
    }
}
