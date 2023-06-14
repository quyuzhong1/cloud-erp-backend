package com.erp.server.dmp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.erp.model.dmp.entity.DmpBomEntity;
import com.erp.model.dmp.entity.DmpSkuInfoEntity;
import com.erp.model.dmp.mabang.ComboSkuInfoEntity;
import com.erp.server.dmp.mapper.DmpBomMapper;
import com.erp.server.dmp.service.DmpBomService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * sku bom关系表 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-06-09
 */
@Slf4j
@Service
public class DmpBomServiceImpl extends SuperServiceImpl<DmpBomMapper, DmpBomEntity> implements DmpBomService {


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkOrder(ComboSkuInfoEntity ext) {
         List<DmpBomEntity> bomEntityList = getBomEntityList(ext);
        if(CollectionUtil.isEmpty(bomEntityList)){
            return;
        }
        List<DmpBomEntity> bomList = lambdaQuery()
                .eq(DmpBomEntity::getParentSku, ext.getComboSku())
                .eq(DmpBomEntity::getPlatformSign, ext.getPlatformSign())
                .eq(DmpBomEntity::getRelationType, ext.getRelationType())
                .list();
        // 已存在数据进行删除
        if (CollectionUtil.isNotEmpty(bomList)) {
            //如果数据有变动需要更新数据库订单信息
            this.removeByIds(bomList.stream().map(DmpBomEntity::getId).collect(Collectors.toList()));
        }
        this.saveBatch(bomEntityList);
    }

    private List<DmpBomEntity> getBomEntityList(ComboSkuInfoEntity ext) {
        if(CollectionUtil.isEmpty(ext.getComboProductDetail())){
            return null;
        }
        DmpBomEntity modelEntity = new DmpBomEntity();
        modelEntity.setParentSku(ext.getComboSku());
        modelEntity.setParentSkuName(ext.getName());
        modelEntity.setPlatformSign(ext.getPlatformSign());
        modelEntity.setRelationType(ext.getRelationType());
        return ext.getComboProductDetail().stream().map(detail -> {
            DmpBomEntity entity = new DmpBomEntity();
            BeanUtil.copyProperties(modelEntity, entity);
            entity.setSkuNo(detail.getStockSku());
            entity.setQty(detail.getQuantity());
            entity.setName(detail.getNameCN());
            return entity;
        }).collect(Collectors.toList());
    }
}
