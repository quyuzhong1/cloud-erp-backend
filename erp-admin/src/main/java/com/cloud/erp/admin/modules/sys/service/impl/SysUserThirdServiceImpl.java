package com.cloud.erp.admin.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.erp.admin.modules.interceptor.SysInterceptor;
import com.cloud.erp.admin.modules.sys.entity.SysUserThirdEntity;
import com.cloud.erp.admin.modules.sys.mapper.SysUserThirdMapper;
import com.cloud.erp.admin.modules.sys.service.SysUserThirdService;
import com.cloud.erp.common.common.token.vo.LoginUser;
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
        queryWrapper.eq(SysUserThirdEntity::getThirdUnionId, unionId);
        return this.getOne(queryWrapper);
    }

    /**
     * 根据用户id获取对应第三方信息
     *
     * @param uid
     * @return com.cloud.erp.admin.modules.sys.entity.SysUserThirdEntity
     * @author yl
     * @date 2022-07-25 14:37
     */

    @Override
    public SysUserThirdEntity findByUserId(String uid) {
        LambdaQueryWrapper<SysUserThirdEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserThirdEntity::getUserId, uid);
        return this.getOne(queryWrapper);
    }

    /**
     * 检查是否绑定
     *
     * @param uid
     * @param flagId
     * @param bindingPlatform
     * @return boolean
     * @author yl
     * @date 2022-07-26 10:54
     */
    @Override
    public boolean checkIfBinding(String uid, String flagId, String bindingPlatform) {
        LambdaQueryWrapper<SysUserThirdEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserThirdEntity::getUserId, uid);
        queryWrapper.eq(SysUserThirdEntity::getThirdUnionId, flagId);
        queryWrapper.eq(SysUserThirdEntity::getThirdPartyType, bindingPlatform);
        return this.getOne(queryWrapper) == null ? false : true;

    }


    /**
     * 根据绑定的类型 解除
     *
     * @param bindingThird
     * @return boolean
     * @author yl
     * @date 2022-07-26 14:20
     */
    @Override
    public boolean removeThirdParty(String bindingThird) {
        LoginUser loginUser = SysInterceptor.threadLocal.get();
        LambdaQueryWrapper<SysUserThirdEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(SysUserThirdEntity::getThirdPartyType,bindingThird);
        queryWrapper.eq(SysUserThirdEntity::getUserId,loginUser.getUid());
        return baseMapper.delete(queryWrapper)>0?true:false;
    }
}