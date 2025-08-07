package com.erp.server.wms.service;

import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.TransferOutDTO;
import com.erp.model.wms.entity.TransferOutEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 分布式调出单 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface TransferOutService extends SuperService<TransferOutEntity> {
    /**
     * @description: 根据来源单据ids查询
     * @author Will
     * @date: 2023/5/15 9:25
     * @param ids
     * @return List<TransferOutEntity>
     */
    List<TransferOutEntity> listBySourceIds(List<String> ids);

    /**
     * 新增
     * @param addDTO
     */
    String add(TransferOutDTO.AddDTO addDTO);

    /**
     * 分页列表
     * @param pagingParamDTO
     * @return
     */
    PagingVO<TransferOutDTO.PagingViewDTO> paging(PagingDTO<TransferOutDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 导出Excel
     *
     * @param param
     */
    void exportList(TransferOutDTO.ExportDTO param);

    /**
     * 状态统计
     * @param param
     * @return
     */
    List<TransferOutDTO.TabListDTO> listCount(PermissionsDTO param);

    /**
     * 修改
     * @param updateDTO
     */
    void update(TransferOutDTO.UpdateDTO updateDTO);

    /**
     * 详情
     * @param id
     * @return
     */
    TransferOutDTO.ViewDTO view(String id);

    /**
     * 提交审核
     * @param ids
     */
    void submit(List<String> ids);

    /**
     * 修改并提交审核
     * @param dto
     */
    void updateAndSubmit(TransferOutDTO.UpdateDTO dto);

    /**
     * 审核
     * @param approveOneDTO
     */
    BatchResultDTO approve(ApproveOneDTO approveOneDTO, TransferOutEntity entity);
    /**
     * 审核结束
     * @author will
     * @date 2025/4/22 15:53
     * @param dto
     * @param entity
     * @return Boolean
     */
    Boolean approveEnd(ApproveOneDTO dto, TransferOutEntity entity);
    /**
     * 删除
     * @param ids
     */
    void delete(List<String> ids);

    /**
     * 原子批量删除分布式调出单
     * @param ids
     * @param returnDetails
     * @return List<BatchResultDTO>
     */
    List<BatchResultDTO> deleteByIds(List<String> ids, boolean returnDetails);

    /**
     * 删除单个分布式调出单
     * @param entity
     * @return com.common.business.dto.base.BatchResultDTO
     */
    BatchResultDTO deleteEntity(TransferOutEntity entity);

    /**
     * 根据ID列表获取实体Map
     * @param ids
     * @return java.util.Map<java.lang.String, com.erp.model.wms.entity.TransferOutEntity>
     */
    Map<String, TransferOutEntity> mapByIds(List<String> ids);

    /**
     * 作废
     * @param ids
     * @param remark
     */
    void invalid(List<String> ids, String remark);

    /**
     * 撤销
     * @param ids
     */
    void cancel(List<String> ids);

    /**
     * 反审核
     * @param
     */
    BatchResultDTO disApprove(TransferOutEntity transferOutEntity);

    /**
     * 分步式调出单下推分布式调入单
     * @param ids
     * @return
     */
    List<TransferOutDTO.ViewGenerateTransferInDTO> viewGenerateTransferIn(List<String> ids);

    /**
     * 下推分布式调入单保存
     * @param dataList
     */
    void generateTransferIn(ValidList<TransferOutDTO.GenerateTransferInDTO> dataList);

    /**
     * 下推分布式调入单修改页面选择产品信息
     * @param param
     * @return
     */
    List<TransferOutDTO.ChooseListDTO> listTransferOut(TransferOutDTO.SearchParamDTO param);

    /**
     * 根据分步式调出单单号获取信息
     * @param codes
     * @return
     */
    List<TransferOutEntity> findByCodes(List<String> codes);

    PagingVO<TransferOutDTO.PagingViewDTO> exportTransferOut(PagingDTO<TransferOutDTO.ExportDTO> dto);

    List<TransferOutDTO.PutawayDetailDTO> listPutawayDetail(String id);
    /**
     *  更新明细金蝶id
     * @author will
     * @date 2025/4/23 17:46
     * @param businessId
     * @param syncKingdeeId
     * @return void
     */
    Boolean updateSyncKingdeeId(String businessId, String syncKingdeeId);
}
