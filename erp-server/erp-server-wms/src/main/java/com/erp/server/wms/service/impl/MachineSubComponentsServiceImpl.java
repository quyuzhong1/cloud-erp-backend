package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.constant.CfgApiAuthContant;
import com.erp.model.dmp.dto.CfgApiAuthDTO;
import com.erp.model.dmp.entity.CfgApiAuthEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.MachineSubComponentsDTO;
import com.erp.model.wms.entity.MachineDetailEntity;
import com.erp.model.wms.entity.MachineInfoEntity;
import com.erp.model.wms.entity.MachineSubComponentsEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.MachineSubComponentsMapper;
import com.erp.server.wms.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
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

    @Resource
    private MachineInfoService machineInfoService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(List<MachineSubComponentsDTO.AddDTO> addList, String detailId,String mainId) {
        if (CollectionUtils.isEmpty(addList)) {
            return;
        }
        List<MachineSubComponentsEntity> list = BeanMapperUtils.copyList(MachineSubComponentsEntity.class, addList);

        //处理明细数据
        doOpHandleDetails(list,detailId,mainId,Boolean.FALSE);
        //批量新增
        this.saveBatch(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<MachineSubComponentsDTO.UpdateDTO> updateList, String detailId,String mainId) {
        if (CollectionUtils.isEmpty(updateList)) {
            return;
        }
        //原明细数据
        List<MachineSubComponentsEntity> oldList = this.listByDetailId(detailId);
        List<String> deleteIds = getDeleteIds(updateList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<MachineSubComponentsEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个子件SKU【%s】", ModuleTypeEnum.TRANSFER_INFO.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        List<MachineSubComponentsEntity> newList = BeanMapperUtils.copyList(MachineSubComponentsEntity.class, updateList);

        //处理明细id及操作日志
        doOpHandleDetails(newList,detailId,mainId,Boolean.TRUE);

        //新增或修改明细
        this.saveOrUpdateBatch(newList);
    }

    @Override
    public List<MachineSubComponentsEntity> listByDetailId(String detailId) {
        return lambdaQuery()
                .eq(MachineSubComponentsEntity::getDetailId, detailId)
                .orderByAsc(MachineSubComponentsEntity::getSkuId)
                .orderByAsc(MachineSubComponentsEntity::getIsChild)
                .list();
    }

    @Override
    public List<MachineSubComponentsEntity> listByDetailIds(List<String> detailIds) {
        return lambdaQuery()
                .in(MachineSubComponentsEntity::getDetailId, detailIds)
                .orderByAsc(MachineSubComponentsEntity::getSkuId)
                .orderByAsc(MachineSubComponentsEntity::getIsChild)
                .list();
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
        List<String> newIds = newList.stream().filter(g -> CharSequenceUtil.isNotBlank(g.getId())).
                map(MachineSubComponentsDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(MachineSubComponentsEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }


    /**
     * 处理明细中的数据id
     */
    private void doOpHandleDetails (List<MachineSubComponentsEntity> newList, String detailId,String mainId, Boolean isUpdate) {
        //需要新增数据
        List<MachineSubComponentsEntity> addList = newList.stream().filter(c -> CharSequenceUtil.isBlank(c.getId())).collect(Collectors.toList());

        //SKU信息
        List<String> skuIds = newList.stream().map(MachineSubComponentsEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        //需要修改的数据
        List<String> ids = newList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getId())).map(MachineSubComponentsEntity::getId).collect(Collectors.toList());
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

        MachineInfoEntity machineInfoEntity = machineInfoService.getById(mainId);
        if (ObjectUtils.isEmpty(machineInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_99052);
        }

        //仓位必填验证
        checkWarehouseLocation(warehouseList,newList);

        for (MachineSubComponentsEntity detail:newList) {
            //单位
            String unit = skuList.stream().filter(obj -> obj.getSkuId().equals(detail.getSkuId()) && CharSequenceUtil.isNotBlank(obj.getUnitName())).map(SkuVO::getUnitName).findFirst().orElse("");
            detail.setUnit(unit);
            //仓库名称
            WarehouseEntity warehouseEntity = warehouseList.stream().filter(obj -> obj.getId().equals(detail.getWarehouseId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(warehouseEntity)) {
                throw new ServiceException(ApiError.ERROR_99002);
            }
            //验证组织是否一致
            if (!StringUtils.equals(machineInfoEntity.getInventoryOrgId(),warehouseEntity.getOrgId())) {
                throw new ServiceException(ApiError.ERROR_MACHINE_WAREHOUSE_ORG_DIFF,detail.getSkuNo(),warehouseEntity.getName(),machineInfoEntity.getInventoryOrgName());
            }
            detail.setWarehouseName(warehouseEntity.getName());

            detail.setDetailId(detailId);

            //修改操作日志
            if (CharSequenceUtil.isNotBlank(detail.getId())) {
                if (CollectionUtils.isEmpty(list)) {
                    throw new ServiceException(ApiError.ERROR_99056);
                }
                MachineSubComponentsEntity old = list.stream().filter(obj -> obj.getId().equals(detail.getId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_99056);
                }
                operateLogService.addModuleOperateLogByObj(old,detail, ModuleTypeEnum.MACHINE_INFO.getCode(),mainId,"",String.format("子件【%s】",old.getSkuNo()));
            }
        }
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList) && isUpdate) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个子件SKU【%s】", ModuleTypeEnum.MACHINE_INFO.getCode(), addPairList, "编辑操作");
        }
    }
    /**
     * @description: 仓位必填验证
     * @author Will
     * @date: 2023/12/19 15:19
     * @param warehouseList
     * @param list
     */
    private void checkWarehouseLocation (List<WarehouseEntity> warehouseList,List<MachineSubComponentsEntity> list) {
        //仓库配置
        CfgApiAuthEntity cfgApiAuthEntity = dmpTaskFeign.getByKey(new CfgApiAuthDTO.FeignDTO(CfgApiAuthContant.WAREHOUSE_LOCATION_VALIDATE));
        List<String> warehouseIdList = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(cfgApiAuthEntity)) {
            CfgApiAuthDTO.WarehouseLocationValidateDTO warehouseLocationValidateDTO = JSONUtil.toBean(cfgApiAuthEntity.getValue(), CfgApiAuthDTO.WarehouseLocationValidateDTO.class);
            warehouseIdList = Arrays.stream(warehouseLocationValidateDTO.getWarehouseIds().split(",")).collect(Collectors.toList());
        }
        for (MachineSubComponentsEntity entity : list) {
            //仓库
            WarehouseEntity inWarehouse = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getWarehouseId())).findFirst().orElse(new WarehouseEntity());
            if (warehouseIdList.contains(inWarehouse.getId()) && CharSequenceUtil.isBlank(entity.getWarehouseLocation())) {
                throw new ServiceException(ApiError.ERROR_WAREHOUSE_LOCATION_NOT_NULL,inWarehouse.getName());
            }
        }
    }


}
