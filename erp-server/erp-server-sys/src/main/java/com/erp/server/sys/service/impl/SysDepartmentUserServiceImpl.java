package com.erp.server.sys.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.RedisService;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.SysDepartmentUserEntity;
import com.erp.server.sys.mapper.SysDepartmentUserMapper;
import com.erp.server.sys.service.SysDepartmentService;
import com.erp.server.sys.service.SysDepartmentUserService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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
    @Resource
    private RedisService redisService;

    @Override
    public PagingVO findDepartmentUser(PagingDTO<DepartmentSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        DepartmentSearchDTO params = dto.getParams();
        List<String> departmentIds = sysDepartmentService.getDepartmentIds(params.getDepartmentId());
        IPage pageData = baseMapper.findDepartmentUser(query, params, departmentIds);
        return new PagingVO(pageData);
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
        LoginUser loginUser = UserContext.getNonLoginUser();
        //删除缓存@Cacheable(value = "cache:sys:dept:getDeptByUserId", key = "#userId")
        redisService.deleteObject(String.format("cache:sys:dept:getDeptByUserId::%s", loginUser.getUid()));
        LambdaUpdateWrapper<SysDepartmentUserEntity> updateWrapper = new LambdaUpdateWrapper();
        updateWrapper.set(SysDepartmentUserEntity::getLeadState, dto.getState());
        updateWrapper.set(SysDepartmentUserEntity::getUpdateTime, LocalDateTime.now());
        updateWrapper.set(SysDepartmentUserEntity::getUpdateUserId, loginUser.getUid());
        updateWrapper.set(SysDepartmentUserEntity::getUpdateUserName, loginUser.getUserName());
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

        List<SysDepartmentUserEntity> dbList = this.listByDepartmentIds(Arrays.asList(departmentId));
        List<String> existUserIdList = dbList.stream().map(SysDepartmentUserEntity::getUserId).collect(Collectors.toList());
        List<String> removeIdList = dbList.stream().filter(d -> !userIds.contains(d.getUserId())).
                map(SysDepartmentUserEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(removeIdList)) {
            this.removeByIds(removeIdList);
        }
        List<String> addUserList = userIds.stream().filter(a -> !existUserIdList.contains(a)).collect(Collectors.toList());
        //在添加
        List<SysDepartmentUserEntity> addList = new LinkedList<>();
        LocalDateTime now = LocalDateTime.now();
        LoginUser loginUser = UserContext.getNonLoginUser();
        String currentUserId = loginUser.getUid();
        String userName = loginUser.getUserName();
        for (String userId : addUserList) {
            SysDepartmentUserEntity entity = new SysDepartmentUserEntity();
            entity.setUserId(userId);
            entity.setDepartmentId(departmentId);
            entity.setUpdateTime(now);
            entity.setUpdateUserId(currentUserId);
            entity.setUpdateUserName(userName);
            entity.setCreateTime(now);
            entity.setCreateUserId(currentUserId);
            entity.setCreateUserName(userName);
            //删除缓存@Cacheable(value = "cache:sys:dept:getDeptByUserId", key = "#userId")
            redisService.deleteObject(String.format("cache:sys:dept:getDeptByUserId::%s", userId));
            addList.add(entity);
        }
        if (CollectionUtils.isNotEmpty(addList)) {
            return this.saveBatch(addList);
        }
        return false;
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
    @Cacheable(value = "cache:sys:dept:getDeptByUserId", key = "#userId")
    public SysDepartmentUserNumberDTO getDeptByUserId(String userId) {
        SysDepartmentUserNumberDTO deptByUserId = baseMapper.getDeptByUserId(userId);
        if (ObjectUtils.isEmpty(deptByUserId)) {
            return new SysDepartmentUserNumberDTO();
        }
        return deptByUserId;
    }

    @Override
    public SysDepartmentUserNumberDTO getDeptByUserIdWithDisabledFilter(String userId) {
        List<SysDepartmentUserNumberDTO> deptByUserId = baseMapper.getDeptByUserIdWithDisabledFilter(userId);
        if (CollectionUtils.isEmpty(deptByUserId)) {
            return new SysDepartmentUserNumberDTO();
        }
        return deptByUserId.get(0);
    }

    @Override
    public List<UserSuperiorDTO> listSuperiorByUserId(String userId) {
        List<UserSuperiorDTO> resultList = baseMapper.listSuperiorByUserId(userId);
        if (CollUtil.isEmpty(resultList)) {
            return resultList;
        }
        // 统计是否包含自己，如果只有自己，则直接返回，否则过滤掉包含自己的上级
        long count = resultList.stream().filter(x -> CharSequenceUtil.isNotBlank(x.getUserId()) && !userId.equals(x.getUserId())).count();
        if (count > 0) {
            return resultList.stream().filter(x -> !userId.equals(x.getUserId())).collect(Collectors.toList());
        }
        return resultList;
    }

    @Override
    public List<UserSuperiorDTO> listDeptByUserId(String userId) {
        return baseMapper.listSuperiorByUserId(userId);
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
        List<String> userIds = userIdList.stream()
                .filter(com.alibaba.nacos.common.utils.StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<SysDepartmentUserNumberDTO> result = new ArrayList<>();
        List<String> missUserIds = new ArrayList<>();
        for (String userId : userIds) {
            String redisKey = String.format("cache:sys:dept:getDeptByUserId::%s", userId);
            SysDepartmentUserNumberDTO cacheObject = redisService.getCacheObject(redisKey);
            if (cacheObject != null) {
                if (Objects.nonNull(cacheObject.getDepartmentId())) {
                    result.add(cacheObject);
                }
            } else {
                missUserIds.add(userId);
            }
        }
        if (CollectionUtils.isNotEmpty(missUserIds)) {
            List<SysDepartmentUserNumberDTO> dbList = baseMapper.listDeptUserByUserIdList(missUserIds);
            Map<String, SysDepartmentUserNumberDTO> dbMap = dbList.stream()
                    .collect(Collectors.toMap(SysDepartmentUserNumberDTO::getUserId, Function.identity(), (existing, replacement) -> existing));
            for (String userId : missUserIds) {
                SysDepartmentUserNumberDTO dto = dbMap.getOrDefault(userId, new SysDepartmentUserNumberDTO());
                String redisKey = String.format("cache:sys:dept:getDeptByUserId::%s", userId);
                redisService.setCacheObject(redisKey, dto, 8L, TimeUnit.HOURS);
                result.add(dto);
            }
        }
        return result;
    }

    @Override
    public List<SysDepartmentUserNumberDTO> listDeptUserByDeptIdList(List<String> deptIdList) {
        if (CollectionUtils.isEmpty(deptIdList)) {
            return Collections.emptyList();
        }
        return baseMapper.listDeptUserByDeptIdList(deptIdList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "cache:sys:dept:getDeptByUserId", key = "#uid")
    public void batchSaveOrUpdate(String uid, List<String> departmentIdList, boolean ifAdd) {
        if (StringUtils.isBlank(uid)) {
            return;
        }
        //如果是修改 则要先删除数据
        if (!ifAdd) {
            deleteUidDepartmentRef(uid);
        }
        if (CollectionUtils.isNotEmpty(departmentIdList)) {
            //排除已存在的关联数据
            List<SysDepartmentUserEntity> oldDepartmentIds = lambdaQuery().in(SysDepartmentUserEntity::getDepartmentId, departmentIdList).eq(SysDepartmentUserEntity::getUserId, uid).list();
            if (CollUtil.isNotEmpty(oldDepartmentIds)) {
                Set<String> existingIds = oldDepartmentIds.stream()
                        .map(SysDepartmentUserEntity::getDepartmentId)
                        .collect(Collectors.toSet());
                departmentIdList.removeIf(existingIds::contains);
            }
            if (CollectionUtils.isNotEmpty(departmentIdList)) {
                List<SysDepartmentUserEntity> addList = new LinkedList<>();
                for (String departmentId : departmentIdList) {
                    SysDepartmentUserEntity entity = new SysDepartmentUserEntity();
                    entity.setUserId(uid);
                    entity.setDepartmentId(departmentId);
                    addList.add(entity);
                }
                this.saveBatch(addList);
            }
        }
    }

    @Override
    public List<SysDepartmentUserNumberDTO> listDeptByUserIdWithDisabledFilter(String userId) {
        return baseMapper.getDeptByUserIdWithDisabledFilter(userId);
    }

    @Override
    public void deleteByUserIds(List<String> uids) {
        if (CollectionUtils.isEmpty(uids)) {
            return;
        }
        uids.forEach(userId -> {
            //删除缓存@Cacheable(value = "cache:sys:dept:getDeptByUserId", key = "#userId")
            redisService.deleteObject(String.format("cache:sys:dept:getDeptByUserId::%s", userId));
        });
        lambdaUpdate().in(SysDepartmentUserEntity::getUserId, uids).remove();
    }

    @Override
    public boolean deleteByIds(List<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return true;
        }
        List<SysDepartmentUserEntity> entityList = listByIds(ids);
        if (CollUtil.isEmpty(entityList)) {
            return true;
        }
        entityList.stream().map(SysDepartmentUserEntity::getUserId).distinct().forEach(userId -> {
            //删除缓存@Cacheable(value = "cache:sys:dept:getDeptByUserId", key = "#userId")
            redisService.deleteObject(String.format("cache:sys:dept:getDeptByUserId::%s", userId));
        });
        this.removeByIds(ids);
        return true;
    }

    private void deleteUidDepartmentRef(String uid) {
        LambdaQueryWrapper<SysDepartmentUserEntity> wrapper = new LambdaQueryWrapper();
        wrapper.eq(SysDepartmentUserEntity::getUserId, uid);
        baseMapper.delete(wrapper);
    }


    public void removeDepartmentUser(String departmentId, Set<String> userIds) {
        if (CollectionUtils.isNotEmpty(userIds)) {
            LambdaQueryWrapper<SysDepartmentUserEntity> wrapper = new LambdaQueryWrapper();
            wrapper.eq(SysDepartmentUserEntity::getDepartmentId, departmentId);
            baseMapper.delete(wrapper);
        }
    }
}
