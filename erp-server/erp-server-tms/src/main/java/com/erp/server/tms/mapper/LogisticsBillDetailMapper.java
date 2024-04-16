package com.erp.server.tms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 物流单明细表 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
 */
@Mapper
public interface LogisticsBillDetailMapper extends BaseMapper<LogisticsBillDetailEntity> {

    IPage<LogisticsBillDetailEntity> getTrackPage(@Param("page") Page<LogisticsBillDetailEntity> page, @Param("query") LogisticsBillDetailQueryDTO query);
    IPage<LogisticsTrackDTO.UpdateTrackDTO> getTrackDtoPage(@Param("page") Page<LogisticsTrackDTO.UpdateTrackDTO> page, @Param("query") LogisticsBillDetailQueryDTO query);

}
