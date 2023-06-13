package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 采购订单表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Mapper
public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrderEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/3/27 12:33
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<PurchaseOrderDTO.ListDTO> paging(Page query,@Param("params") PurchaseOrderDTO.SearchParamDTO params);
    /**
     * @description: 导出查询数据
     * @author Will
     * @date: 2023/3/27 16:01
     * @param params
     * @return List<PurchaseOrderDTO.ListDTO>
     */
    List<PurchaseOrderDTO.ListDTO> listExportExcel(@Param("params") PurchaseOrderDTO.SearchParamDTO params);
    /**
     * @description: 查询列表数量
     * @author Will
     * @date: 2023/3/29 10:11
     * @param params
     * @return Integer
     */
    Integer listCount(@Param("params") PurchaseOrderDTO.SearchParamDTO params);

    /**
     * 下推收货单列表
     * @Author Luo_WG
     * @Date 2023/4/18 18:06
     * @param ids ids
     * @return java.util.List<com.erp.model.scm.dto.PurchaseOrderDTO.ViewGenerateReceiveDTO>
     **/
    List<PurchaseOrderDTO.ViewGenerateReceiveDTO> viewGenerateReceive(@Param("ids") List<String> ids);

    /**
     * 获取订单信息
     * @author yl
     * @date 2023-04-23 14:10
     * @param purchaseOrderIds
     * @return java.util.List<com.erp.model.scm.dto.PurchaseOrderDTO.GetOneDTO>
     */
    List<PurchaseOrderDTO.PurchaseOrderInfoDTO> getPurchaseOrderByOrderIds(@Param("purchaseOrderIds") List<String> purchaseOrderIds);

    /**
     * 根据采购订单id 获取下推数据显示
     * @author yl
     * @date 2023-04-25 9:43
     * @param ids
     * @return com.erp.model.wms.dto.PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO
     */
    List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO> viewGeneratePurchaseReturnOrder(@Param("purchaseOrderIds") List<String> ids);
    /**
     * @description: 根据来源明细ids查询
     * @author Will
     * @date: 2023/6/13 15:38
     * @param sourceDetailIds
     * @return List<ListDTO>
     */
    List<PurchaseOrderDTO.ListDTO> listBySourceDetailIds(@Param("sourceDetailIds") List<String> sourceDetailIds);
}
