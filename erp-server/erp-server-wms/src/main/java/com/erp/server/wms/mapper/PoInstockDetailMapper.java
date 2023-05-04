package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.entity.PoInstockDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 采购入库明细表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-04-10
 */
@Mapper
public interface PoInstockDetailMapper extends BaseMapper<PoInstockDetailEntity> {
    Integer getStockInQty(@Param("purchaseOrderDetailId") String purchaseOrderDetailId);
    /**
     * 根据来源明细ids查询
     */
    List<PoInstockDetailEntity> listDetailBySourceDetailIds(@Param("sourceDetailIds") List<String> sourceDetailIds);
    /**
     * 根据采购明细ids查询
     */
    List<PoInstockDetailEntity> listDetailByPodIds(@Param("podIds") List<String> podIds);
}
