package com.erp.server.wms.service;


import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.wms.dto.SupplierCountDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.dto.excel.WarehouseReceiveExportExcelDTO;
import com.erp.model.wms.entity.PoInstockEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * <p>
 *  采购收货服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-04-06
 */
public interface WarehouseReceiveService extends SuperService<WarehouseReceiveEntity> {
    /**
     * 分页查询
     * @Author Luo_WG
     * @Date 2023/4/13 15:41
     * @param pagingParamDTO pagingParamDTO
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.WarehouseReceiveDTO.PagingViewDTO>
     **/
    PagingVO<WarehouseReceiveDTO.PagingViewDTO> paging(PagingDTO<WarehouseReceiveDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/4/17 13:13
     * @param dto dto
     * @return java.util.List<com.erp.model.wms.dto.WarehouseReceiveDTO.WarehouseReceiveCountDTO>
     **/
    List<WarehouseReceiveDTO.WarehouseReceiveCountDTO> listCount(PermissionsDTO dto);

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/13 11:03
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    WarehouseReceiveEntity add(WarehouseReceiveDTO.AddDTO dto);

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/13 14:51
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean update(WarehouseReceiveDTO.UpdateDTO dto);

    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/4/13 17:10
     * @param id id
     * @return com.erp.model.wms.dto.WarehouseReceiveDTO.ViewDTO
     **/
    WarehouseReceiveDTO.ViewDTO view(String id);

    /**
     * 提交
     * @Author Luo_WG
     * @Date 2023/4/14 10:04
     * @param ids ids
     * @return java.lang.Boolean
     **/
    Boolean submit(List<String> ids);

    /**
     * 新增提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return java.lang.Boolean
     **/
    WarehouseReceiveEntity addAndSubmit(WarehouseReceiveDTO.AddDTO dto);

    /**
     * 修改提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean updateAndSubmit(WarehouseReceiveDTO.UpdateDTO dto);

    /**
     * 批量审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess
     * @return java.lang.Boolean
     **/
    BatchResultDTO approve(WarehouseReceiveEntity entity, String type, String comment, Boolean isNeedProcess,List<WarehouseReceiveDetailEntity> receiveDetailList);

    /**
     * 批量反审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param entity
     * @return java.lang.Boolean
     **/
    BatchResultDTO disApprove(WarehouseReceiveEntity entity,List<WarehouseReceiveDetailEntity> detailEntityList);

    /**
     * 取消流程
     * @Author Luo_WG
     * @Date 2023/4/13 18:58
     * @param ids ids
     * @return java.lang.Boolean
     **/
    Boolean cancelProcess(List<String> ids);

    /**
     * 批量作废
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param ids ids
     * @param remark remark
     * @return java.lang.Boolean
     **/
    Boolean invalid(List<String> ids, String remark);

    /**
     * 批量删除
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param ids ids
     * @return java.lang.Boolean
     **/
    Boolean delete(List<String> ids);

    /**
     * 导出
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     **/
    Boolean exportExcel(@RequestBody WarehouseReceiveDTO.PagingParamDTO dto);

    /**
     * 下推入库单列表查询
     * @Author Luo_WG
     * @Date 2023/4/14 14:24
     * @param ids ids
     * @return java.util.List<com.erp.model.wms.dto.WarehouseReceiveDTO.GenerateStockInViewDTO>
     **/
    List<WarehouseReceiveDTO.GenerateStockInViewDTO> generateStockInView(List<String> ids);

    /**
     * 下推入库单
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param dtos dtos
     * @return java.lang.Boolean
     **/
    Boolean generateStockIn(List<WarehouseReceiveDTO.GenerateStockInDTO> dtos);

    /**
     * 采购订单-关联的收货单据
     * @Author Luo_WG
     * @Date 2023/4/13 18:47
     * @param purchaseOrderId purchaseOrderId
     * @return java.lang.Integer
     **/
    List<WarehouseReceiveDTO.OrderRefReceiveDTO> purchaseOrderRefReceive(String purchaseOrderId);

    /**
     * 根据采购订单ids 获取收获数据
     * @author yl
     * @date 2023-04-27 18:31
     * @param purchaseOrderIds
     * @return java.util.List<com.erp.model.wms.entity.WarehouseReceiveEntity>
     */
    List<WarehouseReceiveEntity> listByPurchaseOrderIds(List<String> purchaseOrderIds);

    /**
     * 采购订单-下推收货单保存按钮
     * @Author Luo_WG
     * @Date 2023/4/24 13:54
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean generateReceive(PurchaseOrderDTO.ListGenerateReceiveDTO dto);

    /**
     * 根据供应商id集合查询收货批次和收货数量
     */
    List<WarehouseReceiveDTO.SupplierReceiveInfoDTO> getReceiveInfoBySupplierIds(WarehouseReceiveDTO.SupplierReceiveParamDTO dto);

    /**
     * pda:分页查询
     * @Author Luo_WG
     * @Date 2023/4/13 15:41
     * @param pagingParamDTO pagingParamDTO
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.WarehouseReceiveDTO.PagingViewDTO>
     **/
    PagingVO<WarehouseReceiveDTO.PdaPagingViewDTO> pdaPaging(PagingDTO<WarehouseReceiveDTO.PdaPagingParamDTO> pagingParamDTO);

    /**
     * pda:列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/4/17 13:13
     * @param dto dto
     * @return java.util.List<com.erp.model.wms.dto.WarehouseReceiveDTO.WarehouseReceiveCountDTO>
     **/
    List<WarehouseReceiveDTO.PdaPoReceiveCountDTO> pdaListCount(PermissionsDTO dto);

    /**
     * PDA:新增
     * @Author Luo_WG
     * @Date 2023/4/13 11:03
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    String pdaAdd(WarehouseReceiveDTO.AddDTO dto);

    /**
     * PDA:修改
     * @Author Luo_WG
     * @Date 2023/8/14 14:29
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean pdaUpdate(WarehouseReceiveDTO.UpdateDTO dto);

    /**
     * PDA:查询详情
     * @Author Luo_WG
     * @Date 2023/4/13 17:10
     * @param id id
     * @return com.erp.model.wms.dto.WarehouseReceiveDTO.ViewDTO
     **/
    WarehouseReceiveDTO.ViewDTO pdaView(String id);

    /**
     * PDA:条件查询收货单
     * @Author Luo_WG
     * @Date 2023/8/18 11:10
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.WarehouseReceiveDTO.PdaPoReceive>
     **/
    List<WarehouseReceiveDTO.PdaPoReceive> pdaList(WarehouseReceiveDTO.PdaPoReceiveParam dto);

    /**
     * 新增提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean pdaAddAndSubmit(WarehouseReceiveDTO.AddDTO dto);

    /**
     * 修改提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean pdaUpdateAndSubmit(WarehouseReceiveDTO.UpdateDTO dto);

    /**
     * pda:待入库查询
     * @Author Luo_WG
     * @Date 2023/9/6 10:33
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.WarehouseReceiveDTO.WaitInStockPaging>
     **/
    PagingVO<List<WarehouseReceiveDTO.WaitInStockPaging>> waitInStockPaging(PagingDTO<WarehouseReceiveDTO.WaitInStockPagingParam> dto);

    /**
     * PDA:待入库查询表头数量
     * @Author Luo_WG
     * @Date 2023/9/6 11:38
     * @param dto
     * @return com.common.business.vo.PagingVO<java.util.List<com.erp.model.wms.dto.WarehouseReceiveDTO.WaitInStockPaging>>
     **/
     List<WarehouseReceiveDTO.WaitInStockCountDTO> waitInStockListCount(PermissionsDTO dto);

    /**
     *更改金蝶同步状态
     * @param id
     * @param syncKingdeeId
     * @return java.lang.Boolean
     **/
    Boolean updateSyncKingdeeId(String id, String syncKingdeeId);

    /**
     * 批量质检完成时生成入库单
     * @param ids ：  收货单id
     * @return java.lang.Boolean
     **/
    Boolean generateStockInWhenQcFinish(List<String> ids);

    /**
     *
     * @param supplierId
     * @return
     */
    SupplierCountDTO countOrderBySupplierId(String supplierId);
    /**
     * 根据采购订单获取收货单明细
     * @param purchaseOrderIds
     * @return
     */
    List<WarehouseReceiveDTO.PurchaseOrderDetailDTO> getReceiveListByPurchaseOrderIds(List<String> purchaseOrderIds);
    List<WarehouseReceiveDTO.PurchaseOrderDetailDTO> getReceiveListByPurchaseOrderIdsAll(List<String> purchaseOrderIds);

    List<WarehouseReceiveEntity> listReceiveBySourceTypeAndIds(WarehouseReceiveDTO.SourceParamDTO dto);

    /**
     * 修改入库状态
     * @author hyj
     * @date 2024/5/14 14:30
     * @param list
     */
     void updateReceiveInStockStatus(List<PoInstockEntity> list);

    /**
     * 清洗入库状态
     * @author hyj
     * @date 2024/5/14 14:30
     */
    void instockStatusCleanJob();

    PagingVO<WarehouseReceiveExportExcelDTO> exportWarehouseReceive(PagingDTO<WarehouseReceiveDTO.PagingParamDTO> dto);

    WarehouseReceiveDTO.PagingTotalDTO pagingTotal(WarehouseReceiveDTO.PagingParamDTO dto);

    /**
     * 单提交
     */
    BatchResultDTO submitEntity(WarehouseReceiveEntity mainEntity);

    /**
     * 单作废
     **/
    BatchResultDTO invalidEntity(WarehouseReceiveEntity entity, String remark);

    /**
     * 单取消流程
     **/
    BatchResultDTO cancelProcessEntity(WarehouseReceiveEntity entity);

    /**
     * 批量删除
     *
     **/
    BatchResultDTO deleteEntity(WarehouseReceiveEntity entity);
    /**
     * 根据ids查询收货信息
     * @author will
     * @date 2025/6/12 09:54
     * @param idList
     * @return List<ReceiveSourceDTO>
     */
    List<WarehouseReceiveDTO.ReceiveSourceDTO> listReceiveSourceByDetailIds(List<String> idList);
}
