package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.scm.dto.PurchaseApplicationRefPoDTO;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.model.scm.entity.PurchaseApplicationDetailEntity;
import com.erp.model.scm.entity.PurchaseApplicationRefPoEntity;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.enums.CreatePoTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.mapper.PurchaseOrderDetailMapper;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.PurchaseApplicationDetailService;
import com.erp.server.scm.service.PurchaseApplicationRefPoService;
import com.erp.server.scm.service.PurchaseOrderDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Service
public class PurchaseOrderDetailServiceImpl extends SuperServiceImpl<PurchaseOrderDetailMapper, PurchaseOrderDetailEntity> implements PurchaseOrderDetailService {

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PurchaseApplicationRefPoService purchaseApplicationRefPoService;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private PurchaseApplicationDetailService purchaseApplicationDetailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(List<PurchaseOrderDetailDTO.AddDTO> details, String purchaseOrderId) {
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        List<PurchaseOrderDetailEntity> list = BeanMapperUtils.copyList(PurchaseOrderDetailEntity.class, details);
        //处理明细中的数据id
        doOpHandleDetails(list,purchaseOrderId);
        //批量新增
        boolean flag = this.saveBatch(list);
        if (flag) {
            //新增关联关系
            List<PurchaseApplicationRefPoEntity> refList = new ArrayList<>();
            for (PurchaseOrderDetailEntity entity : list) {
                if (StringUtils.isBlank(entity.getPurchaseApplicationDetailId())) {
                    continue;
                }
                PurchaseApplicationRefPoEntity refPoEntity = new PurchaseApplicationRefPoEntity();
                refPoEntity.setPurchaseOrderId(purchaseOrderId);
                refPoEntity.setPurchaseOrderDetailId(entity.getId());
                refPoEntity.setPurchaseApplicationId(entity.getPurchaseApplicationId());
                refPoEntity.setPurchaseApplicationDetailId(entity.getPurchaseApplicationDetailId());
                refList.add(refPoEntity);
            }
            purchaseApplicationRefPoService.saveBatch(refList);
        }
    }

