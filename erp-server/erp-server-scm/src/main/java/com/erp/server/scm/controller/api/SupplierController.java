package com.erp.server.scm.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.PurchaseOrderSupplierDTO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.server.scm.service.PurchaseOrderSupplierService;
import com.erp.server.scm.service.SupplierService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 供应商管理
 *
 * @author yl
 * @since 2023-03-15
 */
@RestController
@RequestMapping("/supplier")
public class SupplierController extends BaseController {


    @Resource
    private SupplierService supplierService;


    @Resource
    private PurchaseOrderSupplierService purchaseOrderSupplierService;


    /**
     * 供应商分页列表
     *
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id",
            menuCode = "scm:supplier:paging",
            tableAlias = "supplier"
    )
    public ApiResult<PagingVO<SupplierDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<SupplierDTO.PagingParamDTO> dto) {
        PagingVO<SupplierDTO.PagingViewDTO> pagingVO = supplierService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 添加供应商
     *
     * @param dto
     * @return
     */
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:supplier:add",
            serviceClass = SupplierService.class,
            keyIdName = "id"
    )
    public ApiResult add(@RequestBody @Validated SupplierDTO.AddDTO dto) {
        String supplierId = supplierService.addSupplier(dto);
        return StringUtils.isNotBlank(supplierId) ? success() : failure();
    }


    /**
     * 提交并审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:supplier:addAndSubmit",
            serviceClass = SupplierService.class,
            keyIdName = "id"
    )
    public ApiResult addAndSubmit(@RequestBody @Validated SupplierDTO.AddDTO dto) {
        Boolean result = supplierService.addAndSubmit(dto);
        return result == true ? success() : failure();
    }


    /**
     * 修改供应商
     *
     * @param dto
     * @return
     */
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:supplier:update",
            serviceClass = SupplierService.class,
            keyIdName = "id"
    )
    public ApiResult update(@RequestBody @Validated SupplierDTO.UpdateDTO dto) {
        String supplierId = supplierService.updateSupplier(dto);
        return StringUtils.isNotBlank(supplierId) ? success() : failure();
    }

    /**
     * 修改并审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:supplier:updateAndSubmit",
            serviceClass = SupplierService.class,
            keyIdName = "id"
    )
    public ApiResult updateAndSubmit(@RequestBody @Validated SupplierDTO.UpdateDTO dto) {
        Boolean result = supplierService.updateAndSubmit(dto);
        return result == true ? success() : failure();
    }


    /**
     * 供应商详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:supplier:view",
            serviceClass = SupplierService.class,
            keyIdName = "id"
    )
    public ApiResult<SupplierDTO.SupplierViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        SupplierDTO.SupplierViewDTO view = supplierService.view(dto.getId());
        return success(view);
    }


    /**
     * 删除供应商
     *
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:supplier:delete",
            serviceClass = SupplierService.class,
            keyIdName = "ids"
    )
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = supplierService.deleteByIds(dto.getIds());
        return result == true ? success() : failure();
    }


    /**
     * 供应商提交审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:supplier:submit",
            serviceClass = SupplierService.class,
            keyIdName = "ids"
    )
    public ApiResult submit(@RequestBody BaseIdsDTO.IdsDTO dto) {
        Boolean result = supplierService.submit(dto.getIds());
        return result == true ? success() : failure();
    }

    /**
     * 启用供应商
     *
     * @param dto
     * @return
     */
    @PostMapping("/updateStatus")
    public ApiResult updateStatus(@RequestBody @Validated UpdateStateDTO dto) {
        Boolean result = supplierService.updateStatus(dto);
        return result == true ? success() : failure();
    }

    /**
     * 审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:supplier:approve",
            serviceClass = SupplierService.class,
            keyIdName = "ids"
    )
    public ApiResult audit(@RequestBody @Validated BaseApproveParamDTO dto) {
        Boolean result = supplierService.approve(dto);
        return result == true ? success() : failure();
    }

    /**
     * 反审核
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-03-22 11:56
     */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:supplier:disApprove",
            serviceClass = SupplierService.class,
            keyIdName = "ids"
    )
    public ApiResult disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = supplierService.disApprove(dto.getIds());
        return flag == true ? success() : failure();
    }


    /**
     * 供应商导入
     */
    @PostMapping("/import")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = supplierService.importFile(excelFile, response);
        return result == true ? success() : failure();
    }

    /**
     * 供应商导出
     */
    @PostMapping("/exportSupplier")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id",
            menuCode = "scm:supplier:paging",
            tableAlias = "supplier"
    )
    public ApiResult exportSupplier(@RequestBody @Valid SupplierDTO.ExportDTO dto, HttpServletResponse response) {
        supplierService.exportSupplier(dto, response);
        return success();
    }

    /**
     * 下载模板
     *
     * @return
     */
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        supplierService.downloadTemplate(response);
        return success();
    }


    /**
     * 供应商采购记录
     * 分页
     */
    @PostMapping("/purchasePaging")
    public ApiResult<PagingVO<PurchaseOrderSupplierDTO.SupplierPurchaseDTO>> purchasePaging(@RequestBody @Validated PagingDTO<BaseIdDTO> dto) {
        PagingVO<PurchaseOrderSupplierDTO.SupplierPurchaseDTO> pagingVO = purchaseOrderSupplierService.supplierPurchasePaging(dto);
        return success(pagingVO);
    }


    /**
     * 获取供应商的信息
     *
     * @return
     */
    @GetMapping("/getSupplierInfo")
    public ApiResult<SupplierDTO.ViewDTO> getSupplierContact(@RequestParam(value = "supplierId") String supplierId) {
        SupplierDTO.ViewDTO viewDTO = supplierService.getBySupplierId(supplierId);
        return success(viewDTO);
    }


    /**
     * 根据供应商类型 获取到 对应供应商
     * logistics 物流供应商
     * other 货代供应商
     * @return
     */
    @GetMapping("/listSupplierByCategoryType")
    public ApiResult<List<BaseIdDTO>> listSupplierByCategoryType(@RequestParam("categoryType") String categoryType) {
        List<BaseIdDTO> list = supplierService.listSupplierByCategoryType(categoryType);
        return success(list);
    }

}
