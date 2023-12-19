package com.erp.server.wms.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.mapper.SoB2cDeliveryDetailMapper;
import com.erp.server.wms.service.SoB2cDeliveryDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SoB2cDeliveryDetailDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * b2c发货单详情 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
 */
@Slf4j
@Service
public class SoB2cDeliveryDetailServiceImpl extends SuperServiceImpl<SoB2cDeliveryDetailMapper, SoB2cDeliveryDetailEntity> implements SoB2cDeliveryDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private SoB2cFeign soB2cFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(List<SoB2cDeliveryDetailEntity> entities, String mainId) {
        // 数据处理
        handleData(entities, mainId);

        log.info("开始新增b2c发货单详情");
        boolean save = super.saveBatch(entities);
        if(!save) {
            throw new ServiceException("b2c发货单详情保存失败");
        }
    }

    @Override
    public List<SoB2cDeliveryDetailEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(SoB2cDeliveryDetailEntity::getMainId, mainIds).list();
    }

    @Override
    public List<SoB2cDeliveryDetailEntity> listBySoDetailIds(List<String> soDetailIdList) {
        if(CollectionUtils.isEmpty(soDetailIdList)){
         return Collections.emptyList();
        }
        return this.lambdaQuery().in(SoB2cDeliveryDetailEntity::getSourceDetailId, soDetailIdList).list();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<SoB2cDeliveryDetailEntity> entities, String mainId) {
        //查询销售订单详情信息
        List<String> soDetailIds = entities.stream().map(req -> req.getSourceDetailId()).collect(Collectors.toList());
        List<SoB2cDetailEntity> soB2cDetailEntities = soB2cFeign.listDetailByIds(soDetailIds);

        //设置详情字段
        for (SoB2cDeliveryDetailEntity entity : entities) {
            entity.setMainId(mainId);

            //匹配销售单详情，映射仓库字段
            SoB2cDetailEntity soB2cDetailEntity = soB2cDetailEntities.stream().filter(req -> req.getId().equals(entity.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(soB2cDetailEntity)) {
                entity.setWarehouseId(soB2cDetailEntity.getWarehouseId());
                entity.setWarehouseName(soB2cDetailEntity.getWarehouseName());
                entity.setWarehouseLocation(soB2cDetailEntity.getWarehouseLocation());
            }
        }
    }
}
