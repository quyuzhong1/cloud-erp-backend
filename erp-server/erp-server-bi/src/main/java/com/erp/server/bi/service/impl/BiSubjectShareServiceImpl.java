package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.constant.BaseStateConstants;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.bi.dto.UpdateSubjectShareDTO;
import com.erp.model.bi.entity.BiSubjectEntity;
import com.erp.model.bi.entity.BiSubjectShareEntity;
import com.erp.model.bi.enums.BiShareIdentityTypeEnum;
import com.erp.server.bi.enums.DashboardEnum;
import com.erp.server.bi.mapper.BiSubjectShareMapper;
import com.erp.server.bi.service.BiSubjectService;
import com.erp.server.bi.service.BiSubjectShareService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 主题分享表(BiSubjectShare)表服务实现类
 *
 * @author yl
 * @since 2022-12-08 14:33:46
 */
@Service
public class BiSubjectShareServiceImpl extends ServiceImpl<BiSubjectShareMapper, BiSubjectShareEntity> implements BiSubjectShareService {

    @Resource
    private BiSubjectService subjectService;


    /**
     * 专题设置权限
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-12-08 17:19
     */
    @Override
    public String setShare(UpdateSubjectShareDTO dto) {
        //主题id
        String subjectId = dto.getId();
        BiSubjectEntity subject = subjectService.getById(subjectId);
        if (Objects.isNull(subject)) {
            throw new ServiceException(ApiError.ERROR_97000);
        }
        String loginUserId = UserContext.getDefaultLoginUser().getUid();
        if (!loginUserId.equals(subject.getCreateUserId())) {
            throw new ServiceException(ApiError.ERROR_97002);
        }
        String shareFlag = dto.getShareFlag();
        String name = dto.getName();
        //当名子不一样就改变名字
        if (!name.equals(subject.getName())) {
            subjectService.checkName(subjectId, name);
            subject.setName(name);
        }
        subject.setShareFlag(shareFlag);
        subject.setIsFrequently(dto.getIsFrequently());
        boolean flag = subjectService.updateById(subject);
        //如果是分享
        if (!DashboardEnum.PERSONAL.getFlag().equals(shareFlag) && flag) {
            checkAndAddSubjectShare(dto.checkAndGetShareFlagIdList(), subjectId, shareFlag);
        }else{
            //删除分享的数据
            deleteBySubjectId(subjectId);
        }
        return subjectId;
    }


    /**
     * 获取分享给我的仪表盘id
     *
     * @param userId
     * @param roleIdList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-12-09 11:02
     */
    @Override
    public List<String> getShareToMeDashboardIds(String userId, List<String> roleIdList) {
        LambdaQueryWrapper<BiSubjectShareEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(BiSubjectShareEntity::getSubjectId)
                .eq(BiSubjectShareEntity::getIdentityId, userId)
        ;
        return this.listObjs(queryWrapper, Object::toString);
    }


    /**
     * 保存专题分享的信息
     *
     * @param identityIdList
     * @param subjectId
     * @param identityTypeEnum
     * @return void
     * @author yl
     * @date 2022-12-13 11:38
     */
    @Override
    public Boolean addSubjectShare(List<String> identityIdList, String subjectId, BiShareIdentityTypeEnum identityTypeEnum) {
        //先删除分享的数据
        deleteBySubjectId(subjectId);
        if (CollectionUtils.isNotEmpty(identityIdList)) {
            List<BiSubjectShareEntity> addList = new ArrayList<>();
            for (String userId : identityIdList) {
                BiSubjectShareEntity share = new BiSubjectShareEntity();
                share.setSubjectId(subjectId);
                share.setIdentityId(userId);
                share.setIdentityType(identityTypeEnum.getCode());
                addList.add(share);
            }
            return this.saveBatch(addList);
        }
        return true;
    }


    /**
     * 根据主题id  删除 对应的分享信息
     *
     * @param subjectId
     * @return void
     * @author yl
     * @date 2022-12-08 17:22
     */
    @Override
    public void deleteBySubjectId(String subjectId) {
        LambdaUpdateWrapper<BiSubjectShareEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(BiSubjectShareEntity::getIsDeleted, BaseStateConstants.DELETED_YES);
        updateWrapper.eq(BiSubjectShareEntity::getSubjectId, subjectId);
        this.update(updateWrapper);
    }