    @Override
    public PurchaseOrderDetailEntity getByPurchaseOrderIdAndSkuId(String purchaseOrderId, String skuId) {
        return lambdaQuery()
                .eq(PurchaseOrderDetailEntity::getPurchaseOrderId,purchaseOrderId)
                .eq(PurchaseOrderDetailEntity::getSkuId,skuId)
                .one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<PurchaseOrderDetailDTO.UpdateDTO> details, String purchaseOrderId) {
        if (details == null) {
            details = new ArrayList<>();
        }
        //原明细数据
        List<PurchaseOrderDetailEntity> oldList = this.listByPurchaseOrderId(purchaseOrderId);
        List<String> deleteIds = getDeleteIds(details, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<PurchaseOrderDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getPurchaseOrderId(), obj.getSkuNo())).collect(Collectors.toList());
            moduleOperateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.PURCHASE_ORDER.getCode(),pairList,"编辑操作");
            //删除关联关系
            purchaseApplicationRefPoService.removeByPurchaseOrderDetailIds(deleteIds);
            this.removeByIds(deleteIds);
        }
        List<PurchaseOrderDetailEntity> newList = BeanMapperUtils.copyList(PurchaseOrderDetailEntity.class, details);
        //处理明细id及操作日志
        doOpHandleDetails(newList,purchaseOrderId);

        //新增或修改采购订单明细
        this.saveOrUpdateBatch(newList);

        //更新采购申请单生成类型
        updateCreatePoType(purchaseOrderId);
    }

    @Override
    public List<PurchaseOrderDetailEntity> listByPurchaseOrderId(String purchaseOrderId) {
        return  lambdaQuery().eq(PurchaseOrderDetailEntity::getPurchaseOrderId,purchaseOrderId).list();
    }

    @Override
    public List<PurchaseOrderDetailEntity> listByPurchaseOrderIds(List<String> purchaseOrderIds) {
        return  lambdaQuery().in(PurchaseOrderDetailEntity::getPurchaseOrderId,purchaseOrderIds).list();
    }

    @Override
    public void removeByPurchaseOrderIds(List<String> purchaseOrderIds) {
        lambdaUpdate().in(PurchaseOrderDetailEntity::getPurchaseOrderId,purchaseOrderIds).remove();
    }

    @Override
    public void updateArrivalStatusByIds(String arrivalStatus, List<String> ids) {
        lambdaUpdate()
                .in(PurchaseOrderDetailEntity::getId,ids)
                .set(PurchaseOrderDetailEntity::getArrivalStatus,arrivalStatus)
                .set(PurchaseOrderDetailEntity::getArrivalTime, LocalDateTime.now())
                .update();
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<PurchaseOrderDetailDTO.UpdateDTO> newList, List<PurchaseOrderDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(PurchaseOrderDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * 处理明细中的数据id
     */
    private void doOpHandleDetails (List<PurchaseOrderDetailEntity> newList, String purchaseOrderId) {

        //收料组织信息
        List<String> receiveOrgIds = newList.stream().map(PurchaseOrderDetailEntity::getReceiveOrgId).collect(Collectors.toList());
        List<BaseIdDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(receiveOrgIds);


        for (PurchaseOrderDetailEntity entity : newList) {
            //收料组织名称
            if (CollectionUtils.isEmpty(accountingCompanyList)) {
                throw new ServiceException(ApiError.ERROR_9040);
            }
            String receiveOrgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getReceiveOrgId())).map(BaseIdDTO::getName).findFirst().orElse(null);
            entity.setPurchaseOrderId(purchaseOrderId);
            entity.setTaxRate(MathUtil.divide(entity.getTaxRate(), MathUtil.BigDecimal_100));
            entity.setReceiveOrgName(receiveOrgName);
            entity.setPurchaseAmount(MathUtil.multiply(entity.getTaxPrice(),entity.getPurchaseQty()));
            //操作日志
            if (StringUtils.isBlank(entity.getId())) {
                moduleOperateLogService.addModuleOperateLog(String.format("新增了一条SKU【%s】",entity.getSkuNo()), ModuleTypeEnum.PURCHASE_ORDER.getCode(),purchaseOrderId,"编辑操作");
            } else {
                PurchaseOrderDetailEntity old = this.getById(entity.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_98026);
                }
                moduleOperateLogService.addModuleOperateLogByObj(old,entity, ModuleTypeEnum.PURCHASE_ORDER.getCode(),purchaseOrderId,"",String.format("【%s】",old.getSkuNo()));
            }
        }
    }

    /**
     * 更新生成状态
     */
    private void updateCreatePoType (String purchaseOrderId) {
        List<PurchaseApplicationRefPoDTO.ListDTO> refList = purchaseApplicationRefPoService.list(new PurchaseApplicationRefPoDTO.SearchParamDTO().setPurchaseOrderIds(Arrays.asList(purchaseOrderId)));
        //无关联数据则不处理
        if (CollectionUtils.isEmpty(refList)) {
            return;
        }
        List<String> purchaseApplicationDetailIds = refList.stream().map(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseApplicationDetailId).collect(Collectors.toList());

        //采购申请明细下已采购数据
        List<PurchaseApplicationRefPoDTO.ListDTO> list = purchaseApplicationRefPoService.list(new PurchaseApplicationRefPoDTO.SearchParamDTO().setPurchaseApplicationDetailIds(purchaseApplicationDetailIds));
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //采购申请单明细数据
        List<PurchaseApplicationDetailEntity> purchaseApplicationDetailList = purchaseApplicationDetailService.listByIds(purchaseApplicationDetailIds);
        if (CollectionUtils.isEmpty(purchaseApplicationDetailList)) {
            throw new ServiceException(ApiError.ERROR_98017);
        }
        List<PurchaseApplicationDetailEntity> resultList = new ArrayList<>();
        Map<String, List<PurchaseApplicationRefPoDTO.ListDTO>> map = list.stream().collect(Collectors.groupingBy(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseApplicationDetailId));
        for (Map.Entry<String, List<PurchaseApplicationRefPoDTO.ListDTO>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<PurchaseApplicationRefPoDTO.ListDTO> value = entry.getValue();

            PurchaseApplicationDetailEntity entity = new PurchaseApplicationDetailEntity();
            entity.setId(key);
            //申请数量
            PurchaseApplicationDetailEntity applicationDetail = purchaseApplicationDetailList.stream().filter(obj -> obj.getId().equals(key)).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(applicationDetail)) {
                throw new ServiceException(ApiError.ERROR_98017);
            }
            Integer applyQty = applicationDetail.getApplyQty();
            //采购数量
            Integer purchaseQty = value.stream().map(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);
            if (MathUtil.compareTo(purchaseQty,MathUtil.ZERO) == MathUtil.ZERO) {
                entity.setCreatePoType(CreatePoTypeEnum.NOT_GENERATED.getStatus());
            }
            if (MathUtil.compareTo(applyQty,purchaseQty) == MathUtil.ZERO) {
                entity.setCreatePoType(CreatePoTypeEnum.ALL_GENERATED.getStatus());
            }
            if (MathUtil.compareTo(applyQty,purchaseQty) > MathUtil.ZERO) {
                entity.setCreatePoType(CreatePoTypeEnum.PARTIAL_GENERATED.getStatus());
            }
            if (MathUtil.compareTo(purchaseQty,applyQty) > MathUtil.ZERO) {
                throw new ServiceException(new ApiResult(1,String.format("采购申请明细SKU【%s】采购数量【%s】不能大于【%s】",applicationDetail.getSkuNo(),purchaseQty,applyQty)));
            }
            resultList.add(entity);
        }
        purchaseApplicationDetailService.updateBatchById(resultList);
    }

}
