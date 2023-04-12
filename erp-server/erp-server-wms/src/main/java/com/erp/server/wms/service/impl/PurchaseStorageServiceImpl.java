package com.erp.server.wms.service.impl;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.PurchaseStorageDTO;
import com.erp.model.wms.entity.PurchaseStorageEntity;
import com.erp.server.wms.mapper.PurchaseStorageMapper;
import com.erp.server.wms.service.PurchaseStorageService;
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
public class PurchaseStorageServiceImpl extends SuperServiceImpl<PurchaseStorageMapper, PurchaseStorageEntity> implements PurchaseStorageService {

    @Override
    public PagingVO<PurchaseStorageDTO.ListDTO> paging(PagingDTO<PurchaseStorageDTO.SearchParamDTO> dto) {
        return null;
    }

    @Override
    public List<PurchaseStorageDTO.ListStatusCountDTO> listCount(PermissionsDTO dto) {
        return null;
    }

    @Override
    public String add(PurchaseStorageDTO.AddDTO dto) {
        return null;
    }

    @Override
    public Boolean addAndSubmit(PurchaseStorageDTO.AddDTO dto) {
        return null;
    }

    @Override
    public Boolean update(PurchaseStorageDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public Boolean updateAndSubmit(PurchaseStorageDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public Boolean submit(BaseIdsDTO.IdsDTO dto) {
        return null;
    }

    @Override
    public PurchaseStorageDTO.ViewDTO view(String id) {
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
    public Boolean exportExcel(PurchaseStorageDTO.SearchParamDTO dto, HttpServletResponse response) {
        return null;
    }

    @Override
    public List<PurchaseStorageDTO.ViewGeneratePurchaseReturnOrderDTO> viewGeneratePurchaseReturnOrder(String id) {
        return null;
    }

    @Override
    public Boolean generatePurchaseReturnOrder(PurchaseStorageDTO.GeneratePurchaseReturnOrderDTO dto) {
        return null;
    }
}
