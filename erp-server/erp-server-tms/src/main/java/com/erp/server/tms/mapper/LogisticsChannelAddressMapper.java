package com.erp.server.tms.mapper;
import com.erp.model.tms.dto.LogisticsChannelAddressDTO;
import com.erp.model.tms.entity.LogisticsChannelAddressEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 渠道地址表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Mapper
public interface LogisticsChannelAddressMapper extends BaseMapper<LogisticsChannelAddressEntity> {
   
    /**
     * 根据渠道id查询数据
     *@parms channelId
     *@return 
     *@author yl
     *@date 2023-11-14
     */
    List<LogisticsChannelAddressDTO.ViewDTO> listByChannelId(@Param("channelId") String channelId);
}
