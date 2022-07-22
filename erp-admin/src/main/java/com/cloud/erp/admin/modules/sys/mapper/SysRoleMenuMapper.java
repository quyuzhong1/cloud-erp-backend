package com.cloud.erp.admin.modules.sys.mapper;

import com.cloud.erp.admin.modules.sys.entity.SysRoleMenuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ${comments}
 * 
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-11 14:05:47
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenuEntity> {

    List<String> findMenuIdsByRoleIds(@Param("roleIds") List<String> roleIds);

    List<String> findMenuCodeByRoleIds(@Param("roleIds") List<String> roleIds,@Param("type") Integer type);

    List<String> findAllMenuCode(@Param("type") Integer type);
}
