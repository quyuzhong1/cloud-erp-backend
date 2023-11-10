package com.erp.server.tms.mapper;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * <p>
 * 物流渠道表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Mapper
public interface LogisticsChannelMapper extends BaseMapper<LogisticsChannelEntity> {
    /**
     * @description: 物流渠道列表
     * @author Will
     * @date: 2023/11/10 10:08
     * @return List<ListSelectDTO>
     */
    List<LogisticsChannelDTO.ListSelectDTO> listLogisticsChannel();
}
