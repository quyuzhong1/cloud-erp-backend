package com.erp.server.wms.service;

import com.common.business.dto.base.*;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO;
import com.erp.model.wms.dto.inventory.VirtualFlowRefactorDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeEntity;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 发货通知单主表明细表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-05-10
 */
public interface SoDeliveryNoticeService extends SuperService<SoDeliveryNoticeEntity> {

    /**
     * 分页查询
     * @Author Luo_WG
     * @Date 2023/4/13 15:41
     * @param pagingParamDTO pagingParamDTO
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoDeliveryNoticeDTO.PagingViewDTO>
     **/
    PagingVO<SoDeliveryNoticeDTO.PagingView> paging(PagingDTO<SoDeliveryNoticeDTO.PagingParam> pagingParamDTO);

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/4/17 13:13
     * @param dto dto
     * @return java.util.List<com.erp.model.wms.dto.SoDeliveryNoticeDTO.WarehouseReceiveCountDTO>
     **/
    List<SoDeliveryNoticeDTO.StatusCountDTO> listCount(PermissionsDTO dto);

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/13 11:03
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    String add(SoDeliveryNoticeDTO.Add dto);

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/13 14:51
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean update(SoDeliveryNoticeDTO.Update dto);

    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/4/13 17:10
     * @param id id
     * @return com.erp.model.wms.dto.SoDeliveryNoticeDTO.ViewDTO
     **/
    SoDeliveryNoticeDTO.View view(String id);

    /**
     * 提交
     * @Author Luo_WG
     * @Date 2023/4/14 10:04
     * @param entity entity
     * @return java.lang.BatchResultDTO
     **/
    BatchResultDTO submit(SoDeliveryNoticeEntity entity,Boolean isNeedProcess);

    /**
     * 新增提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean addAndSubmit(SoDeliveryNoticeDTO.Add dto);

    /**
     * 修改提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean updateAndSubmit(SoDeliveryNoticeDTO.Update dto);

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
    BatchResultDTO approve(SoDeliveryNoticeEntity entity, String type, String comment, Boolean isNeedProcess);

    /**
     * 批量反审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param entity
     * @return java.lang.Boolean
     **/
    BatchResultDTO disApprove(SoDeliveryNoticeEntity entity);

    /**
     * 取消流程
     * @Author Luo_WG
     * @Date 2023/4/13 18:58
     * @param dto ids
     * @return java.lang.Boolean
     **/
    Boolean cancelProcess(ApproveDTO.BatchCancelProcessDTO dto);

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
     * @description: 原子批量删除发货通知单
     * @author Will
     * @date: 2023/5/17 15:15
     * @param ids
     * @param returnDetails
     * @return List<BatchResultDTO>
     */
    List<BatchResultDTO> deleteByIds(List<String> ids, boolean returnDetails);

    /**
     * 删除单个实体
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param entity
     * @return BatchResultDTO
     **/
    BatchResultDTO deleteEntity(SoDeliveryNoticeEntity entity);

    /**
     * 导出
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     **/
    Boolean exportExcel(@RequestBody SoDeliveryNoticeDTO.PagingParam dto);



    /**
     * 下推销售出库单-保存
     *
     * @param id           id
     * @param deliveryDate
     * @return void
     **/
    BatchResultDTO generateSoDeliverySave(String id, LocalDate deliveryDate);

    /**
     * 下推发货通知单-保存
     * @Author Luo_WG
     * @Date 2023/5/25 12:30
     * @param list list
     * @return java.lang.Boolean
     **/
    Boolean generateDeliverySave(List<SoInfoDTO.GenerateDeliveryView> list);

    /**
     * 销售单详情-单据关联-发货通知单
     * @Author Luo_WG
     * @Date 2023/5/25 16:36
     * @param sourceId
     * @return java.util.List<com.erp.model.wms.dto.SoDeliveryNoticeDTO.PagingView>
     **/
    List<SoDeliveryNoticeDTO.PagingView> listSoReturnDetailBySourceId(String sourceId);

    /**
     * 根据销售 销售订单ids 获取是否有下推的单据
     * @author yl
     * @date 2023-05-25 10:27
     * @return java.lang.Integer
     */
    Integer getPushDownBySourceIds(List<String> soIds);

    /**
     * 根据销售订单ids获取下推发货通知单的数量
     * @param soIds
     * @return
     */
    Map<String,Long> getPushDownDeliveryNoticeCnt(List<String> soIds);


    /**
     * 根据来源ids 获取数据
     * @author yl
     * @date 2023-08-30 19:23
     * @param sourceIds
     * @return java.util.List<com.erp.model.wms.entity.SoDeliveryNoticeEntity>
     */
    List<SoDeliveryNoticeEntity> listBySourceIdList(List<String> sourceIds);

