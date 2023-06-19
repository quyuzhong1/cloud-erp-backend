package com.erp.server.wms.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.PurchaseOrderService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.PurchaseOrderDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 采购订单表
 *
 * @author Luo_WG
 * @since 2023-06-19
 */
@RestController
@RequestMapping("/purchaseOrder")
public class PurchaseOrderController extends BaseController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:purchaseOrder:paging",
            tableAlias = ""
    )
    public ApiResult<List<PurchaseOrderDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(purchaseOrderService.tabList(dto));
    }

    /**
    * 列表查询
    * @author Luo_WG
    * @date: 2023-06-19
    * @param dto
    * @return ApiResult<PagingVO<PurchaseOrderDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:purchaseOrder:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<PurchaseOrderDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<PurchaseOrderDTO.PagingParamDTO> dto) {
        return success(purchaseOrderService.paging(dto));
    }

   /**
   * 新增
   * @author Luo_WG
   * @date:  2023-06-19
   * @param dto
   * @return ApiResult<Void>
   */
   @PostMapping("/add")
   @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
           tableField = "create_user_id",
           menuCode = "wms:purchaseOrder:add",
           serviceClass = PurchaseOrderService.class,
           keyIdName = "id")
   public ApiResult<Void> add(@RequestBody @Validated PurchaseOrderDTO.AddDTO dto) {
      purchaseOrderService.add(dto);
      return success();
   }

    /**
    * 修改
    * @author Luo_WG
    * @date:  2023-06-19
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:purchaseOrder:update",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "id")
    public ApiResult<Void> update(@RequestBody @Validated PurchaseOrderDTO.UpdateDTO dto) {
        purchaseOrderService.update(dto);
        return success();
    }

    /**
    * 新增并提交审核
    * @author Luo_WG
    * @date:  2023-06-19
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:purchaseOrder:addAndSubmit",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "id")
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated PurchaseOrderDTO.AddDTO dto) {
        purchaseOrderService.addAndSubmit(dto);
        return success();
    }

    /**
    * 修改并提交审核
    * @author Luo_WG
    * @date:  2023-06-19
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:purchaseOrder:updateAndSubmit",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated PurchaseOrderDTO.UpdateDTO dto) {
        purchaseOrderService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author Luo_WG
    * @date:  2023-06-19
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:purchaseOrder:submit",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "ids")
    public ApiResult<Void> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        purchaseOrderService.submit(dto.getIds());
        return success();
    }

    /**
    * 审核
    * @author Luo_WG
    * @date:  2023-06-19
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:purchaseOrder:approve",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "ids")
    public ApiResult<Void> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        purchaseOrderService.approve(dto);
        return success();
    }

    /**
    * 反审核
    * @author Luo_WG
    * @date:  2023-06-19
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:purchaseOrder:disApprove",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "ids")
    public ApiResult<Void> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        purchaseOrderService.disApprove(dto.getIds());
        return success();
    }


    /**
    * 删除
    * @author Luo_WG
    * @date:  2023-06-19
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:purchaseOrder:delete",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "ids")
    public ApiResult<Void> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        purchaseOrderService.delete(dto.getIds());
        return success();
    }
    /**
    * 作废
    * @author Luo_WG
    * @date:  2023-06-19
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:purchaseOrder:invalid",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "ids")
    public ApiResult<Void> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        purchaseOrderService.invalid(dto.getIds(), dto.getRemark());
        return success();
    }

    /**
    * 撤销
    * @author Luo_WG
    * @date:  2023-06-19
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:purchaseOrder:cancel",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "ids")
    public ApiResult<Void> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        purchaseOrderService.cancelProcess(dto.getIds());
        return success();
    }

    /**
    * 详情
    * @author Luo_WG
    * @date:  2023-06-19
    * @param id
    * @return ApiResult<PurchaseOrderDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:purchaseOrder:view",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "id")
    public ApiResult<PurchaseOrderDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(purchaseOrderService.view(id));
    }

    /**
    * 导出Excel数据
    * @author Luo_WG
    * @date:  2023-06-19
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:purchaseOrder:export",
            tableAlias = ""
    )
    public void exportList(@RequestBody @Validated PurchaseOrderDTO.ExportDTO dto, HttpServletResponse response) {
        purchaseOrderService.exportList(dto, response);
    }


}
