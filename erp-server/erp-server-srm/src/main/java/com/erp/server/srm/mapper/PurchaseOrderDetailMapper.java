package com.erp.server.srm.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.dto.PurchaseOrderSrmDTO;
import com.erp.model.srm.entity.PurchaseOrderDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 采购订单明细表（已确认） Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2024-01-27
 */
@Mapper
public interface PurchaseOrderDetailMapper extends BaseMapper<PurchaseOrderDetailEntity> {
    /**
     * srm 分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<PurchaseOrderDTO.ListDTO> paging(Page query, @Param("params") PurchaseOrderDTO.SrmSearchParamDTO params);
    /**
     * 汇总待发货订单明细数量
     * @param supplierId
     * @return
     */
    PurchaseOrderSrmDTO.WaitDeliveryCountDTO srmWaitDeliveryCount(@Param("supplierId")String supplierId);
    /**
     * 查询确认订单列表
     * @param params
     * @return
     */
    List<PurchaseOrderDTO.ListDTO> srmPurchaseOrderList(@Param("params") PurchaseOrderDTO.SrmSearchParamDTO params);

}
