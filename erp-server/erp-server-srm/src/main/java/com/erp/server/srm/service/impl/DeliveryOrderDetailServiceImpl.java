package com.erp.server.srm.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.srm.dto.DeliveryOrderDetailDTO;
import com.erp.model.srm.entity.DeliveryOrderDetailEntity;
import com.erp.model.srm.entity.DeliveryOrderEntity;
import com.erp.model.srm.enums.DeliveryOrderEnum;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.entity.PoInstockDetailEntity;
import com.erp.model.wms.entity.PoReturnDetailEntity;
import com.erp.model.wms.enums.ReturnModeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.srm.convert.DeliveryOrderConverter;
import com.erp.server.srm.mapper.DeliveryOrderDetailMapper;
import com.erp.server.srm.service.DeliveryOrderDetailService;
import com.erp.server.srm.service.DeliveryOrderService;
import com.erp.server.srm.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
/**
 * <p>
 * 送货单明细 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-01-12
 */
@Slf4j
@Service
public class DeliveryOrderDetailServiceImpl extends SuperServiceImpl<DeliveryOrderDetailMapper, DeliveryOrderDetailEntity> implements DeliveryOrderDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private DeliveryOrderService deliveryOrderService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Transactional(rollbackFor = Exception.class)
    public void add(List<DeliveryOrderDetailDTO.AddDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<DeliveryOrderDetailEntity> list = BeanMapperUtils.copyList(DeliveryOrderDetailEntity.class, detailList);

        //处理明细数据
        handleData(list,mainId);

        this.saveBatch(list);
    }

    @Override
    public List<DeliveryOrderDetailEntity> listByMainId(String mainId) {
        return this.lambdaQuery().eq(DeliveryOrderDetailEntity::getMainId, mainId).list();
    }

    @Override
    public List<DeliveryOrderDetailEntity> listByMainIdList(List<String> mainIdList) {
        return this.lambdaQuery().in(DeliveryOrderDetailEntity::getMainId, mainIdList).list();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<DeliveryOrderDetailDTO.UpdateDTO> updateDTOList,String mainId) {
        if(CollectionUtils.isEmpty(updateDTOList)){
            return true;
        }
        //数据校验
        checkDelivery(updateDTOList,mainId);

        List<DeliveryOrderDetailEntity> oldDetailList = this.listByMainId(mainId);
        Set<String> existDetailIds = updateDTOList.stream().map(DeliveryOrderDetailDTO.UpdateDTO::getDetailId).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        //处理删除的明细
        List<DeliveryOrderDetailEntity> needDeleteDetailList = oldDetailList.stream().filter(v->!existDetailIds.contains(v.getId())).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(needDeleteDetailList)){
            needDeleteDetailList.forEach(v->{
                String msg = StrUtil.format("用户【{}】删除sku为【{}】的送货单明细 ", UserContext.getDefaultLoginUser().getUserName(), v.getSkuNo());
                operateLogService.addModuleOperateLog(msg,ModuleTypeEnum.DELIVERY_ORDER.getCode(),v.getMainId(),"删除操作");
            });
            if(!this.removeByIds(needDeleteDetailList.stream().map(BaseEntity::getId).collect(Collectors.toList()))){
                throw new ServiceException("送货单明细删除失败");
            }
        }
        //处理修改的明细
        List<DeliveryOrderDetailEntity> needUpdateDetailList = oldDetailList.stream().filter(v->existDetailIds.contains(v.getId())).collect(Collectors.toList());
        Map<String,DeliveryOrderDetailDTO.UpdateDTO> updateDTOMap = updateDTOList.stream().collect(Collectors.toMap(DeliveryOrderDetailDTO.UpdateDTO::getDetailId, Function.identity()));
        for(DeliveryOrderDetailEntity deliveryOrderDetailEntity : needUpdateDetailList){
            DeliveryOrderDetailEntity old =  DeliveryOrderConverter.INSTANCE.detailConvert(deliveryOrderDetailEntity);
            DeliveryOrderDetailDTO.UpdateDTO updateDTO = updateDTOMap.get(deliveryOrderDetailEntity.getId());
            BeanUtil.copyProperties(updateDTO,deliveryOrderDetailEntity);
            String msg = StrUtil.format("用户【{}】修改sku为【{}】的送货单明细 ", UserContext.getDefaultLoginUser().getUserName(), deliveryOrderDetailEntity.getSkuNo());
            operateLogService.addModuleOperateLogByObj(old, deliveryOrderDetailEntity, ModuleTypeEnum.DELIVERY_ORDER.getCode(), deliveryOrderDetailEntity.getMainId(), msg);
        }
        if(CollectionUtils.isNotEmpty(needUpdateDetailList)){
            if(!this.updateBatchById(needUpdateDetailList)){
                throw new ServiceException("送货单明细更新失败");
            }
        }


        //处理新增的明细
        List<DeliveryOrderDetailDTO.UpdateDTO> needAddDTOList = updateDTOList.stream().filter(v->StringUtils.isBlank(v.getDetailId())).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(needAddDTOList)){
            return true;
        }
        List<DeliveryOrderDetailEntity> list = BeanMapperUtils.copyList(DeliveryOrderDetailEntity.class, needAddDTOList);
        //处理明细数据
        handleData(list,mainId);
        if(! this.saveBatch(list)){
            throw new ServiceException("送货单明细新增失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByMainIds(List<String> mainIds) {
        List<DeliveryOrderDetailEntity> detailEntityList = this.lambdaQuery().in(DeliveryOrderDetailEntity::getMainId, mainIds).list();
        return this.removeByIds(detailEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
    }

    @Override
    public Map<String,List<DeliveryOrderDetailDTO.PrintDTO>> mapPrintByMainIds(List<String> mainIds) {
        List<DeliveryOrderDetailEntity> detailEntityList = this.lambdaQuery().in(DeliveryOrderDetailEntity::getMainId, mainIds).list();
        List<DeliveryOrderDetailDTO.PrintDTO> printDTOList = BeanMapperUtils.copyList(DeliveryOrderDetailDTO.PrintDTO.class, detailEntityList);
        List<String> skuIdList = detailEntityList.stream().map(DeliveryOrderDetailEntity::getSkuId).collect(Collectors.toList());
        Map<String,SkuVO> skuMap = plmTaskFeign.listSkuProductByIds(skuIdList).stream().collect(Collectors.toMap(SkuVO::getSkuId,Function.identity(),(v1, v2)->v1));
        printDTOList.forEach(v->{
            SkuVO skuVO = skuMap.get(v.getSkuId());
            if(Objects.nonNull(skuVO)){
                v.setUnit(skuVO.getUnitName());
            }
        });
        return printDTOList.stream().collect(Collectors.groupingBy(DeliveryOrderDetailDTO.PrintDTO::getMainId));
    }

    @Override
    public List<DeliveryOrderDetailEntity> listDetailByDetailSourceIds(List<String> purchaseDetailIds) {
        if(CollectionUtils.isEmpty(purchaseDetailIds)){
            return new ArrayList<>();
        }
        return this.lambdaQuery().in(DeliveryOrderDetailEntity::getSourceDetailId, purchaseDetailIds).list();
    }

    /**
     * 获取明细和主表状态
     * @param purchaseDetailIds
     * @return
     */
    @Override
    public List<DeliveryOrderDetailDTO.ListDTO> listDetailDTOByDetailSourceIds(List<String> purchaseDetailIds) {
        return baseMapper.listDetailDTOByDetailSourceIds(purchaseDetailIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateDeliveryDetail(List<DeliveryOrderDetailEntity> detailEntityGroupList) {
        this.updateBatchById(detailEntityGroupList);
        List<String> mainIds = detailEntityGroupList.stream().map(DeliveryOrderDetailEntity::getMainId).distinct().collect(Collectors.toList());
        return deliveryOrderService.updateReceiveStatus(mainIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean confirmReceiveStatus(List<String> detailIds) {
        if(CollectionUtils.isEmpty(detailIds)){
            return true;
        }
        List<DeliveryOrderDetailEntity> detailList = this.listByIds(detailIds);
        detailList.forEach(v-> {
            v.setReceiptStatus(DeliveryOrderEnum.ReceiptStatusEnum.CONFIRMED.getCode());
            v.setConfirmReceiveDate(LocalDate.now());
        });
        this.updateBatchById(detailList);
        List<String> mainIds = detailList.stream().map(DeliveryOrderDetailEntity::getMainId).distinct().collect(Collectors.toList());
        deliveryOrderService.updateReceiveStatus(mainIds);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unConfirmReceiveStatus(List<String> detailIds) {
        if(CollectionUtils.isEmpty(detailIds)){
            return true;
        }
        List<DeliveryOrderDetailEntity> detailList = this.listByIds(detailIds);
        detailList.forEach(v-> {
            v.setReceiptStatus(DeliveryOrderEnum.ReceiptStatusEnum.WAIT_CONFIRMED.getCode());
            v.setConfirmReceiveDate(null);
        });
        this.updateBatchById(detailList);
        List<String> mainIds = detailList.stream().map(DeliveryOrderDetailEntity::getMainId).distinct().collect(Collectors.toList());
        deliveryOrderService.updateReceiveStatus(mainIds);
        //收货单反确认删除对账明细
        deliveryOrderService.removePoReconciliationDetail(mainIds);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelReceive(List<String> detailIds) {
        if(CollectionUtils.isEmpty(detailIds)){
            return true;
        }
        List<DeliveryOrderDetailEntity> detailList = this.listByIds(detailIds);
        detailList.forEach(v-> {
            v.setReceiptStatus("");
            v.setReceiveCode("");
            v.setConfirmReceiveDate(null);
            v.setReceiveUserId("");
            v.setReceiveUserName("");
            v.setReceiveQty(0);
            v.setGiftReceiveQty(0);
        });
        this.updateBatchById(detailList);
        List<String> mainIds = detailList.stream().map(DeliveryOrderDetailEntity::getMainId).distinct().collect(Collectors.toList());
        deliveryOrderService.updateReceiveStatus(mainIds);
        return true;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(List<DeliveryOrderDetailEntity> detailList, String mainId) {
        detailList.forEach(v->v.setMainId(mainId));
        //添加操作日志
        if (CollectionUtils.isNotEmpty(detailList)) {
            String msg = StrUtil.format("用户【{}】新增sku为【%s】的送货单明细 ", UserContext.getDefaultLoginUser().getUserName());
            List<Pair<String, String>> addPairList = detailList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_ORDER.getCode(), addPairList, "编辑操作");
        }
    }

    /**
     * 校验送货数量是否超过可送货数量
     * @return true:通过
     */
    private void checkDelivery(String sourceDetailId,String detailId,Integer deliveryQty,Integer orderQty){
        List<DeliveryOrderDetailEntity> sameSourceDetailList = this.listDetailByDetailSourceIds(Collections.singletonList(sourceDetailId));
        if(CollectionUtils.isEmpty(sameSourceDetailList)){
            if(orderQty < deliveryQty){
                throw new ServiceException("送货数量不可超过【采购数量-累计已送货数量】");
            }
        }
        if(StringUtils.isNotBlank(detailId)){
            sameSourceDetailList = sameSourceDetailList.stream().filter(v->!v.getId().equals(detailId)).collect(Collectors.toList());
        }
        Integer nowDeliveryQty = sameSourceDetailList.stream().mapToInt(DeliveryOrderDetailEntity::getDeliveryQty).sum();
        if(orderQty < deliveryQty + nowDeliveryQty){
            throw new ServiceException("送货数量不可超过【采购数量-累计已送货数量】");
        }
    }
    /**
     * 数据校验
     * @author will
     * @date 2024/9/19 11:13
     * @param updateDTOList
     * @param mainId
     */
    private void checkDelivery (List<DeliveryOrderDetailDTO.UpdateDTO> updateDTOList,String mainId) {
        if (CollectionUtils.isEmpty(updateDTOList)) {
            return;
        }
        //送货主表信息
        DeliveryOrderEntity deliveryOrderEntity = deliveryOrderService.getById(mainId);
        if (ObjectUtil.isEmpty(deliveryOrderEntity)) {
            throw new ServiceException("送货单主表信息未找到");
        }

        // 采购订单明细id集合
        List<String> podIds = updateDTOList.stream().map(DeliveryOrderDetailDTO.UpdateDTO::getSourceDetailId).collect(Collectors.toList());
        //送货信息
        List<DeliveryOrderDetailDTO.ListDTO> deliveryOrderDetailList = this.listDetailDTOByDetailSourceIds(podIds);
        //查询采购签收信息
        List<WarehouseReceiveDTO.PurchaseOrderDetailDTO> receiveList = wmsTaskFeign.getReceiveListByPurchaseOrderIds(Arrays.asList(deliveryOrderEntity.getSourceId()));
        //入库信息
        List<PoInstockDetailEntity> stockInDetailList = wmsTaskFeign.listPurchaseStockInDetailByPodIds(podIds);
        //退货信息
        List<PoReturnDetailEntity> returnOrderDetailList = wmsTaskFeign.listReturnOrderDetailByPodIds(podIds);

        for (DeliveryOrderDetailDTO.UpdateDTO updateDTO : updateDTOList) {
            //已送货数量
            Integer deliveryQty = MathUtil.ZERO;
            //有送货单的收货数量
            Integer hasDeliveryReceiveQty = MathUtil.ZERO;
            //无送货单收货数量
            Integer unDeliveryReceiveQty = MathUtil.ZERO;
            //无收货单的入库数量
            Integer unReceiveInstockQty = MathUtil.ZERO;
            //收发差异
            Integer diffSendAndReceive = MathUtil.ZERO;
            //退货补货数量
            Integer returnQty = MathUtil.ZERO;
            //收货数量
            if (CollectionUtils.isNotEmpty(receiveList)) {
                hasDeliveryReceiveQty = receiveList.stream().filter(e -> org.apache.commons.lang3.StringUtils.isNotEmpty(e.getSourceType())
                                && e.getSourceType().equalsIgnoreCase(SourceTypeEnum.DELIVERY_ORDER.getCode())
                                && e.getPurchaseOrderDetailId().equals(updateDTO.getSourceDetailId()))
                        .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                unDeliveryReceiveQty = receiveList.stream().filter(e -> StringUtils.isEmpty(e.getSourceId()) && e.getPurchaseOrderDetailId().equals(updateDTO.getSourceDetailId()))
                        .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            //无收货单的入库数量
            if (CollectionUtils.isNotEmpty(stockInDetailList)){
                // 采购入库单（无收货单），只有审核通过的才占用库存数量
                unReceiveInstockQty = stockInDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(updateDTO.getSourceDetailId())
                                && Objects.equals(e.getSourceDetailId(), updateDTO.getSourceDetailId())
                                && Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus()) )
                        .map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);

            }
            // 退货单（退货补货的才会导致在途数量变化）
            if (CollectionUtils.isNotEmpty(returnOrderDetailList)){
                returnQty = returnOrderDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(updateDTO.getSourceDetailId())
                                && Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())
                                && StrUtils.isNotEmpty(e.getPurchaseOrderDetailId())
                                && Objects.equals(e.getReturnMode(), ReturnModeEnum.REPLENISHMENT.getCode()))
                        .map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            //已送货数量
            if (CollectionUtils.isNotEmpty(deliveryOrderDetailList)) {
                deliveryQty = deliveryOrderDetailList.stream().filter(e -> e.getSourceDetailId().equals(updateDTO.getSourceDetailId()))
                        .map(DeliveryOrderDetailDTO.ListDTO::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
                //收发差异
                //发货数量 - 已审核收货数量
                Integer srmDeliveryQty = deliveryOrderDetailList.stream().filter(e -> e.getSourceDetailId().equals(updateDTO.getSourceDetailId())
                                && com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(e.getReceiptStatus()) && e.getReceiptStatus().equals(DeliveryOrderEnum.ReceiptStatusEnum.CONFIRMED.getCode()) )
                        .map(DeliveryOrderDetailDTO.ListDTO::getDeliveryQty)
                        .reduce(MathUtil.ZERO, Integer::sum);
                diffSendAndReceive = srmDeliveryQty - hasDeliveryReceiveQty;
            }
            //待送货数量
            Integer unDeliveryQty = updateDTO.getOrderQty() - deliveryQty - unDeliveryReceiveQty - unReceiveInstockQty + diffSendAndReceive + returnQty;
            if (updateDTO.getDeliveryQty() > unDeliveryQty) {
                throw new ServiceException(StrUtil.format("送货数量不能超过【{}】",unDeliveryQty));
            }
        }
    }
}
