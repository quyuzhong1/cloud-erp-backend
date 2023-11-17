package com.erp.server.scm.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchaseApplicationRefPoDTO;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.CreatePoTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PurchaseOrderTypeEnum;
import com.erp.model.wms.entity.PoInstockDetailEntity;
import com.erp.model.wms.entity.PurchaseReturnOrderDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.model.wms.enums.ReturnModeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.mapper.PurchaseOrderDetailMapper;
import com.erp.server.scm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
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
    private MQProducerService mQProducerService;

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private PurchaseApplicationRefPoService purchaseApplicationRefPoService;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private PurchaseApplicationDetailService purchaseApplicationDetailService;

    @Resource
    private PurchaseOrderSupplierService purchaseOrderSupplierService;

    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;

    @Resource
    private SubcontractOrderDetailService subcontractOrderDetailService;

    @Resource
    private SupplierService supplierService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(List<PurchaseOrderDetailDTO.AddDTO> details, String purchaseOrderId) {
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        //验证报价信息
        checkPurchasePrice(details,purchaseOrderId);

        //验证明细信息
        checkPurchaseOrderDetail(details,purchaseOrderId);

        List<PurchaseOrderDetailEntity> list = BeanMapperUtils.copyList(PurchaseOrderDetailEntity.class, details);
        //处理明细中的数据id
        doOpHandleDetails(list,purchaseOrderId,Boolean.TRUE);
        //批量新增
        boolean flag = this.saveBatch(list);
        if (flag) {
            //更新sku为不可删除标识
            List<String> skuIds = list.stream().map(PurchaseOrderDetailEntity::getSkuId).distinct().collect(Collectors.toList());
            plmTaskFeign.updateOccupyStatus(skuIds);
            //同步到WMS
            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_SCM_TO_WMS_PURCHASE_TOPIC, RocketMqTagEnum.SYNC_WMS_PURCHASE_ORDER_DETAIL_TAG.getName(), list, IdUtil.simpleUUID());
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
            if (CollectionUtils.isNotEmpty(refList)) {
                //新增关联关系
                purchaseApplicationRefPoService.saveBatch(refList);

                PurchaseOrderEntity purchaseOrderEntity = purchaseOrderService.getById(purchaseOrderId);
                if (ObjectUtils.isEmpty(purchaseOrderEntity)) {
                    throw new ServiceException(ApiError.ERROR_98025);
                }
                //采购申请单生成日志
                List<Pair<String, String>> pairList = refList.stream().map(obj -> new Pair<>(obj.getPurchaseApplicationId(), purchaseOrderEntity.getCode())).distinct().collect(Collectors.toList());
                moduleOperateLogService.batchAddModuleOperateLog("生成采购单【%s】", ModuleTypeEnum.PURCHASE_APPLICATION.getCode(),pairList,"生成采购单");
            }

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
        List<PurchaseOrderDetailDTO.AddDTO> addList = BeanMapperUtils.copyList(PurchaseOrderDetailDTO.AddDTO.class, details);
        //验证明细信息
        checkPurchaseOrderDetail(addList,purchaseOrderId);

        //原明细数据
        List<PurchaseOrderDetailEntity> oldList = this.listByPurchaseOrderId(purchaseOrderId);
        List<String> deleteIds = getDeleteIds(details, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<PurchaseOrderDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getPurchaseOrderId(), obj.getSkuNo())).collect(Collectors.toList());
            moduleOperateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.PURCHASE_ORDER.getCode(),pairList,"编辑操作");
            List<PurchaseOrderDetailEntity> list = lambdaQuery().in(PurchaseOrderDetailEntity::getPurchaseOrderId, deleteIds).list();
            list.forEach(req -> req.setIsDeleted(Boolean.TRUE));
            //同步到WMS
            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_SCM_TO_WMS_PURCHASE_TOPIC, RocketMqTagEnum.SYNC_WMS_PURCHASE_ORDER_DETAIL_TAG.getName(), list, IdUtil.simpleUUID());
            this.removeByIds(deleteIds);
        }
        List<PurchaseOrderDetailEntity> newList = BeanMapperUtils.copyList(PurchaseOrderDetailEntity.class, details);

        //处理明细id及操作日志
        doOpHandleDetails(newList,purchaseOrderId,Boolean.FALSE);

        //新增或修改采购订单明细
        this.saveOrUpdateBatch(newList);
        //更新sku为不可删除标识
        List<String> skuIds = newList.stream().map(PurchaseOrderDetailEntity::getSkuId).collect(Collectors.toList());
        plmTaskFeign.updateOccupyStatus(skuIds);
        //同步到WMS
        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_SCM_TO_WMS_PURCHASE_TOPIC, RocketMqTagEnum.SYNC_WMS_PURCHASE_ORDER_DETAIL_TAG.getName(), newList, IdUtil.simpleUUID());

        //更新采购申请单生成类型
        purchaseOrderService.updateCreatePoType(Arrays.asList(purchaseOrderId));
        //删除关联关系
        purchaseApplicationRefPoService.removeByPurchaseOrderDetailIds(deleteIds);
    }


    @Override
    public List<PurchaseOrderDetailEntity> listByPurchaseOrderId(String purchaseOrderId) {
        return  lambdaQuery()
                .eq(PurchaseOrderDetailEntity::getPurchaseOrderId,purchaseOrderId)
                .orderByAsc(PurchaseOrderDetailEntity::getId)
                .list();
    }

    @Override
    public List<PurchaseOrderDetailEntity> listByPurchaseOrderIds(List<String> purchaseOrderIds) {
        return  lambdaQuery().in(PurchaseOrderDetailEntity::getPurchaseOrderId,purchaseOrderIds).list();
    }

    @Override
    public void removeByPurchaseOrderIds(List<String> purchaseOrderIds) {
        List<PurchaseOrderDetailEntity> list = lambdaQuery().in(PurchaseOrderDetailEntity::getPurchaseOrderId, purchaseOrderIds).list();
        list.forEach(req -> req.setIsDeleted(Boolean.TRUE));
        //同步到WMS
        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_SCM_TO_WMS_PURCHASE_TOPIC, RocketMqTagEnum.SYNC_WMS_PURCHASE_ORDER_DETAIL_TAG.getName(), list, IdUtil.simpleUUID());

        lambdaUpdate().in(PurchaseOrderDetailEntity::getPurchaseOrderId,purchaseOrderIds).remove();
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
    private void doOpHandleDetails (List<PurchaseOrderDetailEntity> newList, String purchaseOrderId,Boolean isAdd) {

        //添加操作日志
        List<PurchaseOrderDetailEntity> addList = newList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        //新增不需要添加新增SKU的日志
        if (CollectionUtils.isNotEmpty(addList) && !isAdd) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(purchaseOrderId, obj.getSkuNo())).collect(Collectors.toList());
            moduleOperateLogService.batchAddModuleOperateLog("新增了一条SKU【%s】", ModuleTypeEnum.PURCHASE_ORDER.getCode(), addPairList, "编辑操作");
        }

        //产品信息
        List<String> skuIds = newList.stream().map(PurchaseOrderDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);

        for (PurchaseOrderDetailEntity entity : newList) {
            //赠品单价默认0
             if (ObjectUtils.isNotEmpty(entity.getIsGift()) && entity.getIsGift()) {
                entity.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
                entity.setCurrencySymbol(CurrencyEnum.CNY.getCurrencySymbol());
                entity.setTaxPrice(BigDecimal.ZERO);
                entity.setTaxRate(BigDecimal.ZERO);
            }
            //产品信息
            SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(entity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(skuVO)) {
                entity.setProductName(skuVO.getSkuName());
                entity.setVariantProperty(skuVO.getVariantProperty());
                entity.setDeclareModel(skuVO.getDeclareModel());
                entity.setDeclareName(skuVO.getDeclareName());
            }

            entity.setPurchaseOrderId(purchaseOrderId);
            entity.setTaxRate(MathUtil.divide(entity.getTaxRate(), MathUtil.BigDecimal_100));
            entity.setPurchaseAmount(MathUtil.multiply(entity.getTaxPrice(),entity.getPurchaseQty()));
            //操作日志
            if (StringUtils.isNotBlank(entity.getId())) {
                PurchaseOrderDetailEntity old = this.getById(entity.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_98026);
                }
                moduleOperateLogService.addModuleOperateLogByObj(old,entity, ModuleTypeEnum.PURCHASE_ORDER.getCode(),purchaseOrderId,"",String.format("【%s】",old.getSkuNo()));
            }
        }
    }


    /**
     * 供应商报价验证
     */
    private void checkPurchaseOrderDetail (List<PurchaseOrderDetailDTO.AddDTO> details,String purchaseOrderId) {
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        PurchaseOrderEntity entity = purchaseOrderService.getById(purchaseOrderId);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        //采购日期不能大于预计交货日期
        String skuNos = details.stream().filter(obj -> ObjectUtils.isNotEmpty(obj.getPlanDeliveryDate()) && entity.getPurchaseDate().isAfter(obj.getPlanDeliveryDate())).map(PurchaseOrderDetailDTO.AddDTO::getSkuNo).collect(Collectors.joining(","));
        if (StringUtils.isNotBlank(skuNos)) {
            throw new ServiceException(ApiError.ERROR_PURCHASE_DATE,skuNos,entity.getPurchaseDate());
        }
        //非正品单价必须大于0
        String notGiftSkuNos = details.stream().filter(obj -> ObjectUtils.isNotEmpty(obj.getIsGift()) && !obj.getIsGift() && MathUtil.compareTo(obj.getTaxPrice(), MathUtil.ZERO) <= MathUtil.ZERO).map(PurchaseOrderDetailDTO.AddDTO::getSkuNo).collect(Collectors.joining(","));
        if (StringUtils.isNotBlank(notGiftSkuNos)) {
            throw new ServiceException(ApiError.ERROR_PURCHASE_PRICE,notGiftSkuNos);
        }
    }

    /**
     * 补货采购订单获取报价信息
     */
    private void checkPurchasePrice (List<PurchaseOrderDetailDTO.AddDTO> details,String purchaseOrderId) {

        PurchaseOrderSupplierEntity supplierEntity = purchaseOrderSupplierService.getByPurchaseOrderId(purchaseOrderId);
        if (ObjectUtils.isEmpty(supplierEntity)) {
            throw new ServiceException(ApiError.ERROR_98036);
        }
        //验证录入的SKU明细报价信息是否正确
        List<PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO> priceList = details.stream().filter(obj -> !Boolean.TRUE.equals(obj.getIsGift())).map(obj -> new PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO(obj.getPurchaseQty(), obj.getSkuId(), obj.getSkuNo(), supplierEntity.getSupplierId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(priceList)) {
            return;
        }
        PurchaseOrderEntity entity = purchaseOrderService.getById(purchaseOrderId);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }

        for (PurchaseOrderDetailDTO.AddDTO addDTO : details) {
            PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO priceDTO = priceList.stream().filter(obj -> obj.getSkuId().equals(addDTO.getSkuId()) && MathUtil.compareTo(addDTO.getPurchaseQty(),obj.getPurchaseQty()) == MathUtil.ZERO).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(priceDTO)) {
                continue;
            }
            List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> taxPriceList = purchasePriceDetailService.getTaxPrice(priceDTO);
            PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO viewDTO = taxPriceList.get(0);
            //汇率
            BigDecimal taxRate = viewDTO.getTaxRate();
            //单价
            BigDecimal taxPrice = viewDTO.getTaxPrice();

            if (ObjectUtils.isEmpty(addDTO)) {
                String error = String.format("SKU【%s】未找到数量【%s】的供应商报价信息", priceDTO.getSkuNo(), priceDTO.getPurchaseQty());
                throw new ServiceException(new ApiResult(1,error));
            }
            if (MathUtil.compareTo(taxPrice,addDTO.getTaxPrice()) != MathUtil.ZERO && PurchaseOrderTypeEnum.ENUM_PURCHASE.getCode().equals(entity.getType())) {
                String error = String.format("SKU【%s】,数量【%s】录入单价与报价单价不匹配", priceDTO.getSkuNo(), priceDTO.getPurchaseQty());
                throw new ServiceException(new ApiResult(1,error));
            }
            if (PurchaseOrderTypeEnum.ENUM_RETURN.getCode().equals(entity.getType())) {
                addDTO.setTaxPrice(taxPrice);
            }
            addDTO.setTaxRate(taxRate);
        }
    }

    /**
     * 根据明细id查询明细
     * @Author Luo_WG
     * @Date 2023/4/13 14:00
     * @param ids ids
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderDetailEntity>
     **/
    @Override
    public List<PurchaseOrderDetailEntity> listDetailByIds(List<String> ids){
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.EMPTY_LIST;
        }
        LambdaQueryWrapper<PurchaseOrderDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(PurchaseOrderDetailEntity::getId, ids);
        return this.list(queryWrapper);
    }

    /**
     * 根据主表Id查询明细
     * @Author Luo_WG
     * @Date 2023/4/20 18:37
     * @param id id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderDetailEntity>
     **/
    @Override
    public List<PurchaseOrderDetailEntity> listPurchaseOrderDetailByOrderId(String id) {
        LambdaQueryWrapper<PurchaseOrderDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(PurchaseOrderDetailEntity::getPurchaseOrderId, id);
        return this.list(queryWrapper);
    }


    @Override
    public List<PurchaseOrderDetailDTO.ViewProductDTO> viewProduct(PurchaseOrderDetailDTO.ProductSearchParamDTO dto) {
        List<PurchaseOrderDetailDTO.ViewProductDTO> list = baseMapper.viewProduct(dto);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        List<String> purchaseDetailIds = list.stream().map(PurchaseOrderDetailDTO.ViewProductDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        //查询收货数据
        List<WarehouseReceiveDetailEntity> receiveDetails = wmsTaskFeign.listWarehouseReceiveDetailByPodIds(purchaseDetailIds);

        //查询退货数据
        List<PurchaseReturnOrderDetailEntity> purchaseReturnOrderDetailEntities = wmsTaskFeign.listReturnOrderDetailByPodIds(purchaseDetailIds);

        //查询入库数据
        List<PoInstockDetailEntity> stockInDetails = wmsTaskFeign.listPurchaseStockInDetailByPodIds(purchaseDetailIds);

        List<String> skuIdList = list.stream().map(PurchaseOrderDetailDTO.ViewProductDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        List<String> supplierIdList = skuList.stream().filter(obj -> StringUtils.isNotBlank(obj.getSupplierId())).map(SkuVO::getSupplierId).collect(Collectors.toList());
        List<SupplierEntity> supplierList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(supplierList)) {
            supplierList = supplierService.listByIds(supplierIdList);
        }

        for (PurchaseOrderDetailDTO.ViewProductDTO viewProductDTO : list) {

            Integer receiveQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(receiveDetails)) {
                 receiveQty = receiveDetails.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(viewProductDTO.getPurchaseOrderDetailId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            }

            Integer returnQty = purchaseReturnOrderDetailEntities.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(viewProductDTO.getPurchaseOrderDetailId()) && obj.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus()) && obj.getReturnMode().equals(ReturnModeEnum.REPLENISHMENT.getCode())).map(PurchaseReturnOrderDetailEntity::getReplenishQty).reduce(MathUtil.ZERO, Integer::sum);

            //收货数量
            viewProductDTO.setReceiveQty(receiveQty);
            //未收货数量
            viewProductDTO.setUnReceiveQty(viewProductDTO.getPurchaseQty() + returnQty - receiveQty);

            //参考供应商
            String supplierId = skuList.stream().filter(obj -> obj.getSkuId().equals(viewProductDTO.getSkuId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getSupplierId())).orElse("");
            if (CollectionUtils.isNotEmpty(supplierIdList)) {
                String supplierName = supplierList.stream().filter(obj -> obj.getId().equals(supplierId)).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                viewProductDTO.setMainSupplierId(supplierId);
                viewProductDTO.setMainSupplierName(supplierName);
            }

            //超收数量
            Integer exceedQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(receiveDetails)) {
                exceedQty = receiveDetails.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(viewProductDTO.getPurchaseOrderDetailId())).map(WarehouseReceiveDetailEntity::getExceedQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            viewProductDTO.setExceedQty(exceedQty);

            //退货数量
            Integer realityReturnQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(purchaseReturnOrderDetailEntities)) {
                realityReturnQty = purchaseReturnOrderDetailEntities.stream().filter(req -> req.getPurchaseOrderDetailId().equals(viewProductDTO.getPurchaseOrderDetailId())).map(PurchaseReturnOrderDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);

            }
            viewProductDTO.setRealityReturnQty(realityReturnQty);
            //有效入库数量（未审核通过）
            Integer effectiveStockInQty = MathUtil.ZERO;
            //已入库数量（审核通过）
            Integer hasStockInQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(stockInDetails)) {
                effectiveStockInQty = stockInDetails.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(viewProductDTO.getPurchaseOrderDetailId())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                hasStockInQty = stockInDetails.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(viewProductDTO.getPurchaseOrderDetailId()) && obj.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            viewProductDTO.setHasStockInQty(hasStockInQty);
            viewProductDTO.setEffectiveStockInQty(effectiveStockInQty);
            //未入库数量
            viewProductDTO.setUnStockInQty(viewProductDTO.getPurchaseQty() - effectiveStockInQty + returnQty );
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePoArrivalStatus(PurchaseOrderDetailEntity entity) {

        //更新采购订单交货状态
        boolean update = lambdaUpdate()
                .eq(PurchaseOrderDetailEntity::getId, entity.getId())
                .set(PurchaseOrderDetailEntity::getArrivalStatus, entity.getArrivalStatus())
                .set(PurchaseOrderDetailEntity::getArrivalTime, entity.getArrivalTime())
                .set(ObjectUtils.isNotNull(entity.getPurchaseAmount()), PurchaseOrderDetailEntity::getPurchaseAmount, entity.getPurchaseAmount())
                .update();
        if (!update) {
            throw new ServiceException(ApiError.ERROR_98081);
        }
        //采购订单
        PurchaseOrderEntity purchaseOrderEntity = purchaseOrderService.getById(entity.getPurchaseOrderId());
        if (ObjectUtils.isEmpty(purchaseOrderEntity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        if (StringUtils.isBlank(purchaseOrderEntity.getSubcontractType())) {
            return Boolean.TRUE;
        }
        //委外订单更新到货状态
        subcontractOrderDetailService.updateArrivalStatusByIds(entity.getSubArrivalStatus(),Arrays.asList(entity.getSourceDetailId()),Boolean.FALSE);
        return Boolean.TRUE;
    }

    @Override
    public List<PurchaseOrderDetailEntity> listBySourceDetailIds(List<String> sourceDetailIds) {
       return baseMapper.listBySourceDetailIds(sourceDetailIds);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateArrivalStatusByIds(String arrivalStatus, List<String> ids, List<PurchaseOrderDetailEntity> purchaseOrderDetailList, String remark) {
        List<PurchaseOrderDetailEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }

        // 结束交货备注追加在原sku备注
        Map<String,PurchaseOrderDetailEntity> productOrderDetailMap =  purchaseOrderDetailList.stream().collect(Collectors.toMap(PurchaseOrderDetailEntity::getId, Function.identity()));
        productOrderDetailMap.forEach((detailId, purchaseOrderDetail)->{
            String oldRemark = purchaseOrderDetail.getRemark();
            String newRemark = remark;
            if(StrUtils.isNotEmpty(oldRemark)) {
                if(oldRemark.endsWith(";") || oldRemark.endsWith("；")) {
                    newRemark = oldRemark + remark;
                } else {
                    newRemark = oldRemark + "；" + remark;
                }
            }
            lambdaUpdate()
                    .eq(PurchaseOrderDetailEntity::getId,detailId)
                    .set(PurchaseOrderDetailEntity::getArrivalStatus,arrivalStatus)
                    .set(PurchaseOrderDetailEntity::getArrivalTime, LocalDateTime.now())
                    .set(PurchaseOrderDetailEntity::getIsEndReceive,Boolean.TRUE)
                    .set(PurchaseOrderDetailEntity::getRemark, newRemark)
                    .update();
        });
        list.forEach(req -> req.setIsDeleted(Boolean.TRUE));
        //同步到WMS
        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_SCM_TO_WMS_PURCHASE_TOPIC, RocketMqTagEnum.SYNC_WMS_PURCHASE_ORDER_DETAIL_TAG.getName(), list, IdUtil.simpleUUID());
    }

    @Override
    public List<PurchaseOrderDetailEntity> getLatest(List<String> skuIds) {
        return this.baseMapper.getLatest(skuIds);
    }
    @Override
    public List<PurchaseOrderDetailEntity> getLatestByCrtTime(List<String> skuIds) {
        return this.baseMapper.getLatestByCrtTime(skuIds);
    }

    @Override
    public void updateKingdeeDetailId(JSONArray list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (Object obj : list) {
            JSONObject jsonObject = JSONUtil.parseObj(obj);
            String detailId = (String) jsonObject.get("detailId");
            String kingdeeDetailId = (String) jsonObject.get("kingdeeDetailId");
            this.lambdaUpdate()
                    .set(PurchaseOrderDetailEntity::getKingdeeDetailId, kingdeeDetailId)
                    .eq(PurchaseOrderDetailEntity::getId, detailId)
                    .update();
        }
    }

    @Override
    public void updateRemarkByIds(List<String> ids, String remark) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        lambdaUpdate().in(PurchaseOrderDetailEntity::getId,ids)
                .set(PurchaseOrderDetailEntity::getRemark,remark)
                .update(new PurchaseOrderDetailEntity());
    }

    @Override
    public List<String> listPoIdBySkuNo(String skuNo) {
        LambdaQueryWrapper<PurchaseOrderDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(PurchaseOrderDetailEntity::getPurchaseOrderId);
        queryWrapper.eq(PurchaseOrderDetailEntity::getSkuNo, skuNo);
        queryWrapper.eq(PurchaseOrderDetailEntity::getIsDeleted, Boolean.FALSE);
        queryWrapper.groupBy(PurchaseOrderDetailEntity::getPurchaseOrderId);
        return listObjs(queryWrapper, Object::toString);
    }
}
