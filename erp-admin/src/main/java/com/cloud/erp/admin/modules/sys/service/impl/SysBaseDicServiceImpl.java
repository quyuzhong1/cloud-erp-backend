package com.cloud.erp.admin.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.erp.admin.modules.sys.entity.SysBaseDicEntity;
import com.cloud.erp.admin.modules.sys.mapper.SysBaseDicMapper;
import com.cloud.erp.admin.modules.sys.service.SysBaseDicService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.dto.base.BaseDicDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-08-09
 */
@Service
public class SysBaseDicServiceImpl extends ServiceImpl<SysBaseDicMapper, SysBaseDicEntity> implements SysBaseDicService {
    @Override
    public List<SysBaseDicEntity> listByDicType(BaseDicDTO dto) {
        LambdaQueryWrapper<SysBaseDicEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysBaseDicEntity::getDicType, dto.getDicType());
        String searchKeyword = dto.getSearchKeyword();
        if (StringUtils.isNotBlank(searchKeyword)) {
            queryWrapper.like(SysBaseDicEntity::getDicValue,searchKeyword);
        }
        return this.list(queryWrapper);
    }
}
