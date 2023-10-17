package com.erp.server.dmp.mapper;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 中台同步任务表 Mapper 接口
 * </p>
 *
 * @author Cloud
 * @since 2023-09-06
 */
@Mapper
public interface DmpPushTaskMapper extends BaseMapper<DmpPushTaskEntity> {

    /**
     * 根据条件查询数据
     *
     * @param params
     * @return
     */
    DmpPushTaskEntity getEntityByCondition(@Param("params") DmpPushTaskEntity params);
}
