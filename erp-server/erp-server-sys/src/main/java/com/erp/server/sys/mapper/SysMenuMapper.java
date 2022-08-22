package com.erp.server.sys.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.sys.entity.SysMenuEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜单表
 * 
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-11 14:05:47
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenuEntity> {
	
}
