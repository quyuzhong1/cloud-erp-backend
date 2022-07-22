package com.cloud.erp.admin.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.erp.admin.modules.sys.entity.SysUserThirdEntity;
import com.cloud.erp.admin.modules.sys.mapper.SysUserThirdMapper;
import com.cloud.erp.admin.modules.sys.service.SysUserThirdService;
import org.springframework.stereotype.Service;


@Service
public class SysUserThirdServiceImpl extends ServiceImpl<SysUserThirdMapper, SysUserThirdEntity> implements SysUserThirdService {


    /**
     * 绑定第三方
     *
     * @param uid
     * @param flagId
     * @return void
     * @author yl
     * @date 2022-07-21 14:06
     */
    @Override
    public void bindingThirdParty(String uid, String flagId, String bindingPlatform) {
        SysUserThirdEntity thirdEntity = new SysUserThirdEntity();
        thirdEntity.setUserId(uid);
        thirdEntity.setThirdUnionId(flagId);
        thirdEntity.setThirdPartyType(bindingPlatform);
        this.save(thirdEntity);
    }


    /**
     * 根据第三方id 获取用户信息
     *
     * @param unionId
     * @return com.cloud.erp.admin.modules.sys.entity.SysUserThirdEntity
     * @author yl
     * @date 2022-07-21 16:20
     */

    @Override
    public SysUserThirdEntity findByUnionId(String unionId) {
        LambdaQueryWrapper<SysUserThirdEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserThirdEntity::getThirdUnionId,unionId);
        return this.getOne(queryWrapper);
    }
}