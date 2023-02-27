package com.erp.server.plm.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.alibaba.excel.util.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.MonthEnum;
import com.common.business.enums.ProductTypeEnum;
import com.common.business.enums.SalesPlatformEnum;
import com.common.business.enums.SeasonEnum;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.vo.SeriesVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.dto.excel.ProductPlanExcelDTO;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.*;
import com.erp.model.plm.vo.ProductPlanGroupVO;
import com.erp.model.plm.vo.ProductPlanStatisticsVO;
import com.erp.model.plm.vo.ProductPlanVO;
import com.erp.model.sys.dto.SysUserDeptDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.listener.ProductPlanExcelListener;
import com.erp.server.plm.mapper.ProductPlanMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 19:53
 */
@Service
public class ProductPlanServiceImpl extends ServiceImpl<ProductPlanMapper, ProductPlanEntity>
        implements ProductPlanService {
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ProductPlanPurchaseService productPlanPurchaseService;

    @Resource
    private ProductPlanSaleService productPlanSaleService;

    @Resource
    private ProductPlanSaleInfoService productPlanSaleInfoService;

    @Resource
    private ProductPlanRemarkService productPlanRemarkService;

    @Resource
    private CommonService commonService;

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private ProjectInfoService projectInfoService;

    @Resource
    private ProjectStatusTimeService projectStatusTimeService;

    @Resource
    private BasicDictService basicDictService;

    @Resource
    private BasicCategoryService basicCategoryService;

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private ProductSaleService productSaleService;


    @Override
    public PagingVO<List<ProductPlanVO>> paging(PagingDTO<ProductPlanSearchDTO> pagingDTO) {
        pagingDTO.getParams().setParam(pagingDTO.getParam());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<ProductPlanVO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        List<ProductPlanVO> records = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            records.forEach(obj -> {
                //产品状态格式化
                obj.setProductStatusName(ProductPlanStatusEnum.getNameByCode(obj.getProductStatus()));
                //调研是否延期
                if (ObjectUtils.isNotEmpty(obj.getSurveyDate()) && ObjectUtils.isNotEmpty(obj.getPlanSurveyDate())) {
                    obj.setIsDelaySurvey(obj.getSurveyDate().isAfter(obj.getPlanSurveyDate()));
                }
                //立项是否延期
                if (ObjectUtils.isNotEmpty(obj.getProjectApprovalDate()) && ObjectUtils.isNotEmpty(obj.getPlanProjectApprovalDate())) {
                    obj.setIsDelaySurvey(obj.getProjectApprovalDate().isAfter(obj.getPlanProjectApprovalDate()));
                }
            });
        }
        return new PagingVO(pageData);
    }

    @Override
    public ProductPlanDetailsDTO productPlanDetails(String id) {
        ProductPlanDetailsDTO resultDTO = new ProductPlanDetailsDTO();
        //查询产品信息
        ProductPlanEntity productPlanEntity = this.getById(id);
        if (ObjectUtils.isEmpty(productPlanEntity)) {
            throw new ServiceException(ApiError.ERROR_95133);
        }
        ProductPlanDTO productPlanDTO = new ProductPlanDTO();
        BeanMapperUtils.copy(productPlanEntity,productPlanDTO);
        resultDTO.setProductPlanDTO(productPlanDTO);
        //枚举格式化
        productPlanDTO.setProductStyleName(ProductStyleEnum.getNameByCode(productPlanEntity.getProductStyle()));
        productPlanDTO.setProductTypeName(ProductTypeEnum.getNameByCode(productPlanEntity.getProductType()));
        productPlanDTO.setThreeGenerationPlanningName(ThreeGenerationPlanningEnum.getNameByCode( productPlanEntity.getThreeGenerationPlanning()));
        productPlanDTO.setPlanMarketingSeasonName(SeasonEnum.getNameByCode(productPlanEntity.getPlanMarketingSeason()));
        //调研是否延期
        if (ObjectUtils.isNotEmpty(productPlanEntity.getSurveyDate()) && ObjectUtils.isNotEmpty(productPlanEntity.getPlanSurveyDate())) {
            productPlanDTO.setIsDelaySurvey(productPlanEntity.getSurveyDate().isAfter(productPlanEntity.getPlanSurveyDate()));
        }
        //立项是否延期
        if (ObjectUtils.isNotEmpty(productPlanEntity.getProjectApprovalDate()) && ObjectUtils.isNotEmpty(productPlanEntity.getPlanProjectApprovalDate())) {
            productPlanDTO.setIsDelaySurvey(productPlanEntity.getProjectApprovalDate().isAfter(productPlanEntity.getPlanProjectApprovalDate()));
        }
        //是否需要ID设计
        productPlanDTO.setIsNeedIDDesignStr(productPlanEntity.getIsNeedIDDesign() ? "是" : "否");
        //是否需要结构设计
        productPlanDTO.setIsNeedStructuralDesignStr(productPlanEntity.getIsNeedStructuralDesign() ? "是" : "否");
        //查询一级类目
        if (StringUtils.isNotBlank(productPlanEntity.getCategoryId())) {
            List<BasicCategoryEntity> categoryList = basicCategoryService.listParentEntity(productPlanEntity.getCategoryId());
            if (CollectionUtils.isNotEmpty(categoryList)) {
                //一级品类
                BasicCategoryEntity bestEntity = categoryList.stream().filter(obj -> "0".equals(obj.getPid())).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(bestEntity)) {
                    productPlanDTO.setFirstCategory(bestEntity.getName());
                }
            }
        }


        //查询采购信息
        ProductPlanPurchaseEntity productPlanPurchaseEntity = productPlanPurchaseService.getByProductPlanId(id);
        if (ObjectUtils.isEmpty(productPlanPurchaseEntity)) {
            throw new ServiceException(ApiError.ERROR_95134);
        }
        ProductPlanPurchaseDTO productPlanPurchaseDTO = new ProductPlanPurchaseDTO();
        BeanMapperUtils.copy(productPlanPurchaseEntity,productPlanPurchaseDTO);
        productPlanPurchaseDTO.setSupplierStatusName(productPlanPurchaseEntity.getSupplierStatus());
        resultDTO.setProductPlanPurchaseDTO(productPlanPurchaseDTO);

        //查询销售信息
        ProductPlanSaleEntity productPlanSaleEntity = productPlanSaleService.getByProductPlanId(id);
        if (ObjectUtils.isEmpty(productPlanPurchaseEntity)) {
            throw new ServiceException(ApiError.ERROR_95134);
        }
        ProductPlanSaleDTO productPlanSaleDTO = new ProductPlanSaleDTO();
        BeanMapperUtils.copy(productPlanSaleEntity,productPlanSaleDTO);
        resultDTO.setProductPlanSaleDTO(productPlanSaleDTO);
        productPlanSaleDTO.setSalesPlatformName(SalesPlatformEnum.getNameByName(productPlanSaleEntity.getSalesPlatform()));

        //查询备注信息
       List<ProductPlanRemarkEntity> remarkList = productPlanRemarkService.listByProductPlanId(id);
        if (CollectionUtils.isNotEmpty(remarkList)) {
            List<ProductPlanRemarkDTO> list = BeanMapperUtils.copyList(ProductPlanRemarkDTO.class,remarkList);
            resultDTO.setRemarkList(list);
        }

        //查询销售数据信息
        List<ProductPlanSaleInfoEntity> saleInfoList = productPlanSaleInfoService.listByProductPlanId(id);
        if (CollectionUtils.isNotEmpty(saleInfoList)) {
            Integer year = saleInfoList.stream().max(Comparator.comparingInt(ProductPlanSaleInfoEntity::getYear)).map(ProductPlanSaleInfoEntity::getYear).get();
            //最新年份的
            saleInfoList = saleInfoList.stream().filter(e -> e.getYear().equals(year)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(saleInfoList)) {
                List<ProductPlanSaleInfoDTO> list = BeanMapperUtils.copyList(ProductPlanSaleInfoDTO.class,saleInfoList);
                Integer totalQty = list.stream().map(ProductPlanSaleInfoDTO::getSalesQty).reduce(Integer::sum).get();
                BigDecimal totalAmount = list.stream().map(ProductPlanSaleInfoDTO::getSalesAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                resultDTO.setTotalQty(totalQty);
                resultDTO.setYear(year);
                resultDTO.setTotalAmount(totalAmount);
                resultDTO.setSaleInfoList(list);
            }
        }

        List<ProductPlanProgressDTO> progressList = new ArrayList<>();
        //规划进度
        ProductPlanProcessEnum[] values = ProductPlanProcessEnum.values();
        for (ProductPlanProcessEnum plmEnum : values) {
            setProductPlanProgress(productPlanEntity, progressList, plmEnum.getName());
        }
        resultDTO.setProgressList(progressList);
        return resultDTO;
    }

    @Override
    @Transactional
    public Boolean deleteById(String id) {
        ProductPlanEntity productPlanEntity = this.getById(id);
        if (ObjectUtils.isEmpty(productPlanEntity)) {
            throw new ServiceException(ApiError.ERROR_95133);
        }
        //删除采购信息
        productPlanPurchaseService.removeByProductPlanId(id);
        //删除销售信息
        productPlanSaleService.removeByProductPlanId(id);
        //删除销售数据
        productPlanSaleInfoService.removeByProductPlanId(id);
        //删除备注信息
        productPlanRemarkService.removeByProductPlanId(id);
        //删除规划
        return this.removeByProductPlanId(id);
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        ProductPlanExcelListener excelListenerUtil = new ProductPlanExcelListener(this,basicDictService,basicCategoryService,productPlanSaleService,productPlanSaleInfoService,productPlanPurchaseService,productPlanRemarkService,sysUserFeign);
        try {
            EasyExcel.read(excelFile.getInputStream(), ProductPlanExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            throw new ServiceException(ApiError.ERROR_1016);
        }
        List<ProductPlanExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        List<ProductPlanExcelDTO> list = excelListenerUtil.getDateList();
        if (list.size() > 0) {
            StringBuffer sb = new StringBuffer();
            String excelPath = "excel/productPlan.xlsx";
            String name = "productPlan";
            String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            sb.append(date);
            sb.append(name);
            try {
                new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
            } catch (IOException e) {
                throw new ServiceException(ApiError.ERROR_95125);
            }
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean exportProductPlan(ProductPlanSearchDTO productPlanSearchDTO, HttpServletResponse response) {
        List<ProductPlanExcelDTO> productPlanExcelDTOS = baseMapper.listExportExcel(productPlanSearchDTO);
        if (CollectionUtils.isNotEmpty(productPlanExcelDTOS)) {
            List<String> productPlanIds = productPlanExcelDTOS.stream().map(ProductPlanExcelDTO::getProductPlanId).collect(Collectors.toList());
            List<ProductPlanSaleInfoEntity> productPlanSaleInfoList = productPlanSaleInfoService.listByProductPlanIds(productPlanIds);
            productPlanExcelDTOS.forEach(obj -> {
                //枚举格式化
                obj.setProductStyleName(ProductStyleEnum.getNameByCode(obj.getProductStyleName()));
                obj.setProductTypeName(ProductTypeEnum.getNameByCode(obj.getProductTypeName()));
                obj.setThreeGenerationPlanningName(ThreeGenerationPlanningEnum.getNameByCode( obj.getThreeGenerationPlanningName()));
                obj.setSalesPlatformName(SalesPlatformEnum.getNameByName(obj.getSalesPlatformName()));
                obj.setPlanMarketingSeasonName(SeasonEnum.getNameByCode(obj.getPlanMarketingSeasonName()));
                //销售数据信息
                if (CollectionUtils.isEmpty(productPlanSaleInfoList)) {
                    return;
                }
                List<ProductPlanSaleInfoEntity> saleInfoList = productPlanSaleInfoList.stream().filter(e -> e.getProductPlanId().equals(obj.getProductPlanId())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(saleInfoList)) {
                    return;
                }
                for (ProductPlanSaleInfoEntity entity: saleInfoList) {
                    if (MonthEnum.JANUARY.getCode().equals(entity.getMonth().toString())) {
                        obj.setJanuaryQtyStr(entity.getSalesQty().toString());
                        obj.setJanuaryAmountStr(entity.getSalesAmount().stripTrailingZeros().toPlainString());
                    }
                    if (MonthEnum.FEBRUARY.getCode().equals(entity.getMonth().toString())) {
                        obj.setFebruaryQtyStr(entity.getSalesQty().toString());
                        obj.setFebruaryAmountStr(entity.getSalesAmount().stripTrailingZeros().toPlainString());
                    }
                    if (MonthEnum.MARCH.getCode().equals(entity.getMonth().toString())) {
                        obj.setMarchQtyStr(entity.getSalesQty().toString());
                        obj.setMarchAmountStr(entity.getSalesAmount().stripTrailingZeros().toPlainString());
                    }
                    if (MonthEnum.APRIL.getCode().equals(entity.getMonth().toString())) {
                        obj.setAprilQtyStr(entity.getSalesQty().toString());
                        obj.setAprilAmountStr(entity.getSalesAmount().stripTrailingZeros().toPlainString());
                    }
                    if (MonthEnum.MAY.getCode().equals(entity.getMonth().toString())) {
                        obj.setMayQtyStr(entity.getSalesQty().toString());
                        obj.setMayAmountStr(entity.getSalesAmount().stripTrailingZeros().toPlainString());
                    }
                    if (MonthEnum.JUNE.getCode().equals(entity.getMonth().toString())) {
                        obj.setJuneQtyStr(entity.getSalesQty().toString());
                        obj.setJuneAmountStr(entity.getSalesAmount().stripTrailingZeros().toPlainString());
                    }
                    if (MonthEnum.JULY.getCode().equals(entity.getMonth().toString())) {
                        obj.setJulyQtyStr(entity.getSalesQty().toString());
                        obj.setJulyAmountStr(entity.getSalesAmount().stripTrailingZeros().toPlainString());
                    }
                    if (MonthEnum.AUGUST.getCode().equals(entity.getMonth().toString())) {
                        obj.setAugustQtyStr(entity.getSalesQty().toString());
                        obj.setAugustAmountStr(entity.getSalesAmount().stripTrailingZeros().toPlainString());
                    }
                    if (MonthEnum.SEPTEMBER.getCode().equals(entity.getMonth().toString())) {
                        obj.setSeptemberQtyStr(entity.getSalesQty().toString());
                        obj.setSeptemberAmountStr(entity.getSalesAmount().stripTrailingZeros().toPlainString());
                    }
                    if (MonthEnum.OCTOBER.getCode().equals(entity.getMonth().toString())) {
                        obj.setOctoberQtyStr(entity.getSalesQty().toString());
                        obj.setOctoberAmountStr(entity.getSalesAmount().stripTrailingZeros().toPlainString());
                    }
                    if (MonthEnum.NOVEMBER.getCode().equals(entity.getMonth().toString())) {
                        obj.setNovemberQtyStr(entity.getSalesQty().toString());
                        obj.setNovemberAmountStr(entity.getSalesAmount().stripTrailingZeros().toPlainString());
                    }
                    if (MonthEnum.DECEMBER.getCode().equals(entity.getMonth().toString())) {
                        obj.setDecemberQtyStr(entity.getSalesQty().toString());
                        obj.setDecemberAmountStr(entity.getSalesAmount().stripTrailingZeros().toPlainString());
                    }
                }
            });
        }
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/exportProductPlan.xlsx";
        String name = "产品规划";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(productPlanExcelDTOS, response, sb.toString(), excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean addRemark(ProductPlanRemarkDTO dto) {
        ProductPlanEntity productPlanEntity = this.getById(dto.getProductPlanId());
        if (ObjectUtils.isEmpty(productPlanEntity)) {
            throw new ServiceException(ApiError.ERROR_95133);
        }
        //新增备注
        ProductPlanRemarkEntity productPlanRemarkEntity = new ProductPlanRemarkEntity();
        BeanMapperUtils.copy(dto,productPlanRemarkEntity);
        return productPlanRemarkService.save(productPlanRemarkEntity);
    }

    @Override
    @Transactional
    public Boolean planDevelopProduct(ProductPlanDevelopDTO dto) {
        ProductPlanEntity productPlanEntity = this.getById(dto.getId());
        if (ObjectUtils.isEmpty(productPlanEntity)) {
            throw new ServiceException(ApiError.ERROR_95134);
        }
        if (StringUtils.isNotBlank(productPlanEntity.getProductId())) {
            throw new ServiceException(ApiError.ERROR_95138);
        }
        //根据产品名称判断是否已经存在开发产品
        ProductInfoEntity productInfoEntity = productInfoService.getByName(dto.getName());
        if (ObjectUtils.isEmpty(productInfoEntity)) {
            ProductDTO productDTO = new ProductDTO();
            BeanMapperUtils.copy(dto,productDTO);
            productDTO.setChargeIds(dto.getChargeIdList());
            //等级编码
            BasicDictEntity grade = basicDictService.getById(dto.getId());
            if (ObjectUtils.isNotEmpty(grade)) {
                productDTO.setGrade(grade.getValue());
            }
            productDTO.setId(null);
            //当需要新增产品时
            String productId = productInfoService.saveOrUpdateProduct(productDTO);
            if (StringUtils.isBlank(productId)) {
                throw new ServiceException(ApiError.ERROR_95139);
            }
            productInfoEntity = productInfoService.getById(productId);
        } else {
            //判断产品是否已关联规划
            ProductPlanEntity found = this.getByProductId(productInfoEntity.getId());
            if (ObjectUtils.isNotEmpty(found)) {
                throw new ServiceException(ApiError.ERROR_95142);
            }
        }
        //存在数据则关联规划并且需要同步的数据以产品的为准
        updateProductPlanByProduct(productPlanEntity,productInfoEntity);
        return Boolean.TRUE;
    }

    @Override
    public void updateProductPlanByProduct(ProductPlanEntity productPlanEntity,ProductInfoEntity productInfoEntity) {
        if (ObjectUtils.isEmpty(productPlanEntity)
                || ObjectUtils.isEmpty(productInfoEntity)
                || StringUtils.isBlank(productPlanEntity.getId())) {
            throw new ServiceException(ApiError.ERROR_95140);
        }
        productPlanEntity.setProductId(productInfoEntity.getId());
        productPlanEntity.setChargeId(productInfoEntity.getChargeId());
        productPlanEntity.setChargeName(productInfoEntity.getChargeName());
        productPlanEntity.setBrandId(productInfoEntity.getBrandId());
        productPlanEntity.setBrandName(productInfoEntity.getBrandName());
        productPlanEntity.setCategoryId(productInfoEntity.getCategoryId());
        productPlanEntity.setCategory(productInfoEntity.getCategory());
        productPlanEntity.setGradeId(productInfoEntity.getGradeId());
        productPlanEntity.setGrade(productInfoEntity.getGrade());
        productPlanEntity.setSpuNo(productInfoEntity.getSpuNo());
        productPlanEntity.setPropertyId(productInfoEntity.getPropertyId());
        productPlanEntity.setProperty(productInfoEntity.getProperty());
        ProductPlanStatusEnum  statusEnum = ProductPlanStatusEnum.getByName(ApprovalStatusEnum.getName(productInfoEntity.getApprovalStatus()));
        if (ObjectUtils.isNotEmpty(statusEnum)) {
            productPlanEntity.setProductStatus(statusEnum.getCode());
        }
        this.updateById(productPlanEntity);
    }

    @Override
    public void relatedProductPlanByProduct(String productPlanId, ProductInfoEntity entity) {
        ProductPlanEntity productPlanEntity = this.getById(productPlanId);
        if (ObjectUtils.isEmpty(productPlanEntity)) {
            throw new ServiceException(ApiError.ERROR_95134);
        }
        if (ObjectUtils.isEmpty(entity.getApprovalStatus())) {
            entity.setApprovalStatus(ApprovalStatusEnum.WAIT.getCode());
        }
        //更新同步规划数据
        updateProductPlanByProduct(productPlanEntity,entity);
    }

    @Override
    public void updateRealDateByProductId(String productId) {
        ProductPlanEntity productPlanEntity = this.getByProductId(productId);
        if (ObjectUtils.isEmpty(productPlanEntity)) {
            return;
        }
        //查询产品信息最后的首批入库时间
        List<ProductDetailEntity> skuList = productDetailService.getSkuListByProductId(productId);
        if (CollectionUtils.isEmpty(skuList)) {
            return;
        }
        List<ProductDetailEntity>  firstMassProductList= skuList .stream().filter(obj -> ObjectUtils.isNotEmpty(obj.getFirstMassProductDate())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(firstMassProductList)) {
            Date firstMassProductDate = firstMassProductList.stream().max(Comparator.comparing(ProductDetailEntity::getFirstMassProductDate))
                    .map(ProductDetailEntity::getFirstMassProductDate).get();
            productPlanEntity.setFirstMassStockInDate(ObjectUtils.isEmpty(firstMassProductDate) ? null : LocalDateUtil.date2LocalDate(firstMassProductDate));
        }
        List<String> skuIds = skuList.stream().map(ProductDetailEntity::getId).collect(Collectors.toList());
        //查询销售信息最后的上市时间
        List<ProductSaleEntity> productSaleList = productSaleService.listBySkuIds(skuIds);
        if (CollectionUtils.isNotEmpty(productSaleList)) {
            productSaleList = productSaleList.stream().filter(obj -> ObjectUtils.isNotEmpty(obj.getListingTime())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(productSaleList)) {
                Date listingTime = productSaleList.stream().max(Comparator.comparing(ProductSaleEntity::getListingTime))
                        .map(ProductSaleEntity::getListingTime).get();
                productPlanEntity.setListingDate(ObjectUtils.isEmpty(listingTime) ? null : LocalDateUtil.date2LocalDate(listingTime));
            }

        }
        this.updateById(productPlanEntity);
    }

    @Override
    public void removeProductId(String productId) {
        LambdaUpdateWrapper<ProductPlanEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ProductPlanEntity::getProductId,"");
        updateWrapper.eq(ProductPlanEntity::getProductId,productId);
        this.update(updateWrapper);
    }

    @Override
    public List<ProductPlanStatisticsVO> listProductPlanStatistics(ProductPlanGroupSerachDTO dto) {
        List<ProductPlanStatisticsVO> reslutList = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDateTime startTime = LocalDateUtil.getThisMonthStart(now);
        LocalDateTime endTime = LocalDateUtil.getThisMonthEnd(now);
        //查询规划总数及本月新增
        Integer count = this.baseMapper.listProductPlanTotalCount(dto, null,null);
        Integer crtCount = this.baseMapper.listProductPlanTotalCount(dto,startTime,endTime);
        setProductPlanStatisticsVO(reslutList,"规划总数",count,"本月新增",crtCount);
        //待开发
        Integer notSurveyCount = this.baseMapper.listProductPlanStatusCount(dto, MathUtil.ONE,null,null);
        setProductPlanStatisticsVO(reslutList,"待开发",notSurveyCount,"",null);
        //已立项
        Integer approvalCount = this.baseMapper.listProductPlanStatusCount(dto, MathUtil.TWO,null,null);
        Integer thisApprovalCount = this.baseMapper.listProductPlanStatusCount(dto, MathUtil.TWO,startTime,endTime);
        setProductPlanStatisticsVO(reslutList,"已立项",approvalCount,"本月立项",thisApprovalCount);
        //已进行中
        Integer handCount = this.baseMapper.listProductPlanStatusCount(dto, MathUtil.THREE,null,null);
        Integer thisHandCount = this.baseMapper.listProductPlanStatusCount(dto, MathUtil.THREE,startTime,endTime);
        setProductPlanStatisticsVO(reslutList,"进行中",handCount,"本月进行中",thisHandCount);
        //已完成
        Integer completeCount = this.baseMapper.listProductPlanStatusCount(dto, MathUtil.FOUR,null,null);
        Integer thisCompleteCount = this.baseMapper.listProductPlanStatusCount(dto, MathUtil.FOUR,startTime,endTime);
        setProductPlanStatisticsVO(reslutList,"已完成",completeCount,"本月已完成",thisCompleteCount);
        //立项延期
        Integer deferCount = this.baseMapper.listProductPlanStatusCount(dto, MathUtil.SIX,null,null);
        Integer thisDeferCount = this.baseMapper.listProductPlanStatusCount(dto, MathUtil.SIX,startTime,endTime);
        setProductPlanStatisticsVO(reslutList,"立项延期",deferCount,"本月延期数",thisDeferCount);
        //已取消
        Integer cacelCount = this.baseMapper.listProductPlanStatusCount(dto, MathUtil.FIVE,null,null);
        Integer thisCacelCount = this.baseMapper.listProductPlanStatusCount(dto, MathUtil.FIVE,startTime,endTime);
        setProductPlanStatisticsVO(reslutList,"已取消",cacelCount,"本月已取消",thisCacelCount);
        return reslutList;
    }



    @Override
    public List<ProductPlanGroupVO> listTableChargeName(ProductPlanGroupSerachDTO dto) {
        //产品经理
        dto.setGroupField("charge_id");
        dto.setGroupFields("charge_id,charge_name");
        List<ProductPlanGroupVO> list = baseMapper.listProductPlanGroupTable(dto);
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        List<String> chargeList = list.stream().map(ProductPlanGroupVO::getChargeId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(chargeList)) {
            return list;
        }
        List<SysUserDeptDTO> userDeptList = sysUserFeign.getUserDeptList();
        if (CollectionUtils.isEmpty(userDeptList)) {
            return list;
        }
        for (ProductPlanGroupVO productPlanGroupVO: list) {
            String deptNames = userDeptList.stream().filter(obj -> productPlanGroupVO.getChargeId().equals(obj.getUid())).map(SysUserDeptDTO::getDeptName).collect(Collectors.joining(","));
            productPlanGroupVO.setDeptName(deptNames);
        }
        return list;
    }

    @Override
    public List<ProductPlanGroupVO> listTableGrade(ProductPlanGroupSerachDTO dto) {
        //产品等级
        dto.setGroupField("grade_id");
        dto.setGroupFields("grade_id,grade");
        return baseMapper.listProductPlanGroupTable(dto);
    }

    @Override
    public List<ProductPlanGroupVO> listTableCategory(ProductPlanGroupSerachDTO dto) {
        //产品分类
        dto.setGroupField("category_id");
        dto.setGroupFields("category_id,category");
        return baseMapper.listProductPlanGroupTable(dto);
    }


    @Override
    public List<SeriesVO> listApprovalTrend(ProductPlanGroupSerachDTO dto) {
        List<SeriesVO> resultList = new ArrayList<>();
        MonthEnum[] values = MonthEnum.values();
        Integer year = dto.getYear();
        if (ObjectUtils.isEmpty(dto.getYear())) {
            throw new ServiceException(ApiError.ERROR_95141);
        }
        LocalDate localDate = LocalDateTimeUtil.parseDate(String.valueOf(year), "yyyy");
        //所选年份的
        LocalDateTime thisYearStart = LocalDateUtil.getThisYearStart(localDate);
        LocalDateTime thisYearEnd = LocalDateUtil.getThisYearEnd(localDate);
        //立项数量
        List<ProductPlanApprovalTrendDTO> approvalList = baseMapper.countApprovalTrend(dto, thisYearStart, thisYearEnd);
        //完成数量
        List<ProductPlanApprovalTrendDTO> completeList = baseMapper.countCompleteTrend(dto, thisYearStart, thisYearEnd);
        List<String> monthList = new ArrayList<>();
        List<Integer> approvalCountList = new ArrayList<>();
        List<Integer> completeCountList = new ArrayList<>();
        for (MonthEnum monthEnum:values) {
            //立项数量
            Integer approvalCount = MathUtil.ZERO;
            //完成数量
            Integer completeCount = MathUtil.ZERO;
            monthList.add(monthEnum.getCode().concat("月份"));
            if (CollectionUtils.isNotEmpty(approvalList)){
                 approvalCount = approvalList.stream().filter(obj -> (year + monthEnum.getCode()).equals(String.valueOf(obj.getMonth()))).map(ProductPlanApprovalTrendDTO::getApprovalCount).findFirst().orElse(0);
            }
            if (CollectionUtils.isNotEmpty(completeList)){
                 completeCount = completeList.stream().filter(obj -> (year + monthEnum.getCode()).equals(String.valueOf(obj.getMonth()))).map(ProductPlanApprovalTrendDTO::getCompleteCount).findFirst().orElse(0);
            }
            approvalCountList.add(approvalCount);
            completeCountList.add(completeCount);
        }
        SeriesVO seriesVO1 = new SeriesVO();
        seriesVO1.setName("月份");
        seriesVO1.setData(monthList);
        resultList.add(seriesVO1);

        SeriesVO seriesVO2 = new SeriesVO();
        seriesVO2.setName("立项数量");
        seriesVO2.setData(approvalCountList);
        resultList.add(seriesVO2);

        SeriesVO seriesVO3 = new SeriesVO();
        seriesVO3.setName("完成数量");
        seriesVO3.setData(completeCountList);
        resultList.add(seriesVO3);
        return resultList;
    }


    @Override
    public Boolean uploadImageUrl(MultipartFile multipartFiles, String id) {
        ProductPlanEntity productPlanEntity = this.getById(id);
        if (ObjectUtils.isEmpty(productPlanEntity)) {
            throw new ServiceException(ApiError.ERROR_95133);
        }
        String filePath = FastDFSClientUtil.uploadFile(multipartFiles);
        productPlanEntity.setImageUrl(filePath);
        return this.updateById(productPlanEntity);
    }



    @Override
    public ProductPlanEntity getByYearAndName(Integer year, String name) {
        LambdaQueryWrapper<ProductPlanEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductPlanEntity::getYear,year);
        queryWrapper.eq(ProductPlanEntity::getName,name);
        return this.getOne(queryWrapper);
    }

    @Override
    public void updateProductPlanStatus(String productId, Integer status, Integer type) {
        ProductPlanEntity productPlanEntity = this.getByProductId(productId);
        if (ObjectUtils.isEmpty(productPlanEntity)) {
            return;
        }
        //产品状态
        if (MathUtil.ONE.equals(type)) {
            //未开始
            if (ApprovalStatusEnum.WAIT.getCode().equals(status)) {
                productPlanEntity.setProductStatus(ProductPlanStatusEnum.WAIT.getCode());
            }
            //调研中
            if (ApprovalStatusEnum.PROBE.getCode().equals(status)) {
                productPlanEntity.setProductStatus(ProductPlanStatusEnum.PROBE.getCode());
                productPlanEntity.setSurveyDate(LocalDate.now());
            }
            //已立项
            if (ApprovalStatusEnum.APPROVAL.getCode().equals(status)) {
                productPlanEntity.setProductStatus(ProductPlanStatusEnum.APPROVAL.getCode());
                productPlanEntity.setProjectApprovalDate(LocalDate.now());
            }
            //已中止
            if (ApprovalStatusEnum.TERMINATE.getCode().equals(status)) {
                productPlanEntity.setProductStatus(ProductPlanStatusEnum.CANCEL.getCode());
            }
        }
        //项目状态
        if (MathUtil.TWO.equals(type)) {
            //启动
            if (ProjectStateEnum.YES_START.getState().equals(status)) {
                productPlanEntity.setProductStatus(ProductPlanStatusEnum.YES_START.getCode());
            }
            //进行中
            if (ProjectStateEnum.ING.getState().equals(status)) {
                productPlanEntity.setProductStatus(ProductPlanStatusEnum.ING.getCode());
            }
            //完成
            if (ProjectStateEnum.FINISH.getState().equals(status)) {
                productPlanEntity.setProductStatus(ProductPlanStatusEnum.FINISH.getCode());
            }
            //中止
            if (ProjectStateEnum.STOP.getState().equals(status)) {
                productPlanEntity.setProductStatus(ProductPlanStatusEnum.CANCEL.getCode());
            }
        }
        this.updateById(productPlanEntity);
    }

    @Override
    public List<SelectShowDTO> listNotRelatedProductPlan() {
        List<SelectShowDTO> resultList = new ArrayList<>();
        //查询所有未关联产品的规划
        List<ProductPlanEntity> list = this.listAllNotRelatedProductPlan();
        if (CollectionUtils.isEmpty(list)) {
            return  resultList;
        }
        list.forEach(obj -> {
            resultList.add(new SelectShowDTO(null,obj.getId(),obj.getName()));
        });
        return resultList;
    }

    /**
     * @description: 删除规划
     * @author Will
     * @date: 2023/2/21 18:00
     * @param productPlanId
     * @return Boolean
     */
    private Boolean removeByProductPlanId(String productPlanId) {
        //获取当前登录人
        LoginUser userInfo = commonService.getUserInfo();
        if (ObjectUtils.isEmpty(userInfo)) {
            throw new ServiceException(ApiError.ERROR_9011);
        }
        LambdaUpdateWrapper<ProductPlanEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProductPlanEntity::getId,productPlanId);
        updateWrapper.set(ProductPlanEntity::getIsDeleted, Boolean.TRUE);
        updateWrapper.set(ProductPlanEntity::getDeletedTime, LocalDateTime.now());
        updateWrapper.set(ProductPlanEntity::getDeletedUserId,userInfo.getUid());
        return this.update(updateWrapper);
    }

    /**
     * @description:设置进度
     * @author Will
     * @date: 2023/2/21 19:57
     * @param productPlanEntity
     * @param progressList
     * @param typeName
     */
    private void setProductPlanProgress(ProductPlanEntity productPlanEntity,List<ProductPlanProgressDTO> progressList,String typeName) {
        ProductPlanProgressDTO productPlanProgressDTO = new ProductPlanProgressDTO();
        productPlanProgressDTO.setTypeName(typeName);
        productPlanProgressDTO.setIsComplete(Boolean.FALSE);
        if (ProductPlanProcessEnum.NEW_PRODUCT_PLAN.getName().equals(typeName)) {
            productPlanProgressDTO.setStartTime(LocalDateTimeUtil.format(productPlanEntity.getCreateTime(),DateUtil.DATE_TIME_PATTERN_NO_SEC));
            productPlanProgressDTO.setUserName(productPlanEntity.getCreateUserName());
            productPlanProgressDTO.setIsComplete(Boolean.TRUE);
            progressList.add(productPlanProgressDTO);
            return;
        }
        String productId = productPlanEntity.getProductId();
        if (StringUtils.isNotBlank(productId)) {
            //已关联产品
            ProductInfoEntity productInfoEntity = productInfoService.getById(productId);
            if (ObjectUtils.isEmpty(productInfoEntity)) {
                throw new ServiceException(ApiError.ERROR_95010);
            }
            if (ProductPlanProcessEnum.PRODUCT_DEVELOP.getName().equals(typeName)) {
                //转开发
                productPlanProgressDTO.setIsComplete(Boolean.TRUE);
                productPlanProgressDTO.setStartTime(DateUtils.format(productInfoEntity.getCreateTime(), DateUtil.DATE_TIME_PATTERN_NO_SEC));
                productPlanProgressDTO.setUserName(productInfoEntity.getCreateUserName());
                progressList.add(productPlanProgressDTO);
                return;
            }
            //已生成项目
            ProjectInfoEntity projectInfoEntity = projectInfoService.getByProductId(productId);
            if (ObjectUtils.isEmpty(projectInfoEntity) || ProjectStateEnum.NOT_START.getState().equals(projectInfoEntity.getProjectStatus())) {
                progressList.add(productPlanProgressDTO);
                return;
            }
            if (ProductPlanProcessEnum.PROJECT_STARTUP.getName().equals(typeName)) {
                //项目已启动
                ProjectStatusTimeEntity projectStatusTimeEntity = projectStatusTimeService.getByProjectIdAndProjectStatus(projectInfoEntity.getId(), ProjectStateEnum.YES_START.getState());
                if (ObjectUtils.isNotEmpty(projectStatusTimeEntity)) {
                    productPlanProgressDTO.setIsComplete(Boolean.TRUE);
                    productPlanProgressDTO.setStartTime(LocalDateTimeUtil.format(projectStatusTimeEntity.getStatusTime(), DateUtil.DATE_TIME_PATTERN_NO_SEC));
                    productPlanProgressDTO.setUserName(projectStatusTimeEntity.getCreateUserName());
                }
            }
            if (ProductPlanProcessEnum.PROJECT_COMPLETE.getName().equals(typeName)) {
                //项目已完成
                ProjectStatusTimeEntity projectStatusTimeEntity = projectStatusTimeService.getByProjectIdAndProjectStatus(projectInfoEntity.getId(), ProjectStateEnum.FINISH.getState());
                if (ObjectUtils.isNotEmpty(projectStatusTimeEntity)) {
                    productPlanProgressDTO.setIsComplete(Boolean.TRUE);
                    productPlanProgressDTO.setStartTime(LocalDateTimeUtil.format(projectStatusTimeEntity.getStatusTime(), DateUtil.DATE_TIME_PATTERN_NO_SEC));
                    productPlanProgressDTO.setUserName(projectStatusTimeEntity.getCreateUserName());
                }
            }
        }
        progressList.add(productPlanProgressDTO);
    }

    /**
     * 指标数量
     */
    private void setProductPlanStatisticsVO(List<ProductPlanStatisticsVO> resultList,String describe,Integer totalCount,String thisDescribe,Integer thisMonthCount) {
        ProductPlanStatisticsVO productPlanStatisticsVO = new ProductPlanStatisticsVO();
        productPlanStatisticsVO.setDescribe(describe);
        productPlanStatisticsVO.setTotalCount(totalCount == null ? MathUtil.ZERO :totalCount);
        productPlanStatisticsVO.setThisDescribe(thisDescribe);
        if ("未调研".equals(describe)) {
            productPlanStatisticsVO.setThisMonthCount(null);
        } else {
            productPlanStatisticsVO.setThisMonthCount(thisMonthCount == null ? MathUtil.ZERO : thisMonthCount);
        }
        resultList.add(productPlanStatisticsVO);
    }

    /**
     * 根据产品id查询
     */
    private ProductPlanEntity getByProductId(String productId) {
        LambdaQueryWrapper<ProductPlanEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductPlanEntity::getProductId,productId);
        return this.getOne(queryWrapper);
    }

    /**
     * 查询所有未关联产品的规划
     */
    private List<ProductPlanEntity> listAllNotRelatedProductPlan() {
        LambdaQueryWrapper<ProductPlanEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductPlanEntity::getProductId,"");
        return this.list(queryWrapper);
    }
}
