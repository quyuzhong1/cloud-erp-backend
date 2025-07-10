package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.entity.DmpSkuCostEntity;
import com.erp.model.oms.dto.excel.LogisticsProductExcelDTO;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.plm.dto.ProductCustomsDTO;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.*;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.PageListTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.dto.ProductRegistrationDTO;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import com.erp.model.tms.enums.ProductRegistrationEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.CfgSettingFeign;
import com.erp.rpc.tms.feign.ForecastFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.constant.ProductConstant;
import com.erp.server.plm.listener.LogisticsProductExcelListener;
import com.erp.server.plm.mapper.ProductDetailMapper;
import com.erp.server.plm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static cn.hutool.core.text.CharSequenceUtil.*;
import static com.alibaba.excel.EasyExcelFactory.read;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_LOGISTICS_PRODUCT;

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

    @Resource
    private CommonService commonService;


    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private ForecastFeign forecastFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private SysLogService sysLogService;
    private static final String SKUCLASSPATH = String.valueOf(ProductDetailEntity.class);

    @Override
    public PagingVO<LogisticsProductDTO.PagingVO> paging(PagingDTO<LogisticsProductDTO.PagingParamDTO> dto) {
        LogisticsProductDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<LogisticsProductDTO.PagingParamDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        //列表Tab查询状态处理
        Boolean isFlag = doOpHandleTableParam(params);
        if (!isFlag) {
            return new PagingVO<>();
        }
        Integer approvalStatus = ProductDetailStatusEnum.APPROVAL_PASS.getCode();
        IPage<LogisticsProductDTO.PagingVO> pageData = baseMapper.logisticsProductPaging(query, params, approvalStatus);
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
        List<String> categoryIdList = StringUtils.isNotBlank(categoryId) ? basicCategoryService.getPidList(categoryId) : Collections.emptyList();
        List<BasicCategoryEntity> categoryList = CollectionUtils.isNotEmpty(categoryIdList) ? basicCategoryService.listByIds(categoryIdList) : Collections.emptyList();
        String categoryName = categoryList.stream().map(BasicCategoryEntity::getName).collect(Collectors.joining("-"));
        productBaseInfo.setCategoryName(categoryName);
        result.setProductBaseInfo(productBaseInfo);
        LogisticsProductDTO.DeclareInfoDTO declareInfo = new LogisticsProductDTO.DeclareInfoDTO();
        ProductLogisticsEntity productLogistics = productLogisticsService.getBySkuId(skuId);
        if (ObjectUtil.isEmpty(productLogistics)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"产品物流信息");
        }

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
        //原产国
        String sourceCountry = productLogistics.getSourceCountry();
        if (StringUtils.isNotBlank(sourceCountry)) {
            DictCountryEntity countryEntity = sysUserFeign.getCountryById(sourceCountry);
            if (Objects.nonNull(countryEntity)) {
                sourceCountryName = countryEntity.getNameCn();
            }
        }
        BeanMapper.copy(productLogistics, declareInfo);

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
        declareInfo.setLogisticsApproveStatus(productLogistics.getApproveStatus().getStatus());
        declareInfo.setLogisticsApproveStatusName(productLogistics.getApproveStatus().getName());
        //报关申报价币种
        String declareCurrency=declareInfo.getDeclareCurrency();
        //目的国申报价币种
        String destCurrency=declareInfo.getDestCurrency();
        if(StringUtils.isBlank(declareCurrency)){
            declareInfo.setDeclareCurrency(CurrencyEnum.CNY.getCurrencyCode());
            declareInfo.setDeclareCurrencySymbol(CurrencyEnum.CNY.getCurrencySymbol());
        }
        if(StringUtils.isBlank(destCurrency)){
            declareInfo.setDestCurrency(CurrencyEnum.USD.getCurrencyCode());
            declareInfo.setDestCurrencySymbol(CurrencyEnum.USD.getCurrencySymbol());
        }
        result.setDeclareInfo(declareInfo);
