package com.erp.server.dmp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.dmp.entity.ApiPlmSyncLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author Will
 * @version 1.0

 * @date 2023/1/11 11:40
 */
@Repository
@Mapper
public interface ApiPlmSyncLogMapper extends BaseMapper<ApiPlmSyncLogEntity> {
}
