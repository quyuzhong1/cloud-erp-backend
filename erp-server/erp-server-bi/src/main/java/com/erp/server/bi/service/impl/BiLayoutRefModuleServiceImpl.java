package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.bi.entity.BiLayoutRefModuleEntity;
import com.erp.server.bi.mapper.BiLayoutRefModuleMapper;
import com.erp.server.bi.service.BiLayoutRefModuleService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 布局与模块关系表(BiLayoutRefModule)表服务实现类
 *
 * @author yl
 * @since 2022-12-08 14:29:39
 */
@Service
public class BiLayoutRefModuleServiceImpl extends ServiceImpl<BiLayoutRefModuleMapper, BiLayoutRefModuleEntity> implements BiLayoutRefModuleService {


    /**
     * 保存模块
     *
     * @param layoutId
     * @param blockNo
     * @param moduleIdList
     * @return void
     * @author yl
     * @date 2022-12-13 16:37
     */
    @Override
    public void addLayoutRefModule(String subjectId, String layoutId, String blockNo, List<String> moduleIdList) {
        if (CollectionUtils.isNotEmpty(moduleIdList)) {
            int size = moduleIdList.size();
            List<BiLayoutRefModuleEntity> addList = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                BiLayoutRefModuleEntity refModule = new BiLayoutRefModuleEntity();
                refModule.setBlockNo(blockNo);
                refModule.setSubjectId(subjectId);
                refModule.setLayoutId(layoutId);
                refModule.setSerialNo(i + 1);
                refModule.setModuleId(moduleIdList.get(i));
                addList.add(refModule);
            }
            this.saveBatch(addList);
        }

    }


    /**
     * 根据布局id集合 获取 布局与模块关系
     *
     * @param layoutIdList
     * @return java.util.List<com.erp.model.bi.entity.BiLayoutRefModuleEntity>
     * @author yl
     * @date 2022-12-14 9:32
     */
    @Override
    public List<BiLayoutRefModuleEntity> getByLayoutIds(List<String> layoutIdList) {
        if (CollectionUtils.isNotEmpty(layoutIdList)) {
            LambdaQueryWrapper<BiLayoutRefModuleEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(BiLayoutRefModuleEntity::getLayoutId, layoutIdList);
            return this.list(queryWrapper);
        }
        return new ArrayList<>();
    }


    /**
     * 根据专题id 删除模块布局关系
     *
     * @param subjectId
     * @return void
     * @author yl
     * @date 2022-12-14 15:04
     */
    @Override
    public void deleteBySubjectId(String subjectId) {
        LambdaQueryWrapper<BiLayoutRefModuleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiLayoutRefModuleEntity::getSubjectId, subjectId);
        this.remove(queryWrapper);
    }


    /**
     * 删除
     *
     * @param layoutId
     * @param moduleId
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-12-14 15:28
     */
    @Override
    public Boolean deleteLayoutModuleId(String layoutId, String moduleId) {
        LambdaQueryWrapper<BiLayoutRefModuleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiLayoutRefModuleEntity::getLayoutId, layoutId);
        queryWrapper.eq(BiLayoutRefModuleEntity::getModuleId, moduleId);
        return this.remove(queryWrapper);
    }

    @Override
    public Boolean deleteLayout(String layoutId) {
        LambdaQueryWrapper<BiLayoutRefModuleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiLayoutRefModuleEntity::getLayoutId, layoutId);
        return this.remove(queryWrapper);
    }
}
