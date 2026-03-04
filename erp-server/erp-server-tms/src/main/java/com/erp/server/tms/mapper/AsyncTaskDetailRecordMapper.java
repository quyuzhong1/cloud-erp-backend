package com.erp.server.tms.mapper;
import com.erp.model.tms.entity.AsyncTaskDetailRecordEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.tms.dto.AsyncTaskDetailRecordDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
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
public interface AsyncTaskDetailRecordMapper extends BaseMapper<AsyncTaskDetailRecordEntity> {

}
