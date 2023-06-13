package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperServiceImpl;
import com.erp.model.scm.dto.PurchaseBusinessGatherTableDTO;
import com.erp.model.scm.dto.SalesDemandDTO;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.SubcontractOrderEntity;
import com.erp.server.scm.mapper.ReportFormsManageMapper;
import com.erp.server.scm.mapper.SubcontractOrderMapper;
import com.erp.server.scm.service.ReportFormsManageService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 报表管理
 * @Author Luo_WG
 * @Date 2023/6/12 17:34
 **/
@Service
public class ReportFormsManageServiceImpl extends SuperServiceImpl<ReportFormsManageMapper, PurchaseOrderEntity> implements ReportFormsManageService {

    @Override
    public List<PurchaseBusinessGatherTableDTO.PagingViewDTO> purchaseBusinessGatherTablPaginge(PagingDTO<PurchaseBusinessGatherTableDTO.PagingParamDTO> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<PurchaseBusinessGatherTableDTO.PagingViewDTO> pageData = baseMapper.paging(query, pagingDTO.getParams());
        return null;
    }
}
