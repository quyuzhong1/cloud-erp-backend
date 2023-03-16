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
    public PagingVO<List<PurchaseOrderPagingViewDTO>> paging(PagingDTO<PurchaseOrderPagingParamDTO> dto) {
        return null;
    }

    @Override
    public String getCode() {
        return null;
    }

    @Override
    public Boolean add(PurchaseOrderDTO scmPurchaseOrderDTO) {
        return null;
    }

    @Override
    public Boolean update(PurchaseOrderDTO scmPurchaseOrderDTO) {
        return null;
    }

    @Override
    public PurchaseOrderDTO view(String id) {
        return null;
    }

    @Override
    public Boolean delete(String id) {
        return null;
    }

    @Override
    public void approve(BaseApproveParamDTO baseApproveParamDTO) {

    }

    @Override
    public Boolean unApprove(List<String> ids) {
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
    public Boolean copy(String id) {
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
    public List<PurchaseOrderViewDTO> viewForWarehouseReceive(String id) {
        return null;
    }

    @Override
    public Boolean generateWarehouseReceive(PurchaseOrderViewDTO purchaseOrderViewDTO) {
        return null;
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }

    @Override
    public Boolean exportExcel(PurchaseOrderPagingParamDTO purchaseOrderPagingParamDTO, HttpServletResponse response) {
        return null;
    }
}
