package com.erp.server.scm.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.dto.SubcontractOrderDTO;
import com.erp.server.scm.service.SubcontractOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 委外订单
 *
 * @author will
 * @since 2023-06-08
 */
@RestController
@RequestMapping("/subcontractOrder")
public class SubcontractOrderController extends BaseController {

    @Autowired
    private SubcontractOrderService subcontractOrderService;

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:tabList",
            tableAlias = "so"
    )
    public ApiResult<List<SubcontractOrderDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(subcontractOrderService.tabList(dto));
    }

    /**
    * 列表查询
    * @author will
    * @date: 2023-06-08
    * @param dto
    * @return ApiResult<PagingVO<SubcontractOrderDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:paging",
            tableAlias = "so"
    )
    public ApiResult<PagingVO<SubcontractOrderDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SubcontractOrderDTO.PagingParamDTO> dto) {
        return success(subcontractOrderService.paging(dto));
    }

    /**
     * 列表根据明细id查询采购订单
     * @author Will
     * @date: 2023/6/9 16:16
     * @param dto
     * @return ApiResult<List<PurchaseOrderDTO.ListDTO>>
     */
    @PostMapping("/listPurchaseOrderByDetailId")
    public ApiResult<List<PurchaseOrderDTO.ListDTO>> listPurchaseOrderByDetailId(@RequestBody @Validated BaseIdDTO dto) {
        return success(subcontractOrderService.listPurchaseOrderByDetailId(dto.getId()));
    }


   /**
   * 新增
   * @author will
   * @date:  2023-06-08
   * @param dto
   * @return ApiResult<Void>
   */
   @PostMapping("/add")
   @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
           tableField = "create_user_id",
           menuCode = "scm:subcontractOrder:add",
           serviceClass = SubcontractOrderService.class,
           keyIdName = "id")
   public ApiResult<Void> add(@RequestBody @Validated SubcontractOrderDTO.AddDTO dto) {
      subcontractOrderService.add(dto);
      return success();
   }

    /**
    * 修改
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:update",
            serviceClass = SubcontractOrderService.class,
            keyIdName = "id")
    public ApiResult<Void> update(@RequestBody @Validated SubcontractOrderDTO.UpdateDTO dto) {
        subcontractOrderService.update(dto);
        return success();
    }

    /**
    * 新增并提交审核
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:addAndSubmit",
            serviceClass = SubcontractOrderService.class,
            keyIdName = "id")
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated SubcontractOrderDTO.AddDTO dto) {
        subcontractOrderService.addAndSubmit(dto);
        return success();
    }

    /**
    * 修改并提交审核
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:updateAndSubmit",
            serviceClass = SubcontractOrderService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated SubcontractOrderDTO.UpdateDTO dto) {
        subcontractOrderService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:submit",
            serviceClass = SubcontractOrderService.class,
            keyIdName = "ids")
    public ApiResult<Void> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        subcontractOrderService.submit(dto.getIds());
        return success();
    }

    /**
    * 审核
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:approve",
            serviceClass = SubcontractOrderService.class,
            keyIdName = "ids")
    public ApiResult<Void> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        subcontractOrderService.approve(dto);
        return success();
    }

    /**
    * 反审核
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:disApprove",
            serviceClass = SubcontractOrderService.class,
            keyIdName = "ids")
    public ApiResult<Void> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        subcontractOrderService.disApprove(dto.getIds());
        return success();
    }

    /**
     * 结束交货
     * @author Will
     * @date: 2023/6/9 16:33
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/finishDelivery")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:finishDelivery",
            serviceClass = SubcontractOrderService.class,
            keyIdName = "ids")
    public ApiResult finishDelivery(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean result = subcontractOrderService.finishDelivery(dto.getIds(),dto.getRemark());
        return result == true ? success() : failure();
    }


    /**
    * 删除
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:delete",
            serviceClass = SubcontractOrderService.class,
            keyIdName = "ids")
    public ApiResult<Void> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        subcontractOrderService.delete(dto.getIds());
        return success();
    }

    /**
    * 撤销
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:cancel",
            serviceClass = SubcontractOrderService.class,
            keyIdName = "ids")
    public ApiResult<Void> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        subcontractOrderService.cancelProcess(dto.getIds());
        return success();
    }

    /**
     * 批量作废
     * @author Will
     * @date: 2023-06-08
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:invalid",
            serviceClass = SubcontractOrderService.class,
            keyIdName = "ids")
    public ApiResult<Void> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        subcontractOrderService.invalid(dto.getIds(),dto.getRemark());
        return success();
    }

    /**
    * 详情
    * @author will
    * @date:  2023-06-08
    * @param id
    * @return ApiResult<SubcontractOrderDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:view",
            serviceClass = SubcontractOrderService.class,
            keyIdName = "id")
    public ApiResult<SubcontractOrderDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(subcontractOrderService.view(id));
    }

    /**
    * 导出Excel数据
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:export",
            tableAlias = "so"
    )
    public void exportList(@RequestBody @Validated SubcontractOrderDTO.ExportDTO dto, HttpServletResponse response) {
        subcontractOrderService.exportList(dto, response);
    }


    /**
     * 下推采购单显示
     * @author Will
     * @date: 2023/6/12 14:00
     * @param dto
     * @return ApiResult<List<ViewGeneratePoDTO>>
     */
    @PostMapping(value = "/viewGeneratePo")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:viewGeneratePo",
            tableAlias = "so"
    )
    public ApiResult<List<SubcontractOrderDTO.ViewGeneratePoDTO>> viewGeneratePo(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<SubcontractOrderDTO.ViewGeneratePoDTO> list = subcontractOrderService.viewGeneratePo(dto.getIds());
        return success(list);
    }

    /**
     * 下推采购单保存
     * @author Will
     * @date: 2023/6/12 14:00
     * @param list
     * @return ApiResult<Void>
     */
    @PostMapping(value = "/generatePo")
    public ApiResult<Void> generatePo(@RequestBody @Validated ValidList<SubcontractOrderDTO.GeneratePoDTO> list) {
         subcontractOrderService.generatePo(list);
        return success();
    }

    /**
     * 添加已有产品显示
     * @author Will
     * @date: 2023/6/12 16:00
     * @param dto
     * @return ApiResult<List<ViewAddDetailDTO>>
     */
    @PostMapping(value = "/viewAddDetail")
    public ApiResult<List<SubcontractOrderDTO.ViewAddDetailDTO>> viewAddDetail(@RequestBody @Validated SubcontractOrderDTO.ViewAddDetailParamDTO dto) {
        List<SubcontractOrderDTO.ViewAddDetailDTO> list = subcontractOrderService.viewAddDetail(dto);
        return success(list);
    }

}
