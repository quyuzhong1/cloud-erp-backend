package com.erp.server.tms.mapper;

import com.erp.model.tms.dto.ReportPeriodMonthDTO;
import com.erp.model.tms.entity.ReportPeriodMonthEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 核算期间月份表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2024-08-20
 */
@Mapper
public interface ReportPeriodMonthMapper extends BaseMapper<ReportPeriodMonthEntity> {
    /**
     * 根据组织获取未确认核算期间列表
     * @param orgIds
     * @return
     */
    List<ReportPeriodMonthDTO.SelectDTO> queryList(@Param("orgIds") List<String> orgIds);
}
