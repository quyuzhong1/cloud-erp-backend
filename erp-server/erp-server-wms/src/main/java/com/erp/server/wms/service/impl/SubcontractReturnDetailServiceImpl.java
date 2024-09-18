package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.SubcontractReturnTypeEnum;
import com.erp.model.wms.enums.SubcontractReturnTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.server.wms.mapper.SubcontractReturnDetailMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SubcontractReturnDetailDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 委外退料明细单 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-09-15
 */
@Slf4j
@Service
public class SubcontractReturnDetailServiceImpl extends SuperServiceImpl<SubcontractReturnDetailMapper, SubcontractReturnDetailEntity> implements SubcontractReturnDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Resource
    private SubcontractReturnService subcontractReturnService;
    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private ScmTaskFeign scmTaskFeign;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

//    @GlobalTransactional(rollbackFor = Exception.class)
//    @Transactional(rollbackFor = Exception.class)
//    @Override
//    public BaseResultDTO.AddDTO add(SubcontractReturnDetailDTO.AddDTO addDTO) {
//        SubcontractReturnDetailEntity subcontractReturnDetailEntity = new SubcontractReturnDetailEntity();
//        BeanMapperUtils.copy(addDTO, subcontractReturnDetailEntity);
//
//        // 数据处理
//        handleData(subcontractReturnDetailEntity);
//
//        log.info("开始新增委外退料明细单");
//        boolean save = super.save(subcontractReturnDetailEntity);
//        if(!save) {
//            throw new ServiceException("委外退料明细单保存失败");
//        }
//
//        // 操作日志
//        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "委外退料明细单" , subcontractReturnDetailEntity.getId());
//        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLog(msg, null, subcontractReturnDetailEntity.getId(), "新增操作");
//        // TODO 新增明细（如果有明细的话）
//
//        return new BaseResultDTO.AddDTO(subcontractReturnDetailEntity.getId(), subcontractReturnDetailEntity.getId());
//    }
//
//    /**
//    * 修改
//    */
//    @Transactional(rollbackFor = Exception.class)
//    @Override
//    public Boolean update(SubcontractReturnDetailDTO.UpdateDTO updateDTO) {
//        SubcontractReturnDetailEntity old = super.getById(updateDTO.getId());
//        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "委外退料明细单"));
//        SubcontractReturnDetailEntity subcontractReturnDetailEntity =  BeanMapperUtils.map(SubcontractReturnDetailEntity.class, updateDTO);
//
//        // 数据处理
//        handleData(subcontractReturnDetailEntity);
//        log.info("编辑 开始修改委外退料明细单数据，id：【{}】", old.getId());
//        boolean save = super.updateById(subcontractReturnDetailEntity);
//        if(!save) {
//            throw new ServiceException("委外退料明细单保存失败");
//        }
//        // TODO 修改明细数据（包含增删改）（如果有明细的话）
//
//        // 记录主单操作日志
//            log.info("编辑 开始记录委外退料明细单日志数据，id：【{}】", subcontractReturnDetailEntity.getId());
//            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), subcontractReturnDetailEntity.getId(), "委外退料明细单");
//        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLogByObj(old, subcontractReturnDetailEntity, null, subcontractReturnDetailEntity.getId(), msg);
//        return Boolean.TRUE;
//    }

    @Override
    public void add(List<SubcontractReturnDetailDTO.AddDTO> details, String mainId) {
        if (CollectionUtil.isEmpty(details)) {
            throw new ServiceException(ApiError.ERROR_1041, SourceTypeEnum.SUBCONTRACT_RETURN.getName());
        }
        List<SubcontractReturnDetailEntity> list = BeanMapperUtils.copyList(SubcontractReturnDetailEntity.class, details);

        //委外退料主表信息
        SubcontractReturnEntity subcontractReturnEntity = subcontractReturnService.getById(mainId);
        if (ObjectUtil.isEmpty(subcontractReturnEntity)) {
            throw new ServiceException(ApiError.ERROR_SUBCONTRACT_RETURN_NOT_EXIST);
        }

        // 数据处理
        handleData(list,subcontractReturnEntity);
        //数据验证
        checkData(list,subcontractReturnEntity);

        log.info("开始新增委外退料明细单");
        boolean save = super.saveBatch(list);
        if(!save) {
            throw new ServiceException("委外退料明细单保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "委外退料明细单" , mainId);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUBCONTRACT_RETURN.getCode(), mainId, "新增操作");
    }

    @Override
    public Boolean update(List<SubcontractReturnDetailDTO.UpdateDTO> details, String mainId) {
        if (CollectionUtil.isEmpty(details)) {
            throw new ServiceException(ApiError.ERROR_1041, SourceTypeEnum.SUBCONTRACT_RETURN.getName());
        }
        List<SubcontractReturnDetailEntity> list = BeanMapperUtils.copyList(SubcontractReturnDetailEntity.class, details);

        //原明细数据
        List<SubcontractReturnDetailEntity> oldList = this.listByMainIds(Arrays.asList(mainId));
        List<String> deleteIds = getDeleteIds(list, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<SubcontractReturnDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.SUBCONTRACT_RETURN.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }

        //委外发料主表信息
        SubcontractReturnEntity subcontractReturnEntity = subcontractReturnService.getById(mainId);
        if (ObjectUtil.isEmpty(subcontractReturnEntity)) {
            throw new ServiceException(ApiError.ERROR_SUBCONTRACT_RETURN_NOT_EXIST);
        }

        // 数据处理
        handleData(list,subcontractReturnEntity);

        //数据验证
        checkData(list,subcontractReturnEntity);

        log.info("编辑 开始修改委外发料明细单数据，id：【{}】", mainId);
        //新增或修改采购订单明细
        boolean save = this.saveOrUpdateBatch(list);

        if(!save) {
            throw new ServiceException("委外发料明细单保存失败");
        }
        return Boolean.TRUE;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<SubcontractReturnDetailEntity> list,SubcontractReturnEntity subcontractReturnEntity) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        //委外明细id集合
        List<String> subcontractDetailIdList = list.stream().map(SubcontractReturnDetailEntity::getSubcontractOrderDetailId).collect(Collectors.toList());
        //委外子SKU明细信息
        List<SubcontractOrderDetailEntity>  childDetailList = scmTaskFeign.listSubcontractDetailByIds(subcontractDetailIdList);

        //委外父级SKU明细信息
        List<String> parentIdList = childDetailList.stream().map(SubcontractOrderDetailEntity::getParentId).collect(Collectors.toList());
        List<SubcontractOrderDetailEntity> parentDetailList = scmTaskFeign.listSubcontractDetailByIds(parentIdList);

        //bom信息
        List<String> parentSkuIdList = parentDetailList.stream().map(SubcontractOrderDetailEntity::getSkuId).collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listHistoryBomChildBySkuIds(parentSkuIdList);

        //仓库信息
        List<String> warehouseIdList = list.stream().map(SubcontractReturnDetailEntity::getWarehouseId).collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIdList);

        for (SubcontractReturnDetailEntity detailEntity : list) {
            //来源明细id默认委外明细id
            if (SourceTypeEnum.SUBCONTRACT_ORDER.getCode().equals(subcontractReturnEntity.getSourceType()) && StrUtil.isBlank(detailEntity.getSourceDetailId())) {
                detailEntity.setSourceDetailId(detailEntity.getSubcontractOrderDetailId());
            }
            //委外子SKU明细信息
            SubcontractOrderDetailEntity  childDetailEntity = childDetailList.stream().filter(obj -> obj.getId().equals(detailEntity.getSubcontractOrderDetailId()))
                    .findFirst().orElse(null);
            if (com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isEmpty(childDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_98072);
            }
            //委外父级SKU明细信息
            SubcontractOrderDetailEntity parentDetailEntity = parentDetailList.stream().filter(obj -> obj.getId().equals(childDetailEntity.getParentId()))
                    .findFirst().orElse(null);
            if (com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isEmpty(parentDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_98071);
            }
            //bom信息
            BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenSkuList.stream().filter(obj -> obj.getParentSkuId().equals(parentDetailEntity.getSkuId()) && obj.getSkuId().equals(childDetailEntity.getSkuId()))
                    .findFirst().orElse(null);
            if (com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isEmpty(bomChildrenSkuDTO)) {
                throw new ServiceException(ApiError.ERROR_95163);
            }
            detailEntity.setParentSkuId(parentDetailEntity.getSkuId());
            detailEntity.setParentSkuNo(parentDetailEntity.getSkuNo());
            detailEntity.setSkuId(childDetailEntity.getSkuId());
            detailEntity.setSkuNo(childDetailEntity.getSkuNo());
            detailEntity.setBomVersion(childDetailEntity.getBomVersion());
            detailEntity.setQuantity(bomChildrenSkuDTO.getQuantity());
            detailEntity.setMainId(subcontractReturnEntity.getId());
            detailEntity.setReturnQty(childDetailEntity.getDeliveryQty());
            detailEntity.setWarehouseLocation(detailEntity.getWarehouseLocation());

            //仓库名称
            String warehouseName = warehouseList.stream().filter(obj -> obj.getId().equals(detailEntity.getWarehouseId())).map(WarehouseEntity::getName)
                    .findFirst().orElse("");
            detailEntity.setWarehouseName(warehouseName);

            //操作日志
            if (StringUtils.isNotBlank(detailEntity.getId())) {
                SubcontractReturnDetailEntity old = this.getById(detailEntity.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_98026);
                }
                operateLogService.addModuleOperateLogByObj(old,detailEntity, ModuleTypeEnum.SUBCONTRACT_RETURN.getCode(),subcontractReturnEntity.getId(),"",String.format("【%s】",old.getSkuNo()));
            }
        }
        //添加操作日志
        List<SubcontractReturnDetailEntity> addList = list.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        //新增不需要添加新增SKU的日志
        if (CollectionUtils.isNotEmpty(addList) && addList.size() != list.size()) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(subcontractReturnEntity.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("新增了一条SKU【%s】", ModuleTypeEnum.SUBCONTRACT_RETURN.getCode(), addPairList, "编辑操作");
        }
    }

    @Override
    public List<SubcontractReturnDetailEntity> listByMainIds(List<String> mainIdList) {
        if (CollectionUtil.isEmpty(mainIdList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(SubcontractReturnDetailEntity::getMainId,mainIdList).list();
    }

    @Override
    public void deleteByMainId(String mainId) {
        lambdaUpdate().eq(SubcontractReturnDetailEntity::getMainId,mainId).remove();
    }

    @Override
    public List<SubcontractReturnDetailEntity> listBySourceDetailIdList(List<String> sourceDetailIdList) {
        if (CollectionUtils.isEmpty(sourceDetailIdList)) {
            return Collections.EMPTY_LIST;
        }
        return baseMapper.listBySourceDetailIdList(sourceDetailIdList);
    }

    @Override
    public List<SubcontractReturnDetailEntity> listBySubcontractOrderDetailIdList(List<String> subcontractOrderDetailIdList) {
        if (CollectionUtils.isEmpty(subcontractOrderDetailIdList)) {
            return Collections.EMPTY_LIST;
        }
        return baseMapper.listBySubcontractOrderDetailIdList(subcontractOrderDetailIdList);
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<SubcontractReturnDetailEntity> newList, List<SubcontractReturnDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SubcontractReturnDetailEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(SubcontractReturnDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * @description: 验证信息
     * @author Will
     * @date: 2024/1/9 17:14
     * @param list
     */
    private void checkData (List<SubcontractReturnDetailEntity> list,SubcontractReturnEntity subcontractReturnEntity) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //委外明细信息
        List<String> subcontractOrderDetailIdList = list.stream().map(SubcontractReturnDetailEntity::getSubcontractOrderDetailId).collect(Collectors.toList());
        List<SubcontractOrderDetailEntity> subcontractOrderDetailList = scmTaskFeign.listSubcontractDetailByIds(subcontractOrderDetailIdList);

        //委外明细已关联的委外发料
        List<SubcontractReturnDetailEntity> subcontractReturnDetailList = this.listBySubcontractOrderDetailIdList(subcontractOrderDetailIdList);

        for (SubcontractReturnDetailEntity entity : list) {
            SubcontractOrderDetailEntity detailEntity = subcontractOrderDetailList.stream().filter(obj -> obj.getId().equals(entity.getSubcontractOrderDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(detailEntity)) {
                throw new ServiceException(ApiError.ERROR_98072);
            }
            //正常领料需要验证发料数量
            if (SubcontractReturnTypeEnum.NORMAL.getCode().equals(subcontractReturnEntity.getType())) {
                //已下推发料数量
                Integer totalReturnQty = subcontractReturnDetailList.stream().filter(obj -> obj.getSubcontractOrderDetailId().equals(entity.getSubcontractOrderDetailId())
                                && SubcontractReturnTypeEnum.NORMAL.getCode().equals(obj.getType()) && !obj.getId().equals(entity.getId()))
                        .map(SubcontractReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                if (MathUtil.add(totalReturnQty,entity.getReturnQty()) > detailEntity.getDeliveryQty()) {
                    throw new ServiceException(ApiError.ERROR_SUBCONTRACT_RETURN_QTY_EXCEED,detailEntity.getSkuNo(),detailEntity.getDeliveryQty() - totalReturnQty);
                }
            }
        }
    }
}
