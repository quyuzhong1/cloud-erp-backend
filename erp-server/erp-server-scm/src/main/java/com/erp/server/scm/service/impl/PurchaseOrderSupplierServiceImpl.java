package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.scm.dto.PurchaseOrderSupplierDTO;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.scm.mapper.PurchaseOrderSupplierMapper;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.PurchaseOrderSupplierService;
import com.erp.server.scm.service.SupplierService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
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

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Override
    public void deleteByPurchaseOrderIds(List<String> purchaseOrderIds) {
        List<PurchaseOrderSupplierEntity> list = lambdaQuery().in(PurchaseOrderSupplierEntity::getPurchaseOrderId, purchaseOrderIds).list();
        list.forEach(req -> req.setIsDeleted(Boolean.TRUE));
        //同步到WMS
//        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_SCM_TO_WMS_PURCHASE_TOPIC, RocketMqTagEnum.SYNC_WMS_PURCHASE_ORDER_SUPPLIER_TAG.getName(), list, IdUtil.simpleUUID());
        lambdaUpdate().in(PurchaseOrderSupplierEntity::getPurchaseOrderId, purchaseOrderIds).remove();
    }

    @Override
    public PurchaseOrderSupplierEntity getByPurchaseOrderId(String purchaseOrderId) {
        return lambdaQuery().eq(PurchaseOrderSupplierEntity::getPurchaseOrderId, purchaseOrderId).one();
    }

    @Override
    public List<PurchaseOrderSupplierEntity> listByPurchaseOrderIds(List<String> purchaseOrderIds) {
        return lambdaQuery().in(PurchaseOrderSupplierEntity::getPurchaseOrderId, purchaseOrderIds).list();
    }

    @Override
    public void add(PurchaseOrderSupplierDTO.AddDTO dto, String purchaseOrderId) {
        if (ObjectUtils.isEmpty(dto)) {
            return;
        }
        PurchaseOrderSupplierEntity entity = new PurchaseOrderSupplierEntity();
        BeanMapperUtils.copy(dto, entity);
        entity.setPurchaseOrderId(purchaseOrderId);
        doOpHandleDataId(dto.getSupplierId(), entity);
        this.save(entity);
        //同步到WMS
//        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_SCM_TO_WMS_PURCHASE_TOPIC, RocketMqTagEnum.SYNC_WMS_PURCHASE_ORDER_SUPPLIER_TAG.getName(), Arrays.asList(entity), IdUtil.simpleUUID());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(PurchaseOrderSupplierDTO.UpdateDTO dto, String purchaseOrderId) {
        if (ObjectUtils.isEmpty(dto)) {
            return;
        }
        PurchaseOrderSupplierEntity entity = new PurchaseOrderSupplierEntity();
        BeanMapperUtils.copy(dto, entity);
        entity.setPurchaseOrderId(purchaseOrderId);
        doOpHandleDataId(dto.getSupplierId(), entity);

        PurchaseOrderSupplierEntity old = this.getById(dto.getId());
        if (ObjectUtils.isEmpty(old)) {
            throw new ServiceException(ApiError.ERROR_98036);
        }
        //操作日志
        moduleOperateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.PURCHASE_ORDER.getCode(), purchaseOrderId, "", "");
        this.saveOrUpdate(entity);
        //同步到WMS
//        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_SCM_TO_WMS_PURCHASE_TOPIC, RocketMqTagEnum.SYNC_WMS_PURCHASE_ORDER_SUPPLIER_TAG.getName(), Arrays.asList(entity), IdUtil.simpleUUID());
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


    /**
     * 根据供应商获取到 供应商订单信息
     *
     * @param supplierIdList
     * @return void
     * @author yl
     * @date 2023-04-03 17:11
     */
    @Override
    public List<PurchaseOrderSupplierEntity> getBySupplierIds(List<String> supplierIdList) {
        if (CollectionUtils.isEmpty(supplierIdList)) {
            return Collections.emptyList();
        }
        List<PurchaseOrderSupplierEntity> list = lambdaQuery().in(PurchaseOrderSupplierEntity::getSupplierId, supplierIdList).list();
        return list;
    }


    /**
     * 检查采购订单是否有关联到供应商id
     * 如果有就不能删除
     *
     * @param supplierIds
     * @return void
     * @author yl
     * @date 2023-04-14 11:55
     */
    @Override
    public void checkIsRefSupplier(List<String> supplierIds) {
        if (CollectionUtils.isEmpty(supplierIds)) {
            return;
        }
        int count = baseMapper.getRefSupplierCount(supplierIds);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98053);
        }


    }

    @Override
    public List<PurchaseOrderSupplierEntity> listOrderSupplierByOrderIdList(List<String> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(PurchaseOrderSupplierEntity::getPurchaseOrderId, idList).list();
    }

    /**
     * 同步id对应名称
     */
    private void doOpHandleDataId(String supplierId, PurchaseOrderSupplierEntity entity) {
        SupplierEntity supplierEntity = supplierService.getById(supplierId);
        if (ObjectUtils.isEmpty(supplierEntity)) {
            throw new ServiceException(ApiError.ERROR_98039);
        }
        entity.setSupplierName(supplierEntity.getName());
    }
}
