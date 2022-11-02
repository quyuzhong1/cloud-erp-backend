package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.sys.dto.BatchSysDepartUserDTO;
import com.erp.model.sys.dto.DepartmentSearchDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.sys.dto.UpdateUserStateDTO;
import com.erp.model.sys.entity.SysDepartmentUserEntity;
import com.erp.server.sys.mapper.SysDepartmentUserMapper;
import com.erp.server.sys.service.SysDepartmentService;
import com.erp.server.sys.service.SysDepartmentUserService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @Classname SysDepartmentUserServiceImpl
 * @Description TODO
 * @Date 2022-07-13 18:54
 * @Created by yl
 */
@Service
public class SysDepartmentUserServiceImpl extends ServiceImpl<SysDepartmentUserMapper, SysDepartmentUserEntity> implements SysDepartmentUserService {

    @Autowired
    private SysDepartmentService sysDepartmentService;

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
    @Transactional
    public boolean saveBatchDepartmentUser(BatchSysDepartUserDTO dto) {
        Set<String> userIds = dto.getUserIds();
        String departmentId = dto.getDepartmentId();
        //先删除对应的关系
        removeDepartmentUser(departmentId, userIds);
        //在添加
        List<SysDepartmentUserEntity> addList=new LinkedList<>();
        for(String userId:userIds){
            SysDepartmentUserEntity entity=new SysDepartmentUserEntity();
            entity.setUserId(userId);
            entity.setDepartmentId(departmentId);
            addList.add(entity);
        }
        if(CollectionUtils.isNotEmpty(addList)){
            return this.saveBatch(addList);
        }
        return false;
    }


    public void removeDepartmentUser(String departmentId, Set<String> userIds) {
        if (CollectionUtils.isNotEmpty(userIds)) {
            LambdaQueryWrapper<SysDepartmentUserEntity> wrapper = new LambdaQueryWrapper();
            wrapper.eq(SysDepartmentUserEntity::getDepartmentId, departmentId);
            baseMapper.delete(wrapper);
        }
    }
}
