package com.erp.server.plm.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.util.DateUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.common.business.dto.base.BaseIdDTO;
import com.erp.common.business.dto.base.PagingDTO;
import com.erp.common.business.vo.LoginUser;
import com.erp.common.business.vo.PagingVO;
import com.erp.common.business.vo.SeriesVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.dto.excel.ProductPlanExcelDTO;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.ProductPlanProcessEnum;
import com.erp.model.plm.enums.ProjectStateEnum;
import com.erp.model.plm.vo.ProductPlanGroupVO;
import com.erp.model.plm.vo.ProductPlanRemarkVO;
import com.erp.model.plm.vo.ProductPlanStatisticsVO;
import com.erp.model.plm.vo.ProductPlanVO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.constant.IsConstant;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    @Override
    public PagingVO<List<ProductPlanVO>> paging(PagingDTO<ProductPlanSearchDTO> pagingDTO) {
        pagingDTO.getParams().setParam(pagingDTO.getParam());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<ProductPlanVO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
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

        //查询备注信息
       List<ProductPlanRemarkEntity> remarkList = productPlanRemarkService.listByProductPlanId(id);
        if (CollectionUtils.isNotEmpty(remarkList)) {
            List<ProductPlanRemarkDTO> list = BeanMapperUtils.copyList(ProductPlanRemarkDTO.class,remarkList);
            resultDTO.setRemarkList(list);
        }

        //查询销售数据信息
        List<ProductPlanSaleInfoEntity> saleInfoList = productPlanSaleInfoService.listByProductPlanId(id);
        if (CollectionUtils.isNotEmpty(saleInfoList)) {
            List<ProductPlanSaleInfoDTO> list = BeanMapperUtils.copyList(ProductPlanSaleInfoDTO.class,saleInfoList);
            resultDTO.setSaleInfoList(list);
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
        ProductPlanExcelListener excelListenerUtil = new ProductPlanExcelListener(this, sysUserFeign);
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
        return null;
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
    public List<ProductPlanRemarkVO> listRemark(BaseIdDTO dto) {
        return null;
    }

    @Override
    public Boolean planDevelopProduct(ProductPlanDevelopDTO dto) {
        return null;
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
        updateWrapper.eq(ProductPlanEntity::getProductId,productPlanId);
        updateWrapper.set(ProductPlanEntity::getIsDeleted, IsConstant.YES);
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
