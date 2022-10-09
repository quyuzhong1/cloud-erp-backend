package com.erp.server.plm.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.entity.UserAddProductEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @Entity .com.erp.model.plm.entity.UserAddProductEntity
 */
@Mapper
public interface UserAddProductEntityMapper extends BaseMapper<UserAddProductEntity> {

    List<Map<String, Object>> listByUserId(String uid);
}




