package com.erp.server.scm.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
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
import com.erp.model.scm.dto.PurchaseApplicationRefPoDTO;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.dto.SubcontractOrderDetailDTO;
import com.erp.model.scm.entity.PurchaseApplicationDetailEntity;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.scm.entity.SubcontractOrderEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.mapper.SubcontractOrderDetailMapper;
import com.erp.server.scm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 委外订单明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-06-08
 */
@Slf4j
@Service
public class SubcontractOrderDetailServiceImpl extends SuperServiceImpl<SubcontractOrderDetailMapper, SubcontractOrderDetailEntity> implements SubcontractOrderDetailService {

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;
    @Resource
    private PurchasePriceService purchasePriceService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private PurchaseApplicationDetailService purchaseApplicationDetailService;

    @Resource
    private SubcontractOrderService subcontractOrderService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    @Resource
    private PurchaseApplicationRefPoService purchaseApplicationRefPoService;

    @Override
    public void updateArrivalStatusByIds(String arrivalStatus, List<String> ids,Boolean isFinishDelivery) {
        lambdaUpdate()
                .in(SubcontractOrderDetailEntity::getId,ids)
                .set(SubcontractOrderDetailEntity::getArrivalStatus,arrivalStatus)
                .set(SubcontractOrderDetailEntity::getArrivalTime, LocalDateTime.now())
                .set(isFinishDelivery,SubcontractOrderDetailEntity::getIsEndReceive,Boolean.TRUE)
                .update();
    }

    @Override
    public void removeByMainIds(List<String> mainIds) {
        lambdaUpdate()
                .in(SubcontractOrderDetailEntity::getMainId,mainIds)
                .remove();
    }

    @Override
    public void add(List<SubcontractOrderDetailDTO.AddDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<SubcontractOrderDetailEntity> list = new ArrayList<>();
        for (SubcontractOrderDetailDTO.AddDTO addDTO : detailList) {
            SubcontractOrderDetailEntity entity = BeanMapperUtils.map(SubcontractOrderDetailEntity.class, addDTO);
            List<SubcontractOrderDetailDTO.AddDTO> addList = addDTO.getChildList();
            List<SubcontractOrderDetailDTO.UpdateDTO> updateList = BeanMapperUtils.copyList(SubcontractOrderDetailDTO.UpdateDTO.class, addList);
            entity.setChildList(updateList);
            list.add(entity);
        }
        //处理父子级数据
        List<SubcontractOrderDetailEntity> resultList = generateResultDetail(list, mainId,Boolean.TRUE);

        this.saveBatch(resultList);
        //标记SKU
        List<String> skuIds = resultList.stream().map(SubcontractOrderDetailEntity::getSkuId).collect(Collectors.toList());
        plmTaskFeign.updateOccupyStatus(skuIds);
    }


