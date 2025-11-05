package com.erp.server.workflow.mapper;
import com.erp.model.workflow.entity.ApproveTaskDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 三方生成查询明细 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2025-05-27
 */
@Mapper
public interface ApproveTaskDetailMapper extends BaseMapper<ApproveTaskDetailEntity> {
    /**
     * 根据主表id查询
     * @author will
     * @date 2025/11/3 11:25
     * @param mainId
     * @return List<ApproveTaskDetailEntity>
     */
    List<ApproveTaskDetailEntity> listByMainId(@Param("mainId") String mainId);
}
