package com.erp.server.tms.mapper;

import com.erp.model.tms.entity.InitFirstMileAllocationDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 期初头程分摊明细 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2024-08-13
 */
@Mapper
public interface InitFirstMileAllocationDetailMapper extends BaseMapper<InitFirstMileAllocationDetailEntity> {
    /**
     * 根据来源id和状态进行查询数据
     * @param sourceIds
     * @param status
     * @return
     */
    List<InitFirstMileAllocationDetailEntity> listBySourceIds(@Param("sourceIds") List<String> sourceIds, @Param("status") String status);
}
