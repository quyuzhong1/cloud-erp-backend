package com.erp.server.plm.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.common.business.dto.base.BaseIdDTO;
import com.erp.common.business.dto.base.PagingDTO;
import com.erp.common.business.vo.PagingVO;
import com.erp.common.business.vo.SeriesVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.dto.excel.ProductPlanExcelDTO;
import com.erp.model.plm.entity.ProductPlanEntity;
import com.erp.model.plm.vo.ProductPlanGroupVO;
import com.erp.model.plm.vo.ProductPlanRemarkVO;
import com.erp.model.plm.vo.ProductPlanStatisticsVO;
import com.erp.model.plm.vo.ProductPlanVO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.listener.ProductPlanExcelListener;
import com.erp.server.plm.mapper.ProductPlanMapper;
import com.erp.server.plm.service.ProductPlanService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

    @Override
    public PagingVO<List<ProductPlanVO>> paging(PagingDTO<ProductPlanSearchDTO> dto) {
        return null;
    }

    @Override
    public ProductPlanDetailsDTO productPlanDetails(String id) {
        return null;
    }

    @Override
    public Boolean deleteById(String id) {
        return null;
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
        return null;
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
    public List<ProductPlanGroupVO> listProductPlanTable(ProductPlanGroupSerachDTO dto) {
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
}
