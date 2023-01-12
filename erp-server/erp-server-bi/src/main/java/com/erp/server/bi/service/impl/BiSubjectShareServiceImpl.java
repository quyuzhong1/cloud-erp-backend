package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.bi.dto.UpdateSubjectShareDTO;
import com.erp.model.bi.entity.BiSubjectEntity;
import com.erp.model.bi.entity.BiSubjectShareEntity;
import com.erp.server.bi.constant.IsDeleted;
import com.erp.server.bi.enums.DashboardEnum;
import com.erp.server.bi.mapper.BiSubjectShareMapper;
import com.erp.server.bi.service.BiSubjectService;
import com.erp.server.bi.service.BiSubjectShareService;
import com.erp.server.bi.service.CommonService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Resource
    private CommonService commonService;


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
        String loginUserId = commonService.getUserInfo().getUid();
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
        Boolean flag = subjectService.updateById(subject);
        //如果是分享
        if (DashboardEnum.SHARE.getFlag().equals(shareFlag) && flag) {
            addSubjectShare(dto.getShareUserIdList(), subjectId);
        }
        return subjectId;
    }


    /**
     * 获取分享给我的仪表盘id
     *
     * @param userId
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-12-09 11:02
     */
    @Override
    public List<String> getShareToMeDashboardIds(String userId) {
        LambdaQueryWrapper<BiSubjectShareEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(BiSubjectShareEntity::getSubjectId);
        queryWrapper.eq(BiSubjectShareEntity::getUserId, userId);
        return this.listObjs(queryWrapper, Object::toString);
    }


    /**
     * 保存专题分享的信息
     *
     * @param userList
     * @param subjectId
     * @return void
     * @author yl
     * @date 2022-12-13 11:38
     */
    @Override
    public Boolean addSubjectShare(List<String> userList, String subjectId) {
        //先删除分享的数据
        deleteBySubjectId(subjectId);
        if (CollectionUtils.isNotEmpty(userList)) {
            List<BiSubjectShareEntity> addList = new ArrayList<>();
            for (String userId : userList) {
                BiSubjectShareEntity share = new BiSubjectShareEntity();
                share.setSubjectId(subjectId);
                share.setUserId(userId);
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
        updateWrapper.set(BiSubjectShareEntity::getIsDeleted, IsDeleted.YES);
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
    public void checkPermission(String userId, BiSubjectEntity subject) {
        //这个是 这个人是在分享的里面
        Boolean shareFlag = getShare(userId, subject.getId());
        //如果在 就返回
        if (shareFlag) {
            return;
        }
        //如果不在 那么就要看这个专题 是不是没有设置权限  就是私人的
        if (DashboardEnum.PERSONAL.getFlag().equals(subject.getShareFlag())) {
            //当不是创建人的时候 就没有权限看咯
            if (!userId.equals(subject.getCreateUserId())) {
                throw new ServiceException(ApiError.ERROR_97006);
            }
        }


    }

    @Override
    public List<String> getUserIdsBySubjectId(String subjectId) {
        LambdaQueryWrapper<BiSubjectShareEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(BiSubjectShareEntity::getUserId);
        queryWrapper.eq(BiSubjectShareEntity::getSubjectId, subjectId);
        return this.listObjs(queryWrapper, Object::toString);
    }

    private Boolean getShare(String userId, String subjectId) {
        LambdaQueryWrapper<BiSubjectShareEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiSubjectShareEntity::getUserId, userId);
        queryWrapper.eq(BiSubjectShareEntity::getSubjectId, subjectId);
        queryWrapper.last("LIMIT 1");
        int count = this.count(queryWrapper);
        return count > 0 ? true : false;
    }


}
