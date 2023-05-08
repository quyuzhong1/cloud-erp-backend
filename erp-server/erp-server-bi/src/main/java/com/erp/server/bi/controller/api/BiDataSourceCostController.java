package com.erp.server.bi.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.PagingDTO;
import com.common.core.enums.ApiError;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.exception.ServiceException;
import com.common.business.vo.PagingVO;
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
import java.util.List;

/**
 * 数据源管理
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 16:33
 */
@RestController
@RequestMapping("dataSourceCost")
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
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:dataSourceCost:paging", tableAlias = "bdsc")
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
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:dataSourceCost:exportExcel", tableAlias = "bdsc")
    public ApiResult exportExcel(@RequestBody BiDataSourceCostSearchDTO dto, HttpServletResponse response) {
        biDataSourceCostService.exportExcel(dto, response);
        return success();
    }


    /**
     * 成本数据-导入
     * @author Will
     * @date: 2022/12/16 11:10
     * @param excelFile
     * @param response
     */
    @PostMapping("/importBiDataSourceCostFile")
    public ApiResult importBiDataSourceCostFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean flag = biDataSourceCostService.importExcel(excelFile, response);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 成本数据-编辑
     * @author Will
     * @date: 2022/12/21 17:02
     * @param list
     */
    @PostMapping("/updateBiDataSourceCost")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "bi:dataSourceCost:updateBiDataSourceCost",
            serviceClass =  BiDataSourceCostService.class,
            keyIdName = "id")
    public ApiResult updateBiDataSourceCost(@RequestBody List<LinkedHashMap<String,Object>> list) {
        biDataSourceCostService.updateBiDataSourceCost(list);
        return success();
    }

    /**
     * 成本数据-下载模板
     * @author Will
     * @date: 2022/12/15 18:42
     * @param request
     * @param response
     */
    @GetMapping("/exportTemplate")
    public ApiResult exportTemplate(HttpServletRequest request, HttpServletResponse response) {
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
            throw new ServiceException(ApiError.Default);
        }
        return success();
    }

}
