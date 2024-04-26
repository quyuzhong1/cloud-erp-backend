package com.erp.server.wms.service.impl;


import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.FirstMileDeliveryDetailDTO;
import com.erp.model.wms.dto.WmsCartonDTO;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.FirstMileDeliveryDetailMapper;
import com.erp.server.wms.service.FirstMileDeliveryDetailService;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.WmsCartonDetailService;
import com.erp.server.wms.service.WmsCartonService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private PlmTaskFeign plmTaskFeign;
    @Autowired
    private OmsListingInfoFeign omsListingInfoFeign;
    @Autowired
    private WmsCartonService wmsCartonService;
    @Autowired
    private WmsCartonDetailService wmsCartonDetailService;
    @Autowired
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

    @Override
    public  List<FirstMileDeliveryDTO.GroupSkuDTO> listGroupSkuByMainId(String mainId) {
        List<FirstMileDeliveryDTO.GroupSkuDTO> list = baseMapper.listGroupSkuByMainId(mainId);

        //查询产品信息
        List<String> skuIdList = list.stream().map(req -> req.getSkuId()).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);

        //查询已装箱数
        List<WmsCartonDTO.PackingQtyDTO> packingQtyDTOS = wmsCartonService.listPackingQtyByMainId(mainId, null);
        for (FirstMileDeliveryDTO.GroupSkuDTO groupSkuDTO : list) {
            //待装箱数量=发货数量-已装箱数量
            int usePackQty = packingQtyDTOS.stream()
                    .filter(req -> req.getSourceId().equals(groupSkuDTO.getId())
                            && req.getSkuId().equals(groupSkuDTO.getSkuId()))
                    .mapToInt(req -> req.getUsePackQty()).sum();
            groupSkuDTO.setWaitPackQty(groupSkuDTO.getDeliveryQty() - usePackQty);
            groupSkuDTO.setPackQty(usePackQty);
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(groupSkuDTO.getSkuId())).findFirst().orElse(new SkuVO());
            groupSkuDTO.setProductName(skuVO.getSkuName());
        }
        return list;
    }

    @Override
    public List<FirstMileDeliveryDTO.GroupSkuDTO> listCartonGroupSkuByMainId(String mainId , Integer boxSpecNo) {
        List<FirstMileDeliveryDTO.GroupSkuDTO> list = baseMapper.listCartonGroupSkuBySourceId(mainId, boxSpecNo);

        //查询产品信息
        List<String> skuIdList = list.stream().map(req -> req.getSkuId()).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);

        //查询已装箱数
        List<WmsCartonDTO.PackingQtyDTO> packingQtyDTOS = wmsCartonService.listPackingQtyByMainId(mainId, boxSpecNo);
        for (FirstMileDeliveryDTO.GroupSkuDTO groupSkuDTO : list) {
            //待装箱数量=发货数量-已装箱数量
            WmsCartonDTO.PackingQtyDTO packingQtyDTO = packingQtyDTOS.stream()
                    .filter(req -> req.getSourceId().equals(groupSkuDTO.getId())
                            && req.getSkuId().equals(groupSkuDTO.getSkuId()))
                    .findFirst().orElse(new WmsCartonDTO.PackingQtyDTO());
            groupSkuDTO.setWaitPackQty(groupSkuDTO.getDeliveryQty() - packingQtyDTO.getUsePackQty());
            groupSkuDTO.setPackQty(packingQtyDTO.getPackQty());
            groupSkuDTO.setCartonId(packingQtyDTO.getCartonId());

            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(groupSkuDTO.getSkuId())).findFirst().orElse(new SkuVO());
            groupSkuDTO.setProductName(skuVO.getSkuName());
        }
        return list;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<FirstMileDeliveryDetailEntity> list, String mainId, Boolean isUpdate, String deliveryWarehouseId) {
        //需要新增的数据
        List<FirstMileDeliveryDetailEntity> addList = list.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());

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
            if (StringUtils.isNotBlank(firstMileDeliveryDetailEntity.getId())) {
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
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(FirstMileDeliveryDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(FirstMileDeliveryDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }
}
