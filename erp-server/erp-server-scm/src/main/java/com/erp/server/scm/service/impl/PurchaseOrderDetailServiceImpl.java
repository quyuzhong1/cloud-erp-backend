package com.erp.server.scm.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SubcontractTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
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
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.ConfirmTypeEnum;
import com.erp.model.scm.enums.ExecutionStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PurchaseOrderTypeEnum;
import com.erp.model.srm.entity.DeliveryOrderDetailEntity;
import com.erp.model.wms.dto.SoReturnInstockDetailDTO;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.dto.inventory.InstockForcastDTO;
import com.erp.model.wms.dto.inventory.InventoryFinishDeliveryDetailDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.ReturnModeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.srm.feign.SrmDeliveryOrderFeign;
import com.erp.rpc.wms.feign.InventoryFeign;
import com.erp.rpc.wms.feign.WarehouseLocationFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.mapper.PurchaseOrderDetailMapper;
import com.erp.server.scm.service.*;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
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
@Slf4j
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
    private PurchaseOrderSupplierService purchaseOrderSupplierService;

    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;

    @Resource
    private SubcontractOrderDetailService subcontractOrderDetailService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private SrmDeliveryOrderFeign srmDeliveryOrderFeign;

    @Resource
    private InventoryFeign inventoryFeign;
    @Resource
    private WarehouseLocationFeign warehouseLocationFeign;

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
//            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_SCM_TO_WMS_PURCHASE_TOPIC, RocketMqTagEnum.SYNC_WMS_PURCHASE_ORDER_DETAIL_TAG.getName(), list, IdUtil.simpleUUID());
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
//            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_SCM_TO_WMS_PURCHASE_TOPIC, RocketMqTagEnum.SYNC_WMS_PURCHASE_ORDER_DETAIL_TAG.getName(), list, IdUtil.simpleUUID());
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
//        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_SCM_TO_WMS_PURCHASE_TOPIC, RocketMqTagEnum.SYNC_WMS_PURCHASE_ORDER_DETAIL_TAG.getName(), list, IdUtil.simpleUUID());

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
        List<SkuVO> skuList = plmTaskFeign.listSkuLogisticsByIds(skuIds);

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

        PurchaseOrderEntity entity = purchaseOrderService.getById(purchaseOrderId);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }

        PurchaseOrderSupplierEntity supplierEntity = purchaseOrderSupplierService.getByPurchaseOrderId(purchaseOrderId);
        if (ObjectUtils.isEmpty(supplierEntity)) {
            throw new ServiceException(ApiError.ERROR_98036);
        }
        //验证录入的SKU明细报价信息是否正确
        List<PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO> priceList = details.stream().filter(obj -> !Boolean.TRUE.equals(obj.getIsGift()))
                .map(obj -> new PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO(obj.getPurchaseQty(), obj.getSkuId(), obj.getSkuNo(), supplierEntity.getSupplierId(),entity.getPurchaseOrgId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(priceList)) {
            return;
        }
        List<String> skuIdList = details.stream().map(PurchaseOrderDetailDTO.AddDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);
        if (CollectionUtils.isEmpty(skuVOList)) {
            throw new ServiceException(ApiError.ERROR_95107);
        }
        //根据sku查询是否是组合品
        List<BomChildrenSkuDTO> skuDTOList = plmTaskFeign.listBomChildBySkuIds(skuIdList);
        List<SubcontractOrderDetailEntity> subcontractOrderDetailEntityList = null;
        if (PurchaseOrderTypeEnum.ENUM_SUBCONTRACT.getCode().equals(entity.getType())){
            if (StrUtil.isBlank(entity.getSourceId())){
                throw new ServiceException("委外订单id不能为空");
            }
            subcontractOrderDetailEntityList = subcontractOrderDetailService.listByMainId(entity.getSourceId());
            if (CollectionUtils.isEmpty(subcontractOrderDetailEntityList)){
                throw new ServiceException("委外订单明细记录不能为空");
            }
        }
        //退货单记录
        List<PoReturnDetailEntity> poReturnDetailEntityList = null;
        if (PurchaseOrderTypeEnum.ENUM_RETURN.getCode().equals(entity.getType())){

            String sourceId = entity.getSourceId();
            List<PoReturnEntity> poReturnEntityList = wmsTaskFeign.listPoReturnByIdList(Collections.singletonList(sourceId));
            if (CollectionUtils.isEmpty(poReturnEntityList)){
                throw new ServiceException(StrUtil.format("采购退货单【{}】记录不存在", entity.getSourceCode()));
            }
            //根据主键唯一 只会存在一个退货单记录
            PoReturnEntity poReturnEntity = poReturnEntityList.get(0);
            poReturnDetailEntityList = wmsTaskFeign.listReturnOrderDetailByPodIds(Collections.singletonList(poReturnEntity.getId()));
            if (CollectionUtils.isEmpty(poReturnDetailEntityList)){
                throw new ServiceException(StrUtil.format("采购退货单【{}】明细记录不存在", poReturnEntity.getCode()));
            }
        }

        List<PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO> viewDTOList = purchasePriceDetailService.batchGetTaxPrice(priceList);
        for (PurchaseOrderDetailDTO.AddDTO addDTO : details) {
            if (PurchaseOrderTypeEnum.ENUM_PURCHASE.getCode().equals(entity.getType()) || PurchaseOrderTypeEnum.ENUM_SUBCONTRACT.getCode().equals(entity.getType())){
                //委外成品时，取委外订单中的含税单价
                if (PurchaseOrderTypeEnum.ENUM_SUBCONTRACT.getCode().equals(entity.getType()) && SubcontractTypeEnum.ENUM_PARENT.getCode().equals(entity.getSubcontractType())){
                    //sku是组合品时，取委外订单含税单价
                    SubcontractOrderDetailEntity subcontractOrderDetailEntity = subcontractOrderDetailEntityList.stream().filter(e -> Objects.nonNull(e) && StrUtil.isNotBlank(e.getSkuId()) && Objects.equals(e.getSkuId(), addDTO.getSkuId()))
                            .findFirst().orElse(null);
                    if (Objects.isNull(subcontractOrderDetailEntity)){
                        throw new ServiceException(StrUtil.format("SKU【{}】是组合品，未找到委外订单明细记录",addDTO.getSkuNo()));
                    }else {
                        BigDecimal price = Objects.nonNull(subcontractOrderDetailEntity.getPrice()) ? subcontractOrderDetailEntity.getPrice():BigDecimal.ZERO;
                        Integer qty = Objects.nonNull(addDTO.getPurchaseQty()) ? addDTO.getPurchaseQty() : MathUtil.ZERO;
                        addDTO.setTaxPrice(subcontractOrderDetailEntity.getPrice());
                        addDTO.setTaxRate(subcontractOrderDetailEntity.getTaxRate());
                        addDTO.setCurrency(subcontractOrderDetailEntity.getCurrency());
                        addDTO.setCurrencySymbol(subcontractOrderDetailEntity.getCurrencySymbol());
                        addDTO.setPurchaseAmount(MathUtil.multiply(price,qty));
                    }
                    //成品直接返回
                    continue;
                }
                //子件
                PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO viewDTO = viewDTOList.stream().filter(obj -> obj.getSkuId().equals(addDTO.getSkuId())
                        && obj.getSupplierId().equals(supplierEntity.getSupplierId())
                        && StrUtil.equals(obj.getPurchaseOrgId(),addDTO.getPurchaseOrderId())
                        && (Objects.equals(addDTO.getPurchaseQty(), obj.getPurchaseQty()))).findFirst().orElse(null);
                if (Objects.nonNull(viewDTO)){
                    addDTO.setCurrency(viewDTO.getCurrency());
                    addDTO.setCurrencySymbol(viewDTO.getCurrencySymbol());
                    addDTO.setTaxPrice(viewDTO.getTaxPrice());
                    addDTO.setTaxRate(viewDTO.getTaxRate());
                    addDTO.setPurchaseAmount(MathUtil.multiply(viewDTO.getTaxPrice(),addDTO.getPurchaseQty()));
                }
            }else if (PurchaseOrderTypeEnum.ENUM_RETURN.getCode().equals(entity.getType())){
                PoReturnDetailEntity poReturnDetailEntity = poReturnDetailEntityList.stream().filter(e -> Objects.nonNull(e) && StrUtil.isNotBlank(e.getSkuId())
                        && StrUtil.isNotBlank(addDTO.getSkuId()) && Objects.equals(e.getSkuId(), addDTO.getSkuId())).findFirst().orElse(null);
                if (Objects.nonNull(poReturnDetailEntity)){
                    BigDecimal returnPrice = Objects.nonNull(poReturnDetailEntity.getReturnPrice()) ? poReturnDetailEntity.getReturnPrice() : BigDecimal.ZERO;
                    addDTO.setTaxPrice(returnPrice);
                    addDTO.setCurrency(poReturnDetailEntity.getCurrency());
                    addDTO.setCurrencySymbol(poReturnDetailEntity.getCurrencySymbol());
                    Integer qty = Objects.nonNull(addDTO.getPurchaseQty()) ? addDTO.getPurchaseQty() : MathUtil.ZERO;
                    addDTO.setPurchaseAmount(MathUtil.multiply(returnPrice,qty));
                }
            }
//
//
//            //采购单价赋值
//            PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO viewDTO = viewDTOList.stream().filter(obj ->
//                            obj.getSkuId().equals(addDTO.getSkuId())
//                            && obj.getSupplierId().equals(supplierEntity.getSupplierId())
//                            && StrUtil.equals(obj.getPurchaseOrgId(),entity.getPurchaseOrgId())
//                            && (addDTO.getPurchaseQty() == obj.getPurchaseQty()))
//                    .findFirst().orElse(null);
//            if (ObjectUtils.isEmpty(viewDTO)) {
//                continue;
//            }
//            //汇率
//            BigDecimal taxRate = viewDTO.getTaxRate();
//            //单价
//            BigDecimal taxPrice = viewDTO.getTaxPrice();
//            if (ObjectUtils.isEmpty(addDTO)) {
//                String error = String.format("SKU【%s】未找到数量【%s】的供应商报价信息", addDTO.getSkuNo(), addDTO.getPurchaseQty());
//                throw new ServiceException(new ApiResult(1,error));
//            }
//            if (MathUtil.compareTo(taxPrice,addDTO.getTaxPrice()) != MathUtil.ZERO && PurchaseOrderTypeEnum.ENUM_PURCHASE.getCode().equals(entity.getType())) {
//                String error = String.format("SKU【%s】,数量【%s】录入单价与报价单价不匹配", addDTO.getSkuNo(), addDTO.getPurchaseQty());
//                throw new ServiceException(new ApiResult(1,error));
//            }
//            if (PurchaseOrderTypeEnum.ENUM_RETURN.getCode().equals(entity.getType())) {
//                addDTO.setTaxPrice(taxPrice);
//            }
//            addDTO.setTaxRate(taxRate);
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

        //查询发货数据
        List<DeliveryOrderDetailEntity> deliveryDetailList = srmDeliveryOrderFeign.listDetailByDetailSourceIds(purchaseDetailIds);
        //查询收货数据
        List<WarehouseReceiveDetailEntity> receiveDetails = wmsTaskFeign.listWarehouseReceiveDetailByPodIds(purchaseDetailIds);

        //查询退货数据
        List<PoReturnDetailEntity> purchaseReturnOrderDetailEntities = wmsTaskFeign.listReturnOrderDetailByPodIds(purchaseDetailIds);

        //查询入库数据
        List<PoInstockDetailEntity> stockInDetails = wmsTaskFeign.listPurchaseStockInDetailByPodIds(purchaseDetailIds);

        List<String> skuIdList = list.stream().map(PurchaseOrderDetailDTO.ViewProductDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuPurchaseByIds(skuIdList);

        List<String> supplierIdList = skuList.stream().filter(obj -> StringUtils.isNotBlank(obj.getSupplierId())).map(SkuVO::getSupplierId).collect(Collectors.toList());
        List<SupplierEntity> supplierList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(supplierList)) {
            supplierList = supplierService.listByIds(supplierIdList);
        }
        //仓位信息查询
        List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> paramList = list.stream().map(obj -> new WarehouseLocationDTO.WarehouseLocationSearchParamDTO(obj.getDeliveryWarehouseId(), obj.getWarehouseLocation())).collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationEntityList = warehouseLocationFeign.listByWarehouseIdAndCode(paramList);

        for (PurchaseOrderDetailDTO.ViewProductDTO viewProductDTO : list) {

            Integer receiveQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(receiveDetails)) {
                 receiveQty = receiveDetails.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(viewProductDTO.getPurchaseOrderDetailId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            }


            //已发货数量
            Integer deliveryQty = deliveryDetailList.stream().filter(v->v.getSourceDetailId().equals(viewProductDTO.getPurchaseOrderDetailId())).mapToInt(DeliveryOrderDetailEntity::getDeliveryQty).sum();
            viewProductDTO.setDeliveryQty(deliveryQty);

            Integer returnQty = purchaseReturnOrderDetailEntities.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(viewProductDTO.getPurchaseOrderDetailId()) && obj.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus()) && obj.getReturnMode().equals(ReturnModeEnum.REPLENISHMENT.getCode())).map(PoReturnDetailEntity::getReplenishQty).reduce(MathUtil.ZERO, Integer::sum);

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
                realityReturnQty = purchaseReturnOrderDetailEntities.stream().filter(req -> req.getPurchaseOrderDetailId().equals(viewProductDTO.getPurchaseOrderDetailId())).map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);

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
            //仓位名称填充
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntityList.stream().filter(e -> e.getWarehouseId().equals(viewProductDTO.getDeliveryWarehouseId()) && e.getCode().equals(viewProductDTO.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            viewProductDTO.setWarehouseLocationName(warehouseLocationEntity.getName());
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePoArrivalStatus(PurchaseOrderDetailEntity entity) {

        //更新采购订单交货状态
        boolean update = lambdaUpdate()
                .eq(PurchaseOrderDetailEntity::getId, entity.getId())
                .set(PurchaseOrderDetailEntity::getExecutionStatus, entity.getExecutionStatus())
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

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateArrivalStatusByIds(String executionStatus, List<String> ids, List<PurchaseOrderDetailEntity> purchaseOrderDetailList, String remark) {
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
                    .set(PurchaseOrderDetailEntity::getExecutionStatus,executionStatus)
                    .set(PurchaseOrderDetailEntity::getIsEndReceive,Boolean.TRUE)
                    .set(PurchaseOrderDetailEntity::getEndReceiveTime, LocalDateTime.now())
                    .set(PurchaseOrderDetailEntity::getRemark, newRemark)
                    .update();
        });
        list.forEach(req -> req.setIsDeleted(Boolean.TRUE));
        //同步到WMS
//        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_SCM_TO_WMS_PURCHASE_TOPIC, RocketMqTagEnum.SYNC_WMS_PURCHASE_ORDER_DETAIL_TAG.getName(), list, IdUtil.simpleUUID());
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

    @Override
    public void purchaseOrderConfirm(List<String> detailIdList, ExecutionStatusEnum typeEnum, String remark, ConfirmTypeEnum confirmType) {
        if (CollectionUtils.isEmpty(detailIdList)) {
            return;
        }
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        lambdaUpdate().in(PurchaseOrderDetailEntity::getId,detailIdList)
                .set(PurchaseOrderDetailEntity::getExecutionStatus, typeEnum.getCode())
                .set(PurchaseOrderDetailEntity::getConfirmRemark,remark)
                .set(PurchaseOrderDetailEntity::getConfirmUserId, ObjectUtil.isEmpty(userInfo) ? "":userInfo.getUid())
                .set(PurchaseOrderDetailEntity::getConfirmUserName, ObjectUtil.isEmpty(userInfo) ? "system":userInfo.getUserName())
                .set(PurchaseOrderDetailEntity::getConfirmDate, LocalDate.now())
                .set(PurchaseOrderDetailEntity::getConfirmType, confirmType.getCode())
                .update(new PurchaseOrderDetailEntity());
    }

    @Override
    public void purchaseOrderAutoConfirm(List<String> detailIdList) {
        if (CollectionUtils.isEmpty(detailIdList)) {
            return;
        }
        purchaseOrderConfirm(detailIdList, ExecutionStatusEnum.CONFIRM,"系统自动确认",ConfirmTypeEnum.AUTO);
    }

    @Override
    public void updateExecutionStatus(List<String> mainIdList, ExecutionStatusEnum statusEnum) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return;
        }
        lambdaUpdate().in(PurchaseOrderDetailEntity::getPurchaseOrderId,mainIdList)
                .set(PurchaseOrderDetailEntity::getExecutionStatus,statusEnum.getCode())
                .update();
    }

    @Override
    public List<PurchaseOrderDetailDTO.ImportEndReceiveDTO> listImportEndReceive(List<String> codeList, List<String> skuNoList) {
        if (CollectionUtils.isEmpty(codeList) || CollectionUtils.isEmpty(skuNoList)) {
            return Collections.EMPTY_LIST;
        }
        return this.baseMapper.listImportEndReceive(codeList,skuNoList);
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean finishDelivery(List<String> ids, String remark,Boolean isValid) {
        //ids为采购订单明细id集合
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        //已确认、已拒绝、送货中允许结束交货
        long count = purchaseOrderDetailList.stream().filter(obj -> !ExecutionStatusEnum.CONFIRM.getCode().equals(obj.getExecutionStatus())
                && !ExecutionStatusEnum.REJECT.getCode().equals(obj.getExecutionStatus())
                && !ExecutionStatusEnum.DELIVERY.getCode().equals(obj.getExecutionStatus())
        ).count();
        if (count > 0 && isValid) {
            throw new ServiceException(ApiError.ERROR_98035);
        }
        List<String> mainIds = purchaseOrderDetailList.stream().map(PurchaseOrderDetailEntity::getPurchaseOrderId).distinct().collect(Collectors.toList());
        List<PurchaseOrderEntity> mainList = purchaseOrderService.getList(mainIds);
        //更新明细中的交货状态
        this.updateArrivalStatusByIds(ExecutionStatusEnum.CLOSED.getCode(), ids, purchaseOrderDetailList, remark);
//        //TODO 关闭时，更新订单明细状态
//        if (CollectionUtils.isNotEmpty(ids)){
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.putOpt("detailIds",ids);
//            jsonObject.putOpt("executionStatus",ExecutionStatusEnum.CLOSED.getCode());
//            //同步scm 确认订单 到 srm
//            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_SCM_TO_SRM_PURCHASE_ORDER_DETAIL_TOPIC, RocketMqTagEnum.SYNC_SRM_PURCHASE_ORDER_DETAIL_TAG.getName(),jsonObject, IdUtil.simpleUUID());
//        }
        // 更新库存
        updateInventoryFinish(mainList, purchaseOrderDetailList);
        //操作日志
        List<Pair<String, String>> pairList = purchaseOrderDetailList.stream().map(obj -> new Pair<>(obj.getPurchaseOrderId(), obj.getSkuNo())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("SKU【%s】结束交货，结束原因：".concat(StrUtils.null2EmptyWithTrim(remark)), ModuleTypeEnum.PURCHASE_ORDER.getCode(), pairList, "结束交货操作");
        return Boolean.TRUE;
    }

    /**
     * 更新库存（结束交货）
     * @param list
     * @param details
     */
    public void updateInventoryFinish(List<PurchaseOrderEntity> list, List<PurchaseOrderDetailEntity> details) {
        Map<String, PurchaseOrderEntity> poMap = list.stream().collect(Collectors.toMap(PurchaseOrderEntity::getId, Function.identity()));
        Map<String,List<PurchaseOrderDetailEntity>> detailMap = details.stream().collect(Collectors.groupingBy(PurchaseOrderDetailEntity::getPurchaseOrderId));

        List<String> podIds = details.stream().map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
        // 收货信息（结束交货一定会存在下推收货单）
        List<WarehouseReceiveDetailEntity> receiveDetailList = wmsTaskFeign.listWarehouseReceiveDetailByPodIds(podIds);
        // 采购入库（无收货单）信息（采购入库会扣减在途库存）
        List<PoInstockDetailEntity> poInstockDetailList = wmsTaskFeign.listPurchaseStockInDetailByPodIds(podIds);
        //退货数量
        List<PoReturnDetailEntity> purchaseReturnOrderDetailEntities = wmsTaskFeign.listReturnOrderDetailByPodIds(podIds);

        List<InstockForcastDTO.FinishDeliveryDTO> inventoryList = Lists.newArrayList();

        poMap.forEach((mainId, po)->{

            InstockForcastDTO.FinishDeliveryDTO inventoryDTO = new InstockForcastDTO.FinishDeliveryDTO();
            inventoryDTO.setPurchaseOrderId(mainId);
            List<PurchaseOrderDetailEntity> detailMembers = detailMap.get(mainId);

            List<InventoryFinishDeliveryDetailDTO.AddDTO> inventoryMembers = Lists.newArrayListWithExpectedSize(detailMembers.size());
            detailMembers.stream().forEach(member->{
                //执行状态已完成或已关闭
                // 的无需再次结束交货
                if (ExecutionStatusEnum.FINISH.getCode().equals(member.getExecutionStatus()) ) {
                    log.info("采购订单明细id:{}，对应采购订单:{}, 已经到货，无需结束交货", member.getId(), po.getCode());
                    return;
                }
                InventoryFinishDeliveryDetailDTO.AddDTO inventoryMember = new InventoryFinishDeliveryDetailDTO.AddDTO();
                inventoryMember.setSkuId(member.getSkuId());
                inventoryMember.setSkuNo(member.getSkuNo());
                inventoryMember.setPurchaseOrderDetailId(member.getId());
                Integer receiveQty = MathUtil.ZERO;
                if (CollUtil.isNotEmpty(receiveDetailList)) {
                    // 此处收货单需过滤为审核通过的，只有审核通过的才占用库存数量
                    receiveQty = receiveDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(member.getId())
                                    && Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus()) )
                            .map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                }
                Integer poQty = MathUtil.ZERO;
                if(CollUtil.isNotEmpty(poInstockDetailList)) {
                    // 采购入库单（无收货单），只有审核通过的才占用库存数量
                    poQty = poInstockDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(member.getId())
                                    && Objects.equals(e.getSourceDetailId(), member.getId())
                                    && Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus()) )
                            .map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                }
                Integer returnQty = MathUtil.ZERO;
                if(CollUtil.isNotEmpty(purchaseReturnOrderDetailEntities)) {
                    // 退货单（退货补货的才会导致在途数量变化）
                    returnQty = purchaseReturnOrderDetailEntities.stream().filter(e -> e.getPurchaseOrderDetailId().equals(member.getId())
                                    && Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())
                                    && StrUtils.isNotEmpty(e.getPurchaseOrderDetailId())
                                    && Objects.equals(e.getReturnMode(), ReturnModeEnum.REPLENISHMENT.getCode()))
                            .map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                }
                log.info("采购订单明细id:{}，对应采购订单:{}, 采购订单明细采购数量:{}", member.getId(), po.getCode(), member.getPurchaseQty());
                log.info("采购订单明细id:{}，对应采购订单:{}, 采购订单明细对应收货单收货数量:{}", member.getId(), po.getCode(), receiveQty);
                log.info("采购订单明细id:{}，对应采购订单:{}, 采购订单明细对应采购入库单（无收货单）入库数量:{}", member.getId(), po.getCode(), poQty);
                log.info("采购订单明细id:{}，对应采购订单:{}, 采购订单明细对应退货单（退货补货）退货数量:{}", member.getId(), po.getCode(), returnQty);
                // 采购订单增加的在途，收货单，采购订单直接生成入库单减少的在途未必一样
                Integer deliveryQty = member.getPurchaseQty() + returnQty - receiveQty - poQty;
                log.info("采购订单明细id:{}，对应采购订单:{}, 采购订单明细剩余待交数量:{}", member.getId(), po.getCode(), deliveryQty);
                inventoryMember.setQty(deliveryQty);
                inventoryMembers.add(inventoryMember);
            });
            if (CollectionUtils.isEmpty(inventoryMembers)) {
                return;
            }
            inventoryDTO.setMembers(inventoryMembers);
            inventoryList.add(inventoryDTO);
        });
        if (CollectionUtils.isNotEmpty(inventoryList)) {
            inventoryFeign.finishDeliveryBatch(inventoryList);
        }
    }
}
