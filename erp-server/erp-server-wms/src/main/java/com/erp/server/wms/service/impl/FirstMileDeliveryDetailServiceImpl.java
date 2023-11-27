package com.erp.server.wms.service.impl;


import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.FirstMileDeliveryDetailDTO;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.FirstMileDeliveryDetailMapper;
import com.erp.server.wms.service.FirstMileDeliveryDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * FBA发货单明细表 服务实现类
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
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.FBA_DELIVERY.getCode(),pairList,"编辑操作");
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
        return lambdaQuery().in(FirstMileDeliveryDetailEntity::getMainId, mainIds).list();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<FirstMileDeliveryDetailEntity> list, String mainId, Boolean isUpdate, String deliveryWarehouseId) {
        //需要新增的数据
        List<FirstMileDeliveryDetailEntity> addList = list.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());

        //获取sku信息
        List<String> skuNoList = list.stream().map(FirstMileDeliveryDetailEntity::getSkuNo).collect(Collectors.toList());
        //产品名称
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNoList);
        if (CollectionUtils.isEmpty(skuVOList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        List<FirstMileDeliveryDetailEntity> oldList = this.listByMainIds(Arrays.asList(mainId));

        //获取库存sku信息
        List<SkuMappingDTO.listStockSkuNoByProductSkuNoView> listStockSkuNoByProductSkuNoViews = omsListingInfoFeign.listStockSkuNoByProductSkuNo(skuNoList);

        for (FirstMileDeliveryDetailEntity firstMileDeliveryDetailEntity : list) {
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(firstMileDeliveryDetailEntity.getSkuNo())).findFirst().orElse(new SkuVO());
            firstMileDeliveryDetailEntity.setMainId(mainId);
            firstMileDeliveryDetailEntity.setProductName(skuVO.getSkuName());
            firstMileDeliveryDetailEntity.setWarehouseLocation(firstMileDeliveryDetailEntity.getWarehouseLocation());

            //库存sku
            String stockSku = listStockSkuNoByProductSkuNoViews.stream()
                    .filter(req -> req.getProductSkuNo().equals(firstMileDeliveryDetailEntity.getSkuNo())
                            && req.getWarehouseId().equals(deliveryWarehouseId))
                    .distinct()
                    .findFirst()
                    .flatMap(obj -> Optional.ofNullable(obj.getWarehouseSkuNo())).orElse("");
            firstMileDeliveryDetailEntity.setStockSku(stockSku);

            //校验是否是修改，如果是就新增修改日志
            if (StringUtils.isNotBlank(firstMileDeliveryDetailEntity.getId())) {
                FirstMileDeliveryDetailEntity old = oldList.stream().filter(obj -> obj.getId().equals(firstMileDeliveryDetailEntity.getId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_NOT_FBA_DELIVERY_DETAIL);
                }
                operateLogService.addModuleOperateLogByObj(old, firstMileDeliveryDetailEntity, ModuleTypeEnum.FBA_DELIVERY.getCode(),mainId,"",String.format("【%s】",old.getSkuNo()));
            }
        }
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList) && isUpdate) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.FBA_DELIVERY.getCode(), addPairList, "编辑操作");
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
