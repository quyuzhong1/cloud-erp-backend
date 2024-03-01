package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.model.scm.dto.SkuCostDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Mapper
public interface PurchaseOrderDetailMapper extends BaseMapper<PurchaseOrderDetailEntity> {

    /**
     * @description: 添加产品数据显示
     * @author Will
     * @date: 2023/4/14 10:33
     * @param params
     * @return ViewProductDTO
     */
    List<PurchaseOrderDetailDTO.ViewProductDTO> viewProduct(@Param("params") PurchaseOrderDetailDTO.ProductSearchParamDTO params);
    /**
     * @description: 根据来源明细ids查询
     * @author Will
     * @date: 2023/6/19 15:50
     * @param sourceDetailIds
     * @return List<PurchaseOrderDetailEntity>
     */
    List<PurchaseOrderDetailEntity> listBySourceDetailIds(@Param("sourceDetailIds") List<String> sourceDetailIds);

    /**
     * 根据sku id集合获取最新的一个审核通过的采购订单，按采购日期倒序
     * @param skuIds
     * @return
     */
    List<PurchaseOrderDetailEntity> getLatest(@Param(value = "skuIds") List<String> skuIds);
    /**
     * 根据sku id集合获取最新的一个审核通过的采购订单，按创建日期倒序
     * @param skuIds
     * @return
     */
    List<PurchaseOrderDetailEntity> getLatestByCrtTime(@Param(value = "skuIds") List<String> skuIds);

    /**
     *  汇总供应商 当前周期内已确认订单数量
     * @param supplierId
     * @param startTime
     * @param endTime
     * @return
     */
    Integer countOrderBySupplierId(@Param(value = "supplierId") String supplierId,@Param(value = "startTime") LocalDate startTime,@Param(value = "endTime") LocalDate endTime);
}
