package com.erp.server.srm.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.entity.PoReconciliationDetailEntity;
import com.erp.server.srm.mapper.PoReconciliationDetailMapper;
import com.erp.server.srm.service.CommonService;
import com.erp.server.srm.service.PoReconciliationDetailScmService;
import com.erp.server.srm.service.PoReconciliationDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 采购对账单明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-01-19
 */
@Slf4j
@Service
public class PoReconciliationDetailServiceImpl extends SuperServiceImpl<PoReconciliationDetailMapper, PoReconciliationDetailEntity> implements PoReconciliationDetailService {

    @Autowired
    private PoReconciliationDetailScmService poReconciliationDetailScmService;

    @Autowired
    private CommonService commonService;



    @Override
    public PagingVO<PoReconciliationDetailDTO.ListDTO> paging(PagingDTO<PoReconciliationDetailDTO.PagingParamDTO> pagingParamDTO) {
        //默认查询当前登录人的绑定的供应商数据
        SupplierEntity supplierEntity = commonService.getSupplierEntity();
        pagingParamDTO.getParams().setSupplierId(supplierEntity.getId());
        pagingParamDTO.getParams().setIsSrm(Boolean.TRUE);
        return poReconciliationDetailScmService.paging(pagingParamDTO);
    }

    @Override
    public void exportList(PoReconciliationDetailDTO.PagingParamDTO dto, HttpServletResponse response) {
        //默认查询当前登录人的绑定的供应商数据
        SupplierEntity supplierEntity = commonService.getSupplierEntity();
        dto.setSupplierId(supplierEntity.getId());

        List<PoReconciliationDetailDTO.ListDTO> list = this.baseMapper.listExport(dto);
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 数据处理
        poReconciliationDetailScmService.fillList(list,Boolean.TRUE);
        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/poReconciliationDetail.xlsx";
        String name = "对账明细导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }
}
