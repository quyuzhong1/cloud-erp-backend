package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.BiDataSourceCustomSearchDTO;
import com.erp.model.bi.vo.ChartVO;
import com.erp.server.bi.service.BiDataSourceCustomService;
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
import java.util.List;

/**
 * 数据源管理
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 16:33
 */
@RestController
@RequestMapping("bi/dataSourceCustom")
public class BiDataSourceCustomController extends BaseController {

    @Resource
    private BiDataSourceCustomService biDataSourceCustomService;

   /**
    * 自助数据-分页查询
    * @author Will
    * @date: 2022/12/16 13:18
    * @param dto
    * @return ApiResult<PagingVO<LinkedHashMap<String,Object>>>
    */
    @PostMapping("/paging")
    public ApiResult<PagingVO<LinkedHashMap<String,Object>>> queryByPage(@RequestBody @Validated PagingDTO<BiDataSourceCustomSearchDTO> dto) {
        PagingVO<LinkedHashMap<String,Object>> pagingVO = biDataSourceCustomService.paging(dto);
        return success(pagingVO);
    }

    /**
     *  自助数据-导出
     * @author Will
     * @date: 2022/12/15 10 10:45
     * @param dto
     * @param response
     */
    @PostMapping(value = "/exportExcel")
    public void exportExcel(@RequestBody BiDataSourceCustomSearchDTO dto, HttpServletResponse response) {
        biDataSourceCustomService.exportExcel(dto, response);
    }


    /**
     * 自助数据-导入
     * @author Will
     * @date: 2022/12/16 11:10
     * @param excelFile
     * @param importType
     * @param response
     */
    @PostMapping("/importBiDataSourceCustomFile")
    public void importBiDataSourceCustomFile(@RequestParam(value = "excelFile") MultipartFile excelFile, @RequestParam(value = "importType") Integer importType, HttpServletResponse response) {
        biDataSourceCustomService.importExcel(excelFile, response,importType);
    }


    /**
     * 自助数据-下载模板
     * @author Will
     * @date: 2022/12/15 18:42
     * @param request
     * @param response
     */
    @GetMapping("/exportTemplate")
    public void exportTemplate(HttpServletRequest request, HttpServletResponse response , @RequestParam(value = "importType") Integer importType) {
        String path = "";
        switch (importType) {
            case 1:
                path = "classpath:excel/biDataSourceCustomYear.xlsx";
                break;
            case 2:
                path = "classpath:excel/biDataSourceCustomQuarter.xlsx";
                break;
            case 3:
                path = "classpath:excel/biDataSourceCustomMonth.xlsx";
                break;
            case 4:
                path = "classpath:excel/biDataSourceCustomWeek.xlsx";
                break;
            case 5:
                path = "classpath:excel/biDataSourceCustomDay.xlsx";
                break;
            default:
                break;
        }
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
            throw new ServiceException(ApiError.Default);
        }

    }
    /**
     * 自助数据-查询指标分类
     * @author Will
     * @date: 2022/12/28 9:07
     * @param moduleName
     * @param year
     * @param type
     * @return ApiResult
     */
    @GetMapping("/listTargetType")
    public ApiResult<List<String>> listTargetType(@RequestParam("moduleName") String moduleName,@RequestParam("year") Integer year,@RequestParam("type") Integer type) {
       List<String> targetTypeList= biDataSourceCustomService.listTargetType(moduleName,type,year);
       return success(targetTypeList);
    }

    /**
     * 自助数据-柱状图数据查询
     * @author Will
     * @date: 2022/12/28 11:50
     * @param moduleName
     * @param year
     * @return ApiResult
     */
    @GetMapping("/listGraphicalData")
    public ApiResult<ChartVO> listGraphicalData(@RequestParam("moduleName") String moduleName,@RequestParam("year") Integer year) {
        ChartVO vo =biDataSourceCustomService.listGraphicalData(moduleName,year);
        return success(vo);
    }



}
