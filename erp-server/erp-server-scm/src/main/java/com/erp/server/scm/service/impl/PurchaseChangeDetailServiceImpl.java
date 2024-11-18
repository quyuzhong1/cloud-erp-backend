package com.erp.server.scm.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.scm.dto.PurchaseChangeDetailDTO;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.entity.PurchaseChangeDetailEntity;
import com.erp.model.scm.entity.PurchaseChangeEntity;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.enums.ExecutionStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PurchaseOrderTypeEnum;
import com.erp.model.wms.entity.PoInstockDetailEntity;
import com.erp.model.wms.entity.PoReturnDetailEntity;
import com.erp.model.wms.entity.PoReturnEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.model.wms.enums.ReturnModeEnum;
import com.erp.model.wms.enums.ReturnOrderSourceEnum;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.mapper.PurchaseChangeDetailMapper;
import com.erp.server.scm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static cn.hutool.core.text.CharSequenceUtil.format;
import static cn.hutool.core.text.CharSequenceUtil.isNotBlank;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Service
public class PurchaseChangeDetailServiceImpl extends SuperServiceImpl<PurchaseChangeDetailMapper, PurchaseChangeDetailEntity> implements PurchaseChangeDetailService {

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;
    @Resource
    private PurchasePriceService purchasePriceService;

    @Resource
    private PurchaseChangeService purchaseChangeService;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(List<PurchaseChangeDetailDTO.AddDTO> details, String purchaseChangeId) {
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        List<PurchaseChangeDetailEntity> list = BeanMapperUtils.copyList(PurchaseChangeDetailEntity.class, details);
        //采购变更id赋值
        list.forEach(obj -> obj.setPurchaseChangeId(purchaseChangeId));

        //新增变更时验证采购订单是否关闭
        checkPoPushDown(list);
        //验证数量、单价是否符合供应商报价
        checkPurchasePrice(list,purchaseChangeId);
        //计算金额
        doOpCalculateAmount(list,purchaseChangeId,Boolean.TRUE);
        this.saveBatch(list);
    }

