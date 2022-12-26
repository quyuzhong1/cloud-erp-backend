package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.bi.entity.BiDataSourceCustomDetailEntity;
import com.erp.server.bi.mapper.BiDataSourceCustomDetailMapper;
import com.erp.server.bi.service.BiDataSourceCustomDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/15 18:15
 */
@Service
public class BiDataSourceCustomDetailServiceImpl extends ServiceImpl<BiDataSourceCustomDetailMapper, BiDataSourceCustomDetailEntity>
        implements BiDataSourceCustomDetailService {
    @Override
    public List<BiDataSourceCustomDetailEntity> listByCustomIds(List<String> customIds) {
        LambdaQueryWrapper<BiDataSourceCustomDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(BiDataSourceCustomDetailEntity::getCustomId,customIds);
        return this.list(queryWrapper);
    }
}
