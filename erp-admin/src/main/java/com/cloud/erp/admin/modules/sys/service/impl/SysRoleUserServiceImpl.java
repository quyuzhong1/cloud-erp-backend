package com.cloud.erp.admin.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.erp.admin.modules.sys.entity.SysRoleUserEntity;
import com.cloud.erp.admin.modules.sys.mapper.SysRoleUserMapper;
import com.cloud.erp.admin.modules.sys.service.SysRoleUserService;
import com.cloud.erp.admin.modules.sys.vo.SysUserVO;
import com.cloud.erp.common.common.dto.BaseSearchDTO;
import com.cloud.erp.common.utils.BeanMapperUtils;
import com.cloud.erp.common.utils.PageUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


@Service
public class SysRoleUserServiceImpl extends ServiceImpl<SysRoleUserMapper, SysRoleUserEntity> implements SysRoleUserService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        return null;
    }

    /**
     * 批量保存 用户 与角色的  关系
     *
     * @param uid     用户id
     * @param roleIds 角色id
     * @param ifAdd
     */
    @Override
    public void batchInsertRef(String uid, List<String> roleIds, boolean ifAdd) {
        //如果是修改 则要先删除数据
        if (!ifAdd) {
            deleteUidRoleRef(uid);
        }
        if (CollectionUtils.isNotEmpty(roleIds)) {
            List<SysRoleUserEntity> saveList = new LinkedList<>();
            for (String roleId : roleIds) {
                SysRoleUserEntity entity = new SysRoleUserEntity();
                entity.setRoleId(roleId);
                entity.setUserId(uid);
                saveList.add(entity);
            }
            this.saveBatch(saveList);
        }


    }

    /**
     * 根据角色id 删除对应关系
     *
     * @param roleIds
     */
    @Override
    public void removeRefByRoleId(List<String> roleIds) {
        LambdaQueryWrapper<SysRoleUserEntity> wrapper = new LambdaQueryWrapper();
        wrapper.in(SysRoleUserEntity::getRoleId, roleIds);
        baseMapper.delete(wrapper);
    }

    @Override
    public List<SysUserVO> findRoleUser(BaseSearchDTO dto) {
        List<SysUserVO> list = baseMapper.findRoleUser(dto);
        return list;
    }

    /**
     * 根据
     *
     * @param uid
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-07-20 14:07
     */
    @Override
    public List<String> findRoleIdsByUid(String uid) {
        LambdaQueryWrapper<SysRoleUserEntity> wrapper = new LambdaQueryWrapper();
        wrapper.select(SysRoleUserEntity::getRoleId);
        wrapper.eq(SysRoleUserEntity::getUserId, uid);
        List<Object> list = baseMapper.selectObjs(wrapper);
        List<String> resultList=new ArrayList<>(list.size());
        resultList= BeanMapperUtils.copyList(String.class,list);
        return resultList;
    }


    /**
     * @param uid
     */
    private void deleteUidRoleRef(String uid) {
        LambdaQueryWrapper<SysRoleUserEntity> wrapper = new LambdaQueryWrapper();
        wrapper.eq(SysRoleUserEntity::getUserId, uid);
        baseMapper.delete(wrapper);
    }


}