package com.erp.server.dmp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.dmp.entity.ApiSyncTaskEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Will
 * @version 1.0
 * @description: 推送任务
 * @date 2023/7/10 16:09
 */
@Mapper
public interface ApiSyncTaskMapper extends BaseMapper<ApiSyncTaskEntity> {

}
