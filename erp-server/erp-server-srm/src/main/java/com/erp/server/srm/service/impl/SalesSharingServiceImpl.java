package com.erp.server.srm.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.enums.SaleStateEnum;
import com.erp.model.srm.entity.SalesSharingEntity;
import com.erp.model.sys.vo.SupplierUserVO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.srm.mapper.SalesSharingMapper;
import com.erp.server.srm.service.SalesSharingService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.srm.dto.SalesSharingDTO;

import java.io.IOException;
import java.util.*;

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
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SalesSharingDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        return new PagingVO(pageData);
    }


    @Override
    public void exportList(SalesSharingDTO.PagingParamDTO pagingDTO ,HttpServletResponse response) {
//        downloadTaskFeign.saveDownloadTask("销量共享导出", EXPORT_SRM_SALES_SHARING_REPORT.getCode(), pagingParamDTO);
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
        }


    }


}
