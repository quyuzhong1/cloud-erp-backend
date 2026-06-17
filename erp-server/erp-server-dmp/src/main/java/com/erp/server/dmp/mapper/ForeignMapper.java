package com.erp.server.dmp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.dmp.entity.AmzReportScheduleEntity;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 外部表 Mapper 接口
 * </p>
 *
 * @author Jim
 * @since 2024-01-18
 */
@Mapper
public interface ForeignMapper extends BaseMapper<Object> {

    List<LogisticsTrackDTO.UpdateTrackDTO> listTrackDto(@Param("query") LogisticsBillDetailQueryDTO query);
}
