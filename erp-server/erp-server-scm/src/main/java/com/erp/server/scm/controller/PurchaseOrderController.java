package com.erp.server.scm.controller;


import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.*;
import com.erp.server.scm.service.PurchaseOrderService;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.common.core.controller.BaseController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * <p>
 * 采购订单表 前端控制器
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@RestController
@RequestMapping("/purchaseOrder")
public class PurchaseOrderController extends BaseController {

    @Resource
    private PurchaseOrderService purchaseOrderService;

    /**
     * 分页查询
     * @author Will
     * @date: 2023/3/15 16:47
     * @param dto
     * @return ApiResult<PagingVO<List<ScmSalesDemandDTO>>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<List<PurchaseOrderPagingViewDTO>>> queryByPage(@RequestBody @Validated PagingDTO<PurchaseOrderPagingParamDTO> dto) {
        PagingVO<List<PurchaseOrderPagingViewDTO>> pagingVO = purchaseOrderService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 获取采购单号
     * @author Will
     * @date: 2023/3/16 10:52
     * @return ApiResult
     */
    @GetMapping("/getCode")
    public ApiResult getCode() {
        String code = purchaseOrderService.getCode();
        return success(code);
    }

    /**
     * 新增
     * @author Will
     * @date: 2023/3/15 17:34
     * @param scmPurchaseOrderDTO
     * @return ApiResult
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated PurchaseOrderDTO scmPurchaseOrderDTO) {
        Boolean flag = purchaseOrderService.add(scmPurchaseOrderDTO);
        return flag == true ? success() : failure();
    }

    /**
     * 修改
     * @author Will
     * @date: 2023/3/15 17:34
     * @param scmPurchaseOrderDTO
     * @return ApiResult
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated PurchaseOrderDTO scmPurchaseOrderDTO) {
        Boolean flag = purchaseOrderService.update(scmPurchaseOrderDTO);
        return flag == true ? success() : failure();
    }

    /**
     * 查询详情
     * @author Will
     * @date: 2023/3/15 17:44
     * @param id
     * @return ApiResult<scmPurchaseOrderDTO>
     */
    @GetMapping("/view")
    public ApiResult<PurchaseOrderDTO> view(@Param("id") String id) {
        PurchaseOrderDTO scmPurchaseOrderDTO = purchaseOrderService.view(id);
        return success(scmPurchaseOrderDTO);
    }

    /**
     * 删除
     * @author Will
     * @date: 2023/3/15 17:47
     * @param id
     * @return ApiResult
     */
    @PostMapping("/delete")
    public ApiResult delete(@RequestParam("id") String id) {
        Boolean flag = purchaseOrderService.delete(id);
        return flag == true ? success() : failure();
    }

    /**
     * 提交
     * @author Will
     * @date: 2023/3/15 17:47
     * @param id
     * @return ApiResult
     */
    @PostMapping("/commit")
    public ApiResult commit(@RequestParam("id") String id) {
        Boolean flag = purchaseOrderService.commit(id);
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
     * @param ids
     * @return ApiResult
     */
    @PostMapping("/unApprove")
    public ApiResult unAudit(@RequestParam("ids") List<String> ids) {
        Boolean flag = purchaseOrderService.unApprove(ids);
        return flag == true ? success() : failure();
    }


    /**
     * 取消流程
     * @author Will
     * @date: 2023/3/15 17:59
     * @param id
     * @return ApiResult
     */
    @PostMapping("/cancelProcess")
    public ApiResult cancelProcess(@RequestParam("id") String id) {
        Boolean result = purchaseOrderService.cancelProcess(id);
        return result == true ? success() : failure();
    }

    /**
     * 结束交货
     * @author Will
     * @date: 2023/3/15 17:59
     * @param id
     * @return ApiResult
     */
    @PostMapping("/finishDelivery")
    public ApiResult finishDelivery(@RequestParam("id") String id) {
        Boolean result = purchaseOrderService.finishDelivery(id);
        return result == true ? success() : failure();
    }

    /**
     * 再次购买
     * @author Will
     * @date: 2023/3/15 17:59
     * @param id
     * @return ApiResult
     */
    @PostMapping("/copy")
    public ApiResult copy(@RequestParam("id") String id) {
        Boolean result = purchaseOrderService.copy(id);
        return result == true ? success() : failure();
    }

    /**
     * 采购变更
     * @author Will
     * @date: 2023/3/15 17:59
     * @param id
     * @return ApiResult
     */
    @PostMapping("/purchaseChange")
    public ApiResult purchaseChange(@RequestParam("id") String id) {
        Boolean result = purchaseOrderService.purchaseChange(id);
        return result == true ? success() : failure();
    }


    /**
     * 仓库签收单弹框数据显示
     * @author Will
     * @date: 2023/3/15 17:44
     * @param id
     * @return ApiResult<scmPurchaseOrderDTO>
     */
    @GetMapping("/viewForWarehouseReceive")
    public ApiResult<List<PurchaseOrderViewDTO>> viewForWarehouseReceive(@Param("id") String id) {
        List<PurchaseOrderViewDTO> list = purchaseOrderService.viewForWarehouseReceive(id);
        return success(list);
    }

    /**
     * 下推签收保存
     * @author Will
     * @date: 2023/3/15 17:59
     * @param purchaseOrderViewDTO
     * @return ApiResult
     */
    @PostMapping("/generateWarehouseReceive")
    public ApiResult generateWarehouseReceive(@RequestBody @Validated PurchaseOrderViewDTO purchaseOrderViewDTO) {
        Boolean result = purchaseOrderService.generateWarehouseReceive(purchaseOrderViewDTO);
        return result == true ? success() : failure();
    }


    /**
     * 导出采购合同PDF
     * @author Will
     * @date: 2023/3/15 17:59
     * @param id
     * @return ApiResult
     */
    @PostMapping("/exportPurchaseContractPdf")
    public ApiResult exportPurchaseContractPdf(@RequestParam("id") String id) {
        Boolean result = purchaseOrderService.exportPurchaseContractPdf(id);
        return result == true ? success() : failure();
    }

    /**
     * 导入
     * @author Will
     * @date: 2023/3/15 18:22
     * @param excelFile
     * @param response
     * @return ApiResult
     */
    @PostMapping("/importFile")
    public ApiResult importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean flag = purchaseOrderService.importFile(excelFile,response);
        return flag == true ? success() : failure();
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
        String path = "classpath:excel/purchaseOrder.xlsx";
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
     * @description: 导出
     * @author Will
     * @date: 2023/3/15 18:23
     * @param purchaseOrderPagingParamDTO
     * @param response
     * @return ApiResult
     */
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody PurchaseOrderPagingParamDTO purchaseOrderPagingParamDTO, HttpServletResponse response) {
        Boolean flag = purchaseOrderService.exportExcel(purchaseOrderPagingParamDTO, response);
        return flag == true ? success() : failure();
    }

}
