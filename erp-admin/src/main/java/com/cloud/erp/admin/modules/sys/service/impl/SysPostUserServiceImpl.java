package com.cloud.erp.admin.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.erp.admin.modules.sys.dto.BatchSavePostUserDTO;
import com.cloud.erp.admin.modules.sys.entity.SysDepartmentUserEntity;
import com.cloud.erp.admin.modules.sys.entity.SysPostUserEntity;
import com.cloud.erp.admin.modules.sys.mapper.SysPostUserMapper;
import com.cloud.erp.admin.modules.sys.service.SysPostUserService;
import com.cloud.erp.admin.modules.sys.vo.SysUserVO;
import com.erp.common.dto.BaseSearchDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @Classname SysPostUserServiceImpl
 * @Description TODO
 * @Date 2022-07-12 17:10
 * @Created by yl
 */
@Service
public class SysPostUserServiceImpl extends ServiceImpl<SysPostUserMapper, SysPostUserEntity> implements SysPostUserService {

    /**
     * 根据岗位id 集合删除对应关系表
     *
     * @param postIds
     * @return void
     * @author yl
     * @date 2022-07-12 17:17
     */

    @Override
    public void removeByPostId(List<String> postIds) {
        LambdaQueryWrapper<SysPostUserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysPostUserEntity::getPostId, postIds);
        baseMapper.delete(queryWrapper);
    }

    /**
     * 根据关键字 获取用户信息
     *
     * @param dto
     * @return java.util.List<com.cloud.erp.admin.modules.sys.vo.SysUserVO>
     * @author yl
     * @date 2022-07-12 17:49
     */

    @Override
    public List<SysUserVO> findPostUser(BaseSearchDTO dto) {
        List<SysUserVO> list = baseMapper.findPostUser(dto);
        return list;
    }

    /**
     * 批量保存 岗位用户信息
     *
     * @param dto
     * @return boolean
     * @author yl
     * @date 2022-07-29 14:03
     */
    @Override
    public boolean saveBatchPostUser(BatchSavePostUserDTO dto) {
        Set<String> userIds = dto.getUserIds();
        String postId = dto.getPostId();
        //先删除对应的关系
        removePostUser(postId, userIds);
        //在添加
        List<SysPostUserEntity> addList = new LinkedList<>();
        for (String userId : userIds) {
            SysPostUserEntity entity = new SysPostUserEntity();
            entity.setUserId(userId);
            entity.setPostId(postId);
            addList.add(entity);
        }
        if (CollectionUtils.isNotEmpty(addList)) {
            return this.saveBatch(addList);
        }
        return false;
    }


    public void removePostUser(String postId, Set<String> userIds) {
        if (CollectionUtils.isNotEmpty(userIds)) {
            LambdaQueryWrapper<SysPostUserEntity> wrapper = new LambdaQueryWrapper();
            wrapper.in(SysPostUserEntity::getUserId, userIds);
            wrapper.eq(SysPostUserEntity::getPostId, postId);
            baseMapper.delete(wrapper);
        }
    }
}
