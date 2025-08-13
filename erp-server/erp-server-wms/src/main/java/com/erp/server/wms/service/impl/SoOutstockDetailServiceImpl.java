package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
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
    public void add(String mainId, List<SoOutstockDetailDTO.AddDTO> detailList, String orderType, SoOutstockEntity entity) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
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
            handleB2cDetailData(addList);
        }
        //赋值仓库名称
        if(Objects.nonNull(entity)){
            addList.forEach(v->v.setWarehouseName(entity.getWarehouseName()));
        }

        super.saveBatch(addList);
        wmsAttachmentService.saveBatch(batchAttachmentList);
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
        LambdaQueryWrapper<SoOutstockDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SoOutstockDetailEntity::getMainId, mainIdList);
        this.remove(queryWrapper);

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
            throw new ServiceException(ApiError.ERROR_92029);
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
        if (soDeliveryNotice.equals(sourceType)) {
            List<SoOutstockDetailEntity> detailEntities = listDetailBySoIds(Collections.singletonList(soId));
            //添加校验
            List<SoDetailEntity> soDetails = soInfoFeign.listSoDetailByMainIds(Collections.singletonList(soId));
            for (SoOutstockDetailDTO.UpdateDTO dto : detailList) {
                int sellQty = soDetails.stream()
                        .filter(v -> v.getSkuNo().equals(dto.getSkuNo()))
                        .mapToInt(SoDetailEntity::getQty).sum();
                if (sellQty == 0) {
                    throw new ServiceException(ApiError.ERROR_99107, dto.getSkuNo());
                }
                int actualQty = detailEntities.stream()
                        .filter(e -> Boolean.FALSE.equals(e.getInvalidStatus()))
                        .filter(v -> v.getSkuNo().equals(dto.getSkuNo()))
                        .mapToInt(SoOutstockDetailEntity::getActualQty)
                        .sum();
                if (sellQty < actualQty + Optional.ofNullable(dto.getPlanQty()).orElse(0)) {
                    throw new ServiceException(ApiError.ERROR_99103, dto.getSkuNo());
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
                        throw new ServiceException(ApiError.ERROR_92027);
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
                        throw new ServiceException(ApiError.ERROR_92028);
                    }
                    if (ObjectUtil.isEmpty(batchNo)) {
                        //即时库存
                        Integer inventory = skuInventoryList.stream().filter(s -> s.getSkuId().equals(skuId) && s.getWarehouseLocationId().
                                equals(warehouseLocation)).findFirst().flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);
                        if (planQty > inventory) {
                            throw new ServiceException(ApiError.ERROR_92030);
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
            throw new ServiceException(ApiError.ERROR_92029);
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
            return new ArrayList<>();
        }
        List<String> newSoIds = soIds.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(newSoIds)) {
            return new ArrayList<>();
        }
		List<SoOutstockDetailEntity> resultList = baseMapper.listDetailBySoIds(newSoIds);
        return resultList;
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
            throw new ServiceException(ApiError.ERROR_99002);
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
                        throw new ServiceException(ApiError.ERROR_92028);
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
                        throw new ServiceException(ApiError.ERROR_92030);
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
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST_WAREHOUSE);
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
                            throw new ServiceException(ApiError.SKU_MAPPING_INVENTORY_INSUFFICIENT, currentSkuMappingDTO.getProductSkuNo());
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
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<String> soDetailIdList = detailList.stream().map(SoOutstockDetailEntity::getSoDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailList = soInfoFeign.listSoDetailByIds(soDetailIdList);

        //销售订单主表信息
        List<String> soIdList = soDetailList.stream().map(SoDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<SoInfoEntity> soInfoList = soInfoFeign.listSoInfoByIds(soIdList);

        //查询销售订单下的销售出库单
        List<SoOutstockDetailEntity> soOutstockDetailList = this.listBySoDetailIds(soDetailIdList);

        for (SoOutstockDetailEntity detailEntity : detailList) {
            //销售订单明细
            SoDetailEntity soDetailEntity = soDetailList.stream().filter(obj -> obj.getId().equals(detailEntity.getSoDetailId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(soDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_92015);
            }
            SoInfoEntity soInfoEntity = soInfoList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), soDetailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soInfoEntity)) {
                throw new ServiceException(ApiError.ERROR_92016);
            }
            //虚拟仓信息
            detailEntity.setVirtualWarehouseId(soInfoEntity.getVirtualWarehouseId());

            //单价信息
            detailEntity.setPrice(soDetailEntity.getPrice());
            detailEntity.setTaxRate(soDetailEntity.getTaxRate());
            detailEntity.setExchangeRate(soDetailEntity.getExchangeRate());
            detailEntity.setAmount(MathUtil.multiplyWithTwo(soDetailEntity.getPrice(), detailEntity.getActualQty()));
            detailEntity.setCurrency(soDetailEntity.getCurrency());
            detailEntity.setCurrencySymbol(soDetailEntity.getCurrencySymbol());
            //销售订单明细已下推的销售出库单
            /**
             *  单SKU价税合计(本位币)=SKU的价税合计(本位币)*(出库数量/销售订单数量)
             *  最后一笔价税合计(本位币)=总价税合计(本位币)-价税合计SKU累计(本位币)
             */
            List<SoOutstockDetailEntity> soOutStockDetailList = soOutstockDetailList.stream().filter(obj -> obj.getSoDetailId().equals(soDetailEntity.getId())).collect(Collectors.toList());
            BigDecimal allAmountLocalCurrency = MathUtil.multiplyWithTwo(soDetailEntity.getAllAmountLocalCurrency(), MathUtil.divide(new BigDecimal(detailEntity.getActualQty()), new BigDecimal(soDetailEntity.getQty())));
            if (CollectionUtils.isNotEmpty(soOutStockDetailList)) {
                Integer totalActualQty = soOutStockDetailList.stream().map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
                if (MathUtil.compareTo(totalActualQty + detailEntity.getActualQty(), soDetailEntity.getAllAmountLocalCurrency()) == MathUtil.ZERO) {
                    BigDecimal totalAllAmount = soOutStockDetailList.stream().map(SoOutstockDetailEntity::getAllAmountLocalCurrency).reduce(BigDecimal.ZERO, BigDecimal::add);
                    allAmountLocalCurrency = MathUtil.subtract(soDetailEntity.getAllAmountLocalCurrency(), totalAllAmount);
                }
            }
            detailEntity.setAllAmountLocalCurrency(allAmountLocalCurrency);
            detailEntity.setRemark(soDetailEntity.getRemark());
        }
    }


    /**
     * 处理b2c 销售订单明细信息
     *
     * @param detailList
     */
    private void handleB2cDetailData(List<SoOutstockDetailEntity> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<String> soDetailIdList = detailList.stream().map(SoOutstockDetailEntity::getSoDetailId).collect(Collectors.toList());
        List<SoB2cDetailEntity> soDetailList = soB2cFeign.listDetailByIds(soDetailIdList);
        List<String> currencyIdList = soDetailList.stream().map(SoB2cDetailEntity::getCurrency).collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyList = CollectionUtils.isNotEmpty(currencyIdList) ? sysUserFeign.listByCurrency(currencyIdList) : Collections.emptyList();
        for (SoOutstockDetailEntity detailEntity : detailList) {
            //销售订单明细
            SoB2cDetailEntity soDetailEntity = soDetailList.stream().filter(obj -> obj.getId().equals(detailEntity.getSoDetailId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(soDetailEntity)) {
                continue;
            }
            //虚拟仓库
            if(StringUtils.isBlank(detailEntity.getVirtualWarehouseId())){
                detailEntity.setVirtualWarehouseId(soDetailEntity.getVirtualWarehouseId());
            }

            BigDecimal price=soDetailEntity.getPrice();
            //单价信息
            detailEntity.setPrice(price);
            BigDecimal exchangeRate=soDetailEntity.getExchangeRate();
            detailEntity.setExchangeRate(exchangeRate);
            BigDecimal amount=MathUtil.multiplyWithTwo(price, detailEntity.getActualQty());
            detailEntity.setAmount(amount);
            String currency = soDetailEntity.getCurrency();
            detailEntity.setCurrency(currency);
            String symbol = currencyList.stream().filter(c -> c.getId().equals(currency)).findFirst().map(CurrencyDTO.ViewDTO::getSymbol).orElse("");
            detailEntity.setCurrencySymbol(symbol);
            BigDecimal amountLocalCurrency=amount;
            if(BigDecimal.ZERO.compareTo(exchangeRate)!=0){
                amountLocalCurrency=MathUtil.multiplyWithTwo(amount,exchangeRate,4);
            }
            detailEntity.setAllAmountLocalCurrency(amountLocalCurrency);
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
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateSoOutPrice(List<SoDetailEntity> soDetailEntityList) {
        soDetailEntityList = soDetailEntityList.stream().filter(v->CharSequenceUtil.isNotBlank(v.getId())).collect(Collectors.toList());
        List<String> soDetailIds = soDetailEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(soDetailIds)){
            return true;
        }
        List<SoOutstockDetailEntity> soOutstockDetailEntityList = this.lambdaQuery().in(SoOutstockDetailEntity::getSoDetailId,soDetailIds).list();
        List<SoOutstockDetailEntity> updateList = new ArrayList<>();
        for (SoOutstockDetailEntity detailEntity : soOutstockDetailEntityList) {
            //销售订单明细
            SoDetailEntity soDetailEntity = soDetailEntityList.stream().filter(obj -> obj.getId().equals(detailEntity.getSoDetailId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(soDetailEntity)) {
                continue;
            }
            BigDecimal price=soDetailEntity.getPrice();
            //单价信息
            detailEntity.setPrice(price);
            BigDecimal exchangeRate=soDetailEntity.getExchangeRate();
            BigDecimal amount=MathUtil.multiplyWithTwo(price, detailEntity.getActualQty());
            detailEntity.setAmount(amount);
            BigDecimal amountLocalCurrency=amount;
            if(Objects.nonNull(exchangeRate) && BigDecimal.ZERO.compareTo(exchangeRate)!=0){
                amountLocalCurrency=MathUtil.multiplyWithTwo(amount,exchangeRate,4);
            }
            detailEntity.setAllAmountLocalCurrency(amountLocalCurrency);
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
}
