package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PermissionsDTO;
import com.erp.model.wms.dto.QcInfoDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.dto.WarehouseReceiveExcelDTO;
import com.erp.model.wms.entity.WarehouseReceiveEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 *  采购收货单Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-04-06
 */
@Mapper
public interface WarehouseReceiveMapper extends BaseMapper<WarehouseReceiveEntity> {

    IPage<WarehouseReceiveDTO.PagingViewDTO> paging(Page query, @Param("params") WarehouseReceiveDTO.PagingParamDTO params);
    IPage<WarehouseReceiveDTO.PagingViewDTO> pageDetail(Page query, @Param("params") WarehouseReceiveDTO.PagingParamDTO params);
    Integer getCount();

    Integer getQty(@Param("sourceId") String sourceId, @Param("sourceDetailId") String sourceDetailId);
    Integer getReceiveQtyById(@Param("id") String id);

    List<WarehouseReceiveDTO.WarehouseReceiveCountDTO> listCount(@Param("params") PermissionsDTO params);

    List<WarehouseReceiveDTO.GetReceiveDTO> getReceiveQty(@Param("purchaseOrderId") String purchaseOrderId);

    List<WarehouseReceiveDTO.OrderRefReceiveDTO> purchaseOrderRefReceive(@Param("purchaseOrderId") String purchaseOrderId);

    List<WarehouseReceiveDTO.GenerateStockInViewDTO> generateStockInView(@Param("ids") List<String> ids);

    List<WarehouseReceiveExcelDTO> warehouseReceiveExportExcel(@Param("params") WarehouseReceiveDTO.PagingParamDTO params);
    Page<WarehouseReceiveExcelDTO> warehouseReceiveExportExcel(@Param("page") Page<WarehouseReceiveExcelDTO> page, @Param("params") WarehouseReceiveDTO.PagingParamDTO params);

    List<QcInfoDTO.ReceiveToQcDTO> getQcList(@Param("mainIds") List<String> mainIds);

    /**
     * 根据供应商id集合查询收货批次和收货数量
     */
    List<WarehouseReceiveDTO.SupplierReceiveInfoDTO> getReceiveInfoBySupplierIds(@Param("supplierIds") List<String> supplierIds, @Param("dateList") List<LocalDate> dateList);

    /**
     * PDA:首页分页查询
     * @Author Luo_WG
     * @Date 2023/8/11 9:28
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.wms.dto.WarehouseReceiveDTO.PdaPagingViewDTO>
     **/
    IPage<WarehouseReceiveDTO.PdaPagingViewDTO> pdaPaging(Page query, @Param("params") WarehouseReceiveDTO.PdaPagingParamDTO params);

    /**
     * PDA:列表tab页单据数量
     * @Author Luo_WG
     * @Date 2023/8/11 9:37
     * @param params
     * @return java.lang.Integer
     **/
    Integer pdaListCount(@Param("params") WarehouseReceiveDTO.PagingParamDTO params);

    /**
     * PDA:条件查询收货单
     * @Author Luo_WG
     * @Date 2023/8/18 11:10
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.WarehouseReceiveDTO.PdaPoReceive>
     **/
    List<WarehouseReceiveDTO.PdaPoReceive> pdaList(WarehouseReceiveDTO.PdaPoReceiveParam dto);

    /**
     * PDA:待入库查询
     * @Author Luo_WG
     * @Date 2023/9/6 11:09
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.wms.dto.WarehouseReceiveDTO.WaitInStockPaging>
     **/
    IPage<WarehouseReceiveDTO.WaitInStockPaging> pdaWaitInStockPaging(Page query, @Param("params") WarehouseReceiveDTO.WaitInStockPagingParam params);

    /**
     * PDA:待入库查询表头数量
     * @Author Luo_WG
     * @Date 2023/9/6 14:56
     * @param params
     * @return java.lang.Integer
     **/
    Integer waitInStockListCount(@Param("params") WarehouseReceiveDTO.PagingParamDTO params);

    /**
     *  汇总供应商 当前周期内已确认订单数量
     * @param supplierId
     * @param startTime
     * @param endTime
     * @return
     */
    Integer countOrderBySupplierId(@Param(value = "supplierId") String supplierId,@Param(value = "startTime") LocalDate startTime,@Param(value = "endTime") LocalDate endTime);

    /**
     * 根据采购订单获取收货明细列表
     * @param purchaseOrderIds
     * @return
     */
    List<WarehouseReceiveDTO.PurchaseOrderDetailDTO> getReceiveListByPurchaseOrderIds(@Param(value = "purchaseOrderIds") List<String> purchaseOrderIds);

    List<WarehouseReceiveDTO.PurchaseOrderDetailDTO> getReceiveListByPurchaseOrderIdsAll(@Param(value = "purchaseOrderIds") List<String> purchaseOrderIds);

    WarehouseReceiveDTO.PagingTotalDTO pagingTotal(@Param("params") WarehouseReceiveDTO.PagingParamDTO dto);
    /**
     * 根据ids查询收货信息
     * @author will 
     * @date 2025/6/12 10:39
     * @param idList 
     * @return List<ReceiveSourceDTO>
     */
    List<WarehouseReceiveDTO.ReceiveSourceDTO> listReceiveSourceByDetailIds(@Param("idList")List<String> idList);

    List<WarehouseReceiveDTO.ReceiveInfoDTO> getReceiveByParams(@Param("params") WarehouseReceiveDTO.ReceiveParamDTO params);
}
