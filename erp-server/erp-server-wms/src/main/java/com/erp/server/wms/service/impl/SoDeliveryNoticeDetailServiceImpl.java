package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDetailDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.SoDeliveryNoticeDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.SoDeliveryNoticeDetailService;
import com.erp.server.wms.service.SoOutstockDetailService;
import com.erp.server.wms.service.WmsAttachmentService;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
    private PlmTaskFeign plmTaskFeign;

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

        // 忽略库存计算SKU
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = Lists.newArrayList();
        if(CollUtil.isNotEmpty(ignoreInventorySkuList)) {
            ignoreInventorySkuIds = ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
        }

        for (SoDeliveryNoticeDetailDTO.Add detailDto : dto.getDetailList()) {
            SoDeliveryNoticeDetailEntity soDeliveryNoticeDetailEntity = new SoDeliveryNoticeDetailEntity();
            SoDetailEntity soDetailEntity = soDetailEntitieList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            Integer deliveryQty = detailEntityList.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoDeliveryNoticeDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);

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

        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailEntities = this.listByIds(addList);
            List<Pair<String, String>> addPairList = soDeliveryNoticeDetailEntities.stream().map(obj -> new Pair<>(dto.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), addPairList, "编辑操作");
        }
        return this.saveOrUpdateBatch(list);
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
