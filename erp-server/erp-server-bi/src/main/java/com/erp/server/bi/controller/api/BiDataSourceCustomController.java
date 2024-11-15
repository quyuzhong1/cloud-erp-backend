package com.erp.server.bi.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.PagingDTO;
import com.common.core.enums.ApiError;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.BiDataSourceCustomGraphicalDTO;
import com.erp.model.bi.dto.BiDataSourceCustomSearchDTO;
import com.erp.model.bi.dto.BiDataSourceCustomTableDTO;
import com.erp.model.bi.dto.BiTargetTypeDTO;
import com.common.business.vo.ChartVO;
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

 * @date 2022/12/14 16:33
 */
@RestController
@LogSystemModule("数据源管理")
@RequestMapping("dataSourceCustom")
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
    @LogAction(value = LogActionEnum.EXPORT, desc = "自助数据-导出")
    @PostMapping(value = "/exportExcel")
    public ApiResult<Void> exportExcel(@RequestBody BiDataSourceCustomSearchDTO dto, HttpServletResponse response) {
        biDataSourceCustomService.exportExcel(dto, response);
        return  success();
    }

   /**
    * 自助数据-市场数据-分页查询
    * @author Will
    * @date: 2022/12/16 13:18
    * @param dto
    * @return ApiResult<PagingVO<LinkedHashMap<String,Object>>>
    */
    @PostMapping("/market/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "create_user_id", menuCode = "bi:dataSourceCustom:market:paging", tableAlias = "bdsc")
    public ApiResult<PagingVO<LinkedHashMap<String,Object>>> marketQueryByPage(@RequestBody @Validated PagingDTO<BiDataSourceCustomSearchDTO> dto) {
        PagingVO<LinkedHashMap<String,Object>> pagingVO = biDataSourceCustomService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 自助数据-供应链数据-分页查询
     * @author Will
     * @date: 2022/12/16 13:18
     * @param dto
     * @return ApiResult<PagingVO<LinkedHashMap<String,Object>>>
     */
    @PostMapping("/supplyChain/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "create_user_id", menuCode = "bi:dataSourceCustom:supplyChain:paging", tableAlias = "bdsc")
    public ApiResult<PagingVO<LinkedHashMap<String,Object>>> supplyChainQueryByPage(@RequestBody @Validated PagingDTO<BiDataSourceCustomSearchDTO> dto) {
        PagingVO<LinkedHashMap<String,Object>> pagingVO = biDataSourceCustomService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 自助数据-经营数据-分页查询
     * @author Will
     * @date: 2022/12/16 13:18
     * @param dto
     * @return ApiResult<PagingVO<LinkedHashMap<String,Object>>>
     */
    @PostMapping("/operate/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "create_user_id", menuCode = "bi:dataSourceCustom:operate:paging", tableAlias = "bdsc")
    public ApiResult<PagingVO<LinkedHashMap<String,Object>>> operateQueryByPage(@RequestBody @Validated PagingDTO<BiDataSourceCustomSearchDTO> dto) {
        PagingVO<LinkedHashMap<String,Object>> pagingVO = biDataSourceCustomService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 自助数据-财务数据-分页查询
     * @author Will
     * @date: 2022/12/16 13:18
     * @param dto
     * @return ApiResult<PagingVO<LinkedHashMap<String,Object>>>
     */
    @PostMapping("/finance/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "create_user_id", menuCode = "bi:dataSourceCustom:finance:paging", tableAlias = "bdsc")
    public ApiResult<PagingVO<LinkedHashMap<String,Object>>> financeQueryByPage(@RequestBody @Validated PagingDTO<BiDataSourceCustomSearchDTO> dto) {
        PagingVO<LinkedHashMap<String,Object>> pagingVO = biDataSourceCustomService.paging(dto);
        return success(pagingVO);
    }


    /**
     *  自助数据-市场数据-导出
     * @author Will
     * @date: 2022/12/15 10 10:45
     * @param dto
     * @param response
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "自助数据-市场数据-导出")
    @PostMapping(value = "/market/exportExcel")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "create_user_id", menuCode = "bi:dataSourceCustom:market:paging", tableAlias = "bdsc")
    public ApiResult<Void> marketExportExcel(@RequestBody BiDataSourceCustomSearchDTO dto, HttpServletResponse response) {
        biDataSourceCustomService.exportExcel(dto, response);
        return  success();
    }

    /**
     *  自助数据-供应链数据-导出
     * @author Will
     * @date: 2022/12/15 10 10:45
     * @param dto
     * @param response
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "自助数据-供应链数据-导出")
    @PostMapping(value = "/supplyChain/exportExcel")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "create_user_id", menuCode = "bi:dataSourceCustom:supplyChain:paging", tableAlias = "bdsc")
    public ApiResult<Void> supplyChainExportExcel(@RequestBody BiDataSourceCustomSearchDTO dto, HttpServletResponse response) {
        biDataSourceCustomService.exportExcel(dto, response);
        return  success();
    }

    /**
     *  自助数据-经营数据-导出
     * @author Will
     * @date: 2022/12/15 10 10:45
     * @param dto
     * @param response
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "自助数据-经营数据-导出")
    @PostMapping(value = "/operate/exportExcel")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "create_user_id", menuCode = "bi:dataSourceCustom:operate:paging", tableAlias = "bdsc")
    public ApiResult<Void> operateExportExcel(@RequestBody BiDataSourceCustomSearchDTO dto, HttpServletResponse response) {
        biDataSourceCustomService.exportExcel(dto, response);
        return  success();
    }

    /**
     *  自助数据-财务数据-导出
     * @author Will
     * @date: 2022/12/15 10 10:45
     * @param dto
     * @param response
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "自助数据-财务数据-导出")
    @PostMapping(value = "/finance/exportExcel")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "create_user_id", menuCode = "bi:dataSourceCustom:finance:paging", tableAlias = "bdsc")
    public ApiResult<Void> financeExportExcel(@RequestBody BiDataSourceCustomSearchDTO dto, HttpServletResponse response) {
        biDataSourceCustomService.exportExcel(dto, response);
        return  success();
    }

    /**
     * 自助数据-导入
     * @author Will
     * @date: 2022/12/16 11:10
     * @param excelFile
     * @param importType
     * @param response
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "自助数据-导入")
    @PostMapping("/importBiDataSourceCustomFile")
    public ApiResult<Void> importBiDataSourceCustomFile(@RequestParam(value = "excelFile") MultipartFile excelFile, @RequestParam(value = "importType") Integer importType, @RequestParam(value = "dataType") Integer dataType, HttpServletResponse response) {
        boolean flag = biDataSourceCustomService.importExcel(excelFile, response, importType,dataType);
        return flag ? this.success() : this.failure();
    }

    /**
     * 自助数据-下载模板
     * @author Will
     * @date: 2022/12/15 18:42
     * @param request
     * @param response
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "自助数据-下载模板")
    @GetMapping("/exportTemplate")
    public ApiResult<Void> exportTemplate(HttpServletRequest request, HttpServletResponse response , @RequestParam(value = "importType") Integer importType) {
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
        return success();
    }

    /**
     * 自助数据-柱状图数据查询
     * @author Will
     * @date: 2022/12/28 11:50
     * @param moduleId
     * @param year
     * @return ApiResult
     */
    @GetMapping("/listGraphicalData")
    public ApiResult<ChartVO<BiDataSourceCustomGraphicalDTO>> listGraphicalData(@RequestParam("moduleId") String moduleId, @RequestParam("year") Integer year) {
        ChartVO<BiDataSourceCustomGraphicalDTO> vo = biDataSourceCustomService.listGraphicalData(moduleId,year);
        return success(vo);
    }

    /**
     * 自助数据-表格数据查询
     * @author Will
     * @date: 2022/12/28 14:28
     * @param dto
     * @return ApiResult<LinkedHashMap<String,Object>>
     */
    @PostMapping("/listTableData")
    public ApiResult<LinkedHashMap<String,Object>> listTableData(@RequestBody @Validated BiDataSourceCustomTableDTO dto) {
        LinkedHashMap<String,Object> vo = biDataSourceCustomService.listTableData(dto);
        return success(vo);
    }

    /**
     * 自助数据-查询指标分类
     * @author Will
     * @date: 2022/12/28 9:07
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/listTargetType")
    public ApiResult<List<String>> listTargetType(@RequestBody @Validated BiDataSourceCustomTableDTO dto) {
        List<String> targetTypeList = biDataSourceCustomService.listTargetType(dto);
        return success(targetTypeList);
    }

    /**
     * 自助数据-指标分类
     * @author Will
     * @date: 2022/12/28 17:23
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/updateTargetType")
    public ApiResult<Void> updateTargetType(@RequestBody @Validated BiTargetTypeDTO dto) {
        biDataSourceCustomService.updateTargetType(dto);
        return success();
    }

}
