package com.erp.server.sys.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.common.business.dto.FindUserDTO;
import com.erp.model.sys.dto.FindUserByThirdDTO;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.model.sys.entity.SysUserThirdEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;

/**
 * ${comments}
 * 
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-11 14:05:47
 */
@Mapper
public interface SysUserThirdMapper extends BaseMapper<SysUserThirdEntity> {

    SysUserInfoEntity getUserIdByThird(@Param("params") FindUserByThirdDTO thirdDTO);

    List<FindUserDTO> listThirdBindUser();

    SysUserThirdEntity getUserByThird(@Param("platform")String platform, @Param("thirdId")String thirdId);

    List<SysUserThirdEntity> getUserByThirdList(@Param("platform")String platform, @Param("thirdIds")ArrayList<String> thirdIds);
}
