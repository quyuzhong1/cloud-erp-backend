package com.erp.server.plm.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MapUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.entity.DmpSkuCostEntity;
import com.erp.model.oms.dto.excel.LogisticsProductExcelDTO;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.plm.dto.ProductCustomsDTO;
import com.erp.model.plm.dto.excel.BomInfoExcelDTO;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.CombinationDeclareTypeEnums;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.model.plm.enums.SaleStateEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.dto.SkuCostDTO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.dto.DictGlobalAreaDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.entity.CfgSettingEntity;
import com.erp.model.tms.enums.CfgSettingEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.CfgSettingFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.plm.constant.ProductConstant;
import com.erp.server.plm.listener.LogisticsProductExcelListener;
import com.erp.server.plm.mapper.ProductDetailMapper;
import com.erp.server.plm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description TODO
 * @Author yl
 * @Date 2023-11-06 12:28
 */
@Slf4j
@Service
public class LogisticsProductServiceImpl extends SuperServiceImpl<ProductDetailMapper, ProductDetailEntity> implements LogisticsProductService {

    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private SysDictFeign sysDictFeign;

    @Resource
    private BomSkuService bomSkuService;

    @Resource
    private ProductLogisticsService productLogisticsService;

    @Resource
    private ProductCustomsService productCustomsService;

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private BasicCategoryService basicCategoryService;

    @Resource
    private ProductPurchaseService productPurchaseService;

    @Resource
    private CfgSettingFeign cfgSettingFeign;

    @Resource
    private BasicDictService basicDictService;




    @Override
    public PagingVO<LogisticsProductDTO.PagingVO> paging(PagingDTO<LogisticsProductDTO.PagingParamDTO> dto) {
        LogisticsProductDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        Integer approvalStatus = ProductDetailStatusEnum.APPROVAL_PASS.getCode();
        IPage pageData = baseMapper.logisticsProductPaging(query, params, approvalStatus);
        List<LogisticsProductDTO.PagingVO> list = pageData.getRecords();
        fillPagingDb(list);
        return new PagingVO<>(pageData);
    }


    @Override
    public LogisticsProductDTO.ViewDTO view(String skuId) {
        LogisticsProductDTO.ViewDTO result = new LogisticsProductDTO.ViewDTO();
        LogisticsProductDTO.ProductBaseInfoDTO productBaseInfo = baseMapper.getProductBaseInfo(skuId);
        String cny = CurrencyEnum.CNY.getCurrencySymbol();
        ProductPurchaseEntity productPurchase = productPurchaseService.getBySkuId(skuId);
        String mainSupplierId = "";
        if (Objects.nonNull(productPurchase)) {
            mainSupplierId = productPurchase.getMainSupplier();
        }
        List<String> supplierIdList = new ArrayList<>(1);
        if (StringUtils.isNotBlank(mainSupplierId)) {
            supplierIdList.add(mainSupplierId);
        }
        //不含税
        BigDecimal actualNoTaxCost = BigDecimal.ZERO;
        //含税
        BigDecimal actualTaxCost = BigDecimal.ZERO;
        String skuNo=productBaseInfo.getSkuNo();
        List<DmpSkuCostEntity>  skuCostList= dmpTaskFeign.listRedisBySkuNoList(Arrays.asList(skuNo));
        DmpSkuCostEntity skuCostDTO = skuCostList.stream().filter(Objects::nonNull).findFirst().orElse(null);
        if (Objects.nonNull(skuCostDTO)) {
            actualTaxCost=skuCostDTO.getCostPrice();
            actualNoTaxCost=skuCostDTO.getNotTaxCostPrice();
        } else {
            //含税成本
            String actualTaxCostStr = productBaseInfo.getActualTaxCost();
            if (StringUtils.isNotBlank(actualTaxCostStr)) {
                actualTaxCost = new BigDecimal(actualTaxCostStr);
            }
            //不含税成本
            String actualNoTaxCostStr = productBaseInfo.getActualNoTaxCost();
            if (StringUtils.isNotBlank(actualNoTaxCostStr)) {
                actualNoTaxCost = new BigDecimal(actualNoTaxCostStr);
            }
        }
        productBaseInfo.setActualTaxCost(cny.concat(actualTaxCost.toString()));
        productBaseInfo.setActualNoTaxCost(cny.concat(actualNoTaxCost.toString()));
        List<String> skuNoList = Arrays.asList(productBaseInfo.getSkuNo());
        List<BomInfoEntity> bomSkuList = bomSkuService.listAllBomByParentSkuNos(skuNoList);
        productBaseInfo.setIsCombination(CollectionUtils.isNotEmpty(bomSkuList));

        Integer salesStatus = productBaseInfo.getSalesStatus();
        String salesStatusName = SaleStateEnum.getNameByCode(salesStatus);
        productBaseInfo.setSalesStatusName(salesStatusName);
        String categoryId = productBaseInfo.getCategoryId();
        List<String> categoryIdList = basicCategoryService.getPidList(categoryId);
        List<BasicCategoryEntity> categoryList = basicCategoryService.listByIds(categoryIdList);
        String categoryName = categoryList.stream().map(BasicCategoryEntity::getName).collect(Collectors.joining("-"));
        productBaseInfo.setCategoryName(categoryName);
        result.setProductBaseInfo(productBaseInfo);
        LogisticsProductDTO.DeclareInfoDTO declareInfo = new LogisticsProductDTO.DeclareInfoDTO();
        ProductLogisticsEntity productLogistics = productLogisticsService.getBySkuId(skuId);

        String usdCode = CurrencyEnum.USD.getCurrencyCode();
        //汇率
        BigDecimal rate;
        if (Objects.nonNull(skuCostDTO) && Objects.nonNull(skuCostDTO.getCostDate())){
            rate = dmpTaskFeign.getRate(skuCostDTO.getCostDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), usdCode);
        }else {
            rate = dmpTaskFeign.getRate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), usdCode);
        }
        result.setExchangeRate(rate);

        String sourceCountryName = "";