//        //国家列表
//        List<DictCountryDTO.ListDTO>  countryList = sysUserFeign.countryList();
//        List<ProductCustomsEntity> productCustomsList = productCustomsService.listBySkuId(skuId);
//        List<ProductCustomsDTO.ViewDTO> customsList = new ArrayList<>();
//        if (CollectionUtils.isNotEmpty(productCustomsList)) {
//            customsList = BeanMapper.copyList(productCustomsList, ProductCustomsDTO.ViewDTO.class);
//            customsList.forEach(viewDTO -> {
//                if (StringUtils.isEmpty(viewDTO.getToCurrency())){
//                    viewDTO.setToCurrency(CurrencyEnum.USD.getCurrencyCode());
//                    viewDTO.setToCurrencySymbol(CurrencyEnum.USD.getCurrencySymbol());
//                }
//                if (StringUtils.isNotBlank(viewDTO.getCountry())) {
//                    String[] split = viewDTO.getCountry().split(",");
//                    List<String> countryIdList = Arrays.asList(split);
//                    List<DictCountryDTO.ListDTO> dictCountryList = countryList.stream().filter(c->countryIdList.contains(c.getId())).collect(Collectors.toList());
//                    String countryName = dictCountryList.stream().map(DictCountryDTO.ListDTO::getNameCn).collect(Collectors.joining(","));
//                    viewDTO.setCountryName(countryName);
//                }
//            });
//        }
//        productCustomsList.forEach(req -> {
//
//        });
//        result.setCustomsList(customsList);
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
        List<String> customsIds = productCustomsList.stream().map(ProductCustomsEntity::getId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<String> skuIds = productCustomsList.stream().map(ProductCustomsEntity::getSkuId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<ProductCustomsEntity> productCustomsEntities = CollUtil.isNotEmpty(customsIds) ? productCustomsService.listByIds(customsIds) : Collections.emptyList();
        List<ProductDetailEntity> productDetailEntityList = CollUtil.isNotEmpty(skuIds) ? productDetailService.listByIds(skuIds) : Collections.emptyList();
        productCustomsList.forEach(productCustomsEntity -> {
            if (StringUtils.isNotEmpty(productCustomsEntity.getToCurrency())){
                productCustomsEntity.setToCurrencySymbol(CurrencyEnum.getSymbolByCode(productCustomsEntity.getToCurrency()));
            }
            if (CharSequenceUtil.isNotBlank(productCustomsEntity.getId())){
                ProductCustomsEntity customs = productCustomsEntities.stream().filter(e -> Objects.nonNull(e) && productCustomsEntity.getId().equals(e.getId())).findFirst().orElse(null);
                ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getId().equals(productCustomsEntity.getSkuId())).findFirst().orElse(new ProductDetailEntity());
                if (Objects.nonNull(customs)){
                    String msg = CharSequenceUtil.format("手动修改【{}】国家从【{}】改为【{}】，目的国申报价从【{}】改为【{}】", productCustomsEntity.getSkuNo(),customs.getCountry(), productCustomsEntity.getCountry(),customs.getToDeclarePrice(), productCustomsEntity.getToDeclarePrice());
                    sysLogService.addSysLogByOther(new SysLogEntity().setClassPath(SKUCLASSPATH).setPid(productDetailEntity.getProductId())
                            .setBusinessId(productCustomsEntity.getSkuId()).setOperation("编辑操作").setContent(msg));
                }
                productCustomsService.updateById(productCustomsEntity);
            }else {
                productCustomsService.save(productCustomsEntity);
            }
        });
        return logisticsResult;
    }


    @Override
    public Boolean exportExcel(LogisticsProductDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("物流产品列表", EXPORT_PLM_LOGISTICS_PRODUCT.getCode(), dto);
        return Boolean.TRUE;
    }


    @Override
    public Boolean importExcel(MultipartFile excelFile, HttpServletResponse response) {
        LogisticsProductExcelListener excelListenerUtil = new LogisticsProductExcelListener();
        try {
            read(excelFile.getInputStream(), LogisticsProductExcelDTO.class, excelListenerUtil).sheet(0).doRead();
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
            StringBuilder sb = new StringBuilder();
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
        List<LogisticsProductDTO.TabListDTO> list = new ArrayList<>();
        Integer allCount = this.baseMapper.listCount(new LogisticsProductDTO.PagingParamDTO());
        LogisticsProductDTO.TabListDTO allTab = new LogisticsProductDTO.TabListDTO();
        allTab.setTabFlag("all");
        allTab.setTabFlagName("全部");
        allTab.setCount(allCount);
        list.add(allTab);

        LogisticsProductDTO.TabListDTO tab = new LogisticsProductDTO.TabListDTO();
        List<String> fieldList = listField();
        Integer approvalStatus = ProductDetailStatusEnum.APPROVAL_PASS.getCode();
        String permissionSql = dto.getPermissionSql();
        Integer updateCount = baseMapper.logisticsProductUpdateCount(approvalStatus, fieldList, permissionSql);
        tab.setTabFlag("update");
        tab.setTabFlagName("更新");
        tab.setCount(updateCount);

        PageListTypeEnum[] values = PageListTypeEnum.values();
        for (PageListTypeEnum item : values) {
            LogisticsProductDTO.PagingParamDTO searchParamDTO = new LogisticsProductDTO.PagingParamDTO();
            searchParamDTO.setPermissionSql(searchParamDTO.getPermissionSql());
            LogisticsProductDTO.TabListDTO resultDTO = new LogisticsProductDTO.TabListDTO();
            //搜索类型
            searchParamDTO.setTabFlag(item.getCode());
            //列表Tab查询状态处理
            Boolean isFlag = doOpHandleTableParam(searchParamDTO);
            Integer count = MathUtil.ZERO;
            if (isFlag) {
                count = this.baseMapper.listCount(searchParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            resultDTO.setTabFlagName(item.getName());
            list.add(resultDTO);
        }
        list.add(tab);
        return list;
    }

    private Boolean doOpHandleTableParam (LogisticsProductDTO.PagingParamDTO params) {
        List<String> approveStatusList = new ArrayList<>(1);
        //待我审核
        if (PageListTypeEnum.TO_BE_APPROVE.getCode().equals(params.getTabFlag())) {
            approveStatusList.add(ApproveStatusEnum.APPROVE_ING.getStatus());
            //需要审核的业务ids
            List<String> businessIds = commonService.listProcessCurBusinessIds(SourceTypeEnum.PRODUCT_LOGISTICS.getCode());
            if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isEmpty(businessIds)) {
                return Boolean.FALSE;
            }
            params.setIdList(businessIds);
        }
        // 待提交
        if (PageListTypeEnum.WAIT_SUBMIT.getCode().equals(params.getTabFlag())) {
            approveStatusList.add(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        }
        //已审核
        if (PageListTypeEnum.APPROVE.getCode().equals(params.getTabFlag())) {
            approveStatusList.add(ApproveStatusEnum.APPROVE.getStatus());
        }
        //不通过
        if (PageListTypeEnum.REJECT.getCode().equals(params.getTabFlag())) {
            approveStatusList.add(ApproveStatusEnum.REJECT.getStatus());
        }
        if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(approveStatusList)) {
            params.setApproveStatusList(approveStatusList);
        }
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<LogisticsProductDTO.UpdatePagingDTO> updatePaging(PagingDTO<LogisticsProductDTO.UpdatePagingParamDTO> dto) {
        LogisticsProductDTO.UpdatePagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<LogisticsProductDTO.UpdatePagingParamDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        Integer approvalStatus = ProductDetailStatusEnum.APPROVAL_PASS.getCode();
        List<String> fieldList = listField();
        IPage<LogisticsProductDTO.UpdatePagingDTO> pageData = baseMapper.logisticsProductUpdatePaging(query, params, approvalStatus, fieldList);
        return new PagingVO<>(pageData);
    }

    @Override
    public List<LogisticsProductDTO.ProductDTO> listLogisticsProduct(List<String> skuIdList,List<String> skuNoList) {
        if (CollectionUtils.isEmpty(skuIdList) && CollectionUtils.isEmpty(skuNoList)) {
            return Collections.emptyList();
        }
        List<LogisticsProductDTO.ProductDTO> list = baseMapper.listLogisticsProduct(skuIdList,skuNoList);
        List<String> skuNoList1 = list.stream().map(LogisticsProductDTO.ProductDTO::getSkuNo).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<DmpSkuCostEntity> skuCostList = dmpTaskFeign.listRedisBySkuNoList(skuNoList1);
        //属性
        List<BasicDictEntity> dictList = basicDictService.listByType(BasicDictTypeEnum.DECLARE_PROPERTY.getCode());
        for (LogisticsProductDTO.ProductDTO item : list) {
            //毛重
            BigDecimal grossWeight = item.getGrossWeight();
            Integer weight = 0;
            if (Objects.nonNull(grossWeight)) {
                weight = grossWeight.intValue();
            }
            item.setWeight(weight);
            String propertyId = item.getProductPropertyId();
            List<BasicDictEntity> dictEntityList = dictList.stream().filter(e -> StringUtils.isNotBlank(propertyId) && propertyId.contains(e.getId())).collect(Collectors.toList());
            //是否带点
            BasicDictEntity electricDict = dictEntityList.stream().filter(e -> Objects.nonNull(e) && ProductConstant.IS_ELECTRIC.equals(e.getRemark())).findFirst().orElse(null);
            item.setIsElectric(Objects.nonNull(electricDict) ? Boolean.TRUE : Boolean.FALSE);
            //是否液体
            BasicDictEntity liquidDict = dictEntityList.stream().filter(e -> Objects.nonNull(e) && ProductConstant.IS_LIQUID.equals(e.getRemark())).findFirst().orElse(null);
            item.setIsLiquid(Objects.nonNull(liquidDict) ? Boolean.TRUE : Boolean.FALSE);
            //是否纯电
            BasicDictEntity batteryDict = dictEntityList.stream().filter(e -> Objects.nonNull(e) && e.getValue().contains("纯电")).findFirst().orElse(null);
            item.setOnlyBattery(Objects.nonNull(batteryDict) ? Boolean.TRUE : Boolean.FALSE);

            Map<String, BigDecimal> map = getSkuCost(skuCostList, item.getSkuId());
            item.setActualTaxCost(map.get("actualTaxCost"));
            item.setActualNoTaxCost(map.get("actualNoTaxCost"));
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO submit(String id, Boolean aTrue) {
        ProductLogisticsEntity entity = productLogisticsService.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"物流产品");
        }
        // 待提交或审核不通过并且未作废允许提交
        if ((!ApproveStatusEnum.WAIT_SUBMIT.equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.equals(entity.getApproveStatus()))) {
            throw new ServiceException(ApiError.ERROR_98010);
        }

        // 更新单据审核状态
        log.info("提交 开始修改物流产品状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动物流产品流程，id=：【{}】", entity.getId());
        startProcess(entity);
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.SUBMIT);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelProcess(String id) {
        ProductLogisticsEntity entity = productLogisticsService.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"物流产品");
        }
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98007);
        }

        log.info("撤销 开始修改物流产品状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.PRODUCT_LOGISTICS.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        ProductLogisticsEntity entity = productLogisticsService.getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"物流产品");
        }
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.approveStatus(approveStatus));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(String id) {
        ProductLogisticsEntity entity = productLogisticsService.getEntityById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"物流产品");
        }
        // 已审核支持反审核
        if (!Objects.equals(ApproveStatusEnum.APPROVE, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        //校验下推是否备案
        List<ProductRegistrationEntity> productRegistrationList = forecastFeign.listBySkuId(entity.getSkuId());
        if (CollectionUtils.isNotEmpty(productRegistrationList)) {
            long count = productRegistrationList.stream().filter(obj -> !CharSequenceUtil.equals(obj.getStatus(), ProductRegistrationEnum.StatusEnum.DRAFT.getCode())
                    && !CharSequenceUtil.equals(obj.getStatus(), ProductRegistrationEnum.StatusEnum.CANCEL.getCode())).count();
            if (count > 0) {
                throw new ServiceException(format("已下推备案信息(非备案不通过、已取消)不支持反审核",entity.getSkuNo()));
            }
        }
        // 更新审核信息
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.DISAPPROVE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, ProductLogisticsEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        //更新状态
        return updateForApprove(entity.getId(), approveStatus.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> pushRegistration(LogisticsProductDTO.PushRegistrationDTO dto) {
        List<ProductLogisticsEntity> productLogisticsList = productLogisticsService.listByIds(dto.getLogisticsProductIdList());
        if (CollectionUtils.isEmpty(productLogisticsList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"物流产品");
        }
        List<String> skuIdList = productLogisticsList.stream().map(ProductLogisticsEntity::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = productDetailService.listByIds(skuIdList);

        String skuNos = productLogisticsList.stream().filter(obj -> !ApproveStatusEnum.APPROVE.equals(obj.getApproveStatus()))
                .map(obj -> productDetailEntityList.stream().filter(e-> CharSequenceUtil.equals(obj.getSkuId(),e.getId())).findFirst().flatMap(e -> Optional.ofNullable(e.getSkuNo())).orElse(""))
                .collect(Collectors.joining(","));
        if (isNotBlank(skuNos)) {
            throw new ServiceException(format("物流产品信息SKU【{}】备案未审核完成，不支持推送备案",skuNos));
        }
        ProductRegistrationDTO.AddDTO addDTO = new ProductRegistrationDTO.AddDTO();
        addDTO.setDeclareSupplierId(dto.getDeclareSupplierId());
        addDTO.setSkuIds(skuIdList);
        List<BatchResultDTO> resultList = forecastFeign.add(addDTO);
        return resultList;
    }

    @Override
    public PagingVO<LogisticsProductDTO.ExportInfoDTO> exportLogisticsProduct(PagingDTO<LogisticsProductDTO.ExportDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        //列表Tab查询状态处理
        Boolean isFlag = doOpHandleTableParam(dto.getParams());
        if (Boolean.FALSE.equals(isFlag)) {
            return new PagingVO<>();
        }
        Integer approvalStatus = ProductDetailStatusEnum.APPROVAL_PASS.getCode();
        Page<LogisticsProductDTO.ExportInfoDTO> page = baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams(), approvalStatus);
        fillExport(page.getRecords());
        return new PagingVO<>(page);
    }


    /**
     * 更新审核状态
     */
    private void updateApproveStatus(String id, String approveStatus) {
        productLogisticsService.lambdaUpdate().eq(ProductLogisticsEntity::getId, id)
                .set(ProductLogisticsEntity::getApproveStatus, approveStatus)
                .set(ProductLogisticsEntity::getApproveUserId, "")
                .set(ProductLogisticsEntity::getApproveUserName, "")
                .set(ProductLogisticsEntity::getApproveTime, null)
                .update();
    }


    /**
     * 审核更新审核信息
     * @param id
     * @param approveStatus
     */
    public Boolean updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
       return productLogisticsService.lambdaUpdate().eq(ProductLogisticsEntity::getId, id)
                .set(ProductLogisticsEntity::getApproveUserId, userInfo.getUid())
                .set(ProductLogisticsEntity::getApproveUserName, userInfo.getUserName())
                .set(ProductLogisticsEntity::getApproveStatus, approveStatus)
                .set(ProductLogisticsEntity::getApproveTime, LocalDateTime.now())
                .update();
    }
    /**
     * @description: 启动流程
     * @author Will
     * @date: 2024/3/18 18:48
     * @param entity
     */
    public void startProcess(ProductLogisticsEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCustomsCode());
        startDTO.setBusinessKey(SourceTypeEnum.PRODUCT_LOGISTICS.getCode());
        startDTO.setBusinessName(entity.getCustomsCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    /**
     * 审核流程处理
     * @param entity
     * @param dto
     */
    private void approveProcess(ProductLogisticsEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.PRODUCT_LOGISTICS.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
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
        CurrencyEnum cny = CurrencyEnum.CNY;
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
                    filter(p -> p.getSkuId().equals(skuId)).findFirst().orElse(null);

            List<LogisticsProductExcelDTO> value = entry.getValue();
            if (Objects.isNull(logistics)){
                value.forEach(logisticsProductExcelDTO -> {
                    logisticsProductExcelDTO.setErrorMsg("sku不存在或者sku未审核通过");
                });
                continue;
            }
            List<ProductCustomsEntity> customsList = new ArrayList<>(value.size());

            for (LogisticsProductExcelDTO item : value) {
                List<String> errorMsgList = new ArrayList<>();
                String id = logistics.getId();
                BeanMapper.copy(item, logistics);
                logistics.setId(id);
                //国家
                String countryName = item.getCountry();
                String country = "default";
                if (StringUtils.isNotBlank(countryName)) {
                    country = countryList.stream().filter(s -> s.getNameCn().equals(countryName)).findFirst().
                            map(DictCountryEntity::getId).orElse("");
                    if (StringUtils.isBlank(country)) {
                        errorMsgList.add("国家不存在");
                    }
                }
                ProductCustomsEntity customs = new ProductCustomsEntity();
                //根据sku获取是否存在记录
                ProductCustomsEntity oldEntity = productCustomsService.getBySkuIdAndCountry(skuId, country);
                if (Objects.nonNull(oldEntity)){
                    BeanMapperUtils.copy(oldEntity,customs);
                }
                //报关申报价
                String declarePriceStr = item.getDeclarePrice();
                if (StringUtils.isNotBlank(declarePriceStr)) {
                    logistics.setDeclarePrice(new BigDecimal(declarePriceStr));
                    logistics.setDeclareCurrency(cny.getCurrencyCode());
                    logistics.setDeclareCurrencySymbol(cny.getCurrencySymbol());
                }
                //目的国申报价
                String destDeclarePriceStr = item.getDestDeclarePrice();
                if (StringUtils.isNotBlank(destDeclarePriceStr)) {
                    logistics.setDestDeclarePrice(new BigDecimal(destDeclarePriceStr));
                    logistics.setDestCurrencySymbol(usd.getCurrencySymbol());
                    logistics.setDestCurrency(usd.getCurrencyCode());
                    customs.setToDeclarePrice(new BigDecimal(destDeclarePriceStr));
                    customs.setToCurrency(usd.getCurrencyCode());
                    customs.setToCurrencySymbol(usd.getCurrencySymbol());
                }
                logistics.setFirstQty(isBlank(item.getFirstQtyStr()) ? null : new BigDecimal(item.getFirstQtyStr()));
                logistics.setSecondQty(isBlank(item.getSecondQtyStr()) ? null : new BigDecimal(item.getSecondQtyStr()));
                if (StringUtils.isBlank(skuId)) {
                    errorMsgList.add("sku不存在或者sku未审核通过");
                }
                if (!CharSequenceUtil.equals(logistics.getApproveStatus().getCode(),ApproveStatusEnum.WAIT_SUBMIT.getCode())) {
                    errorMsgList.add("只有待提交物流产品支持导入");
                }
                String combinationDeclareTypeStr = item.getCombinationDeclareType();
                String combinationDeclareType = "";
                if (StringUtils.isEmpty(combinationDeclareTypeStr)){
                    combinationDeclareType = CombinationDeclareTypeEnums.SPLIT.getCode();
                }else {
                    combinationDeclareType = CombinationDeclareTypeEnums.getCode(combinationDeclareTypeStr);
                }
                logistics.setCombinationDeclareType(combinationDeclareType);
                if (StringUtils.isBlank(combinationDeclareType)) {
                    errorMsgList.add("组合品申报不存在");
                }
                customs.setSkuId(skuId);
                if (StringUtils.isNotBlank(item.getDestCustomsCode())){
                    customs.setCustomsCode(item.getDestCustomsCode());
                }
                if (StringUtils.isNotBlank(country)){
                    customs.setCountryName(countryName);
                    customs.setCountry(country);
                }
                customs.setSkuNo(item.getSkuNo());
                String taxRateStr = item.getTaxRate();
                if (StringUtils.isNotBlank(taxRateStr)) {
                    BigDecimal taxRate = new BigDecimal(taxRateStr);
                    customs.setTaxRate(taxRate);
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

           productLogisticsService.saveOrUpdate(logistics);
            productCustomsService.saveOrUpdateBatch(customsList);
            productCustomsService.addDefaultCustoms(Collections.singletonList(skuId));
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
        List<String> skuIdList = list.stream().map(LogisticsProductDTO.PagingVO::getSkuId).distinct().collect(Collectors.toList());
        //根据SKU查询BOM判断是否是组合SKU
        List<BomChildrenSkuDTO> bomChildrenList = bomSkuService.listBomChildBySkuIds(skuIdList);
        String bomType = BomTypeEnum.COMBINATION.getType();
        for (LogisticsProductDTO.PagingVO item : list) {
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
            //是否是组合SKU
            Boolean isCombination = Boolean.FALSE;
            if (CollectionUtils.isNotEmpty(bomChildrenList)) {
                long count = bomChildrenList.stream().filter(e -> e.getParentSkuId().equals(item.getSkuId())&& bomType.equals(e.getType())).count();
                if (count > 0) {
                    isCombination = Boolean.TRUE;
                }
            }
            item.setIsCombination(isCombination);
            item.setLogisticsApproveStatusName(ApproveStatusEnum.getName(item.getLogisticsApproveStatus()));
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

        List<DmpSkuCostEntity>  skuCostList= dmpTaskFeign.listRedisBySkuNoList(skuNoList);

        for (LogisticsProductDTO.ExportInfoDTO item : list) {
            String skuId = item.getSkuId();
            item.setLogisticsApproveStatusName(ApproveStatusEnum.getName(item.getLogisticsApproveStatus()));
            //含税成本
            BigDecimal actualTaxCost = item.getActualTaxCost();
            if (Objects.isNull(actualTaxCost) || zero.compareTo(actualTaxCost) == 0) {
                Map<String, BigDecimal> map = getSkuCost(skuCostList, skuId);
                actualTaxCost = map.get("actualTaxCost");
                item.setActualTaxCost(actualTaxCost);
                item.setActualNoTaxCost(map.get("actualNoTaxCost"));
            }
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
