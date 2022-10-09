package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.dto.RoleDTO;
import com.erp.model.plm.entity.RoleEntity;
import com.erp.server.plm.mapper.RoleMapper;
import com.erp.server.plm.service.RoleService;
import org.springframework.stereotype.Service;

/**
 * @Classname RoleServiceImpl
 * @Description TODO
 * @Date 2022-10-09 19:48
 * @Created by yl
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, RoleEntity> implements RoleService {
    @Override
    public Boolean saveRole(RoleDTO dto) {
        return null;
    }
}
