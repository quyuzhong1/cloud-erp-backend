package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.SoOutstockDetiailDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.WmsAttachmentEntity;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.SoOutstockDetailMapper;
import com.erp.server.wms.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 销售订单出库明细 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class SoOutstockDetailServiceImpl extends SuperServiceImpl<SoOutstockDetailMapper, SoOutstockDetailEntity> implements SoOutstockDetailService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoInfoFeign soInfoFeign;


    @Resource
    private WmsAttachmentService wmsAttachmentService;

    @Resource
    private InventoryService inventoryService;

    @Resource
    private OperateLogService operateLogService;


    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;


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
     * @return void
     * @author yl
     * @date 2023-05-19 10:18
     */
    @Override
    public void add(String mainId, List<SoOutstockDetiailDTO.AddDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<String> skuIdList = detailList.stream().map(SoOutstockDetiailDTO.AddDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        Class<SoOutstockDetailEntity> credentialClass = SoOutstockDetailEntity.class;
        TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
        List<SoOutstockDetailEntity> addList = new ArrayList<>(detailList.size());
        //获取到表名
        String type = tableName.value();
        List<WmsAttachmentEntity> batchAttachmentList = new ArrayList<>(10);

        for (SoOutstockDetiailDTO.AddDTO item : detailList) {
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
        this.saveBatch(addList);
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
    public List<SoOutstockDetiailDTO.ViewDTO> listByMainId(String mainId, String warehouseId) {
        List<SoOutstockDetailEntity> dbList = this.listBaseByMainId(mainId);
        List<SoOutstockDetiailDTO.ViewDTO> resultList = BeanMapper.copyList(dbList, SoOutstockDetiailDTO.ViewDTO.class);
        List<String> skuIdList = resultList.stream().map(SoOutstockDetiailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<String> warehouseLocationList = resultList.stream().map(SoOutstockDetiailDTO.ViewDTO::getWarehouseLocation).collect(Collectors.toList());
        InventoryQtyDTO.SkuInventoryParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryParamDTO();
        skuInventoryDTO.setSkuIdList(skuIdList);
        skuInventoryDTO.setWarehouseIdList(Arrays.asList(warehouseId));
        skuInventoryDTO.setWarehouseLocationIdList(warehouseLocationList);
        skuInventoryDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        //可用库存
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryList = inventoryService.listSkuInventory(skuInventoryDTO);
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        for (SoOutstockDetiailDTO.ViewDTO item : resultList) {
            String skuId = item.getSkuId();
            String warehouseLocation = item.getWarehouseLocation();
            String skuName = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSkuName())).orElse("");
            item.setProductName(skuName);
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
    public void removeByMainIdList(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return;
        }
        LambdaQueryWrapper<SoOutstockDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SoOutstockDetailEntity::getMainId, mainIdList);
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
    public void checkOutQty(String warehouseId, String soId, String sourceId, String sourceType, List<SoOutstockDetiailDTO.UpdateDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_92029);
        }
        //手动新增
        String selfAdd = SourceTypeEnum.SELF_ADD.getCode();
        List<String> sourceDetailIdList = detailList.stream().map(SoOutstockDetiailDTO.AddDTO::getSourceDetailId).collect(Collectors.toList());
        List<String> skuIdList = detailList.stream().map(SoOutstockDetiailDTO.AddDTO::getSkuId).collect(Collectors.toList());
        List<String> warehouseLocationList = detailList.stream().map(SoOutstockDetiailDTO.AddDTO::getWarehouseLocation).collect(Collectors.toList());
        //这个是已出数量
        List<SoOutstockDetailEntity> soOutstockDetailList = this.listDetailBySourceDetailId(sourceDetailIdList);
        //表示新增加
        if (selfAdd.equals(sourceType)) {
            InventoryQtyDTO.SkuInventoryParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryParamDTO();
            skuInventoryDTO.setSkuIdList(skuIdList);
            skuInventoryDTO.setWarehouseIdList(Arrays.asList(warehouseId));
            skuInventoryDTO.setWarehouseLocationIdList(warehouseLocationList);
            skuInventoryDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
            //可用数量
            List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryList = inventoryService.listSkuInventory(skuInventoryDTO);
            //这个是销售订单的
            List<SoDetailEntity> soDetailList = soInfoFeign.listSoDetailByIds(sourceDetailIdList);
            for (SoOutstockDetiailDTO.UpdateDTO item : detailList) {
                String id = item.getId();
                String skuId = item.getSkuId();
                //库位
                String warehouseLocation = item.getWarehouseLocation();
                //实发数量
                Integer actualQty = item.getActualQty();
                //应发数量
                Integer planQty = item.getPlanQty();
                if (actualQty > planQty) {
                    throw new ServiceException(ApiError.ERROR_92027);
                }

                String sourceDetailId = item.getSourceDetailId();
                //这个是销售数量
                Integer soQty = soDetailList.stream().filter(s -> s.getId().equals(sourceDetailId)).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getQty())).orElse(0);

                //这个是已出的数量
                Integer outStockQty = soOutstockDetailList.stream().filter(s ->
                        s.getSourceDetailId().equals(sourceDetailId) &&
                                !s.getId().equals(id)
                ).mapToInt(SoOutstockDetailEntity::getActualQty).sum();
                if (outStockQty + planQty > soQty) {
                    throw new ServiceException(ApiError.ERROR_92028);
                }
                //即时库存
                Integer inventory = skuInventoryList.stream().filter(s -> s.getSkuId().equals(skuId) && s.getWarehouseLocationId().
                        equals(warehouseLocation)).findFirst().flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);
                if (planQty > inventory) {
                    throw new ServiceException(ApiError.ERROR_92030);
                }
            }

        } else {
            //表示是发货通知单的
            List<SoDeliveryNoticeDetailEntity> deliveryNoticeDetailList = soDeliveryNoticeDetailService.listDetailBySourceDetailIds(sourceDetailIdList);
            for (SoOutstockDetiailDTO.UpdateDTO item : detailList) {
                String sourceDetailId = item.getSourceDetailId();
                String id = item.getId();
                Integer deliveryQty = deliveryNoticeDetailList.stream().filter(d -> d.getSourceDetailId().equals(sourceDetailId)).
                        findFirst().flatMap(obj -> Optional.ofNullable(obj.getDeliveryQty())).orElse(0);
                //实发数量
                Integer actualQty = item.getActualQty();
                //应发数量
                Integer planQty = item.getPlanQty();
                if (actualQty > planQty) {
                    throw new ServiceException(ApiError.ERROR_92027);
                }
                if (!planQty.equals(deliveryQty)) {
                    throw new ServiceException(ApiError.ERROR_92031);
                }

                //这个是已出的数量
                Integer outStockQty = soOutstockDetailList.stream().filter(s ->
                        s.getSourceDetailId().equals(sourceDetailId) &&
                                !s.getId().equals(id)
                ).mapToInt(SoOutstockDetailEntity::getActualQty).sum();
                if (outStockQty + planQty > deliveryQty) {
                    throw new ServiceException(ApiError.ERROR_92031);
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
    public void updateDetail(String mainId, List<SoOutstockDetiailDTO.UpdateDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_92029);
        }
        List<SoOutstockDetiailDTO.UpdateDTO> updateList = detailList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());
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


        List<String> skuIdList = detailList.stream().map(SoOutstockDetiailDTO.AddDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        Class<SoOutstockDetailEntity> credentialClass = SoOutstockDetailEntity.class;
        TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
        List<SoOutstockDetailEntity> addOrUpdateList = new ArrayList<>(detailList.size());

        List<SoOutstockDetailEntity> addEntityList = new ArrayList<>(detailList.size());

        List<SoOutstockDetailEntity> updateEntityList = new ArrayList<>(detailList.size());
        //获取到表名
        String type = tableName.value();
        List<WmsAttachmentEntity> batchAttachmentList = new ArrayList<>(10);

        for (SoOutstockDetiailDTO.UpdateDTO item : detailList) {
            String id = item.getId();
            Boolean isAdd = StringUtils.isBlank(id);
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
        this.saveOrUpdateBatch(addOrUpdateList);
        wmsAttachmentService.saveBatch(batchAttachmentList);

        //这是添加
        List<Pair<String, String>> addPairList = addEntityList.stream(). map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("添加了一个销售产品【%s】", ModuleTypeEnum.SO_OUT_STOCK.getCode(), addPairList, "编辑操作");
        for (SoOutstockDetailEntity update : updateEntityList) {
            String id = update.getId();
            SoOutstockDetailEntity old = dbList.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
            if(old!=null){
                operateLogService.addModuleOperateLogByObj(old,update, ModuleTypeEnum.SO_OUT_STOCK.getCode(),mainId,"","");
            }
        }
    }

    private List<String> getDeleteIds(List<Pair<String, String>> pairList, List<SoOutstockDetailEntity> dbList) {
        List<String> ids = pairList.stream().filter(g -> StringUtils.isNotBlank(g.getKey())).
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
        if (StringUtils.isNotBlank(mainId)) {
            return this.lambdaQuery().eq(SoOutstockDetailEntity::getMainId, mainId).list();
        }
        return Collections.emptyList();

    }
}
