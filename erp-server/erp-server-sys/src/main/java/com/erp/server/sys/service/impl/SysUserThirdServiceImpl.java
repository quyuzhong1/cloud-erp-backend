package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.common.core.utils.BeanMapper;
import com.common.business.interceptor.CommonInterceptor;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.sys.dto.FindUserByThirdDTO;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.common.business.vo.LoginUser;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.model.sys.entity.SysUserThirdEntity;
import com.erp.server.sys.mapper.SysUserThirdMapper;
import com.erp.server.sys.service.SysUserThirdService;
import org.springframework.stereotype.Service;

import java.util.List;


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
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        LambdaQueryWrapper<SysUserThirdEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(SysUserThirdEntity::getThirdPartyType, bindingThird);
        queryWrapper.eq(SysUserThirdEntity::getUserId, loginUser.getUid());
        return baseMapper.delete(queryWrapper) > 0 ? true : false;
    }


    /**
     * 根据第三方绑定消息 获取用户实体
     *
     * @param thirdDTO
     * @return com.erp.model.sys.entity.SysUserInfoEntity
     * @author yl
     * @date 2022-11-14 10:33
     */
    @Override
    public SysUserInfoEntity getUserIdByThird(FindUserByThirdDTO thirdDTO) {
        return baseMapper.getUserIdByThird(thirdDTO);
    }


    /**
     * 根据平台获取对应的用户与Union 关系
     *
     * @param platform
     * @return java.util.List<com.erp.model.sys.vo.ThirdUnionDTO>
     * @author yl
     * @date 2022-11-15 11:03
     */
    @Override
    public List<ThirdUnionDTO> getUnionByPlatform(String platform) {
        LambdaQueryWrapper<SysUserThirdEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(SysUserThirdEntity::getThirdPartyType, platform);
        List<SysUserThirdEntity> list = this.list(queryWrapper);
        List<ThirdUnionDTO> resultList = BeanMapper.copyList(list, ThirdUnionDTO.class);
        return resultList;
    }


    /**
     * 根据用户id 删除绑定关系
     *
     * @param userIds
     * @return void
     * @author yl
     * @date 2022-11-25 11:26
     */
    @Override
    public void deleteByUserIds(List<String> userIds) {
        LambdaQueryWrapper<SysUserThirdEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysUserThirdEntity::getUserId, userIds);
        this.remove(queryWrapper);

    }

    @Override
    public ThirdUnionDTO getUnionByPlatformAndUserId(String platform, String userId) {
        LambdaQueryWrapper<SysUserThirdEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(SysUserThirdEntity::getThirdPartyType, platform);
        queryWrapper.eq(SysUserThirdEntity::getUserId, userId);
        SysUserThirdEntity sysUserThirdEntity =  this.getOne(queryWrapper);
        ThirdUnionDTO thirdUnionDTO = new ThirdUnionDTO();
        BeanMapperUtils.copy(sysUserThirdEntity,thirdUnionDTO);
        return thirdUnionDTO;
    }
}