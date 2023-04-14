package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.dto.PurchaseOrderSupplierDTO;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
public interface PurchaseOrderSupplierMapper extends BaseMapper<PurchaseOrderSupplierEntity> {

    /**
     * 获取采购记录
     * @author yl
     * @date 2023-04-03 16:53
     * @param query
     * @param supplierId
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.scm.dto.PurchaseOrderSupplierDTO.SupplierPurchaseDTO>
     */
    IPage<PurchaseOrderSupplierDTO.SupplierPurchaseDTO> supplierPurchasePaging(Page query, @Param("supplierId") String supplierId);

    /**
     * 根据供应商id  查出是否有关联的订单
     * @author yl
     * @date 2023-04-14 12:01
     * @param supplierIds
     * @return int
     */
    int getRefSupplierCount(@Param("supplierIds") List<String> supplierIds);
}
