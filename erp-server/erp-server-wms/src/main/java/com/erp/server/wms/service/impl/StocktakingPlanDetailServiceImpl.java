package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseIdDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.StocktakingPlanDTO;
import com.erp.model.wms.dto.StocktakingPlanDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.StocktakingPlanDetailEntity;
import com.erp.model.wms.entity.StocktakingProfitLossDetailEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.StocktakingPlanDetailMapper;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.StocktakingPlanDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.WarehouseService;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 盘点计划明细表 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-08-08
 */
@Slf4j
@Service
public class StocktakingPlanDetailServiceImpl extends SuperServiceImpl<StocktakingPlanDetailMapper, StocktakingPlanDetailEntity> implements StocktakingPlanDetailService {

    @Resource
    private WarehouseService warehouseService;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void saveList(List<StocktakingPlanDTO.DetailDTO> detailList, String mainId) {
        if (CollUtil.isEmpty(detailList)) {
            throw new RuntimeException("盘点计划明细不能为空");
        }
        List<BaseIdDTO> orgList = sysUserFeign.listAccountingCompany();
        Map<String, String> orgMap = orgList.stream().collect(Collectors.toMap(BaseIdDTO::getId, BaseIdDTO::getName));
        // 查询仓库与仓库组织信息
        List<StocktakingPlanDetailEntity> insertList = detailList.stream().map(item -> {
            WarehouseDTO.UpdateDTO updateDTO = warehouseService.detailWithCache(item.getWarehouseId());
            if(ObjectUtil.isNotEmpty(updateDTO) && updateDTO.getDisabled()){
                return null;
            }
            String orgName = orgMap.get(updateDTO.getOrgId());
            return new StocktakingPlanDetailEntity(item, mainId, updateDTO, orgName);
        }).filter(Objects::nonNull).collect(Collectors.toList());
        // 批量插入
        this.saveBatch(insertList);
        //标记SKU
        List<String> skuIds = insertList.stream().map(StocktakingPlanDetailEntity::getSkuId).collect(Collectors.toList());
        plmTaskFeign.updateOccupyStatus(skuIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void updateList(List<StocktakingPlanDTO.DetailDTO> detailList, String mainId) {
        if (CollUtil.isEmpty(detailList)) {
            throw new RuntimeException("盘点计划明细不能为空");
        }
        List<BaseIdDTO> orgList = sysUserFeign.listAccountingCompany();
        Map<String, String> orgMap = orgList.stream().collect(Collectors.toMap(BaseIdDTO::getId, BaseIdDTO::getName));
        // 查询仓库与仓库组织信息
        List<StocktakingPlanDetailEntity> newDetailList = detailList.stream().map(item -> {
            WarehouseDTO.UpdateDTO updateDTO = warehouseService.detailWithCache(item.getWarehouseId());
            String orgName = orgMap.get(updateDTO.getOrgId());
            return new StocktakingPlanDetailEntity(item, mainId,updateDTO, orgName);
        }).collect(Collectors.toList());
        // 根据main ID 查询所有明细
        List<StocktakingPlanDetailEntity> oldDetailList = listByMainId(mainId);
        List<StocktakingPlanDetailEntity> updateList = newDetailList.stream().filter(item -> CharSequenceUtil.isNotBlank(item.getId())).collect(Collectors.toList());
        List<StocktakingPlanDetailEntity> insertList = newDetailList.stream().filter(item -> CharSequenceUtil.isBlank(item.getId())).collect(Collectors.toList());
        List<String> updateIds = updateList.stream().map(StocktakingPlanDetailEntity::getId).collect(Collectors.toList());
        List<StocktakingPlanDetailEntity> removeList = oldDetailList.stream().filter(item -> !updateIds.contains(item.getId())).collect(Collectors.toList());

        // 删除移除的明细数据
        if (CollUtil.isNotEmpty(removeList)){
            List<String> removeIds = removeList.stream().map(StocktakingPlanDetailEntity::getId).collect(Collectors.toList());
            removeByIds(removeIds);
            List<Pair<String, String>> removePairList = removeList.stream().map(obj -> new Pair<>(mainId, CharSequenceUtil.format("仓库名称：{}，库区：{}，仓位：{}，SKU：{}",
                    obj.getWarehouseName(), obj.getWarehouseArea(), obj.getWarehouseLocation(), obj.getSkuNo()))).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除明细数据【%s】", ModuleTypeEnum.STOCKTAKING_PLAN.getCode(), removePairList, "编辑操作");
        }
        // 更新存在的明细数据
        if (CollUtil.isNotEmpty(updateList)){
            if (!updateBatchById(updateList)) {
                throw new RuntimeException("更新盘点计划明细失败");
            }
            for (StocktakingPlanDetailEntity update : updateList) {
                String id = update.getId();
                StocktakingPlanDetailEntity old = updateList.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
                if (old != null) {
                    operateLogService.addModuleOperateLogByObj(old, update, ModuleTypeEnum.STOCKTAKING_PLAN.getCode(), mainId,"编辑操作");
                }
            }
        }
        // 新增不存在的明细数据
        if (CollUtil.isNotEmpty(insertList)){
            saveBatch(insertList);
            List<Pair<String, String>> addPairList = insertList.stream().map(obj -> new Pair<>(mainId,CharSequenceUtil.format("仓库名称：{}，库区：{}，仓位：{}，SKU：{}",
                    obj.getWarehouseName(), obj.getWarehouseArea(), obj.getWarehouseLocation(), obj.getSkuNo()))).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加明细数据【%s】", ModuleTypeEnum.STOCKTAKING_PLAN.getCode(), addPairList, "编辑操作");
        }
        //标记SKU
        List<String> skuIds = detailList.stream().map(StocktakingPlanDTO.DetailDTO::getSkuId).collect(Collectors.toList());
        plmTaskFeign.updateOccupyStatus(skuIds);
    }

    @Override
    public List<StocktakingPlanDetailEntity> listByMainId(String mainId) {
        return lambdaQuery().eq(StocktakingPlanDetailEntity::getMainId, mainId).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeByMainId(String mainId) {
        lambdaUpdate().eq(StocktakingPlanDetailEntity::getMainId, mainId).remove();
        return Boolean.TRUE;
    }

    @Override
    public List<StocktakingPlanDetailDTO.ViewDTO> listByMainIdAndType(String mainId) {
        return baseMapper.listByMainIdAndType(mainId);
    }
}
