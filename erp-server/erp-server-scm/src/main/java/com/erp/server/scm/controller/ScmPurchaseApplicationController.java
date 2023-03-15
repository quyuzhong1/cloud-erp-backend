package com.erp.server.scm.controller;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseAuditParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.*;
import com.erp.server.scm.service.ScmPurchaseApplicationService;
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
 * 采购申请表 前端控制器
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
@RestController
@RequestMapping("/scmPurchaseApplication")
public class ScmPurchaseApplicationController extends BaseController {

    @Resource
    private ScmPurchaseApplicationService scmPurchaseApplicationService;

    /**
     * 分页查询
     * @author Will
     * @date: 2023/3/15 16:47
     * @param dto
     * @return ApiResult<PagingVO<List<ScmPurchaseApplicationViewDTO>>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<List<ScmPurchaseApplicationViewDTO>>> queryByPage(@RequestBody @Validated PagingDTO<ScmPurchaseApplicationSearchDTO> dto) {
        PagingVO<List<ScmPurchaseApplicationViewDTO>> pagingVO = scmPurchaseApplicationService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 新增或修改
     * @author Will
     * @date: 2023/3/15 17:34
     * @param scmPurchaseApplicationDTO
     * @return ApiResult
     */
    @PostMapping("/addOrUpdate")
    public ApiResult addOrUpdate(@RequestBody @Validated ScmPurchaseApplicationDTO scmPurchaseApplicationDTO) {
        Boolean flag = scmPurchaseApplicationService.addOrUpdateScmPurchaseApplication(scmPurchaseApplicationDTO);
        return flag == true ? success() : failure();
    }


    /**
     * 审核
     * @author Will
     * @date: 2023/3/15 17:54
     * @param baseAuditParamDTO
     * @return ApiResult
     */
    @PostMapping("/audit")
    public ApiResult audit(@RequestBody @Validated BaseAuditParamDTO baseAuditParamDTO) {
        scmPurchaseApplicationService.audit(baseAuditParamDTO);
        return success();
    }

    /**
     * 批量反审核
     * @author Will
     * @date: 2023/3/15 17:50
     * @param ids
     * @return ApiResult
     */
    @PostMapping("/unAudit")
    public ApiResult unAudit(@RequestParam("ids") List<String> ids) {
        Boolean flag = scmPurchaseApplicationService.unAudit(ids);
        return flag == true ? success() : failure();
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
        Boolean flag = scmPurchaseApplicationService.removeById(id);
        return flag == true ? success() : failure();
    }

    /**
     * @description: 生成采购单
     * @author Will
     * @date: 2023/3/15 18:26
     * @param id
     * @return ApiResult
     */
    @PostMapping("/generatePurchaseOrder")
    public ApiResult generatePurchaseOrder(@RequestParam("id") String id) {
        Boolean flag = scmPurchaseApplicationService.generatePurchaseOrder(id);
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
        Boolean flag = scmPurchaseApplicationService.importFile(excelFile,response);
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
        String path = "classpath:excel/scmPurchaseApplication.xlsx";
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
     * @param scmPurchaseApplicationSearchDTO
     * @param response
     * @return ApiResult
     */
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody ScmPurchaseApplicationSearchDTO scmPurchaseApplicationSearchDTO, HttpServletResponse response) {
        Boolean flag = scmPurchaseApplicationService.exportExcel(scmPurchaseApplicationSearchDTO, response);
        return flag == true ? success() : failure();
    }

}
