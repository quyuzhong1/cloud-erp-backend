package com.cloud.erp.admin.modules.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.erp.admin.modules.sys.entity.SysRoleUserEntity;
import com.cloud.erp.admin.modules.sys.vo.SysUserVO;
import com.erp.common.dto.base.BaseSearchDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ${comments}
 * 
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-08 11:23:00
 */
@Mapper
public interface SysRoleUserMapper extends BaseMapper<SysRoleUserEntity> {

    List<SysUserVO> findRoleUser(@Param("params") BaseSearchDTO dto);
}
