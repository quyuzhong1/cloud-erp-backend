package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.BiDataSourceCostSearchDTO;
import com.erp.server.bi.service.BiDataSourceCostService;
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
import java.util.LinkedHashMap;

/**
 * 数据源管理
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 16:33
 */
@RestController
@RequestMapping("bi/dataSourceCost")
public class BiDataSourceCostController extends BaseController {

    @Resource
    private BiDataSourceCostService biDataSourceCostService;

    /**
     * 成本数据-分页查询
     * @author Will
     * @date: 2022/12/16 13:17
     * @param dto
     * @return ApiResult<PagingVO<LinkedHashMap<String,Object>>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<LinkedHashMap<String,Object>>> queryByPage(@RequestBody @Validated PagingDTO<BiDataSourceCostSearchDTO> dto) {
        PagingVO<LinkedHashMap<String,Object>> pagingVO = biDataSourceCostService.paging(dto);
        return success(pagingVO);
    }

    /**
     *  成本数据-导出
     * @author Will
     * @date: 2022/12/15 10 10:45
     * @param dto
     * @param response
     */
    @PostMapping(value = "/exportExcel")
    public void exportExcel(@RequestBody BiDataSourceCostSearchDTO dto, HttpServletResponse response) {
        biDataSourceCostService.exportExcel(dto, response);
    }


    /**
     * 成本数据-导入
     * @author Will
     * @date: 2022/12/16 11:10
     * @param excelFile
     * @param response
     */
    @PostMapping("/importBiDataSourceCostFile")
    public void importBiDataSourceCostFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        biDataSourceCostService.importExcel(excelFile, response);
    }


    /**
     * 成本数据-下载模板
     * @author Will
     * @date: 2022/12/15 18:42
     * @param request
     * @param response
     */
    @GetMapping("/exportTemplate")
    public void exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/biDataSourceCost.xlsx";
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
        }

    }

}
