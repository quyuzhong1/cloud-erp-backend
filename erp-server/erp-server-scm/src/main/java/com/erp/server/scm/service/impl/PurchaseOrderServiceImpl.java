package com.erp.server.scm.service.impl;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.serveice.SuperServiceImpl;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.server.scm.mapper.PurchaseOrderMapper;
import com.erp.server.scm.service.PurchaseOrderService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 采购订单表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Service
public class PurchaseOrderServiceImpl extends SuperServiceImpl<PurchaseOrderMapper, PurchaseOrderEntity> implements PurchaseOrderService {

    @Override
    public PagingVO<PurchaseOrderDTO.listDTO> paging(PagingDTO<PurchaseOrderDTO.searchParamDTO> dto) {
        return null;
    }

    @Override
    public Boolean add(PurchaseOrderDTO.addDTO dto) {
        return null;
    }

    @Override
    public Boolean update(PurchaseOrderDTO.updateDTO dto) {
        return null;
    }

    @Override
    public PurchaseOrderDTO.viewDTO view(String id) {
        return null;
    }

    @Override
    public Boolean delete(List<String> ids) {
        return null;
    }

    @Override
    public void approve(BaseApproveParamDTO baseApproveParamDTO) {

    }

    @Override
    public Boolean disApprove(List<String> ids) {
        return null;
    }

    @Override
    public Boolean cancelProcess(String id) {
        return null;
    }

    @Override
    public Boolean finishDelivery(String id) {
        return null;
    }
    @Override
    public Boolean purchaseChange(String id) {
        return null;
    }

    @Override
    public Boolean exportPurchaseContractPdf(String id) {
        return null;
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }

    @Override
    public Boolean exportExcel(PurchaseOrderDTO.searchParamDTO dto, HttpServletResponse response) {
        return null;
    }

    @Override
    public Boolean submit(List<String> ids) {
        return null;
    }

    @Override
    public Boolean addAndSubmit(PurchaseOrderDTO.addDTO dto) {
        return null;
    }
}
