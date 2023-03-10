package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.entity.RoleRefMemberEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * @Entity .com.erp.model.plm.entity.RoleRefMember
 */
@Mapper
public interface RoleRefMemberMapper extends BaseMapper<RoleRefMemberEntity> {

    /**
     * 根据用户获取项目组角色
     * @author yl
     * @date 2023-03-10 9:28
     * @param userId
     * @param productId
     * @return java.util.List<java.lang.String>
     */
    List<String> getUserRole(@Param("userId") String userId,@Param("productId") String productId);
}




