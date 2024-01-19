package com.erp.server.tms.mapper;
import com.common.business.dto.base.BaseIdDTO;
import com.erp.model.tms.dto.TransferLogisticsChannelDTO;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 中转报关服务商渠道表 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
@Mapper
public interface TransferLogisticsChannelMapper extends BaseMapper<TransferLogisticsChannelEntity> {

    /**
     * 根据中转服务商id查询渠道
     * @Author Luo_WG
     * @Date 2024/1/19 14:37
     * @param logisticsSupplierIds
     * @return java.util.List<com.erp.model.tms.dto.TransferLogisticsChannelDTO.ListSelectDTO>
     **/
    List<TransferLogisticsChannelDTO.ListSelectDTO> listLogisticsChannel(@Param("logisticsSupplierIds") List<String> logisticsSupplierIds);

    /**
     * 根据供应商id 获取启用禁用的列表
     * @Author Luo_WG
     * @Date 2024/1/19 14:56
     * @param supplierId
     * @return java.util.List<com.common.business.dto.base.BaseIdDTO.CodeDTO>
     **/
    List<BaseIdDTO.CodeDTO> listBySupplierId(@Param("supplierId") String supplierId);
}
