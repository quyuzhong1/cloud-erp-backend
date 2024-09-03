package com.erp.server.mrp.service.impl;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.dto.FileExcelDTO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.mrp.dto.CfgRuleStockUpDTO;
import com.erp.model.mrp.dto.HistoryImportRecordDTO;
import com.erp.model.mrp.dto.excel.*;
import com.erp.model.mrp.entity.ReplenishmentSuggestionEntity;
import com.erp.model.mrp.enums.HistoryImportRecordTypeEnum;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.excel.OtherOutStockImportExcelDTO;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.server.mrp.listener.*;
import com.erp.server.mrp.service.CfgRuleStockUpService;
import com.erp.server.mrp.service.HistoryImportRecordService;
import com.erp.server.mrp.service.ReplenishmentSuggestionImportService;
import com.erp.server.mrp.service.ReplenishmentSuggestionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReplenishmentSuggestionImportServiceImpl implements ReplenishmentSuggestionImportService {

    @Resource
    private ReplenishmentSuggestionService replenishmentSuggestionService;

    @Resource
    private HistoryImportRecordService historyImportRecordService;

    @Resource
    private CfgRuleStockUpService cgRuleStockUpService;

    @Resource
    private CustomerFeign customerFeign;
    @Override
    public void downloadRuleTemplate(HttpServletResponse response) {
        String path = "classpath:excel/replenishmentRuleTemplate.xlsx";
        String excelName = "template.xlsx";
        ExcelUtil.downloadTemplate(path,excelName,response);
    }

    @Override
    public void importRule(String id,MultipartFile excelFile, HttpServletResponse response) {
        ReplenishmentSuggestionEntity suggestionEntity = replenishmentSuggestionService.getById(id);
        if (ObjectUtil.isEmpty(suggestionEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"补货建议");
        }
        //平台信息
        List<DictBasicDTO.ViewDTO> platformViewList = customerFeign.getDictBasicByKey(DictBasicTypeEnum.SALES_PLATFORM.getType());

        //备货
        Pair<List<StockUpImportExcelDTO>, List<StockUpImportExcelDTO>> stockUpPair = importStockUp(excelFile,platformViewList);
        //动态备货系数
        Pair<List<StockingRatioImportExcelDTO>, List<StockingRatioImportExcelDTO>> stockingRatioPair = importStockingRatio(excelFile,platformViewList);
        //默认日销量
        Pair<List<DefaultSalesQtyImportExcelDTO>, List<DefaultSalesQtyImportExcelDTO>> defaultSalesQtyPair = importDefaultSalesQty(excelFile,platformViewList);
        //动态日销量
        Pair<List<DynamicSalesQtyImportExcelDTO>, List<DynamicSalesQtyImportExcelDTO>> dynamicSalesQtyPair = importDynamicSalesQty(excelFile,platformViewList);
        //固定日销量
        Pair<List<FixedSalesQtyImportExcelDTO>, List<FixedSalesQtyImportExcelDTO>> fixedSalesQtyPair = importFixedSalesQty(excelFile,platformViewList);
        //固定日销量
        Pair<List<SalesDenoisingImportExcelDTO>, List<SalesDenoisingImportExcelDTO>> salesDenoisingPair = importSalesDenoising(excelFile,platformViewList);

        //上传正确数据
        upLoadSuccessExcel (suggestionEntity,stockUpPair.getKey(),stockingRatioPair.getKey(),defaultSalesQtyPair.getKey(),
                dynamicSalesQtyPair.getKey(),fixedSalesQtyPair.getKey(),salesDenoisingPair.getKey());
        //导出错误数据
        exportErrorExcel (response,stockUpPair.getValue(),stockingRatioPair.getValue(),defaultSalesQtyPair.getValue(),
                dynamicSalesQtyPair.getValue(),fixedSalesQtyPair.getValue(),salesDenoisingPair.getValue());
    }
    /**
     * 上传正确数据
     * @author will
     * @date 2024/9/3 14:39
     * @param stockUpList
     * @param stockingRatioList
     * @param defaultSalesQtyList
     * @param dynamicSalesQtyList
     * @param fixedSalesQtyList
     * @param salesDenoisingList
     */
    private void upLoadSuccessExcel (ReplenishmentSuggestionEntity suggestionEntity,List<StockUpImportExcelDTO> stockUpList,List<StockingRatioImportExcelDTO> stockingRatioList,List<DefaultSalesQtyImportExcelDTO> defaultSalesQtyList,
                                   List<DynamicSalesQtyImportExcelDTO> dynamicSalesQtyList,List<FixedSalesQtyImportExcelDTO> fixedSalesQtyList,List<SalesDenoisingImportExcelDTO> salesDenoisingList) {
        //全部为空则无需处理
        if (CollectionUtils.isEmpty(stockUpList) && CollectionUtils.isEmpty(stockingRatioList)  && CollectionUtils.isEmpty(defaultSalesQtyList)
                && CollectionUtils.isEmpty(dynamicSalesQtyList) && CollectionUtils.isEmpty(fixedSalesQtyList) && CollectionUtils.isEmpty(salesDenoisingList)) {
            return;
        }
        String fileName = "补货规则.xlsx";
        FileExcelDTO.ExportFileDTO exportFileDTO = new FileExcelDTO.ExportFileDTO();
        exportFileDTO.setFileName(fileName);
        List<FileExcelDTO.ExportFileSheetDTO> sheetList = new ArrayList<>();
        sheetList.add(new FileExcelDTO.ExportFileSheetDTO("销量",stockUpList,StockUpImportExcelDTO.class));
        sheetList.add(new FileExcelDTO.ExportFileSheetDTO("动态备货系数",stockingRatioList,StockingRatioImportExcelDTO.class));
        sheetList.add(new FileExcelDTO.ExportFileSheetDTO("默认日销量",defaultSalesQtyList,DefaultSalesQtyImportExcelDTO.class));
        sheetList.add(new FileExcelDTO.ExportFileSheetDTO("动态日销量",dynamicSalesQtyList,DynamicSalesQtyImportExcelDTO.class));
        sheetList.add(new FileExcelDTO.ExportFileSheetDTO("固定日销量",fixedSalesQtyList,FixedSalesQtyImportExcelDTO.class));
        sheetList.add(new FileExcelDTO.ExportFileSheetDTO("销量去噪",salesDenoisingList,SalesDenoisingImportExcelDTO.class));
        exportFileDTO.setSheetList(sheetList);

        //添加导入记录
        HistoryImportRecordDTO.AddDTO dto = new HistoryImportRecordDTO.AddDTO();
        dto.setBusinessId(suggestionEntity.getId());
        dto.setName(fileName);
        dto.setModule(SourceTypeEnum.REPLENISHMENT_SUGGESTION.getCode());
        dto.setType(HistoryImportRecordTypeEnum.CFG_RULE_REPLENISHMENT.getCode());
        dto.setExportFileDTO(exportFileDTO);
        historyImportRecordService.add(dto);
    }

    /**
     * 导出错误数据
     * @author will
     * @date 2024/9/3 14:38
     * @param stockUpList
     * @param stockingRatioList
     * @param defaultSalesQtyList
     * @param dynamicSalesQtyList
     * @param fixedSalesQtyList
     * @param salesDenoisingList
     */
    private void exportErrorExcel ( HttpServletResponse response,List<StockUpImportExcelDTO> stockUpList,List<StockingRatioImportExcelDTO> stockingRatioList,List<DefaultSalesQtyImportExcelDTO> defaultSalesQtyList,
                                   List<DynamicSalesQtyImportExcelDTO> dynamicSalesQtyList,List<FixedSalesQtyImportExcelDTO> fixedSalesQtyList,List<SalesDenoisingImportExcelDTO> salesDenoisingList) {
        List<Pair<Integer, List<?>>> pairList = new ArrayList<>();
        pairList.add(new Pair<>(MathUtil.ZERO,stockUpList));
        pairList.add(new Pair<>(MathUtil.ONE,stockingRatioList));
        pairList.add(new Pair<>(MathUtil.TWO,defaultSalesQtyList));
        pairList.add(new Pair<>(MathUtil.THREE,dynamicSalesQtyList));
        pairList.add(new Pair<>(MathUtil.FOUR,fixedSalesQtyList));
        pairList.add(new Pair<>(MathUtil.FIVE,salesDenoisingList));

        String name = "补货规则错误数据";
        StringBuffer sb = new StringBuffer();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        String excelPath = "excel/replenishmentRuleError.xlsx";
        try {
            new ExcelPrintUtils().sheetPatchExport(pairList, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("信息导出出错 >>>>>{}", e);
            throw new ServiceException("补货规则错误数据导出失败");
        }
    }

    /**
     * 备货
     */
    private Pair<List<StockUpImportExcelDTO>,List<StockUpImportExcelDTO>> importStockUp (MultipartFile excelFile, List<DictBasicDTO.ViewDTO> platformViewList) {
        StockUpImportExcelListener excelListenerUtil = new StockUpImportExcelListener();

        try {
            EasyExcel.read(excelFile.getInputStream(), StockUpImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
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
        handleImportStockUp(successList, errorList,platformViewList);
        return new Pair<>(successList,errorList);
    }
    /**
     * 备货数据处理
     * @author will
     * @date 2024/9/3 15:48
     * @param successList
     * @param errorList
     */
    private void handleImportStockUp (List<StockUpImportExcelDTO> successList, List<StockUpImportExcelDTO> errorList, List<DictBasicDTO.ViewDTO> platformViewList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        //平台
        List<String> platformList = successList.stream().map(StockUpImportExcelDTO::getPlatform).distinct().collect(Collectors.toList());
        List<String> platformCodeList = platformViewList.stream().filter(obj -> platformList.contains(obj.getName())).map(DictBasicDTO.ViewDTO::getValue).collect(Collectors.toList());


        //店铺
        List<String> shopNameList = successList.stream().map(StockUpImportExcelDTO::getShopName).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = FeignQuery.create(ShopInfoEntity.class).in(ShopInfoEntity::getName, shopNameList).list();
        List<String> shopIdList = shopInfoList.stream().map(ShopInfoEntity::getId).collect(Collectors.toList());


        //SKU
        List<String> skuNoList = successList.stream().map(StockUpImportExcelDTO::getSkuNo).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = FeignQuery.create(ProductDetailEntity.class).in(ProductDetailEntity::getSkuNo, skuNoList).list();
        List<String> skuIdList = productDetailList.stream().map(ProductDetailEntity::getId).collect(Collectors.toList());

        //根据平台、店铺、skuId查询补货建议数据
        replenishmentSuggestionService.listByUnique(platformCodeList,shopIdList,skuIdList);

        for (StockUpImportExcelDTO excelDTO : successList) {
            //平台信息
            String platformCode = platformViewList.stream().filter(obj -> StrUtil.equals(obj.getName(), excelDTO.getPlatform())).map(DictBasicDTO.ViewDTO::getValue).findFirst().orElse("");
            //店铺信息
            String shopId = shopInfoList.stream().filter(obj -> StrUtil.equals(obj.getName(), excelDTO.getShopName()) && StrUtil.equals(obj.getDictPlatform(), platformCode)).map(ShopInfoEntity::getId).findFirst().orElse("");
            //SKU
            String skuId = productDetailList.stream().filter(obj -> StrUtil.equals(obj.getSkuNo(), excelDTO.getSkuNo())).map(ProductDetailEntity::getId).findFirst().orElse("");

            CfgRuleStockUpDTO.UpdateDTO updateDTO = new CfgRuleStockUpDTO.UpdateDTO();
            cgRuleStockUpService.update(updateDTO);
        }

    }

    /**
     * 动态备货系数
     */
    private  Pair<List<StockingRatioImportExcelDTO>,List<StockingRatioImportExcelDTO>> importStockingRatio (MultipartFile excelFile, List<DictBasicDTO.ViewDTO> platformViewList){
        StockingRatioImportExcelListener excelListenerUtil = new StockingRatioImportExcelListener();

        try {
            EasyExcel.read(excelFile.getInputStream(), StockingRatioImportExcelDTO.class, excelListenerUtil).sheet(1).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<StockingRatioImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        //导入数据处理
        List<StockingRatioImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<StockingRatioImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理校验导入成功数据
        handleImportStockingRatio(successList, errorList,platformViewList);
        return new Pair<>(successList,errorList);
    }
    /**
     * 动态备货系数数据
     * @author will
     * @date 2024/9/3 15:48
     * @param successList
     * @param errorList
     */
    private void handleImportStockingRatio (List<StockingRatioImportExcelDTO> successList, List<StockingRatioImportExcelDTO> errorList, List<DictBasicDTO.ViewDTO> platformViewList) {

    }

    /**
     * 导入默认日销量
     */
    private Pair<List<DefaultSalesQtyImportExcelDTO>,List<DefaultSalesQtyImportExcelDTO>> importDefaultSalesQty (MultipartFile excelFile, List<DictBasicDTO.ViewDTO> platformViewList) {
        DefaultSalesQtyImportExcelListener excelListenerUtil = new DefaultSalesQtyImportExcelListener();

        try {
            EasyExcel.read(excelFile.getInputStream(), DefaultSalesQtyImportExcelDTO.class, excelListenerUtil).sheet(2).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<DefaultSalesQtyImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        //导入数据处理
        List<DefaultSalesQtyImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<DefaultSalesQtyImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理校验导入成功数据
        handleImportDefaultSalesQty(successList,errorList,platformViewList);
        return new Pair<>(successList,errorList);
    }
    /**
     * 导入默认日销量处理
     * @author will
     * @date 2024/9/3 15:49
     * @param successList
     * @param errorList
     */
    private void handleImportDefaultSalesQty (List<DefaultSalesQtyImportExcelDTO> successList, List<DefaultSalesQtyImportExcelDTO> errorList, List<DictBasicDTO.ViewDTO> platformViewList) {

    }

    /**
     * 导入动态日销量
     */
    private Pair<List<DynamicSalesQtyImportExcelDTO>,List<DynamicSalesQtyImportExcelDTO>>  importDynamicSalesQty (MultipartFile excelFile, List<DictBasicDTO.ViewDTO> platformViewList) {
        DynamicSalesQtyImportExcelListener excelListenerUtil = new DynamicSalesQtyImportExcelListener();

        try {
            EasyExcel.read(excelFile.getInputStream(), DynamicSalesQtyImportExcelDTO.class, excelListenerUtil).sheet(3).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<DynamicSalesQtyImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        //导入数据处理
        List<DynamicSalesQtyImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<DynamicSalesQtyImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理校验导入成功数据
        handleImportDynamicSalesQty(successList,errorList,platformViewList);
        return new Pair<>(successList,errorList);
    }

    /**
     * 导入动态日销量
     * @author will
     * @date 2024/9/3 15:49
     * @param successList
     * @param errorList
     */
    private void handleImportDynamicSalesQty (List<DynamicSalesQtyImportExcelDTO> successList, List<DynamicSalesQtyImportExcelDTO> errorList, List<DictBasicDTO.ViewDTO> platformViewList) {

    }

    /**
     * 导入固定日销量
     */
    private Pair<List< FixedSalesQtyImportExcelDTO>,List<FixedSalesQtyImportExcelDTO>> importFixedSalesQty (MultipartFile excelFile, List<DictBasicDTO.ViewDTO> platformViewList) {
        FixedSalesQtyImportExcelListener excelListenerUtil = new FixedSalesQtyImportExcelListener();

        try {
            EasyExcel.read(excelFile.getInputStream(), FixedSalesQtyImportExcelDTO.class, excelListenerUtil).sheet(4).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<FixedSalesQtyImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        //导入数据处理
        List<FixedSalesQtyImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<FixedSalesQtyImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理校验导入成功数据
        handleImportFixedSalesQty(successList, errorList,platformViewList);
        return new Pair<>(successList,errorList);
    }

    /**
     * 导入固定日销量
     * @author will
     * @date 2024/9/3 15:49
     * @param successList
     * @param errorList
     */
    private void handleImportFixedSalesQty (List<FixedSalesQtyImportExcelDTO> successList, List<FixedSalesQtyImportExcelDTO> errorList, List<DictBasicDTO.ViewDTO> platformViewList) {

    }

    /**
     * 销量去噪
     */
    private Pair<List<SalesDenoisingImportExcelDTO>, List<SalesDenoisingImportExcelDTO>> importSalesDenoising (MultipartFile excelFile, List<DictBasicDTO.ViewDTO> platformViewList) {
        SalesDenoisingImportExcelListener excelListenerUtil = new SalesDenoisingImportExcelListener();

        try {
            EasyExcel.read(excelFile.getInputStream(), SalesDenoisingImportExcelDTO.class, excelListenerUtil).sheet(5).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<SalesDenoisingImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        //导入数据处理
        List<SalesDenoisingImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<SalesDenoisingImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理校验导入成功数据
        handleImportSalesDenoising(successList, errorList,platformViewList);
        return new Pair<>(successList,errorList);
    }

    /**
     * 导入销量去噪
     * @author will
     * @date 2024/9/3 15:49
     * @param successList
     * @param errorList
     */
    private void handleImportSalesDenoising (List<SalesDenoisingImportExcelDTO> successList, List<SalesDenoisingImportExcelDTO> errorList, List<DictBasicDTO.ViewDTO> platformViewList) {

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
            EasyExcel.read(excelFile.getInputStream(), OtherOutStockImportExcelDTO.class, excelListenerUtil).sheet(6).doRead();
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
        handleImportSalesEstimate(successList, errorList);

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
     * 处理校销量预估导入成功数据
     * @author will
     * @date 2024/8/30 17:01
     * @param successList
     * @param errorList
     */
    private void handleImportSalesEstimate (List<StockUpImportExcelDTO> successList, List<StockUpImportExcelDTO> errorList) {

    }
}
