package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.UserCancelNoticeEntity;
import com.erp.server.plm.mapper.UserCancelNoticeMapper;
import com.erp.server.plm.service.UserCancelNoticeService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Classname UserCancelNoticeServiceImpl
 * @Description TODO
 * @Date 2022-11-10 15:10
 * @Created by yl
 */
@Service
public class UserCancelNoticeServiceImpl extends ServiceImpl<UserCancelNoticeMapper, UserCancelNoticeEntity>
        implements UserCancelNoticeService {

    /**
     * 获取 用户取消的 通知节点id
     *
     * @param userId
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-11-10 15:26
     */
    @Override
    public List<String> getUserCancelNoticeIds(String userId) {
        LambdaQueryWrapper<UserCancelNoticeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(UserCancelNoticeEntity::getCancelNoticeId);
        queryWrapper.eq(UserCancelNoticeEntity::getUserId, userId);
        return listObjs(queryWrapper, Object::toString);
    }

    /**
     * @param userId
     * @param id
     * @param state
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-10 16:10
     */
    @Override
    public Boolean updateState(String userId, String id, Boolean state) {
        //表示打开
        if (state) {
            //删除取消的表内容
            deleteUserCancel(userId, id);
        } else {
            //表示关闭 就要添加对应的数据
            return addUserCancel(userId, id);
        }

        return true;
    }

    /**
     * 获取取消通知的用户id
     *
     * @param noticeId
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-11-15 10:14
     */
    @Override
    public List<String> cancelNoticeUserIds(String noticeId) {
        LambdaQueryWrapper<UserCancelNoticeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(UserCancelNoticeEntity::getUserId);
        queryWrapper.eq(UserCancelNoticeEntity::getCancelNoticeId, noticeId);
        return listObjs(queryWrapper,Object::toString);
    }


    /**
     * 添加取消的消息表
     *
     * @param userId
     * @param id
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-10 16:23
     */
    private Boolean addUserCancel(String userId, String id) {
        UserCancelNoticeEntity entity = new UserCancelNoticeEntity();
        entity.setCancelNoticeId(id);
        entity.setUserId(userId);
        return this.save(entity);
    }

    /**
     * 删除用户取消的数据
     *
     * @param userId
     * @param noticeId
     */
    public void deleteUserCancel(String userId, String noticeId) {
        LambdaQueryWrapper<UserCancelNoticeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCancelNoticeEntity::getUserId, userId);
        queryWrapper.eq(UserCancelNoticeEntity::getCancelNoticeId, noticeId);
        this.remove(queryWrapper);

    }
}
