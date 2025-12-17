package com.erp.server.wms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.TransferInfoDTO;
import com.erp.model.wms.dto.TransferInfoDetailDTO;
import com.erp.model.wms.entity.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 直接调拨单主表
 *
 * @author will
 * @since 2023-05-10
 */
public interface TransferInfoService extends SuperService<TransferInfoEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/5/15 11:22
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<TransferInfoDTO.ListDTO> paging(PagingDTO<TransferInfoDTO.SearchParamDTO> dto);
    /**
     * @description: 查询数量
     * @author Will
     * @date: 2023/5/15 11:23
     * @param dto
     * @return List<ListStatusCountDTO>
     */
    List<TransferInfoDTO.ListStatusCountDTO> listCount(PermissionsDTO dto);
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/5/15 11:23
     * @param dto
     * @return String
     */
    String add(TransferInfoDTO.AddDTO dto);
    /**
     * @description: 新增并提交
     * @author Will
     * @date: 2023/5/15 11:23
     * @param dto
     * @return String
     */
    String addAndSubmit(TransferInfoDTO.AddDTO dto);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/5/15 11:25
     * @param dto
     * @return Boolean
     */
    Boolean update(TransferInfoDTO.UpdateDTO dto);
    /**
     * @description: 修改并提交
     * @author Will
     * @date: 2023/5/15 11:25
     * @param dto
     * @return Boolean
     */
    Boolean updateAndSubmit(TransferInfoDTO.UpdateDTO dto);

    /**
     * 保存并审核
     * @param dto
     * @return
     */
    String addAndApprove(TransferInfoDTO.AddDTO dto);
    /**
     * @description: 提交
     * @author Will
     * @date: 2023/5/15 11:25
     * @param entity
     * @param isStartProcess 是否启用审核流程（系统自动审核的不需要启动审核流）
     * @return Boolean
     */
    BatchResultDTO submit(TransferInfoEntity entity, Boolean isStartProcess);
    /**
     * @description: 查看详情
     * @author Will
     * @date: 2023/5/15 11:25
     * @param id
     * @return ViewDTO
     */
    TransferInfoDTO.ViewDTO view(String id);
    /**
     * @description: 删除
     * @author Will
     * @date: 2023/5/15 11:25
     * @param ids
     * @return Boolean
     */
    Boolean delete(List<String> ids);

    /**
     * @description: 删除直接调拨单（返回详细结果）
     * @author Will
     * @date: 2023/5/15 11:25
     * @param ids
     * @return java.util.List<com.common.business.dto.base.BatchResultDTO>
     */
    List<BatchResultDTO> delete(List<String> ids, boolean returnDetails);

    /**
     * @description: 作废
     * @author Will
     * @date: 2023/5/15 11:25
     * @param ids
     * @param remark
     * @return Boolean
     */
    Boolean invalid(List<String> ids, String remark);
    /**
     * @description: 审核
     * @author Will
     * @date: 2023/5/15 11:25
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess
     * @param isSyncKingDee
     * @param isStartProcess 是否启用审核流程（系统自动审核的不需要启动审核流）
     */
    BatchResultDTO approve(TransferInfoEntity entity, String type, String comment, Boolean isNeedProcess,Boolean isSyncKingDee, Boolean isStartProcess);
    /**
     * 审核结束
     * @Author Luo_WG
     * @Date 2024/9/6 16:57
     * @param entity
     * @param type
     * @param comment
     * @param isSyncKingDee
     * @return java.lang.Boolean
     **/
    Boolean approveEnd(TransferInfoEntity entity, String type, String comment, Boolean isSyncKingDee);

    /**
     * 虚拟仓库存扣减处理
     * @author will
     * @date 2024/8/13 18:04
     * @param list
     * @param detailList
     */
    void updateVirtualInventoryTransCore (List<TransferInfoEntity> list,List<TransferInfoDetailEntity> detailList);
    /**
     * @description: 反审核
     * @author Will
     * @date: 2023/5/15 11:26
     * @param entity
     * @param isPushKingDee
     * @return Boolean
     */
    BatchResultDTO disApprove(TransferInfoEntity entity, Boolean isPushKingDee,Boolean isManual);
    /**
     * @description: 取消流程
     * @author Will
     * @date: 2023/5/15 11:26
     * @param ids
     * @return Boolean
     */
    Boolean cancelProcess(List<String> ids);
    /**
     * @param dto
     * @return Boolean
     * @description: 导出
     * @author Will
     * @date: 2023/5/15 11:26
     */
    Boolean exportExcel(TransferInfoDTO.SearchParamDTO dto);
    /**
     * @description: 根据来源ids查询
     * @author Will
     * @date: 2023/5/15 11:26
     * @param sourceIds
     * @return List<TransferInfoEntity>
     */
    List<TransferInfoEntity> listBySourceIds(List<String> sourceIds);
    /**
     * @description: 更新金蝶状态等信息
     * @author Will
     * @date: 2023/5/23 17:41
     * @param id
     * @param syncKingdeeId
     * @return Boolean
     */
    Boolean updateSyncKingdeeId(String id,String syncKingdeeId);
    /**
     * @description: 根据编码查询有效直接直接调拨单
     * @author Will
     * @date: 2023/6/28 17:35
     * @param code
     * @return ViewDTO
     */
    TransferInfoDTO.ViewDTO viewTransferInfoByCode(String code);

    /**
     * 验证SKU是否缺货
     * @param dto
     * @return
     */
    String checkSkuInventory(TransferInfoDTO.CommonDTO dto, List<TransferInfoDetailDTO.AddDTO> detailList);

    /**
     * pda:列表查询
     * @Author Luo_WG
     * @Date 2023/8/24 15:36
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.TransferInfoDTO.PdaListDTO>
     **/
    PagingVO<TransferInfoDTO.PdaListDTO> pdaPaging(PagingDTO<TransferInfoDTO.PdaSearchParamDTO> dto);

    /**
     * PDA:列表数量
     * @Author Luo_WG
     * @Date 2023/8/24 15:51
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.TransferInfoDTO.PdaListStatusCountDTO>
     **/
    List<TransferInfoDTO.PdaListStatusCountDTO> pdaListCount(PermissionsDTO dto);

    /**
     * 海外入库单生成直接调拨
     * @param mainEntity 主记录
     * @param receivedEntityList 签收记录
     * @param remark 备注
     * @return 调拨单主表id
     */
    String generateFromOverseasInbound(OverseasWarehouseInboundEntity mainEntity, List<OverseasWarehouseInboundDetailEntity> detailList, List<OverseasWarehouseInboundReceivedEntity> receivedEntityList, String remark,Boolean isToOnwayWarehouse);

    /**
     * 要货申请处理撤销
     * @Author Luo_WG
     * @Date 2024/1/10 11:15
     * @param code
     * @param sourceType
     * @return java.lang.Boolean
     **/
    void requisitionApplicationCancelProcess(String code, String sourceType);


    /**
     * 检查并反审核和删除对应调拨单
     * @Author Jim
     * @Date 2024/4/30 15:15
     * @param sourceCode 来源号
     * @param sourceType 来源类型
     * @param billDate 单据日期
     * @return java.lang.Boolean 处理结果
     **/
    Boolean checkHistoryAndDel(String sourceCode,String sourceType, LocalDate billDate);
    /**
     * 根据批次号查询直接调拨单
     * @author will
     * @date 2024/7/12 10:11
     * @param batchNoList
     * @return List<TransferInfoEntity>
     */
    List<TransferInfoEntity> listByBatchNoList(List<String> batchNoList);
    /**
     * 根据来源id查询
     * @author will
     * @date 2024/7/19 20:04
     * @param sourceId
     * @return List<TransferInfoEntity>
     */
    List<TransferInfoEntity> listBySourceId(String sourceId);

    PagingVO<TransferInfoDTO.ListDTO> exportTransferInfo(PagingDTO<TransferInfoDTO.SearchParamDTO> dto);

    /**
     * 批量修改调拨日期
     * @param entity
     * @param billDate
     * @return
     */
    BatchResultDTO updateBillDate(TransferInfoEntity entity, LocalDate billDate);
    /**
     * 处理错误数据
     * @author will
     * @date 2024/11/27 11:03
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO handleErrorData(String id);

    List<TransferInfoEntity> listByCodes(List<String> list);

    void updateApproveStatus(TransferInfoDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO);
}
