package com.erp.server.plm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.FindUserDTO;
import com.common.core.enums.ApiError;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.common.business.enums.MonthEnum;
import com.common.business.enums.ProductTypeEnum;
import com.common.business.enums.SeasonEnum;
import com.erp.model.plm.dto.excel.ProductPlanExcelDTO;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.BasicDictTypeEnum;
import com.erp.model.plm.enums.ProductStyleEnum;
import com.erp.model.plm.enums.ThreeGenerationPlanningEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.service.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/21 10:16
 */
public class ProductPlanExcelListener extends AnalysisEventListener<ProductPlanExcelDTO> {

    private SysUserFeign sysUserFeign;

    private ProductPlanService productPlanService;

    private BasicDictService basicDictService;

    private BasicCategoryService basicCategoryService;

    private ProductPlanSaleService productPlanSaleService;

    private ProductPlanSaleInfoService productPlanSaleInfoService;

    private ProductPlanPurchaseService  productPlanPurchaseService;

    private ProductPlanRemarkService productPlanRemarkService;
    /**
     * 错误数据返回集合
     */
    private List<ProductPlanExcelDTO> list;

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d");
    /**
     * 导入数据，用于判断导入是否为空
     */
    private List<ProductPlanExcelDTO> dataList = new ArrayList<>();

    public ProductPlanExcelListener(ProductPlanService productPlanService, BasicDictService basicDictService,
                                    BasicCategoryService basicCategoryService, ProductPlanSaleService productPlanSaleService, ProductPlanSaleInfoService productPlanSaleInfoService,
                                    ProductPlanPurchaseService  productPlanPurchaseService,ProductPlanRemarkService productPlanRemarkService, SysUserFeign sysUserFeign) {
        this.productPlanService = productPlanService;
        this.basicDictService = basicDictService;
        this.basicCategoryService = basicCategoryService;
        this.productPlanSaleService = productPlanSaleService;
        this.productPlanSaleInfoService = productPlanSaleInfoService;
        this.productPlanPurchaseService = productPlanPurchaseService;
        this.productPlanRemarkService = productPlanRemarkService;
        this.sysUserFeign = sysUserFeign;
        this.list = new ArrayList<>();
    }

