package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.bi.entity.BiSubjectDefaultEntity;
import com.erp.model.bi.entity.BiSubjectEntity;
import com.erp.server.bi.mapper.BiSubjectDefaultMapper;
import com.erp.server.bi.service.BiSubjectDefaultService;
import com.erp.server.bi.service.CommonService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Classname BiSubjectDefaultServiceImpl
 * @Description TODO
 * @Date 2022-12-09 14:19
 * @Created by yl
 */
@Service
public class BiSubjectDefaultServiceImpl extends ServiceImpl<BiSubjectDefaultMapper, BiSubjectDefaultEntity> implements BiSubjectDefaultService {

    @Resource
    private CommonService commonService;

    /**
     * 根据用户Id获取用户默认的专题信息
     *
     * @param userId
     * @return java.util.List<com.erp.model.bi.entity.BiSubjectDefaultEntity>
     * @author yl
     * @date 2022-12-09 14:27
     */
    @Override
    public List<BiSubjectDefaultEntity> getByUserId(String userId) {
        LambdaQueryWrapper<BiSubjectDefaultEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiSubjectDefaultEntity::getUserId, userId);
        return this.list(queryWrapper);
    }


    /**
     * 设置默认的专题
     *
     * @param subjectId
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-12-09 14:50
     */
    @Override
    public Boolean setDefault(String subjectId) {
        String userId = commonService.getUserInfo().getUid();
        //先删除已有的
        deleteByUserId(userId);
        BiSubjectDefaultEntity defaultSubject = new BiSubjectDefaultEntity();
        defaultSubject.setSubjectId(subjectId);
        defaultSubject.setUserId(userId);
        return this.save(defaultSubject);
    }

    private void deleteByUserId(String userId) {
        LambdaQueryWrapper<BiSubjectDefaultEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiSubjectDefaultEntity::getUserId, userId);
        this.remove(queryWrapper);
    }


    @Override
    public List<BiSubjectEntity> getDefault(String userId) {
        return baseMapper.getDefaultSubject(userId);
    }


    /**
     * 根据专题id 删除默认信息
     *
     * @param subjectId
     * @return void
     * @author yl
     * @date 2022-12-13 12:00
     */
    @Override
    public void deleteBySubjectId(String subjectId) {
        LambdaQueryWrapper<BiSubjectDefaultEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiSubjectDefaultEntity::getSubjectId, subjectId);
        this.remove(queryWrapper);
    }
}
