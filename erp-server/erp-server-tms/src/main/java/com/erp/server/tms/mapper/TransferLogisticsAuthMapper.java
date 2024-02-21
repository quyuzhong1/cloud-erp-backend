package com.erp.server.tms.mapper;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.dto.TransferLogisticsSupplierDTO;
import com.erp.model.tms.entity.TransferLogisticsAuthEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 物流授权表 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
@Mapper
public interface TransferLogisticsAuthMapper extends BaseMapper<TransferLogisticsAuthEntity> {

    /**
     * 根据渠道id查询授权信息
     * @Author Luo_WG
     * @Date 2024/1/19 12:25
     * @param channelId
     * @return com.erp.model.tms.dto.TransferLogisticsSupplierDTO.AuthDTO
     **/
    TransferLogisticsSupplierDTO.AuthDTO getAuthByChannelId(@Param("channelId") String channelId);
}
