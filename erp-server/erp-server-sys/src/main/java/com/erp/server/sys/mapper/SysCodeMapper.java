package com.erp.server.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.sys.entity.SysCodeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/21 11:35
 */
@Mapper
public interface SysCodeMapper extends BaseMapper<SysCodeEntity> {

    int updateNum(@Param(value = "id") String id, @Param(value = "updateTime") LocalDateTime updateTime, @Param(value = "updateUserId") String updateUserId, @Param(value = "updateUserName") String updateUserName);

}
