package com.erp.server.plm.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.alibaba.excel.EasyExcel;
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
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.dto.excel.ProductPlanExcelDTO;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.*;
import com.erp.model.plm.vo.ProductPlanGroupVO;
import com.erp.model.plm.vo.ProductPlanStatisticsVO;
import com.erp.model.plm.vo.ProductPlanVO;
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

        //查询采购信息
        ProductPlanPurchaseEntity productPlanPurchaseEntity = productPlanPurchaseService.getByProductPlanId(id);
        if (ObjectUtils.isEmpty(productPlanPurchaseEntity)) {
            throw new ServiceException(ApiError.ERROR_95134);
        }
        ProductPlanPurchaseDTO productPlanPurchaseDTO = new ProductPlanPurchaseDTO();
        BeanMapperUtils.copy(productPlanPurchaseEntity,productPlanPurchaseDTO);
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
            int year = LocalDate.now().getYear();
            //最新年份的
            saleInfoList = saleInfoList.stream().filter(e -> e.getYear().equals(Integer.valueOf(year))).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(saleInfoList)) {
                List<ProductPlanSaleInfoDTO> list = BeanMapperUtils.copyList(ProductPlanSaleInfoDTO.class,saleInfoList);
                Integer totalQty = list.stream().map(ProductPlanSaleInfoDTO::getSalesQty).reduce(Integer::sum).get();
                BigDecimal totalAmount = list.stream().map(ProductPlanSaleInfoDTO::getSalesAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                resultDTO.setTotalQty(totalQty);
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
        String excelPath = "excel/productPlan.xlsx";
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
            //当需要新增产品时
            String productId = productInfoService.saveOrUpdateProduct(productDTO);
            if (StringUtils.isBlank(productId)) {
                throw new ServiceException(ApiError.ERROR_95139);
            }
            productInfoEntity = productInfoService.getById(productId);
        }
        //存在数据则关联规划并且需要同步的数据以产品的为准
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
        return  this.updateById(productPlanEntity);
    }


    @Override
    public List<ProductPlanStatisticsVO> listProductPlanStatistics(ProductPlanGroupSerachDTO dto) {
        return null;
    }

    @Override
    public List<ProductPlanGroupVO> listTableChargeName(ProductPlanGroupSerachDTO dto) {
        return null;
    }

    @Override
    public List<ProductPlanGroupVO> listTableGrade(ProductPlanGroupSerachDTO dto) {
        return null;
    }

    @Override
    public List<ProductPlanGroupVO> listTableCategory(ProductPlanGroupSerachDTO dto) {
        return null;
    }


    @Override
    public List<SeriesVO> listApprovalTrend(ProductPlanGroupSerachDTO dto) {
        return null;
    }

    @Override
    public Boolean uploadImageUrl(ProductPlanImageDTO dto) {
        return null;
    }

    @Override
    public ProductPlanEntity getByYearAndName(Integer year, String name) {
        LambdaQueryWrapper<ProductPlanEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductPlanEntity::getYear,year);
        queryWrapper.eq(ProductPlanEntity::getName,name);
        return this.getOne(queryWrapper);
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
        updateWrapper.set(ProductPlanEntity::getDeletedUserName,userInfo.getUserName());
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
            ProjectInfoEntity projectInfoEntity = projectInfoService.getById(productId);
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

}
