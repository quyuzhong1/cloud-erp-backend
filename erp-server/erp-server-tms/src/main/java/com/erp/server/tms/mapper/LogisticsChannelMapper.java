package com.erp.server.tms.mapper;
import com.common.business.dto.base.BaseIdDTO;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
    List<LogisticsChannelDTO.ListSelectDTO> listLogisticsChannel(@Param("logisticsSupplierIds") List<String> logisticsSupplierIds);

    /**
     * 根据供应商id获取到 对应渠道下 启用禁用的列表
     *@parms supplierId
     *@return 
     *@author yl
     *@date 2023-11-20
     */
    List<BaseIdDTO.CodeDTO> listBySupplierId(@Param("supplierId") String supplierId);

    /** 根据地址id获取数据
     *
     *@parms addressId
     *@return 
     *@author yl
     *@date 2023-11-20
     */
    List<LogisticsChannelEntity> listByAddressId(@Param("addressId") String addressId);
}
