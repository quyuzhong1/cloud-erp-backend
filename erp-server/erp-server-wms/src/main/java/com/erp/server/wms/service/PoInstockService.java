package com.erp.server.wms.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.wms.dto.PoInstockDTO;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.entity.*;

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
    PoInstockEntity add(PoInstockDTO.AddDTO dto,Boolean isNotCheck);

    /**
     * @param dto
     * @return Boolean
     * @description: 新增并提交
     * @author Will
     * @date: 2023/4/12 11:44
     */
    PoInstockEntity addAndSubmit(PoInstockDTO.AddDTO dto);


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
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess
     * @description: 审核
     * @author Will
     * @date: 2023/4/12 11:57
     */
    BatchResultDTO approve(PoInstockEntity entity, String type, String comment, Boolean isNeedProcess);

    /**
     * @param entity
     * @param returnEntityList
     * @param issueEntityList
     * @return Boolean
     * @description: 反审核
     * @author Will
     * @date: 2023/4/12 11:58
     */
    BatchResultDTO disApprove(PoInstockEntity entity, List<PoReturnEntity> returnEntityList, List<SubcontractIssueEntity> issueEntityList);

    /**
     * @param dto
     * @return Boolean
     * @description: 取消流程
     * @author Will
     * @date: 2023/4/12 11:59
     */
    Boolean cancelProcess(ApproveDTO.BatchCancelProcessDTO dto);

    /**
     * @param dto
     * @return Boolean
     * @description: 导出
     * @author Will
     * @date: 2023/4/12 12:00
     */
    Boolean exportExcel(PoInstockDTO.ExportParamDTO dto);

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
     * @param syncKingdeeId
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/24 15:29
     **/
    Boolean updateSyncKingdeeId(String id, String syncKingdeeId);

    /**
     * 根据供应商id集合获取入库单量和入库数量
     * @param dto
     * @return
     */
    List<PoInstockDTO.SupplierInstockInfoDTO> getInstockInfoBySupplierIds(PoInstockDTO.SupplierInstockParamDTO dto);

    /**
     * PDA:列表查询
     * @Author Luo_WG
     * @Date 2023/8/16 14:52
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.PoInstockDTO.PdaListDTO>
     **/
    PagingVO<PoInstockDTO.PdaPagingView> PdaPaging(PagingDTO<PoInstockDTO.PdaSearchParamDTO> dto);

    /**
     * PDA:列表数量
     * @Author Luo_WG
     * @Date 2023/8/16 17:51
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.PoInstockDTO.PdaPoInStockCountDTO>
     **/
    List<PoInstockDTO.PdaPoInStockCountDTO> pdaListCount(PermissionsDTO dto);

    /**
     * PDA:新增
     * @Author Luo_WG
     * @Date 2023/8/29 14:35
     * @param dto
     * @param aFalse
     * @return java.lang.String
     **/
    String pdaAdd(PoInstockDTO.AddDTO dto, Boolean aFalse);

    /**
     * PDA:修改
     * @Author Luo_WG
     * @Date 2023/8/29 14:40
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean pdaUpdate(PoInstockDTO.UpdateDTO dto);

    /**
     * PDA:新增并提交
     * @Author Luo_WG
     * @Date 2023/8/30 18:24
     * @param dto
     * @return java.lang.String
     **/
    String pdaAddAndSubmit(PoInstockDTO.AddDTO dto);

    /**
     * PDA:修改并提交
     * @Author Luo_WG
     * @Date 2023/8/30 18:24
     * @param dto
     * @return java.lang.String
     **/
    Boolean pdaUpdateAndSubmit(PoInstockDTO.UpdateDTO dto);

    /**
     * PDA:详情
     * @Author Luo_WG
     * @Date 2023/9/5 15:56
     * @param id
     * @return com.erp.model.wms.dto.PoInstockDTO.ViewDTO
     **/
    PoInstockDTO.ViewDTO pdaView(String id);

    /**
     * 统计数量
     * @author yl
     * @date 2023-10-24 12:11
     * @param dto
     * @return com.erp.model.wms.dto.PoInstockDTO.PagingTotalDTO
     */
    PoInstockDTO.PagingTotalDTO pagingTotal(PoInstockDTO.SearchParamDTO  dto);

    List<QcInfoEntity> getReceiveQcInfo (List<WarehouseReceiveDetailEntity> resultReceiveDetailList, List<String> notHasPodIdList);

    PagingVO<PoInstockDTO.ListDTO> exportPoInStock(PagingDTO<PoInstockDTO.ExportParamDTO> dto);


    /**
     * 批量提交
     */
    void submitList(List<String> ids, List<PoInstockEntity> list);

    /**
     * 单提交
     */
    BatchResultDTO submitEntity(PoInstockEntity entity);

    /**
     * 单删除
     */
    BatchResultDTO deleteEntity(PoInstockEntity entity);

    /**
     * 单删除
     */
    BatchResultDTO invalidEntity(PoInstockEntity entity, String reason);


    /**
     * 单撤销
     */
    BatchResultDTO cancelProcess(PoInstockEntity entity);

    List<PoInstockDTO.PoInStockInfoDTO> getPoStockInByParams(PoInstockDTO.PoInStockParamDTO dto);
}
