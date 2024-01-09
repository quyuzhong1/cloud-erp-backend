package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.SubcontractIssueDetailEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.wms.mapper.SubcontractIssueDetailMapper;
import com.erp.server.wms.service.SubcontractIssueDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.erp.server.wms.service.WarehouseService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SubcontractIssueDetailDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 委外发料明细单 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-01-08
 */
@Slf4j
@Service
public class SubcontractIssueDetailServiceImpl extends SuperServiceImpl<SubcontractIssueDetailMapper, SubcontractIssueDetailEntity> implements SubcontractIssueDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private ScmTaskFeign scmTaskFeign;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(List<SubcontractIssueDetailDTO.AddDTO> details, String mainId) {
        if (CollectionUtil.isEmpty(details)) {
            throw new ServiceException(ApiError.ERROR_1041, SourceTypeEnum.SUBCONTRACT_ISSUE.getName());
        }
        List<SubcontractIssueDetailEntity> list = BeanMapperUtils.copyList(SubcontractIssueDetailEntity.class, details);
        // 数据处理
        handleData(list,mainId);

        log.info("开始新增委外发料明细单");
        boolean save = super.saveBatch(list);
        if(!save) {
            throw new ServiceException("委外发料明细单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "委外发料明细单" , mainId);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUBCONTRACT_ISSUE.getCode(), mainId, "新增操作");
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<SubcontractIssueDetailDTO.UpdateDTO> details, String mainId) {
        if (CollectionUtil.isEmpty(details)) {
            throw new ServiceException(ApiError.ERROR_1041, SourceTypeEnum.SUBCONTRACT_ISSUE.getName());
        }

        List<SubcontractIssueDetailEntity> list = BeanMapperUtils.copyList(SubcontractIssueDetailEntity.class, details);

        //原明细数据
        List<SubcontractIssueDetailEntity> oldList = this.listByMainIds(Arrays.asList(mainId));
        List<String> deleteIds = getDeleteIds(list, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<SubcontractIssueDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.SUBCONTRACT_ISSUE.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }

        // 数据处理
        handleData(list,mainId);

        log.info("编辑 开始修改委外发料明细单数据，id：【{}】", mainId);
        //新增或修改采购订单明细
        boolean save = this.saveOrUpdateBatch(list);

        if(!save) {
            throw new ServiceException("委外发料明细单保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public List<SubcontractIssueDetailEntity> listByMainIds(List<String> mainIdList) {
        if (CollectionUtil.isEmpty(mainIdList)) {
            return Collections.EMPTY_LIST;
        }
       return lambdaQuery().in(SubcontractIssueDetailEntity::getMainId,mainIdList).list();
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<SubcontractIssueDetailEntity> newList, List<SubcontractIssueDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SubcontractIssueDetailEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(SubcontractIssueDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<SubcontractIssueDetailEntity> list,String mainId) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        //委外子SKU明细信息
        List<String> sourceDetailIdList = list.stream().map(SubcontractIssueDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<SubcontractOrderDetailEntity> childDetailList = scmTaskFeign.listSubcontractDetailByIds(sourceDetailIdList);

        //委外父级SKU明细信息
        List<String> parentIdList = childDetailList.stream().map(SubcontractOrderDetailEntity::getParentId).collect(Collectors.toList());
        List<SubcontractOrderDetailEntity> parentDetailList = scmTaskFeign.listSubcontractDetailByIds(parentIdList);

        //bom信息
        List<String> parentSkuIdList = parentDetailList.stream().map(SubcontractOrderDetailEntity::getSkuId).collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listHistoryBomChildBySkuIds(parentSkuIdList);

        //仓库信息
        List<String> warehouseIdList = list.stream().map(SubcontractIssueDetailEntity::getWarehouseId).collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIdList);

        for (SubcontractIssueDetailEntity detailEntity : list) {
            //委外子SKU明细信息
            SubcontractOrderDetailEntity childDetailEntity = childDetailList.stream().filter(obj -> obj.getId().equals(detailEntity.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(childDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_98072);
            }

            //委外父级SKU明细信息
            SubcontractOrderDetailEntity parentDetailEntity = parentDetailList.stream().filter(obj -> obj.getId().equals(childDetailEntity.getParentId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(parentDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_98071);
            }
            //bom信息
            BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenSkuList.stream().filter(obj -> obj.getParentSkuId().equals(parentDetailEntity.getSkuId()) && obj.getSkuId().equals(childDetailEntity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(bomChildrenSkuDTO)) {
                throw new ServiceException(ApiError.ERROR_95163);
            }
            detailEntity.setParentSkuId(parentDetailEntity.getSkuId());
            detailEntity.setParentSkuNo(parentDetailEntity.getSkuNo());
            detailEntity.setSkuId(childDetailEntity.getSkuId());
            detailEntity.setSkuNo(childDetailEntity.getSkuNo());
            detailEntity.setBomVersion(childDetailEntity.getBomVersion());
            detailEntity.setQuantity(bomChildrenSkuDTO.getQuantity());
            detailEntity.setMainId(mainId);
            detailEntity.setReceiveQty(childDetailEntity.getDeliveryQty());
            detailEntity.setWarehouseLocation(detailEntity.getWarehouseLocation());

            //仓库名称
            String warehouseName = warehouseList.stream().filter(obj -> obj.getId().equals(detailEntity.getWarehouseId())).map(WarehouseEntity::getName).findFirst().orElse("");
            detailEntity.setWarehouseName(warehouseName);

            //操作日志
            if (StringUtils.isNotBlank(detailEntity.getId())) {
                SubcontractIssueDetailEntity old = this.getById(detailEntity.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_98026);
                }
                operateLogService.addModuleOperateLogByObj(old,detailEntity, ModuleTypeEnum.SUBCONTRACT_ISSUE.getCode(),mainId,"",String.format("【%s】",old.getSkuNo()));
            }
        }
        //添加操作日志
        List<SubcontractIssueDetailEntity> addList = list.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        //新增不需要添加新增SKU的日志
        if (CollectionUtils.isNotEmpty(addList) && addList.size() != list.size()) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("新增了一条SKU【%s】", ModuleTypeEnum.SUBCONTRACT_ISSUE.getCode(), addPairList, "编辑操作");
        }
    }
}
