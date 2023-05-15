package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.oms.entity.SoOutstockDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 销售订单出库明细 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Mapper
public interface SoOutstockDetailMapper extends BaseMapper<SoOutstockDetailEntity> {
    /**
     * 根据来源明细id查询出库表
     * @Author Luo_WG
     * @Date 2023/5/15 15:14
     * @param sourceDetailIds sourceDetailIds
     * @return java.util.List<com.erp.model.oms.entity.SoOutstockDetailEntity>
     **/
    List<SoOutstockDetailEntity> listSoOutstockBySourceDetailId(@Param("sourceDetailIds") List<String> sourceDetailIds);
}
