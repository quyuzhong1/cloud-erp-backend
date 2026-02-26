package com.erp.server.tms.mapper;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 异步任务记录 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2026-01-28
 */
@Mapper
public interface AsyncTaskRecordMapper extends BaseMapper<TmsAsyncTaskRecordEntity> {

}
