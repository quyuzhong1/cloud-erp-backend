package com.erp.server.srm.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.enums.SaleStateEnum;
import com.erp.model.scm.entity.CfgSupplierSalesEntity;
import com.erp.model.scm.entity.SupplierRefUserEntity;
import com.erp.model.scm.enums.CfgSupplierSalesPermissionEnum;
import com.erp.model.srm.entity.SalesSharingEntity;
import com.erp.model.sys.vo.SupplierUserVO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.srm.mapper.SalesSharingMapper;
import com.erp.server.srm.service.SalesSharingService;
import com.common.business.service.impl.SuperServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.srm.dto.SalesSharingDTO;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SRM_SALES_SHARING_REPORT;

/**
 * <p>
 * 销量共享表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-06-18
 */
@Slf4j
@Service
public class SalesSharingServiceImpl extends SuperServiceImpl<SalesSharingMapper, SalesSharingEntity> implements SalesSharingService {


    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Override
    public PagingVO<SalesSharingDTO.ListDTO> paging(PagingDTO<SalesSharingDTO.PagingParamDTO> pagingParamDTO) {
        //获取供应商id
        List<String> supplierIds = getSupplierIds();
        if(CollUtil.isEmpty(supplierIds)){
            return new PagingVO(new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize()));
        }

        //拼接供应商id
        SalesSharingDTO.PagingParamDTO params = pagingParamDTO.getParams();
        Map<String, String> sqlMap = params.getSqlMap();
        String strSql = sqlMap.get("default");
        strSql = strSql + " and  ss.supplier_id = '"+supplierIds.get(0)+"' ";
        sqlMap.put("default", strSql);
        params.setSqlMap(sqlMap);

        params.setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SalesSharingDTO.ListDTO> pageData = this.baseMapper.paging(query, params);
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        return new PagingVO(pageData);
    }

    //获取供应商ids
    private static List<String> getSupplierIds() {
        LoginUser login = UserContext.getDefaultLoginUser();

        String uid = login.getUid();

        List<SupplierRefUserEntity> supplierRefUserList = FeignQuery.create(SupplierRefUserEntity.class)
                .eq(SupplierRefUserEntity::getUid, uid)
                .eq(SupplierRefUserEntity::getDisabled, Boolean.FALSE)
                .eq(SupplierRefUserEntity::getIsDeleted, Boolean.FALSE)
                .list();

        return supplierRefUserList.stream().map(SupplierRefUserEntity::getSupplierId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }


    @Override
    public String exportList(SalesSharingDTO.PagingParamDTO pagingDTO ,HttpServletResponse response) {
//        downloadTaskFeign.saveDownloadTask("销量共享导出", EXPORT_SRM_SALES_SHARING_REPORT.getCode(), pagingParamDTO);
        //获取供应商id
        List<String> supplierIds = getSupplierIds();
        if(CollUtil.isEmpty(supplierIds)){
            return "供应商信息不存在";
        }

        //获取供应商的销量设置信息
        List<CfgSupplierSalesEntity> cfgSupplierSalesList = FeignQuery.create(CfgSupplierSalesEntity.class)
                .eq(CfgSupplierSalesEntity::getSupplierId, supplierIds.get(0))
                .eq(CfgSupplierSalesEntity::getIsDeleted, Boolean.FALSE)
                .list();
        if(CollUtil.isEmpty(cfgSupplierSalesList)){
            return "销量设置信息不存在";
        }

        String permission = cfgSupplierSalesList.get(0).getPermission();
        if(!Objects.equals(permission, CfgSupplierSalesPermissionEnum.DOWNLOAD.getCode())){
            return "仅查看数据";
        }

        List<SalesSharingDTO.ListDTO> list = this.baseMapper.listByParams( pagingDTO);
        String name = "销量共享列表";
        StringBuffer sb = new StringBuffer();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        String excelPath = "excel/salesSharingExport.xlsx";
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("销量共享列表导出出错 >>>>>{}", e);
            return "销量共享列表导出出错";
        }

        return "";
    }

}
