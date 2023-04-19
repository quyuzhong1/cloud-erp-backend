package com.erp.server.wms.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.PurchaseStockInDTO;
import com.erp.model.wms.entity.PurchaseStockInEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 采购入库单 服务类
 * </p>
 *
 * @author will
 * @since 2023-04-10
 */
public interface PurchaseStockInService extends SuperService<PurchaseStockInEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/4/12 11:40
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<PurchaseStockInDTO.ListDTO> paging(PagingDTO<PurchaseStockInDTO.SearchParamDTO> dto);
    /**
     * @description: 查询数量
     * @author Will
     * @date: 2023/4/12 11:43
     * @param dto
     * @return List<ListStatusCountDTO>
     */
    List<PurchaseStockInDTO.ListStatusCountDTO> listCount(PermissionsDTO dto);
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/4/12 11:41
     * @param dto
     * @return String
     */
    String add(PurchaseStockInDTO.AddDTO dto);
    /**
     * @description: 新增并提交
     * @author Will
     * @date: 2023/4/12 11:44
     * @param dto
     * @return Boolean
     */
    Boolean addAndSubmit(PurchaseStockInDTO.AddDTO dto);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/4/12 11:45
     * @param dto
     * @return Boolean
     */
    Boolean update(PurchaseStockInDTO.UpdateDTO dto);
    /**
     * @description: 修改并提交
     * @author Will
     * @date: 2023/4/12 11:46
     * @param dto
     * @return Boolean
     */
    Boolean updateAndSubmit(PurchaseStockInDTO.UpdateDTO dto);
    /**
     * @description: 提交
     * @author Will
     * @date: 2023/4/12 11:46
     * @param ids
     * @return Boolean
     */
    Boolean submit(List<String> ids);
    /**
     * @description: 查看
     * @author Will
     * @date: 2023/4/12 11:55
     * @param id
     * @return ViewDTO
     */
    PurchaseStockInDTO.ViewDTO view(String id);
    /**
     * @description: 删除
     * @author Will
     * @date: 2023/4/12 11:56
     * @param ids
     * @return Boolean
     */
    Boolean delete(List<String> ids);
    /**
     * @description: 作废
     * @author Will
     * @date: 2023/4/12 11:57
     * @param ids
     * @param remark
     * @return Boolean
     */
    Boolean invalid(List<String> ids, String remark);
    /**
     * @description: 审核
     * @author Will
     * @date: 2023/4/12 11:57
     * @param baseApproveParamDTO
     */
    void approve(BaseApproveParamDTO baseApproveParamDTO);
    /**
     * @description: 反审核
     * @author Will
     * @date: 2023/4/12 11:58
     * @param ids
     * @return Boolean
     */
    Boolean disApprove(List<String> ids);
    /**
     * @description: 取消流程
     * @author Will
     * @date: 2023/4/12 11:59
     * @param ids
     * @return Boolean
     */
    Boolean cancelProcess(List<String> ids);
    /**
     * @description: 导出
     * @author Will
     * @date: 2023/4/12 12:00
     * @param dto
     * @param response
     * @return Boolean
     */
    Boolean exportExcel(PurchaseStockInDTO.SearchParamDTO dto, HttpServletResponse response);
    /**
     * @description: 下推退货单数据显示
     * @author Will
     * @date: 2023/4/12 12:02
     * @param ids
     * @return List<ViewGeneratePurchaseReturnOrderDTO>
     */
    List<PurchaseStockInDTO.ViewGeneratePurchaseReturnOrderDTO> viewGeneratePurchaseReturnOrder(List<String> ids);
    /**
     * @description: 下推退货单数据保存
     * @author Will
     * @date: 2023/4/12 12:02
     * @param dto
     * @return Boolean
     */
    Boolean generatePurchaseReturnOrder(PurchaseStockInDTO.ListGeneratePurchaseReturnOrderDTO dto);

    /**
     * 根据来源Id查询入库单
     * @Author Luo_WG
     * @Date 2023/4/18 10:30
     * @param sourceId sourceId
     * @return com.erp.model.wms.entity.PurchaseStockInEntity
     **/
    List<PurchaseStockInEntity> getStockInBySourceId(String sourceId);
    /**
     * @description: 批量新增入库单
     * @author Will
     * @date: 2023/4/18 10:48
     * @param resultList
     * @return Boolean
     */
    Boolean batchAddPurchaseStockIn(List<PurchaseStockInDTO.AddDTO> resultList);

    /**
     * 根据采购单获取入库数量
     * @Author Luo_WG
     * @Date 2023/4/18 19:36
     * @param ids ids
     * @return java.util.List<com.erp.model.wms.dto.PurchaseStockInDTO.GetStockInQty>
     **/
    List<PurchaseStockInDTO.GetStockInQty> getStockInQty(List<String> ids);
    /**
     * @description: 采购订单查询关联入库单
     * @author Will
     * @date: 2023/4/19 16:14
     * @param purchaseOrderId
     * @return List<OrderRefStockInDTO>
     */
    List<PurchaseStockInDTO.OrderRefStockInDTO> purchaseOrderRefStockIn(String purchaseOrderId);
}
