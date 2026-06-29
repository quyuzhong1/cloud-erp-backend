package com.erp.server.tms.mapper;
import com.erp.model.tms.entity.TmsAsyncTaskDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * 限量查询已完成主任务下仍为 ING 的明细 ID。
     */
    List<String> listOrphanIngDetailIds(@Param("limit") int limit);

    /**
     * 限量查询指定任务类型下已僵死的 ING 明细 ID。
     */
    List<String> listStaleIngDetailIdsByTaskType(@Param("businessType") String businessType,
                                                 @Param("methodType") String methodType,
                                                 @Param("staleBefore") LocalDateTime staleBefore,
                                                 @Param("limit") int limit);
}
