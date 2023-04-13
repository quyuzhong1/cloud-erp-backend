package com.erp.server.wms.service.impl;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.PurchaseStockInDTO;
import com.erp.model.wms.entity.PurchaseStockInEntity;
import com.erp.server.wms.mapper.PurchaseStorageMapper;
import com.erp.server.wms.service.PurchaseStockInService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 采购入库单 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-04-10
 */
@Service
public class PurchaseStockInServiceImpl extends SuperServiceImpl<PurchaseStorageMapper, PurchaseStockInEntity> implements PurchaseStockInService {

    @Override
    public PagingVO<PurchaseStockInDTO.ListDTO> paging(PagingDTO<PurchaseStockInDTO.SearchParamDTO> dto) {
        return null;
    }

    @Override
    public List<PurchaseStockInDTO.ListStatusCountDTO> listCount(PermissionsDTO dto) {
        return null;
    }

    @Override
    public String add(PurchaseStockInDTO.AddDTO dto) {
        return null;
    }

    @Override
    public Boolean addAndSubmit(PurchaseStockInDTO.AddDTO dto) {
        return null;
    }

    @Override
    public Boolean update(PurchaseStockInDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public Boolean updateAndSubmit(PurchaseStockInDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public Boolean submit(BaseIdsDTO.IdsDTO dto) {
        return null;
    }

    @Override
    public PurchaseStockInDTO.ViewDTO view(String id) {
        return null;
    }

    @Override
    public Boolean delete(List<String> ids) {
        return null;
    }

    @Override
    public Boolean invalid(List<String> ids, String remark) {
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
    public Boolean cancelProcess(List<String> ids) {
        return null;
    }

    @Override
    public Boolean exportExcel(PurchaseStockInDTO.SearchParamDTO dto, HttpServletResponse response) {
        return null;
    }

    @Override
    public List<PurchaseStockInDTO.ViewGeneratePurchaseReturnOrderDTO> viewGeneratePurchaseReturnOrder(String id) {
        return null;
    }

    @Override
    public Boolean generatePurchaseReturnOrder(PurchaseStockInDTO.GeneratePurchaseReturnOrderDTO dto) {
        return null;
    }
}