//        BigDecimal actualTaxCostUsd = MathUtil.divide(actualTaxCost, rate);
        if (Objects.nonNull(productLogistics)) {
            //目的国申报价
//            BigDecimal destDeclarePrice = productLogistics.getDestDeclarePrice();
//            if (destDeclarePrice.compareTo(BigDecimal.ZERO) == 0) {
//                BigDecimal resultDestDeclarePrice = getDestDeclarePrice(actualTaxCostUsd);
//                productLogistics.setDestDeclarePrice(resultDestDeclarePrice);
//            }
            //原产国
            String sourceCountry = productLogistics.getSourceCountry();
            if (StringUtils.isNotBlank(sourceCountry)) {
                DictCountryEntity countryEntity = sysUserFeign.getCountryById(sourceCountry);
                if (Objects.nonNull(countryEntity)) {
                    sourceCountryName = countryEntity.getNameCn();
                }
            }
            BeanMapper.copy(productLogistics, declareInfo);
        }
        //这个是属性id 可能多个逗号分割
        String propertyId = productLogistics.getProductPropertyId();
        String propertyName = "";
        if (StringUtils.isNotBlank(propertyId)) {
            List<String> dictIdList = Arrays.asList(propertyId.split(","));
            List<BasicDictEntity> dictList = basicDictService.listByIds(dictIdList);
            propertyName = dictList.stream().map(BasicDictEntity::getName).collect(Collectors.joining(","));
            //增加是否存在电池属性
            BasicDictEntity isElectric = dictList.stream().filter(e -> e.getRemark().equals("isElectric")).findFirst().orElse(null);
            if (Objects.nonNull(isElectric)){
                productBaseInfo.setElectric(true);
                productBaseInfo.setInputParams(String.format("电压:%s%s/电流:%s%s/功率:%s%s/电池容量:%s%s",
                        productLogistics.getInputVoltage().stripTrailingZeros().toPlainString(),productLogistics.getVoltageUnit(),
                        productLogistics.getInputElectric().stripTrailingZeros().toPlainString(),productLogistics.getElectricUnit(),
                        productLogistics.getInputPower().stripTrailingZeros().toPlainString(),productLogistics.getPowerUnit(),
                        productLogistics.getInputBatteryCapacity().stripTrailingZeros().toPlainString(),productLogistics.getBatteryCapacityUnit()));
                productBaseInfo.setOutputParams(String.format("电压:%s%s/电流:%s%s/功率:%s%s/电池容量:%s%s",
                        productLogistics.getOutputVoltage().stripTrailingZeros().toPlainString(),productLogistics.getVoltageUnit(),
                        productLogistics.getOutputElectric().stripTrailingZeros().toPlainString(),productLogistics.getElectricUnit(),
                        productLogistics.getOutputPower().stripTrailingZeros().toPlainString(),productLogistics.getPowerUnit(),
                        productLogistics.getOutputBatteryCapacity().stripTrailingZeros().toPlainString(),productLogistics.getBatteryCapacityUnit()));
            }
        }
        String combinationDeclareType = declareInfo.getCombinationDeclareType();
        if(StringUtils.isBlank(combinationDeclareType)){
            declareInfo.setCombinationDeclareType(CombinationDeclareTypeEnums.SPLIT.getCode());
        }
        //物流属性
        productBaseInfo.setLogisticsPropertyName(propertyName);
        declareInfo.setSourceCountryName(sourceCountryName);
        //报关申报价币种
        String declareCurrency=declareInfo.getDeclareCurrency();
        //目的国申报价币种
        String destCurrency=declareInfo.getDestCurrency();
        if(StringUtils.isBlank(declareCurrency)){
            declareInfo.setDeclareCurrency(CurrencyEnum.USD.getCurrencyCode());
            declareInfo.setDeclareCurrencySymbol(CurrencyEnum.USD.getCurrencySymbol());
        }
        if(StringUtils.isBlank(destCurrency)){
            declareInfo.setDestCurrency(CurrencyEnum.USD.getCurrencyCode());
            declareInfo.setDestCurrencySymbol(CurrencyEnum.USD.getCurrencySymbol());
        }
        result.setDeclareInfo(declareInfo);
        List<ProductCustomsEntity> productCustomsList = productCustomsService.listBySkuId(skuId);
        List<ProductCustomsDTO.ViewDTO> customsList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(productCustomsList)) {
            customsList = BeanMapper.copyList(productCustomsList, ProductCustomsDTO.ViewDTO.class);
        }
        result.setCustomsList(customsList);
        return result;
    }

    /**
     * 编辑信息
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-11-08 8:37
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(LogisticsProductDTO.UpdateDTO dto) {
        //报关信息
        LogisticsProductDTO.DeclareInfoDTO declareInfo = dto.getDeclareInfo();
        ProductLogisticsEntity productLogistics = new ProductLogisticsEntity();
        BeanMapper.copy(declareInfo, productLogistics);
        handleProductLogistics(productLogistics);

        List<ProductCustomsDTO.ViewDTO> customsList = dto.getCustomsList();
        List<ProductCustomsEntity> productCustomsList = BeanMapper.copyList(customsList, ProductCustomsEntity.class);
        List<String> deleteIdList = handleCustoms(productCustomsList);
        Boolean logisticsResult = productLogisticsService.saveOrUpdate(productLogistics);
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            productCustomsService.removeByIds(deleteIdList);
        }
        Boolean customsResult = productCustomsService.saveOrUpdateBatch(productCustomsList);
        return logisticsResult && customsResult;
    }


    @Override
    public Boolean exportExcel(LogisticsProductDTO.ExportDTO dto, HttpServletResponse response) {
        Integer approvalStatus = ProductDetailStatusEnum.APPROVAL_PASS.getCode();
        List<LogisticsProductDTO.ExportInfoDTO> list = baseMapper.listExport(dto, approvalStatus);
        fillExport(list);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/productLogistics.xlsx";
        String name = "物流产品列表";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("物流产品导出出错 {}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }


    @Override
    public Boolean importExcel(MultipartFile excelFile, HttpServletResponse response) {
        LogisticsProductExcelListener excelListenerUtil = new LogisticsProductExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), LogisticsProductExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入物流场频错误！{}", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！{}", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        List<LogisticsProductExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        List<LogisticsProductExcelDTO> errorList = excelListenerUtil.getErrorList();
        List<LogisticsProductExcelDTO> successList = excelListenerUtil.getSuccessList();
        //处理验证成功数据
        handleImportSuccessList(successList, errorList);

        if (errorList.size() > 0) {
            StringBuffer sb = new StringBuffer();
            String excelPath = "excel/logisticsProductError.xlsx";
            String name = "productLogistics";
            String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            sb.append(date);
            sb.append(name);
            try {
                new ExcelPrintUtils().patchExport(errorList, response, sb.toString(), excelPath);
            } catch (IOException e) {
                throw new ServiceException(ApiError.ERROR_95125);
            }
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public List<LogisticsProductDTO.TabListDTO> tabList(PermissionsDTO dto) {
        LogisticsProductDTO.TabListDTO tab = new LogisticsProductDTO.TabListDTO();
        List<String> fieldList = listField();
        Integer approvalStatus = ProductDetailStatusEnum.APPROVAL_PASS.getCode();
        String permissionSql = dto.getPermissionSql();
        Integer count = baseMapper.logisticsProductUpdateCount(approvalStatus, fieldList, permissionSql);
        tab.setType("update");
        tab.setCount(count);
        return Arrays.asList(tab);
    }


    @Override
    public PagingVO<LogisticsProductDTO.UpdatePagingDTO> updatePaging(PagingDTO<LogisticsProductDTO.UpdatePagingParamDTO> dto) {
        LogisticsProductDTO.UpdatePagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        Integer approvalStatus = ProductDetailStatusEnum.APPROVAL_PASS.getCode();
        List<String> fieldList = listField();
        IPage pageData = baseMapper.logisticsProductUpdatePaging(query, params, approvalStatus, fieldList);
        return new PagingVO<>(pageData);
    }

    @Override
    public List<LogisticsProductDTO.ProductDTO> listLogisticsProduct(List<String> skuIdList) {
        if (CollectionUtils.isEmpty(skuIdList)) {
            return Collections.emptyList();
        }
        List<LogisticsProductDTO.ProductDTO> list = baseMapper.listLogisticsProduct(skuIdList);
        String isElectricFlag = ProductConstant.IS_ELECTRIC;
        //属性
        List<String> propertyIdList = list.stream().map(LogisticsProductDTO.ProductDTO::getProductPropertyId).distinct().collect(Collectors.toList());
        List<BasicDictEntity> dictList = CollectionUtils.isNotEmpty(propertyIdList) ? basicDictService.listByIds(propertyIdList) : Collections.emptyList();
        for (LogisticsProductDTO.ProductDTO item : list) {
            //毛重
            BigDecimal grossWeight = item.getGrossWeight();
            Integer weight = 0;
            if (Objects.nonNull(grossWeight)) {
                weight = grossWeight.intValue();
            }
            item.setWeight(weight);
            String propertyId = item.getProductPropertyId();
            String flag = dictList.stream().filter(d -> d.getId().equals(propertyId)).findFirst().map(BasicDictEntity::getRemark).orElse("");
            item.setIsElectric(isElectricFlag.equals(flag));
        }
        return list;
    }

    /**
     * 根据dmp计算含税成本 重算物流目的国申报单价
     * @param dmpSkuCostEntityList
     */
    @Override
    public void recalDestDeclarePrice(List<DmpSkuCostEntity> dmpSkuCostEntityList) {
        if (CollectionUtils.isEmpty(dmpSkuCostEntityList)){
            return;
        }
        //系统比例配置
        CfgSettingEntity setting = cfgSettingFeign.getByKey(CfgSettingEnum.LOGISTICS_PRODUCT_DEST_DECLARE_PRICE.getCode());
        if (Objects.isNull(setting) || Objects.isNull(setting.getDataJson()) || CollectionUtils.isEmpty(setting.getDataJson().getJSONArray("data"))){
            return;
        }
        List<CfgSettingValueDTO.LogisticsProductDestDeclarePrice> settings = JSONUtil.toList(setting.getDataJson().getJSONArray("data"), CfgSettingValueDTO.LogisticsProductDestDeclarePrice.class);
        for (DmpSkuCostEntity dmpSkuCostEntity : dmpSkuCostEntityList) {
            if (Objects.isNull(dmpSkuCostEntity) || StringUtils.isEmpty(dmpSkuCostEntity.getSkuId())){
                continue;
            }
            ProductLogisticsEntity productLogistics = productLogisticsService.getBySkuId(dmpSkuCostEntity.getSkuId());
            if (Objects.isNull(productLogistics)) {
                continue;
            }
            BigDecimal destDeclarePrice = productLogistics.getDestDeclarePrice();
            if (Objects.isNull(destDeclarePrice) || destDeclarePrice.compareTo(BigDecimal.ZERO) == 0) {
                //汇率
                BigDecimal exchangeRate = BigDecimal.ONE;
                if (StrUtil.isNotBlank(dmpSkuCostEntity.getCurrency()) && !CurrencyEnum.CNY.getCurrencyCode().equals(dmpSkuCostEntity.getCurrency())) {
                    exchangeRate = dmpTaskFeign.getRate(dmpSkuCostEntity.getCostDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), dmpSkuCostEntity.getCurrency());
                }
                if (Objects.isNull(exchangeRate)){
                    continue;
                }
                BigDecimal actualTaxCostUsd = MathUtil.multiply(dmpSkuCostEntity.getCostPrice(), exchangeRate);
                CfgSettingValueDTO.LogisticsProductDestDeclarePrice declarePrice = settings.stream().filter(e -> e.getStartPrice().compareTo(actualTaxCostUsd) < 0 && e.getEndPrice().compareTo(actualTaxCostUsd) >= 0).findFirst().orElse(null);
                if (Objects.isNull(declarePrice) || Objects.isNull(declarePrice.getRate())){
                    continue;
                }
                BigDecimal resultDestDeclarePrice = actualTaxCostUsd.multiply(declarePrice.getRate()).divide(MathUtil.BigDecimal_100, 4, RoundingMode.HALF_UP);
                //统一换算成美元汇率
                BigDecimal usdRate = dmpTaskFeign.getRate(dmpSkuCostEntity.getCostDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), CurrencyEnum.USD.getCurrencyCode());
                if (Objects.isNull(usdRate)){
                    continue;
                }
                productLogistics.setDestDeclarePrice(MathUtil.divide(resultDestDeclarePrice, usdRate));
                productLogistics.setDestCurrency(CurrencyEnum.USD.getCurrencyCode());
                productLogistics.setDestCurrencySymbol(CurrencyEnum.USD.getCurrencySymbol());
                productLogisticsService.updateById(productLogistics);
            }
        }

    }


    private List<String> listField() {
        List<String> fieldList = new ArrayList<>(10);
        fieldList.add("产品经理");
        fieldList.add("产品类别");
        fieldList.add("产品属性");
        fieldList.add("产品品牌");
        fieldList.add("产品用途");
        fieldList.add("主要材质");
        fieldList.add("实际含税成本");
        fieldList.add("目标不含税成本");
        fieldList.add("EAN码");
        fieldList.add("产品尺寸");
        fieldList.add("毛重");
        fieldList.add("净重");
        return fieldList;
    }

    private void handleImportSuccessList(List<LogisticsProductExcelDTO> successList, List<LogisticsProductExcelDTO> errorList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        CurrencyEnum usd = CurrencyEnum.USD;
        //sku no list
        List<String> skuNoList = successList.stream().map(LogisticsProductExcelDTO::getSkuNo).distinct().collect(Collectors.toList());
        //国家
        List<String> countryNameList = successList.stream().filter(c -> StringUtils.isNotBlank(c.getCountry())).
                map(LogisticsProductExcelDTO::getCountry).distinct().collect(Collectors.toList());
        List<DictCountryEntity> countryList = CollectionUtils.isNotEmpty(countryNameList) ? sysDictFeign.listCountryByNames(countryNameList) : Collections.emptyList();

        List<SkuVO> skuList = productDetailService.getSkuBySkuNos(skuNoList);
        List<String> skuIdList = skuList.stream().map(SkuVO::getSkuId).collect(Collectors.toList());
        List<ProductLogisticsEntity> productLogisticsList = productLogisticsService.listBySkuIdList(skuIdList);
        Map<String, List<LogisticsProductExcelDTO>> map = successList.stream().collect(Collectors.groupingBy(LogisticsProductExcelDTO::getSkuNo));
        for (Map.Entry<String, List<LogisticsProductExcelDTO>> entry : map.entrySet()) {
            String skuNo = entry.getKey();
            String skuId = skuList.stream().filter(s -> s.getSkuNo().equals(skuNo)).findFirst().
                    map(SkuVO::getSkuId).orElse("");
            Boolean isError = Boolean.FALSE;
            ProductLogisticsEntity logistics = productLogisticsList.stream().
                    filter(p -> p.getSkuId().equals(skuId)).findFirst().orElse(new ProductLogisticsEntity());
            List<LogisticsProductExcelDTO> value = entry.getValue();
            List<ProductCustomsEntity> customsList = new ArrayList<>(value.size());

            for (LogisticsProductExcelDTO item : value) {
                String id = logistics.getId();
                BeanMapper.copy(item, logistics);
                logistics.setId(id);
                //报关申报价
                String declarePriceStr = item.getDeclarePrice();
                if (StringUtils.isNotBlank(declarePriceStr)) {
                    logistics.setDeclarePrice(new BigDecimal(declarePriceStr));
                    logistics.setDeclareCurrency(usd.getCurrencyCode());
                    logistics.setDeclareCurrencySymbol(usd.getCurrencySymbol());
                }
                //目的国申报价
                String destDeclarePriceStr = item.getDestDeclarePrice();
                if (StringUtils.isNotBlank(destDeclarePriceStr)) {
                    logistics.setDestDeclarePrice(new BigDecimal(destDeclarePriceStr));
                    logistics.setDestCurrencySymbol(usd.getCurrencySymbol());
                    logistics.setDestCurrency(usd.getCurrencyCode());
                }


                List<String> errorMsgList = new ArrayList<>();
                if (StringUtils.isBlank(skuId)) {
                    errorMsgList.add("sku不存在或者sku未审核通过");
                }
                //国家
                String countryName = item.getCountry();
                String country = "";
                if (StringUtils.isNotBlank(countryName)) {
                    country = countryList.stream().filter(s -> s.getNameCn().equals(countryName)).findFirst().
                            map(DictCountryEntity::getId).orElse("");
                    if (StringUtils.isBlank(country)) {
                        errorMsgList.add("国家不存在");
                    }
                }
                String combinationDeclareTypeStr = item.getCombinationDeclareType();
                String combinationDeclareType = CombinationDeclareTypeEnums.getCode(combinationDeclareTypeStr);
                logistics.setCombinationDeclareType(combinationDeclareType);
                if (StringUtils.isBlank(combinationDeclareType)) {
                    errorMsgList.add("组合品申报不存在");
                }

                ProductCustomsEntity customs = new ProductCustomsEntity();
                customs.setSkuId(skuId);
                customs.setCustomsCode(item.getDestCustomsCode());
                customs.setCountryName(countryName);
                customs.setCountry(country);
                customs.setSkuNo(item.getSkuNo());
                String taxRateStr = item.getTaxRate();
                if (StringUtils.isNotBlank(taxRateStr)) {
                    BigDecimal taxRate = new BigDecimal(taxRateStr);
                    customs.setTaxRate(taxRate);
                } else {
                    customs.setTaxRate(BigDecimal.ZERO);
                }
                customsList.add(customs);

                //存在错误信息则
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    isError = Boolean.TRUE;
                    item.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                    break;
                }
            }


            //更新错误数据
            if (isError) {
                errorList.addAll(value);
                continue;
            }

            Boolean logisticsResult = productLogisticsService.saveOrUpdate(logistics);
            productCustomsService.removeBySkuId(Arrays.asList(skuId));
            productCustomsService.saveBatch(customsList);

        }


    }

    private List<String> handleCustoms(List<ProductCustomsEntity> productCustomsList) {
        if (CollectionUtils.isEmpty(productCustomsList)) {
            return Collections.emptyList();
        }
        //sku
        List<String> skuNoList = productCustomsList.stream().map(ProductCustomsEntity::getSkuNo).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuList = productDetailService.listBySkuNos(skuNoList);
        List<String> countryIdList = productCustomsList.stream().filter(p -> StringUtils.isNotBlank(p.getCountry())).
                map(ProductCustomsEntity::getCountry).distinct().collect(Collectors.toList());
        List<DictCountryEntity> countryList = CollectionUtils.isNotEmpty(countryIdList) ? sysDictFeign.listCountryByIds(countryIdList) : Collections.emptyList();
        for (ProductCustomsEntity item : productCustomsList) {
            String country = item.getCountry();
            String countryName = countryList.stream().filter(c -> c.getId().equals(country)).map(DictCountryEntity::getNameCn).
                    findFirst().orElse("");
            item.setCountryName(countryName);
            String skuId = skuList.stream().filter(s -> s.getSkuNo().equals(item.getSkuNo())).
                    map(ProductDetailEntity::getId).findFirst().orElse("");
            item.setSkuId(skuId);
            BigDecimal taxRate = item.getTaxRate();
            item.setTaxRate(taxRate);
        }
        List<String> updateIdList = productCustomsList.stream().filter(c -> StringUtils.isNotEmpty(c.getId())).
                map(ProductCustomsEntity::getId).collect(Collectors.toList());
        List<ProductCustomsEntity> dbList = productCustomsService.listBySkuId(productCustomsList.get(0).getSkuId());
        return dbList.stream().filter(c -> !updateIdList.contains(c.getId())).map(ProductCustomsEntity::getId).collect(Collectors.toList());

    }

    /**
     * 处理物流产品数据
     *
     * @param productLogistics
     */
    private void handleProductLogistics(ProductLogisticsEntity productLogistics) {
        if (Objects.isNull(productLogistics)) {
            return;
        }
        //报关币种
        String declareCurrency = productLogistics.getDeclareCurrency();
        //目的国币种
        String destCurrency = productLogistics.getDestCurrency();

        List<String> currencyCodeList = Arrays.asList(declareCurrency, destCurrency);
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyCodeList);
        String declareCurrencySymbol = currencyList.stream().filter(c -> c.getId().equals(declareCurrency)).findFirst().
                map(CurrencyDTO.ViewDTO::getSymbol).orElse("");
        productLogistics.setDeclareCurrencySymbol(declareCurrencySymbol);

        String destCurrencySymbol = currencyList.stream().filter(c -> c.getId().equals(destCurrency)).findFirst().
                map(CurrencyDTO.ViewDTO::getSymbol).orElse("");
        productLogistics.setDestCurrencySymbol(destCurrencySymbol);
    }

    /**
     * 填充分页数据
     *
     * @param list
     * @return void
     * @author yl
     * @date 2023-11-06 17:33
     */
    private void fillPagingDb(List<LogisticsProductDTO.PagingVO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        List<String> skuNoList = list.stream().map(LogisticsProductDTO.PagingVO::getSkuNo).collect(Collectors.toList());
        List<BomInfoEntity> bomSkuList = bomSkuService.listAllBomByParentSkuNos(skuNoList);
        for (LogisticsProductDTO.PagingVO item : list) {
            String skuNo = item.getSkuNo();
            Integer salesStatus = item.getSalesStatus();
            String salesStatusName = SaleStateEnum.getNameByCode(salesStatus);
            item.setSalesStatusName(salesStatusName);
            Integer approveStatus = item.getApproveStatus();
            String approveStatusName = ProductDetailStatusEnum.getName(approveStatus);
            item.setApproveStatusName(approveStatusName);
            //产品经理
            String chargeId = item.getChargeId();
            List<String> chargeIdList = StringUtils.isNotBlank(chargeId) ? Arrays.asList(chargeId.split(",")) : Collections.emptyList();
            String chargeName = userList.stream().filter(u -> chargeIdList.contains(u.getUserId())).
                    map(FindUserDTO::getUserName).collect(Collectors.joining(","));
            item.setChargeName(chargeName);
            BomInfoEntity bomInfo = bomSkuList.stream().filter(b -> b.getParentSkuNo().equals(skuNo)).
                    findFirst().orElse(null);
            item.setIsCombination(Objects.nonNull(bomInfo));
        }
    }


    private void fillExport(List<LogisticsProductDTO.ExportInfoDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> dictIdList = new ArrayList<>(20);
        List<String> propertyIdList = list.stream().filter(l -> StringUtils.isNotBlank(l.getPropertyId())).
                map(LogisticsProductDTO.ExportInfoDTO::getPropertyId).collect(Collectors.toList());
        for (String propertyId : propertyIdList) {
            for (String dictId : propertyId.split(",")) {
                dictIdList.add(dictId);
            }
        }
        List<String> skuNoList = list.stream().map(LogisticsProductDTO.ExportInfoDTO::getSkuNo).distinct().collect(Collectors.toList());


        dictIdList = dictIdList.stream().distinct().collect(Collectors.toList());
        List<BasicDictEntity> dictList = basicDictService.listByIds(dictIdList);
        BigDecimal zero = BigDecimal.ZERO;

        String usdCode = CurrencyEnum.USD.getCurrencyCode();
        String nowDay = LocalDate.now().toString();
        //汇率
        BigDecimal rate = dmpTaskFeign.getRate(nowDay, usdCode);
        if (Objects.isNull(rate) || rate.compareTo(BigDecimal.ZERO) == 0) {
            rate = new BigDecimal("7.13");
        }

        List<DmpSkuCostEntity>  skuCostList= dmpTaskFeign.listRedisBySkuNoList(skuNoList);

        for (LogisticsProductDTO.ExportInfoDTO item : list) {
            String skuId = item.getSkuId();

            //含税成本
            BigDecimal actualTaxCost = item.getActualTaxCost();
            if (Objects.isNull(actualTaxCost) || zero.compareTo(actualTaxCost) == 0) {
                Map<String, BigDecimal> map = getSkuCost(skuCostList, skuId);
                actualTaxCost = map.get("actualTaxCost");
                item.setActualTaxCost(actualTaxCost);
                item.setActualNoTaxCost(map.get("actualNoTaxCost"));
            }
            BigDecimal actualTaxCostUsd = MathUtil.divide(actualTaxCost, rate);
            //目的国申报价  20240223这里改成审核后重新计算
//            BigDecimal destDeclarePrice = item.getDestDeclarePrice();
//            if (Objects.isNull(destDeclarePrice) || destDeclarePrice.compareTo(BigDecimal.ZERO) == 0) {
//                BigDecimal resultDestDeclarePrice = getDestDeclarePrice(actualTaxCostUsd);
//                item.setDestDeclarePrice(resultDestDeclarePrice);
//            }

            Integer salesStatus = item.getSalesStatus();
            String salesStatusName = SaleStateEnum.getNameByCode(salesStatus);
            item.setSalesStatusName(salesStatusName);
            String combinationDeclareType = item.getCombinationDeclareType();
            String combinationDeclareTypeStr = CombinationDeclareTypeEnums.getName(combinationDeclareType);
            item.setCombinationDeclareType(combinationDeclareTypeStr);
            String propertyId = item.getPropertyId();
            if (StringUtils.isBlank(propertyId)) {
                item.setLogisticsPropertyName("");
            } else {
                List<String> propertyIds = Arrays.asList(propertyId.split(","));
                String propertyName = dictList.stream().filter(d -> propertyIds.contains(d.getId())).map(BasicDictEntity::getName).
                        collect(Collectors.joining(","));
                item.setLogisticsPropertyName(propertyName);
            }


        }
    }


    public Map<String, BigDecimal> getSkuCost(List<DmpSkuCostEntity> skuCostList, String skuId) {
        DmpSkuCostEntity skuCost = skuCostList.stream().filter(req -> req.getSkuId().equals(skuId)).findFirst().orElse(null);

        BigDecimal actualNoTaxCost = BigDecimal.ZERO;
        BigDecimal actualTaxCost = BigDecimal.ZERO;
        if (Objects.nonNull(skuCost)) {
            //不含税价=含税价÷（1+税率）
            actualNoTaxCost = skuCost.getNotTaxCostPrice();
            actualTaxCost = skuCost.getCostPrice();
        }
        Map<String, BigDecimal> map = new HashMap<>();
        map.put("actualTaxCost", actualTaxCost);
        map.put("actualNoTaxCost", actualNoTaxCost);
        return map;
    }

}
