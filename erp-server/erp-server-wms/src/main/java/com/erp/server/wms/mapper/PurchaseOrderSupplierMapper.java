package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PurchaseOrderSupplierMapper extends BaseMapper<PurchaseOrderSupplierEntity> {
    /**
     * 根据ids查询明细信息
     * @Author Luo_WG
     * @Date 2023/6/19 15:15
     * @param ids
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderDetailEntity>
     **/
    List<PurchaseOrderSupplierEntity> listByIds(@Param("ids") List<String> ids);

    /**
     * 批量修改
     * @Author Luo_WG
     * @Date 2023/6/19 15:15
     * @param list
     * @return int
     **/
    int updateBatchSelective(List<PurchaseOrderSupplierEntity> list);
}
