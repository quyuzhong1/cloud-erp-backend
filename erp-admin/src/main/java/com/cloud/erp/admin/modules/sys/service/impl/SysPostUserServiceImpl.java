package com.cloud.erp.admin.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.erp.admin.modules.sys.entity.SysPostUserEntity;
import com.cloud.erp.admin.modules.sys.mapper.SysPostUserMapper;
import com.cloud.erp.admin.modules.sys.service.SysPostUserService;
import com.cloud.erp.admin.modules.sys.vo.SysUserVO;
import com.cloud.erp.common.common.dto.BaseSearchDTO;
import org.springframework.stereotype.Service;

import java.util.List;

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
        queryWrapper.in(SysPostUserEntity::getPostId,postIds);
        baseMapper.delete(queryWrapper);
    }

    /**
     * 根据关键字 获取用户信息
     * @author yl
     * @date 2022-07-12 17:49
     * @param dto
     * @return java.util.List<com.cloud.erp.admin.modules.sys.vo.SysUserVO>
     */

    @Override
    public List<SysUserVO> findPostUser(BaseSearchDTO dto) {
        List<SysUserVO> list=baseMapper.findPostUser(dto);
        return list;
    }
}
