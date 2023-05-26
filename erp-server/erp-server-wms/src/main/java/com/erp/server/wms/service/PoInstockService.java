package com.erp.server.wms.service;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.dto.PoInstockDTO;
import com.erp.model.wms.entity.PoInstockEntity;

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
public interface PoInstockService extends SuperService<PoInstockEntity> {
    /**
     * @param dto
     * @return PagingVO<ListDTO>
     * @description: 分页查询
     * @author Will
     * @date: 2023/4/12 11:40
     */
    PagingVO<PoInstockDTO.ListDTO> paging(PagingDTO<PoInstockDTO.SearchParamDTO> dto);

    /**
     * @param dto
     * @return List<ListStatusCountDTO>
     * @description: 查询数量
     * @author Will
     * @date: 2023/4/12 11:43
     */
    List<PoInstockDTO.ListStatusCountDTO> listCount(PermissionsDTO dto);

    /**
     * @param dto
     * @return String
     * @description: 新增
     * @author Will
     * @date: 2023/4/12 11:41
     */
    String add(PoInstockDTO.AddDTO dto);

    /**
     * @param dto
     * @return Boolean
     * @description: 新增并提交
     * @author Will
     * @date: 2023/4/12 11:44
     */
    String addAndSubmit(PoInstockDTO.AddDTO dto);


    /**
     * 当质检单 质检类型为b2b 是
     * 批量生成入库单
     *
     * @param list
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-24 15:08
     */
    Boolean batchAdd(List<PoInstockDTO.AddDTO> list);

    /**
     * @param dto
     * @return Boolean
     * @description: 修改
     * @author Will
     * @date: 2023/4/12 11:45
     */
    Boolean update(PoInstockDTO.UpdateDTO dto);

    /**
     * @param dto
     * @return Boolean
     * @description: 修改并提交
     * @author Will
     * @date: 2023/4/12 11:46
     */
    Boolean updateAndSubmit(PoInstockDTO.UpdateDTO dto);

    /**
     * @param ids
     * @return Boolean
     * @description: 提交
     * @author Will
     * @date: 2023/4/12 11:46
     */
    Boolean submit(List<String> ids);

    /**
     * @param id
     * @return ViewDTO
     * @description: 查看
     * @author Will
     * @date: 2023/4/12 11:55
     */
    PoInstockDTO.ViewDTO view(String id);

    /**
     * @param ids
     * @return Boolean
     * @description: 删除
     * @author Will
     * @date: 2023/4/12 11:56
     */
    Boolean delete(List<String> ids);

    /**
     * @param ids
     * @param remark
     * @return Boolean
     * @description: 作废
     * @author Will
     * @date: 2023/4/12 11:57
     */
    Boolean invalid(List<String> ids, String remark);

    /**
     * @param baseApproveParamDTO
     * @description: 审核
     * @author Will
     * @date: 2023/4/12 11:57
     */
    void approve(BaseApproveParamDTO baseApproveParamDTO);

    /**
     * @param ids
     * @return Boolean
     * @description: 反审核
     * @author Will
     * @date: 2023/4/12 11:58
     */
    Boolean disApprove(List<String> ids);

    /**
     * @param ids
     * @return Boolean
     * @description: 取消流程
     * @author Will
     * @date: 2023/4/12 11:59
     */
    Boolean cancelProcess(List<String> ids);

    /**
     * @param dto
     * @param response
     * @return Boolean
     * @description: 导出
     * @author Will
     * @date: 2023/4/12 12:00
     */
    Boolean exportExcel(PoInstockDTO.SearchParamDTO dto, HttpServletResponse response);

    /**
     * @param ids
     * @return List<ViewGeneratePurchaseReturnOrderDTO>
     * @description: 下推退货单数据显示
     * @author Will
     * @date: 2023/4/12 12:02
     */
    List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO> viewGeneratePurchaseReturnOrder(List<String> ids);

    /**
     * @param dto
     * @return Boolean
     * @description: 下推退货单数据保存
     * @author Will
     * @date: 2023/4/12 12:02
     */
    Boolean generatePurchaseReturnOrder(PoInstockDTO.ListGeneratePurchaseReturnOrderDTO dto);

    /**
     * 根据来源Id查询入库单
     *
     * @param sourceId sourceId
     * @return com.erp.model.wms.entity.PurchaseStockInEntity
     * @Author Luo_WG
     * @Date 2023/4/18 10:30
     **/
    List<PoInstockEntity> getStockInBySourceId(String sourceId);


    /**
     * 根据来源Id查询入库单
     *
     * @param sourceIds sourceId
     * @return com.erp.model.wms.entity.PurchaseStockInEntity
     * @Author Luo_WG
     * @Date 2023/4/18 10:30
     **/
    List<PoInstockEntity> getStockInBySourceIds(List<String> sourceIds);

    /**
     * @param resultList
     * @return Boolean
     * @description: 批量新增入库单
     * @author Will
     * @date: 2023/4/18 10:48
     */
    Boolean batchAddPurchaseStockIn(List<PoInstockDTO.AddDTO> resultList);

    /**
     * 根据采购单获取入库数量
     *
     * @param ids ids
     * @return java.util.List<com.erp.model.wms.dto.PurchaseStockInDTO.GetStockInQty>
     * @Author Luo_WG
     * @Date 2023/4/18 19:36
     **/
    List<PoInstockDTO.GetStockInQty> getStockInQty(List<String> ids);

    /**
     * @param purchaseOrderId
     * @return List<OrderRefStockInDTO>
     * @description: 采购订单查询关联入库单
     * @author Will
     * @date: 2023/4/19 16:14
     */
    List<PoInstockDTO.OrderRefStockInDTO> purchaseOrderRefStockIn(String purchaseOrderId);
    /**
     * @description: 生成采购入库单
     * @author Will
     * @date: 2023/4/13 11:39
     * @param dto
     * @return Boolean
     */
    Boolean generateStockIn(PurchaseOrderDTO.ListGenerateStockInDTO dto);

    /**
     * 修改金蝶同步状态
     *
     * @param id
     * @param syncKingdeeStatus
     * @param syncKingdeeId
     * @param syncOperate
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/24 15:29
     **/
    Boolean updateSyncKingdeeStatus(String id, String syncKingdeeStatus, String syncKingdeeId, String syncOperate);
}
