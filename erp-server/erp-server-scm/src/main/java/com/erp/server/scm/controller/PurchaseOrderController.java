package com.erp.server.scm.controller;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.*;
import com.erp.server.scm.service.PurchaseOrderDetailService;
import com.erp.server.scm.service.PurchaseOrderService;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * 采购订单管理
 * @author will
 * @since 2023-03-16
 */
@RestController
@RequestMapping("/purchaseOrder")
public class PurchaseOrderController extends BaseController {

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    /**
     * 分页查询
     * @author Will
     * @date: 2023/3/15 16:47
     * @param dto
     * @return ApiResult<PagingVO<PurchaseOrderDTO.listDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id",
            menuCode = "scm:purchaseOrder:paging",
            tableAlias = "po")
    public ApiResult<PagingVO<PurchaseOrderDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<PurchaseOrderDTO.SearchParamDTO> dto) {
        PagingVO<PurchaseOrderDTO.ListDTO> pagingVO = purchaseOrderService.paging(dto);
        return success(pagingVO);
    }
    
    /**
     * 查询数量
     * @author Will
     * @date: 2023/3/15 17:34
     * @return ApiResult
     */
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id",
            menuCode = "scm:purchaseOrder:paging",
            tableAlias = "po")
    public ApiResult<List<ListStatusCountDTO.PurchaseOrderCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<ListStatusCountDTO.PurchaseOrderCountDTO> list = purchaseOrderService.listCount(dto);
        return success(list);
    }

    /**
     * 新增
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto)
     * @return ApiResult
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated PurchaseOrderDTO.AddDTO dto) {
        purchaseOrderService.add(dto);
        return  success();
    }

    /**
     * 修改
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated PurchaseOrderDTO.UpdateDTO dto) {
        Boolean flag = purchaseOrderService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 新增并提交
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/addAndSubmit")
    public ApiResult addAndSubmit(@RequestBody @Validated PurchaseOrderDTO.AddDTO dto) {
        Boolean flag = purchaseOrderService.addAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改并提交
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/updateAndSubmit")
    public ApiResult updateAndSubmit(@RequestBody @Validated PurchaseOrderDTO.UpdateDTO dto) {
        Boolean flag = purchaseOrderService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查询详情
     * @author Will
     * @date: 2023/3/15 17:44
     * @param id
     * @return ApiResult<PurchaseOrderDTO.viewDTO>
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:purchaseOrder:view",
            serviceClass = PurchaseOrderService.class,
            keyIdName = "id")
    public ApiResult<PurchaseOrderDTO.ViewDTO> view(@Param("id") String id) {
        PurchaseOrderDTO.ViewDTO dto = purchaseOrderService.view(id);
        return success(dto);
    }

    /**
     * 查询关联单据
     * @author Will
     * @date: 2023/4/3 14:32
     * @param dto
     * @return ApiResult<AssociatedDocumentDTO>
     */
    @PostMapping("/viewAssociatedDocuments")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "change_user_id",
            menuCode = "scm:purchaseChange:paging",
            tableAlias = "pc")
    public ApiResult<PurchaseOrderDTO.AssociatedDocumentDTO> viewAssociatedDocuments(@RequestBody @Validated BaseIdDTO dto) {
        PurchaseOrderDTO.AssociatedDocumentDTO resultDTO = purchaseOrderService.viewAssociatedDocuments(dto);
        return success(resultDTO);
    }

    /**
     * 删除
     * @author Will
     * @date: 2023/3/15 17:47
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = purchaseOrderService.delete(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 提交
     * @author Will
     * @date: 2023/3/15 17:47
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/submit")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = purchaseOrderService.submit(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 批量作废
     * @author Will
     * @date: 2023/3/15 17:50
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/invalid")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean flag = purchaseOrderService.invalid(dto.getIds(),dto.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @author Will
     * @date: 2023/3/15 17:54
     * @param baseApproveParamDTO
     * @return ApiResult
     */
    @PostMapping("/approve")
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        purchaseOrderService.approve(baseApproveParamDTO);
        return success();
    }

    /**
     * 批量反审核
     * @author Will
     * @date: 2023/3/15 17:50
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/disApprove")
    public ApiResult disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = purchaseOrderService.disApprove(dto.getIds());
        return flag == true ? success() : failure();
    }


    /**
     * 取消流程
     * @author Will
     * @date: 2023/3/15 17:59
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/cancelProcess")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = purchaseOrderService.cancelProcess(dto.getIds());
        return result == true ? success() : failure();
    }

    /**
     * 采购变更数据显示
     * @author Will
     * @date: 2023/3/31 14:28
     * @param id
     * @return ApiResult<AddDTO>
     */
    @GetMapping("/viewPurchaseChange")
    public ApiResult<PurchaseChangeDTO.ViewDTO> viewPurchaseChange(@RequestParam("id") String id) {
        PurchaseChangeDTO.ViewDTO viewDTO = purchaseOrderService.viewPurchaseChange(id);
        return success(viewDTO);
    }


    /**
     * 下推收货单弹框数据显示
     * @author Will
     * @date: 2023/3/15 18:26
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/viewGenerateReceive")
    public ApiResult<List<PurchaseOrderDTO.ViewGenerateReceiveDTO>> viewGenerateReceive(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<PurchaseOrderDTO.ViewGenerateReceiveDTO> list = purchaseOrderService.viewGenerateReceive(dto.getIds());
        return success(list);
    }


    /**
     * 下推收货单保存
     * @author Will
     * @date: 2023/3/15 18:26
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/generateReceive")
    public ApiResult generateReceive(@RequestBody @Validated PurchaseOrderDTO.ListGenerateReceiveDTO dto) {
        Boolean flag = purchaseOrderService.generateReceive(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 下推采购入库单弹窗显示
     * @author Will
     * @date: 2023/4/13 11:20
     * @param dto
     * @return ApiResult<List<ViewGenerateReceiveDTO>>
     */
    @PostMapping("/viewGenerateStockIn")
    public ApiResult<List<PurchaseOrderDTO.ViewGenerateStockInDTO>> viewGenerateStockIn(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<PurchaseOrderDTO.ViewGenerateStockInDTO> list = purchaseOrderService.viewGenerateStockIn(dto.getIds());
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
        Boolean flag = purchaseOrderService.generateStockIn(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 结束交货
     * @author Will
     * @date: 2023/3/15 17:59
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/finishDelivery")
    public ApiResult finishDelivery(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = purchaseOrderService.finishDelivery(dto.getIds());
        return result == true ? success() : failure();
    }

    /**
     * 导出采购合同PDF
     * @author Will
     * @date: 2023/3/15 17:59
     * @param id
     * @return ApiResult
     */
    @GetMapping("/exportPurchaseContractPdf")
    public ApiResult<PurchaseOrderDTO.ExportPdfDTO> exportPurchaseContractPdf(@RequestParam("id") String id) {
        PurchaseOrderDTO.ExportPdfDTO exportPdfDTO = purchaseOrderService.exportPurchaseContractPdf(id);
        return success(exportPdfDTO);
    }

    /**
     * 导入
     * @author Will
     * @date: 2023/3/15 18:22
     * @param excelImportDTO
     * @param response
     * @return ApiResult
     */
    @PostMapping("/importFile")
    public ApiResult<PurchaseOrderDetailDTO.ImportDTO> importFile(@ModelAttribute @Validated ExcelImportDTO.purchaseOrderExcelImportDTO excelImportDTO, HttpServletResponse response) {
        PurchaseOrderDetailDTO.ImportDTO importDTO = purchaseOrderService.importFile(excelImportDTO.getExcelFile(), excelImportDTO.getSkuIds(),excelImportDTO.getSupplierId(), response);
        return success(importDTO);
    }

    /**
     * 下载模板
     * @author Will
     * @date: 22023/3/15 18:22
     * @param request
     * @param response
     */
    @GetMapping("/exportExcelTemplate")
    public ApiResult exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/purchaseOrderTemplate.xlsx";
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_95131);
        }
        return success();
    }

    /**
     *  导出
     * @author Will
     * @date: 2023/3/15 18:23
     * @param dto
     * @param response
     * @return ApiResult
     */
    @PostMapping(value = "/exportExcel")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id",
            menuCode = "scm:purchaseOrder:paging",
            tableAlias = "po")
    public ApiResult exportExcel(@RequestBody PurchaseOrderDTO.SearchParamDTO dto, HttpServletResponse response) {
        Boolean flag = purchaseOrderService.exportExcel(dto, response);
        return flag == true ? success() : failure();
    }


    /**
     * 查询单个采购订单
     * @author Will
     * @date: 2023/4/17 9:14
     * @param id
     * @return ApiResult<GetOneDTO>
     */
    @GetMapping(value = "/getPurchaseOrder")
    public ApiResult<PurchaseOrderDTO.GetOneDTO> getPurchaseOrder(@RequestParam("id") String id) {
        PurchaseOrderDTO.GetOneDTO getOneDTO = purchaseOrderService.getPurchaseOrder(id);
        return success(getOneDTO);
    }


    /**
     * 根据采购订单id 获取质检产品信息
     * @author yl
     * @date 2023-04-17 18:23
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.scm.dto.PurchaseOrderDTO.GetOneDTO>
     */
    @GetMapping(value = "/getQcProductInfo")
    public ApiResult<PurchaseOrderDTO.GetQcProductDTO> getPurchaseOrder(@RequestParam("id") String id) {
        PurchaseOrderDTO.GetQcProductDTO result = purchaseOrderService.getQcProductInfo(id);
        return success(result);
    }

    /**
     * 添加产品数据显示
     * @author Will
     * @date: 2023/4/14 10:21
     * @param dto
     * @return ApiResult<ViewProductDTO>
     */
    @PostMapping(value = "/viewProduct")
    public ApiResult<List<PurchaseOrderDetailDTO.ViewProductDTO>> viewProduct(@RequestBody @Validated PurchaseOrderDetailDTO.ProductSearchParamDTO dto) {
        List<PurchaseOrderDetailDTO.ViewProductDTO> list = purchaseOrderDetailService.viewProduct(dto);
        return success(list);
    }



}
