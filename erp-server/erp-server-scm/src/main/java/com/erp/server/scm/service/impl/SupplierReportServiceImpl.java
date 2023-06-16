package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.SupplierReportDTO;
import com.erp.server.scm.mapper.SupplierReportMapper;
import com.erp.server.scm.service.SupplierReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 供应商相关报表服务实现类
 * @CreateTime: 2023-06-16  16:49
 * @Author: zhangchunlin
 */
@Service
public class SupplierReportServiceImpl implements SupplierReportService {

    @Autowired
    private SupplierReportMapper supplierReportMapper;

    @Override
    public PagingVO<SupplierReportDTO.PagingViewDTO> supplierPaging(PagingDTO<SupplierReportDTO.PagingSearchParamDTO> paramDTO) {
        paramDTO.getParams().setPermissionSql(paramDTO.getPermissionSql());
        Page query = new Page(paramDTO.getCurrPage(), paramDTO.getPageSize());
        IPage<SupplierReportDTO.PagingViewDTO> pageData = supplierReportMapper.getPurchasePaging(query, paramDTO.getParams());
        return new PagingVO(pageData);
    }

}