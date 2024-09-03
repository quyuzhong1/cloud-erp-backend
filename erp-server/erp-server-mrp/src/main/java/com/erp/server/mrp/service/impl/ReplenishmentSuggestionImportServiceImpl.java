package com.erp.server.mrp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.mrp.dto.excel.StockUpImportExcelDTO;
import com.erp.model.wms.dto.excel.OtherOutStockImportExcelDTO;
import com.erp.server.mrp.listener.StockUpImportExcelListener;
import com.erp.server.mrp.service.ReplenishmentSuggestionImportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReplenishmentSuggestionImportServiceImpl implements ReplenishmentSuggestionImportService {


    @Override
    public void downloadRuleTemplate(HttpServletResponse response) {
        String path = "classpath:excel/replenishmentRuleTemplate.xlsx";
        String excelName = "template.xlsx";
        ExcelUtil.downloadTemplate(path,excelName,response);
    }

    @Override
    public void importRule(MultipartFile excelFile, HttpServletResponse response) {
        StockUpImportExcelListener excelListenerUtil = new StockUpImportExcelListener();

        try {
            EasyExcel.read(excelFile.getInputStream(), OtherOutStockImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<StockUpImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        //导入数据处理
        List<StockUpImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<StockUpImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理校验导入成功数据
        handleImportReplenishmentRule(successList, errorList);

        if (errorList.isEmpty()) {
            return ;
        }
        String excelPath = "excel/replenishmentRuleError.xlsx";
        String name = "replenishmentRuleError";
        try {
            new ExcelPrintUtils().patchExport(errorList,
                    response,
                    StrUtil.builder().append(DateUtil.nowExcelFileFormat()).append(name).toString(),
                    excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_95125);
        }
    }
    /**
     * 导入默认日销量
     */
    private void importDefaultSalesQty () {

    }
    /**
     * 导入
     */
    private void importDynamicSalesQty () {

    }
    private void importFixedSalesQty () {

    }

    private void importSalesDenoising () {

    }

    private void importStockingRatio () {

    }

    private void importStockUp () {

    }

    @Override
    public void downloadSalesEstimateTemplate(HttpServletResponse response) {
        String excelName = "salesEstimateTemplate.xlsx";
        LocalDate now = LocalDate.now();
        LinkedList<String> headerNameList =  Arrays.asList("*平台","*SKU","*店铺",now.format(DateTimeFormatter.ofPattern("yyyy年MM月dd")),now.plusMonths(1L).format(DateTimeFormatter.ofPattern("yyyy年MM月dd")),now.plusMonths(2L).format(DateTimeFormatter.ofPattern("yyyy年MM月dd"))).stream().collect(Collectors.toCollection(LinkedList::new));
        ExcelUtil.downloadDynamicTemplate(headerNameList, excelName, response);
    }

    @Override
    public void importSalesEstimate(MultipartFile excelFile, HttpServletResponse response) {
        StockUpImportExcelListener excelListenerUtil = new StockUpImportExcelListener();

        try {
            EasyExcel.read(excelFile.getInputStream(), OtherOutStockImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<StockUpImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        //导入数据处理
        List<StockUpImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<StockUpImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理校验导入成功数据
        handleImportReplenishmentRule(successList, errorList);

        if (errorList.isEmpty()) {
            return ;
        }
        String excelPath = "excel/salesEstimateError.xlsx";
        String name = "salesEstimateError";
        try {
            new ExcelPrintUtils().patchExport(errorList,
                    response,
                    StrUtil.builder().append(DateUtil.nowExcelFileFormat()).append(name).toString(),
                    excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_95125);
        }
    }

    /**
     * 处理校验补货规则导入成功数据
     * @author will
     * @date 2024/8/30 17:01
     * @param successList
     * @param errorList
     */
    private void handleImportReplenishmentRule (List<StockUpImportExcelDTO> successList, List<StockUpImportExcelDTO> errorList) {

    }
}
