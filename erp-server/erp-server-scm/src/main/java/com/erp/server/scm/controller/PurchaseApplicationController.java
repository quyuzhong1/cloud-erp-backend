package com.erp.server.scm.controller;


import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.*;
import com.erp.server.scm.service.PurchaseApplicationService;
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
 * 采购申请
 *
 * @author will
 * @since 2023-03-15
 */
@RestController
@RequestMapping("/purchaseApplication")
public class PurchaseApplicationController extends BaseController {

    @Resource
    private PurchaseApplicationService purchaseApplicationService;

    /**
     * 分页查询
     * @author Will
     * @date: 2023/3/15 16:47
     * @param dto
     * @return ApiResult<PagingVO<List<ScmPurchaseApplicationViewDTO>>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<List<PurchaseApplicationPagingViewDTO>>> queryByPage(@RequestBody @Validated PagingDTO<PurchaseApplicationPagingParamDTO> dto) {
        PagingVO<List<PurchaseApplicationPagingViewDTO>> pagingVO = purchaseApplicationService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 新增
     * @author Will
     * @date: 2023/3/15 17:34
     * @param purchaseApplicationDTO
     * @return ApiResult
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated PurchaseApplicationDTO purchaseApplicationDTO) {
        Boolean flag = purchaseApplicationService.add(purchaseApplicationDTO);
        return flag == true ? success() : failure();
    }

    /**
     * 修改
     * @author Will
     * @date: 2023/3/15 17:34
     * @param purchaseApplicationDTO
     * @return ApiResult
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated PurchaseApplicationDTO purchaseApplicationDTO) {
        Boolean flag = purchaseApplicationService.update(purchaseApplicationDTO);
        return flag == true ? success() : failure();
    }


    /**
     * 批量提交
     * @author Will
     * @date: 2023/3/15 17:47
     * @param ids
     * @return ApiResult
     */
    @PostMapping("/submit")
    public ApiResult submit(@RequestParam("ids") List<String> ids) {
        Boolean flag = purchaseApplicationService.submit(ids);
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
        purchaseApplicationService.approve(baseApproveParamDTO);
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
        Boolean flag = purchaseApplicationService.disApprove(ids);
        return flag == true ? success() : failure();
    }


    /**
     * 批量删除
     * @author Will
     * @date: 2023/3/15 17:47
     * @param ids
     * @return ApiResult
     */
    @PostMapping("/delete")
    public ApiResult delete(@RequestParam("id") List<String> ids) {
        Boolean flag = purchaseApplicationService.delete(ids);
        return flag == true ? success() : failure();
    }

    /**
     * 生成采购单
     * @author Will
     * @date: 2023/3/15 18:26
     * @param id
     * @return ApiResult
     */
    @PostMapping("/generatePurchaseOrder")
    public ApiResult generatePurchaseOrder(@RequestParam("id") String id) {
        Boolean flag = purchaseApplicationService.generatePurchaseOrder(id);
        return flag == true ? success() : failure();
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
        Boolean flag = purchaseApplicationService.importFile(excelFile,response);
        return flag == true ? success() : failure();
    }

    /**
     * 下载模板
     * @author Will
     * @date: 22023/3/15 18:22
     * @param request
     * @param response
     */
    @GetMapping("/exportTemplate")
    public ApiResult exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/purchaseApplication.xlsx";
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
     * @param purchaseApplicationPagingParamDTO
     * @param response
     * @return ApiResult
     */
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody PurchaseApplicationPagingParamDTO purchaseApplicationPagingParamDTO, HttpServletResponse response) {
        Boolean flag = purchaseApplicationService.exportExcel(purchaseApplicationPagingParamDTO, response);
        return flag == true ? success() : failure();
    }

}
