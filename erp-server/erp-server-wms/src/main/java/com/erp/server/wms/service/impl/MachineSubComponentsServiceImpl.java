package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.MachineSubComponentsDTO;
import com.erp.model.wms.entity.MachineDetailEntity;
import com.erp.model.wms.entity.MachineSubComponentsEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.MachineSubComponentsMapper;
import com.erp.server.wms.service.MachineDetailService;
import com.erp.server.wms.service.MachineSubComponentsService;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.WarehouseService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 加工单子件明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-05-10
 */
@Service
public class MachineSubComponentsServiceImpl extends SuperServiceImpl<MachineSubComponentsMapper, MachineSubComponentsEntity> implements MachineSubComponentsService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private MachineDetailService machineDetailService;

    @Resource
    private WarehouseService warehouseService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(List<MachineSubComponentsDTO.AddDTO> addList, String detailId) {
        if (CollectionUtils.isEmpty(addList)) {
            return;
        }
        List<MachineSubComponentsEntity> list = BeanMapperUtils.copyList(MachineSubComponentsEntity.class, addList);

        //处理明细数据
        doOpHandleDetails(list,detailId,Boolean.FALSE);
        //批量新增
        this.saveBatch(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<MachineSubComponentsDTO.UpdateDTO> updateList, String detailId) {
        if (CollectionUtils.isEmpty(updateList)) {
            return;
        }
        //原明细数据
        List<MachineSubComponentsEntity> oldList = this.listByDetailId(detailId);
        List<String> deleteIds = getDeleteIds(updateList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<MachineSubComponentsEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getDetailId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个子件SKU【%s】", ModuleTypeEnum.TRANSFER_INFO.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        List<MachineSubComponentsEntity> newList = BeanMapperUtils.copyList(MachineSubComponentsEntity.class, updateList);

        //处理明细id及操作日志
        doOpHandleDetails(newList,detailId,Boolean.TRUE);

        //新增或修改明细
        this.saveOrUpdateBatch(newList);
    }

    @Override
    public List<MachineSubComponentsEntity> listByDetailId(String detailId) {
        return lambdaQuery().eq(MachineSubComponentsEntity::getDetailId, detailId).list();
    }

    @Override
    public List<MachineSubComponentsEntity> listByDetailIds(List<String> detailIds) {
        return lambdaQuery().in(MachineSubComponentsEntity::getDetailId, detailIds).list();
    }

    @Override
    public void removeByMainIds(List<String> mainIds) {
        List<MachineDetailEntity> machineDetailList = machineDetailService.listByMainIds(mainIds);
        if (CollectionUtils.isEmpty(machineDetailList)) {
            throw new ServiceException(ApiError.ERROR_99053);
        }
        List<String> detailIds = machineDetailList.stream().map(MachineDetailEntity::getId).collect(Collectors.toList());
        //删除
        lambdaUpdate()
                .in(MachineSubComponentsEntity::getDetailId,detailIds)
                .remove();
    }


    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<MachineSubComponentsDTO.UpdateDTO> newList, List<MachineSubComponentsEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(MachineSubComponentsDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(MachineSubComponentsEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }


    /**
     * 处理明细中的数据id
     */
    private void doOpHandleDetails (List<MachineSubComponentsEntity> newList, String detailId, Boolean isUpdate) {
        //需要新增数据
        List<MachineSubComponentsEntity> addList = newList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());

        //SKU信息
        List<String> skuIds = newList.stream().map(MachineSubComponentsEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        //需要修改的数据
        List<String> ids = newList.stream().filter(obj -> StringUtils.isNotBlank(obj.getId())).map(MachineSubComponentsEntity::getId).collect(Collectors.toList());
        List<MachineSubComponentsEntity> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            list = this.listByIds(ids);
        }

        //仓库信息
        List<String> warehouseIds = newList.stream().map(MachineSubComponentsEntity::getWarehouseId).collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIds);
        if  (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }

        for (MachineSubComponentsEntity detail:newList) {
            //单位
            String unit = skuList.stream().filter(obj -> obj.getSkuId().equals(detail.getSkuId()) && StringUtils.isNotBlank(obj.getUnitName())).map(SkuVO::getUnitName).findFirst().orElse("");
            detail.setUnit(unit);
            //仓库名称
            String warehouseName = warehouseList.stream().filter(obj -> obj.getId().equals(detail.getWarehouseId())).map(WarehouseEntity::getName).findFirst().orElse(null);
            detail.setWarehouseName(warehouseName);

            detail.setDetailId(detailId);

            //修改操作日志
            if (StringUtils.isNotBlank(detail.getId())) {
                if (CollectionUtils.isEmpty(list)) {
                    throw new ServiceException(ApiError.ERROR_99056);
                }
                MachineSubComponentsEntity old = list.stream().filter(obj -> obj.getId().equals(detail.getId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_99056);
                }
                operateLogService.addModuleOperateLogByObj(old,detail, ModuleTypeEnum.MACHINE_INFO.getCode(),detailId,"",String.format("【%s】",old.getSkuNo()));
            }
        }
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList) && isUpdate) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(detailId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个子件SKU【%s】", ModuleTypeEnum.MACHINE_INFO.getCode(), addPairList, "编辑操作");
        }
    }

}
