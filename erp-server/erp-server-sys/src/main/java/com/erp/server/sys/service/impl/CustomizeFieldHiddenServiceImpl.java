package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.dto.CustomizeFieldHiddenDTO;
import com.erp.model.sys.dto.FindCustomizeFieldDTO;
import com.erp.model.sys.entity.CustomizeFieldHiddenEntity;
import com.erp.server.sys.mapper.CustomizeFieldHiddenMapper;
import com.erp.server.sys.service.CustomizeFieldHiddenService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * (CustomizeFieldDisplay)表服务实现类
 *
 * @author yl
 * @since 2023-02-03 18:59:41
 */
@Service
public class CustomizeFieldHiddenServiceImpl extends ServiceImpl<CustomizeFieldHiddenMapper, CustomizeFieldHiddenEntity> implements CustomizeFieldHiddenService {

    @Override
    public Boolean add(List<CustomizeFieldHiddenDTO> dtoList) {
        if (CollectionUtils.isNotEmpty(dtoList)) {
            List<CustomizeFieldHiddenEntity> saveList = new ArrayList<>();
            for (CustomizeFieldHiddenDTO dto : dtoList) {
                CustomizeFieldHiddenEntity entity = new CustomizeFieldHiddenEntity();
                BeanMapper.copy(dto, entity);
                entity.setCreateTime(new Date());
                entity.setUpdateTime(new Date());
                entity.setCreateUserId(dto.getUserId());
                entity.setUpdateUserId(dto.getUserId());
                saveList.add(entity);
            }
            return this.saveBatch(saveList);
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
    public List<CustomizeFieldHiddenDTO> getByUserId(FindCustomizeFieldDTO dto) {
        LambdaQueryWrapper<CustomizeFieldHiddenEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CustomizeFieldHiddenEntity::getModuleCode, dto.getModuleCode());
        queryWrapper.eq(CustomizeFieldHiddenEntity::getUserId, dto.getUserId());
        List<CustomizeFieldHiddenEntity> list = this.list(queryWrapper);
        List<CustomizeFieldHiddenDTO> resultList = new ArrayList<>(list.size());
        resultList = BeanMapper.copyList(list, CustomizeFieldHiddenDTO.class);
        return resultList;
    }


}
