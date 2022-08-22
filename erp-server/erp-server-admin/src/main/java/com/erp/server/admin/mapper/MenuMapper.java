package com.erp.server.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.admin.entity.MenuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Classname MenuMapper
 * @Description TODO
 * @Date 2022-08-22 10:31
 * @Created by yl
 */
@Mapper
public interface MenuMapper  extends BaseMapper<MenuEntity> {
    void removeRoleMenuByMenuIds(@Param("menuIds") List<String> menuIds);
}
