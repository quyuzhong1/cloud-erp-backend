package com.erp.server.tms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.LogisticsAddressDTO;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 物流地址表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Mapper
public interface LogisticsAddressMapper extends BaseMapper<LogisticsAddressEntity> {

    IPage<LogisticsAddressDTO.PagingViewDTO> paging(Page query,@Param("params") LogisticsAddressDTO.PagingParamDTO params);

    /**
     * 查询导出数据
     * @author yl
     * @date 2023-11-08 17:42
     * @return java.util.List<com.erp.model.tms.dto.LogisticsAddressDTO.PagingViewDTO>
     */
    List<LogisticsAddressDTO.PagingViewDTO> listExport(@Param("params")LogisticsAddressDTO.ExportDTO dto);
    Page<LogisticsAddressDTO.PagingViewDTO> listExport(@Param("page") Page<LogisticsAddressDTO.PagingViewDTO> page, @Param("params")LogisticsAddressDTO.ExportDTO dto);

    /**
     * 查询地址信息
     * @param type
     * @param channelId
     * @return
     */
    List<LogisticsAddressEntity> listByTypeAndChannelId(@Param("type") String type,@Param("channelId") String channelId);

    List<LogisticsAddressEntity> listByChannelId(@Param("channelId") String channelId);
}
