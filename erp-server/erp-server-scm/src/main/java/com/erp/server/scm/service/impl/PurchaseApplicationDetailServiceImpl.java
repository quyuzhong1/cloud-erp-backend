package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchaseApplicationDetailDTO;
import com.erp.model.scm.entity.PurchaseApplicationDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.mapper.PurchaseApplicationDetailMapper;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.PurchaseApplicationDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
public class PurchaseApplicationDetailServiceImpl extends SuperServiceImpl<PurchaseApplicationDetailMapper, PurchaseApplicationDetailEntity> implements PurchaseApplicationDetailService {

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Override
    public void add(List<PurchaseApplicationDetailDTO.AddDTO> details, String purchaseApplicationId) {
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        List<PurchaseApplicationDetailEntity> list = BeanMapperUtils.copyList(PurchaseApplicationDetailEntity.class, details);
        doOpHandleDataId(list,purchaseApplicationId,Boolean.TRUE);
        this.saveBatch(list);
        //更新sku为不可删除标识
        List<String> skuIds = list.stream().map(PurchaseApplicationDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        plmTaskFeign.updateOccupyStatus(skuIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<PurchaseApplicationDetailDTO.UpdateDTO> details, String purchaseApplicationId) {
        if (details == null) {
            details = new ArrayList<>();
        }
        //原明细数据
        List<PurchaseApplicationDetailEntity> oldList = this.listByPurchaseApplicationId(purchaseApplicationId);
        List<String> deleteIds = getDeleteIds(details, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {

            List<PurchaseApplicationDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getPurchaseApplicationId(), obj.getSkuNo())).collect(Collectors.toList());
            moduleOperateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.PURCHASE_APPLICATION.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        List<PurchaseApplicationDetailEntity> newList = BeanMapperUtils.copyList(PurchaseApplicationDetailEntity.class, details);
        doOpHandleDataId(newList,purchaseApplicationId,Boolean.FALSE);
        this.saveOrUpdateBatch(newList);
        //更新sku为不可删除标识
        List<String> skuIds = newList.stream().map(PurchaseApplicationDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        plmTaskFeign.updateOccupyStatus(skuIds);
    }

    @Override
    public List<PurchaseApplicationDetailEntity> listByPurchaseApplicationId(String purchaseApplicationId) {
        return  lambdaQuery()
                .eq(PurchaseApplicationDetailEntity::getPurchaseApplicationId,purchaseApplicationId)
                .orderByAsc(PurchaseApplicationDetailEntity::getId)
                .list();
    }

    @Override
    public List<PurchaseApplicationDetailEntity> listByPurchaseApplicationIds(List<String> ids) {
        return  lambdaQuery().in(PurchaseApplicationDetailEntity::getPurchaseApplicationId,ids).list();
    }

    @Override
    public void removeByPurchaseApplicationIds(List<String> purchaseApplicationIds) {
        lambdaUpdate().in(PurchaseApplicationDetailEntity::getPurchaseApplicationId,purchaseApplicationIds).remove();
    }

    @Override
    public PurchaseApplicationDetailEntity getByPurchaseApplicationIdAndSkuId(String purchaseApplicationId, String skuId) {
        return lambdaQuery()
                .eq(PurchaseApplicationDetailEntity::getPurchaseApplicationId,purchaseApplicationId)
                .eq(PurchaseApplicationDetailEntity::getSkuId,skuId)
                .one();
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<PurchaseApplicationDetailDTO.UpdateDTO> newList, List<PurchaseApplicationDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(PurchaseApplicationDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(PurchaseApplicationDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * 处理明细中的数据id
     */
    private void doOpHandleDataId (List<PurchaseApplicationDetailEntity> newList,String purchaseApplicationId,Boolean isAdd) {
        //仓库信息
        List<String> destWarehouseIdList = newList.stream().map(PurchaseApplicationDetailEntity::getDestWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(destWarehouseIdList);
        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        List<String> orgIds = warehouseList.stream().map(WarehouseDTO.UpdateDTO::getOrgId).distinct().collect(Collectors.toList());

        //采购组织Ids
        List<String> purchaseOrgIds = newList.stream().map(PurchaseApplicationDetailEntity::getPurchaseOrgId).distinct().collect(Collectors.toList());
        purchaseOrgIds.addAll(orgIds);
        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(purchaseOrgIds);

        //产品信息
        List<String> skuIds = newList.stream().map(PurchaseApplicationDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuPurchaseByIds(skuIds);

        //添加操作日志
        List<PurchaseApplicationDetailEntity> addList = newList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(addList) && !isAdd) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(purchaseApplicationId, obj.getSkuNo())).collect(Collectors.toList());
            moduleOperateLogService.batchAddModuleOperateLog("新增了一条SKU【%s】", ModuleTypeEnum.PURCHASE_APPLICATION.getCode(), addPairList, "编辑操作");
        }

        for (PurchaseApplicationDetailEntity entity : newList) {
            entity.setPurchaseApplicationId(purchaseApplicationId);
            //仓库名称

            WarehouseDTO.UpdateDTO warehouseDTO = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getDestWarehouseId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(warehouseDTO)) {
                throw new ServiceException(ApiError.ERROR_99002);
            }
            entity.setDestWarehouseName(warehouseDTO.getName());
            entity.setReceiveOrgId(warehouseDTO.getOrgId());

            //核算公司
            if (CollectionUtils.isEmpty(accountingCompanyList)) {
                throw new ServiceException(ApiError.ERROR_9040);
            }

            //采购组织名称
            String purchaseOrgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getPurchaseOrgId())).map(BaseIdDTO.CodeDTO::getName).findFirst().orElse(null);
            entity.setPurchaseOrgName(purchaseOrgName);

            //收料组织名称
            String receiveOrgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getReceiveOrgId())).map(BaseIdDTO.CodeDTO::getName).findFirst().orElse(null);
            entity.setReceiveOrgName(receiveOrgName);

            //产品信息
            SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(entity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(skuVO)) {
                entity.setProductName(skuVO.getSkuName());
                entity.setVariantProperty(skuVO.getVariantProperty());
                entity.setSupplierId(skuVO.getSupplierId());
            }

            //修改操作日志
            if (StringUtils.isNotBlank(entity.getId())) {
                PurchaseApplicationDetailEntity old = this.getById(entity.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_98017);
                }
                moduleOperateLogService.addModuleOperateLogByObj(old,entity, ModuleTypeEnum.PURCHASE_APPLICATION.getCode(),purchaseApplicationId,"",String.format("【%s】",old.getSkuNo()));
            }
        }


    }

    @Override
    public List<PurchaseApplicationDetailDTO.PurchaseSkuQtyDTO> listSkuAndQty(List<String> sourceIds) {
        return baseMapper.listSkuAndQty(sourceIds);
    }
}
