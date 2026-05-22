package com.erp.server.dmp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.CfgApiAuthDTO;
import com.erp.model.dmp.entity.CfgApiAuthEntity;
import com.erp.model.dmp.gyy.GyyDeliveryDetailEntity;
import com.erp.server.dmp.mapper.CfgApiAuthMapper;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.CfgApiAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @author Will
 * @version 1.0

 * @date 2023/1/11 11:38
 */
@Slf4j
@Service
public class CfgApiAuthServiceImpl extends ServiceImpl<CfgApiAuthMapper, CfgApiAuthEntity> implements CfgApiAuthService {

    @Resource
    private MongoService mongoService;

    @Override
    public Boolean insert(CfgApiAuthDTO.ParamDTO dto) {
        //验证数据是否重复
        checkCfgApiAuth(dto);
        CfgApiAuthEntity entity = new CfgApiAuthEntity();
        BeanMapperUtils.copy(dto,entity);
        LocalDateTime now = LocalDateTime.now();
        LoginUser loginUser = UserContext.getNonLoginUser();
        String userId = loginUser.getUid();
        String userName = loginUser.getUserName();
        entity.setUpdateTime(now);
        entity.setUpdateUserId(userId);
        entity.setUpdateUserName(userName);
        entity.setCreateTime(now);
        entity.setCreateUserId(userId);
        entity.setCreateUserName(userName);
        return this.save(entity);
    }

    @Override
    public void update(CfgApiAuthDTO.ParamDTO dto) {
        //验证数据是否重复
        checkCfgApiAuth(dto);
        CfgApiAuthEntity entity = new CfgApiAuthEntity();
        BeanMapperUtils.copy(dto,entity);
        LoginUser loginUser = UserContext.getNonLoginUser();
        String userId = loginUser.getUid();
        String userName = loginUser.getUserName();
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateUserId(userId);
        entity.setUpdateUserName(userName);
        this.updateById(entity);
    }

    @Override
    public CfgApiAuthEntity getByKey (String key ,String apiGroup ,String apiPlatformId) {
      return   lambdaQuery()
              .eq(StrUtil.isNotBlank(apiPlatformId),CfgApiAuthEntity::getApiPlatformId,apiPlatformId)
              .eq(StrUtil.isNotBlank(apiGroup),CfgApiAuthEntity::getApiGroup,apiGroup)
              .eq(CfgApiAuthEntity::getKey,key)
              .last("limit 1")
              .one();
    }

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
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
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMongoTest(String type, String id){
        CfgApiAuthEntity cfgApiAuthEntity = new CfgApiAuthEntity();
        cfgApiAuthEntity.setApiPlatform(type);
        cfgApiAuthEntity.setApiPlatformId(type);
        CfgApiAuthEntity cfgApiAuthEntity2 = new CfgApiAuthEntity();
        cfgApiAuthEntity2.setApiPlatform(type);
        cfgApiAuthEntity2.setApiPlatformId(type);
        cfgApiAuthEntity2.setKey(type);
        cfgApiAuthEntity2.setValue(type);
        if(Integer.valueOf(type) > 100 ) {
            // 保存mysql数据
            save(cfgApiAuthEntity);
            save(cfgApiAuthEntity2);
            log.info("pgSQL_test_transactional_up");
//            Integer s  = 5/0;
        }
    }

    /**
     * 验证数据是否重复
     */
    private void checkCfgApiAuth(CfgApiAuthDTO.ParamDTO dto) {
        CfgApiAuthEntity entity = getByKey(dto.getKey(), dto.getApiGroup(), dto.getApiPlatformId());
        if (ObjectUtils.isNotEmpty(entity) && !entity.getId().equals(dto.getId())) {
            throw new ServiceException(ApiError.MAPPING_EN_DESC_DUPLICATE);
        }
    };
}