    @Override
    public void addByChange(List<SubcontractOrderDetailDTO.UpdateDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<SubcontractOrderDetailEntity> list = BeanMapperUtils.copyList(SubcontractOrderDetailEntity.class, detailList);
        //处理父子级数据
        List<SubcontractOrderDetailEntity> resultList = generateResultDetail(list, mainId,Boolean.FALSE);
        this.saveBatch(resultList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSourceDetailId(List<Pair<String, String>> pairList) {
        if(CollectionUtils.isEmpty(pairList)) {
            return;
        }
        for (Pair<String, String> pair : pairList) {
            lambdaUpdate()
                    .eq(SubcontractOrderDetailEntity::getId,pair.getKey()).
                    set(SubcontractOrderDetailEntity::getSourceDetailId,pair.getValue())
                    .update();
        }
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
                    .set(SubcontractOrderDetailEntity::getKingdeeDetailId, kingdeeDetailId)
                    .eq(SubcontractOrderDetailEntity::getId, detailId)
                    .update();
        }
    }

    @Override
    public List<SubcontractOrderDetailEntity> listChildSubcontractDetailByIds(List<String> parentIdList) {
        if (CollectionUtils.isEmpty(parentIdList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(SubcontractOrderDetailEntity::getParentId,parentIdList).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<SubcontractOrderDetailDTO.UpdateDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<SubcontractOrderDetailEntity> list = BeanMapperUtils.copyList(SubcontractOrderDetailEntity.class, detailList);

        //原明细数据
        List<SubcontractOrderDetailEntity> oldList = this.listByMainId(mainId);
        //将子级SKU添加入集合判断是否删除
        List<SubcontractOrderDetailDTO.UpdateDTO> allDetailList = new ArrayList<>();
        allDetailList.addAll(detailList);
        detailList.forEach(obj -> {
            allDetailList.addAll(obj.getChildList());
        });
        List<String> deleteIds = getDeleteIds(allDetailList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<SubcontractOrderDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            moduleOperateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.PURCHASE_ORDER.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        checkSourceDetailQty(list,mainId);

        //处理父子级数据
        List<SubcontractOrderDetailEntity> resultList = generateResultDetail(list, mainId,Boolean.FALSE);

        this.saveOrUpdateBatch(resultList);
        //标记SKU
        List<String> skuIds = resultList.stream().map(SubcontractOrderDetailEntity::getSkuId).collect(Collectors.toList());
        plmTaskFeign.updateOccupyStatus(skuIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateByChange(List<SubcontractOrderDetailDTO.UpdateDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<SubcontractOrderDetailEntity> list = BeanMapperUtils.copyList(SubcontractOrderDetailEntity.class, detailList);

        checkSourceDetailQty(list,mainId);
        //处理父子级数据
        List<SubcontractOrderDetailEntity> resultList = generateResultDetail(list, mainId,Boolean.FALSE);
        //变更不更新bom版本
        resultList.forEach(obj -> {
            obj.setBomVersion(null);
            obj.setBomHistoryId(null);
        });
        this.saveOrUpdateBatch(resultList);
    }

    @Override
    public List<SubcontractOrderDetailEntity> listByMainId(String mainId) {
       return lambdaQuery().eq(SubcontractOrderDetailEntity::getMainId,mainId).list();
    }

    @Override
    public List<SubcontractOrderDetailEntity> listByMainIds(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(SubcontractOrderDetailEntity::getMainId,mainIdList).list();
    }

    @Override
    public List<SubcontractOrderDetailEntity> listByParentIds(List<String> detailIds) {
        return lambdaQuery()
                .in(SubcontractOrderDetailEntity::getParentId,detailIds)
                .list();
    }

    @Override
    public List<SubcontractOrderDetailEntity> listByMainIdAndSku(String mainId, List<String> skuNoList) {
        return lambdaQuery()
                .eq(SubcontractOrderDetailEntity::getMainId,mainId)
                .in(CollectionUtils.isNotEmpty(skuNoList),SubcontractOrderDetailEntity::getSkuNo,skuNoList)
                .list();
    }

    @Override
    public List<SubcontractOrderDetailEntity> listBySourceDetailIds(List<String> sourceDetailIds) {
        return  baseMapper.listBySourceDetailIds(sourceDetailIds);
    }
    @Override
    public List<SubcontractOrderDetailEntity> listBySourceDetailIdsWithNoPurchase(List<String> sourceDetailIds) {
        return  baseMapper.listBySourceDetailIdsWithNoPurchase(sourceDetailIds);
    }
    /**
     * 根据主表id查询父级SKU数据
     */
    private List<SubcontractOrderDetailEntity> listParentByMainId(String mainId) {
        return lambdaQuery()
                .eq(SubcontractOrderDetailEntity::getMainId,mainId)
                .eq(SubcontractOrderDetailEntity::getParentId,"")
                .list();
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<SubcontractOrderDetailDTO.UpdateDTO> newList, List<SubcontractOrderDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SubcontractOrderDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(SubcontractOrderDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * @description: 修改验证下推数量
     * @author Will
     * @date: 2023/6/15 10:01
     * @param list
     * @param mainId
     */
    private void checkSourceDetailQty(List<SubcontractOrderDetailEntity> list,String mainId) {
        //由采购申请下推的数据
        List<SubcontractOrderDetailEntity> sourceDetailList = list.stream().filter(obj -> StringUtils.isNotBlank(obj.getSourceDetailId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(sourceDetailList)) {
            return;
        }
        //主表信息
        SubcontractOrderEntity entity = subcontractOrderService.getById(mainId);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98073);
        }

        //采购申请单明细
        List<String> sourceDetailIds = sourceDetailList.stream().map(SubcontractOrderDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<PurchaseApplicationDetailEntity> purchaseApplicationDetailList = purchaseApplicationDetailService.listByIds(sourceDetailIds);

        //已下推明细信息
        List<SubcontractOrderDetailEntity> foundList = this.listBySourceDetailIds(sourceDetailIds);

        //产品信息
        List<String> skuIds = sourceDetailList.stream().map(SubcontractOrderDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }

        for (SubcontractOrderDetailEntity detailEntity : sourceDetailList) {
            //sku编码
            String skuNo = skuList.stream().filter(obj -> obj.getSkuId().equals(detailEntity.getSkuId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getSkuNo())).orElse("");

            //申请数量
            Integer applyQty = purchaseApplicationDetailList.stream().filter(obj -> obj.getId().equals(detailEntity.getSourceDetailId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getApplyQty())).orElse(MathUtil.ZERO);
            //本次更新数量
            Integer qty = detailEntity.getQty();
            //已下推数量（不包括本明细数量）
            Integer pushdownQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(foundList)) {
                pushdownQty = foundList.stream().filter(obj ->obj.getSourceDetailId().equals(detailEntity.getSourceDetailId()) && !StringUtils.equals(obj.getId(),detailEntity.getId()) && StringUtils.isBlank(obj.getParentId()))
                        .map(SubcontractOrderDetailEntity::getQty).reduce(MathUtil.ZERO,Integer::sum);
            }
            //下推单据数量验证
            if (qty > applyQty - pushdownQty) {
                throw new ServiceException(new ApiResult(ApiError.ERROR_98074.code, StrUtil.format(ApiError.ERROR_98074.msg,entity.getCode(),skuNo,applyQty - pushdownQty)));
            }
        }
    }


    /**
     * @description: 生成明细结果
     * @author Will
     * @date: 2023/6/15 10:06
     * @param newList
     * @param mainId
     * @return List<SubcontractOrderDetailEntity>
     */
    private List<SubcontractOrderDetailEntity> generateResultDetail (List<SubcontractOrderDetailEntity> newList, String mainId,Boolean isAdd) {
        List<SubcontractOrderDetailEntity> resultList = new ArrayList<>();
        //父级skuIds
        List<String> parentSkuIds = new ArrayList<>();
        //全部skuIds
        List<String> allSkuIds = new ArrayList<>();
        //仓库Ids
        List<String> warehouseIds = new ArrayList<>();
        //供应商Ids
        List<String> supplierIds = new ArrayList<>();
        //id集合赋值
        handleIdList (newList,parentSkuIds,allSkuIds,warehouseIds,supplierIds);

        //申请单下推数量校验
        List<String> sourceDetailIds = newList.stream().filter(obj->StringUtils.isNotBlank(obj.getSourceDetailId())).map(SubcontractOrderDetailEntity::getSourceDetailId).collect(Collectors.toList());
        //采购申请单明细数据
        List<PurchaseApplicationDetailEntity> sourceDetailList = new ArrayList<>();
        List<SubcontractOrderDetailEntity> refDetailList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(sourceDetailIds)) {
            //采购申请信息
            sourceDetailList = purchaseApplicationDetailService.listByIds(sourceDetailIds);
            //委外明细信息
            refDetailList = this.listBySourceDetailIdsWithNoPurchase(sourceDetailIds);
        }
        //查询下推采购单
        PurchaseApplicationRefPoDTO.SearchParamDTO searchParamDTO = new PurchaseApplicationRefPoDTO.SearchParamDTO();
        searchParamDTO.setPurchaseApplicationDetailIds(sourceDetailIds);
        List<PurchaseApplicationRefPoDTO.ListDTO> purchaseRefList = purchaseApplicationRefPoService.list(searchParamDTO);

        //委外订单
        SubcontractOrderEntity subcontractOrderEntity = subcontractOrderService.getById(mainId);
        if (ObjectUtils.isEmpty(subcontractOrderEntity)) {
            throw new ServiceException(ApiError.ERROR_98073);
        }
        //获取采购单价
        String purchaseOrgId = subcontractOrderEntity.getPurchaseOrgId();
        List<PurchasePriceDTO.PriceDTO> list = new ArrayList<>();
        newList.forEach(e ->{
            if (CollectionUtils.isNotEmpty(e.getChildList())){
                e.getChildList().forEach(f -> {
                    if (Objects.nonNull(f.getIsGift()) && !f.getIsGift()){
                        list.add(PurchasePriceDTO.PriceDTO.builder()
                                .purchaseOrgId(purchaseOrgId)
                                .qty(f.getQty())
                                .skuId(f.getSkuId())
                                .supplierId(f.getSupplierId())
                                .build());
                    }
                });
            }
        });
        List<PurchasePriceDTO.PriceDTO> priceList = purchasePriceService.batchGetPurchasePrice(list);
        //BOM信息
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listHistoryBomChildBySkuIds(parentSkuIds);
        if (CollectionUtils.isEmpty(bomChildrenList)) {
            throw new ServiceException(ApiError.ERROR_95163);
        }
        //产品信息
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(allSkuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        List<SupplierEntity> supplierList = null;
        if (CollectionUtils.isNotEmpty(supplierIds)) {
            supplierList = supplierService.listByIds(supplierIds);
        }

        //仓库信息
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(warehouseIds);

        for (SubcontractOrderDetailEntity detailEntity : newList) {

            //父级SKU信息
            SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(skuVO)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }

            //申请数量校验
            if (CollectionUtils.isNotEmpty(sourceDetailList)) {
                //申请单数量
                Integer applyQty = sourceDetailList.stream()
                        .filter(obj -> obj.getId().equals(detailEntity.getSourceDetailId()))
                        .findFirst().flatMap(obj -> Optional.ofNullable(obj.getApplyQty())).orElse(MathUtil.ZERO);

                Integer purchaseQty = MathUtil.ZERO;
                Integer subcontractQty = MathUtil.ZERO;
                if (CollectionUtils.isNotEmpty(refDetailList)) {
                    subcontractQty = refDetailList.stream()
                            .filter(obj -> obj.getSourceDetailId().equals(detailEntity.getSourceDetailId()) && !obj.getId().equals(detailEntity.getId()) && StringUtils.isBlank(obj.getParentId()))
                            .map(SubcontractOrderDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
                }
                //查询已采购数量
                if (CollectionUtils.isNotEmpty(purchaseRefList)) {
                    purchaseQty = purchaseRefList.stream().filter(obj -> detailEntity.getSourceDetailId().equals(obj.getPurchaseApplicationDetailId())).map(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseQty).reduce(0, Integer::sum);
                }

                //已下推数量
                Integer pushdownQty = purchaseQty+subcontractQty;

                if (detailEntity.getQty() > applyQty - pushdownQty) {
                    throw new ServiceException(new ApiResult(ApiError.ERROR_98091.code,StrUtil.format(ApiError.ERROR_98091.msg,skuVO.getSkuNo(),applyQty - pushdownQty)));
                }
            }
            //bom信息
            BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenList.stream().filter(obj -> obj.getParentSkuId().equals(detailEntity.getSkuId())).max(Comparator.comparingDouble(obj -> Double.valueOf(obj.getBomVersion()))).orElse(null);
            if (ObjectUtils.isEmpty(bomChildrenSkuDTO)) {
                throw new ServiceException(ApiError.ERROR_95163);
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
            detailEntity.setBomHistoryId(bomChildrenSkuDTO.getBomHistoryId());
            //仓库名称
            if (CollectionUtils.isNotEmpty(warehouseList)) {
                WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(obj -> obj.getId().equals(detailEntity.getWarehouseId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(updateDTO)) {
                    throw new ServiceException(ApiError.ERROR_99002);
                }
                //仓库组织匹配校验
                if (!StrUtil.equals(updateDTO.getOrgId(),subcontractOrderEntity.getSubcontractOrgId())) {
                    throw new ServiceException(ApiError.ERROR_SUBCONTRACT_ORDER_WAREHOUSE_ORG,updateDTO.getName(),subcontractOrderEntity.getSubcontractOrgName());
                }
                detailEntity.setWarehouseName(updateDTO.getName());
            }
            //供应商名称
            if (CollectionUtils.isNotEmpty(supplierList)) {
                String supplierName = supplierList.stream().filter(obj -> obj.getId().equals(detailEntity.getSupplierId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                detailEntity.setSupplierName(supplierName);
            }
            handleSupplierTaxPrice(detailEntity,Boolean.FALSE,subcontractOrderEntity.getSubcontractOrgId(), priceList);
            //子集SKU信息
            List<SubcontractOrderDetailEntity>   childList = BeanMapperUtils.copyList(SubcontractOrderDetailEntity.class, detailEntity.getChildList());
            for (SubcontractOrderDetailEntity childEntity : childList) {
                //产品信息
                SkuVO childSkuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(childEntity.getSkuId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(childSkuVO)) {
                    throw new ServiceException(ApiError.ERROR_95084);
                }
                if (StringUtils.isBlank(childEntity.getWarehouseLocation())) {
                    throw new ServiceException(ApiError.ERROR_SUB_CHILD_LOCATION_BLANK,childEntity.getSkuNo());
                }

                childEntity.setMainId(mainId);
                childEntity.setParentId(detailEntity.getId());
                childEntity.setVariantProperty(childSkuVO.getVariantProperty());
                childEntity.setSkuNo(childSkuVO.getSkuNo());
                childEntity.setBomVersion(bomChildrenSkuDTO.getBomVersion());
                //仓库名称
                WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(obj -> obj.getId().equals(childEntity.getWarehouseId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(updateDTO)) {
                    throw new ServiceException(ApiError.ERROR_99002);
                }
                childEntity.setWarehouseName(updateDTO.getName());
                //供应商名称
                if (CollectionUtils.isNotEmpty(supplierList)) {
                    String supplierName = supplierList.stream().filter(obj -> obj.getId().equals(childEntity.getSupplierId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                    childEntity.setSupplierName(supplierName);
                }
                handleSupplierTaxPrice(childEntity,Boolean.TRUE,subcontractOrderEntity.getSubcontractOrgId(), priceList);
            }
            resultList.add(detailEntity);
            resultList.addAll(childList);

        }
        //添加修改操作日志
        for (SubcontractOrderDetailEntity resultEntity : resultList) {
            SubcontractOrderDetailEntity old = this.getById(resultEntity.getId());
            if (ObjectUtils.isNotEmpty(old)) {
                moduleOperateLogService.addModuleOperateLogByObj(old,resultEntity, ModuleTypeEnum.SUBCONTRACT_ORDER.getCode(),mainId,"",String.format("【%s】",old.getSkuNo()));
            }
        }
        //新增SKU添加操作日志
        List<SubcontractOrderDetailEntity> addList = newList.stream().filter(c ->c.getIsAdd()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(addList) && !isAdd) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            moduleOperateLogService.batchAddModuleOperateLog("新增了一条父级SKU【%s】", ModuleTypeEnum.SUBCONTRACT_ORDER.getCode(), addPairList, "编辑操作");
        }
        return resultList;
    }


    /**
     * @description: id集合赋值
     * @author Will
     * @date: 2024/2/1 12:01
     * @param newList
     * @param parentSkuIds
     * @param allSkuIds
     * @param warehouseIds
     * @param supplierIds
     */
    private void handleIdList (List<SubcontractOrderDetailEntity> newList,List<String> parentSkuIds,
                               List<String> allSkuIds,List<String> warehouseIds,List<String> supplierIds) {
        for (SubcontractOrderDetailEntity obj : newList) {
            parentSkuIds.add(obj.getSkuId());

            allSkuIds.add(obj.getSkuId());
            List<String> skuIdList = obj.getChildList().stream().map(SubcontractOrderDetailDTO.UpdateDTO::getSkuId).collect(Collectors.toList());
            allSkuIds.addAll(skuIdList);

            warehouseIds.add(obj.getWarehouseId());
            List<String> warehouseIdList = obj.getChildList().stream().map(SubcontractOrderDetailDTO.UpdateDTO::getWarehouseId).collect(Collectors.toList());
            warehouseIds.addAll(warehouseIdList);

            //供应商信息
            if (StringUtils.isNotEmpty(obj.getSupplierId())) {
                supplierIds.add(obj.getSupplierId());
            }
            List<String> supplierIdList = obj.getChildList().stream().filter(e -> StringUtils.isNotEmpty(e.getSupplierId())).map(SubcontractOrderDetailDTO.UpdateDTO::getSupplierId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(supplierIdList)) {
                supplierIds.addAll(supplierIdList);
            }
        }
    }

    /**
     * @param entity
     * @param isChild
     * @param priceList
     * @description: 处理供应商报价
     * @author Will
     * @date: 2023/6/14 17:07
     */
    private void handleSupplierTaxPrice(SubcontractOrderDetailEntity entity, Boolean isChild, String purchaseOrgId, List<PurchasePriceDTO.PriceDTO> priceList) {
        //供应商为空
        if (StringUtils.isBlank(entity.getSupplierId())) {
            return;
        }

        //赠品无需报价,默认人民币
        if (ObjectUtils.isNotEmpty(entity.getIsGift()) && entity.getIsGift()) {
            entity.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
            entity.setCurrencySymbol(CurrencyEnum.CNY.getCurrencySymbol());
            entity.setPrice(BigDecimal.ZERO);
            entity.setAmount(MathUtil.multiply(entity.getPrice(),entity.getQty()));
            return;
        }
        //子件SKU默认取供应商报价
        if (isChild) {
            PurchasePriceDTO.PriceDTO viewDTO = priceList.stream().filter(obj ->
                            obj.getSkuId().equals(entity.getSkuId())
                            && obj.getSupplierId().equals(entity.getSupplierId())
                            && StrUtil.equals(obj.getPurchaseOrgId(),purchaseOrgId))
                    .findFirst().orElse(null);
            if (Objects.isNull(viewDTO)){
                return;
            }
            entity.setCurrency(viewDTO.getCurrency());
            entity.setCurrencySymbol(viewDTO.getCurrencySymbol());
            entity.setPrice(viewDTO.getTaxPrice());
            entity.setTaxRate(viewDTO.getTaxRate());
            entity.setAmount(MathUtil.multiply(entity.getPrice(),entity.getQty()));
        }else {
            //供应商报价信息
            PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO searchDTO = new PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO(entity.getQty(),entity.getSkuId(),entity.getSkuNo(),entity.getSupplierId(),purchaseOrgId);
            List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> taxPriceList = purchasePriceDetailService.getTaxPrice(searchDTO);
            PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO viewDTO = taxPriceList.get(0);
            entity.setCurrency(viewDTO.getCurrency());
            entity.setCurrencySymbol(viewDTO.getCurrencySymbol());
            entity.setTaxRate(viewDTO.getTaxRate());
            entity.setAmount(MathUtil.multiply(entity.getPrice(),entity.getQty()));
        }

    }

}
