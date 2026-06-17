package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.RetailPriceUtil;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.enums.PriceAllocationSourceEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.PlmCfgSettingEntity;
import com.erp.model.plm.entity.SkuStdRetailPriceEntity;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.enums.SkuStdSettingEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.SoOutstockDetailMapper;
import com.erp.server.wms.service.*;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import io.seata.tm.api.transaction.Propagation;
import lombok.extern.slf4j.Slf4j;

import org.apache.bcel.generic.LADD;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <p>
 * 销售订单出库明细 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Slf4j
@Service
public class SoOutstockDetailServiceImpl extends SuperServiceImpl<SoOutstockDetailMapper, SoOutstockDetailEntity> implements SoOutstockDetailService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private SoB2cFeign soB2cFeign;
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private WmsAttachmentService wmsAttachmentService;

    @Resource
    private InventoryService inventoryService;

    @Resource
    private OperateLogService operateLogService;


    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;

    @Resource
    private SoB2cDeliveryDetailService soB2cDeliveryDetailService;

    @Resource
    private WarehouseService warehouseService;
    @Lazy
    @Resource
    private WmsCartonSpecService wmsCartonSpecService;
    @Resource
    private SoOutstockService soOutstockService;


    @Override
    public List<SoOutstockDetailEntity> listDetailBySourceDetailId(List<String> sourceDetailIds) {
        if (CollectionUtils.isEmpty(sourceDetailIds)) {
            return Collections.emptyList();
        }
        return baseMapper.listSoOutstockBySourceDetailId(sourceDetailIds);

    }


    /**
     * 保存销售出库单明细
     *
     * @param mainId
     * @param detailList
     * @param entity
     * @return void
     * @author yl
     * @date 2023-05-19 10:18
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SoOutstockDetailEntity> add(String mainId, List<SoOutstockDetailDTO.AddDTO> detailList, String orderType, SoOutstockEntity entity) {
        if (CollectionUtils.isEmpty(detailList)) {
            return new ArrayList<>();
        }
        List<String> skuIdList = detailList.stream().map(SoOutstockDetailDTO.AddDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);

        Class<SoOutstockDetailEntity> credentialClass = SoOutstockDetailEntity.class;
        TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
        List<SoOutstockDetailEntity> addList = new ArrayList<>(detailList.size());
        //获取到表名
        String type = tableName.value();
        List<WmsAttachmentEntity> batchAttachmentList = new ArrayList<>(10);

        for (SoOutstockDetailDTO.AddDTO item : detailList) {
            SoOutstockDetailEntity addEntity = new SoOutstockDetailEntity();
            BeanMapper.copy(item, addEntity);
            String id = IdWorker.getIdStr();
            String skuId = item.getSkuId();
            String skuNo = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSkuNo())).orElse("");
            addEntity.setSkuNo(skuNo);
            addEntity.setMainId(mainId);
            addEntity.setId(id);
            addList.add(addEntity);
            //附件集合
            List<String> attachmentUrlList = item.getAttachUrlList();
            //附件名
            List<String> attachmentNameList = item.getAttachNameList();
            if (CollectionUtils.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()) {
                for (int i = 0; i < attachmentUrlList.size(); i++) {
                    WmsAttachmentEntity attachment = new WmsAttachmentEntity();
                    attachment.setAttachUrl(attachmentUrlList.get(i));
                    attachment.setAttachName(attachmentNameList.get(i));
                    attachment.setBusinessId(id);
                    attachment.setType(type);
                    batchAttachmentList.add(attachment);
                }
            }
        }
        String b2C = OrderTypeEnum.B2C.getCode();
        if (!b2C.equals(orderType)) {
            //处理明细数据
            handleDetailData(addList);
        } else {
            //处理明细数据
            handleB2cDetailData(addList,entity);
        }
        //赋值仓库名称
        if(Objects.nonNull(entity)){
            addList.forEach(v->v.setWarehouseName(entity.getWarehouseName()));
        }

        super.saveBatch(addList);
        wmsAttachmentService.saveBatch(batchAttachmentList);
        return addList;
    }


    /**
     * 根据 main id  获取对应数据
     *
     * @param mainId
     * @return java.util.List<com.erp.model.wms.dto.SoOutstockDetiailDTO.ViewDTO>
     * @author yl
     * @date 2023-05-19 11:32
     */
    @Override
    public List<SoOutstockDetailDTO.ViewDTO> listByMainId(String mainId, String warehouseId) {
        List<SoOutstockDetailEntity> dbList = this.listBaseByMainId(mainId);
        List<SoOutstockDetailDTO.ViewDTO> resultList = BeanMapper.copyList(dbList, SoOutstockDetailDTO.ViewDTO.class);
        List<String> skuIdList = resultList.stream().map(SoOutstockDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<String> warehouseLocationList = resultList.stream().map(SoOutstockDetailDTO.ViewDTO::getWarehouseLocation).collect(Collectors.toList());
        InventoryQtyDTO.SkuInventoryParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryParamDTO();
        skuInventoryDTO.setSkuIdList(skuIdList);
        skuInventoryDTO.setWarehouseIdList(Collections.singletonList(warehouseId));
        skuInventoryDTO.setWarehouseLocationIdList(warehouseLocationList);
        skuInventoryDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        List<String> idList = dbList.stream().map(SoOutstockDetailEntity::getId).collect(Collectors.toList());
        //附件信息
        List<WmsAttachmentDTO.UpdateDTO> attachmentDbList = wmsAttachmentService.getByBusinessIds(idList);
        //可用库存
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryList = inventoryService.listSkuInventory(skuInventoryDTO);
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
        for (SoOutstockDetailDTO.ViewDTO item : resultList) {
            String skuId = item.getSkuId();
            String id = item.getId();
            List<WmsAttachmentDTO.UpdateDTO> attachmentList = attachmentDbList.stream().filter(a -> a.getBusinessId().equals(id)).collect(Collectors.toList());
            item.setAttachNameList(attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList()));
            item.setAttachUrlList(attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList()));
            String warehouseLocation = item.getWarehouseLocation();
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(new SkuVO());
            item.setProductName(skuVO.getSkuName());
            item.setVariantProperty(skuVO.getVariantProperty());

            String unit = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getUnitName())).orElse("");
            item.setUnit(unit);
            Integer curInventoryQty = skuInventoryList.stream().filter(i -> i.getSkuId().equals(skuId) && i.getWarehouseLocationId().equals(warehouseLocation)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);
            item.setCurInventoryQty(curInventoryQty);
        }
        return resultList;
    }


    /**
     * 删除明细
     *
     * @param mainIdList
     * @return void
     * @author yl
     * @date 2023-05-19 12:28
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByMainIdList(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return;
        }
        LambdaUpdateWrapper<SoOutstockDetailEntity> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.in(SoOutstockDetailEntity::getMainId, mainIdList);
        queryWrapper.set(SoOutstockDetailEntity::getIsDeleted, true);
        queryWrapper.set(SoOutstockDetailEntity::getUpdateTime, LocalDateTime.now());
        this.update(queryWrapper);
    }


    /**
     * 检查数量
     *
     * @param soId       销售订单id
     * @param sourceId
     * @param sourceType
     * @param detailList
     * @return void
     * @author yl
     * @date 2023-05-22 15:54
     */
    @Override
    public void checkOutQty(String warehouseId, String soId, String sourceId, String sourceType, List<SoOutstockDetailDTO.UpdateDTO> detailList, String batchNo) {
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.SO_DELIVERY_OUTBOUND_DETAIL_REQUIRED);
        }
        List<String> excludedIdList = detailList.stream().filter(d -> CharSequenceUtil.isNotBlank(d.getId())).
                map(SoOutstockDetailDTO.UpdateDTO::getId).collect(Collectors.toList());

        //发货通知单
        String soDeliveryNotice = SourceTypeEnum.SO_DELIVERY_NOTICE.getCode();

        List<String> sourceDetailIdList = detailList.stream().map(SoOutstockDetailDTO.AddDTO::getSourceDetailId).collect(Collectors.toList());
        List<String> skuIdList = detailList.stream().map(SoOutstockDetailDTO.AddDTO::getSkuId).collect(Collectors.toList());
        List<String> warehouseLocationList = detailList.stream().map(SoOutstockDetailDTO.AddDTO::getWarehouseLocation).collect(Collectors.toList());
        //这个是已出数量
        List<SoOutstockDetailDTO.DeliveryQtyDTO> soOutstockDetailList = this.listDetailByDetailIds(sourceDetailIdList, excludedIdList);

        // 忽略库存计算SKU
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = Lists.newArrayList();
        if (CollUtil.isNotEmpty(ignoreInventorySkuList)) {
            ignoreInventorySkuIds = ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
        }

        //发货通知到
        if (SourceTypeEnum.SO_DELIVERY_NOTICE.getCode().equals(sourceType) || SourceTypeEnum.B2B_THIRD_DELIVERY.getCode().equals(sourceType)) {
            List<SoOutstockDetailEntity> detailEntities = listDetailBySoIds(Collections.singletonList(soId));
            //添加校验
            List<SoDetailEntity> soDetails = soInfoFeign.listSoDetailByMainIds(Collections.singletonList(soId));
            for (SoOutstockDetailDTO.UpdateDTO dto : detailList) {
                int sellQty = soDetails.stream()
                        .filter(v -> v.getDeliverySkuNo().equals(dto.getSkuNo()))
                        .mapToInt(SoDetailEntity::getBoxQty).sum();
                if (sellQty == 0) {
                    throw new ServiceException(ApiError.SO_PICKLIST_DETAIL_NOT_FOUND_FOR_SO, dto.getSkuNo());
                }
                int actualQty = detailEntities.stream()
                        .filter(e -> Boolean.FALSE.equals(e.getInvalidStatus()))
                        .filter(v -> v.getSkuNo().equals(dto.getSkuNo()))
                        .mapToInt(SoOutstockDetailEntity::getActualQty)
                        .sum();
                if (sellQty < actualQty + Optional.ofNullable(dto.getPlanQty()).orElse(0)) {
                    throw new ServiceException(ApiError.SO_OUTBOUND_QTY_EXCEEDS_ORDER, dto.getSkuNo());
                }
            }
        } else {
            InventoryQtyDTO.SkuInventoryParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryParamDTO();
            skuInventoryDTO.setSkuIdList(skuIdList);
            skuInventoryDTO.setWarehouseIdList(Collections.singletonList(warehouseId));
            skuInventoryDTO.setWarehouseLocationIdList(warehouseLocationList);
            skuInventoryDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
            //可用数量
            List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryList = inventoryService.listSkuInventory(skuInventoryDTO);
            //这个是销售订单的
            List<SoDetailEntity> soDetailList = soInfoFeign.listSoDetailByIds(sourceDetailIdList);
            for (SoOutstockDetailDTO.UpdateDTO item : detailList) {
                String skuId = item.getSkuId();
                //库位
                String warehouseLocation = item.getWarehouseLocation();
                //实发数量
                Integer actualQty = item.getActualQty();
                //应发数量
                Integer planQty = item.getPlanQty();
                if (ignoreInventorySkuIds.contains(item.getSkuId())) {
                    log.warn("sku id: {}产品属性是费用或服务，不参与库存出入库，不做库存验证", item.getSkuId());
                } else {
                    if (actualQty > planQty) {
                        throw new ServiceException(ApiError.SO_DELIVERY_QTY_GT_REQUIRED_QTY);
                    }

                    String sourceDetailId = item.getSourceDetailId();
                    //这个是销售数量
                    Integer soQty = soDetailList.stream().filter(s -> s.getId().equals(sourceDetailId)).findFirst().
                            flatMap(obj -> Optional.ofNullable(obj.getQty())).orElse(0);

                    //这个是已出的数量 这个对应的就是销售订单的详情id
                    Integer outStockQty = soOutstockDetailList.stream().filter(s ->
                            s.getSoDetailId().equals(sourceDetailId)
                    ).mapToInt(SoOutstockDetailDTO.DeliveryQtyDTO::getActualQty).sum();

                    if (outStockQty + planQty > soQty) {
                        throw new ServiceException(ApiError.SO_DELIVERY_QTY_GT_AVAILABLE_QTY);
                    }
                    if (ObjectUtil.isEmpty(batchNo)) {
                        //即时库存
                        Integer inventory = skuInventoryList.stream().filter(s -> s.getSkuId().equals(skuId) && s.getWarehouseLocationId().
                                equals(warehouseLocation)).findFirst().flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);
                        if (planQty > inventory) {
//                            throw new ServiceException(ApiError.ERROR_DELIVERY_QTY_GT_STOCK);
                        }
                    }
                }
            }
        }
    }


    /**
     * 修改销售出库单详情
     *
     * @param mainId
     * @param detailList
     * @return void
     * @author yl
     * @date 2023-05-22 18:21
     */
    @Override
    public void updateDetail(String mainId, List<SoOutstockDetailDTO.UpdateDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.SO_DELIVERY_OUTBOUND_DETAIL_REQUIRED);
        }
        List<SoOutstockDetailDTO.UpdateDTO> updateList = detailList.stream().filter(c -> CharSequenceUtil.isNotBlank(c.getId())).collect(Collectors.toList());
        List<SoOutstockDetailEntity> dbList = this.listBaseByMainId(mainId);
        List<Pair<String, String>> pairList = updateList.stream().map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        List<String> deleteIdList = getDeleteIds(pairList, dbList);
        List<SoOutstockDetailEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }

        //这是删除
        List<Pair<String, String>> removePairList = removeList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("删除了一个产品【%s】", ModuleTypeEnum.SO_OUT_STOCK.getCode(), removePairList, "编辑操作");


        List<String> skuIdList = detailList.stream().map(SoOutstockDetailDTO.AddDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);

        Class<SoOutstockDetailEntity> credentialClass = SoOutstockDetailEntity.class;
        TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
        List<SoOutstockDetailEntity> addOrUpdateList = new ArrayList<>(detailList.size());

        List<SoOutstockDetailEntity> addEntityList = new ArrayList<>(detailList.size());

        List<SoOutstockDetailEntity> updateEntityList = new ArrayList<>(detailList.size());
        //获取到表名
        String type = tableName.value();
        List<WmsAttachmentEntity> batchAttachmentList = new ArrayList<>(10);

        for (SoOutstockDetailDTO.UpdateDTO item : detailList) {
            String id = item.getId();
            Boolean isAdd = CharSequenceUtil.isBlank(id);
            SoOutstockDetailEntity entity = new SoOutstockDetailEntity();
            BeanMapper.copy(item, entity);
            if (isAdd) {
                id = IdWorker.getIdStr();
            }
            String skuId = item.getSkuId();
            String skuNo = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSkuNo())).orElse("");
            entity.setSkuNo(skuNo);
            entity.setMainId(mainId);
            entity.setId(id);
            if (isAdd) {
                addEntityList.add(entity);
            } else {
                updateEntityList.add(entity);
            }

            //现阶段只有修改没有新增则必定会存在对应明细
            String soDetailId = dbList.stream().filter(obj -> obj.getId().equals(item.getId())).map(SoOutstockDetailEntity::getSoDetailId).findFirst().orElse("");
            entity.setSoDetailId(soDetailId);
            addOrUpdateList.add(entity);
            //附件集合
            List<String> attachmentUrlList = item.getAttachUrlList();
            //附件名
            List<String> attachmentNameList = item.getAttachNameList();
            if (CollectionUtils.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()) {
                for (int i = 0; i < attachmentUrlList.size(); i++) {
                    WmsAttachmentEntity attachment = new WmsAttachmentEntity();
                    attachment.setAttachUrl(attachmentUrlList.get(i));
                    attachment.setAttachName(attachmentNameList.get(i));
                    attachment.setBusinessId(id);
                    attachment.setType(type);
                    batchAttachmentList.add(attachment);
                }
            }
        }

        //处理明细数据
        handleDetailData(addOrUpdateList);
        this.saveOrUpdateBatch(addOrUpdateList);
        wmsAttachmentService.saveBatch(batchAttachmentList);

        //这是添加
        List<Pair<String, String>> addPairList = addEntityList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("添加了一个销售产品【%s】", ModuleTypeEnum.SO_OUT_STOCK.getCode(), addPairList, "编辑操作");
        for (SoOutstockDetailEntity update : updateEntityList) {
            String id = update.getId();
            SoOutstockDetailEntity old = dbList.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
            if (old != null) {
                operateLogService.addModuleOperateLogByObj(old, update, ModuleTypeEnum.SO_OUT_STOCK.getCode(), mainId, "", "");
            }
        }
    }


    /**
     * 获取到销售出库明细 根据主表id
     *
     * @param mainIds
     * @return java.util.List<com.erp.model.wms.entity.SoOutstockDetailEntity>
     * @author yl
     * @date 2023-05-23 9:28
     */
    @Override
    public List<SoOutstockDetailEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(SoOutstockDetailEntity::getMainId, mainIds).orderByAsc(SoOutstockDetailEntity::getId).list();
    }


    /**
     * 根据销售订单详情id获取到对应的下推数量
     *
     * @param soDetailIds
     * @return java.lang.Integer
     * @author yl
     * @date 2023-05-25 10:33
     */
    @Override
    public Integer getPushDownCountBySoDetailIds(List<String> soDetailIds) {
        if (CollectionUtils.isEmpty(soDetailIds)) {
            return 0;
        }
        return this.lambdaQuery().in(SoOutstockDetailEntity::getSourceDetailId, soDetailIds).count();
    }

    @Override
    public List<SoOutstockDetailEntity> listDetailBySoIds(List<String> soIds) {
        if (CollectionUtils.isEmpty(soIds)) {
            return Collections.emptyList();
        }
        soIds = soIds.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(soIds)) {
            return Collections.emptyList();
        }
        return baseMapper.listDetailBySoIds(soIds);
    }

    /**
     * 关闭关联单据的关闭状态
     *
     * @param soDetailIds
     * @return void
     * @author yl
     * @date 2023-05-25 19:25
     */
    @Override
    public void closeBySoDetailIds(List<String> soDetailIds) {
    }


    /**
     * 根据销售订单详情ids 获取对应的出库详情
     *
     * @param detailIds
     * @return java.util.List<com.erp.model.wms.entity.SoOutstockDetailEntity>
     * @author yl
     * @date 2023-05-29 17:32
     */
    @Override
    public List<SoOutstockDetailDTO.DeliveryQtyDTO> listDetailBySoDetailIds(List<String> detailIds) {
        if (CollectionUtils.isEmpty(detailIds)) {
            return Collections.emptyList();
        }

        List<SoOutstockDetailEntity> list = this.listBySoDetailIds(detailIds);
        List<SoOutstockDetailDTO.DeliveryQtyDTO> resultList = new ArrayList<>(list.size());
        for (SoOutstockDetailEntity item : list) {
            SoOutstockDetailDTO.DeliveryQtyDTO out = new SoOutstockDetailDTO.DeliveryQtyDTO();
            out.setActualQty(item.getActualQty());
            out.setPlanQty(item.getPlanQty());
            out.setId(item.getId());
            out.setSkuId(item.getSkuId());
            out.setSkuNo(item.getSkuNo());
            out.setApproveStatus(item.getApproveStatus());
            //这个可能是发货通知的单
            String sourceDetailId = item.getSourceDetailId();
            out.setSourceDetailId(sourceDetailId);

            out.setSoDetailId(item.getSoDetailId());
            resultList.add(out);
        }
        return resultList;
    }

    /**
     * 检测b2c 销售订单的数量
     *
     * @param
     * @return
     * @author yl
     * @date 2023-12-11 17:21
     */
    @Override
    public void checkB2cOrderQty(String warehouseId, String soId, String sourceId, String sourceType, List<SoOutstockDetailDTO.UpdateDTO> checkList) {
        if(CharSequenceUtil.isBlank(warehouseId)){
            throw new ServiceException(ApiError.WH_PARAM_NOT_FOUND);
        }
        // 忽略库存计算SKU
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
        List<String> skuIdList = checkList.stream().map(SoOutstockDetailDTO.AddDTO::getSkuId).collect(Collectors.toList());
        List<String> warehouseLocationList = checkList.stream().map(SoOutstockDetailDTO.AddDTO::getWarehouseLocation).collect(Collectors.toList());

        //销售订单详情id
        List<String> soDetailIdList = checkList.stream().map(SoOutstockDetailDTO.UpdateDTO::getSoDetailId).distinct().collect(Collectors.toList());
        //已经出库的数据
        List<SoOutstockDetailEntity> soOutstockDetailList = this.listBySoDetailIds(soDetailIdList);


        //b2c发货单
        String soB2cDelivery = SourceTypeEnum.SO_B2C_DELIVERY.getCode();
        if (soB2cDelivery.equals(sourceType)) {
            List<SoB2cDeliveryDetailEntity> soB2cDeliveryDetailList = soB2cDeliveryDetailService.listBySoDetailIds(soDetailIdList);

            for (SoOutstockDetailDTO.UpdateDTO item : checkList) {
                String soDetailId = item.getSoDetailId();
                //发货单的数量
                Integer deliveryQty = soB2cDeliveryDetailList.stream().
                        filter(s -> s.getSourceDetailId().equals(soDetailId)).mapToInt(SoB2cDeliveryDetailEntity::getDeliveryQty).sum();
                Integer planQty = item.getPlanQty();
                //这个是已出的数量
                Integer outStockQty = soOutstockDetailList.stream().filter(s ->
                        s.getSoDetailId().equals(soDetailId)
                ).mapToInt(SoOutstockDetailEntity::getActualQty).sum();
                if (ignoreInventorySkuIds.contains(item.getSkuId())) {
                    log.warn("sku id: {}产品属性是费用或服务，不参与库存出入库，不做库存验证", item.getSkuId());
                } else {
                    if (outStockQty + planQty > deliveryQty) {
                        throw new ServiceException(ApiError.SO_DELIVERY_QTY_GT_AVAILABLE_QTY);
                    }
                }
            }
        } else {
            InventoryQtyDTO.SkuInventoryParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryParamDTO();
            skuInventoryDTO.setSkuIdList(skuIdList);
            skuInventoryDTO.setWarehouseIdList(Collections.singletonList(warehouseId));
            skuInventoryDTO.setWarehouseLocationIdList(warehouseLocationList);
            skuInventoryDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
            //获取B2C销售订单详情集合
            List<SoB2cDetailEntity> soB2cDetailList = soB2cFeign.listDetailByIds(soDetailIdList);
            //可用数量
            List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryList = inventoryService.listSkuInventory(skuInventoryDTO);
            for (SoOutstockDetailDTO.UpdateDTO item : checkList) {
                String soDetailId = item.getSoDetailId();
                String skuId = item.getSkuId();
                //库位
                String warehouseLocation = item.getWarehouseLocation();
                //实发数量
                Integer actualQty = item.getActualQty();

                if (ignoreInventorySkuIds.contains(item.getSkuId())) {
                    log.warn("sku id: {}产品属性是费用或服务，不参与库存出入库，不做库存验证", item.getSkuId());
                } else {
                    //即时库存
                    Integer inventory = skuInventoryList.stream().filter(s -> s.getSkuId().equals(skuId) && s.getWarehouseLocationId().
                            equals(warehouseLocation)).findFirst().flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);
                    if (actualQty > inventory) {
//                        throw new ServiceException(ApiError.ERROR_DELIVERY_QTY_GT_STOCK);
                    }

                }
            }
        }
    }

    @Override
    public List<SoOutstockDetailDTO.AddDTO> checkAndGenerateDetail(SoOutstockDTO.GenerateB2cDTO dto) {
        if(!dto.isCheckSkuHistory()){
            return dto.getDetailList();
        }
        List<String> notExistMapping = dto.getDetailList()
                .stream()
                .filter(v->CollectionUtils.isEmpty(v.getHistorySkuMappingList()))
                .map(SoOutstockDetailDTO.AddDTO::getSkuNo).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(notExistMapping)){
            throw new ServiceException(CharSequenceUtil.format("{}找不到历史映射关系",notExistMapping));
        }
        // 生成库存检查参数
        List<String> skuIdList = new LinkedList<>();
        List<String> warehouseLocationList = new LinkedList<>();
        List<String> orgIdList = Collections.singletonList(dto.getWarehouseOrgId());
        List<String> warehouseIdList = Collections.singletonList(dto.getWarehouseId());
        for (SoOutstockDetailDTO.AddDTO addDTO : dto.getDetailList()) {
            List<String> skuIds = addDTO.getHistorySkuMappingList()
                    .stream()
                    .map(SoOutstockDetailDTO.ListingInfoWithSkuMappingGenDTO::getProductSkuId)
                    .distinct()
                    .collect(Collectors.toList());
            skuIdList.addAll(skuIds);
            warehouseLocationList.add(addDTO.getWarehouseLocation());
        }

        InventoryDTO.ParamDTO param = new InventoryDTO.ParamDTO();
        param.setOrgIdList(orgIdList);
        param.setSkuIdList(skuIdList);
        param.setWarehouseIdList(warehouseIdList);
        param.setWarehouseLocationList(warehouseLocationList);
        // 当前库存
        List<InventoryEntity> inventoryEntityList = inventoryService.listInventoryByParam(param);
        // Map<仓库ID_库存组织_skuId_仓位, 当前库存数量>
        Map<String, Integer> inventoryQtyMap = inventoryEntityList
                .stream()
                .filter(e-> InventoryStatusEnum.USABLE.getCode().equalsIgnoreCase(e.getDictInventoryStatus()))
                .collect(Collectors.toMap(e -> CharSequenceUtil.format("{}_{}_{}_{}", e.getWarehouseId(), e.getOrgId(), e.getSkuId(), e.getWarehouseLocation()), InventoryEntity::getQty));
        ConcurrentHashMap<String, Integer> currentInventoryQtyMap = new ConcurrentHashMap<>(inventoryQtyMap);
        // 查询仓库是否开启负库存
        WarehouseDTO.UpdateDTO warehouseDTO = warehouseService.detailWithCache(dto.getWarehouseId());
        if (null == warehouseDTO){
            throw new ServiceException(ApiError.SO_B2C_WAREHOUSE_NOT_FOUND);
        }
        Boolean allowNegativeInventory = warehouseDTO.getAllowNegativeInventory();

        // 重新生成的明细
        List<SoOutstockDetailDTO.AddDTO> resultAddDTOList = new LinkedList<>();
        // 根据所有映射关系和当前库存生成
        for (SoOutstockDetailDTO.AddDTO addDTO : dto.getDetailList()) {
            // 当前明细总数量
            Integer currentAllActualQty = addDTO.getActualQty();
            // 当前生成的明细信息
            List<SoOutstockDetailDTO.AddDTO> currentAddDTOList = new LinkedList<>();
            // 当前历史映射数量
            int currentSize = addDTO.getHistorySkuMappingList().size();
            // 当前历史映射
            LinkedList<SoOutstockDetailDTO.ListingInfoWithSkuMappingGenDTO> currentMappingList = addDTO.getHistorySkuMappingList();
            for (int idx = 0; idx < currentSize; idx++) {
                SoOutstockDetailDTO.ListingInfoWithSkuMappingGenDTO currentSkuMappingDTO = currentMappingList.get(idx);
                // 当前库存key
                String currentInventoryQtyKey = CharSequenceUtil.format("{}_{}_{}_{}", dto.getWarehouseId(), dto.getWarehouseOrgId(), currentSkuMappingDTO.getProductSkuId(), addDTO.getWarehouseLocation());
                // 当前库存数量
                Integer currentQty = currentInventoryQtyMap.getOrDefault(currentInventoryQtyKey, 0);

                if (currentQty >= currentAllActualQty) {
                    // 库存满足
                    SoOutstockDetailDTO.AddDTO currentAddDTO = new SoOutstockDetailDTO.AddDTO(currentSkuMappingDTO, addDTO, currentAllActualQty);
                    currentAddDTOList.add(currentAddDTO);
                    // 更新当前库存扣减
                    int resultQty = currentQty - currentAllActualQty;
                    currentInventoryQtyMap.put(currentInventoryQtyKey, resultQty);
                    break;
                } else {
                    // 库存不足
                    if (idx == currentSize - 1) {
                        // 当前元素是列表中的最后一个元素
                        if (allowNegativeInventory){
                            // 允许负库存扣减
                            SoOutstockDetailDTO.AddDTO currentAddDTO = new SoOutstockDetailDTO.AddDTO(currentSkuMappingDTO, addDTO, currentAllActualQty);
                            currentAddDTOList.add(currentAddDTO);
                            break;
                        } else {
                            // 库存不足
                            throw new ServiceException(ApiError.WH_SKU_MAPPING_STOCK_INSUFFICIENT, currentSkuMappingDTO.getProductSkuNo());
                        }
                    } else {
                        // 非最后元素扣减
                        if ( 0 < currentQty){
                            // 扣除当前剩余库存
                            currentAllActualQty = currentAllActualQty - currentQty;
                            SoOutstockDetailDTO.AddDTO currentAddDTO = new SoOutstockDetailDTO.AddDTO(currentSkuMappingDTO, addDTO, currentQty);
                            currentAddDTOList.add(currentAddDTO);
                        }
                        // 库存小于等于0跳过扣除
                    }
                }
            }
            resultAddDTOList.addAll(currentAddDTOList);
        }
        return resultAddDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDetailRemark(String soOutStockId, String remark, boolean updateErrorThrow) {
        boolean update = lambdaUpdate()
                .eq(SoOutstockDetailEntity::getMainId, soOutStockId)
                .set(SoOutstockDetailEntity::getRemark, remark)
                .update();
        if (!update && updateErrorThrow){
            throw new ServiceException("批量更新明细备注失败");
        }
    }


    /**
     * 获取到已生成销售订单的占的数量
     *
     * @param sourceDetailIdList
     * @param excludedIdList
     * @return
     */
    private List<SoOutstockDetailDTO.DeliveryQtyDTO> listDetailByDetailIds(List<String> sourceDetailIdList, List<String> excludedIdList) {
        if (CollectionUtils.isEmpty(sourceDetailIdList)) {
            return Collections.emptyList();
        }
        //发货通知详情列表
        List<SoDeliveryNoticeDetailEntity> noticeDetailSourceDetailList = soDeliveryNoticeDetailService.listByIds(sourceDetailIdList);
        List<String> noticeSourceDetailIdS = noticeDetailSourceDetailList.stream().map(SoDeliveryNoticeDetailEntity::getSourceDetailId).collect(Collectors.toList());
        sourceDetailIdList.addAll(noticeSourceDetailIdS);
        List<SoOutstockDetailEntity> list = this.listBySourceDetailIds(sourceDetailIdList);
        list = list.stream().filter(l -> !excludedIdList.contains(l.getId())).collect(Collectors.toList());

        List<SoOutstockDetailDTO.DeliveryQtyDTO> resultList = new ArrayList<>(list.size());
        for (SoOutstockDetailEntity item : list) {
            SoOutstockDetailDTO.DeliveryQtyDTO out = new SoOutstockDetailDTO.DeliveryQtyDTO();
            out.setActualQty(item.getActualQty());
            out.setPlanQty(item.getPlanQty());
            out.setId(item.getId());
            out.setSkuId(item.getSkuId());
            out.setSkuNo(item.getSkuNo());
            out.setApproveStatus(item.getApproveStatus());
            //这个可能是发货通知的单
            String sourceDetailId = item.getSourceDetailId();
            out.setSourceDetailId(sourceDetailId);
            //销售订单详情id
            String soDetailId = noticeDetailSourceDetailList.stream().filter(n -> n.getId().equals(sourceDetailId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSourceDetailId())).orElse(sourceDetailId);
            out.setSoDetailId(soDetailId);
            resultList.add(out);
        }
        return resultList;
    }

    private List<String> getDeleteIds(List<Pair<String, String>> pairList, List<SoOutstockDetailEntity> dbList) {
        List<String> ids = pairList.stream().filter(g -> CharSequenceUtil.isNotBlank(g.getKey())).
                map(obj -> obj.getKey()).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(SoOutstockDetailEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    /**
     * 根据来源id 集合获取到对应的数据
     *
     * @return
     */
    public List<SoOutstockDetailEntity> listBySourceDetailIds(List<String> sourceDetailIdList) {
        if (CollectionUtils.isEmpty(sourceDetailIdList)) {
            return Collections.emptyList();
        }
        List<SoOutstockDetailEntity> list = baseMapper.listSoOutstockBySourceDetailId(sourceDetailIdList);
        return list.stream().filter(s -> !s.getInvalidStatus()).collect(Collectors.toList());

    }


    /**
     * 获取基础销售出库单列表
     *
     * @param mainId
     * @return java.util.List<com.erp.model.wms.entity.SoOutstockDetailEntity>
     * @author yl
     * @date 2023-05-19 11:36
     */
    private List<SoOutstockDetailEntity> listBaseByMainId(String mainId) {
        if (CharSequenceUtil.isNotBlank(mainId)) {
            return this.lambdaQuery().eq(SoOutstockDetailEntity::getMainId, mainId).orderByAsc(SoOutstockDetailEntity::getId).list();
        }
        return Collections.emptyList();

    }

    /**
     * @param detailList
     * @description: 处理明细数据
     * @author Will
     * @date: 2023/11/1 15:27
     */
    private void handleDetailData(List<SoOutstockDetailEntity> detailList) {
        handleDetailData(detailList, null, null);
    }

    private void handleDetailData(List<SoOutstockDetailEntity> detailList, SoInfoEntity prefetchedSoInfoEntity, List<SoDetailEntity> prefetchedSoDetailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<String> soDetailIdList = detailList.stream().map(SoOutstockDetailEntity::getSoDetailId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        boolean usePrefetchedDetailList = Objects.nonNull(prefetchedSoDetailList);
        List<SoDetailEntity> allSoDetailList = usePrefetchedDetailList ? prefetchedSoDetailList : null;
        List<SoDetailEntity> soDetailList = Objects.nonNull(allSoDetailList)
                ? allSoDetailList.stream().filter(item -> soDetailIdList.contains(item.getId())).collect(Collectors.toList())
                : soInfoFeign.listSoDetailByIds(soDetailIdList);
        soDetailList = Objects.isNull(soDetailList) ? Collections.emptyList() : soDetailList;

        //销售订单主表信息
        List<String> soIdList = soDetailList.stream().map(SoDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<SoInfoEntity> soInfoList = Objects.nonNull(prefetchedSoInfoEntity)
                ? Collections.singletonList(prefetchedSoInfoEntity)
                : usePrefetchedDetailList ? Collections.emptyList() : soInfoFeign.listSoInfoByIds(soIdList);
        soInfoList = Objects.isNull(soInfoList) ? Collections.emptyList() : soInfoList;
        allSoDetailList = Objects.nonNull(allSoDetailList)
                ? allSoDetailList
                : CollectionUtils.isNotEmpty(soIdList) ? soInfoFeign.listSoDetailByMainIds(soIdList) : Collections.emptyList();
        allSoDetailList = Objects.isNull(allSoDetailList) ? Collections.emptyList() : allSoDetailList;
        Map<String, BigDecimal> orderWeightMap = allSoDetailList.stream()
                .collect(Collectors.groupingBy(SoDetailEntity::getMainId,
                        Collectors.mapping(this::calculateB2bDetailWeight,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

        for (SoOutstockDetailEntity detailEntity : detailList) {
            //销售订单明细
            SoDetailEntity soDetailEntity = soDetailList.stream().filter(obj -> obj.getId().equals(detailEntity.getSoDetailId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(soDetailEntity)) {
                throw new ServiceException(ApiError.SO_DETAIL_NOT_FOUND);
            }
            SoInfoEntity soInfoEntity = soInfoList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), soDetailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soInfoEntity)) {
                throw new ServiceException(ApiError.SO_NOT_FOUND);
            }
            //虚拟仓信息
            detailEntity.setVirtualWarehouseId(soInfoEntity.getVirtualWarehouseId());

            //单价信息
            BigDecimal price = getB2bOutstockPrice(soDetailEntity);
            detailEntity.setPrice(price);
            detailEntity.setTaxRate(soDetailEntity.getTaxRate());
            detailEntity.setExchangeRate(soDetailEntity.getExchangeRate());
            detailEntity.setAmount(MathUtil.multiplyWithTwo(price, detailEntity.getActualQty()));
            detailEntity.setCurrency(soDetailEntity.getCurrency());
            detailEntity.setCurrencySymbol(soDetailEntity.getCurrencySymbol());
            BigDecimal allAmountLocalCurrency = calculateB2bAllAmountLocalCurrency(soInfoEntity, soDetailEntity,
                    detailEntity.getActualQty(), orderWeightMap.get(soDetailEntity.getMainId()));
            detailEntity.setAllAmountLocalCurrency(allAmountLocalCurrency);
            detailEntity.setTaxAmount(divideAmount(allAmountLocalCurrency, defaultExchangeRate(soDetailEntity.getExchangeRate()), RoundingMode.HALF_UP));
            detailEntity.setRemark(soDetailEntity.getRemark());
            detailEntity.setCustomerPO(soDetailEntity.getCustomerPO());
        }
    }

    private BigDecimal calculateB2bAllAmountLocalCurrency(SoInfoEntity soInfoEntity, SoDetailEntity soDetailEntity,
                                                          Integer actualQty, BigDecimal totalWeight) {
        BigDecimal exchangeRate = defaultExchangeRate(soDetailEntity.getExchangeRate());
        BigDecimal orderLocalAmount = getB2bOrderLocalAmount(soInfoEntity, exchangeRate);
        return calculateAllocatedAmount(orderLocalAmount, calculateB2bDetailWeight(soDetailEntity), totalWeight,
                getB2bSalesQty(soDetailEntity), actualQty, RoundingMode.DOWN);
    }

    private BigDecimal getB2bOrderLocalAmount(SoInfoEntity soInfoEntity, BigDecimal exchangeRate) {
        BigDecimal orderAmount = soInfoEntity.getOrderAmount();
        if (Objects.nonNull(orderAmount)) {
            return orderAmount.multiply(exchangeRate);
        }
        return MathUtil.nvl(soInfoEntity.getAllAmountLc(), BigDecimal.ZERO);
    }

    private BigDecimal calculateB2bDetailWeight(SoDetailEntity soDetailEntity) {
        return getB2bSalesQty(soDetailEntity).multiply(getB2bOutstockPrice(soDetailEntity));
    }

    private BigDecimal getB2bSalesQty(SoDetailEntity soDetailEntity) {
        Integer boxQty = soDetailEntity.getBoxQty();
        if (Objects.nonNull(boxQty) && boxQty > 0) {
            return BigDecimal.valueOf(boxQty);
        }
        return BigDecimal.valueOf(Objects.nonNull(soDetailEntity.getQty()) ? soDetailEntity.getQty() : 0);
    }

    private BigDecimal getB2bOutstockPrice(SoDetailEntity soDetailEntity) {
        BigDecimal price = MathUtil.nvl(soDetailEntity.getPrice(), BigDecimal.ZERO);
        Integer perBoxQty = soDetailEntity.getPerBoxQty();
        if (Objects.nonNull(perBoxQty) && perBoxQty > 0) {
            return price.multiply(BigDecimal.valueOf(perBoxQty)).setScale(4, RoundingMode.HALF_UP);
        }
        return price;
    }


    /**
     * 处理b2c 销售订单明细信息
     *
     * @param detailList
     */
    @Override
    public void handleB2cDetailData(List<SoOutstockDetailEntity> detailList,SoOutstockEntity entity) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        if(StringUtils.isBlank(entity.getSoId())){
            return;
        }
        SoB2cEntity soB2cEntity = soB2cFeign.getById(entity.getSoId());
        if(Objects.isNull(soB2cEntity)){
            return;
        }
        List<SoB2cDetailEntity> soDetailList = soB2cFeign.listDetailByMainIds(Collections.singletonList(entity.getSoId()));
        handleB2cDetailDataInternal(detailList, entity, soB2cEntity, soDetailList);
    }

    /**
     * 批量入口：复用调用方预取的 SoB2cEntity / SoB2cDetail 列表，避免每个出库单各自走 Feign 远程查询
     */
    private void handleB2cDetailDataInternal(List<SoOutstockDetailEntity> detailList, SoOutstockEntity entity,
                                             SoB2cEntity soB2cEntity, List<SoB2cDetailEntity> soDetailList) {
        if (CollectionUtils.isEmpty(detailList) || Objects.isNull(soB2cEntity) || CollectionUtils.isEmpty(soDetailList)) {
            return;
        }
        List<String> outSkuIds = detailList.stream().map(SoOutstockDetailEntity::getSkuId).collect(Collectors.toList());
        List<String> currencyIdList = soDetailList.stream().map(SoB2cDetailEntity::getCurrency).collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyList = CollectionUtils.isNotEmpty(currencyIdList) ? sysUserFeign.listByCurrency(currencyIdList) : Collections.emptyList();
        List<String> skuIds = soDetailList.stream().map(SoB2cDetailEntity::getSkuId).collect(Collectors.toList());
        //子sku列表
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);
        bomChildrenSkuDTOS = bomChildrenSkuDTOS.stream().filter(v-> BomTypeEnum.COMBINATION.getType().equals(v.getType())).collect(Collectors.toList());
        List<String> childSkuList = bomChildrenSkuDTOS.stream().map(BomChildrenSkuDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuCostByIds(childSkuList);

        SkuStdSettingEnum allocationSettingEnum = SkuStdSettingEnum.COST_AVG;
        // 查询成本分摊配置
        List<PlmCfgSettingEntity> cfgList = FeignQuery.create(PlmCfgSettingEntity.class).eq(PlmCfgSettingEntity::getKey, "sku_std_setting").list();
        if (CollectionUtils.isNotEmpty(cfgList)) {
            allocationSettingEnum = SkuStdSettingEnum.getByCode(cfgList.get(0).getRemark());
        }

        Map<String, BigDecimal> retailPricetotalMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(bomChildrenSkuDTOS) && SkuStdSettingEnum.RETAIL_STD.equals(allocationSettingEnum)){
            List<String> childSkuIds = bomChildrenSkuDTOS.stream().map(BomChildrenSkuDTO::getSkuId).collect(Collectors.toList());
            // 查询标准成本信息
            List<SkuStdRetailPriceEntity> soB2cStdRetailPriceList = FeignQuery.create(SkuStdRetailPriceEntity.class).in(SkuStdRetailPriceEntity::getSkuId,childSkuIds).list();
            // key: skuId|currency  value: 含税标准零售价
            retailPricetotalMap = soB2cStdRetailPriceList.stream().collect(Collectors.toMap(e -> CharSequenceUtil.format("{}|{}", e.getSkuId(), e.getCurrency()), SkuStdRetailPriceEntity::getStdRetailPriceVat, (d1, d2) -> d1));
        }

        for (SoOutstockDetailEntity detailEntity : detailList) {
            SoB2cDetailEntity soDetailEntity = soDetailList.stream().filter(obj -> obj.getId().equals(detailEntity.getSoDetailId())).findFirst().orElse(null);
            if(Objects.nonNull(soDetailEntity) && soDetailEntity.getSkuId().equals(detailEntity.getSkuId())){
                continue;
            }
            BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenSkuDTOS.stream().filter(obj -> obj.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(bomChildrenSkuDTO)) {
                continue;
            }
            SkuVO skuVO = skuVOList.stream().filter(v -> v.getSkuId().equals(bomChildrenSkuDTO.getSkuId())).findFirst().orElse(null);
            if (Objects.isNull(skuVO)) {
                continue;
            }
            // 检查和获取标准零售价
            // Map<SkuId, 含税标准零售价>
            Map<String, BigDecimal> acticityRetailPriceMap = RetailPriceUtil.checkAndGetRetailPrice(allocationSettingEnum.getCode(),
                    retailPricetotalMap,
                    detailEntity.getCurrency(),
                    bomChildrenSkuDTOS.stream()
                            .map(BomChildrenSkuDTO::getSkuId)
                            .distinct()
                            .collect(Collectors.toList())
            );

            // 分摊单价
            BigDecimal allocationPrice =  skuVO.getActualTaxCost();
            BigDecimal retailPrice = acticityRetailPriceMap.get(detailEntity.getSkuId());
            if (null == retailPrice){
                detailEntity.setPriceAllocationSource(CharSequenceUtil.isBlank(skuVO.getPriceAllocationSource()) ? "采购平均成本" : skuVO.getPriceAllocationSource());
            } else {
                detailEntity.setPriceAllocationSource(PriceAllocationSourceEnum.RETAIL_STD.getName());
                allocationPrice = retailPrice;
            }
            if (Objects.isNull(allocationPrice) || allocationPrice.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            // 当前明细分摊金额 = 单价 * BOM数量
            detailEntity.setAllocationAmount(allocationPrice.multiply(new BigDecimal(bomChildrenSkuDTO.getQuantity())));
        }

        BigDecimal allDetailAmount = soDetailList.stream().map(this::calculateB2cDetailWeight).reduce(BigDecimal.ZERO, BigDecimal::add);

        for (SoOutstockDetailEntity detailEntity : detailList) {
            String symbol = currencyList.stream().filter(c -> c.getId().equals(detailEntity.getCurrency())).findFirst().map(CurrencyDTO.ViewDTO::getSymbol).orElse("");
            detailEntity.setCurrencySymbol(symbol);
            if(StringUtils.isBlank(detailEntity.getPlatformCode()) && Objects.nonNull(soB2cEntity)){
                detailEntity.setPlatformCode(soB2cEntity.getPlatformCode());
            }
            //销售订单明细
            SoB2cDetailEntity soDetailEntity = soDetailList.stream().filter(obj -> obj.getId().equals(detailEntity.getSoDetailId())).findFirst().orElse(null);
            BigDecimal price = BigDecimal.ZERO;
            BigDecimal taxPrice = BigDecimal.ZERO;
            if (ObjectUtils.isEmpty(soDetailEntity)) {
                //平台仓订单没有明细通过sku关联
                soDetailEntity = soDetailList.stream().filter(obj -> obj.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(soDetailEntity)){
                    //还关联不到看明细是否有组合品，有的话通过子件关联
                    BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenSkuDTOS.stream().filter(obj -> obj.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
                    if(ObjectUtils.isEmpty(bomChildrenSkuDTO)){
                        continue;
                    }
                    soDetailEntity = soDetailList.stream()
                            .filter(obj -> obj.getSkuId().equals(bomChildrenSkuDTO.getParentSkuId()))
                            .findFirst().orElse(new SoB2cDetailEntity());
                    //查询出库单明细是否有其他子件
                    List<BomChildrenSkuDTO> sameBomChildrenSkuDTOList = listOutBomChildren(bomChildrenSkuDTOS, bomChildrenSkuDTO.getParentSkuId(), outSkuIds);
                    if (!sameBomChildrenSkuDTOList.isEmpty()) {

                        taxPrice = calcDetailTaxUnitPrice(soB2cEntity, soDetailEntity, allDetailAmount);

                        // 有其他子件 将单价分摊
                        price = allocateB2cComboChildUnitAmount(soDetailEntity.getPrice(), detailEntity, bomChildrenSkuDTO, sameBomChildrenSkuDTOList, detailList);
                        taxPrice = allocateB2cComboChildUnitAmount(taxPrice, detailEntity, bomChildrenSkuDTO, sameBomChildrenSkuDTOList, detailList);
                    }
                }else{
                    price = soDetailEntity.getPrice();
                    taxPrice = calcDetailTaxUnitPrice(soB2cEntity, soDetailEntity, allDetailAmount);
                }
            }else{
                price = soDetailEntity.getPrice();
                taxPrice = calcDetailTaxUnitPrice(soB2cEntity, soDetailEntity, allDetailAmount);
                if(!detailEntity.getSkuId().equals(soDetailEntity.getSkuId())){
                    //不是组合品直接取单价
                    String soSkuId = soDetailEntity.getSkuId();
                    //判断是否是订单是组合品，出库单是子件的情况，如果是的话，通过子件分摊成本计算单价
                    BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenSkuDTOS.stream().filter(obj -> obj.getSkuId().equals(detailEntity.getSkuId()) && obj.getParentSkuId().equals(soSkuId)).findFirst().orElse(null);
                    if(ObjectUtils.isNotEmpty(bomChildrenSkuDTO)){
                        //查询出库单明细是否有其他子件
                        List<BomChildrenSkuDTO> sameBomChildrenSkuDTOList = listOutBomChildren(bomChildrenSkuDTOS, bomChildrenSkuDTO.getParentSkuId(), outSkuIds);
                        if (!sameBomChildrenSkuDTOList.isEmpty()) {
                            // 有其他子件 将单价分摊
                            price = allocateB2cComboChildUnitAmount(soDetailEntity.getPrice(), detailEntity, bomChildrenSkuDTO, sameBomChildrenSkuDTOList, detailList);
                            taxPrice = allocateB2cComboChildUnitAmount(taxPrice, detailEntity, bomChildrenSkuDTO, sameBomChildrenSkuDTOList, detailList);
                        }
                    }
                }
            }
            //虚拟仓库
            if(StringUtils.isBlank(detailEntity.getVirtualWarehouseId())){
                detailEntity.setVirtualWarehouseId(soDetailEntity.getVirtualWarehouseId());
            }
            if(StringUtils.isBlank(detailEntity.getSoDetailId())){
                detailEntity.setSoDetailId(soDetailEntity.getId());
            }
            //单价信息
            detailEntity.setPrice(price);
            BigDecimal exchangeRate=soDetailEntity.getExchangeRate();
            detailEntity.setExchangeRate(exchangeRate);
            BigDecimal amount=MathUtil.multiplyWithTwo(price, detailEntity.getActualQty());
            detailEntity.setAmount(amount);
            String currency = soDetailEntity.getCurrency();
            detailEntity.setCurrency(currency);
            symbol = currencyList.stream().filter(c -> c.getId().equals(currency)).findFirst().map(CurrencyDTO.ViewDTO::getSymbol).orElse("");
            detailEntity.setCurrencySymbol(symbol);
            BigDecimal taxAmount = calculateB2cTaxAmount(soB2cEntity, soDetailEntity, detailEntity, taxPrice, allDetailAmount);
            detailEntity.setTaxAmount(taxAmount);
            detailEntity.setAllAmountLocalCurrency(calculateB2cAllAmountLocalCurrency(soB2cEntity, soDetailEntity,
                    detailEntity, taxAmount, allDetailAmount, exchangeRate));
        }
    }

    private List<BomChildrenSkuDTO> listOutBomChildren(List<BomChildrenSkuDTO> bomChildrenSkuDTOS, String parentSkuId, List<String> outSkuIds) {
        return bomChildrenSkuDTOS.stream()
                .filter(v -> CharSequenceUtil.equals(v.getParentSkuId(), parentSkuId) && outSkuIds.contains(v.getSkuId()))
                .collect(Collectors.toList());
    }

    private BigDecimal allocateB2cComboChildUnitAmount(BigDecimal parentUnitAmount, SoOutstockDetailEntity currentDetail,
                                                       BomChildrenSkuDTO currentChild,
                                                       List<BomChildrenSkuDTO> sameBomChildrenSkuDTOList,
                                                       List<SoOutstockDetailEntity> detailList) {
        BigDecimal currentCost = currentDetail.getAllocationAmount();
        BigDecimal totalCost = getB2cComboChildTotalAllocationAmount(currentDetail, currentChild.getParentSkuId(), sameBomChildrenSkuDTOList, detailList);
        BigDecimal childQty = BigDecimal.valueOf(Objects.nonNull(currentChild.getQuantity()) ? currentChild.getQuantity() : 0);
        if (Objects.nonNull(currentCost) && MathUtil.compareTo(totalCost, BigDecimal.ZERO) != 0
                && MathUtil.compareTo(childQty, BigDecimal.ZERO) != 0) {
            return MathUtil.nvl(parentUnitAmount, BigDecimal.ZERO)
                    .multiply(currentCost.divide(totalCost, 4, RoundingMode.HALF_UP))
                    .divide(childQty, 4, RoundingMode.HALF_UP);
        }
        BigDecimal totalQuantity = sameBomChildrenSkuDTOList.stream()
                .map(child -> BigDecimal.valueOf(Objects.nonNull(child.getQuantity()) ? child.getQuantity() : 0))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (MathUtil.compareTo(totalQuantity, BigDecimal.ZERO) == 0 || MathUtil.compareTo(childQty, BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        // 无成本/零售价分摊数据时的兜底：父单价按子件总数均分到每个子件单位上
        // 数学等价于 parentUnitAmount / totalQuantity；保留 childQty / childQty 写法
        // 是为了与有 allocationAmount 时的 (currentCost / totalCost) 分摊公式结构对称、便于对照阅读
        return MathUtil.nvl(parentUnitAmount, BigDecimal.ZERO)
                .multiply(childQty.divide(totalQuantity, 4, RoundingMode.HALF_UP))
                .divide(childQty, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal getB2cComboChildTotalAllocationAmount(SoOutstockDetailEntity currentDetail, String parentSkuId, List<BomChildrenSkuDTO> sameBomChildrenSkuDTOList,
                                                            List<SoOutstockDetailEntity> detailList) {
        Set<String> childSkuIds = sameBomChildrenSkuDTOList.stream()
                .filter(child -> CharSequenceUtil.equals(child.getParentSkuId(), parentSkuId))
                .map(BomChildrenSkuDTO::getSkuId)
                .collect(Collectors.toSet());
        String soDetailId = currentDetail.getSoDetailId();
        return detailList.stream()
                .filter(detail -> childSkuIds.contains(detail.getSkuId()))
                .filter(detail -> CharSequenceUtil.isBlank(soDetailId) || CharSequenceUtil.equals(detail.getSoDetailId(), soDetailId))
                .map(SoOutstockDetailEntity::getAllocationAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 计算 B2C 明细按整单金额分摊后的含税单价；所有空值/零值场景均回退到 0，避免 NPE 与除零。
     */
    private BigDecimal calcDetailTaxUnitPrice(SoB2cEntity soB2cEntity, SoB2cDetailEntity soDetailEntity, BigDecimal allDetailAmount) {
        if (Objects.isNull(soB2cEntity) || Objects.isNull(soDetailEntity)) {
            return BigDecimal.ZERO;
        }
        Integer qty = soDetailEntity.getQty();
        if (qty == null || qty == 0 || Objects.isNull(allDetailAmount) || allDetailAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal detailAmount = MathUtil.nvl(soDetailEntity.getPrice(), BigDecimal.ZERO)
                .multiply(BigDecimal.valueOf(qty));
        return MathUtil.nvl(soB2cEntity.getAmount(), BigDecimal.ZERO)
                .multiply(detailAmount.divide(allDetailAmount, 4, RoundingMode.HALF_UP))
                .divide(BigDecimal.valueOf(qty), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateB2cTaxAmount(SoB2cEntity soB2cEntity, SoB2cDetailEntity soDetailEntity,
                                             SoOutstockDetailEntity detailEntity, BigDecimal taxPrice,
                                             BigDecimal totalWeight) {
        if (Objects.nonNull(soB2cEntity) && Objects.nonNull(soDetailEntity)
                && CharSequenceUtil.equals(detailEntity.getSkuId(), soDetailEntity.getSkuId())) {
            return calculateAllocatedAmount(MathUtil.nvl(soB2cEntity.getAmount(), BigDecimal.ZERO),
                    calculateB2cDetailWeight(soDetailEntity), totalWeight,
                    BigDecimal.valueOf(Objects.nonNull(soDetailEntity.getQty()) ? soDetailEntity.getQty() : 0),
                    detailEntity.getActualQty(), RoundingMode.DOWN);
        }
        return multiplyAmount(taxPrice, BigDecimal.valueOf(Objects.nonNull(detailEntity.getActualQty()) ? detailEntity.getActualQty() : 0),
                RoundingMode.DOWN);
    }

    private BigDecimal calculateB2cAllAmountLocalCurrency(SoB2cEntity soB2cEntity, SoB2cDetailEntity soDetailEntity,
                                                          SoOutstockDetailEntity detailEntity, BigDecimal taxAmount,
                                                          BigDecimal totalWeight, BigDecimal exchangeRate) {
        BigDecimal safeExchangeRate = defaultExchangeRate(exchangeRate);
        if (Objects.nonNull(soB2cEntity) && Objects.nonNull(soDetailEntity)
                && CharSequenceUtil.equals(detailEntity.getSkuId(), soDetailEntity.getSkuId())) {
            return calculateAllocatedAmount(MathUtil.nvl(soB2cEntity.getAmount(), BigDecimal.ZERO).multiply(safeExchangeRate),
                    calculateB2cDetailWeight(soDetailEntity), totalWeight,
                    BigDecimal.valueOf(Objects.nonNull(soDetailEntity.getQty()) ? soDetailEntity.getQty() : 0),
                    detailEntity.getActualQty(), RoundingMode.DOWN);
        }
        return multiplyAmount(taxAmount, safeExchangeRate, RoundingMode.DOWN);
    }

    private BigDecimal calculateB2cDetailWeight(SoB2cDetailEntity soDetailEntity) {
        return MathUtil.nvl(soDetailEntity.getPrice(), BigDecimal.ZERO)
                .multiply(BigDecimal.valueOf(Objects.nonNull(soDetailEntity.getQty()) ? soDetailEntity.getQty() : 0));
    }

    private BigDecimal calculateAllocatedAmount(BigDecimal totalAmount, BigDecimal detailWeight, BigDecimal totalWeight,
                                                BigDecimal salesQty, Integer actualQty, RoundingMode roundingMode) {
        if (MathUtil.compareTo(totalWeight, BigDecimal.ZERO) == 0 || MathUtil.compareTo(salesQty, BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(4, roundingMode);
        }
        BigDecimal qty = BigDecimal.valueOf(Objects.nonNull(actualQty) ? actualQty : 0);
        return MathUtil.nvl(totalAmount, BigDecimal.ZERO)
                .multiply(MathUtil.nvl(detailWeight, BigDecimal.ZERO))
                .divide(totalWeight, 12, RoundingMode.HALF_UP)
                .divide(salesQty, 12, RoundingMode.HALF_UP)
                .multiply(qty)
                .setScale(4, roundingMode);
    }

    private BigDecimal multiplyAmount(BigDecimal amount, BigDecimal multiplier, RoundingMode roundingMode) {
        return MathUtil.nvl(amount, BigDecimal.ZERO)
                .multiply(MathUtil.nvl(multiplier, BigDecimal.ZERO))
                .setScale(4, roundingMode);
    }

    private BigDecimal divideAmount(BigDecimal amount, BigDecimal divisor, RoundingMode roundingMode) {
        if (MathUtil.compareTo(divisor, BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(4, roundingMode);
        }
        return MathUtil.nvl(amount, BigDecimal.ZERO).divide(divisor, 4, roundingMode);
    }

    private BigDecimal defaultExchangeRate(BigDecimal exchangeRate) {
        if (Objects.isNull(exchangeRate) || BigDecimal.ZERO.compareTo(exchangeRate) == 0) {
            return BigDecimal.ONE;
        }
        return exchangeRate;
    }

    @Override
    public void refreshAmountFields(List<SoOutstockDetailEntity> detailList, SoOutstockEntity entity) {
        if (CollectionUtils.isEmpty(detailList) || Objects.isNull(entity)) {
            return;
        }
        if (OrderTypeEnum.B2C.getCode().equals(entity.getOrderType())) {
            handleB2cDetailData(detailList, entity);
        } else {
            handleDetailData(detailList);
        }
    }

    @Override
    public void refreshAmountFields(List<SoOutstockDetailEntity> detailList, SoOutstockEntity entity,
                                    SoB2cEntity soB2cEntity, List<SoB2cDetailEntity> soB2cDetailList) {
        if (CollectionUtils.isEmpty(detailList) || Objects.isNull(entity)) {
            return;
        }
        if (OrderTypeEnum.B2C.getCode().equals(entity.getOrderType())) {
            if (StringUtils.isBlank(entity.getSoId())) {
                return;
            }
            handleB2cDetailDataInternal(detailList, entity, soB2cEntity, soB2cDetailList);
        } else {
            handleDetailData(detailList);
        }
    }

    @Override
    public void refreshAmountFields(List<SoOutstockDetailEntity> detailList, SoOutstockEntity entity,
                                    SoInfoEntity soInfoEntity, List<SoDetailEntity> soDetailList) {
        if (CollectionUtils.isEmpty(detailList) || Objects.isNull(entity)) {
            return;
        }
        if (OrderTypeEnum.B2C.getCode().equals(entity.getOrderType())) {
            handleB2cDetailData(detailList, entity);
        } else {
            handleDetailData(detailList, soInfoEntity, soDetailList);
        }
    }

    /**
     * @param soDetailIdList
     * @return List<SoOutstockDetailEntity>
     * @description: 根据销售订单明细ids查询
     * @author Will
     * @date: 2023/11/1 15:45
     */
    @Override
    public List<SoOutstockDetailEntity> listBySoDetailIds(List<String> soDetailIdList) {
        if (CollectionUtils.isEmpty(soDetailIdList)) {
            return Collections.emptyList();
        }
        List<SoOutstockDetailEntity> list = baseMapper.listBySoDetailIds(soDetailIdList);
        return list;
    }

//    @Override
//    public List<SoOutstockDTO.GroupSkuDTO> listGroupSkuByMainId(String mainId) {
//        List<SoOutstockDTO.GroupSkuDTO> list = baseMapper.listGroupSkuByMainId(mainId);
//
//        //查询产品信息
//        List<String> skuIdList = list.stream().map(req -> req.getSkuId()).collect(Collectors.toList());
//        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);
//
//        //查询已装箱数
//        List<WmsCartonSpecDTO.PackingQtyDTO> packingQtyDTOS = wmsCartonSpecService.listPackingQtyByMainId(mainId, null);
//        for (SoOutstockDTO.GroupSkuDTO groupSkuDTO : list) {
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
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateSoOutPrice(List<SoDetailEntity> soDetailEntityList) {
        soDetailEntityList = soDetailEntityList.stream().filter(v->CharSequenceUtil.isNotBlank(v.getId())).collect(Collectors.toList());
        List<String> soDetailIds = soDetailEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(soDetailIds)){
            return true;
        }
        List<SoOutstockDetailEntity> soOutstockDetailEntityList = this.lambdaQuery().in(SoOutstockDetailEntity::getSoDetailId,soDetailIds).list();
        List<String> orderIdList = soDetailEntityList.stream().map(SoDetailEntity::getMainId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SoInfoEntity> soInfoList = CollectionUtils.isNotEmpty(orderIdList) ? soInfoFeign.listSoInfoByIds(orderIdList) : Collections.emptyList();
        List<SoDetailEntity> allSoDetailList = CollectionUtils.isNotEmpty(orderIdList) ? soInfoFeign.listSoDetailByMainIds(orderIdList) : Collections.emptyList();
        Map<String, BigDecimal> orderWeightMap = allSoDetailList.stream()
                .collect(Collectors.groupingBy(SoDetailEntity::getMainId,
                        Collectors.mapping(this::calculateB2bDetailWeight,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
        List<SoOutstockDetailEntity> updateList = new ArrayList<>();
        for (SoOutstockDetailEntity detailEntity : soOutstockDetailEntityList) {
            //销售订单明细
            SoDetailEntity soDetailEntity = soDetailEntityList.stream().filter(obj -> obj.getId().equals(detailEntity.getSoDetailId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(soDetailEntity)) {
                continue;
            }
            SoInfoEntity soInfoEntity = soInfoList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), soDetailEntity.getMainId())).findFirst().orElse(null);
            if (Objects.isNull(soInfoEntity)) {
                continue;
            }
            BigDecimal price = getB2bOutstockPrice(soDetailEntity);
            //单价信息
            detailEntity.setPrice(price);
            BigDecimal exchangeRate = defaultExchangeRate(soDetailEntity.getExchangeRate());
            detailEntity.setExchangeRate(exchangeRate);
            BigDecimal amount = MathUtil.multiplyWithTwo(price, detailEntity.getActualQty());
            detailEntity.setAmount(amount);
            BigDecimal allAmountLocalCurrency = calculateB2bAllAmountLocalCurrency(soInfoEntity, soDetailEntity,
                    detailEntity.getActualQty(), orderWeightMap.get(soDetailEntity.getMainId()));
            detailEntity.setAllAmountLocalCurrency(allAmountLocalCurrency);
            detailEntity.setTaxAmount(divideAmount(allAmountLocalCurrency, exchangeRate, RoundingMode.HALF_UP));
            updateList.add(detailEntity);
        }
        if(CollectionUtils.isNotEmpty(updateList)){
            this.updateBatchById(updateList);
        }
        
        return true;
    }

    @Override
    public List<SoB2bProcessingDTO.ResponseDTO> listSoOutstockBySourceIdList(List<String> sourceIdList) {
        return baseMapper.listSoOutstockBySourceIdList(sourceIdList);
    }

    @Override
    public List<SoDeliveryNoticeDetailDTO.PushDownDTO> getPushDownBySoDetailIds(List<String> soDetailIds) {
        if (CollectionUtils.isEmpty(soDetailIds)) {
            return new ArrayList<>();
        }
        List<SoDeliveryNoticeDetailDTO.PushDownDTO> result = new ArrayList<>();
        List<SoOutstockDetailEntity> soOutstockDetailEntityList = baseMapper.listBySoDetailIds(soDetailIds);
        if(CollUtil.isNotEmpty(soOutstockDetailEntityList)){
            for (SoOutstockDetailEntity detailEntity : soOutstockDetailEntityList) {
                SoDeliveryNoticeDetailDTO.PushDownDTO pushDownDTO = new SoDeliveryNoticeDetailDTO.PushDownDTO();
                pushDownDTO.setSoDetailId(detailEntity.getSourceDetailId());
                pushDownDTO.setSkuId(detailEntity.getSkuId());
                pushDownDTO.setSkuNo(detailEntity.getSkuNo());
                result.add(pushDownDTO);
            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    @GlobalTransactional(propagation = Propagation.NOT_SUPPORTED)
    public Map<String, LocalDate> mapLastOutstockDateBySkuIds(List<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Collections.emptyMap();
        }
        List<SoOutstockDTO.LastBillDateDTO> lastBillDateDTOS = baseMapper.mapLastOutstockDateBySkuIds(skuIds);
        if (CollectionUtils.isEmpty(lastBillDateDTOS)) {
            return Collections.emptyMap();
        }
        return lastBillDateDTOS.stream().collect(Collectors.toMap(SoOutstockDTO.LastBillDateDTO::getSkuId, SoOutstockDTO.LastBillDateDTO::getBillDate));
    }

    @Override
    public List<SoOutstockDTO.KolSoOutstockDTO> listSoOutstockByTime(SoOutstockDTO.KolSoOutstockDateDTO dto) {
        return baseMapper.listSoOutstockByTime(dto);
    }
}