    /**
     * @description:
     * @author Will
     * @date: 2024/1/17 18:35
     */
    private void checkPoPushDown (List<PurchaseChangeDetailEntity> list) {
        List<String> purchaseOrderDetailIds = list.stream().map(PurchaseChangeDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = purchaseOrderDetailService.listByIds(purchaseOrderDetailIds);
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        //采购订单
        PurchaseOrderEntity entity = purchaseOrderService.getById(purchaseOrderDetailList.get(0).getPurchaseOrderId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        //订单明细数据校验
        String skuNos = purchaseOrderDetailList.stream().filter(obj -> !CharSequenceUtil.equals(ExecutionStatusEnum.CONFIRM.getCode(), obj.getExecutionStatus())
                 && !CharSequenceUtil.equals(ExecutionStatusEnum.DELIVERY.getCode(), obj.getExecutionStatus())
                 && !CharSequenceUtil.equals(ExecutionStatusEnum.FINISH.getCode(), obj.getExecutionStatus())
                )
                .map(PurchaseOrderDetailEntity::getSkuNo).collect(Collectors.joining(","));
        if (isNotBlank(skuNos)) {
            throw new ServiceException(ApiError.ERROR_PURCHASE_ORDER_PUSH_DOWN,entity.getCode(),skuNos);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<PurchaseChangeDetailDTO.UpdateDTO> details, String purchaseChangeId) {
        if (details == null) {
            details = new ArrayList<>();
        }
        //原明细数据
        List<PurchaseChangeDetailEntity> oldList = this.listByPurchaseChangeIds(Arrays.asList(purchaseChangeId));
        List<String> deleteIds = getDeleteIds(details, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {

            List<PurchaseChangeDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getPurchaseChangeId(), obj.getSkuNo())).collect(Collectors.toList());
            moduleOperateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.PURCHASE_CHANGE.getCode(), pairList, "编辑操作");
            this.removeByIds(deleteIds);
        }
        List<PurchaseChangeDetailEntity> newList = BeanMapperUtils.copyList(PurchaseChangeDetailEntity.class, details);
        //采购变更id赋值
        newList.forEach(obj -> obj.setPurchaseChangeId(purchaseChangeId));
        //验证数量、单价是否符合供应商报价
        checkPurchasePrice(newList,purchaseChangeId);
        //计算金额
        doOpCalculateAmount(newList,purchaseChangeId,Boolean.FALSE);
        this.saveOrUpdateBatch(newList);
    }

    @Override
    public List<PurchaseChangeDetailEntity> listByPurchaseChangeIds(List<String> purchaseChangeIds) {
        return lambdaQuery().in(PurchaseChangeDetailEntity::getPurchaseChangeId, purchaseChangeIds).list();
    }


    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<PurchaseChangeDetailDTO.UpdateDTO> newList, List<PurchaseChangeDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(PurchaseChangeDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(PurchaseChangeDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * 更新金额
     */
    private void doOpCalculateAmount(List<PurchaseChangeDetailEntity> newList,String purchaseChangeId,Boolean isAdd) {
        if (CollectionUtils.isEmpty(newList)) {
            return;
        }
        //查询编辑前数据
        List<String> detailIds = newList.stream().map(PurchaseChangeDetailEntity::getId).collect(Collectors.toList());
        List<PurchaseChangeDetailEntity> oldList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(detailIds)) {
             oldList = this.listByIds(detailIds);
        }

        for (PurchaseChangeDetailEntity entity : newList) {
            entity.setPurchaseChangeId(purchaseChangeId);
            entity.setAmount(MathUtil.multiply(entity.getPrice(),entity.getQty()));
            //操作日志
            if (StringUtils.isNotBlank(entity.getId())) {
                PurchaseChangeDetailEntity old = oldList.stream().filter(obj -> obj.getId().equals(entity.getId())).findFirst().orElse(null);
                if (org.springframework.util.ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_98043);
                }
                moduleOperateLogService.addModuleOperateLogByObj(old,entity, ModuleTypeEnum.PURCHASE_CHANGE.getCode(),purchaseChangeId,"",String.format("【%s】",old.getSkuNo()));
            }
        }

        //添加操作日志
        List<PurchaseChangeDetailEntity> addList = newList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(addList) && !isAdd) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(purchaseChangeId, obj.getSkuNo())).collect(Collectors.toList());
            moduleOperateLogService.batchAddModuleOperateLog("新增了一条SKU【%s】", ModuleTypeEnum.PURCHASE_CHANGE.getCode(), addPairList, "编辑操作");
        }
    }

    /**
     * @description: 验证是否存在供应商报价
     * @author Will
     * @date: 2023/4/3 16:44
     */
    @Override
    public void checkPurchasePrice (List<PurchaseChangeDetailEntity> list,String purchaseChangeId) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        PurchaseChangeEntity purchaseChangeEntity = purchaseChangeService.getById(purchaseChangeId);
        if (ObjectUtils.isEmpty(purchaseChangeEntity)) {
            throw new ServiceException(ApiError.ERROR_98042);
        }
        List<String> purchaseOrderDetailIds = list.stream().map(PurchaseChangeDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = purchaseOrderDetailService.listByIds(purchaseOrderDetailIds);
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        List<String> podIds = list.stream().map(PurchaseChangeDetailEntity::getPurchaseOrderDetailId).distinct().collect(Collectors.toList());
        PurchaseOrderEntity purchaseOrderEntity = purchaseOrderService.getById(purchaseOrderDetailList.get(0).getPurchaseOrderId());
        //收货信息
        List<WarehouseReceiveDetailEntity> receiveDetailList = wmsTaskFeign.listWarehouseReceiveDetailByPodIds(podIds);

        //入库信息
        List<PoInstockDetailEntity> purchaseStockInDetailList = wmsTaskFeign.listPurchaseStockInDetailByPodIds(podIds);

        //退货信息
        List<PoReturnDetailEntity> purchaseReturnOrderDetailList = wmsTaskFeign.listReturnOrderDetailByPodIds(podIds);
        //退货单记录
        List<PoReturnDetailEntity> poReturnDetailEntityList = null;
        if (PurchaseOrderTypeEnum.ENUM_RETURN.getCode().equals(purchaseOrderEntity.getType())){

            String sourceId = purchaseOrderEntity.getSourceId();
            List<PoReturnEntity> poReturnEntityList = wmsTaskFeign.listPoReturnByIdList(Collections.singletonList(sourceId));
            if (CollectionUtils.isEmpty(poReturnEntityList)){
                throw new ServiceException(format("采购退货单【{}】记录不存在", purchaseOrderEntity.getSourceCode()));
            }
            //根据主键唯一 只会存在一个退货单记录
            PoReturnEntity poReturnEntity = poReturnEntityList.get(0);
            poReturnDetailEntityList = wmsTaskFeign.listPurchaseReturnOrderDetailByMainIds(Collections.singletonList(poReturnEntity.getId()));
            if (CollectionUtils.isEmpty(poReturnDetailEntityList)){
                throw new ServiceException(format("采购退货单【{}】明细记录不存在", poReturnEntity.getCode()));
            }
        }
        //采购价目查询
        List<PurchasePriceDTO.PriceDTO> priceDTOS = new ArrayList<>();
        list.forEach(e ->{
            PurchaseOrderDetailEntity detailEntity = purchaseOrderDetailList.stream().filter(obj -> obj.getId().equals(e.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (Objects.nonNull(detailEntity) && Objects.nonNull(detailEntity.getIsGift()) && !detailEntity.getIsGift()){
                priceDTOS.add(new PurchasePriceDTO.PriceDTO(e.getQty(),e.getSkuId(),
                        purchaseChangeEntity.getSupplierId(),purchaseChangeEntity.getPurchaseOrgId()));
            }

        });
        List<PurchasePriceDTO.PriceDTO> priceDTOS1 = purchasePriceService.batchGetPurchasePrice(priceDTOS);
        for (PurchaseChangeDetailEntity purchaseChangeDetailEntity : list) {
            //质检退货数量
            Integer returnQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(purchaseReturnOrderDetailList)) {
                //质检退货数量
                returnQty = purchaseReturnOrderDetailList.stream()
                        .filter(req -> req.getPurchaseOrderDetailId().equals(purchaseChangeDetailEntity.getPurchaseOrderDetailId())
                                && req.getReturnMode().equals(ReturnModeEnum.REPLENISHMENT.getCode())
                                && ReturnOrderSourceEnum.QC.getCode().equals(req.getSourceType()))
                        .map(PoReturnDetailEntity::getReturnQty)
                        .reduce(MathUtil.ZERO, Integer::sum);
            }

            //变更后数量不能小于收货数量
            String receiveMsg = "";
            Integer receiveResultQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(receiveDetailList)) {
             Integer receiveQty = receiveDetailList.stream()
                        .filter(obj -> obj.getPurchaseOrderDetailId().equals(purchaseChangeDetailEntity.getPurchaseOrderDetailId()))
                        .map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                receiveResultQty = receiveQty - returnQty;
                if (receiveQty - returnQty >  purchaseChangeDetailEntity.getQty()) {
                    receiveMsg = String.format("SKU【%s】数量不能小于(收货数量-质检退货量)【%s】",purchaseChangeDetailEntity.getSkuNo(),receiveQty - returnQty);
                }
            }
            //变更后数量不能小于入库数量
            String stockInMsg = "";
            Integer stockInQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(purchaseStockInDetailList)) {
                 stockInQty = purchaseStockInDetailList.stream()
                        .filter(obj -> obj.getPurchaseOrderDetailId().equals(purchaseChangeDetailEntity.getPurchaseOrderDetailId()))
                        .map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                if (stockInQty > purchaseChangeDetailEntity.getQty()) {
                    stockInMsg = String.format("SKU【%s】数量不能小于入库数量【%s】",purchaseChangeDetailEntity.getSkuNo(),stockInQty);
                }
            }
            /**
             * 测试要求根据数量的大小来进行错误提示
             */
            if (isNotBlank(receiveMsg) && isNotBlank(stockInMsg))  {
                if (MathUtil.compareTo(receiveResultQty,stockInQty) > MathUtil.ZERO) {
                    throw new ServiceException(ApiError.DEFAULT.code,receiveMsg);
                } else {
                    throw new ServiceException(ApiError.DEFAULT.code,stockInMsg);
                }
            } else {
                if (isNotBlank(receiveMsg)) {
                    throw new ServiceException(ApiError.DEFAULT.code,receiveMsg);
                }
                if (isNotBlank(stockInMsg)) {
                    throw new ServiceException(ApiError.DEFAULT.code,stockInMsg);
                }
            }

            //采购订单明细
            PurchaseOrderDetailEntity detailEntity = purchaseOrderDetailList.stream().filter(obj -> obj.getId().equals(purchaseChangeDetailEntity.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (org.springframework.util.ObjectUtils.isEmpty(detailEntity)) {
                throw new ServiceException(ApiError.ERROR_98026);
            }


            //赠品无需判断供应商报价
            if (!org.springframework.util.ObjectUtils.isEmpty(detailEntity.getIsGift()) && detailEntity.getIsGift()) {
                purchaseChangeDetailEntity.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
                purchaseChangeDetailEntity.setCurrencySymbol(CurrencyEnum.CNY.getCurrencySymbol());
                purchaseChangeDetailEntity.setPrice(BigDecimal.ZERO);
                purchaseChangeDetailEntity.setTaxRate(BigDecimal.ZERO);
                continue;
            }
            PurchasePriceDTO.PriceDTO priceDTO = priceDTOS1.stream().filter(obj -> obj.getSkuId().equals(detailEntity.getSkuId())
                            && obj.getSupplierId().equals(purchaseChangeEntity.getSupplierId())
                            && CharSequenceUtil.equals(obj.getPurchaseOrgId(),purchaseOrderEntity.getPurchaseOrgId()))
                    .findFirst().orElse(null);
            BigDecimal taxRate = (org.springframework.util.ObjectUtils.isEmpty(priceDTO) || Objects.nonNull(priceDTO.getTaxRate())) ?  BigDecimal.ZERO : priceDTO.getTaxRate();
            purchaseChangeDetailEntity.setTaxRate(MathUtil.divide(taxRate,MathUtil.BigDecimal_100));
        }
    }
}