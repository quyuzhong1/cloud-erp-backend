package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.dto.PurchaseOrderSupplierDTO;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.server.scm.mapper.PurchaseOrderSupplierMapper;
import com.erp.server.scm.service.PurchaseOrderSupplierService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Service
public class PurchaseOrderSupplierServiceImpl extends SuperServiceImpl<PurchaseOrderSupplierMapper, PurchaseOrderSupplierEntity> implements PurchaseOrderSupplierService {

    @Override
    public void deleteByPurchaseOrderIds(List<String> purchaseOrderIds) {
        lambdaUpdate().in(PurchaseOrderSupplierEntity::getPurchaseOrderId, purchaseOrderIds).remove();
    }

    @Override
    public PurchaseOrderSupplierEntity listByPurchaseOrderId(String purchaseOrderId) {
        return lambdaQuery().eq(PurchaseOrderSupplierEntity::getPurchaseOrderId, purchaseOrderId).one();
    }

    @Override
    public List<PurchaseOrderSupplierEntity> listByPurchaseOrderIds(List<String> purchaseOrderIds) {
        return lambdaQuery().in(PurchaseOrderSupplierEntity::getPurchaseOrderId, purchaseOrderIds).list();
    }

    @Override
    public void add(PurchaseOrderSupplierDTO.AddDTO dto,String purchaseOrderId) {
        if (ObjectUtils.isEmpty(dto)) {
            return;
        }
        PurchaseOrderSupplierEntity entity = new PurchaseOrderSupplierEntity();
        BeanMapperUtils.copy(dto, entity);
        entity.setPurchaseOrderId(purchaseOrderId);
        this.save(entity);
    }

    @Override
    public void update(PurchaseOrderSupplierDTO.UpdateDTO dto,String purchaseOrderId) {
        if (ObjectUtils.isEmpty(dto)) {
            return;
        }
        PurchaseOrderSupplierEntity entity = new PurchaseOrderSupplierEntity();
        BeanMapperUtils.copy(dto, entity);
        entity.setPurchaseOrderId(purchaseOrderId);
        this.saveOrUpdate(entity);
    }


    /**
     * 获取供应商采购记录
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.scm.dto.PurchaseOrderSupplierDTO.SupplierPurchaseDTO>
     * @author yl
     * @date 2023-03-29 10:50
     */
    @Override
    public PagingVO<PurchaseOrderSupplierDTO.SupplierPurchaseDTO> supplierPurchasePaging(PagingDTO<BaseIdDTO> dto) {
        BaseIdDTO idDTO = dto.getParams();
        String supplierId = idDTO.getId();
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.supplierPurchasePaging(query, supplierId);
        List<PurchaseOrderSupplierDTO.SupplierPurchaseDTO> list = pageData.getRecords();
        for (PurchaseOrderSupplierDTO.SupplierPurchaseDTO item : list) {
            String approveStatus = item.getApproveStatus();
            item.setApproveStatusName(ApproveStatusEnum.getName(approveStatus));
        }
        return new PagingVO(pageData);
    }

}
