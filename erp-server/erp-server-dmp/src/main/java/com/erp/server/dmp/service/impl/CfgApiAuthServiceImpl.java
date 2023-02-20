package com.erp.server.dmp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.CfgApiAuthDTO;
import com.erp.model.dmp.entity.CfgApiAuthEntity;
import com.erp.server.dmp.mapper.CfgApiAuthMapper;
import com.erp.server.dmp.service.CfgApiAuthService;
import org.springframework.stereotype.Service;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/11 11:38
 */
@Service
public class CfgApiAuthServiceImpl extends ServiceImpl<CfgApiAuthMapper, CfgApiAuthEntity> implements CfgApiAuthService {


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
