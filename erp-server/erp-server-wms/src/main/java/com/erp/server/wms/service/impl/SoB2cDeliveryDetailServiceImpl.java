package com.erp.server.wms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.mapper.SoB2cDeliveryDetailMapper;
import com.erp.server.wms.service.SoB2cDeliveryDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
    @Resource
    private SoB2cFeign soB2cFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
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
            entity.setWaitScanQty(entity.getDeliveryQty());
            //匹配销售单详情，映射仓库字段
            SoB2cDetailEntity soB2cDetailEntity = soB2cDetailEntities.stream().filter(req -> req.getId().equals(entity.getSourceDetailId())).findFirst().orElse(null);
            if (Objects.nonNull(soB2cDetailEntity)) {
                if (CharSequenceUtil.isBlank(entity.getWarehouseId())){
                    entity.setWarehouseId(soB2cDetailEntity.getWarehouseId());
                }
                if (CharSequenceUtil.isBlank(entity.getWarehouseName())){
                    entity.setWarehouseName(soB2cDetailEntity.getWarehouseName());
                }
                if (CharSequenceUtil.isBlank(entity.getWarehouseLocation())){
                    entity.setWarehouseLocation(soB2cDetailEntity.getWarehouseLocation());
                }
                if (CharSequenceUtil.isBlank(entity.getVirtualWarehouseId())){
                    //虚拟仓库
                    entity.setVirtualWarehouseId(soB2cDetailEntity.getVirtualWarehouseId());
                }
            }
        }
    }
}
