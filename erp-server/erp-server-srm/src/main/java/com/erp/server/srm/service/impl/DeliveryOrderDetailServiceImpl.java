package com.erp.server.srm.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.srm.dto.DeliveryOrderDetailDTO;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.entity.DeliveryOrderDetailEntity;
import com.erp.model.srm.entity.DeliveryOrderEntity;
import com.erp.model.wms.enums.PoReturnConfirmStatusEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.PurchaseOrderFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.srm.mapper.DeliveryOrderDetailMapper;
import com.erp.server.srm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
    @Autowired
    private CommonService commonService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private DeliveryOrderService deliveryOrderService;

    @Resource
    private PoReconciliationDetailScmService poReconciliationDetailScmService;

    @Resource
    private ScmTaskFeign scmTaskFeign;


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
        List<DeliveryOrderDetailEntity> oldDetailList = this.listByMainId(mainId);
        Set<String> existDetailIds = updateDTOList.stream().map(DeliveryOrderDetailDTO.UpdateDTO::getDetailId).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        //处理删除的明细
        List<DeliveryOrderDetailEntity> needDeleteDetailList = oldDetailList.stream().filter(v->!existDetailIds.contains(v.getId())).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(needDeleteDetailList)){
            if(!this.removeByIds(needDeleteDetailList.stream().map(BaseEntity::getId).collect(Collectors.toList()))){
                throw new ServiceException("送货单明细删除失败");
            }
        }
        //处理修改的明细
        List<DeliveryOrderDetailEntity> needUpdateDetailList = oldDetailList.stream().filter(v->existDetailIds.contains(v.getId())).collect(Collectors.toList());
        Map<String,DeliveryOrderDetailDTO.UpdateDTO> updateDTOMap = updateDTOList.stream().collect(Collectors.toMap(DeliveryOrderDetailDTO.UpdateDTO::getDetailId, Function.identity()));
        for(DeliveryOrderDetailEntity deliveryOrderDetailEntity : needUpdateDetailList){
            DeliveryOrderDetailDTO.UpdateDTO updateDTO = updateDTOMap.get(deliveryOrderDetailEntity.getId());
            BeanUtil.copyProperties(updateDTO,deliveryOrderDetailEntity);

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
        Map<String,SkuVO> skuMap = plmTaskFeign.getSkuInfoByIds(skuIdList).stream().collect(Collectors.toMap(SkuVO::getSkuId,Function.identity(),(v1, v2)->v1));
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

    @Override
    public void deliveryOrderConfirm(List<String> idList) {
        if(CollectionUtils.isEmpty(idList)){
            return ;
        }
        //TODO 收货单确认，反确认

        //收货单确认生成对账明细
        addPoReconciliationDetail(idList);
    }

    @Override
    public void deliveryOrderUnConfirm(List<String> idList) {
        if(CollectionUtils.isEmpty(idList)){
            return ;
        }
        //TODO 收货单确认，反确认

        //收货单反确认删除对账明细
        removePoReconciliationDetail(idList);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<DeliveryOrderDetailEntity> detailList, String mainId) {
        detailList.forEach(v->v.setMainId(mainId));
        //添加操作日志
        if (CollectionUtils.isNotEmpty(detailList)) {
            List<Pair<String, String>> addPairList = detailList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.OVERSEAS_DELIVERY_PLAN.getCode(), addPairList, "编辑操作");
        }
    }

    /**
     * @description: 添加对账明细
     * @author Will
     * @date: 2024/1/26 9:24
     * @param idList
     */
    private void addPoReconciliationDetail (List<String> idList) {
        if (CollectionUtils.isNotEmpty(idList)) {
            return;
        }
        //送货单
        List<DeliveryOrderEntity> deliveryOrderList = deliveryOrderService.listByIds(idList);

        //送货明细
        List<String> deliveryOrderIdList = deliveryOrderList.stream().map(DeliveryOrderEntity::getId).distinct().collect(Collectors.toList());
        List<DeliveryOrderDetailEntity> deliveryOrderDetailList = this.listByMainIdList(deliveryOrderIdList);

        //采购订单
        List<String> sourceIdList = deliveryOrderList.stream().map(DeliveryOrderEntity::getSourceId).collect(Collectors.toList());
        List<PurchaseOrderEntity> purchaseOrderList = scmTaskFeign.listPurchaseOrderByIds(sourceIdList);

        //添加信息
        List<PoReconciliationDetailDTO.AddDTO> addList = new ArrayList<>();
        for (DeliveryOrderDetailEntity detailEntity :deliveryOrderDetailList) {
            PoReconciliationDetailDTO.AddDTO addDTO = new PoReconciliationDetailDTO.AddDTO();
            //送货单主表
            DeliveryOrderEntity deliveryOrderEntity = deliveryOrderList.stream().filter(obj -> StrUtil.equals(obj.getId(), detailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(deliveryOrderEntity)) {
                throw new ServiceException(ApiError.ERROR_DELIVERY_ORDER_NOT_EXIST);
            }

            addDTO.setPoId(deliveryOrderEntity.getSourceId());
            addDTO.setPoCode(deliveryOrderEntity.getSourceCode());
            addDTO.setPodId(detailEntity.getSourceDetailId());
            addDTO.setSupplierId(deliveryOrderEntity.getSupplierId());
            addDTO.setSourceId(deliveryOrderEntity.getId());
            addDTO.setSourceDetailId(detailEntity.getId());
            addDTO.setSourceCode(deliveryOrderEntity.getCode());
            addDTO.setSourceType(SourceTypeEnum.DELIVERY_ORDER.getCode());
            addDTO.setConfirmDate(deliveryOrderEntity.getConfirmReceiveDate());
            addDTO.setSkuId(detailEntity.getSkuId());
            addDTO.setDeliveryQty(detailEntity.getDeliveryQty());
            addDTO.setReceiveQty(detailEntity.getReceiveQty());
            addDTO.setBusinessStatus(PoReturnConfirmStatusEnum.CONFIRM.getCode());

            //采购订单
            PurchaseOrderEntity purchaseOrderEntity = purchaseOrderList.stream().filter(obj -> StrUtil.equals(deliveryOrderEntity.getSourceId(), obj.getId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(purchaseOrderEntity)) {
                throw new ServiceException(ApiError.ERROR_98025);
            }
            addDTO.setSettleOrgId(purchaseOrderEntity.getPurchaseOrgId());

            addList.add(addDTO);
        }
        poReconciliationDetailScmService.add(addList);
    }

    /**
     * @description: 添加对账明细
     * @author Will
     * @date: 2024/1/26 9:24
     * @param idList
     */
    private void removePoReconciliationDetail (List<String> idList) {
        if (CollectionUtils.isNotEmpty(idList)) {
            return;
        }
        //送货单
        List<DeliveryOrderEntity> deliveryOrderList = deliveryOrderService.listByIds(idList);
        if (CollectionUtils.isEmpty(deliveryOrderList)) {
            throw new ServiceException(ApiError.ERROR_DELIVERY_ORDER_NOT_EXIST);
        }
        //送货明细
        List<String> deliveryOrderIdList = deliveryOrderList.stream().map(DeliveryOrderEntity::getId).distinct().collect(Collectors.toList());
        List<DeliveryOrderDetailEntity> deliveryOrderDetailList = this.listByMainIdList(deliveryOrderIdList);
        if (CollectionUtils.isEmpty(deliveryOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_DELIVERY_ORDER_DETAIL_NOT_EXIST);
        }
        List<String> detailIdList = deliveryOrderDetailList.stream().map(DeliveryOrderDetailEntity::getId).collect(Collectors.toList());
        poReconciliationDetailScmService.deleteDetailBySourceDetailIdList(detailIdList);
    }
}
