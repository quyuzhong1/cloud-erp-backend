package com.erp.server.scm.controller;


import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.ExcelImportDTO;
import com.erp.model.scm.dto.ListStatusCountDTO;
import com.erp.model.scm.dto.PurchaseApplicationDTO;
import com.erp.model.scm.dto.PurchaseApplicationDetailDTO;
import com.erp.server.scm.service.PurchaseApplicationService;
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
 * 采购申请管理
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
    public ApiResult<PagingVO<PurchaseApplicationDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<PurchaseApplicationDTO.SearchParamDTO> dto) {
        PagingVO<PurchaseApplicationDTO.ListDTO> pagingVO = purchaseApplicationService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 查询数量
     * @author Will
     * @date: 2023/3/15 17:34
     * @return ApiResult
     */
    @GetMapping("/listCount")
    public ApiResult<List<ListStatusCountDTO.PurchaseApplicationCountDTO>> listCount() {
        List<ListStatusCountDTO.PurchaseApplicationCountDTO> list = purchaseApplicationService.listCount();
        return success(list);
    }

    /**
     * 新增
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated PurchaseApplicationDTO.AddDTO dto) {
         purchaseApplicationService.add(dto);
        return success();
    }

    /**
     * 修改
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated PurchaseApplicationDTO.UpdateDTO dto) {
        Boolean flag = purchaseApplicationService.update(dto);
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
    public ApiResult addAndSubmit(@RequestBody @Validated PurchaseApplicationDTO.AddDTO dto) {
        Boolean flag = purchaseApplicationService.addAndSubmit(dto);
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
    public ApiResult updateAndSubmit(@RequestBody @Validated PurchaseApplicationDTO.UpdateDTO dto) {
        Boolean flag = purchaseApplicationService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查询详情
     * @author Will
     * @date: 2023/3/15 17:44
     * @param id
     * @return ApiResult<PurchaseApplicationDTO.ViewDTO>
     */
    @GetMapping("/view")
    public ApiResult<PurchaseApplicationDTO.ViewDTO> view(@RequestParam("id") String id) {
        PurchaseApplicationDTO.ViewDTO dto = purchaseApplicationService.view(id);
        return success(dto);
    }

    /**
     * 批量提交
     * @author Will
     * @date: 2023/3/15 17:47
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/submit")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = purchaseApplicationService.submit(dto.getIds());
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
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/disApprove")
    public ApiResult disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = purchaseApplicationService.disApprove(dto.getIds());
        return flag == true ? success() : failure();
    }


    /**
     * 批量删除
     * @author Will
     * @date: 2023/3/15 17:47
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = purchaseApplicationService.delete(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 生成采购单弹窗显示
     * @author Will
     * @date: 2023/3/15 18:26
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/viewGeneratePurchaseOrder")
    public ApiResult<List<PurchaseApplicationDTO.ViewGeneratePurchaseOrderDTO>> viewGeneratePurchaseOrder(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<PurchaseApplicationDTO.ViewGeneratePurchaseOrderDTO> list = purchaseApplicationService.viewGeneratePurchaseOrder(dto.getIds());
        return success(list);
    }

    /**
     * 生成采购单
     * @author Will
     * @date: 2023/3/15 18:26
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/generatePurchaseOrder")
    public ApiResult generatePurchaseOrder(@RequestBody @Validated PurchaseApplicationDTO.ListGeneratePurchaseOrderDTO dto) {
        Boolean flag = purchaseApplicationService.generatePurchaseOrder(dto);
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
        Boolean result = purchaseApplicationService.cancelProcess(dto.getIds());
        return result == true ? success() : failure();
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
    public ApiResult<PurchaseApplicationDetailDTO.ImportDTO> importFile(@ModelAttribute @Validated ExcelImportDTO.CommonDTO excelImportDTO, HttpServletResponse response) {
        PurchaseApplicationDetailDTO.ImportDTO dto = purchaseApplicationService.importFile(excelImportDTO.getExcelFile(), excelImportDTO.getSkuIds(), response);
        return success(dto);
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
        String path = "classpath:excel/purchaseApplicationTemplate.xlsx";
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
     * 导出
     * @author Will
     * @date: 2023/3/15 18:23
     * @param dto
     * @param response
     * @return ApiResult
     */
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody PurchaseApplicationDTO.SearchParamDTO dto, HttpServletResponse response) {
        Boolean flag = purchaseApplicationService.exportExcel(dto, response);
        return flag == true ? success() : failure();
    }

}
