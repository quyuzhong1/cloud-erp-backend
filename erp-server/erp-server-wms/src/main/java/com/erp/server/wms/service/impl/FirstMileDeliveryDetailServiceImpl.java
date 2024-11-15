package com.erp.server.wms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.FirstMileDeliveryDetailDTO;
import com.erp.model.wms.dto.WmsCartonSpecDTO;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.model.wms.entity.PackingTaskEntity;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.FirstMileDeliveryDetailMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
/**
 * <p>
 * 头程发货单明细表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Slf4j
@Service
public class FirstMileDeliveryDetailServiceImpl extends SuperServiceImpl<FirstMileDeliveryDetailMapper, FirstMileDeliveryDetailEntity> implements FirstMileDeliveryDetailService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private FirstMileDeliveryService firstMileDeliveryService;
    @Resource
    private WmsCartonSpecService wmsCartonSpecService;
    @Resource
    private PackingTaskService packingTaskService;
    @Resource
    private SkuMappingFeign skuMappingFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(FirstMileDeliveryDTO.AddDTO addDTO, String mainId) {
        List<FirstMileDeliveryDetailDTO.AddDTO> detailList = addDTO.getDetailList();
        //映射字段
        List<FirstMileDeliveryDetailEntity> list = BeanMapperUtils.copyList(FirstMileDeliveryDetailEntity.class, detailList);
        //处理明细数据
        handleData(list, mainId, Boolean.FALSE, addDTO.getDeliveryWarehouseId());
        //批量新增
        this.saveBatch(list);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(FirstMileDeliveryDTO.UpdateDTO updateDTO, String mainId) {
        List<FirstMileDeliveryDetailDTO.UpdateDTO> detailList = updateDTO.getDetailList();
        //原明细数据
        List<FirstMileDeliveryDetailEntity> oldList = this.listByMainIds(Arrays.asList(mainId));
        List<String> deleteIds = getDeleteIds(detailList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<FirstMileDeliveryDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        //映射字段
        List<FirstMileDeliveryDetailEntity> list = BeanMapperUtils.copyList(FirstMileDeliveryDetailEntity.class, detailList);
        //处理明细数据
        handleData(list, mainId, Boolean.FALSE, updateDTO.getDeliveryWarehouseId());

        //新增或修改明细
        this.saveOrUpdateBatch(list);
    }

    @Override
    public Boolean removeByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Boolean.FALSE;
        }
        return lambdaUpdate().in(FirstMileDeliveryDetailEntity::getMainId,mainIds).remove();
    }

    @Override
    public List<FirstMileDeliveryDetailEntity> listBySourceDetailIds(List<String> detailIds) {
        if (CollectionUtils.isEmpty(detailIds)) {
            return Collections.emptyList();
        }
        List<FirstMileDeliveryDetailEntity> list = baseMapper.listBySourceDetailIds(detailIds);
        return list;
    }

    @Override
    public List<FirstMileDeliveryDetailEntity> listByMainIds(List<String> mainIds) {
        if(CollectionUtils.isEmpty(mainIds)){
            return new ArrayList<>();
        }
        return lambdaQuery().in(FirstMileDeliveryDetailEntity::getMainId, mainIds).list();
    }

//    @Override
//    public  List<FirstMileDeliveryDTO.GroupSkuDTO> listGroupSkuByMainId(String mainId) {
//        List<FirstMileDeliveryDTO.GroupSkuDTO> list = baseMapper.listGroupSkuByMainId(mainId);
//        FirstMileDeliveryEntity entity = firstMileDeliveryService.getById(mainId);
//        //查询产品信息
//        List<String> skuIdList = list.stream().map(FirstMileDeliveryDTO.GroupSkuDTO::getSkuId).distinct().collect(Collectors.toList());
//        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);
//        //装箱任务
//        List<PackingTaskEntity> taskEntityList = packingTaskService.listBySourceIdAndSourceType(mainId, entity.getSourceType());
//        List<WmsCartonSpecDTO.PackingQtyDTO> packingQtyDTOS = new ArrayList<>();
//        if (CollectionUtils.isNotEmpty(taskEntityList)){
//            PackingTaskEntity taskEntity = taskEntityList.get(0);
//            //查询已装箱数
//            packingQtyDTOS = wmsCartonSpecService.listPackingQtyByMainId(taskEntity.getId(), null);
//        }
//        for (FirstMileDeliveryDTO.GroupSkuDTO groupSkuDTO : list) {
//            //待装箱数量=发货数量-已装箱数量
//            int usePackQty = packingQtyDTOS.stream()
//                    .filter(req -> req.getSkuId().equals(groupSkuDTO.getSkuId()))
//                    .mapToInt(WmsCartonSpecDTO.PackingQtyDTO::getPackQty).sum();
//            groupSkuDTO.setWaitPackQty(groupSkuDTO.getDeliveryQty() - usePackQty);
//            groupSkuDTO.setPackQty(usePackQty);
//            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(groupSkuDTO.getSkuId())).findFirst().orElse(new SkuVO());
//            groupSkuDTO.setProductName(skuVO.getSkuName());
//        }
//        return list;
//    }

    @Override
    public List<FirstMileDeliveryDetailEntity> listDetailByMainId(String id) {
        if(CharSequenceUtil.isBlank(id)){
            return new ArrayList<>();
        }
        return lambdaQuery().eq(FirstMileDeliveryDetailEntity::getMainId,id).list();
    }

    @Override
    public List<FirstMileDeliveryDetailEntity> listApprovedByFbaShipmentCodes(List<String> fbaShipmentCodeList) {

        if(CollectionUtils.isEmpty(fbaShipmentCodeList)){
            return new ArrayList<>();
        }
        List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntityList = lambdaQuery().in(FirstMileDeliveryDetailEntity::getFbaShipmentCode, fbaShipmentCodeList).list();
        if(CollectionUtils.isEmpty(firstMileDeliveryDetailEntityList)){
            return new ArrayList<>();
        }
        List<String> mainIds = firstMileDeliveryDetailEntityList.stream().map(v->v.getMainId()).distinct().collect(Collectors.toList());
        List<FirstMileDeliveryEntity> firstMileDeliveryEntityList = firstMileDeliveryService.listByIds(mainIds);
        firstMileDeliveryEntityList = firstMileDeliveryEntityList.stream().filter(v->v.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getCode())).collect(Collectors.toList());
        List<String> currentIds = firstMileDeliveryEntityList.stream().map(v->v.getId()).collect(Collectors.toList());
        firstMileDeliveryDetailEntityList = firstMileDeliveryDetailEntityList.stream().filter(v->currentIds.contains(v.getMainId())).collect(Collectors.toList());
        return firstMileDeliveryDetailEntityList;
    }

    @Override
    public List<FirstMileDeliveryDetailEntity> listByFbaShipmentCodes(List<String> shipmentCodes) {
        if (CollectionUtils.isEmpty(shipmentCodes)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(FirstMileDeliveryDetailEntity::getFbaShipmentCode, shipmentCodes).list();

    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<FirstMileDeliveryDetailEntity> list, String mainId, Boolean isUpdate, String deliveryWarehouseId) {
        //需要新增的数据
        List<FirstMileDeliveryDetailEntity> addList = list.stream().filter(c -> CharSequenceUtil.isBlank(c.getId())).collect(Collectors.toList());

        //获取sku信息
        List<String> skuIds = list.stream().map(FirstMileDeliveryDetailEntity::getSkuId).collect(Collectors.toList());
        //产品名称
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);
        if (CollectionUtils.isEmpty(skuVOList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        List<FirstMileDeliveryDetailEntity> oldList = this.listByMainIds(Arrays.asList(mainId));

        //查询库存sku
        List<SkuMappingDTO.ListSkuParamDTO> skuParamDTOList = new ArrayList<>();
        for (FirstMileDeliveryDetailEntity detailEntity : list) {
            SkuMappingDTO.ListSkuParamDTO paramDTO = new SkuMappingDTO.ListSkuParamDTO();
            paramDTO.setSkuNo(detailEntity.getSkuNo());
            paramDTO.setWarehouseId(deliveryWarehouseId);
            skuParamDTOList.add(paramDTO);
        }
        List<SkuMappingDTO.ListSkuDTO> listSkuDTOS = skuMappingFeign.listBySkuNoList(skuParamDTOList);


        for (FirstMileDeliveryDetailEntity firstMileDeliveryDetailEntity : list) {
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(firstMileDeliveryDetailEntity.getSkuNo())).findFirst().orElse(new SkuVO());
            firstMileDeliveryDetailEntity.setMainId(mainId);
            firstMileDeliveryDetailEntity.setProductName(skuVO.getSkuName());
            firstMileDeliveryDetailEntity.setWarehouseLocation(firstMileDeliveryDetailEntity.getWarehouseLocation());

            //库存sku
            String stockSku = listSkuDTOS.stream()
                    .filter(req -> req.getProductSkuId().equals(firstMileDeliveryDetailEntity.getSkuId())
                            && req.getWarehouseId().equals(deliveryWarehouseId))
                    .distinct().findFirst()
                    .flatMap(obj -> Optional.ofNullable(obj.getWarehouseId())).orElse("");
            firstMileDeliveryDetailEntity.setStockSku(stockSku);

            //校验是否是修改，如果是就新增修改日志
            if (CharSequenceUtil.isNotBlank(firstMileDeliveryDetailEntity.getId())) {
                FirstMileDeliveryDetailEntity old = oldList.stream().filter(obj -> obj.getId().equals(firstMileDeliveryDetailEntity.getId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_NOT_FBA_DELIVERY_DETAIL);
                }
                operateLogService.addModuleOperateLogByObj(old, firstMileDeliveryDetailEntity, ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(),mainId,"",String.format("【%s】",old.getSkuNo()));
            }
        }
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList) && isUpdate) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), addPairList, "编辑操作");
        }
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<FirstMileDeliveryDetailDTO.UpdateDTO> newList, List<FirstMileDeliveryDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> CharSequenceUtil.isNotBlank(g.getId())).
                map(FirstMileDeliveryDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(FirstMileDeliveryDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }
}
