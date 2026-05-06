package com.erp.server.tms.mapper;
import com.erp.model.tms.entity.TmsAsyncTaskDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 异步任务记录明细 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2026-01-28
 */
@Mapper
public interface AsyncTaskDetailRecordMapper extends BaseMapper<TmsAsyncTaskDetailEntity> {

}
