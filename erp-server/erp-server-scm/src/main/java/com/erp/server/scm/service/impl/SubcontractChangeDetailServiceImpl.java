package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.dto.SubcontractChangeDetailDTO;
import com.erp.model.scm.entity.SubcontractChangeDetailEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.mapper.SubcontractChangeDetailMapper;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.PurchasePriceDetailService;
import com.erp.server.scm.service.SubcontractChangeDetailService;
import com.erp.server.scm.service.SupplierService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
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
        List<SubcontractChangeDetailEntity> resultList = generateResultDetail(list, mainId);
        this.saveBatch(resultList);
    }

    @Override
    public void update(List<SubcontractChangeDetailDTO.UpdateDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<SubcontractChangeDetailEntity> list = BeanMapperUtils.copyList(SubcontractChangeDetailEntity.class, detailList);

        //原明细数据
        List<SubcontractChangeDetailEntity> oldList = this.listParentByMainId(mainId);
        List<String> deleteIds = getDeleteIds(detailList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<SubcontractChangeDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            moduleOperateLogService.batchAddModuleOperateLog("删除了一个父级SKU【%s】", ModuleTypeEnum.PURCHASE_ORDER.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        checkSourceDetailQty(list,mainId);

        //处理父子级数据
        List<SubcontractChangeDetailEntity> resultList = generateResultDetail(list, mainId);

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
     * 验证数量
     */
    private void checkSourceDetailQty (List<SubcontractChangeDetailEntity> list,String mainId){
        /**
         * 1、变更数量不能小于采购订单数量
         *
         */

    }

    /**
     * 生成明细结果
     */
    private List<SubcontractChangeDetailEntity> generateResultDetail (List<SubcontractChangeDetailEntity> newList, String mainId) {
        List<SubcontractChangeDetailEntity> resultList = new ArrayList<>();
        //父级skuIds
        List<String> parentSkuIds = new ArrayList<>();
        //全部skuIds
        List<String> allSkuIds = new ArrayList<>();
        //仓库Ids
        List<String> warehouseIds = new ArrayList<>();
        //供应商Ids
        List<String> supplierIds = new ArrayList<>();
        newList.forEach(obj -> {

            parentSkuIds.add(obj.getSkuId());

            allSkuIds.add(obj.getSkuId());
            List<String> skuIdList = obj.getChildList().stream().map(SubcontractChangeDetailDTO.UpdateDTO::getSkuId).collect(Collectors.toList());
            allSkuIds.addAll(skuIdList);

            warehouseIds.add(obj.getWarehouseId());
            List<String> warehouseIdList = obj.getChildList().stream().map(SubcontractChangeDetailDTO.UpdateDTO::getWarehouseId).collect(Collectors.toList());
            warehouseIds.addAll(warehouseIdList);

            supplierIds.add(obj.getSupplierId());
            List<String> supplierIdList = obj.getChildList().stream().map(SubcontractChangeDetailDTO.UpdateDTO::getSupplierId).collect(Collectors.toList());
            supplierIds.addAll(supplierIdList);

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
        List<SupplierEntity> supplierList = supplierService.listByIds(supplierIds);
        if (CollectionUtils.isEmpty(supplierList)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        //仓库信息
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(warehouseIds);

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
            detailEntity.setMainId(mainId);
            detailEntity.setVariantProperty(skuVO.getVariantProperty());
            detailEntity.setSkuNo(skuVO.getSkuNo());
            detailEntity.setBomVersion(bomChildrenSkuDTO.getBomVersion());
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
            handleSupplierTaxPrice(detailEntity,Boolean.FALSE);
            //子集SKU信息
            List<SubcontractChangeDetailEntity>   childList = BeanMapperUtils.copyList(SubcontractChangeDetailEntity.class, detailEntity.getChildList());
            for (SubcontractChangeDetailEntity childEntity : childList) {
                //产品信息
                SkuVO childSkuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(childEntity.getSkuId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(childSkuVO)) {
                    throw new ServiceException(ApiError.ERROR_95084);
                }
                childEntity.setMainId(mainId);
                childEntity.setParentId(detailEntity.getId());
                childEntity.setVariantProperty(childSkuVO.getVariantProperty());
                childEntity.setSkuNo(childSkuVO.getSkuNo());
                childEntity.setBomVersion(bomChildrenSkuDTO.getBomVersion());
                //仓库名称
                if (CollectionUtils.isNotEmpty(warehouseList)) {
                    String warehouseName = warehouseList.stream().filter(obj -> obj.getId().equals(childEntity.getWarehouseId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                    detailEntity.setWarehouseName(warehouseName);
                }
                //供应商名称
                if (CollectionUtils.isNotEmpty(supplierList)) {
                    String supplierName = supplierList.stream().filter(obj -> obj.getId().equals(childEntity.getSupplierId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                    detailEntity.setSupplierName(supplierName);
                }
                handleSupplierTaxPrice(childEntity,Boolean.TRUE);
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
        //新增SKU添加操作日志
        List<SubcontractChangeDetailEntity> addList = newList.stream().filter(c ->c.getIsAdd()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(addList)) {
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
    private void handleSupplierTaxPrice(SubcontractChangeDetailEntity entity, Boolean isChild) {
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
