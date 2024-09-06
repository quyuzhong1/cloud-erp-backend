package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.SysDepartmentUserEntity;
import com.erp.server.sys.mapper.SysDepartmentUserMapper;
import com.erp.server.sys.service.SysDepartmentService;
import com.erp.server.sys.service.SysDepartmentUserService;
import com.erp.server.sys.service.SysUserInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname SysDepartmentUserServiceImpl
 * @Date 2022-07-13 18:54
 * @Created by yl
 */
@Service
public class SysDepartmentUserServiceImpl extends ServiceImpl<SysDepartmentUserMapper, SysDepartmentUserEntity> implements SysDepartmentUserService {

    @Autowired
    private SysDepartmentService sysDepartmentService;

    @Autowired
    private SysUserInfoService sysUserInfoService;

    @Override
    public PagingVO findDepartmentUser(PagingDTO<DepartmentSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        DepartmentSearchDTO params = dto.getParams();
        List<String> departmentIds = sysDepartmentService.getDepartmentIds(params.getDepartmentId());
        IPage pageData = baseMapper.findDepartmentUser(query, params, departmentIds);
        return new PagingVO(pageData);
    }


    /**
     * 根据部门Ids 删除对应关系
     *
     * @param ids
     * @return void
     * @author yl
     * @date 2022-07-14 10:03
     */

    @Override
    public void removeByDepartmentIds(List<String> ids) {
        LambdaQueryWrapper<SysDepartmentUserEntity> wrapper = new LambdaQueryWrapper();
        if (CollectionUtils.isNotEmpty(ids)) {
            wrapper.in(SysDepartmentUserEntity::getDepartmentId, ids);
            baseMapper.delete(wrapper);
        }

    }

    /**
     * 设置主管
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-07-14 17:02
     */

    @Override
    public void setLead(UpdateUserStateDTO dto) {
        LambdaUpdateWrapper<SysDepartmentUserEntity> updateWrapper = new LambdaUpdateWrapper();
        updateWrapper.set(SysDepartmentUserEntity::getLeadState, dto.getState());
        updateWrapper.in(SysDepartmentUserEntity::getId, dto.getIds());
        this.update(updateWrapper);

    }

    /**
     * 分组获取部门的用户数
     *
     * @param
     * @return java.util.List<com.cloud.erp.admin.modules.sys.vo.SysDepartmentUserNumber>
     * @author yl
     * @date 2022-07-18 14:11
     */
    @Override
    public List<SysDepartmentUserNumberDTO> findUserNumber() {
        return baseMapper.findUserNumber();
    }


    /**
     * 批量保存部门员工  先删除
     *
     * @param dto
     * @return boolean
     * @author yl
     * @date 2022-07-29 10:45
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchDepartmentUser(BatchSysDepartUserDTO dto) {
        Set<String> userIds = dto.getUserIds();
        String departmentId = dto.getDepartmentId();

        List<SysDepartmentUserEntity> dbList = lambdaQuery().eq(SysDepartmentUserEntity::getDepartmentId, departmentId).in(SysDepartmentUserEntity::getUserId, userIds).list();
        List<String> existUserIdList=dbList.stream().map(SysDepartmentUserEntity::getUserId).collect(Collectors.toList());
        
        List<String> addUserList=userIds.stream().filter(a->!existUserIdList.contains(a)).collect(Collectors.toList());
        //在添加
        List<SysDepartmentUserEntity> addList = new LinkedList<>();
        for (String userId : addUserList) {
            SysDepartmentUserEntity entity = new SysDepartmentUserEntity();
            entity.setUserId(userId);
            entity.setDepartmentId(departmentId);
            addList.add(entity);
        }
        if (CollectionUtils.isNotEmpty(addList)) {
            return this.saveBatch(addList);
        }
        return true;
    }

    @Override
    public SysDepartmentUserNumberDTO getByUserId(String id) {
        LambdaQueryWrapper<SysDepartmentUserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDepartmentUserEntity::getUserId, id);
        queryWrapper.ne(SysDepartmentUserEntity::getDepartmentId, "");
        queryWrapper.last("limit 1");
        SysDepartmentUserEntity sysDepartmentUserEntity = this.getOne(queryWrapper);
        SysDepartmentUserNumberDTO dto = new SysDepartmentUserNumberDTO();
        if (ObjectUtils.isNotEmpty(sysDepartmentUserEntity)) {
            BeanMapperUtils.copy(sysDepartmentUserEntity, dto);
        }
        return dto;
    }

    @Override
    public List<SysDepartmentUserEntity> listByDepartmentIds(List<String> departmentIdList) {
        if (CollectionUtils.isEmpty(departmentIdList)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SysDepartmentUserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysDepartmentUserEntity::getDepartmentId, departmentIdList);
        return this.list(queryWrapper);
    }

    @Override
    public List<SysDepartmentUserEntity> listSuperiorById(String id) {
        LambdaQueryWrapper<SysDepartmentUserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDepartmentUserEntity::getDepartmentId, id);
        queryWrapper.eq(SysDepartmentUserEntity::getLeadState, 1);
        return this.list(queryWrapper);
    }

    @Override
    public SysDepartmentUserNumberDTO getDeptByUserId(String userId) {
        SysDepartmentUserNumberDTO deptByUserId = baseMapper.getDeptByUserId(userId);
        if (ObjectUtils.isEmpty(deptByUserId)) {
            return new SysDepartmentUserNumberDTO();
        }
        return deptByUserId;
    }

    @Override
    public List<UserSuperiorDTO> listSuperiorByUserId(String userId) {
        List<UserSuperiorDTO> resultList = baseMapper.listSuperiorByUserId(userId);
        // 如果不包含自己, 则过滤掉包含自己的上级
        if (CollectionUtils.isNotEmpty(resultList)) {
            resultList = resultList.stream().filter(x -> !userId.equals(x.getUserId())).collect(Collectors.toList());
        }
        return resultList;
    }


    /**
     * 根据部门id 获取部门员工
     *
     * @param deptId
     * @return java.util.List<com.common.business.dto.FindUserDTO>
     * @author yl
     * @date 2023-06-05 12:07
     */
    @Override
    public List<FindUserDTO> listDeptUserByDeptId(String deptId) {
        return baseMapper.listDeptUserByDeptId(deptId);
    }

    /**
     * 根据用户ids 获取部门 用户信息
     *
     * @param userIdList
     * @return java.util.List<com.erp.model.sys.dto.SysDepartmentUserNumberDTO>
     * @author yl
     * @date 2023-06-15 16:56
     */
    @Override
    public List<SysDepartmentUserNumberDTO> listDeptUserByUserIdList(List<String> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return Collections.emptyList();
        }
        return baseMapper.listDeptUserByUserIdList(userIdList);
    }

    @Override
    public List<SysDepartmentUserNumberDTO> listDeptUserByDeptIdList(List<String> deptIdList) {
        if (CollectionUtils.isEmpty(deptIdList)) {
            return Collections.emptyList();
        }
        return baseMapper.listDeptUserByDeptIdList(deptIdList);
    }


    public void removeDepartmentUser(String departmentId, Set<String> userIds) {
        if (CollectionUtils.isNotEmpty(userIds)) {
            LambdaQueryWrapper<SysDepartmentUserEntity> wrapper = new LambdaQueryWrapper();
            wrapper.eq(SysDepartmentUserEntity::getDepartmentId, departmentId);
            baseMapper.delete(wrapper);
        }
    }
}
