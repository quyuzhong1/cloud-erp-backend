package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDetailDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeEntity;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.SoDeliveryNoticeDetailMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 发货通知单主表明细表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-05-10
 */
@Slf4j
@Service
public class SoDeliveryNoticeDetailServiceImpl extends SuperServiceImpl<SoDeliveryNoticeDetailMapper, SoDeliveryNoticeDetailEntity> implements SoDeliveryNoticeDetailService {
    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private WmsAttachmentService wmsAttachmentService;

    @Resource
    private OperateLogService operateLogService;


    @Resource
    private SoOutstockDetailService soOutstockDetailService;

    @Autowired
    private SoDeliveryNoticeService soDeliveryNoticeService;

    @Autowired
    private VirtualInventoryService virtualInventoryService;

    @Autowired
    private VirtualWarehouseService virtualWarehouseService;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Autowired
    private DictBasicService dictBasicService;


    @Autowired
    private VirtualInventoryTransCoreService virtualInventoryTransCoreService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(SoDeliveryNoticeDTO.Add dto, String id) {
        List<String> detailIds = dto.getDetailList().stream().map(SoDeliveryNoticeDetailDTO.Add::getSourceDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntitieList = soInfoFeign.listSoDetailByIds(detailIds);
        if (CollectionUtils.isEmpty(soDetailEntitieList)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }
        List<SoDeliveryNoticeDetailEntity> detailEntityList = this.listDetailBySourceDetailIds(detailIds);
        List<SoDeliveryNoticeDetailEntity> list = new ArrayList<>();

        for (SoDeliveryNoticeDetailDTO.Add detailDto : dto.getDetailList()) {
            SoDeliveryNoticeDetailEntity soDeliveryNoticeDetailEntity = new SoDeliveryNoticeDetailEntity();
            SoDetailEntity soDetailEntity = soDetailEntitieList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            Integer deliveryQty = detailEntityList.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoDeliveryNoticeDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);

            if (soDetailEntity.getQty() < detailDto.getDeliveryQty() + deliveryQty) {
                throw new ServiceException(ApiError.ERROR_92010);
            }
            String idStr = IdWorker.getIdStr();
            soDeliveryNoticeDetailEntity.setId(idStr);
            soDeliveryNoticeDetailEntity.setMainId(id);
            soDeliveryNoticeDetailEntity.setSkuId(soDetailEntity.getSkuId());
            soDeliveryNoticeDetailEntity.setSkuNo(soDetailEntity.getSkuNo());
            soDeliveryNoticeDetailEntity.setBomVersion(soDetailEntity.getBomVersion());
            soDeliveryNoticeDetailEntity.setPlatformSkuNo(soDetailEntity.getPlatformSkuNo());
            soDeliveryNoticeDetailEntity.setDeliveryQty(detailDto.getDeliveryQty());
            soDeliveryNoticeDetailEntity.setIsClose(detailDto.getIsClose());
            soDeliveryNoticeDetailEntity.setRemark(detailDto.getRemark());
            soDeliveryNoticeDetailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            soDeliveryNoticeDetailEntity.setLastPickingQty(soDeliveryNoticeDetailEntity.getDeliveryQty());
            Class<SoDeliveryNoticeDetailEntity> detailEntityClass = SoDeliveryNoticeDetailEntity.class;
            TableName tableName = detailEntityClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            //保存附件
            wmsAttachmentService.batchSave(detailDto.getAttachUrlList(), detailDto.getAttachNameList(), type, idStr);

            list.add(soDeliveryNoticeDetailEntity);
        }

        boolean saveBatch = this.saveBatch(list);

        //虚拟仓扣减库存
        handleVirtualInventory(id,list);
        return saveBatch;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SoDeliveryNoticeDTO.Update dto) {
        List<String> addList = dto.getDetailList().stream().filter(c -> StringUtils.isBlank(c.getId())).map(SoDeliveryNoticeDetailDTO.Update::getId).collect(Collectors.toList());
        List<String> detailIds = dto.getDetailList().stream().map(SoDeliveryNoticeDetailDTO.Update::getSourceDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntitieList = soInfoFeign.listSoDetailByIds(detailIds);
        if (CollectionUtils.isEmpty(soDetailEntitieList)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }
        //原明细数据
        List<SoDeliveryNoticeDetailEntity> oldList = this.listDetailByMainId(dto.getId());
        List<String> deleteIds = getDeleteIds(dto.getDetailList(), oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<SoDeliveryNoticeDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "编辑操作");
            this.removeByIds(deleteIds);
        }

        List<SoDeliveryNoticeDetailEntity> list = new ArrayList<>();
        List<SoDeliveryNoticeDetailEntity> detailEntityList = this.listDetailBySourceDetailIds(detailIds);

        // 忽略库存计算SKU
        /**
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = Lists.newArrayList();
        if(CollUtil.isNotEmpty(ignoreInventorySkuList)) {
            ignoreInventorySkuIds = ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
        }
         */

        for (SoDeliveryNoticeDetailDTO.Update detailDto : dto.getDetailList()) {
            SoDeliveryNoticeDetailEntity soDeliveryNoticeDetailEntity = new SoDeliveryNoticeDetailEntity();
            SoDetailEntity soDetailEntity = soDetailEntitieList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            Integer deliveryQty = detailEntityList.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoDeliveryNoticeDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
            if (StringUtils.isNotBlank(detailDto.getId())) {
                soDeliveryNoticeDetailEntity.setId(detailDto.getId());
                deliveryQty = detailEntityList.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId()) && !req.getId().equals(detailDto.getId())).map(SoDeliveryNoticeDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
            } else {
                String idStr = IdWorker.getIdStr();
                soDeliveryNoticeDetailEntity.setId(idStr);
            }

            /**
            if(ignoreInventorySkuIds.contains(soDetailEntity.getSkuId())) {
                log.warn("sku id: {}，sku编号：{}产品属性是费用或服务，不参与库存出入库，不做库存验证", soDetailEntity.getSkuId(), soDetailEntity.getSkuNo());
            } else {
                if (soDetailEntity.getQty() < detailDto.getDeliveryQty() + deliveryQty) {
                    throw new ServiceException(ApiError.ERROR_92010);
                }
            }
             */
            if (soDetailEntity.getQty() < detailDto.getDeliveryQty() + deliveryQty) {
                throw new ServiceException(ApiError.ERROR_92010);
            }

            soDeliveryNoticeDetailEntity.setMainId(dto.getId());
            soDeliveryNoticeDetailEntity.setSkuId(soDetailEntity.getSkuId());
            soDeliveryNoticeDetailEntity.setSkuNo(soDetailEntity.getSkuNo());
            soDeliveryNoticeDetailEntity.setBomVersion(soDetailEntity.getBomVersion());
            soDeliveryNoticeDetailEntity.setPlatformSkuNo(soDetailEntity.getPlatformSkuNo());
            detailDto.setSkuNo(soDetailEntity.getSkuNo());
            soDeliveryNoticeDetailEntity.setDeliveryQty(detailDto.getDeliveryQty());
            soDeliveryNoticeDetailEntity.setIsClose(detailDto.getIsClose());
            soDeliveryNoticeDetailEntity.setRemark(detailDto.getRemark());
            soDeliveryNoticeDetailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            Class<SoDeliveryNoticeDetailEntity> detailEntityClass = SoDeliveryNoticeDetailEntity.class;
            TableName tableName = detailEntityClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            //保存附件
            wmsAttachmentService.batchSaveNotDel(detailDto.getAttachUrlList(), detailDto.getAttachNameList(), type, soDeliveryNoticeDetailEntity.getId());
            //修改操作日志
            if (StringUtils.isNotBlank(detailDto.getId())) {
                SoDeliveryNoticeDetailEntity old = this.getById(soDeliveryNoticeDetailEntity.getId());
                if (ObjectUtils.isNotEmpty(old)) {
                    operateLogService.addModuleOperateLogByObj(old, soDeliveryNoticeDetailEntity, ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), dto.getId(), "", String.format("【%s】", old.getSkuNo()));

                }
            }
            list.add(soDeliveryNoticeDetailEntity);
        }

        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailEntities = this.listByIds(addList);
            List<Pair<String, String>> addPairList = soDeliveryNoticeDetailEntities.stream().map(obj -> new Pair<>(dto.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), addPairList, "编辑操作");
        }
        return this.saveOrUpdateBatch(list);
    }



    /**
     * 处理虚拟库存数据
     * @author will
     * @date 2024/6/14 10:39
     * @param detailList
     */
    @Override
    public void handleVirtualInventory (String id,List<SoDeliveryNoticeDetailEntity> detailList) {
        //发货通知单
        SoDeliveryNoticeEntity soDeliveryNoticeEntity = soDeliveryNoticeService.getById(id);
        if (ObjectUtil.isEmpty(soDeliveryNoticeEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST,"发货通知单");
        }

        //销售明细
        List<String> sourceDetailIdList = detailList.stream().map(SoDeliveryNoticeDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        List<SoDetailEntity> soDetailList = soInfoFeign.listSoDetailByIds(sourceDetailIdList);

        //销售订单信息
        List<String> mainIdList = soDetailList.stream().map(SoDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<SoInfoEntity> soInfoList = soInfoFeign.listSoInfoByIds(mainIdList);

        //销售订单参数
        List<VirtualInventoryStockDTO.OutInStockDTO> soParamList = new ArrayList<>();

        //销售订单参数
        List<VirtualInventoryStockDTO.OutInStockDTO> subParamList = new ArrayList<>();

        //发货通知单参数
        List<VirtualInventoryStockDTO.OutInStockDTO> addParamList = new ArrayList<>();

        //销售订单冻结数量更新
        List<SoDetailDTO.UpdateFrozenQtyDTO> updateList = new ArrayList<>();

        for (SoDeliveryNoticeDetailEntity detailEntity : detailList) {

            //销售明细
            SoDetailEntity soDetailEntity = soDetailList.stream().filter(obj -> StrUtil.equals(obj.getId(), detailEntity.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_92016);
            }

            //销售订单
            SoInfoEntity soInfoEntity = soInfoList.stream().filter(obj -> StrUtil.equals(obj.getId(), soDetailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_92015);
            }

            //无虚拟仓不扣库存
            if (StrUtil.isBlank(soInfoEntity.getVirtualWarehouseId())) {
                continue;
            }
            //销售订单参数
            handleSoParam(soInfoEntity, soDetailEntity, detailEntity,soParamList);

            //销售订单扣减参数
            handleSubSoParam(soInfoEntity, soDetailEntity,detailEntity,subParamList);

            //发货通知单参数
            handleSoDeliveryNoticeAddParam(soDeliveryNoticeEntity, detailEntity,addParamList);

            //更新冻结库存参数
            if (MathUtil.compareTo(soDetailEntity.getFrozenQty(),MathUtil.ZERO) > MathUtil.ZERO) {
                SoDetailDTO.UpdateFrozenQtyDTO updateFrozenQtyDTO = new SoDetailDTO.UpdateFrozenQtyDTO();
                updateFrozenQtyDTO.setDetailId(soDetailEntity.getId());
                boolean isExceed = soDetailEntity.getFrozenQty() > detailEntity.getDeliveryQty();
                if (isExceed) {
                    updateFrozenQtyDTO.setFrozenQty(soDetailEntity.getFrozenQty() - detailEntity.getDeliveryQty());
                } else {
                    updateFrozenQtyDTO.setFrozenQty(MathUtil.ZERO);
                }
                updateList.add(updateFrozenQtyDTO);
            }
        }

        //销售订单扣减库存
        if (CollectionUtils.isNotEmpty(soParamList)) {
            //添加冻结，扣减可用
            VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
            dto.setParamList(soParamList);
            dto.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_INFO_LOCK_ADD.getCode());
            //更新库存
            virtualInventoryTransCoreService.approve(dto);
        }
        //销售订单减冻结
        if (CollectionUtils.isNotEmpty(subParamList)) {
            //减冻结
            VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
            dto.setParamList(subParamList);
            dto.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_INFO_SUBTRACT_FREEZE.getCode());
            //更新库存
            virtualInventoryTransCoreService.approve(dto);
        }
        //发货通知单添加冻结
        if (CollectionUtils.isNotEmpty(addParamList)) {
            //添加冻结
            VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
            dto.setParamList(addParamList);
            dto.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_DELIVERY_NOTICE_ADD.getCode());
            //更新库存
            virtualInventoryTransCoreService.approve(dto);
        }

        //更新销售订单冻结数量
        soInfoFeign.updateFrozenQty(updateList);
    }

    /**
     * 销售订单参数
     * @author will
     * @date 2024/7/16 19:47
     * @param soInfoEntity
     * @param soDetailEntity
     * @param detailEntity
     * @return OutInStockDTO
     */
    private void handleSoParam(SoInfoEntity soInfoEntity,SoDetailEntity soDetailEntity
            ,SoDeliveryNoticeDetailEntity detailEntity,List<VirtualInventoryStockDTO.OutInStockDTO> soParamList) {
        //如果冻结数量大于发货通知数量则无需库存变动
        if (MathUtil.compareTo(soDetailEntity.getFrozenQty(),detailEntity.getDeliveryQty()) >= MathUtil.ZERO) {
            return;
        }

        VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
        outInStockDTO.setSourceType(InventorySourceTypeEnum.SO_INFO);
        outInStockDTO.setSourceId(soInfoEntity.getId());
        outInStockDTO.setSourceCode(soInfoEntity.getCode());
        outInStockDTO.setSourceDetailId(soDetailEntity.getId());
        outInStockDTO.setBillDate(LocalDate.now());
        outInStockDTO.setSkuId(soDetailEntity.getSkuId());
        outInStockDTO.setSkuNo(soDetailEntity.getSkuNo());
        outInStockDTO.setQty(detailEntity.getDeliveryQty() - soDetailEntity.getFrozenQty());
        outInStockDTO.setWarehouseId(soInfoEntity.getWarehouseId());
        outInStockDTO.setVirtualWarehouseId(soInfoEntity.getVirtualWarehouseId());
        soParamList.add(outInStockDTO);
    }

    /**
     * 销售订单扣减参数
     * @author will
     * @date 2024/8/12 15:32
     * @param soInfoEntity
     * @param soDetailEntity
     * @param detailEntity
     * @param paramList
     */
    private void handleSubSoParam(SoInfoEntity soInfoEntity,SoDetailEntity soDetailEntity,
            SoDeliveryNoticeDetailEntity detailEntity,List<VirtualInventoryStockDTO.OutInStockDTO> paramList) {
        VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
        outInStockDTO.setSourceType(InventorySourceTypeEnum.SO_INFO);
        outInStockDTO.setSourceId(soInfoEntity.getId());
        outInStockDTO.setSourceCode(soInfoEntity.getCode());
        outInStockDTO.setSourceDetailId(soDetailEntity.getId());
        outInStockDTO.setBillDate(LocalDate.now());
        outInStockDTO.setSkuId(soDetailEntity.getSkuId());
        outInStockDTO.setSkuNo(soDetailEntity.getSkuNo());
        outInStockDTO.setQty(detailEntity.getDeliveryQty());
        outInStockDTO.setWarehouseId(soInfoEntity.getWarehouseId());
        outInStockDTO.setVirtualWarehouseId(soInfoEntity.getVirtualWarehouseId());
        paramList.add(outInStockDTO);
    }

    /**
     * 发货通知单参数
     * @author will
     * @date 2024/8/12 15:32
     * @param soDeliveryNoticeEntity
     * @param detailEntity
     * @param paramList
     */
    private void handleSoDeliveryNoticeAddParam(SoDeliveryNoticeEntity soDeliveryNoticeEntity
            ,SoDeliveryNoticeDetailEntity detailEntity,List<VirtualInventoryStockDTO.OutInStockDTO> paramList) {

        VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
        outInStockDTO.setSourceType(InventorySourceTypeEnum.SO_DELIVERY_NOTICE);
        outInStockDTO.setSourceId(soDeliveryNoticeEntity.getId());
        outInStockDTO.setSourceCode(soDeliveryNoticeEntity.getCode());
        outInStockDTO.setSourceDetailId(detailEntity.getId());
        outInStockDTO.setBillDate(LocalDate.now());
        outInStockDTO.setSkuId(detailEntity.getSkuId());
        outInStockDTO.setSkuNo(detailEntity.getSkuNo());
        outInStockDTO.setQty(detailEntity.getDeliveryQty());
        outInStockDTO.setWarehouseId(soDeliveryNoticeEntity.getWarehouseId());
        outInStockDTO.setVirtualWarehouseId(soDeliveryNoticeEntity.getVirtualWarehouseId());
        paramList.add(outInStockDTO);
    }


    private List<String> getDeleteIds(List<SoDeliveryNoticeDetailDTO.Update> newList, List<SoDeliveryNoticeDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SoDeliveryNoticeDetailDTO.Update::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(SoDeliveryNoticeDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    @Override
    public Boolean delete(List<String> mainIds) {
        return lambdaUpdate().set(SoDeliveryNoticeDetailEntity::getIsDeleted, Boolean.TRUE)
                .in(SoDeliveryNoticeDetailEntity::getMainId, mainIds)
                .remove();
    }

    @Override
    public List<SoDeliveryNoticeDetailEntity> listDetailByMainId(String mainId) {
        return lambdaQuery().eq(SoDeliveryNoticeDetailEntity::getMainId, mainId).list();
    }

    @Override
    public List<SoDeliveryNoticeDetailEntity> listDetailByMainIds(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(SoDeliveryNoticeDetailEntity::getMainId, mainIdList).list();
    }

    @Override
    public List<SoDeliveryNoticeDetailEntity> listDetailBySourceIds(List<String> sourceIds) {
        return baseMapper.listDetailBySourceIds(sourceIds);
    }

    @Override
    public List<SoDeliveryNoticeDetailEntity> listDetailBySourceDetailIds(List<String> sourceDetailIds) {
        return baseMapper.listDetailBySourceDetailIds(sourceDetailIds);
    }


    /**
     * 根据销售订单详情id 获取对应 下推的数据
     *
     * @param soDetailIds
     * @return java.lang.Integer
     * @author yl
     * @date 2023-05-25 10:30
     */
    @Override
    public Integer getPushDownBySoDetailIds(List<String> soDetailIds) {
        if (CollectionUtils.isEmpty(soDetailIds)) {
            return 0;
        }
        //发货通知的
        Integer deliveryNoticeCount = this.lambdaQuery().in(SoDeliveryNoticeDetailEntity::getSourceDetailId, soDetailIds).count();

        Integer soOutstockCount = soOutstockDetailService.getPushDownCountBySoDetailIds(soDetailIds);
        return deliveryNoticeCount + soOutstockCount;
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
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void closeBySoDetailIds(List<String> soDetailIds) {
        if (CollectionUtils.isNotEmpty(soDetailIds)) {
            this.lambdaUpdate().set(SoDeliveryNoticeDetailEntity::getIsClose, Boolean.TRUE).
                    set(SoDeliveryNoticeDetailEntity::getIsChangeClose, Boolean.TRUE).
                    in(SoDeliveryNoticeDetailEntity::getSourceDetailId, soDetailIds).update();

            soOutstockDetailService.closeBySoDetailIds(soDetailIds);
        }

    }


    /**
     * 根据来源id 获取到对应的明细
     *
     * @param sourceIdList
     * @return java.util.List<com.erp.model.wms.dto.SoDeliveryNoticeDetailDTO.ListDTO>
     * @author yl
     * @date 2023-06-26 10:10
     */
    @Override
    public List<SoDeliveryNoticeDetailDTO.ListDTO> listBySourceIdList(List<String> sourceIdList) {
        if (CollectionUtils.isEmpty(sourceIdList)) {
            return Collections.emptyList();
        }
        String approveStatus= ApproveStatusEnum.APPROVE.getStatus();
        return baseMapper.listBySourceIdList(sourceIdList,approveStatus);
    }

    @Override
    public List<SoDeliveryNoticeDetailEntity> listNoInventoryOrPicking(String id, List<String> noInventorySku) {

        List<SoDeliveryNoticeDetailEntity> noticeDetailEntities = listDetailByMainId(id);
        return noticeDetailEntities.stream().filter(v -> noInventorySku.contains(v.getSkuId()) || v.getPickingQty() > 0).collect(Collectors.toList());
    }


}
