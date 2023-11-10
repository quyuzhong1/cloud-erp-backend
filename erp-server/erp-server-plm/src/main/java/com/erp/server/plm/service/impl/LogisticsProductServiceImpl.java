package com.erp.server.plm.service.impl;

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
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.excel.LogisticsProductExcelDTO;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.plm.dto.ProductCustomsDTO;
import com.erp.model.plm.dto.excel.BomInfoExcelDTO;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.CombinationDeclareTypeEnums;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.model.plm.enums.SaleStateEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.dto.DictGlobalAreaDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
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
        //含税成本
        String actualTaxCost = productBaseInfo.getActualTaxCost();
        productBaseInfo.setActualTaxCost(cny.concat(actualTaxCost));

        //不含税成本
        String actualNoTaxCost = productBaseInfo.getActualNoTaxCost();
        productBaseInfo.setActualNoTaxCost(cny.concat(actualNoTaxCost));

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
        if (Objects.nonNull(productLogistics)) {
            BeanMapper.copy(productLogistics, declareInfo);
        }
        result.setDeclareInfo(declareInfo);


        List<ProductCustomsEntity> productCustomsList = productCustomsService.listBySkuId(skuId);
        List<ProductCustomsDTO.ViewDTO> customsList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(productCustomsList)) {
            customsList = BeanMapper.copyList(productCustomsList, ProductCustomsDTO.ViewDTO.class);
            BigDecimal flag = MathUtil.BigDecimal_100;
            for (ProductCustomsDTO.ViewDTO item : customsList) {
                BigDecimal taxRate = item.getTaxRate();
                taxRate = MathUtil.multiply(taxRate, flag);
                item.setTaxRate(taxRate);
            }
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
            String excelPath = "excel/productLogisticsError.xlsx";
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
                    logistics.setDestDeclarePrice(new BigDecimal(declarePriceStr));
                    logistics.setDestCurrencySymbol(usd.getCurrencySymbol());
                    logistics.setDestCurrency(usd.getCurrencyCode());

                }


                List<String> errorMsgList = new ArrayList<>();
                if (StringUtils.isBlank(skuId)) {
                    errorMsgList.add("sku不存在");
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
                if (StringUtils.isBlank(combinationDeclareType)) {
                    errorMsgList.add("组合品申报不存在");
                }

                ProductCustomsEntity customs = new ProductCustomsEntity();
                customs.setSkuId(skuId);
                customs.setCustomsCode(item.getCustomsCode());
                customs.setCountryName(countryName);
                customs.setCountry(country);
                customs.setSkuNo(item.getSkuNo());
                String taxRateStr = item.getTaxRate();
                if (StringUtils.isNotBlank(taxRateStr)) {
                    BigDecimal taxRate = new BigDecimal(taxRateStr);
                    taxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
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
        List<String> skuIdList = productCustomsList.stream().map(ProductCustomsEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuList = productDetailService.listByIds(skuIdList);
        List<String> countryIdList = productCustomsList.stream().filter(p -> StringUtils.isNotBlank(p.getCountry())).
                map(ProductCustomsEntity::getCountry).distinct().collect(Collectors.toList());
        List<DictCountryEntity> countryList = CollectionUtils.isNotEmpty(countryIdList) ? sysDictFeign.listCountryByIds(countryIdList) : Collections.emptyList();
        for (ProductCustomsEntity item : productCustomsList) {
            String country = item.getCountry();
            String countryName = countryList.stream().filter(c -> c.getId().equals(country)).map(DictCountryEntity::getNameCn).
                    findFirst().orElse("");
            item.setCountryName(countryName);
            String skuNo = skuList.stream().filter(s -> s.getId().equals(item.getSkuId())).
                    map(ProductDetailEntity::getSkuNo).findFirst().orElse("");
            item.setSkuNo(skuNo);
            BigDecimal taxRate = item.getTaxRate();
            taxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
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
        for (LogisticsProductDTO.ExportInfoDTO item : list) {
            Integer salesStatus = item.getSalesStatus();
            String salesStatusName = SaleStateEnum.getNameByCode(salesStatus);
            item.setSalesStatusName(salesStatusName);
            String combinationDeclareType = item.getCombinationDeclareType();
            item.setCombinationDeclareType(CombinationDeclareTypeEnums.getCode(combinationDeclareType));
        }
    }
}
