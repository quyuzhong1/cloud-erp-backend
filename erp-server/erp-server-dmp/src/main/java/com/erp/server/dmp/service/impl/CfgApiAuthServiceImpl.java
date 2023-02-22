package com.erp.server.dmp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.CfgApiAuthDTO;
import com.erp.model.dmp.entity.CfgApiAuthEntity;
import com.erp.model.dmp.gyy.GyyDeliveryDetailEntity;
import com.erp.server.dmp.mapper.CfgApiAuthMapper;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.CfgApiAuthService;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/11 11:38
 */
@Slf4j
@Service
public class CfgApiAuthServiceImpl extends ServiceImpl<CfgApiAuthMapper, CfgApiAuthEntity> implements CfgApiAuthService {

    @Resource
    private MongoService mongoService;

    @Override
    public Boolean insert(CfgApiAuthDTO dto) {
        //验证数据是否重复
        checkCfgApiAuth(dto);
        CfgApiAuthEntity entity = new CfgApiAuthEntity();
        BeanMapperUtils.copy(dto,entity);
        return this.save(entity);
    }

    @Override
    public void update(CfgApiAuthDTO dto) {
        //验证数据是否重复
        checkCfgApiAuth(dto);
        CfgApiAuthEntity entity = new CfgApiAuthEntity();
        BeanMapperUtils.copy(dto,entity);
        this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMongoTest(String type){
        CfgApiAuthEntity cfgApiAuthEntity = new CfgApiAuthEntity();
        cfgApiAuthEntity.setApiPlatform(type);
        cfgApiAuthEntity.setApiPlatformId(type);
        CfgApiAuthEntity cfgApiAuthEntity2 = new CfgApiAuthEntity();
        cfgApiAuthEntity2.setApiPlatform(type);
        cfgApiAuthEntity2.setApiPlatformId(type);
        cfgApiAuthEntity2.setKey(type);
        cfgApiAuthEntity2.setValue(type);
        if (50 > Integer.valueOf(type) && Integer.valueOf(type) > 10 ){
            // 保存mongo数据
            GyyDeliveryDetailEntity entity = new GyyDeliveryDetailEntity();
            entity.set_id(type);
            mongoService.saveMongoData(entity, "mongo_test_transactional_before");
            // 保存mysql数据
            save(cfgApiAuthEntity);
            throw new RuntimeException("test exception ");
        }
        if(Integer.valueOf(type) < 10 ) {
            // 保存mysql数据
            save(cfgApiAuthEntity);

            // 保存mongo数据
            GyyDeliveryDetailEntity entity = new GyyDeliveryDetailEntity();
            entity.set_id(type);
            mongoService.saveMongoData(entity, "mongo_test_transactional_after");
            throw new RuntimeException("test exception ");
        }
        if( 100 >Integer.valueOf(type) && Integer.valueOf(type) > 50 ) {
            // 保存mysql数据
            save(cfgApiAuthEntity);
            save(cfgApiAuthEntity2);

            // 保存mongo数据
            GyyDeliveryDetailEntity entity = new GyyDeliveryDetailEntity();
            entity.set_id(type);
            mongoService.saveMongoData(entity, "mongo_test_transactional_up");
            log.info("mongo_test_transactional_up");
        }
        if(Integer.valueOf(type) > 100 ) {
            // 保存mysql数据
            save(cfgApiAuthEntity);
            save(cfgApiAuthEntity2);
            log.info("mongo_test_transactional_up");
        }
    }

    /**
     * 验证数据是否重复
     */
    private void checkCfgApiAuth(CfgApiAuthDTO dto) {
        CfgApiAuthEntity entity = getByCfgApiAuth(dto);
        if (ObjectUtils.isNotEmpty(entity) && !entity.getId().equals(dto.getId())) {
            throw new ServiceException(ApiError.ERROR_97024);
        }
    };

    private CfgApiAuthEntity getByCfgApiAuth(CfgApiAuthDTO dto){
        LambdaQueryWrapper<CfgApiAuthEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CfgApiAuthEntity::getApiPlatformId,dto.getApiPlatformId());
        queryWrapper.eq(CfgApiAuthEntity::getKey,dto.getKey());
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }

}
