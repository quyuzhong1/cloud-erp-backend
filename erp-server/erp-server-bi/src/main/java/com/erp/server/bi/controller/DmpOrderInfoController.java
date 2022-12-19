package com.erp.server.bi.controller;

import com.alibaba.excel.EasyExcel;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.*;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.enums.OrderStateEnum;
import com.erp.server.bi.listener.DmpOrderInfoExcelListener;
import com.erp.server.bi.service.DmpOrderInfoService;
import com.erp.server.bi.service.DmpShopInfoService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 数据源管理
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/13 15:15
 */
@RestController
@RequestMapping("bi/dmpOrderInfo")
public class DmpOrderInfoController extends BaseController {

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    @Resource
    private DmpShopInfoService dmpShopInfoService;

    @Resource
    private SysUserFeign sysUserFeign;

    /**
     * 销售数据-分页查询
     * @author Will
     * @date: 2022/12/15 10:48
     * @param dto
     * @return ApiResult<PagingVO<DmpOrderInfoDTO>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<DmpOrderInfoDTO>> queryByPage(@RequestBody @Validated PagingDTO<DmpOrderInfoSearchDTO> dto) {
        PagingVO<DmpOrderInfoDTO> pagingVO = dmpOrderInfoService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 销售数据-修改状态
     * @author Will
     * @date: 2022/12/15 10:13
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/updateState")
    public ApiResult updateState(@RequestBody @Validated DmpOrderStateDTO dto) {
        Boolean flag = dmpOrderInfoService.updateState(dto);
        return flag == true ? this.success() : this.failure();
    }


    /**
     *  销售数据-导出
     * @author Will
     * @date: 2022/12/15 10 10:45
     * @param dto
     * @param response
     */
    @PostMapping(value = "/exportExcel")
    public void exportExcel(@RequestBody DmpOrderInfoSearchDTO dto, HttpServletResponse response) {
        dmpOrderInfoService.exportExcel(dto, response);
    }


    /**
     * 销售数据-导入
     * @author Will
     * @date: 2022/12/16 11:10
     * @param excelFile
     * @param importType
     * @param response
     */
    @PostMapping("/importOrderFile")
    public void importOrderFile(@RequestParam(value = "excelFile") MultipartFile excelFile, @RequestParam(value = "importType") Integer importType, HttpServletResponse response) {
        DmpOrderInfoExcelListener excelListenerUtil = new DmpOrderInfoExcelListener(importType, dmpOrderInfoService, dmpShopInfoService, sysUserFeign);
        try {
            EasyExcel.read(excelFile.getInputStream(), DmpOrderInfoImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
            List<DmpOrderInfoImportExcelDTO> list = excelListenerUtil.getDateList();
            if (list.size() > 0) {
                StringBuffer sb = new StringBuffer();
                String excelPath = "excel/dmpOrderInfo.xlsx";
                String name = "dmpOrderInfo";
                String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
                sb.append(date);
                sb.append(name);
                new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 销售数据-下载模板
     * @author Will
     * @date: 2022/12/15 18:42
     * @param request
     * @param response
     */
    @GetMapping("/exportTemplate")
    public void exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/dmpOrderInfoTemplate.xlsx";
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

}
