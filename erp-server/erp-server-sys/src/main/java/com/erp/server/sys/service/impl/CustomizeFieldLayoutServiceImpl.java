package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.dto.CustomizeFieldLayoutDTO;
import com.erp.model.sys.dto.FindCustomizeFieldDTO;
import com.erp.model.sys.entity.CustomizeFieldLayoutEntity;
import com.erp.model.sys.vo.UserFieldVO;
import com.erp.server.sys.mapper.CustomizeFieldLayoutMapper;
import com.erp.server.sys.service.CustomizeFieldLayoutService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * (CustomizeFieldDisplay)表服务实现类
 *
 * @author yl
 * @since 2023-02-03 18:59:41
 */
@Service
public class CustomizeFieldLayoutServiceImpl extends ServiceImpl<CustomizeFieldLayoutMapper, CustomizeFieldLayoutEntity> implements CustomizeFieldLayoutService {

    @Override
    public Boolean add(CustomizeFieldLayoutDTO dto) {
        if (dto != null) {
            CustomizeFieldLayoutEntity entity = new CustomizeFieldLayoutEntity();
            BeanMapper.copy(dto, entity);
            entity.setCreateTime(new Date());
            entity.setUpdateTime(new Date());
            entity.setCreateUserId(dto.getUserId());
            entity.setUpdateUserId(dto.getUserId());
            return this.save(entity);
        }
        return true;
    }


    /**
     * 获取用户设置的字段
     *
     * @param dto
     * @return java.util.List<com.erp.model.sys.dto.CustomizeFieldHiddenDTO>
     * @author yl
     * @date 2023-02-08 16:23
     */
    @Override
    public UserFieldVO getByUserId(FindCustomizeFieldDTO dto) {
        LambdaQueryWrapper<CustomizeFieldLayoutEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CustomizeFieldLayoutEntity::getModuleCode, dto.getModuleCode());
        queryWrapper.eq(CustomizeFieldLayoutEntity::getUserId, dto.getUserId());
        queryWrapper.orderByDesc(CustomizeFieldLayoutEntity::getCreateTime);
        queryWrapper.last("LIMIT 1");
        CustomizeFieldLayoutEntity entity = this.getOne(queryWrapper);
        UserFieldVO result = new UserFieldVO();
        if(entity!=null){
            result.setModuleName(entity.getModuleName());
            result.setModuleCode(entity.getModuleCode());
            result.setLayoutJson(entity.getLayoutJson());
        }

        return result;
    }


}