    @Override
    public void invoke(ProductPlanExcelDTO productPlanExcelDTO, AnalysisContext analysisContext) {
        //列表返回错误信息
        List<String> errorMsgList = new ArrayList<>();
        ProductPlanEntity productPlanEntity = new ProductPlanEntity();
        BeanMapperUtils.copy(productPlanExcelDTO, productPlanEntity);
        //添加数据用于判断是否为空
        dataList.add(productPlanExcelDTO);
        //注解验证信息
        List<String> msgList = FieldValidUtil.fieldValid(productPlanExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        if (StringUtils.isNotBlank(productPlanExcelDTO.getChargeName())) {
            FindUserDTO charge = sysUserFeign.getUserByUserName(productPlanExcelDTO.getChargeName());
            if (ObjectUtils.isEmpty(charge) || StringUtils.isBlank(charge.getUserId())) {
                errorMsgList.add("产品经理在系统中未找到");
            }
            productPlanEntity.setChargeId(charge.getUserId());
        }

        if (StringUtils.isNotBlank(productPlanExcelDTO.getBrandName())) {
            BasicDictEntity productBrand = basicDictService.checkBasicDict(BasicDictTypeEnum.PRODUCT_BRAND.getCode(), productPlanExcelDTO.getBrandName());
            if (ObjectUtils.isEmpty(productBrand)) {
                errorMsgList.add("产品品牌在系统中未找到");
            } else {
                productPlanEntity.setBrandId(productBrand.getId());
            }
        }
        if (StringUtils.isNotBlank(productPlanExcelDTO.getProperty())) {
            BasicDictEntity productProperty = basicDictService.checkBasicDict(BasicDictTypeEnum.PRODUCT_PROPERTY.getCode(), productPlanExcelDTO.getProperty());
            if (ObjectUtils.isEmpty(productProperty)) {
                errorMsgList.add("项目类型在系统中未找到");
            } else {
                productPlanEntity.setPropertyId(productProperty.getId());
            }
        }
        if (StringUtils.isNotBlank(productPlanExcelDTO.getGrade())) {
            BasicDictEntity grade = basicDictService.checkBasicDict(BasicDictTypeEnum.PRODUCT_GRADE.getCode(), productPlanExcelDTO.getGrade());
            if (ObjectUtils.isEmpty(grade)) {
                errorMsgList.add("产品等级在系统中未找到");
            } else {
                productPlanEntity.setGradeId(grade.getId());
            }
        }

        //产品分类
        String category = productPlanExcelDTO.getCategory();
        if (StringUtils.isNotBlank(category)) {
            BasicCategoryEntity basicCategoryEntity = basicCategoryService.getCategoryByName(category, Boolean.FALSE);
            if (ObjectUtils.isEmpty(basicCategoryEntity)) {
                errorMsgList.add("产品分类不存在");
            } else {
                //父级品类
                List<BasicCategoryEntity> categoryList = basicCategoryService.listParentEntity(basicCategoryEntity.getId());
                if (CollectionUtils.isEmpty(categoryList)) {
                    errorMsgList.add("产品分类不存在");
                }
                //一级品类
                BasicCategoryEntity bestEntity = categoryList.stream().filter(obj -> "0".equals(obj.getPid())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(bestEntity) || StringUtils.isBlank(bestEntity.getCode())) {
                    errorMsgList.add(ApiError.ERROR_95091.msg);
                }
                //二级品类
                BasicCategoryEntity secondEntity = categoryList.stream().filter(obj -> bestEntity.getId().equals(obj.getPid())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(secondEntity) || StringUtils.isBlank(secondEntity.getCode())) {
                    errorMsgList.add(ApiError.ERROR_95092.msg);
                }
                productPlanEntity.setCategoryId(basicCategoryEntity.getId());
            }
        }
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            productPlanExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            list.add(productPlanExcelDTO);
            return;
        }
        productPlanEntity.setYear(Integer.valueOf(productPlanExcelDTO.getYearStr()));
        productPlanEntity.setProductType(ProductTypeEnum.getCodeByName(productPlanExcelDTO.getProductTypeName()));
        productPlanEntity.setProductStyle(ProductStyleEnum.getCodeByName(productPlanExcelDTO.getProductStyleName()));
        productPlanEntity.setThreeGenerationPlanning(ThreeGenerationPlanningEnum.getCodeByName(productPlanExcelDTO.getThreeGenerationPlanningName()));
        productPlanEntity.setSkuQty(StringUtils.isBlank(productPlanExcelDTO.getSkuQtyStr()) ? 0 : Integer.valueOf(productPlanExcelDTO.getSkuQtyStr()));
        productPlanEntity.setIsNeedIDDesign(StringUtils.isBlank(productPlanExcelDTO.getIsNeedIDDesignStr()) ? Boolean.FALSE : ("是".equals(productPlanExcelDTO.getIsNeedIDDesignStr()) ? Boolean.TRUE : Boolean.FALSE));
        productPlanEntity.setIsNeedStructuralDesign(StringUtils.isBlank(productPlanExcelDTO.getIsNeedStructuralDesignStr()) ? Boolean.FALSE : ("是".equals(productPlanExcelDTO.getIsNeedStructuralDesignStr()) ? Boolean.TRUE : Boolean.FALSE));
        productPlanEntity.setPlanMarketingSeason(SeasonEnum.getCodeByName(productPlanExcelDTO.getPlanMarketingSeasonName()));
        if (StringUtils.isNotBlank(productPlanExcelDTO.getPlanSurveyDateStr())) {
            productPlanEntity.setPlanSurveyDate(LocalDate.parse(productPlanExcelDTO.getPlanSurveyDateStr(), dateTimeFormatter));
        }
        if (StringUtils.isNotBlank(productPlanExcelDTO.getPlanProjectApprovalDateStr())) {
            productPlanEntity.setPlanProjectApprovalDate(LocalDate.parse(productPlanExcelDTO.getPlanProjectApprovalDateStr(), dateTimeFormatter));
        }
        if (StringUtils.isNotBlank(productPlanExcelDTO.getPlanFirstMassStockInDateStr())) {
            productPlanEntity.setPlanFirstMassStockInDate(LocalDate.parse(productPlanExcelDTO.getPlanFirstMassStockInDateStr(), dateTimeFormatter));
        }
        if (StringUtils.isNotBlank(productPlanExcelDTO.getPlanListingDateStr())) {
            productPlanEntity.setPlanListingDate(LocalDate.parse(productPlanExcelDTO.getPlanListingDateStr(), dateTimeFormatter));
        }

        //规划信息
        ProductPlanEntity planFound = productPlanService.getByYearAndName(productPlanEntity.getYear(), productPlanEntity.getName());
        if (ObjectUtils.isNotEmpty(planFound)) {
            productPlanEntity.setId(planFound.getId());
        }
        productPlanService.saveOrUpdate(productPlanEntity);

        //销售信息
        ProductPlanSaleEntity productPlanSaleEntity = new ProductPlanSaleEntity();
        ProductPlanSaleEntity saleFound = productPlanSaleService.getByProductPlanId(productPlanEntity.getId());
        if (ObjectUtils.isNotEmpty(saleFound)) {
            productPlanSaleEntity.setId(saleFound.getId());
        }
        productPlanSaleEntity.setPriceCny(StringUtils.isBlank(productPlanExcelDTO.getPriceCnyStr()) ? BigDecimal.ZERO : MathUtil.valueOf(productPlanExcelDTO.getPriceCnyStr()));
        productPlanSaleEntity.setPriceUsd(StringUtils.isBlank(productPlanExcelDTO.getPriceUsdStr()) ? BigDecimal.ZERO : MathUtil.valueOf(productPlanExcelDTO.getPriceUsdStr()));
        productPlanSaleEntity.setSalesPlatform(productPlanExcelDTO.getSalesPlatformName());
        productPlanSaleEntity.setSalesTargetCountry(productPlanExcelDTO.getSalesTargetCountry());
        productPlanSaleEntity.setProductPlanId(productPlanEntity.getId());
        productPlanSaleService.saveOrUpdate(productPlanSaleEntity);

        //采购信息
        ProductPlanPurchaseEntity productPlanPurchaseEntity = new ProductPlanPurchaseEntity();
        ProductPlanPurchaseEntity purchaseFound = productPlanPurchaseService.getByProductPlanId(productPlanEntity.getId());
        if (ObjectUtils.isNotEmpty(purchaseFound)) {
            productPlanPurchaseEntity.setId(purchaseFound.getId());
        }
        productPlanPurchaseEntity.setTargetCost(StringUtils.isBlank(productPlanExcelDTO.getTargetCostStr()) ? BigDecimal.ZERO : MathUtil.valueOf(productPlanExcelDTO.getTargetCostStr()));
        productPlanPurchaseEntity.setEstimatedMoldCost(StringUtils.isBlank(productPlanExcelDTO.getEstimatedMoldCostStr()) ? BigDecimal.ZERO : MathUtil.valueOf(productPlanExcelDTO.getEstimatedMoldCostStr()));
        productPlanPurchaseEntity.setSupplierStatus(productPlanExcelDTO.getSupplierStatus());
        productPlanPurchaseEntity.setMainSupplierName(productPlanExcelDTO.getMainSupplierName());
        productPlanPurchaseEntity.setProductPlanId(productPlanEntity.getId());
        productPlanPurchaseService.saveOrUpdate(productPlanPurchaseEntity);

        //备注信息
        ProductPlanRemarkEntity productPlanRemarkEntity = new ProductPlanRemarkEntity();
        productPlanRemarkEntity.setRemark(productPlanExcelDTO.getRemark());
        productPlanRemarkEntity.setProductPlanId(productPlanEntity.getId());
        productPlanRemarkService.save(productPlanRemarkEntity);


        //删除销售信息
        productPlanSaleInfoService.removeByProductPlanId(productPlanEntity.getId());
        List<ProductPlanSaleInfoEntity> saleInfoList = new ArrayList<>();
        //销售数据信息
        MonthEnum[] values = MonthEnum.values();
        for (MonthEnum monthEnum : values) {
            ProductPlanSaleInfoEntity productPlanSaleInfoEntity = new ProductPlanSaleInfoEntity();
            productPlanSaleInfoEntity.setProductPlanId(productPlanEntity.getId());
            productPlanSaleInfoEntity.setYear(Integer.valueOf(productPlanExcelDTO.getYearStr()));
            productPlanSaleInfoEntity.setMonth(Integer.valueOf(monthEnum.getCode()));
            if (MonthEnum.JANUARY.getCode().equals(monthEnum.getCode())) {
                productPlanSaleInfoEntity.setSalesQty(StringUtils.isBlank(productPlanExcelDTO.getJanuaryQtyStr()) ? MathUtil.ZERO : Integer.valueOf(productPlanExcelDTO.getJanuaryQtyStr()));
                productPlanSaleInfoEntity.setSalesAmount(MathUtil.valueOf(productPlanExcelDTO.getJanuaryAmountStr()));
            }
            if (MonthEnum.FEBRUARY.getCode().equals(monthEnum.getCode())) {
                productPlanSaleInfoEntity.setSalesQty(StringUtils.isBlank(productPlanExcelDTO.getFebruaryQtyStr()) ? MathUtil.ZERO : Integer.valueOf(productPlanExcelDTO.getFebruaryQtyStr()));
                productPlanSaleInfoEntity.setSalesAmount(MathUtil.valueOf(productPlanExcelDTO.getFebruaryAmountStr()));
            }
            if (MonthEnum.MARCH.getCode().equals(monthEnum.getCode())) {
                productPlanSaleInfoEntity.setSalesQty(StringUtils.isBlank(productPlanExcelDTO.getMarchQtyStr()) ? MathUtil.ZERO : Integer.valueOf(productPlanExcelDTO.getMarchQtyStr()));
                productPlanSaleInfoEntity.setSalesAmount(MathUtil.valueOf(productPlanExcelDTO.getMarchAmountStr()));
            }
            if (MonthEnum.APRIL.getCode().equals(monthEnum.getCode())) {
                productPlanSaleInfoEntity.setSalesQty(StringUtils.isBlank(productPlanExcelDTO.getAprilQtyStr()) ? MathUtil.ZERO : Integer.valueOf(productPlanExcelDTO.getAprilQtyStr()));
                productPlanSaleInfoEntity.setSalesAmount(MathUtil.valueOf(productPlanExcelDTO.getAprilAmountStr()));
            }
            if (MonthEnum.MAY.getCode().equals(monthEnum.getCode())) {
                productPlanSaleInfoEntity.setSalesQty(StringUtils.isBlank(productPlanExcelDTO.getMayQtyStr()) ? MathUtil.ZERO : Integer.valueOf(productPlanExcelDTO.getMayQtyStr()));
                productPlanSaleInfoEntity.setSalesAmount(MathUtil.valueOf(productPlanExcelDTO.getMayAmountStr()));
            }
            if (MonthEnum.JUNE.getCode().equals(monthEnum.getCode())) {
                productPlanSaleInfoEntity.setSalesQty(StringUtils.isBlank(productPlanExcelDTO.getJuneQtyStr()) ? MathUtil.ZERO : Integer.valueOf(productPlanExcelDTO.getJuneQtyStr()));
                productPlanSaleInfoEntity.setSalesAmount(MathUtil.valueOf(productPlanExcelDTO.getJuneAmountStr()));
            }
            if (MonthEnum.JULY.getCode().equals(monthEnum.getCode())) {
                productPlanSaleInfoEntity.setSalesQty(StringUtils.isBlank(productPlanExcelDTO.getJulyQtyStr()) ? MathUtil.ZERO : Integer.valueOf(productPlanExcelDTO.getJulyQtyStr()));
                productPlanSaleInfoEntity.setSalesAmount(MathUtil.valueOf(productPlanExcelDTO.getJulyAmountStr()));
            }
            if (MonthEnum.AUGUST.getCode().equals(monthEnum.getCode())) {
                productPlanSaleInfoEntity.setSalesQty(StringUtils.isBlank(productPlanExcelDTO.getAugustQtyStr()) ? MathUtil.ZERO : Integer.valueOf(productPlanExcelDTO.getAugustQtyStr()));
                productPlanSaleInfoEntity.setSalesAmount(MathUtil.valueOf(productPlanExcelDTO.getAugustAmountStr()));
            }
            if (MonthEnum.SEPTEMBER.getCode().equals(monthEnum.getCode())) {
                productPlanSaleInfoEntity.setSalesQty(StringUtils.isBlank(productPlanExcelDTO.getSeptemberQtyStr()) ? MathUtil.ZERO : Integer.valueOf(productPlanExcelDTO.getSeptemberQtyStr()));
                productPlanSaleInfoEntity.setSalesAmount(MathUtil.valueOf(productPlanExcelDTO.getSeptemberAmountStr()));
            }
            if (MonthEnum.OCTOBER.getCode().equals(monthEnum.getCode())) {
                productPlanSaleInfoEntity.setSalesQty(StringUtils.isBlank(productPlanExcelDTO.getOctoberQtyStr()) ? MathUtil.ZERO : Integer.valueOf(productPlanExcelDTO.getOctoberQtyStr()));
                productPlanSaleInfoEntity.setSalesAmount(MathUtil.valueOf(productPlanExcelDTO.getOctoberAmountStr()));
            }
            if (MonthEnum.NOVEMBER.getCode().equals(monthEnum.getCode())) {
                productPlanSaleInfoEntity.setSalesQty(StringUtils.isBlank(productPlanExcelDTO.getNovemberQtyStr()) ? MathUtil.ZERO : Integer.valueOf(productPlanExcelDTO.getNovemberQtyStr()));
                productPlanSaleInfoEntity.setSalesAmount(MathUtil.valueOf(productPlanExcelDTO.getNovemberAmountStr()));
            }
            if (MonthEnum.DECEMBER.getCode().equals(monthEnum.getCode())) {
                productPlanSaleInfoEntity.setSalesQty(StringUtils.isBlank(productPlanExcelDTO.getDecemberQtyStr()) ? MathUtil.ZERO : Integer.valueOf(productPlanExcelDTO.getDecemberQtyStr()));
                productPlanSaleInfoEntity.setSalesAmount(MathUtil.valueOf(productPlanExcelDTO.getDecemberAmountStr()));
            }
            saleInfoList.add(productPlanSaleInfoEntity);
        }
        if (CollectionUtils.isNotEmpty(saleInfoList)) {
            productPlanSaleInfoService.saveBatch(saleInfoList);
        }
    }
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    public List<ProductPlanExcelDTO> getDateList(){
        return list;
    }

    public List<ProductPlanExcelDTO> getExcelDateList(){
        return dataList;
    }
}
