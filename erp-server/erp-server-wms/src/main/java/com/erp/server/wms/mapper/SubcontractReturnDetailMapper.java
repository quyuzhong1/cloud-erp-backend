package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.SubcontractReturnDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 委外退料明细单 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2024-09-15
 */
@Mapper
public interface SubcontractReturnDetailMapper extends BaseMapper<SubcontractReturnDetailEntity> {

    /**
     *根据委外订单明细id集合查询
     * @param subcontractOrderDetailIdList
     * @return
     */
    List<SubcontractReturnDetailEntity> listBySubcontractOrderDetailIdList(@Param("subcontractOrderDetailIdList")List<String> subcontractOrderDetailIdList);
}
