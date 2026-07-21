package com.erp.server.tms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 异步任务记录 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2026-01-28
 */
@Mapper
public interface TmsAsyncTaskRecordMapper extends BaseMapper<TmsAsyncTaskRecordEntity> {

    List<TmsAsyncTaskRecordDTO.TabListDTO> tabList(@Param("params") TmsAsyncTaskRecordDTO.PagingParamDTO searchParam);

    IPage<TmsAsyncTaskRecordDTO.ListDTO> paging(Page query,@Param("params") TmsAsyncTaskRecordDTO.PagingParamDTO params);

    IPage<TmsAsyncTaskRecordDTO.DetailListDTO> pagingError(Page query, @Param("id") String id);

    Integer isExist(@Param("businessType")String businessType, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 限量查询已超时的执行中主任务，供 watchdog 兜底终止。
     */
    List<TmsAsyncTaskRecordEntity> listTimedOutTasksForWatchdog(@Param("defaultExecTimeout") int defaultExecTimeout,
                                                                @Param("now") LocalDateTime now,
                                                                @Param("limit") int limit);

    /**
     * 在事务内抢占来源任务的错误重试资格，避免旧实体重复创建重试任务。
     */
    int claimErrorRetry(@Param("id") String id,
                        @Param("finishStatus") String finishStatus);
}
