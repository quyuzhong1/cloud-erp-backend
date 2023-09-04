package com.erp.server.dmp.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.server.dmp.mapper.CfgAppClientMapper;
import com.erp.server.dmp.service.CfgAppClientService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 * 第三方应用程序信息表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Slf4j
@Service
public class CfgAppClientServiceImpl extends SuperServiceImpl<CfgAppClientMapper, CfgAppClientEntity> implements CfgAppClientService {



    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(CfgAppClientDTO.AddDTO addDTO) {
        CfgAppClientEntity cfgAppClientEntity = new CfgAppClientEntity();
        BeanMapperUtils.copy(addDTO, cfgAppClientEntity);
        // 数据处理
        handleData(cfgAppClientEntity);
        log.info("开始新增第三方应用程序信息单");
        boolean save = super.save(cfgAppClientEntity);
        if(!save) {
            throw new ServiceException("第三方应用程序信息单保存失败");
        }
        return cfgAppClientEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgAppClientDTO.UpdateDTO updateDTO) {
        CfgAppClientEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "第三方应用程序信息单"));
        CfgAppClientEntity cfgAppClientEntity =  BeanMapperUtils.map(CfgAppClientEntity.class, updateDTO);

        // 数据处理
        handleData(cfgAppClientEntity);
        log.info("编辑 开始修改第三方应用程序信息单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgAppClientEntity);
        if(!save) {
            throw new ServiceException("第三方应用程序信息单保存失败");
        }
        return Boolean.TRUE;
    }

    private void handleData(CfgAppClientEntity cfgAppClientEntity) {
    }


    /**
     * 获取根据信息 获取到配置信息
     * @author yl
     * @date 2023-08-29 10:43
     * @param dto
     * @return com.erp.model.dmp.entity.CfgAppClientEntity
     */
    @Override
    public CfgAppClientEntity getCfgAppClient(CfgAppClientDTO.FindDTO dto) {
        return this.lambdaQuery().eq(CfgAppClientEntity::getBusinessType,dto.getBusinessType()).
                eq(CfgAppClientEntity::getDictPlatform,dto.getDictPlatform()).
                eq(CfgAppClientEntity::getPlatformType,dto.getPlatformType()).
                last("LIMIT 1").one();
    }


 
}
