package com.erp.server.dmp.mapper;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;


/**
 * <p>
 * 中台同步任务表 Mapper 接口
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
 */
@Repository
@Mapper
public interface DmpPullTaskMapper extends BaseMapper<DmpPullTaskEntity> {


}
