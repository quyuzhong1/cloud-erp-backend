package com.erp.server.scm.controller;


import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.server.scm.service.PurchaseOrderService;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    /**
     * 分页查询
     * @author Will
     * @date: 2023/3/15 16:47
     * @param dto
     * @return ApiResult<PagingVO<PurchaseOrderDTO.listDTO>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<PurchaseOrderDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<PurchaseOrderDTO.SearchParamDTO> dto) {
        PagingVO<PurchaseOrderDTO.ListDTO> pagingVO = purchaseOrderService.paging(dto);
        return success(pagingVO);
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
     * 查询详情
     * @author Will
     * @date: 2023/3/15 17:44
     * @param id
     * @return ApiResult<PurchaseOrderDTO.viewDTO>
     */
    @GetMapping("/view")
    public ApiResult<PurchaseOrderDTO.ViewDTO> view(@Param("id") String id) {
        PurchaseOrderDTO.ViewDTO dto = purchaseOrderService.view(id);
        return success(dto);
    }

    /**
     * 删除
     * @author Will
     * @date: 2023/3/15 17:47
     * @param ids
     * @return ApiResult
     */
    @PostMapping("/delete")
    public ApiResult delete(@RequestParam("ids") List<String> ids) {
        Boolean flag = purchaseOrderService.delete(ids);
        return flag == true ? success() : failure();
    }

    /**
     * 提交
     * @author Will
     * @date: 2023/3/15 17:47
     * @param ids
     * @return ApiResult
     */
    @PostMapping("/submit")
    public ApiResult submit(@RequestParam("ids") List<String> ids) {
        Boolean flag = purchaseOrderService.submit(ids);
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
    @PostMapping("/disApprove")
    public ApiResult disApprove(@RequestParam("ids") List<String> ids) {
        Boolean flag = purchaseOrderService.disApprove(ids);
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
     *  导出
     * @author Will
     * @date: 2023/3/15 18:23
     * @param dto
     * @param response
     * @return ApiResult
     */
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody PurchaseOrderDTO.SearchParamDTO dto, HttpServletResponse response) {
        Boolean flag = purchaseOrderService.exportExcel(dto, response);
        return flag == true ? success() : failure();
    }

}