    /**
     * 检查用户id 是否可见 该专题
     *
     * @param userId
     * @param subject
     * @return void
     * @author yl
     * @date 2022-12-13 18:10
     */
    @Override
    public void checkPermission(String userId, BiSubjectEntity subject, List<String> roleIdList) {
        //这个是 这个人是在分享的里面
        Boolean shareFlag = getShare(userId, subject.getId(), roleIdList);
        //如果在 就返回
        if (Boolean.FALSE.equals(shareFlag)
                && DashboardEnum.PERSONAL.getFlag().equals(subject.getShareFlag()) && !userId.equals(subject.getCreateUserId())) {//如果不在 那么就要看这个专题 是不是没有设置权限  就是私人的
            throw new ServiceException(ApiError.ERROR_97006);
        }
    }

    @Override
    public List<String> getUserIdsBySubjectId(String subjectId) {
        LambdaQueryWrapper<BiSubjectShareEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(BiSubjectShareEntity::getIdentityId);
        queryWrapper.eq(BiSubjectShareEntity::getSubjectId, subjectId);
        return this.listObjs(queryWrapper, Object::toString);
    }

    /**
     * 检查和添加共享记录
     */
    @Override
    public void checkAndAddSubjectShare(List<String> shareFlagIdList, String subjectId, String shareFlag) {
        // 私人
        if (DashboardEnum.PERSONAL.getFlag().equals(shareFlag)) {
            // 移除其他
            deleteBySubjectId(subjectId);
            return;
        }

        // 检查对应身份类型
        BiShareIdentityTypeEnum refTypeEnum = BiShareIdentityTypeEnum.isRoleCheck(shareFlag);

        //添加专题的分享用户/角色
        this.addSubjectShare(shareFlagIdList, subjectId, refTypeEnum);
    }

    /**
     * 查询用户支持的专题
     */
    @Override
    public List<String> findSubjectId(String userId, List<String> roleIdList) {
        LambdaQueryChainWrapper<BiSubjectShareEntity> lambdaWrapper = lambdaQuery()
                .eq(BiSubjectShareEntity::getIdentityType, BiShareIdentityTypeEnum.USER.getCode())
                .eq(BiSubjectShareEntity::getIdentityId, userId);

        if (CollectionUtils.isNotEmpty(roleIdList)){
            lambdaWrapper = lambdaWrapper.or(w->
                            w.eq(BiSubjectShareEntity::getIdentityType, BiShareIdentityTypeEnum.ROLE.getCode())
                            .in(BiSubjectShareEntity::getIdentityId, roleIdList)
                    );
        }
        return lambdaWrapper.list().stream()
                .map(BiSubjectShareEntity::getSubjectId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 当前用户是否有权限
     */
    @Override
    public Boolean getShare(String userId, String subjectId, List<String> roleIdList) {
        LambdaQueryWrapper<BiSubjectShareEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiSubjectShareEntity::getSubjectId, subjectId);
        queryWrapper.and(ww -> ww.or(w->
                w.eq(BiSubjectShareEntity::getIdentityId, userId)
                .eq(BiSubjectShareEntity::getIdentityType, BiShareIdentityTypeEnum.USER.getCode())
        ).or(sw -> sw
                .in(CollectionUtils.isNotEmpty(roleIdList), BiSubjectShareEntity::getIdentityId, roleIdList)
                .eq(CollectionUtils.isNotEmpty(roleIdList), BiSubjectShareEntity::getIdentityType, BiShareIdentityTypeEnum.ROLE.getCode())
                ));

        int count = this.count(queryWrapper);
        return count > 0;
    }

    @Override
    public List<BiSubjectShareEntity> findBySubjectId(String subjectId) {
        return lambdaQuery()
                .eq(BiSubjectShareEntity::getSubjectId, subjectId)
                .list();
    }

    @Override
    public Map<String, List<BiSubjectShareEntity>> mapBySubjectIds(List<String> subjectIds) {
        if (CollectionUtils.isEmpty(subjectIds)){
            return Collections.emptyMap();
        }
        return lambdaQuery()
                .in(BiSubjectShareEntity::getSubjectId, subjectIds)
                .list()
                .stream()
                .collect(Collectors.groupingBy(BiSubjectShareEntity::getSubjectId))
                ;
    }


}
