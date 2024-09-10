package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.SubcontractIssueDetailDTO;
import com.erp.model.wms.entity.SubcontractIssueDetailEntity;
import com.erp.model.wms.entity.SubcontractIssueEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.SubcontractIssueTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.server.wms.mapper.SubcontractIssueDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.SubcontractIssueDetailService;
import com.erp.server.wms.service.SubcontractIssueService;
import com.erp.server.wms.service.WarehouseService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
    private WarehouseService warehouseService;

    @Autowired
    private ScmTaskFeign scmTaskFeign;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Autowired
    private SubcontractIssueService subcontractIssueService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(List<SubcontractIssueDetailDTO.AddDTO> details, String mainId) {
        if (CollectionUtil.isEmpty(details)) {
            throw new ServiceException(ApiError.ERROR_1041, SourceTypeEnum.SUBCONTRACT_ISSUE.getName());
        }
        List<SubcontractIssueDetailEntity> list = BeanMapperUtils.copyList(SubcontractIssueDetailEntity.class, details);

        //委外发料主表信息
        SubcontractIssueEntity subcontractIssueEntity = subcontractIssueService.getById(mainId);
        if (ObjectUtil.isEmpty(subcontractIssueEntity)) {
            throw new ServiceException(ApiError.ERROR_SUBCONTRACT_ISSUE_NOT_EXIST);
        }

        // 数据处理
        handleData(list,subcontractIssueEntity);
        //数据验证
        checkData(list,subcontractIssueEntity);

        log.info("开始新增委外发料明细单");
        boolean save = super.saveBatch(list);
        if(!save) {
            throw new ServiceException("委外发料明细单保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "委外发料明细单" , mainId);
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

        //委外发料主表信息
        SubcontractIssueEntity subcontractIssueEntity = subcontractIssueService.getById(mainId);
        if (ObjectUtil.isEmpty(subcontractIssueEntity)) {
            throw new ServiceException(ApiError.ERROR_SUBCONTRACT_ISSUE_NOT_EXIST);
        }

        // 数据处理
        handleData(list,subcontractIssueEntity);

        //数据验证
        checkData(list,subcontractIssueEntity);

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

    @Override
    public void deleteByMainId(String mainId) {
        lambdaUpdate().eq(SubcontractIssueDetailEntity::getMainId,mainId).remove();
    }

    @Override
    public List<SubcontractIssueDetailEntity> listBySourceDetailIdList(List<String> sourceDetailIdList) {
        if (CollectionUtils.isEmpty(sourceDetailIdList)) {
            return Collections.EMPTY_LIST;
        }
        return baseMapper.listBySourceDetailIdList(sourceDetailIdList);
    }

    @Override
    public List<SubcontractIssueDetailEntity> listBySubcontractOrderDetailIdList(List<String> subcontractOrderDetailIdList) {
        if (CollectionUtils.isEmpty(subcontractOrderDetailIdList)) {
            return Collections.EMPTY_LIST;
        }
        return baseMapper.listBySubcontractOrderDetailIdList(subcontractOrderDetailIdList);
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
    private void handleData(List<SubcontractIssueDetailEntity> list,SubcontractIssueEntity subcontractIssueEntity) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        //委外明细id集合
        List<String> subcontractDetailIdList = list.stream().map(SubcontractIssueDetailEntity::getSubcontractOrderDetailId).collect(Collectors.toList());
        //委外子SKU明细信息
        List<SubcontractOrderDetailEntity>  childDetailList = scmTaskFeign.listSubcontractDetailByIds(subcontractDetailIdList);

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
            //来源明细id默认委外明细id
            if (SourceTypeEnum.SUBCONTRACT_ORDER.getCode().equals(subcontractIssueEntity.getSourceType()) && StrUtil.isBlank(detailEntity.getSourceDetailId())) {
                detailEntity.setSourceDetailId(detailEntity.getSubcontractOrderDetailId());
            }
            //委外子SKU明细信息
            SubcontractOrderDetailEntity  childDetailEntity = childDetailList.stream().filter(obj -> obj.getId().equals(detailEntity.getSubcontractOrderDetailId()))
                        .findFirst().orElse(null);
            if (ObjectUtils.isEmpty(childDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_98072);
            }
            //委外父级SKU明细信息
            SubcontractOrderDetailEntity parentDetailEntity = parentDetailList.stream().filter(obj -> obj.getId().equals(childDetailEntity.getParentId()))
                    .findFirst().orElse(null);
            if (ObjectUtils.isEmpty(parentDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_98071);
            }
            //bom信息
            BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenSkuList.stream().filter(obj -> obj.getParentSkuId().equals(parentDetailEntity.getSkuId()) && obj.getSkuId().equals(childDetailEntity.getSkuId()))
                    .findFirst().orElse(null);
            if (ObjectUtils.isEmpty(bomChildrenSkuDTO)) {
                throw new ServiceException(ApiError.ERROR_95163);
            }
            detailEntity.setParentSkuId(parentDetailEntity.getSkuId());
            detailEntity.setParentSkuNo(parentDetailEntity.getSkuNo());
            detailEntity.setSkuId(childDetailEntity.getSkuId());
            detailEntity.setSkuNo(childDetailEntity.getSkuNo());
            detailEntity.setBomVersion(childDetailEntity.getBomVersion());
            detailEntity.setQuantity(bomChildrenSkuDTO.getQuantity());
            detailEntity.setMainId(subcontractIssueEntity.getId());
            detailEntity.setReceiveQty(childDetailEntity.getDeliveryQty());
            detailEntity.setWarehouseLocation(detailEntity.getWarehouseLocation());

            //仓库名称
            String warehouseName = warehouseList.stream().filter(obj -> obj.getId().equals(detailEntity.getWarehouseId())).map(WarehouseEntity::getName)
                    .findFirst().orElse("");
            detailEntity.setWarehouseName(warehouseName);

            //操作日志
            if (StringUtils.isNotBlank(detailEntity.getId())) {
                SubcontractIssueDetailEntity old = this.getById(detailEntity.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_98026);
                }
                operateLogService.addModuleOperateLogByObj(old,detailEntity, ModuleTypeEnum.SUBCONTRACT_ISSUE.getCode(),subcontractIssueEntity.getId(),"",String.format("【%s】",old.getSkuNo()));
            }
        }
        //添加操作日志
        List<SubcontractIssueDetailEntity> addList = list.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        //新增不需要添加新增SKU的日志
        if (CollectionUtils.isNotEmpty(addList) && addList.size() != list.size()) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(subcontractIssueEntity.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("新增了一条SKU【%s】", ModuleTypeEnum.SUBCONTRACT_ISSUE.getCode(), addPairList, "编辑操作");
        }
    }

    /**
     * @description: 验证信息
     * @author Will
     * @date: 2024/1/9 17:14
     * @param list
     */
    private void checkData (List<SubcontractIssueDetailEntity> list,SubcontractIssueEntity subcontractIssueEntity) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //委外明细信息
        List<String> subcontractOrderDetailIdList = list.stream().map(SubcontractIssueDetailEntity::getSubcontractOrderDetailId).collect(Collectors.toList());
        List<SubcontractOrderDetailEntity> subcontractOrderDetailList = scmTaskFeign.listSubcontractDetailByIds(subcontractOrderDetailIdList);

        //委外明细已关联的委外发料
        List<SubcontractIssueDetailEntity> subcontractIssueDetailList = this.listBySubcontractOrderDetailIdList(subcontractOrderDetailIdList);

        for (SubcontractIssueDetailEntity entity : list) {
            SubcontractOrderDetailEntity detailEntity = subcontractOrderDetailList.stream().filter(obj -> obj.getId().equals(entity.getSubcontractOrderDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(detailEntity)) {
                throw new ServiceException(ApiError.ERROR_98072);
            }
            //正常领料需要验证发料数量
            if (SubcontractIssueTypeEnum.NORMAL.getCode().equals(subcontractIssueEntity.getType())) {
                //已下推发料数量
                Integer totalIssueQty = subcontractIssueDetailList.stream().filter(obj -> obj.getSubcontractOrderDetailId().equals(entity.getSubcontractOrderDetailId())
                                && SubcontractIssueTypeEnum.NORMAL.getCode().equals(obj.getType()) && !obj.getId().equals(entity.getId()))
                        .map(SubcontractIssueDetailEntity::getIssueQty).reduce(MathUtil.ZERO, Integer::sum);
                if (MathUtil.add(totalIssueQty,entity.getIssueQty()) > detailEntity.getDeliveryQty()) {
                    throw new ServiceException(ApiError.ERROR_SUBCONTRACT_ISSUE_QTY_EXCEED,detailEntity.getSkuNo(),detailEntity.getDeliveryQty() - totalIssueQty);
                }
            }
        }
    }
}