    /**
     * PDA:根据发货通知单获取详情
     * @Author Luo_WG
     * @Date 2023/9/6 18:10
     * @param id
     * @return java.lang.Boolean
     **/
    List<SoOutstockDTO.GenerateSoOutstockViewDTO> pdaDeliveryDetail(String id);

    /**
     * PDA：详情
     * @Author Luo_WG
     * @Date 2023/9/6 18:11
     * @param id
     * @return com.erp.model.wms.dto.SoDeliveryNoticeDTO.View
     **/
    SoDeliveryNoticeDTO.View pdaView(String id);
    /**
     * PDA:根据sku编号查询发货通知单
     * @Author Luo_WG
     * @Date 2023/8/22 18:26
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.SoDeliveryNoticeDTO.PdaSoDeliveryNotice>
     **/
    List<SoDeliveryNoticeDTO.PdaSoDeliveryNotice> pdaList(SoDeliveryNoticeDTO.PdaSoDeliveryNoticeParam dto);

    /**
     * 根据sourceId 获取发货单通知信息
     * @param sourceId
     * @return
     */
    SoDeliveryNoticeEntity getDeliveryNoticeBySourceId(String sourceId);
    /**
     * 生成拣货单
     * @param picking 参数
     */
    List<WarehouseLocationMoveDTO.GenPickToSkuMove> generatePickingList(SoDeliveryNoticeDTO.GeneratePickingDTO picking);

    /**
     * 生成拣货单的弹窗
     * @param page 要货单id
     */
    PagingVO<SoDeliveryNoticeDTO.PickingViewDTO> generatePickingView(PagingDTO<String> page);


    void writeBackData(List<String> sourceDetailIds);

    /**
     * 根据记录更新状态
     * @param id
     * @param packingStatus
     */
    void updatePackingStatus(String id, String packingStatus);

    BatchResultDTO generatePackingTask(SoDeliveryNoticeEntity entity);

    SoDeliveryNoticeEntity getByCode(String key);

    List<SoDeliveryNoticeEntity> listByCodes(List<String> codes);
    /**
     * 处理数据
     * @author will
     * @date 2024/8/13 17:01
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO handleErrorData(String id);

    PagingVO<SoDeliveryNoticeDTO.PagingView> exportSoDeliveryNotice(PagingDTO<SoDeliveryNoticeDTO.PagingParam> dto);


    /**
     * 更新中转仓库配置
     * @param entity
     * @param changeIds
     * @return
     */
    BatchResultDTO updateTransferWarehouse(SoDeliveryNoticeEntity entity, List<String> changeIds);
    void updateByNoticeChange(List<SoDeliveryNoticeDetailEntity> addList, List<SoDeliveryNoticeDetailEntity> updateList, List<SoDeliveryNoticeDetailEntity> deleteList);

    Boolean generateMachineInfo(List<String> ids);
    /**
     * 查询b2b流水
     * @author will
     * @date 2025/3/31 11:28
     * @return java.util.List<com.erp.model.wms.entity.VirtualTransFlowEntity>
     */
    List<VirtualFlowRefactorDTO.OutInStockDTO> rebuildB2bVirtualFlow();

    List<SoDeliveryNoticeDTO.PrintSkuLabelDTO> printSkuLabelView(List<String> ids);

    void printSkuLabelConfirm(SoDeliveryNoticeDTO.PrintSkuLabelConfirmDTO dto, HttpServletResponse response);

    /**
     * 根据ID列表获取实体Map
     * @param ids
     * @return Map<String, SoDeliveryNoticeEntity>
     */
    Map<String, SoDeliveryNoticeEntity> mapByIds(List<String> ids);

    WorkflowTaskRecordDTO.MqResponseDTO generateDeliveryApprove(WorkflowTaskRecordDTO.MqRequestDTO dto);

    WorkflowTaskRecordDTO.MqResponseDTO autoDeliveryDisApprove(WorkflowTaskRecordDTO.MqRequestDTO dto);
    /**
     * 更新发货通知单状态
     * @author will
     * @date 2025/8/29 17:43
     * @param entity
     * @return BatchResultDTO
     */
    BatchResultDTO updateIsAllowOutstock(SoDeliveryNoticeEntity entity, SoDeliveryNoticeDTO.PermitOutstockDTO dto);

    List<SoDeliveryNoticeEntity> listDeliveryNoticeBySoIds(List<String> soIds);

    void updateSalesInfo(SoInfoEntity soInfoEntity);

    /**
     * 审核通过
     * @author will
     * @date 2025/10/22 15:53
     * @param dto
     * @param entity
     * @return Boolean
     */
    Boolean approveEnd(ApproveOneDTO dto, SoDeliveryNoticeEntity entity);
}
