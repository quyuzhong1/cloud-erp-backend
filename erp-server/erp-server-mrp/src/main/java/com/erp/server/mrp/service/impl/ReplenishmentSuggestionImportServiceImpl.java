package com.erp.server.mrp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.CharSequenceUtil;
import cn.hutool.json.JSONNull;
import cn.hutool.json.JSONObject;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.exception.ExcelCommonException;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.dto.FileExcelDTO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.dto.excel.*;
import com.erp.model.mrp.entity.CfgRuleSalesQtyEntity;
import com.erp.model.mrp.entity.CfgRuleStockUpEntity;
import com.erp.model.mrp.entity.ReplenishmentSuggestionEntity;
import com.erp.model.mrp.enums.*;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.server.mrp.listener.*;
import com.erp.server.mrp.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
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
    private CfgRuleStockingRatioService cfgRuleStockingRatioService;

    @Resource
    private HistoryImportRecordService historyImportRecordService;

    @Resource
    private CfgRuleStockUpService cgRuleStockUpService;

    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private CfgRuleStockUpService cfgRuleStockUpService;

    @Resource
    private CfgRuleSalesFormulaService cfgRuleSalesFormulaService;

    @Resource
    private CfgRuleSalesQtyService cfgRuleSalesQtyService;

    @Resource
    private CfgRuleSalesDenoisingService cfgRuleSalesDenoisingService;

    @Resource
    private SalesEstimateManualService salesEstimateManualService;

    private static  final String SALE_ERROR_MSG = "平台【{}】、店铺【{}】、SKU【{}】未找到对应的销量设置数据";

    @Override
    public void downloadRuleTemplate(HttpServletResponse response) {
        String path = "classpath:excel/replenishmentRuleTemplate.xlsx";
        String excelName = "template.xlsx";
        ExcelUtil.downloadTemplate(path,excelName,response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importRule(MultipartFile excelFile,String platformType, HttpServletResponse response) {
        //平台信息
        List<DictBasicDTO.ViewDTO> platformViewList = customerFeign.getDictBasicByKey(DictBasicTypeEnum.SALES_PLATFORM.getType());

        //备货
        Pair<List<StockUpImportExcelDTO>, List<StockUpImportExcelDTO>> stockUpPair = importStockUp(excelFile,platformViewList,platformType);
        //动态备货系数
        Pair<List<StockingRatioImportExcelDTO>, List<StockingRatioImportExcelDTO>> stockingRatioPair = importStockingRatio(excelFile,platformViewList,platformType);
        //默认日销量
        Pair<List<DefaultSalesQtyImportExcelDTO>, List<DefaultSalesQtyImportExcelDTO>> defaultSalesQtyPair = importDefaultSalesQty(excelFile,platformViewList,platformType);
        //动态日销量
        Pair<List<DynamicSalesQtyImportExcelDTO>, List<DynamicSalesQtyImportExcelDTO>> dynamicSalesQtyPair = importDynamicSalesQty(excelFile,platformViewList,platformType);
        //固定日销量
        Pair<List<FixedSalesQtyImportExcelDTO>, List<FixedSalesQtyImportExcelDTO>> fixedSalesQtyPair = importFixedSalesQty(excelFile,platformViewList,platformType);
        //固定日销量
        Pair<List<SalesDenoisingImportExcelDTO>, List<SalesDenoisingImportExcelDTO>> salesDenoisingPair = importSalesDenoising(excelFile,platformViewList,platformType);
        //导入文件名称
        String originalFilename = excelFile.getOriginalFilename();
        //上传正确数据
        upLoadSuccessExcel (originalFilename,stockUpPair.getKey(),stockingRatioPair.getKey(),defaultSalesQtyPair.getKey(),
                dynamicSalesQtyPair.getKey(),fixedSalesQtyPair.getKey(),salesDenoisingPair.getKey(),platformType);
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
    private void upLoadSuccessExcel (String originalFilename,List<StockUpImportExcelDTO> stockUpList,List<StockingRatioImportExcelDTO> stockingRatioList,List<DefaultSalesQtyImportExcelDTO> defaultSalesQtyList,
                                   List<DynamicSalesQtyImportExcelDTO> dynamicSalesQtyList,List<FixedSalesQtyImportExcelDTO> fixedSalesQtyList,List<SalesDenoisingImportExcelDTO> salesDenoisingList,String platformType) {
        //全部为空则无需处理
        if (CollectionUtils.isEmpty(stockUpList) && CollectionUtils.isEmpty(stockingRatioList)  && CollectionUtils.isEmpty(defaultSalesQtyList)
                && CollectionUtils.isEmpty(dynamicSalesQtyList) && CollectionUtils.isEmpty(fixedSalesQtyList) && CollectionUtils.isEmpty(salesDenoisingList)) {
            return;
        }
        String fileName = CharSequenceUtil.isBlank(originalFilename) ? "补货规则.xlsx" : originalFilename;
        String pathUrl = "excel/replenishmentRule.xlsx";
        FileExcelDTO.ExportFileDTO exportFileDTO = new FileExcelDTO.ExportFileDTO();
        exportFileDTO.setFileName(fileName);
        exportFileDTO.setPathUrl(pathUrl);
        List<Pair<Integer, List<?>>> sheetList = new ArrayList<>();
        sheetList.add(new Pair<>(MathUtil.ZERO,stockUpList));
        sheetList.add(new Pair<>(MathUtil.ONE,stockingRatioList));
        sheetList.add(new Pair<>(MathUtil.TWO,defaultSalesQtyList));
        sheetList.add(new Pair<>(MathUtil.THREE,dynamicSalesQtyList));
        sheetList.add(new Pair<>(MathUtil.FOUR,fixedSalesQtyList));
        sheetList.add(new Pair<>(MathUtil.FIVE,salesDenoisingList));
        exportFileDTO.setSheetList(sheetList);

        //添加导入记录
        HistoryImportRecordDTO.AddDTO dto = new HistoryImportRecordDTO.AddDTO();
        dto.setName(fileName);
        dto.setModule(SourceTypeEnum.REPLENISHMENT_SUGGESTION.getCode());
        dto.setType(HistoryImportRecordTypeEnum.CFG_RULE_REPLENISHMENT.getCode());
        dto.setExportFileDTO(exportFileDTO);
        dto.setPlatformType(platformType);
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
        if (CollectionUtils.isEmpty(stockUpList) && CollectionUtils.isEmpty(stockingRatioList)
                && CollectionUtils.isEmpty(defaultSalesQtyList) && CollectionUtils.isEmpty(dynamicSalesQtyList)
                && CollectionUtils.isEmpty(fixedSalesQtyList) && CollectionUtils.isEmpty(salesDenoisingList)) {
            return;
        }
        List<Pair<Integer, List<?>>> pairList = new ArrayList<>();
        pairList.add(new Pair<>(MathUtil.ZERO,stockUpList));
        pairList.add(new Pair<>(MathUtil.ONE,stockingRatioList));
        pairList.add(new Pair<>(MathUtil.TWO,defaultSalesQtyList));
        pairList.add(new Pair<>(MathUtil.THREE,dynamicSalesQtyList));
        pairList.add(new Pair<>(MathUtil.FOUR,fixedSalesQtyList));
        pairList.add(new Pair<>(MathUtil.FIVE,salesDenoisingList));
        String name = "补货规则错误数据";
        StringBuilder sb = new StringBuilder();
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
    private Pair<List<StockUpImportExcelDTO>,List<StockUpImportExcelDTO>> importStockUp (MultipartFile excelFile, List<DictBasicDTO.ViewDTO> platformViewList,String platformType) {
        StockUpImportExcelListener excelListenerUtil = new StockUpImportExcelListener();

        try {
            EasyExcelFactory.read(excelFile.getInputStream(), StockUpImportExcelDTO.class, excelListenerUtil).headRowNumber(2).sheet(0).doRead();
        } catch (IOException e) {
            log.error(ApiError.ERROR_95124.msg, e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error(ApiError.ERROR_1016.msg, e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<StockUpImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            return new Pair<>(new ArrayList<>(),new ArrayList<>());
        }
        //导入数据处理
        List<StockUpImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<StockUpImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理校验导入成功数据
        handleImportStockUp(successList, errorList,platformViewList,platformType);
        return new Pair<>(successList,errorList);
    }
    /**
     * 备货数据处理
     * @author will
     * @date 2024/9/3 15:48
     * @param successList
     * @param errorList
     */
    private void handleImportStockUp (List<StockUpImportExcelDTO> successList, List<StockUpImportExcelDTO> errorList, List<DictBasicDTO.ViewDTO> platformViewList,String platformType) {
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
        List<ReplenishmentSuggestionEntity> replenishmentSuggestionList = replenishmentSuggestionService.listByUnique(platformCodeList, shopIdList, skuIdList);
        //记录错误数据
        List<StockUpImportExcelDTO>  wrongList = new ArrayList<>();

        for (StockUpImportExcelDTO excelDTO : successList) {
            List<String> errorMsgList = new ArrayList<>();
            //平台信息
            String platformCode = platformViewList.stream().filter(obj -> CharSequenceUtil.equals(obj.getName(), excelDTO.getPlatform())).map(DictBasicDTO.ViewDTO::getValue).findFirst().orElse("");
            //店铺信息
            String shopId = shopInfoList.stream().filter(obj -> CharSequenceUtil.equals(obj.getName(), excelDTO.getShopName()) && CharSequenceUtil.equals(obj.getDictPlatform(), platformCode)).map(ShopInfoEntity::getId).findFirst().orElse("");
            //SKU
            String skuId = productDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuNo(), excelDTO.getSkuNo())).map(ProductDetailEntity::getId).findFirst().orElse("");

            //物流信息
            checkLogisticsData(excelDTO,errorMsgList);

            //补货建议主表信息
            ReplenishmentSuggestionEntity entity = replenishmentSuggestionList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), skuId) && CharSequenceUtil.equals(obj.getShopId(), shopId) && CharSequenceUtil.equals(platformCode, obj.getPlatform())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(entity)) {
                errorMsgList.add(CharSequenceUtil.format(SALE_ERROR_MSG,excelDTO.getPlatform(),excelDTO.getShopName(),excelDTO.getSkuNo()));
            }
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                //错误数据
                wrongList.add(excelDTO);

                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                continue;
            }
            CfgRuleStockUpDTO.CustomUpdateDTO customUpdateDTO = formatCfgRuleStockUpDTO(excelDTO,entity);
            try {
                cgRuleStockUpService.customUpdate(customUpdateDTO);
            }catch (Exception e) {
                errorMsgList.add(e.getMessage());
            }
            //添加错误数据
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                //错误数据
                wrongList.add(excelDTO);
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
            }
        }
        successList.removeAll(wrongList);
    }

    /**
     * 验证 物流方式
     * @author will
     * @date 2024/9/4 15:07
     * @param excelDTO
     * @param errorMsgList
     */
    private void checkLogisticsData (StockUpImportExcelDTO excelDTO,List<String> errorMsgList) {
        boolean isOneFull = (CharSequenceUtil.isNotBlank(excelDTO.getOneLogisticsDays()) || CharSequenceUtil.isNotBlank(excelDTO.getOneLogisticsCycleDays()) || CharSequenceUtil.isNotBlank(excelDTO.getOneIndex()))
                && (CharSequenceUtil.isBlank(excelDTO.getOneLogisticsDays()) || CharSequenceUtil.isBlank(excelDTO.getOneLogisticsCycleDays()) || CharSequenceUtil.isBlank(excelDTO.getOneIndex()));
        if (isOneFull) {
            errorMsgList.add("物流方式【空运】未设置完全");
        }
        boolean isTwoFull = (CharSequenceUtil.isNotBlank(excelDTO.getTwoLogisticsDays()) || CharSequenceUtil.isNotBlank(excelDTO.getTwoLogisticsCycleDays()) || CharSequenceUtil.isNotBlank(excelDTO.getTwoIndex()))
                && (CharSequenceUtil.isBlank(excelDTO.getTwoLogisticsDays()) || CharSequenceUtil.isBlank(excelDTO.getTwoLogisticsCycleDays()) || CharSequenceUtil.isBlank(excelDTO.getTwoIndex()));
        if (isTwoFull) {
            errorMsgList.add("物流方式【快递】未设置完全");
        }
        boolean isThreeFull = (CharSequenceUtil.isNotBlank(excelDTO.getThreeLogisticsDays()) || CharSequenceUtil.isNotBlank(excelDTO.getThreeLogisticsCycleDays()) || CharSequenceUtil.isNotBlank(excelDTO.getThreeIndex()))
                && (CharSequenceUtil.isBlank(excelDTO.getThreeLogisticsDays()) || CharSequenceUtil.isBlank(excelDTO.getThreeLogisticsCycleDays()) || CharSequenceUtil.isBlank(excelDTO.getThreeIndex()));
        if (isThreeFull) {
            errorMsgList.add("物流方式【海运散装】未设置完全");
        }
        boolean isFourFull = (CharSequenceUtil.isNotBlank(excelDTO.getFourLogisticsDays()) || CharSequenceUtil.isNotBlank(excelDTO.getFourLogisticsCycleDays()) || CharSequenceUtil.isNotBlank(excelDTO.getFourIndex()))
                && (CharSequenceUtil.isBlank(excelDTO.getFourLogisticsDays()) || CharSequenceUtil.isBlank(excelDTO.getFourLogisticsCycleDays()) || CharSequenceUtil.isBlank(excelDTO.getFourIndex()));
        if (isFourFull) {
            errorMsgList.add("物流方式【海运整柜】未设置完全");
        }
        boolean isFiveFull = (CharSequenceUtil.isNotBlank(excelDTO.getFiveLogisticsDays()) || CharSequenceUtil.isNotBlank(excelDTO.getFiveLogisticsCycleDays()) || CharSequenceUtil.isNotBlank(excelDTO.getFiveIndex()))
                && (CharSequenceUtil.isBlank(excelDTO.getFiveLogisticsDays()) || CharSequenceUtil.isBlank(excelDTO.getFiveLogisticsCycleDays()) || CharSequenceUtil.isBlank(excelDTO.getFiveIndex()));
        if (isFiveFull) {
            errorMsgList.add("物流方式【铁运散装】未设置完全");
        }
        boolean isSixFull = (CharSequenceUtil.isNotBlank(excelDTO.getSixLogisticsDays()) || CharSequenceUtil.isNotBlank(excelDTO.getSixLogisticsCycleDays()) || CharSequenceUtil.isNotBlank(excelDTO.getSixIndex()))
                && (CharSequenceUtil.isBlank(excelDTO.getSixLogisticsDays()) || CharSequenceUtil.isBlank(excelDTO.getSixLogisticsCycleDays()) || CharSequenceUtil.isBlank(excelDTO.getSixIndex()));
        if (isSixFull) {
            errorMsgList.add("物流方式【铁运整柜】未设置完全");
        }
        List<String> indexList = Arrays.asList(excelDTO.getOneIndex(),excelDTO.getTwoIndex(),excelDTO.getThreeIndex(),excelDTO.getFourIndex(),excelDTO.getFiveIndex(),excelDTO.getSixIndex());
        String indexs = indexList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj) && indexList.stream().filter(e -> CharSequenceUtil.equals(e, obj)).count() > MathUtil.ONE).map(obj -> obj).distinct().collect(Collectors.joining(","));
        if (CharSequenceUtil.isNotBlank(indexs)) {
            errorMsgList.add(CharSequenceUtil.format("物流时效优先级【{}】重复",indexs));
        }

    }
    /**
     * 备货数据格式化
     * @author will
     * @date 2024/9/4 14:44
     * @param excelDTO
     * @param entity
     * @return CustomUpdateDTO
     */
    private CfgRuleStockUpDTO.CustomUpdateDTO formatCfgRuleStockUpDTO (StockUpImportExcelDTO excelDTO,ReplenishmentSuggestionEntity entity) {
        CfgRuleStockUpDTO.CustomUpdateDTO resultDTO = new CfgRuleStockUpDTO.CustomUpdateDTO();
        resultDTO.setRefId(entity.getId());
        resultDTO.setRefType(SourceTypeEnum.REPLENISHMENT_SUGGESTION.getCode());
        resultDTO.setPlatformType(entity.getPlatformType());
        if (ObjectUtil.isNotEmpty(excelDTO.getProductionDays())) {
            resultDTO.setProductionDays(Integer.valueOf(excelDTO.getProductionDays()));
        }
        if (ObjectUtil.isNotEmpty(excelDTO.getPurchaseApproveDays())) {
            resultDTO.setPurchaseApproveDays(Integer.valueOf(excelDTO.getPurchaseApproveDays()));
        }
        if (ObjectUtil.isNotEmpty(excelDTO.getSupplierDeliveryDays())) {
            resultDTO.setSupplierDeliveryDays(Integer.valueOf(excelDTO.getSupplierDeliveryDays()));
        }
        if (ObjectUtil.isNotEmpty(excelDTO.getQcDays())) {
            resultDTO.setQcDays(Integer.valueOf(excelDTO.getQcDays()));
        }
        if (ObjectUtil.isNotEmpty(excelDTO.getPurchaseCycleDays())) {
            resultDTO.setPurchaseCycleDays(Integer.valueOf(excelDTO.getPurchaseCycleDays()));
        }
        if (ObjectUtil.isNotEmpty(excelDTO.getSafeDays())) {
            resultDTO.setSafeDays(Integer.valueOf(excelDTO.getSafeDays()));
        }
        if (ObjectUtil.isNotEmpty(excelDTO.getStockingRatio())) {
            resultDTO.setStockingRatio(MathUtil.valueOf(excelDTO.getStockingRatio()));
        }

        //物流信息
        List<CfgRuleLogisticsDTO.UpdateDTO> cfgLogisticsList = new ArrayList<>();

        //空运
        if (CharSequenceUtil.isNotBlank(excelDTO.getOneIndex())) {
            CfgRuleLogisticsDTO.UpdateDTO oneUpdateDTO = new CfgRuleLogisticsDTO.UpdateDTO();
            oneUpdateDTO.setLogisticsMethod(LogisticsMethodEnum.AIRFREIGHT.getCode());
            oneUpdateDTO.setIndex(Integer.valueOf(excelDTO.getOneIndex()));
            oneUpdateDTO.setLogisticsDays(Integer.valueOf(excelDTO.getOneLogisticsDays()));
            oneUpdateDTO.setLogisticsCycleDays(Integer.valueOf(excelDTO.getOneLogisticsCycleDays()));
            cfgLogisticsList.add(oneUpdateDTO);
        }

        //快递
        if (CharSequenceUtil.isNotBlank(excelDTO.getTwoIndex())) {
            CfgRuleLogisticsDTO.UpdateDTO twoUpdateDTO = new CfgRuleLogisticsDTO.UpdateDTO();
            twoUpdateDTO.setLogisticsMethod(LogisticsMethodEnum.EXPRESS.getCode());
            twoUpdateDTO.setIndex(Integer.valueOf(excelDTO.getTwoIndex()));
            twoUpdateDTO.setLogisticsDays(Integer.valueOf(excelDTO.getTwoLogisticsDays()));
            twoUpdateDTO.setLogisticsCycleDays(Integer.valueOf(excelDTO.getTwoLogisticsCycleDays()));
            cfgLogisticsList.add(twoUpdateDTO);
        }

        //海运散装
        if (CharSequenceUtil.isNotBlank(excelDTO.getThreeIndex())) {
            CfgRuleLogisticsDTO.UpdateDTO threeUpdateDTO = new CfgRuleLogisticsDTO.UpdateDTO();
            threeUpdateDTO.setLogisticsMethod(LogisticsMethodEnum.OCEAN_FREIGHT_BULK.getCode());
            threeUpdateDTO.setIndex(Integer.valueOf(excelDTO.getThreeIndex()));
            threeUpdateDTO.setLogisticsDays(Integer.valueOf(excelDTO.getThreeLogisticsDays()));
            threeUpdateDTO.setLogisticsCycleDays(Integer.valueOf(excelDTO.getThreeLogisticsCycleDays()));
            cfgLogisticsList.add(threeUpdateDTO);
        }

        //海运整柜
        if (CharSequenceUtil.isNotBlank(excelDTO.getFourIndex())) {
            CfgRuleLogisticsDTO.UpdateDTO fourUpdateDTO = new CfgRuleLogisticsDTO.UpdateDTO();
            fourUpdateDTO.setLogisticsMethod(LogisticsMethodEnum.OCEAN_FREIGHT_FCL.getCode());
            fourUpdateDTO.setIndex(Integer.valueOf(excelDTO.getFourIndex()));
            fourUpdateDTO.setLogisticsDays(Integer.valueOf(excelDTO.getFourLogisticsDays()));
            fourUpdateDTO.setLogisticsCycleDays(Integer.valueOf(excelDTO.getFourLogisticsCycleDays()));
            cfgLogisticsList.add(fourUpdateDTO);
        }

        //铁运散装
        if (CharSequenceUtil.isNotBlank(excelDTO.getFiveIndex())) {
            CfgRuleLogisticsDTO.UpdateDTO fiveUpdateDTO = new CfgRuleLogisticsDTO.UpdateDTO();
            fiveUpdateDTO.setLogisticsMethod(LogisticsMethodEnum.RAILWAY_TRANSPORTATION_BULK.getCode());
            fiveUpdateDTO.setIndex(Integer.valueOf(excelDTO.getFiveIndex()));
            fiveUpdateDTO.setLogisticsDays(Integer.valueOf(excelDTO.getFiveLogisticsDays()));
            fiveUpdateDTO.setLogisticsCycleDays(Integer.valueOf(excelDTO.getFiveLogisticsCycleDays()));
            cfgLogisticsList.add(fiveUpdateDTO);
        }

        //铁运散装
        if (CharSequenceUtil.isNotBlank(excelDTO.getSixIndex())) {
            CfgRuleLogisticsDTO.UpdateDTO sixUpdateDTO = new CfgRuleLogisticsDTO.UpdateDTO();
            sixUpdateDTO.setLogisticsMethod(LogisticsMethodEnum.RAILWAY_TRANSPORTATION_FCL.getCode());
            sixUpdateDTO.setIndex(Integer.valueOf(excelDTO.getSixIndex()));
            sixUpdateDTO.setLogisticsDays(Integer.valueOf(excelDTO.getSixLogisticsDays()));
            sixUpdateDTO.setLogisticsCycleDays(Integer.valueOf(excelDTO.getSixLogisticsCycleDays()));
            cfgLogisticsList.add(sixUpdateDTO);
        }
        resultDTO.setCfgLogisticsList(cfgLogisticsList);
        return resultDTO;
    }

    /**
     * 动态备货系数
     */
    private  Pair<List<StockingRatioImportExcelDTO>,List<StockingRatioImportExcelDTO>> importStockingRatio (MultipartFile excelFile, List<DictBasicDTO.ViewDTO> platformViewList,String platformType){
        StockingRatioImportExcelListener excelListenerUtil = new StockingRatioImportExcelListener();

        try {
            EasyExcelFactory.read(excelFile.getInputStream(), StockingRatioImportExcelDTO.class, excelListenerUtil).sheet(1).doRead();
        } catch (IOException e) {
            log.error(ApiError.ERROR_95124.msg, e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error(ApiError.ERROR_1016.msg, e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<StockingRatioImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            return new Pair<>(new ArrayList<>(),new ArrayList<>());
        }
        //导入数据处理
        List<StockingRatioImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<StockingRatioImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理校验导入成功数据
        handleImportStockingRatio(successList, errorList,platformViewList,platformType);
        return new Pair<>(successList,errorList);
    }
    /**
     * 动态备货系数数据
     * @author will
     * @date 2024/9/3 15:48
     * @param successList
     * @param errorList
     */
    private void handleImportStockingRatio (List<StockingRatioImportExcelDTO> successList, List<StockingRatioImportExcelDTO> errorList, List<DictBasicDTO.ViewDTO> platformViewList,String platformType) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        //平台
        List<String> platformList = successList.stream().map(StockingRatioImportExcelDTO::getPlatform).distinct().collect(Collectors.toList());
        List<String> platformCodeList = platformViewList.stream().filter(obj -> platformList.contains(obj.getName())).map(DictBasicDTO.ViewDTO::getValue).collect(Collectors.toList());

        //店铺
        List<String> shopNameList = successList.stream().map(StockingRatioImportExcelDTO::getShopName).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = FeignQuery.create(ShopInfoEntity.class).in(ShopInfoEntity::getName, shopNameList).list();
        List<String> shopIdList = shopInfoList.stream().map(ShopInfoEntity::getId).collect(Collectors.toList());

        //SKU
        List<String> skuNoList = successList.stream().map(StockingRatioImportExcelDTO::getSkuNo).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = FeignQuery.create(ProductDetailEntity.class).in(ProductDetailEntity::getSkuNo, skuNoList).list();
        List<String> skuIdList = productDetailList.stream().map(ProductDetailEntity::getId).collect(Collectors.toList());

        //根据平台、店铺、skuId查询补货建议数据
        List<ReplenishmentSuggestionEntity> replenishmentSuggestionList = replenishmentSuggestionService.listByUnique(platformCodeList, shopIdList, skuIdList);

        //查询备货信息
        List<String> refIdList = replenishmentSuggestionList.stream().map(ReplenishmentSuggestionEntity::getId).distinct().collect(Collectors.toList());
        List<CfgRuleStockUpEntity> cfgRuleStockUpList = cfgRuleStockUpService.listByRefIdList(refIdList);

        Map<String, List<StockingRatioImportExcelDTO>> map = successList.stream().collect(Collectors.groupingBy(obj -> obj.getPlatform().concat(obj.getShopName()).concat(obj.getSkuNo())));

        //记录错误数据
        List<StockingRatioImportExcelDTO>  wrongList = new ArrayList<>();

        for (Map.Entry<String, List<StockingRatioImportExcelDTO>> entry : map.entrySet()) {
            List<StockingRatioImportExcelDTO> value = entry.getValue();

            StockingRatioImportExcelDTO excelDTO = value.get(0);

            List<String> errorMsgList = new ArrayList<>();
            //平台信息
            String platformCode = platformViewList.stream().filter(obj -> CharSequenceUtil.equals(obj.getName(), excelDTO.getPlatform())).map(DictBasicDTO.ViewDTO::getValue).findFirst().orElse("");
            //店铺信息
            String shopId = shopInfoList.stream().filter(obj -> CharSequenceUtil.equals(obj.getName(), excelDTO.getShopName()) && CharSequenceUtil.equals(obj.getDictPlatform(), platformCode)).map(ShopInfoEntity::getId).findFirst().orElse("");
            //SKU
            String skuId = productDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuNo(), excelDTO.getSkuNo())).map(ProductDetailEntity::getId).findFirst().orElse("");

            //补货建议主表信息
            ReplenishmentSuggestionEntity entity = replenishmentSuggestionList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), skuId) && CharSequenceUtil.equals(obj.getShopId(), shopId) && CharSequenceUtil.equals(platformCode, obj.getPlatform())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(entity)) {
                errorMsgList.add(CharSequenceUtil.format("平台【{}】、店铺【{}】、SKU【{}】未找到对应的补货建议数据",excelDTO.getPlatform(),excelDTO.getShopName(),excelDTO.getSkuNo()));
            }

            //备货信息
            String suggestId = ObjectUtils.isEmpty(entity) ? "" : entity.getId();
            CfgRuleStockUpEntity cfgRuleStockUpEntity = cfgRuleStockUpList.stream().filter(obj -> CharSequenceUtil.equals(obj.getRefId(), suggestId)).findFirst().orElse(new CfgRuleStockUpEntity());
            if (CharSequenceUtil.isBlank(cfgRuleStockUpEntity.getId())) {
                errorMsgList.add(CharSequenceUtil.format("平台【{}】、店铺【{}】、SKU【{}】未找到对应的备货设置数据",excelDTO.getPlatform(),excelDTO.getShopName(),excelDTO.getSkuNo()));
            }
            List<CfgRuleStockingRatioDTO.UpdateDTO> updateDTOList = new ArrayList<>();
            for (StockingRatioImportExcelDTO importExcelDTO: value) {
                List<String> errorMsgDetailList = new ArrayList<>();
                //上级错误信息
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    errorMsgDetailList.addAll(errorMsgList);
                }
                long count = value.stream().filter(obj -> CharSequenceUtil.equals(obj.getName(), importExcelDTO.getName())).count();
                if (count > 1) {
                    errorMsgDetailList.add("备货系数名称唯一不能添加重复数据");
                }
                //日期
                LocalDate startDate = LocalDateUtil.parseStrToLocalDate(importExcelDTO.getStartDateStr());
                LocalDate endDate = LocalDateUtil.parseStrToLocalDate(importExcelDTO.getEndDateStr());
                if (startDate.isAfter(endDate)) {
                    errorMsgDetailList.add("开始时间不能大于结束时间");
                }

                if (CollectionUtils.isNotEmpty(errorMsgDetailList)) {
                    //错误数据
                    wrongList.add(importExcelDTO);
                    importExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgDetailList));
                    errorList.add(importExcelDTO);
                    continue;
                }
                CfgRuleStockingRatioDTO.UpdateDTO updateDTO = formatCfgRuleStockUpDTO(importExcelDTO);
                updateDTOList.add(updateDTO);
            }
            cfgRuleStockingRatioService.update(updateDTOList,cfgRuleStockUpEntity.getId(), CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode(),Boolean.TRUE);
        }
        successList.removeAll(wrongList);
    }

    /**
     * 格式化动态备货系数
     * @author will
     * @date 2024/9/4 16:07
     * @param excelDTO
     * @return List<UpdateDTO>
     */
    private CfgRuleStockingRatioDTO.UpdateDTO formatCfgRuleStockUpDTO (StockingRatioImportExcelDTO excelDTO) {
        CfgRuleStockingRatioDTO.UpdateDTO updateDTO = new CfgRuleStockingRatioDTO.UpdateDTO();
        updateDTO.setName(excelDTO.getName());
        updateDTO.setStockingRatio(MathUtil.valueOf(excelDTO.getStockingRatioStr()));
        //日期
        LocalDate startDate = LocalDateUtil.parseStrToLocalDate(excelDTO.getStartDateStr());
        LocalDate endDate = LocalDateUtil.parseStrToLocalDate(excelDTO.getEndDateStr());
        updateDTO.setDateList(Arrays.asList(startDate,endDate));
        return updateDTO;
    }

    /**
     * 导入默认日销量
     */
    private Pair<List<DefaultSalesQtyImportExcelDTO>,List<DefaultSalesQtyImportExcelDTO>> importDefaultSalesQty (MultipartFile excelFile, List<DictBasicDTO.ViewDTO> platformViewList,String platformType) {
        DefaultSalesQtyImportExcelListener excelListenerUtil = new DefaultSalesQtyImportExcelListener();

        try {
            EasyExcelFactory.read(excelFile.getInputStream(), DefaultSalesQtyImportExcelDTO.class, excelListenerUtil).headRowNumber(2).sheet(2).doRead();
        } catch (IOException e) {
            log.error(ApiError.ERROR_95124.msg, e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error(ApiError.ERROR_1016.msg, e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<DefaultSalesQtyImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            return new Pair<>(new ArrayList<>(),new ArrayList<>());
        }
        //导入数据处理
        List<DefaultSalesQtyImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<DefaultSalesQtyImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理校验导入成功数据
        handleImportDefaultSalesQty(successList,errorList,platformViewList,platformType);
        return new Pair<>(successList,errorList);
    }
    /**
     * 导入默认日销量处理
     * @author will
     * @date 2024/9/3 15:49
     * @param successList
     * @param errorList
     */
    private void handleImportDefaultSalesQty (List<DefaultSalesQtyImportExcelDTO> successList, List<DefaultSalesQtyImportExcelDTO> errorList, List<DictBasicDTO.ViewDTO> platformViewList,String platformType) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        //平台
        List<String> platformList = successList.stream().map(DefaultSalesQtyImportExcelDTO::getPlatform).distinct().collect(Collectors.toList());
        List<String> platformCodeList = platformViewList.stream().filter(obj -> platformList.contains(obj.getName())).map(DictBasicDTO.ViewDTO::getValue).collect(Collectors.toList());

        //店铺
        List<String> shopNameList = successList.stream().map(DefaultSalesQtyImportExcelDTO::getShopName).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = FeignQuery.create(ShopInfoEntity.class).in(ShopInfoEntity::getName, shopNameList).list();
        List<String> shopIdList = shopInfoList.stream().map(ShopInfoEntity::getId).collect(Collectors.toList());

        //SKU
        List<String> skuNoList = successList.stream().map(DefaultSalesQtyImportExcelDTO::getSkuNo).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = FeignQuery.create(ProductDetailEntity.class).in(ProductDetailEntity::getSkuNo, skuNoList).list();
        List<String> skuIdList = productDetailList.stream().map(ProductDetailEntity::getId).collect(Collectors.toList());

        //根据平台、店铺、skuId查询补货建议数据
        List<ReplenishmentSuggestionEntity> replenishmentSuggestionList = replenishmentSuggestionService.listByUnique(platformCodeList, shopIdList, skuIdList);

        //销量数据
        List<String> refIdList = replenishmentSuggestionList.stream().map(ReplenishmentSuggestionEntity::getId).distinct().collect(Collectors.toList());
        List<CfgRuleSalesQtyEntity> cfgRuleSalesQtyList = cfgRuleSalesQtyService.listByRefIdList(refIdList);

        //记录错误数据
        List<DefaultSalesQtyImportExcelDTO>  wrongList = new ArrayList<>();
        for (DefaultSalesQtyImportExcelDTO excelDTO : successList) {
            List<String> errorMsgList = new ArrayList<>();
            //平台信息
            String platformCode = platformViewList.stream().filter(obj -> CharSequenceUtil.equals(obj.getName(), excelDTO.getPlatform())).map(DictBasicDTO.ViewDTO::getValue).findFirst().orElse("");
            //店铺信息
            String shopId = shopInfoList.stream().filter(obj -> CharSequenceUtil.equals(obj.getName(), excelDTO.getShopName()) && CharSequenceUtil.equals(obj.getDictPlatform(), platformCode)).map(ShopInfoEntity::getId).findFirst().orElse("");
            //SKU
            String skuId = productDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuNo(), excelDTO.getSkuNo())).map(ProductDetailEntity::getId).findFirst().orElse("");

            if (CfgRuleSalesFormulaDefaultTypeEnum.FIXED.getCode().equals(excelDTO.getDefaultTypeName()) && CharSequenceUtil.isBlank(excelDTO.getFixedValue())) {
                errorMsgList.add("默认固定销量类型，固定日销量必填");
            }
            //补货建议主表信息
            ReplenishmentSuggestionEntity entity = replenishmentSuggestionList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), skuId) && CharSequenceUtil.equals(obj.getShopId(), shopId) && CharSequenceUtil.equals(platformCode, obj.getPlatform())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(entity)) {
                errorMsgList.add(CharSequenceUtil.format("平台【{}】、店铺【{}】、SKU【{}】未找到对应的补货建议数据",excelDTO.getPlatform(),excelDTO.getShopName(),excelDTO.getSkuNo()));
            }

            //销量数据
            String suggestId = ObjectUtils.isEmpty(entity) ? "" : entity.getId();
            CfgRuleSalesQtyEntity salesQtyEntity = cfgRuleSalesQtyList.stream().filter(obj -> CharSequenceUtil.equals(obj.getRefId(), suggestId)).findFirst().orElse(new CfgRuleSalesQtyEntity());
            if (ObjectUtil.isEmpty(salesQtyEntity)) {
                
                errorMsgList.add(CharSequenceUtil.format(SALE_ERROR_MSG,excelDTO.getPlatform(),excelDTO.getShopName(),excelDTO.getSkuNo()));
            }
            //错误数据
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                wrongList.add(excelDTO);
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                continue;
            }
            //销量主表
            CfgRuleSalesQtyDTO.UpdateDetailDTO updateDTO = formatSalesQty(Optional.ofNullable(entity).orElse(new ReplenishmentSuggestionEntity()));
            //默认销量
            List<CfgRuleSalesFormulaDTO.UpdateDTO> salesFormulaList = formatDefaultSalesQty(excelDTO);
            try {
                //更新产品销量配置
                String salesQtyId = cfgRuleSalesQtyService.update(updateDTO);

                //添加默认配置
                cfgRuleSalesFormulaService.update(salesFormulaList,salesQtyId,Boolean.FALSE);
            } catch (Exception e) {
                errorMsgList.add(e.getMessage());
            }
            //保存里面的验证
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                wrongList.add(excelDTO);
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
            }
        }
        successList.removeAll(wrongList);
    }

    /**
     * 销量主表数据
     * @author will
     * @date 2024/9/10 11:45
     * @param entity
     * @return UpdateDetailDTO
     */
    private CfgRuleSalesQtyDTO.UpdateDetailDTO  formatSalesQty (ReplenishmentSuggestionEntity entity) {
        CfgRuleSalesQtyDTO.UpdateDetailDTO updateDTO = new CfgRuleSalesQtyDTO.UpdateDetailDTO();
        updateDTO.setPlatformType(entity.getPlatformType());
        updateDTO.setRefId(entity.getId());
        updateDTO.setRefType(SourceTypeEnum.REPLENISHMENT_SUGGESTION.getCode());
        updateDTO.setIsCustom(Boolean.TRUE);
        return updateDTO;
    }

    /**
     * 格式化默认销量
     * @author will
     * @date 2024/9/4 19:52
     * @param excelDTO
     * @return List<UpdateDTO>
     */
    private List<CfgRuleSalesFormulaDTO.UpdateDTO>  formatDefaultSalesQty (DefaultSalesQtyImportExcelDTO excelDTO) {
        CfgRuleSalesFormulaDTO.UpdateDTO updateDTO = new CfgRuleSalesFormulaDTO.UpdateDTO();
        updateDTO.setType(CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode());
        updateDTO.setDefaultType(CfgRuleSalesFormulaDefaultTypeEnum.getCode(excelDTO.getDefaultTypeName()));
        updateDTO.setPriority(MathUtil.THREE);
        if (CfgRuleSalesFormulaDefaultTypeEnum.DYNAMIC.getCode().equals(updateDTO.getDefaultType())) {
            CfgRuleSalesFormulaDTO.PercentJsonDTO percentJsonDTO = formatPercentJson(excelDTO);
            updateDTO.setPercentJsonDTO(percentJsonDTO);
        } else {
            updateDTO.setFixedValue(Integer.valueOf(excelDTO.getFixedValue()));
        }
        return Arrays.asList(updateDTO);
    }

    /**
     *
     * @author will
     * @date 2024/9/4 20:02
     * @param excelDTO
     * @return PercentJsonDTO
     */
    private CfgRuleSalesFormulaDTO.PercentJsonDTO formatPercentJson (DefaultSalesQtyImportExcelDTO excelDTO) {
        CfgRuleSalesFormulaDTO.PercentJsonDTO percentJsonDTO = new CfgRuleSalesFormulaDTO.PercentJsonDTO();
        percentJsonDTO.setThreeDaysRatio(MathUtil.valueOfInteger(excelDTO.getThreeDaysRatio()));
        percentJsonDTO.setSevenDaysRatio(MathUtil.valueOfInteger(excelDTO.getSevenDaysRatio()));
        percentJsonDTO.setNinetyDaysRatio(MathUtil.valueOfInteger(excelDTO.getNinetyDaysRatio()));
        percentJsonDTO.setFourteenDaysRatio(MathUtil.valueOfInteger(excelDTO.getFourteenDaysRatio()));
        percentJsonDTO.setThirtyDaysRatio(MathUtil.valueOfInteger(excelDTO.getThirtyDaysRatio()));
        percentJsonDTO.setSixtyDaysRatio(MathUtil.valueOfInteger(excelDTO.getSixtyDaysRatio()));
        percentJsonDTO.setNinetyDaysRatio(MathUtil.valueOfInteger(excelDTO.getNinetyDaysRatio()));
        percentJsonDTO.setOneHundredEightyDaysRatio(MathUtil.valueOfInteger(excelDTO.getOneHundredEightyDaysRatio()));
        percentJsonDTO.setTwoHundredSeventyDaysRatio(MathUtil.valueOfInteger(excelDTO.getTwoHundredSeventyDaysRatio()));
        percentJsonDTO.setThreeHundredSixtyDaysRatio(MathUtil.valueOfInteger(excelDTO.getThreeHundredSixtyDaysRatio()));
        return percentJsonDTO;
    }

    /**
     * 导入动态日销量
     */
    private Pair<List<DynamicSalesQtyImportExcelDTO>,List<DynamicSalesQtyImportExcelDTO>>  importDynamicSalesQty (MultipartFile excelFile, List<DictBasicDTO.ViewDTO> platformViewList,String platformType) {
        DynamicSalesQtyImportExcelListener excelListenerUtil = new DynamicSalesQtyImportExcelListener();

        try {
            EasyExcelFactory.read(excelFile.getInputStream(), DynamicSalesQtyImportExcelDTO.class, excelListenerUtil).headRowNumber(2).sheet(3).doRead();
        } catch (IOException e) {
            log.error(ApiError.ERROR_95124.msg, e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error(ApiError.ERROR_1016.msg, e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<DynamicSalesQtyImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            return new Pair<>(new ArrayList<>(),new ArrayList<>());
        }
        //导入数据处理
        List<DynamicSalesQtyImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<DynamicSalesQtyImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理校验导入成功数据
        handleImportDynamicSalesQty(successList,errorList,platformViewList,platformType);
        return new Pair<>(successList,errorList);
    }

    /**
     * 导入动态日销量
     * @author will
     * @date 2024/9/3 15:49
     * @param successList
     * @param errorList
     */
    private void handleImportDynamicSalesQty (List<DynamicSalesQtyImportExcelDTO> successList, List<DynamicSalesQtyImportExcelDTO> errorList, List<DictBasicDTO.ViewDTO> platformViewList,String platformType) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        //平台
        List<String> platformList = successList.stream().map(DynamicSalesQtyImportExcelDTO::getPlatform).distinct().collect(Collectors.toList());
        List<String> platformCodeList = platformViewList.stream().filter(obj -> platformList.contains(obj.getName())).map(DictBasicDTO.ViewDTO::getValue).collect(Collectors.toList());

        //店铺
        List<String> shopNameList = successList.stream().map(DynamicSalesQtyImportExcelDTO::getShopName).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = FeignQuery.create(ShopInfoEntity.class).in(ShopInfoEntity::getName, shopNameList).list();
        List<String> shopIdList = shopInfoList.stream().map(ShopInfoEntity::getId).collect(Collectors.toList());

        //SKU
        List<String> skuNoList = successList.stream().map(DynamicSalesQtyImportExcelDTO::getSkuNo).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = FeignQuery.create(ProductDetailEntity.class).in(ProductDetailEntity::getSkuNo, skuNoList).list();
        List<String> skuIdList = productDetailList.stream().map(ProductDetailEntity::getId).collect(Collectors.toList());

        //根据平台、店铺、skuId查询补货建议数据
        List<ReplenishmentSuggestionEntity> replenishmentSuggestionList = replenishmentSuggestionService.listByUnique(platformCodeList, shopIdList, skuIdList);

        //销量数据
        List<String> refIdList = replenishmentSuggestionList.stream().map(ReplenishmentSuggestionEntity::getId).distinct().collect(Collectors.toList());
        List<CfgRuleSalesQtyEntity> cfgRuleSalesQtyList = cfgRuleSalesQtyService.listByRefIdList(refIdList);

        //记录错误数据
        List<DynamicSalesQtyImportExcelDTO>  wrongList = new ArrayList<>();

        for (DynamicSalesQtyImportExcelDTO excelDTO : successList) {
            List<String> errorMsgList = new ArrayList<>();
            //平台信息
            String platformCode = platformViewList.stream().filter(obj -> CharSequenceUtil.equals(obj.getName(), excelDTO.getPlatform())).map(DictBasicDTO.ViewDTO::getValue).findFirst().orElse("");
            //店铺信息
            String shopId = shopInfoList.stream().filter(obj -> CharSequenceUtil.equals(obj.getName(), excelDTO.getShopName()) && CharSequenceUtil.equals(obj.getDictPlatform(), platformCode)).map(ShopInfoEntity::getId).findFirst().orElse("");
            //SKU
            String skuId = productDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuNo(), excelDTO.getSkuNo())).map(ProductDetailEntity::getId).findFirst().orElse("");

            //补货建议主表信息
            ReplenishmentSuggestionEntity entity = replenishmentSuggestionList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), skuId) && CharSequenceUtil.equals(obj.getShopId(), shopId) && CharSequenceUtil.equals(platformCode, obj.getPlatform())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(entity)) {
                errorMsgList.add(CharSequenceUtil.format("平台【{}】、店铺【{}】、SKU【{}】未找到对应的补货建议数据",excelDTO.getPlatform(),excelDTO.getShopName(),excelDTO.getSkuNo()));
            }
            //销量数据
            String suggestId = ObjectUtils.isEmpty(entity) ? "" : entity.getId();
            CfgRuleSalesQtyEntity salesQtyEntity = cfgRuleSalesQtyList.stream().filter(obj -> CharSequenceUtil.equals(obj.getRefId(), suggestId)).findFirst().orElse(new CfgRuleSalesQtyEntity());
            if (ObjectUtil.isEmpty(salesQtyEntity)) {
                errorMsgList.add(CharSequenceUtil.format(SALE_ERROR_MSG,excelDTO.getPlatform(),excelDTO.getShopName(),excelDTO.getSkuNo()));
            }
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                wrongList.add(excelDTO);
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                continue;
            }
            List<CfgRuleSalesFormulaDTO.UpdateDTO> salesFormulaList = formatDynamicSalesQty(excelDTO);
            try {
                cfgRuleSalesFormulaService.update(salesFormulaList,salesQtyEntity.getId(),Boolean.TRUE);
            } catch (Exception e) {
                errorMsgList.add(e.getMessage());
            }
            //保存里面的验证
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                wrongList.add(excelDTO);
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
            }
        }
        successList.removeAll(wrongList);
    }
    /**
     * 格式化动态日销量
     * @author will
     * @date 2024/9/5 8:57
     * @param excelDTO
     * @return List<UpdateDTO>
     */
    private List<CfgRuleSalesFormulaDTO.UpdateDTO>  formatDynamicSalesQty (DynamicSalesQtyImportExcelDTO excelDTO) {
        CfgRuleSalesFormulaDTO.UpdateDTO updateDTO = new CfgRuleSalesFormulaDTO.UpdateDTO();
        updateDTO.setType(CfgRuleSalesFormulaTypeEnum.DYNAMIC.getCode());
        updateDTO.setPriority(MathUtil.TWO);
        updateDTO.setName(excelDTO.getName());
        LocalDate startDate = LocalDateUtil.parseStrToLocalDate(excelDTO.getStartDateStr());
        LocalDate endDate = LocalDateUtil.parseStrToLocalDate(excelDTO.getEndDateStr());
        updateDTO.setDateList(Arrays.asList(startDate,endDate));
        DefaultSalesQtyImportExcelDTO salesQtyImportExcelDTO = BeanMapperUtils.map(DefaultSalesQtyImportExcelDTO.class, excelDTO);
        CfgRuleSalesFormulaDTO.PercentJsonDTO percentJsonDTO = formatPercentJson(salesQtyImportExcelDTO);
        updateDTO.setPercentJsonDTO(percentJsonDTO);
        return Arrays.asList(updateDTO);
    }

    /**
     * 导入固定日销量
     */
    private Pair<List< FixedSalesQtyImportExcelDTO>,List<FixedSalesQtyImportExcelDTO>> importFixedSalesQty (MultipartFile excelFile, List<DictBasicDTO.ViewDTO> platformViewList,String platformType) {
        FixedSalesQtyImportExcelListener excelListenerUtil = new FixedSalesQtyImportExcelListener();

        try {
            EasyExcelFactory.read(excelFile.getInputStream(), FixedSalesQtyImportExcelDTO.class, excelListenerUtil).sheet(4).doRead();
        } catch (IOException e) {
            log.error(ApiError.ERROR_95124.msg, e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error(ApiError.ERROR_1016.msg, e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<FixedSalesQtyImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            return new Pair<>(new ArrayList<>(),new ArrayList<>());
        }
        //导入数据处理
        List<FixedSalesQtyImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<FixedSalesQtyImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理校验导入成功数据
        handleImportFixedSalesQty(successList, errorList,platformViewList,platformType);
        return new Pair<>(successList,errorList);
    }

    /**
     * 导入固定日销量
     * @author will
     * @date 2024/9/3 15:49
     * @param successList
     * @param errorList
     */
    private void handleImportFixedSalesQty (List<FixedSalesQtyImportExcelDTO> successList, List<FixedSalesQtyImportExcelDTO> errorList, List<DictBasicDTO.ViewDTO> platformViewList,String platformType) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        //平台
        List<String> platformList = successList.stream().map(FixedSalesQtyImportExcelDTO::getPlatform).distinct().collect(Collectors.toList());
        List<String> platformCodeList = platformViewList.stream().filter(obj -> platformList.contains(obj.getName())).map(DictBasicDTO.ViewDTO::getValue).collect(Collectors.toList());

        //店铺
        List<String> shopNameList = successList.stream().map(FixedSalesQtyImportExcelDTO::getShopName).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = FeignQuery.create(ShopInfoEntity.class).in(ShopInfoEntity::getName, shopNameList).list();
        List<String> shopIdList = shopInfoList.stream().map(ShopInfoEntity::getId).collect(Collectors.toList());

        //SKU
        List<String> skuNoList = successList.stream().map(FixedSalesQtyImportExcelDTO::getSkuNo).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = FeignQuery.create(ProductDetailEntity.class).in(ProductDetailEntity::getSkuNo, skuNoList).list();
        List<String> skuIdList = productDetailList.stream().map(ProductDetailEntity::getId).collect(Collectors.toList());

        //根据平台、店铺、skuId查询补货建议数据
        List<ReplenishmentSuggestionEntity> replenishmentSuggestionList = replenishmentSuggestionService.listByUnique(platformCodeList, shopIdList, skuIdList);

        //销量数据
        List<String> refIdList = replenishmentSuggestionList.stream().map(ReplenishmentSuggestionEntity::getId).distinct().collect(Collectors.toList());
        List<CfgRuleSalesQtyEntity> cfgRuleSalesQtyList = cfgRuleSalesQtyService.listByRefIdList(refIdList);

        //记录错误数据
        List<FixedSalesQtyImportExcelDTO>  wrongList = new ArrayList<>();

        for (FixedSalesQtyImportExcelDTO excelDTO : successList) {
            List<String> errorMsgList = new ArrayList<>();
            //平台信息
            String platformCode = platformViewList.stream().filter(obj -> CharSequenceUtil.equals(obj.getName(), excelDTO.getPlatform())).map(DictBasicDTO.ViewDTO::getValue).findFirst().orElse("");
            //店铺信息
            String shopId = shopInfoList.stream().filter(obj -> CharSequenceUtil.equals(obj.getName(), excelDTO.getShopName()) && CharSequenceUtil.equals(obj.getDictPlatform(), platformCode)).map(ShopInfoEntity::getId).findFirst().orElse("");
            //SKU
            String skuId = productDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuNo(), excelDTO.getSkuNo())).map(ProductDetailEntity::getId).findFirst().orElse("");

            //补货建议主表信息
            ReplenishmentSuggestionEntity entity = replenishmentSuggestionList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), skuId) && CharSequenceUtil.equals(obj.getShopId(), shopId) && CharSequenceUtil.equals(platformCode, obj.getPlatform())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(entity)) {
                errorMsgList.add(CharSequenceUtil.format("平台【{}】、店铺【{}】、SKU【{}】未找到对应的补货建议数据",excelDTO.getPlatform(),excelDTO.getShopName(),excelDTO.getSkuNo()));
            }

            //销量数据
            String suggestId = ObjectUtils.isEmpty(entity) ? "" : entity.getId();
            CfgRuleSalesQtyEntity salesQtyEntity = cfgRuleSalesQtyList.stream().filter(obj -> CharSequenceUtil.equals(obj.getRefId(), suggestId)).findFirst().orElse(new CfgRuleSalesQtyEntity());
            if (ObjectUtil.isEmpty(salesQtyEntity)) {
                errorMsgList.add(CharSequenceUtil.format(SALE_ERROR_MSG,excelDTO.getPlatform(),excelDTO.getShopName(),excelDTO.getSkuNo()));
            }

            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                wrongList.add(excelDTO);
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                continue;
            }
            List<CfgRuleSalesFormulaDTO.UpdateDTO> salesFormulaList = formatFixedSalesQty(excelDTO);
            try {
                cfgRuleSalesFormulaService.update(salesFormulaList,salesQtyEntity.getId(),Boolean.TRUE);
            } catch (Exception e) {
                errorMsgList.add(e.getMessage());
            }
            //保存里面的验证
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                wrongList.add(excelDTO);
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
            }
        }
        successList.removeAll(wrongList);
    }

    /**
     * 固定日销量
     * @author will
     * @date 2024/9/5 9:12
     * @param excelDTO
     * @return List<UpdateDTO>
     */
    private List<CfgRuleSalesFormulaDTO.UpdateDTO>  formatFixedSalesQty (FixedSalesQtyImportExcelDTO excelDTO) {
        CfgRuleSalesFormulaDTO.UpdateDTO updateDTO = new CfgRuleSalesFormulaDTO.UpdateDTO();
        updateDTO.setName(excelDTO.getName());
        updateDTO.setFixedValue(Integer.valueOf(excelDTO.getFixedValue()));
        updateDTO.setPriority(MathUtil.ONE);
        updateDTO.setType(CfgRuleSalesFormulaTypeEnum.FIXED.getCode());
        LocalDate startDate = LocalDateUtil.parseStrToLocalDate(excelDTO.getStartDateStr());
        LocalDate endDate = LocalDateUtil.parseStrToLocalDate(excelDTO.getEndDateStr());
        updateDTO.setDateList(Arrays.asList(startDate,endDate));
        return Arrays.asList(updateDTO);
    }

    /**
     * 销量去噪
     */
    private Pair<List<SalesDenoisingImportExcelDTO>, List<SalesDenoisingImportExcelDTO>> importSalesDenoising (MultipartFile excelFile, List<DictBasicDTO.ViewDTO> platformViewList,String platformType) {
        SalesDenoisingImportExcelListener excelListenerUtil = new SalesDenoisingImportExcelListener();

        try {
            EasyExcelFactory.read(excelFile.getInputStream(), SalesDenoisingImportExcelDTO.class, excelListenerUtil).sheet(5).doRead();
        } catch (IOException e) {
            log.error(ApiError.ERROR_95124.msg, e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error(ApiError.ERROR_1016.msg, e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<SalesDenoisingImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            return new Pair<>(new ArrayList<>(),new ArrayList<>());
        }
        //导入数据处理
        List<SalesDenoisingImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<SalesDenoisingImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理校验导入成功数据
        handleImportSalesDenoising(successList, errorList,platformViewList,platformType);
        return new Pair<>(successList,errorList);
    }

    /**
     * 导入销量去噪
     * @author will
     * @date 2024/9/3 15:49
     * @param successList
     * @param errorList
     */
    private void handleImportSalesDenoising (List<SalesDenoisingImportExcelDTO> successList, List<SalesDenoisingImportExcelDTO> errorList, List<DictBasicDTO.ViewDTO> platformViewList,String platformType) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        //平台
        List<String> platformList = successList.stream().map(SalesDenoisingImportExcelDTO::getPlatform).distinct().collect(Collectors.toList());
        List<String> platformCodeList = platformViewList.stream().filter(obj -> platformList.contains(obj.getName())).map(DictBasicDTO.ViewDTO::getValue).collect(Collectors.toList());

        //店铺
        List<String> shopNameList = successList.stream().map(SalesDenoisingImportExcelDTO::getShopName).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = FeignQuery.create(ShopInfoEntity.class).in(ShopInfoEntity::getName, shopNameList).list();
        List<String> shopIdList = shopInfoList.stream().map(ShopInfoEntity::getId).collect(Collectors.toList());

        //SKU
        List<String> skuNoList = successList.stream().map(SalesDenoisingImportExcelDTO::getSkuNo).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = FeignQuery.create(ProductDetailEntity.class).in(ProductDetailEntity::getSkuNo, skuNoList).list();
        List<String> skuIdList = productDetailList.stream().map(ProductDetailEntity::getId).collect(Collectors.toList());

        //根据平台、店铺、skuId查询补货建议数据
        List<ReplenishmentSuggestionEntity> replenishmentSuggestionList = replenishmentSuggestionService.listByUnique(platformCodeList, shopIdList, skuIdList);


        //销量数据
        List<String> refIdList = replenishmentSuggestionList.stream().map(ReplenishmentSuggestionEntity::getId).distinct().collect(Collectors.toList());
        List<CfgRuleSalesQtyEntity> cfgRuleSalesQtyList = cfgRuleSalesQtyService.listByRefIdList(refIdList);

        //记录错误数据
        List<SalesDenoisingImportExcelDTO>  wrongList = new ArrayList<>();
        for (SalesDenoisingImportExcelDTO excelDTO : successList) {
            List<String> errorMsgList = new ArrayList<>();
            //平台信息
            String platformCode = platformViewList.stream().filter(obj -> CharSequenceUtil.equals(obj.getName(), excelDTO.getPlatform())).map(DictBasicDTO.ViewDTO::getValue).findFirst().orElse("");
            //店铺信息
            String shopId = shopInfoList.stream().filter(obj -> CharSequenceUtil.equals(obj.getName(), excelDTO.getShopName()) && CharSequenceUtil.equals(obj.getDictPlatform(), platformCode)).map(ShopInfoEntity::getId).findFirst().orElse("");
            //SKU
            String skuId = productDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuNo(), excelDTO.getSkuNo())).map(ProductDetailEntity::getId).findFirst().orElse("");

            //补货建议主表信息
            ReplenishmentSuggestionEntity entity = replenishmentSuggestionList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), skuId) && CharSequenceUtil.equals(obj.getShopId(), shopId) && CharSequenceUtil.equals(platformCode, obj.getPlatform())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(entity)) {
                errorMsgList.add(CharSequenceUtil.format("平台【{}】、店铺【{}】、SKU【{}】未找到对应的补货建议数据",excelDTO.getPlatform(),excelDTO.getShopName(),excelDTO.getSkuNo()));
            }

            //销量数据
            String suggestId = ObjectUtils.isEmpty(entity) ? "" : entity.getId();
            CfgRuleSalesQtyEntity salesQtyEntity = cfgRuleSalesQtyList.stream().filter(obj -> CharSequenceUtil.equals(obj.getRefId(), suggestId)).findFirst().orElse(new CfgRuleSalesQtyEntity());
            if (ObjectUtil.isEmpty(salesQtyEntity)) {
                errorMsgList.add(CharSequenceUtil.format(SALE_ERROR_MSG,excelDTO.getPlatform(),excelDTO.getShopName(),excelDTO.getSkuNo()));
            }

            //添加验证信息
            checkSalesDenoising(excelDTO,errorMsgList);

            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                wrongList.add(excelDTO);
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                continue;
            }
            List<CfgRuleSalesDenoisingDTO.UpdateDTO> updateDTOList = formatSalesDenoising(excelDTO);
            try {
                cfgRuleSalesDenoisingService.update(updateDTOList,salesQtyEntity.getId(),Boolean.TRUE);
            } catch (Exception e) {
                errorMsgList.add(e.getMessage());
            }
            //保存里面的验证
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                wrongList.add(excelDTO);
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
            }
        }
        successList.removeAll(wrongList);
    }

    /**
     * 去噪类型验证
     * @author will
     * @date 2024/9/14 17:04
     * @param excelDTO
     * @param errorMsgList
     */
    private void checkSalesDenoising (SalesDenoisingImportExcelDTO excelDTO,List<String> errorMsgList) {
        //固定值验证
        if (CharSequenceUtil.equals(CfgRuleSalesDenoisingDenoisingTypeEnum.FIXED_VALUE.getName(),excelDTO.getDenoisingTypeName())
                && ObjectUtil.isEmpty(excelDTO.getFixedValue())) {
            errorMsgList.add("去噪类型为固定值去噪时固定值不能为空");
        }
        //百分比验证
        if (CharSequenceUtil.equals(CfgRuleSalesDenoisingDenoisingTypeEnum.PERCENTAGE.getName(),excelDTO.getDenoisingTypeName())
                && ObjectUtil.isEmpty(excelDTO.getPercentageValue())) {
            errorMsgList.add("去噪类型为百分比去噪时百分比去噪不能为空");
        }
    }

    /**
     * 销量去噪
     * @author will
     * @date 2024/9/5 9:27
     * @param excelDTO
     * @return List<UpdateDTO>
     */
    private List<CfgRuleSalesDenoisingDTO.UpdateDTO>  formatSalesDenoising (SalesDenoisingImportExcelDTO excelDTO) {
        CfgRuleSalesDenoisingDTO.UpdateDTO updateDTO = new CfgRuleSalesDenoisingDTO.UpdateDTO();
        updateDTO.setName(excelDTO.getName());
        //时间
        LocalDate startDate = LocalDateUtil.parseStrToLocalDate(excelDTO.getStartDateStr());
        LocalDate endDate = LocalDateUtil.parseStrToLocalDate(excelDTO.getEndDateStr());
        updateDTO.setDateList(Arrays.asList(startDate,endDate));
        //销量去噪值
        updateDTO.setDenoisingType(CfgRuleSalesDenoisingDenoisingTypeEnum.getCode(excelDTO.getDenoisingTypeName()));
        if (CharSequenceUtil.equals(updateDTO.getDenoisingType(),CfgRuleSalesDenoisingDenoisingTypeEnum.PERCENTAGE.getCode())) {
            updateDTO.setEffectiveValue(Integer.valueOf(excelDTO.getPercentageValue()));
        }
        if (CharSequenceUtil.equals(updateDTO.getDenoisingType(),CfgRuleSalesDenoisingDenoisingTypeEnum.FIXED_VALUE.getCode())) {
            updateDTO.setEffectiveValue(Integer.valueOf(excelDTO.getFixedValue()));
        }
        return Arrays.asList(updateDTO);
    }


    @Override
    public void downloadSalesEstimateTemplate(HttpServletResponse response) {
        String excelName = "salesEstimateTemplate.xlsx";
        LinkedList<String> headerNameList = new LinkedList<>(getImportHeader().keySet());
        ExcelUtil.downloadDynamicTemplate(headerNameList, excelName, response);
    }

    /**
     * 运营月销预估表头
     * @author will
     * @date 2024/9/5 10:13
     * @return JSONObject
     */
    private  JSONObject getImportHeader () {
        String dateFormat = "yyyy年MM月";
        LocalDate now = LocalDate.now();
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("*平台","platform");
        jsonObject.set("*SKU","skuNo");
        jsonObject.set("*店铺","shopName");
        jsonObject.set(now.format(DateTimeFormatter.ofPattern(dateFormat)),"currentMonthSalesQty");
        jsonObject.set(now.plusMonths(1L).format(DateTimeFormatter.ofPattern(dateFormat)),"nextMonthSales");
        jsonObject.set(now.plusMonths(2L).format(DateTimeFormatter.ofPattern(dateFormat)),"followingMonthSales");
        return jsonObject;
    }

    @Override
    public void importSalesEstimate(MultipartFile excelFile,String platformType, HttpServletResponse response) {
        SalesEstimateExcelListener excelListenerUtil = new SalesEstimateExcelListener();

        try {
            EasyExcelFactory.read(excelFile.getInputStream(), excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error(ApiError.ERROR_95124.msg, e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error(ApiError.ERROR_1016.msg, e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<JSONObject> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        //导入数据处理
        List<JSONObject> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<JSONObject> errorList = excelListenerUtil.getErrorList();
        //表头
        List<String> headList = excelListenerUtil.getHeadList();

        //处理校验导入成功数据
        handleImportSalesEstimate(successList, errorList,headList,platformType);
        //导入文件名称
        String originalFilename = excelFile.getOriginalFilename();
        //上传正确数据
        upLoadSuccessExcel(originalFilename,successList,headList,platformType);

        //导出错误数据
        exportErrorExcel (errorList,headList,response);
    }

    /**
     * 导出错误数据
     * @author will
     * @date 2024/9/5 18:40
     * @param errorList
     * @param headList
     * @param response
     */
    private void exportErrorExcel (List<JSONObject> errorList,List<String> headList, HttpServletResponse response) {
        if (CollectionUtils.isEmpty(errorList)) {
            return;
        }
        String fileName = "运营月销预估";
        ExcelUtil.customExportUtil(headList,errorList,fileName, response);
    }

    /**
     * 上传成功excel
     * @author will
     * @date 2024/9/5 18:51
     * @param successList
     * @param headList
     */
    private void upLoadSuccessExcel (String originalFilename,List<JSONObject> successList,List<String> headList,String platformType) {
        //全部为空则无需处理
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        String fileName = CharSequenceUtil.isBlank(originalFilename) ? "运营月销预估.xlsx" : originalFilename;
        FileExcelDTO.ExportFileDTO exportFileDTO = new FileExcelDTO.ExportFileDTO();
        exportFileDTO.setFileName(fileName);
        List<List<Object>> exportList = successList.stream().map(obj -> checkToList(obj.values())).collect(Collectors.toList());
        headList.remove("错误信息");
        exportFileDTO.setCustomSheet(new FileExcelDTO.ExportFileSheetDTO("运营月销预估",exportList,null,headList));

        //添加导入记录
        HistoryImportRecordDTO.AddDTO dto = new HistoryImportRecordDTO.AddDTO();
        dto.setName(fileName);
        dto.setModule(SourceTypeEnum.REPLENISHMENT_SUGGESTION.getCode());
        dto.setType(HistoryImportRecordTypeEnum.SALES_ESTIMATE_MANUAL.getCode());
        dto.setExportFileDTO(exportFileDTO);
        dto.setPlatformType(platformType);
        historyImportRecordService.add(dto);
    }

    private List<Object> checkToList(Collection<Object> values) {
        return values.stream().map(e -> e instanceof JSONNull ? null : e).collect(Collectors.toList());
    }

    /**
     * 处理校销量预估导入成功数据
     * @author will
     * @date 2024/8/30 17:01
     * @param successList
     * @param errorList
     */
    private void handleImportSalesEstimate (List<JSONObject> successList, List<JSONObject> errorList,List<String> headList,String platformType) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        //表头
        JSONObject headMap = getImportHeader();
        //平台
        List<String> platformList = successList.stream().filter(obj -> ObjectUtil.isNotEmpty(obj.get("0"))).map(obj -> obj.get("0").toString()).collect(Collectors.toList());
        List<DictBasicDTO.ViewDTO> platformViewList = customerFeign.getDictBasicByKey(DictBasicTypeEnum.SALES_PLATFORM.getType());
        List<String> platformCodeList = platformViewList.stream().filter(obj -> platformList.contains(obj.getName())).map(DictBasicDTO.ViewDTO::getValue).collect(Collectors.toList());

        //SKU
        List<String> skuNoList = successList.stream().filter(obj -> ObjectUtil.isNotEmpty(obj.get("1"))).map(obj -> obj.get("1").toString()).collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = FeignQuery.create(ProductDetailEntity.class).in(ProductDetailEntity::getSkuNo, skuNoList).list();
        List<String> skuIdList = productDetailList.stream().map(ProductDetailEntity::getId).collect(Collectors.toList());

        //店铺
        List<String> shopNameList = successList.stream().filter(obj -> ObjectUtil.isNotEmpty(obj.get("2"))).map(obj -> obj.get("2").toString()).collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = FeignQuery.create(ShopInfoEntity.class).in(ShopInfoEntity::getName, shopNameList).list();
        List<String> shopIdList = shopInfoList.stream().map(ShopInfoEntity::getId).collect(Collectors.toList());


        //根据平台、店铺、skuId查询补货建议数据
        List<ReplenishmentSuggestionEntity> replenishmentSuggestionList = replenishmentSuggestionService.listByUnique(platformCodeList, shopIdList, skuIdList);

        //错误信息序号
        Integer errorIndex = getMapKey(headMap);

        if (ObjectUtil.isEmpty(errorIndex)) {
            throw new ServiceException("导入模板错误");
        }
        //记录错误数据
        List<JSONObject>  wrongList = new ArrayList<>();
        for (JSONObject jsonObject : successList) {
            //主数据
            JSONObject successJson = new JSONObject();
            //错误信息
            List<String> errorMsgList = new ArrayList<>();
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                //字段名称
                String field = headList.get(Integer.parseInt(entry.getKey()));
                //字段编码
                String fieldCode = ObjectUtil.isEmpty(headMap.get(field)) ? "" : headMap.get(field).toString();
                //json数据
                successJson.set(fieldCode,entry.getValue());
            }
            SalesEstimateImportExcelDTO excelDTO = BeanUtil.toBean(successJson, SalesEstimateImportExcelDTO.class);
            //基础验证
            List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
            if (CollectionUtils.isNotEmpty(msgList)) {
                errorMsgList.addAll(msgList);
            }

            //平台信息
            String platformCode = platformViewList.stream().filter(obj -> CharSequenceUtil.equals(obj.getName(), excelDTO.getPlatform())).map(DictBasicDTO.ViewDTO::getValue).findFirst().orElse("");
            //店铺信息
            String shopId = shopInfoList.stream().filter(obj -> CharSequenceUtil.equals(obj.getName(), excelDTO.getShopName()) && CharSequenceUtil.equals(obj.getDictPlatform(), platformCode)).map(ShopInfoEntity::getId).findFirst().orElse("");
            //SKU
            String skuId = productDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuNo(), excelDTO.getSkuNo())).map(ProductDetailEntity::getId).findFirst().orElse("");

            //补货建议主表信息
            ReplenishmentSuggestionEntity entity = replenishmentSuggestionList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), skuId) && CharSequenceUtil.equals(obj.getShopId(), shopId) && CharSequenceUtil.equals(platformCode, obj.getPlatform())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(entity)) {
                errorMsgList.add(CharSequenceUtil.format("平台【{}】、店铺【{}】、SKU【{}】未找到对应的补货建议数据",excelDTO.getPlatform(),excelDTO.getShopName(),excelDTO.getSkuNo()));
            } else {
                if (!CharSequenceUtil.equals(entity.getPlatformType(),platformType)) {
                    errorMsgList.add(CharSequenceUtil.format("【{}】平台不支持导入其他平台数据",CfgRulePlatformTypeEnum.getName(platformType)));
                }
            }

            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                wrongList.add(jsonObject);
                jsonObject.set(String.valueOf(errorIndex),FieldValidUtil.getMsgSort(errorMsgList));
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(jsonObject);
                continue;
            }
            entity = Optional.ofNullable(entity)
                    .orElse(new ReplenishmentSuggestionEntity());
            SalesEstimateManualDTO.UpdateDTO salesEstimateManualList = formatSalesEstimateManual(excelDTO, entity);
            try {
                salesEstimateManualService.update(salesEstimateManualList,entity.getId());
            } catch (Exception e) {
                errorMsgList.add(e.getMessage());
            }
            //保存里面的验证
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                wrongList.add(jsonObject);
                jsonObject.set(String.valueOf(errorIndex),FieldValidUtil.getMsgSort(errorMsgList));
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(jsonObject);
            }
        }
        successList.removeAll(wrongList);
    }

    /**
     * @param headMap
     * @return String
     * @description: 根据value获取对应的key
     * @author Will
     * @date: 2024/5/11 11:41
     */
    private Integer getMapKey (JSONObject headMap) {
        Integer resultKey = null;
        for (Object key : headMap.keySet()) {
            // 获取对应的value
            Object value = headMap.get(key);

            // 如果value等于目标值，输出对应的key
            if (value.equals("错误信息")) {
                resultKey = Integer.valueOf(key.toString());
                // 如果只需要找到一个匹配的key，可以break
                break;
            }
        }
        return  resultKey;
    }

    /**
     * 格式化运营月销预估
     * @author will
     * @date 2024/9/5 18:51
     * @param excelDTO
     * @param entity
     * @return UpdateDTO
     */
    private SalesEstimateManualDTO.UpdateDTO formatSalesEstimateManual (SalesEstimateImportExcelDTO excelDTO,ReplenishmentSuggestionEntity entity ) {
        SalesEstimateManualDTO.UpdateDTO updateDTO = new SalesEstimateManualDTO.UpdateDTO();
        updateDTO.setReplenishmentId(entity.getId());
        updateDTO.setCurrentMonthSalesQty(MathUtil.valueOf(excelDTO.getCurrentMonthSalesQty()));
        updateDTO.setNextMonthSales(MathUtil.valueOf(excelDTO.getNextMonthSales()));
        updateDTO.setFollowingMonthSales(MathUtil.valueOf(excelDTO.getFollowingMonthSales()));
        return updateDTO;
    }
}
