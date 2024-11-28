package com.erp.server.tms.mapper;
import com.common.business.enums.OmsPlatformEnum;
import com.erp.model.tms.dto.LogisticsAuthDTO;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 物流授权表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Mapper
public interface LogisticsAuthMapper extends BaseMapper<LogisticsAuthEntity> {

    LogisticsSupplierDTO.AuthDTO getAuthByChannelId(@Param("channelId") String channelId);

    List<LogisticsSupplierDTO.AuthChannelViewDTO> listAuthChannelView(@Param("channelIds") List<String> channelIds);

    LogisticsSupplierDTO.AuthDTO getAuthBySupplierId(@Param("logisticsSupplierId") String logisticsSupplierId);

    /**
     * 查询所有渠道
     * @param list 平台
     */
    List<String> listAllChannelByOverseas(@Param("list") List<String> list);
}
