package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.bi.entity.BiSubjectRefLayoutEntity;
import com.erp.server.bi.mapper.BiSubjectRefLayoutMapper;
import com.erp.server.bi.service.BiSubjectRefLayoutService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 专题与布局关系表(BiSubjectRefLayout)表服务实现类
 *
 * @author yl
 * @since 2022-12-08 14:32:40
 */
@Service
public class BiSubjectRefLayoutServiceImpl extends ServiceImpl<BiSubjectRefLayoutMapper, BiSubjectRefLayoutEntity> implements BiSubjectRefLayoutService {


    /**
     * 保存专题与布局关系表
     *
     * @param subjectId
     * @param layoutIds
     * @return void
     * @author yl
     * @date 2022-12-13 16:54
     */
    @Override
    public void addSubjectRefLayout(String subjectId, List<String> layoutIds) {
        if (CollectionUtils.isNotEmpty(layoutIds)) {
            List<BiSubjectRefLayoutEntity> addList = new ArrayList<>(layoutIds.size());
            for (String layoutId : layoutIds) {
                BiSubjectRefLayoutEntity entity = new BiSubjectRefLayoutEntity();
                entity.setLayoutId(layoutId);
                entity.setSubjectId(subjectId);
                addList.add(entity);
            }
            this.saveBatch(addList);
        }
    }


    /**
     * 删除专题与布局关系
     *
     * @param subjectId
     * @return void
     * @author yl
     * @date 2022-12-14 14:57
     */
    @Override
    public void deleteBySubjectId(String subjectId) {
        LambdaQueryWrapper<BiSubjectRefLayoutEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiSubjectRefLayoutEntity::getSubjectId, subjectId);
        this.remove(queryWrapper);
    }

}
