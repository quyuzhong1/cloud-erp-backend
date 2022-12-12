package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.bi.dto.ModuleSysDTO;
import com.erp.model.bi.entity.BiSysModuleEntity;
import com.erp.server.bi.mapper.BiSysModuleMapper;
import com.erp.server.bi.service.BiSysModuleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 系统模块表(BiSysModule)表服务实现类
 *
 * @author yl
 * @since 2022-12-12 16:54:14
 */
@Service
public class BiSysModuleServiceImpl extends ServiceImpl<BiSysModuleMapper, BiSysModuleEntity> implements BiSysModuleService {


    @Override
    public Boolean insert(ModuleSysDTO dto) {
        BiSysModuleEntity sysModule = new BiSysModuleEntity();
        sysModule.setName(dto.getName());
        sysModule.setPid(dto.getPid());
        checkName(dto.getId(), dto.getName());
        return this.save(sysModule);
    }

    @Override
    public Boolean updateSysModule(ModuleSysDTO dto) {
        BiSysModuleEntity sysModule = new BiSysModuleEntity();
        checkName(dto.getId(), dto.getName());
        sysModule.setName(dto.getName());
        sysModule.setPid(dto.getPid());
        return this.updateById(sysModule);
    }


    /**
     * 获取系统模块信息
     *
     * @param isAdd
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     * @author yl
     * @date 2022-12-12 17:13
     */
    @Override
    public List<Map<String, Object>> getSysModuleList(Integer isAdd) {
        LambdaQueryWrapper<BiSysModuleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(BiSysModuleEntity::getId, BiSysModuleEntity::getName);
        queryWrapper.eq(BiSysModuleEntity::getIsAdd, isAdd);
        return this.listMaps(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getPid(String pid) {
        LambdaQueryWrapper<BiSysModuleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(BiSysModuleEntity::getId, BiSysModuleEntity::getName);
        queryWrapper.eq(BiSysModuleEntity::getPid, pid);
        return this.listMaps(queryWrapper);
    }

    /**
     * 修改添加状态
     *
     * @param id
     * @param isAddFlag
     * @return void
     * @author yl
     * @date 2022-12-12 17:49
     */
    @Override
    public void updateAddState(String id, Integer isAddFlag) {
        LambdaUpdateWrapper<BiSysModuleEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(BiSysModuleEntity::getIsAdd, isAddFlag);
        updateWrapper.eq(BiSysModuleEntity::getId, id);
        this.update(updateWrapper);
    }

    private void checkName(String id, String name) {
        LambdaQueryWrapper<BiSysModuleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiSysModuleEntity::getName, name);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(BiSysModuleEntity::getId, id);
        }
        queryWrapper.last("LIMIT 1");
        int count = this.count(queryWrapper);
        if (count > 0) {
            new ServiceException(ApiError.ERROR_97003);
        }
    }
}
