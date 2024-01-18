package com.erp.server.scm.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.dto.SubcontractChangeDetailDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.SubcontractChangeDetailEntity;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.mapper.SubcontractChangeDetailMapper;
import com.erp.server.scm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 委外变单明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-06-08
 */
@Slf4j
@Service
public class SubcontractChangeDetailServiceImpl extends SuperServiceImpl<SubcontractChangeDetailMapper, SubcontractChangeDetailEntity> implements SubcontractChangeDetailService {
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SupplierService supplierService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;

    @Resource
    private SubcontractOrderDetailService subcontractOrderDetailService;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    @Override
    public void add(List<SubcontractChangeDetailDTO.AddDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<SubcontractChangeDetailEntity> list = new ArrayList<>();
        for (SubcontractChangeDetailDTO.AddDTO addDTO : detailList) {
            SubcontractChangeDetailEntity entity = BeanMapperUtils.map(SubcontractChangeDetailEntity.class, addDTO);
            List<SubcontractChangeDetailDTO.AddDTO> addList = addDTO.getChildList();
            List<SubcontractChangeDetailDTO.UpdateDTO> updateList = BeanMapperUtils.copyList(SubcontractChangeDetailDTO.UpdateDTO.class, addList);
            entity.setChildList(updateList);
            list.add(entity);
        }
        //处理父子级数据
        List<SubcontractChangeDetailEntity> resultList = generateResultDetail(list, mainId,Boolean.TRUE);
        this.saveBatch(resultList);
    }

    @Override
    public void update(List<SubcontractChangeDetailDTO.UpdateDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<SubcontractChangeDetailEntity> list = BeanMapperUtils.copyList(SubcontractChangeDetailEntity.class, detailList);

        //原明细数据
        List<SubcontractChangeDetailEntity> oldList = this.listByMainIds(Arrays.asList(mainId));
        //将子级SKU添加入集合判断是否删除
        List<SubcontractChangeDetailDTO.UpdateDTO> allDetailList = new ArrayList<>();
        allDetailList.addAll(detailList);
        detailList.forEach(obj -> {
            allDetailList.addAll(obj.getChildList());
        });
        List<String> deleteIds = getDeleteIds(allDetailList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<SubcontractChangeDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            moduleOperateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.PURCHASE_ORDER.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }

        //处理父子级数据
        List<SubcontractChangeDetailEntity> resultList = generateResultDetail(list, mainId,Boolean.FALSE);

        this.saveOrUpdateBatch(resultList);
    }

    @Override
    public void removeByMainIds(List<String> mainIds) {
        lambdaUpdate().in(SubcontractChangeDetailEntity::getMainId,mainIds).remove();
    }

