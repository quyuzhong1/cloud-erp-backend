package com.erp.server.tms.mapper;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 物流轨迹表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2023-11-14
 */
@Mapper
public interface LogisticsTrackMapper extends BaseMapper<LogisticsTrackEntity> {

    /**
     * 查询3个月无物流轨迹记录数据
     * @param query
     * @return
     */
    List<LogisticsTrackDTO.UpdateTrackDTO> listBeforeThreeMonthTrack(@Param("query") LogisticsBillDetailQueryDTO query);
}
