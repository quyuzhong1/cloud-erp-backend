package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.LogisticsThirdChannelRefDTO;
import com.erp.model.tms.entity.LogisticsThirdChannelRefEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 物流-第三方渠道关系表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2025-05-29
 */
@Mapper
public interface LogisticsThirdChannelRefMapper extends BaseMapper<LogisticsThirdChannelRefEntity> {

    /**
     * 分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<LogisticsThirdChannelRefDTO.PagingVO> paging(Page<Object> query, LogisticsThirdChannelRefDTO.PagingParamDTO params);

    List<LogisticsThirdChannelRefDTO.PagingVO> listByPlatform(@Param("platformType") String platformType);

    /**
     * 根据物流渠道id获取已启用查询配置
     */
    List<LogisticsThirdChannelRefDTO.PagingVO> listByChannelId(@Param("channelId") String channelId);

    List<LogisticsThirdChannelRefEntity> existRefBySalePlatform(@Param("salePlatform") String salePlatform, @Param("channelId")String channelId,@Param("logisticsSupplierId") String logisticsSupplierId);

    /**
     * 根据销售平台、物流渠道、物流商获取轨迹查询方式
     */
    String getTrackQueryModeBySalePlatform(@Param("salePlatform") String salePlatform, @Param("channelId") String channelId, @Param("logisticsSupplierId") String logisticsSupplierId);

    /**
     * 根据单号批量查询轨迹配置信息
     *
     * @param trackNos 单号列表
     * @return 配置映射列表
     * @author jack
     * @date 2026-04-02
     */
    List<LogisticsThirdChannelRefDTO.ListByTrackNosDTO> listByTrackNos(@Param("trackNos") List<String> trackNos);
}
