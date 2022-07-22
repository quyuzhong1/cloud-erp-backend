package com.cloud.erp.admin.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.erp.admin.modules.sys.dto.DepartmentSearchDTO;
import com.cloud.erp.admin.modules.sys.dto.UpdateUserStateDTO;
import com.cloud.erp.admin.modules.sys.entity.SysDepartmentUserEntity;
import com.cloud.erp.admin.modules.sys.mapper.SysDepartmentUserMapper;
import com.cloud.erp.admin.modules.sys.service.SysDepartmentService;
import com.cloud.erp.admin.modules.sys.service.SysDepartmentUserService;
import com.cloud.erp.admin.modules.sys.vo.SysDepartmentUserNumber;
import com.cloud.erp.common.common.dto.PagingDTO;
import com.cloud.erp.common.common.vo.PagingVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Classname SysDepartmentUserServiceImpl
 * @Description TODO
 * @Date 2022-07-13 18:54
 * @Created by yl
 */
@Service
public class SysDepartmentUserServiceImpl extends ServiceImpl<SysDepartmentUserMapper, SysDepartmentUserEntity> implements SysDepartmentUserService {

    @Autowired
    private SysDepartmentService  sysDepartmentService;

    @Override
    public PagingVO findDepartmentUser(PagingDTO<DepartmentSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        DepartmentSearchDTO params = dto.getParams();
        List<String> departmentIds = sysDepartmentService.getDepartmentIds(params.getDepartmentId());
        IPage pageData =baseMapper.findDepartmentUser(query,params,departmentIds);
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
        wrapper.in(SysDepartmentUserEntity::getDepartmentId, ids);
        baseMapper.delete(wrapper);

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
     * @author yl
     * @date 2022-07-18 14:11
     * @param
     * @return java.util.List<com.cloud.erp.admin.modules.sys.vo.SysDepartmentUserNumber>
     */
    @Override
    public List<SysDepartmentUserNumber> findUserNumber() {
        return baseMapper.findUserNumber();
    }
}
