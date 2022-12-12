package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.dto.base.UpdateStateDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.ModuleDTO;
import com.erp.model.bi.dto.ModulePagingDTO;
import com.erp.model.bi.entity.BiModuleEntity;
import com.erp.server.bi.constant.IsDeleted;
import com.erp.server.bi.mapper.BiModuleMapper;
import com.erp.server.bi.service.BiModulePermissionService;
import com.erp.server.bi.service.BiModuleService;
import com.erp.server.bi.service.BiSysModuleService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 模块表(BiModule)表服务实现类
 *
 * @author yl
 * @since 2022-12-08 14:31:14
 */
@Service("biModuleService")
public class BiModuleServiceImpl extends ServiceImpl<BiModuleMapper, BiModuleEntity> implements BiModuleService {


    @Resource
    private BiModulePermissionService modulePermissionService;

    @Resource
    private BiSysModuleService sysModuleService;


    /**
     * 模块分页
     *
     * @param dto
     * @return com.erp.common.vo.PagingVO<com.erp.model.bi.dto.ModulePagingDTO>
     * @author yl
     * @date 2022-12-12 11:37
     */
    @Override
    public PagingVO<ModulePagingDTO> paging(PagingDTO<BaseSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BaseSearchDTO params = dto.getParams();
        IPage pageData = baseMapper.paging(query, params);
        return new PagingVO(pageData);
    }


    /**
     * 修改模板状态
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-12-12 11:59
     */
    @Override
    public Boolean updateState(UpdateStateDTO dto) {
        BiModuleEntity entity = this.getById(dto.getId());
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_97004);
        }
        Boolean stateFlag = dto.getState();
        if (stateFlag) {
            entity.setState(IsDeleted.YES);
        } else {
            entity.setState(IsDeleted.NO);
        }
        return this.updateById(entity);
    }

    /**
     * 新增数据
     *
     * @param biModule 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional
    public Boolean insert(ModuleDTO biModule) {
        BiModuleEntity module = new BiModuleEntity();
        String name = biModule.getName();
        checkName(null, name);
        String sysModuleId = biModule.getSysModuleId();
        module.setImageUrl(biModule.getImageUrl());
        module.setName(name);
        module.setRemark(biModule.getRemark());
        module.setViewCode(biModule.getViewCode());
        module.setCategoryId(biModule.getCategoryId());
        module.setSysModuleId(sysModuleId);
        List<String> permissionUserIdList = biModule.getPermissionUserIdList();
        boolean flag = this.save(module);
        if (flag) {
            sysModuleService.updateAddState(sysModuleId,IsDeleted.YES);
            if (CollectionUtils.isNotEmpty(permissionUserIdList)) {
                modulePermissionService.addModulePermission(module.getId(), permissionUserIdList);
            }
        }
        return flag;
    }


    /**
     * 检查模块名 是否重复
     * 只检查二级分类的
     *
     * @param id 表id  pid pid name 名字
     * @return void
     * @author yl
     * @date 2022-12-12 10:34
     */
    public void checkName(String id, String name) {
        LambdaQueryWrapper<BiModuleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiModuleEntity::getName, name);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(BiModuleEntity::getId, id);
        }
        queryWrapper.last("LIMIT 1");
        int count = this.count(queryWrapper);
        if (count > 0) {
            new ServiceException(ApiError.ERROR_97003);
        }


    }

    /**
     * 修改数据
     *
     * @param biModule 实例对象
     * @return 实例对象
     */
    @Override
    public Boolean update(ModuleDTO biModule) {
        BiModuleEntity module = this.getById(biModule.getId());
        if(Objects.isNull(module)){
            throw new ServiceException(ApiError.ERROR_97004);
        }
        String name = biModule.getName();
        checkName(biModule.getId(), name);
        module.setImageUrl(biModule.getImageUrl());
        module.setName(name);
        module.setRemark(biModule.getRemark());
        module.setViewCode(biModule.getViewCode());
        String sysModuleId = biModule.getSysModuleId();
        String dbSysModuleId=module.getSysModuleId();
        module.setCategoryId(biModule.getCategoryId());
        module.setSysModuleId(sysModuleId);
        List<String> permissionUserIdList = biModule.getPermissionUserIdList();
        boolean flag = this.updateById(module);
        if (flag) {
            /*
             *当两个传来的不一样 说明更改了系统的模块
             * 那么原来的
             */
            if(!sysModuleId.equals(dbSysModuleId)){
                sysModuleService.updateAddState(sysModuleId,IsDeleted.YES);
                sysModuleService.updateAddState(dbSysModuleId,IsDeleted.NO);
            }
            if (CollectionUtils.isNotEmpty(permissionUserIdList)) {
                modulePermissionService.addModulePermission(module.getId(), permissionUserIdList);
            }
        }
        return flag;
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public Boolean deleteById(String id) {
        Boolean flag = this.removeById(id);
        if (flag) {
            modulePermissionService.deleteByModuleId(id);
        }
        return flag;
    }


}