    @Override
    public List<SubcontractChangeDetailEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery().in(SubcontractChangeDetailEntity::getMainId,mainIds).list();
    }

    @Override
    public void updateSourceDetailId(List<Pair<String, String>> pairList) {
        if(CollectionUtils.isEmpty(pairList)) {
            return;
        }
        for (Pair<String, String> pair : pairList) {
            lambdaUpdate()
                    .eq(SubcontractChangeDetailEntity::getId,pair.getKey()).
                    set(SubcontractChangeDetailEntity::getSourceDetailId,pair.getValue())
                    .update();
        }
    }

    /**
     * 根据主表id查询父级SKU数据
     */
    private List<SubcontractChangeDetailEntity> listParentByMainId(String mainId) {
        return lambdaQuery()
                .eq(SubcontractChangeDetailEntity::getMainId,mainId)
                .eq(SubcontractChangeDetailEntity::getParentId,"")
                .list();
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<SubcontractChangeDetailDTO.UpdateDTO> newList, List<SubcontractChangeDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SubcontractChangeDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(SubcontractChangeDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * 生成明细结果
     */
    private List<SubcontractChangeDetailEntity> generateResultDetail (List<SubcontractChangeDetailEntity> newList, String mainId,Boolean isAdd) {
        List<SubcontractChangeDetailEntity> resultList = new ArrayList<>();
        //父级skuIds
        List<String> parentSkuIds = new ArrayList<>();
        //全部skuIds
        List<String> allSkuIds = new ArrayList<>();
        //仓库Ids
        List<String> warehouseIds = new ArrayList<>();
        //供应商Ids
        List<String> supplierIds = new ArrayList<>();

        //来源明细ids
        List<String> sourceDetailIds = new ArrayList<>();
        newList.forEach(obj -> {

            parentSkuIds.add(obj.getSkuId());

            allSkuIds.add(obj.getSkuId());
            List<String> skuIdList = obj.getChildList().stream().map(SubcontractChangeDetailDTO.UpdateDTO::getSkuId).collect(Collectors.toList());
            allSkuIds.addAll(skuIdList);

            warehouseIds.add(obj.getWarehouseId());
            List<String> warehouseIdList = obj.getChildList().stream().map(SubcontractChangeDetailDTO.UpdateDTO::getWarehouseId).collect(Collectors.toList());
            warehouseIds.addAll(warehouseIdList);

            //供应商信息
            if (StringUtils.isNotEmpty(obj.getSupplierId())) {
                supplierIds.add(obj.getSupplierId());
            }
            List<String> supplierIdList = obj.getChildList().stream().filter(e -> StringUtils.isNotEmpty(e.getSupplierId())).map(SubcontractChangeDetailDTO.UpdateDTO::getSupplierId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(supplierIdList)) {
                supplierIds.addAll(supplierIdList);
            }

            sourceDetailIds.add(obj.getSourceDetailId());
            List<String> sourceDetailList = obj.getChildList().stream().map(SubcontractChangeDetailDTO.UpdateDTO::getSourceDetailId).collect(Collectors.toList());
            sourceDetailIds.addAll(sourceDetailList);


        });

        //BOM信息
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(parentSkuIds);
        if (CollectionUtils.isEmpty(bomChildrenList)) {
            throw new ServiceException(ApiError.ERROR_95163);
        }
        //产品信息
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(allSkuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        //供应商信息
        List<SupplierEntity> supplierList = null;
        if (CollectionUtils.isNotEmpty(supplierIds)) {
            supplierList = supplierService.listByIds(supplierIds);
        }

        //仓库信息
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(warehouseIds);

        //委外变更单明细数据
        List<SubcontractOrderDetailEntity> subcontractOrderDetailList = subcontractOrderDetailService.listByIds(sourceDetailIds);

        for (SubcontractChangeDetailEntity detailEntity : newList) {
            //bom信息
            BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenList.stream().filter(obj -> obj.getParentSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(bomChildrenSkuDTO)) {
                throw new ServiceException(ApiError.ERROR_95163);
            }
            //父级SKU信息
            SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(skuVO)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            detailEntity.setIsAdd(Boolean.FALSE);
            //新增数据手动添加ID
            if (StringUtils.isBlank(detailEntity.getId())) {
                detailEntity.setId(IdWorker.getIdStr());
                detailEntity.setIsAdd(Boolean.TRUE);
            }
            //仓库名称
            if (CollectionUtils.isNotEmpty(warehouseList)) {
                String warehouseName = warehouseList.stream().filter(obj -> obj.getId().equals(detailEntity.getWarehouseId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                detailEntity.setWarehouseName(warehouseName);
            }
            //供应商名称
            if (CollectionUtils.isNotEmpty(supplierList)) {
                String supplierName = supplierList.stream().filter(obj -> obj.getId().equals(detailEntity.getSupplierId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                detailEntity.setSupplierName(supplierName);
            }
            //委外原数据
            SubcontractOrderDetailEntity subEntity = subcontractOrderDetailList.stream().filter(obj -> obj.getId().equals(detailEntity.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(subEntity)) {
                //委外变更不给改数量
                detailEntity.setQty(subEntity.getQty());
                detailEntity.setOldQty(subEntity.getQty());
                detailEntity.setOldPrice(subEntity.getPrice());
                detailEntity.setOldAmount(subEntity.getAmount());
                detailEntity.setOldDeliveryQty(subEntity.getDeliveryQty());
                detailEntity.setBomVersion(subEntity.getBomVersion());
            }

            detailEntity.setMainId(mainId);
            detailEntity.setVariantProperty(skuVO.getVariantProperty());
            detailEntity.setSkuNo(skuVO.getSkuNo());
            detailEntity.setBomVersion(StringUtils.isBlank(detailEntity.getBomVersion()) ? bomChildrenSkuDTO.getBomVersion() : detailEntity.getBomVersion());

            handleSupplierTaxPrice(detailEntity,subEntity,Boolean.FALSE);
            //子集SKU信息
            List<SubcontractChangeDetailEntity>   childList = BeanMapperUtils.copyList(SubcontractChangeDetailEntity.class, detailEntity.getChildList());
            for (SubcontractChangeDetailEntity childEntity : childList) {
                //产品信息
                SkuVO childSkuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(childEntity.getSkuId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(childSkuVO)) {
                    throw new ServiceException(ApiError.ERROR_95084);
                }
                //仓库名称
                if (CollectionUtils.isNotEmpty(warehouseList)) {
                    String warehouseName = warehouseList.stream().filter(obj -> obj.getId().equals(childEntity.getWarehouseId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                    childEntity.setWarehouseName(warehouseName);
                }
                //供应商名称
                if (CollectionUtils.isNotEmpty(supplierList)) {
                    String supplierName = supplierList.stream().filter(obj -> obj.getId().equals(childEntity.getSupplierId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                    childEntity.setSupplierName(supplierName);
                }
                //委外原数据
                SubcontractOrderDetailEntity childSubEntity = subcontractOrderDetailList.stream().filter(obj -> obj.getId().equals(childEntity.getSourceDetailId())).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(childSubEntity)) {
                    //委外变更不给改数量
                    childEntity.setQty(childSubEntity.getQty());
                    childEntity.setOldQty(childSubEntity.getQty());
                    childEntity.setOldPrice(childSubEntity.getPrice());
                    childEntity.setOldAmount(childSubEntity.getAmount());
                    childEntity.setOldDeliveryQty(childSubEntity.getDeliveryQty());
                    childEntity.setBomVersion(childSubEntity.getBomVersion());
                }

                childEntity.setMainId(mainId);
                childEntity.setParentId(detailEntity.getId());
                childEntity.setVariantProperty(childSkuVO.getVariantProperty());
                childEntity.setSkuNo(childSkuVO.getSkuNo());
                childEntity.setBomVersion(StringUtils.isBlank(childEntity.getBomVersion()) ? bomChildrenSkuDTO.getBomVersion() : childEntity.getBomVersion());

                handleSupplierTaxPrice(childEntity,childSubEntity,Boolean.TRUE);
            }
            resultList.add(detailEntity);
            resultList.addAll(childList);

        }
        //添加修改操作日志
        for (SubcontractChangeDetailEntity resultEntity : resultList) {
            SubcontractChangeDetailEntity old = this.getById(resultEntity.getId());
            if (ObjectUtils.isNotEmpty(old)) {
                moduleOperateLogService.addModuleOperateLogByObj(old,resultEntity, ModuleTypeEnum.SUBCONTRACT_CHANGE.getCode(),mainId,"",String.format("【%s】",old.getSkuNo()));
            }
        }
        //新增SKU添加操作日志,仅修改提交
        List<SubcontractChangeDetailEntity> addList = newList.stream().filter(c ->c.getIsAdd()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(addList) && !isAdd) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            moduleOperateLogService.batchAddModuleOperateLog("新增了一条父级SKU【%s】", ModuleTypeEnum.SUBCONTRACT_CHANGE.getCode(), addPairList, "编辑操作");
        }
        return resultList;
    }

    /**
     * @description: 处理供应商报价
     * @author Will
     * @date: 2023/6/14 17:07
     * @param entity
     * @param isChild
     */
    private void handleSupplierTaxPrice(SubcontractChangeDetailEntity entity,SubcontractOrderDetailEntity subEntity  , Boolean isChild) {

        //供应商为空
        if (StringUtils.isBlank(entity.getSupplierId())) {
            return;
        }

        //赠品无需报价,默认人民币
        if (ObjectUtils.isNotEmpty(subEntity) && ObjectUtils.isNotEmpty(subEntity.getIsGift()) && subEntity.getIsGift()) {
            entity.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
            entity.setCurrencySymbol(CurrencyEnum.CNY.getCurrencySymbol());
            entity.setPrice(BigDecimal.ZERO);
            entity.setAmount(MathUtil.multiply(entity.getPrice(),entity.getQty()));
            return;
        }
        //供应商报价信息
        PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO searchDTO = new PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO();
        searchDTO.setSkuId(entity.getSkuId());
        searchDTO.setSupplierId(entity.getSupplierId());
        searchDTO.setPurchaseQty(entity.getQty());
        searchDTO.setSkuNo(entity.getSkuNo());
        List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> taxPriceList = purchasePriceDetailService.getTaxPrice(searchDTO);
        PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO viewDTO = taxPriceList.get(0);
        entity.setCurrency(viewDTO.getCurrency());
        entity.setCurrencySymbol(viewDTO.getCurrencySymbol());
        //子件SKU默认取供应商报价
        if (isChild) {
            entity.setPrice(viewDTO.getTaxPrice());
        }
        entity.setAmount(MathUtil.multiply(entity.getPrice(),entity.getQty()));
    }

}
