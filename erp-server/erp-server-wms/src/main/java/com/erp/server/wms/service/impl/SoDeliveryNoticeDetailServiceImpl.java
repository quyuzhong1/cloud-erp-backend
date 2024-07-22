package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDetailDTO;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeEntity;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.enums.DictBasicEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.SoDeliveryNoticeDetailMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            soDeliveryNoticeDetailEntity.setDeliveryQty(detailDto.getDeliveryQty());
            soDeliveryNoticeDetailEntity.setIsClose(detailDto.getIsClose());
            soDeliveryNoticeDetailEntity.setRemark(detailDto.getRemark());
            soDeliveryNoticeDetailEntity.setSourceDetailId(detailDto.getSourceDetailId());

            Class<SoDeliveryNoticeDetailEntity> detailEntityClass = SoDeliveryNoticeDetailEntity.class;
            TableName tableName = detailEntityClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            //保存附件
            wmsAttachmentService.batchSave(detailDto.getAttachUrlList(), detailDto.getAttachNameList(), type, idStr);

            list.add(soDeliveryNoticeDetailEntity);
        }
        //验证虚拟仓是否缺货
        checkVirtualInventoryQty(list,id);
        return this.saveBatch(list);
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

        //验证虚拟仓是否缺货
        checkVirtualInventoryQty(list,dto.getId());

        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailEntities = this.listByIds(addList);
            List<Pair<String, String>> addPairList = soDeliveryNoticeDetailEntities.stream().map(obj -> new Pair<>(dto.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), addPairList, "编辑操作");
        }
        return this.saveOrUpdateBatch(list);
    }

    /**
     * 校验虚拟仓库存
     * @author will
     * @date 2024/6/19 15:10
     * @param detailList
     * @param id
     */
    private void checkVirtualInventoryQty (List<SoDeliveryNoticeDetailEntity> detailList ,String id) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        //销售订单
        SoDeliveryNoticeEntity soDeliveryNoticeEntity = soDeliveryNoticeService.getById(id);
        if (ObjectUtil.isEmpty(soDeliveryNoticeEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_DELIVERY_NOTICE_NOT_EXIST);
        }
        List<String> skuIdList = detailList.stream().map(SoDeliveryNoticeDetailEntity::getSkuId).distinct().collect(Collectors.toList());

        //销售套装bom
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(skuIdList);
        List<String> allSkuIdList = bomChildrenSkuList.stream().flatMap(obj -> Stream.of(obj.getSkuId(), obj.getParentSkuId())).collect(Collectors.toList());

        skuIdList.addAll(allSkuIdList);
        //虚拟库存
        VirtualInventoryDTO.VirtualInventoryParamDTO paramDTO = new VirtualInventoryDTO.VirtualInventoryParamDTO();
        paramDTO.setSkuIdList(skuIdList);
        paramDTO.setWarehouseIdList(Arrays.asList(soDeliveryNoticeEntity.getWarehouseId()));
        paramDTO.setVirtualWarehouseIdList(Arrays.asList(soDeliveryNoticeEntity.getVirtualWarehouseId()));
        paramDTO.setDictInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryList = virtualInventoryService.listInventoryQty(paramDTO);

        //虚拟仓库
        VirtualWarehouseEntity virtualWarehouseEntity = virtualWarehouseService.getById(soDeliveryNoticeEntity.getVirtualWarehouseId());
        if (ObjectUtil.isEmpty(virtualWarehouseEntity)) {
           return;
        }

        // 产品属性为费用或服务的sku忽略库存计算
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = CollUtil.isNotEmpty(ignoreInventorySkuList) ?
                ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList()): Lists.newArrayList();

        //销售订单
        List<String> sourceDetailIdList = detailList.stream().map(SoDeliveryNoticeDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        List<SoDetailEntity> soDetailList = CollectionUtils.isEmpty(sourceDetailIdList) ? new ArrayList<>() : FeignQuery.create(SoDetailEntity.class).in(SoDetailEntity::getId, sourceDetailIdList).list();


        Map<String, List<SoDeliveryNoticeDetailEntity>> map = detailList.stream().collect(Collectors.groupingBy(SoDeliveryNoticeDetailEntity::getSkuId));
        for (Map.Entry<String,List<SoDeliveryNoticeDetailEntity>> entry : map.entrySet()) {
            String skuId = entry.getKey();
            if (ignoreInventorySkuIds.contains(skuId)) {
                continue;
            }
            //是否拆分bom
            List<DictBasicDTO.ListDTO> list = dictBasicService.getByKey(DictBasicEnum.VIRTUAL_SPLIT_BOM.getKey());

            //冻结数量
            List<String> thisDetailIdList = entry.getValue().stream().map(SoDeliveryNoticeDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
            Integer frozenQty = soDetailList.stream().filter(obj -> thisDetailIdList.contains(obj.getId())).map(SoDetailEntity::getFrozenQty).reduce(MathUtil.ZERO, Integer::sum);

            Integer deliveryQty = entry.getValue().stream().map(SoDeliveryNoticeDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
            //bom信息
            List<BomChildrenSkuDTO> childList = bomChildrenSkuList.stream().filter(obj -> StrUtil.equals(obj.getParentSkuId(), skuId) && StrUtil.equals(obj.getType(), BomTypeEnum.COMBINATION.getType())).collect(Collectors.toList());
            /**
             * 存在BOM、并且需要拆分
             */
            if (CollectionUtils.isNotEmpty(childList) && CollectionUtil.isNotEmpty(list) && Boolean.valueOf(list.get(0).getValue())) {
                for (BomChildrenSkuDTO childrenSkuDTO : childList) {
                    //虚拟库存
                    Integer virtualInventoryQty = virtualInventoryList.stream().filter(obj -> StrUtil.equals(obj.getSkuId(), childrenSkuDTO.getSkuId()))
                            .map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty).findFirst().orElse(MathUtil.ZERO);

                    //发货数量 > 可用数量 + 冻结数量
                    if (deliveryQty * childrenSkuDTO.getQuantity() > virtualInventoryQty + frozenQty  * childrenSkuDTO.getQuantity()) {
                        throw new ServiceException(StrUtil.format("SKU【{}】，实体仓库【{}】，虚拟仓库【{}】库存不足，可用【{}】，发货【{}】，冻结【{}】",childrenSkuDTO.getSkuNo()
                                ,soDeliveryNoticeEntity.getWarehouseName(),virtualWarehouseEntity.getName(),virtualInventoryQty,deliveryQty,frozenQty));
                    }
                }
            } else {
                //虚拟库存
                Integer virtualInventoryQty = virtualInventoryList.stream().filter(obj -> StrUtil.equals(obj.getSkuId(), skuId))
                        .map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty).findFirst().orElse(MathUtil.ZERO);

                //发货数量 > 可用数量 + 冻结数量
                if (deliveryQty > virtualInventoryQty + frozenQty) {
                    throw new ServiceException(StrUtil.format("SKU【{}】，实体仓库【{}】，虚拟仓库【{}】库存不足，可用【{}】，发货【{}】，冻结【{}】",entry.getValue().get(0).getSkuNo()
                            ,soDeliveryNoticeEntity.getWarehouseName(),virtualWarehouseEntity.getName(),virtualInventoryQty,deliveryQty,frozenQty));
                }
            }
        }
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


}
