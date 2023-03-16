package com.erp.server.scm.service.impl;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.serveice.SuperServiceImpl;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.entity.ScmPurchaseOrderEntity;
import com.erp.server.scm.mapper.ScmPurchaseOrderMapper;
import com.erp.server.scm.service.ScmPurchaseOrderService;
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
public class ScmPurchaseOrderServiceImpl extends SuperServiceImpl<ScmPurchaseOrderMapper, ScmPurchaseOrderEntity> implements ScmPurchaseOrderService {

    @Override
    public PagingVO<List<ScmPurchaseOrderPagingViewDTO>> paging(PagingDTO<ScmPurchaseOrderPagingParamDTO> dto) {
        return null;
    }

    @Override
    public String getCode() {
        return null;
    }

    @Override
    public Boolean add(ScmPurchaseOrderDTO scmPurchaseOrderDTO) {
        return null;
    }

    @Override
    public Boolean update(ScmPurchaseOrderDTO scmPurchaseOrderDTO) {
        return null;
    }

    @Override
    public ScmPurchaseOrderDTO view(String id) {
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
    public List<ScmPurchaseOrderViewDTO> viewForWarehouseReceive(String id) {
        return null;
    }

    @Override
    public Boolean generateWarehouseReceive(ScmPurchaseOrderViewDTO scmPurchaseOrderViewDTO) {
        return null;
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }

    @Override
    public Boolean exportExcel(ScmPurchaseOrderPagingParamDTO scmPurchaseOrderPagingParamDTO, HttpServletResponse response) {
        return null;
    }
}
