package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.common.business.dto.base.BaseIdDTO;
import com.erp.model.wms.dto.StocktakingPlanDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.StocktakingPlanDetailEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.StocktakingPlanDetailMapper;
import com.erp.server.wms.service.StocktakingPlanDetailService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.wms.service.WarehouseService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveList(List<StocktakingPlanDTO.DetailDTO> detailList, String mainId) {
        if (CollUtil.isEmpty(detailList)) {
            throw new RuntimeException("盘点计划明细不能为空");
        }
        List<BaseIdDTO> orgList = sysUserFeign.listAccountingCompany();
        Map<String, String> orgMap = orgList.stream().collect(Collectors.toMap(BaseIdDTO::getId, BaseIdDTO::getName));
        // 查询仓库与仓库组织信息
        List<StocktakingPlanDetailEntity> insertList = detailList.stream().map(item -> {
            WarehouseDTO.UpdateDTO updateDTO = warehouseService.detailWithCache(item.getWarehouseId());
            String orgName = orgMap.get(updateDTO.getOrgId());
            return new StocktakingPlanDetailEntity(item, mainId,updateDTO, orgName);
        }).collect(Collectors.toList());
        // 批量插入
        this.saveBatch(insertList);
    }
}
