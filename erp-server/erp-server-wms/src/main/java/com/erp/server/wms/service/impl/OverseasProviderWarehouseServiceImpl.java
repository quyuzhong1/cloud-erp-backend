package com.erp.server.wms.service.impl;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.server.wms.mapper.OverseasProviderWarehouseMapper;
import com.erp.server.wms.service.OverseasProviderWarehouseService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.erp.server.wms.service.WarehouseService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 海外物流商仓库 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class OverseasProviderWarehouseServiceImpl extends SuperServiceImpl<OverseasProviderWarehouseMapper, OverseasProviderWarehouseEntity> implements OverseasProviderWarehouseService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private WarehouseService warehouseService;

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(OverseasProviderDTO.UpdateDTO updateDTO, String mainId) {
        List<OverseasProviderWarehouseDTO.UpdateDTO> detailList = updateDTO.getDetailList();
        //映射字段
        List<OverseasProviderWarehouseEntity> list = BeanMapperUtils.copyList(OverseasProviderWarehouseEntity.class, detailList);
        // 数据处理
        handleData(list, mainId);
        boolean save = this.updateBatchById(list);
        if(!save) {
            throw new ServiceException("海外物流商仓库保存失败");
        }

        return Boolean.TRUE;
    }

    @Override
    public OverseasProviderWarehouseEntity getByWarehouseId(String warehouseId) {
        return lambdaQuery()
                .eq(OverseasProviderWarehouseEntity::getWarehouseId, warehouseId)
                .last("LIMIT 1")
                .one();
    }

    @Override
    public List<OverseasProviderWarehouseEntity> listByWarehouseIds(List<String> warehouseIds) {
        if (CollectionUtils.isEmpty(warehouseIds)) {
            return Collections.emptyList();
        }

        return lambdaQuery()
                .in(OverseasProviderWarehouseEntity::getWarehouseId, warehouseIds)
                .list();
    }

    @Override
    public OverseasProviderWarehouseEntity getByPlatform(String mainId,String platformWarehouseCode) {
        return lambdaQuery()
                .eq(OverseasProviderWarehouseEntity::getMainId, mainId)
                .eq(OverseasProviderWarehouseEntity::getPlatformWarehouseCode, platformWarehouseCode)
                .last("LIMIT 1")
                .one();
    }

    @Override
    public List<OverseasProviderWarehouseEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery().in(OverseasProviderWarehouseEntity::getMainId, mainIds).list();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<OverseasProviderWarehouseEntity> list, String mainId) {

        List<OverseasProviderWarehouseEntity> oldList = this.listByMainIds(Arrays.asList(mainId));

        List<String> warehouseIds = list.stream().map(req -> req.getWarehouseId()).distinct().collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseDtoList = warehouseService.listWarehouseByIds(warehouseIds);
        for (OverseasProviderWarehouseEntity detailEntity : list) {
            //如果启用，校验仓库是否绑定
            if (!detailEntity.getDisabled()) {
                if (StringUtils.isBlank(detailEntity.getWarehouseId())) {
                    throw new ServiceException(ApiError.ERROR_NOT_WAREHOUSE);
                }
            }

            detailEntity.setMainId(mainId);
            WarehouseDTO.UpdateDTO updateDTO = warehouseDtoList.stream().filter(req -> req.getId().equals(detailEntity.getWarehouseId())).distinct().findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(updateDTO)) {
                detailEntity.setWarehouseCode(updateDTO.getKingdeeWarehouseCode());
                detailEntity.setWarehouseName(updateDTO.getName());
            }
            //校验是否是修改，如果是就新增修改日志
            if (StringUtils.isNotBlank(detailEntity.getId())) {
                OverseasProviderWarehouseEntity old = oldList.stream().filter(obj -> obj.getId().equals(detailEntity.getId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_NOT_FBA_DELIVERY_DETAIL);
                }
                operateLogService.addModuleOperateLogByObj(old,detailEntity, ModuleTypeEnum.FBA_DELIVERY.getCode(),mainId,"",String.format("【%s】",old.getWarehouseName()));
            }
        }

    }
}
