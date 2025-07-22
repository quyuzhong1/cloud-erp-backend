package com.erp.server.sys.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.FindUserDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.sys.dto.FindUserByThirdDTO;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.model.sys.entity.SysUserThirdEntity;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.server.sys.mapper.SysUserThirdMapper;
import com.erp.server.sys.service.SysUserInfoService;
import com.erp.server.sys.service.SysUserThirdService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class SysUserThirdServiceImpl extends ServiceImpl<SysUserThirdMapper, SysUserThirdEntity> implements SysUserThirdService {

    @Resource
    private SysUserInfoService sysUserInfoService;

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
    public void bindingThirdParty(String uid, String flagId,String thirdOpenId,String thirdUserId, String bindingPlatform) {
        SysUserThirdEntity thirdEntity = new SysUserThirdEntity();
        thirdEntity.setUserId(uid);
        thirdEntity.setThirdUnionId(flagId);
        thirdEntity.setThirdOpenId(thirdOpenId);
        thirdEntity.setThirdUserId(thirdUserId);
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
     * @param bindingPlatform
     * @return boolean
     * @author yl
     * @date 2022-07-26 10:54
     */
    @Override
    public boolean checkIfBinding(String uid, String bindingPlatform) {
        LambdaQueryWrapper<SysUserThirdEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserThirdEntity::getUserId, uid);
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
        LoginUser loginUser = UserContext.getLoginUser();
        LambdaQueryWrapper<SysUserThirdEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(SysUserThirdEntity::getThirdPartyType, bindingThird);
        queryWrapper.eq(SysUserThirdEntity::getUserId, loginUser.getUid());
        baseMapper.delete(queryWrapper);
        return Boolean.TRUE;
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
    public List<ThirdUnionDTO> getUnionByPlatformAndUserIds(String platform, List<String> userIds) {
        LambdaQueryWrapper<SysUserThirdEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(SysUserThirdEntity::getThirdPartyType, platform);
        queryWrapper.in(SysUserThirdEntity::getUserId, userIds);
        List<SysUserThirdEntity> sysUserThirdEntity =  this.list(queryWrapper);
        List<ThirdUnionDTO> thirdUnionDTOs = BeanMapperUtils.copyList(ThirdUnionDTO.class,sysUserThirdEntity);
        if(CollUtil.isEmpty(thirdUnionDTOs)){
            return Collections.emptyList();
        }
        List<FindUserDTO> userList = sysUserInfoService.getUserListByUserIds(thirdUnionDTOs.stream().map(ThirdUnionDTO::getUserId).collect(Collectors.toList()));
        Map<String, String> userMap = userList.stream().collect(Collectors.toMap(FindUserDTO::getUserId, FindUserDTO::getUserName));
        thirdUnionDTOs.forEach(thirdUnionDTO -> {
            thirdUnionDTO.setUserName(userMap.get(thirdUnionDTO.getUserId()));
        });
        return thirdUnionDTOs;
    }

    @Override
    public List<ThirdUnionDTO> getThirdByUserIds(String platform, List<String> userIds) {
        if(StringUtils.isBlank(platform) || CollUtil.isEmpty(userIds)){
            return Collections.emptyList();
        }
        List<SysUserThirdEntity> list = lambdaQuery().eq(SysUserThirdEntity::getThirdPartyType, platform)
                .in(SysUserThirdEntity::getUserId, userIds)
                .ne(SysUserThirdEntity::getThirdOpenId, "")
                .ne(SysUserThirdEntity::getThirdUserId, "")
                .ne(SysUserThirdEntity::getThirdUnionId, "")
                .list();
        List<ThirdUnionDTO> thirdUnionDTOs = BeanMapperUtils.copyList(ThirdUnionDTO.class,list);
        if(CollUtil.isEmpty(thirdUnionDTOs)){
            return Collections.emptyList();
        }
        List<FindUserDTO> userList = sysUserInfoService.getUserListByUserIds(thirdUnionDTOs.stream().map(ThirdUnionDTO::getUserId).collect(Collectors.toList()));
        Map<String, String> userMap = userList.stream().collect(Collectors.toMap(FindUserDTO::getUserId, FindUserDTO::getUserName));
        thirdUnionDTOs.forEach(thirdUnionDTO -> {
            thirdUnionDTO.setUserName(userMap.get(thirdUnionDTO.getUserId()));
        });
        return thirdUnionDTOs;
    }


    @Override
    public SysUserThirdEntity getUserByThird(String platform, String thirdId) {
        if(StringUtils.isBlank(platform) || StringUtils.isBlank(thirdId)){
            return null;
        }
        return baseMapper.getUserByThird(platform, thirdId);
    }

    @Override
    public List<SysUserThirdEntity> getUserByThirdIdList(String platform, ArrayList<String> thirdIds) {
        if(StringUtils.isBlank(platform) || CollUtil.isEmpty(thirdIds)){
            return null;
        }
        return baseMapper.getUserByThirdList(platform, thirdIds);
    }

    /**
     * 获取第三方绑定的用户
     * @author yl
     * @date 2023-06-19 20:00
     * @param
     * @return java.util.List<com.common.business.dto.FindUserDTO>
     */
    @Override
    public List<FindUserDTO> listThirdBindUser() {
        return baseMapper.listThirdBindUser();
    }
}