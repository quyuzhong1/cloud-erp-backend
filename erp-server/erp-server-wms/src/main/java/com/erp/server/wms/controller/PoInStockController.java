package com.erp.server.wms.controller;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.wms.dto.PoInstockDTO;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.server.wms.service.PoInstockService;
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
@RequestMapping("/poInStock")
public class PoInStockController extends BaseController {

    @Resource
    private PoInstockService poInstockService;


    /**
     * 列表查询
     * @author Will
     * @date: 2023/4/11 19:56
     * @param dto 
     * @return ApiResult<PagingVO<ListDTO>> 
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:poInStock:paging",
            tableAlias = "psi"
    )
    public ApiResult<PagingVO<PoInstockDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<PoInstockDTO.SearchParamDTO> dto) {
        PagingVO<PoInstockDTO.ListDTO> pagingVO = poInstockService.paging(dto);
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
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:poInStock:paging",
            tableAlias = "psi"
    )
    public ApiResult<List<PoInstockDTO.ListStatusCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<PoInstockDTO.ListStatusCountDTO> list = poInstockService.listCount(dto);
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:poInStock:add",
            serviceClass = PoInstockService.class,
            keyIdName = "id")
    public ApiResult add(@RequestBody @Validated PoInstockDTO.AddDTO dto) {
        String id = poInstockService.add(dto);
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:poInStock:addAndSubmit",
            serviceClass = PoInstockService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated PoInstockDTO.AddDTO dto) {
        String id = poInstockService.addAndSubmit(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }
    
    /**
     * 修改
     * @author Will
     * @date: 2023/4/11 20:02
     * @param dto 
     * @return ApiResult 
     */
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:poInStock:update",
            serviceClass = PoInstockService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated PoInstockDTO.UpdateDTO dto) {
        Boolean flag = poInstockService.update(dto);
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:poInStock:updateAndSubmit",
            serviceClass = PoInstockService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated PoInstockDTO.UpdateDTO dto) {
        Boolean flag = poInstockService.updateAndSubmit(dto);
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:poInStock:submit",
            serviceClass = PoInstockService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = poInstockService.submit(dto.getIds());
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:poInStock:view",
            serviceClass = PoInstockService.class,
            keyIdName = "id")
    public ApiResult<PoInstockDTO.ViewDTO> view(@RequestParam("id") String id) {
        PoInstockDTO.ViewDTO dto = poInstockService.view(id);
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:poInStock:delete",
            serviceClass = PoInstockService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = poInstockService.delete(dto.getIds());
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:poInStock:invalid",
            serviceClass = PoInstockService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean flag = poInstockService.invalid(dto.getIds(),dto.getRemark());
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:poInStock:approve",
            serviceClass = PoInstockService.class,
            keyIdName = "ids")
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        poInstockService.approve(baseApproveParamDTO);
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:poInStock:disApprove",
            serviceClass = PoInstockService.class,
            keyIdName = "ids")
    public ApiResult disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = poInstockService.disApprove(dto.getIds());
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:poInStock:cancelProcess",
            serviceClass = PoInstockService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = poInstockService.cancelProcess(dto.getIds());
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
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:poInStock:paging",
            tableAlias = "psi"
    )
    public ApiResult exportExcel(@RequestBody PoInstockDTO.SearchParamDTO dto, HttpServletResponse response) {
        Boolean flag = poInstockService.exportExcel(dto, response);
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
    public ApiResult<List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO>> viewGeneratePurchaseReturnOrder(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO> list = poInstockService.viewGeneratePurchaseReturnOrder(dto.getIds());
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:poInStock:generatePurchaseReturnOrder",
            serviceClass = PoInstockService.class,
            keyIdName = "id")
    public ApiResult generatePurchaseReturnOrder(@RequestBody @Validated PoInstockDTO.ListGeneratePurchaseReturnOrderDTO dto) {
        Boolean flag = poInstockService.generatePurchaseReturnOrder(dto);
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
    public ApiResult<List<PoInstockDTO.OrderRefStockInDTO>> purchaseOrderRefStockIn(@RequestBody @Validated BaseIdDTO dto) {
        List<PoInstockDTO.OrderRefStockInDTO> list = poInstockService.purchaseOrderRefStockIn(dto.getId());
        return success(list);
    }

    /**
     * 下推采购入库单保存
     * @author Will
     * @date: 2023/4/13 11:37
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/generateStockIn")
    public ApiResult generateStockIn(@RequestBody @Validated PurchaseOrderDTO.ListGenerateStockInDTO dto) {
        Boolean flag = poInstockService.generateStockIn(dto);
        return flag == true ? success() : failure();
    }
}
