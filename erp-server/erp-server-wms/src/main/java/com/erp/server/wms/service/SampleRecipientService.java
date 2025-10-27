package com.erp.server.wms.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.ClientTypeEnum;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SampleRecipientDTO;
import com.erp.model.wms.dto.excel.SampleRecipientExcelDTO;
import com.erp.model.wms.entity.SampleRecipientEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 样品领用单 服务类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
public interface SampleRecipientService extends SuperService<SampleRecipientEntity> {

    /**
    * 新增
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SampleRecipientDTO.AddDTO dto);

    /**
    * 修改
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    Boolean update(SampleRecipientDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author wuhaotian
    * @date: 2025-08-21
    * @param pagingParamDTO
    * @return PagingVO<SampleRecipientDTO.ListDTO>>
    */
    PagingVO<SampleRecipientDTO.ListDTO> paging(PagingDTO<SampleRecipientDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return List<SampleRecipientDTO.TabListDTO>>
    */
    List<SampleRecipientDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author wuhaotian
    * @date: 2025-08-21
    * @param id
    * @return
    */
    SampleRecipientDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(SampleRecipientDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    void updateAndSubmit(SampleRecipientDTO.UpdateDTO dto);

    /**
     * 提交审核
     * @author wuhaotian
     * @date: 2025-08-21
     * @param id
     * @param clientType
     * @return
     */
    BatchResultDTO submit(String id, ClientTypeEnum clientType);
    BatchResultDTO submit(String id);

    /**
     * 审核
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     * @param clientType
     * @return
     */
    BatchResultDTO approve(ApproveOneDTO dto, ClientTypeEnum clientType);
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
     * 反审核
     * @author wuhaotian
     * @date: 2025-08-21
     * @param id
     * @param clientType
     * @return
     */
    BatchResultDTO disApprove(String id, ClientTypeEnum clientType);
    BatchResultDTO disApprove(String id);

    /**
     * 删除
     * @author wuhaotian
     * @date: 2025-08-21
     * @param id
     * @param clientType
     * @return
     */
    BatchResultDTO delete(String id, ClientTypeEnum clientType);
    BatchResultDTO delete(String id);
    /**
     * 作废
     * @author wuhaotian
     * @date: 2025-08-21
     * @param id
     * @param remark
     * @param clientType
     * @return
     */
    BatchResultDTO invalid(String id, String remark, ClientTypeEnum clientType);
    BatchResultDTO invalid(String id, String remark);

    /**
     * 撤销
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     * @param clientType
     * @return
     */
    BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto, ClientTypeEnum clientType);
   BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto);

    /**
    * 导出Excel
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @param response
    * @return
    */
    Boolean exportList(SampleRecipientDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, SampleRecipientEntity entity);

    /**
    * 结束领用
    * @author wuhaotian
    * @date: 2025-08-21
    * @param id
    * @return
    */
    BatchResultDTO finishRecipient(String id);

    /**
    * 结束领用（带原因）
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    List<BatchResultDTO> finishRecipient(SampleRecipientDTO.FinishRecipientDTO dto);

        /**
     * 查询SKU成本
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     * @return
     */
    List<SampleRecipientDTO.SkuDTO> querySkuCost(SampleRecipientDTO.SkuCostQueryDTO dto);
    
    /**
     * 获取SKU可领用库存
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     * @return
     */
    List<SampleRecipientDTO.SkuAvailableStockDTO> querySkuAvailableStock(SampleRecipientDTO.SkuAvailableStockQueryDTO dto);
    
    /**
     * 获取SKU列表（支持高级查询和模糊搜索）
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     * @return
     */
    PagingVO<SampleRecipientDTO.SkuListResponseDTO> getSkuList(SampleRecipientDTO.SkuListQueryDTO dto);

    /**
     * 下推其他出库单查询
     * @author wuhaotian
     * @date: 2025-08-21
     * @param ids
     * @return
     */
    List<SampleRecipientDTO.ViewGenerateOutboundOrderDTO> viewGenerateOutboundOrder(List<String> ids);

    /**
     * 下推其他出库单保存
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     * @return
     */
    List<BatchResultDTO> generateOutboundOrder(SampleRecipientDTO.ListGenerateOutboundOrderDTO dto);

    /**
     * 下载模板
     * @author wuhaotian
     * @date: 2025-08-21
     * @param response
     * @return
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 异步导入
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     * @return
     */
    Boolean importExcel(BaseDTO.ImportDTO dto);

    /**
     * 导入样品领用单
     * @author wuhaotian
     * @date: 2025-08-22
     * @param dto
     */
    void importSampleRecipient(BaseDTO.ImportDTO dto);

    /**
     * 增加已出库数量（其他出库单审核通过时调用）
     * @author wuhaotian
     * @date: 2025-08-21
     * @param detailId 样品领用单明细ID
     * @param qty 出库数量
     * @return 是否成功
     */
    Boolean increaseDeliveryQty(String detailId, Integer qty);

    /**
     * 减少已出库数量（其他出库单反审核时调用）
     * @author wuhaotian
     * @date: 2025-08-21
     * @param detailId 样品领用单明细ID
     * @param qty 出库数量
     * @return 是否成功
     */
    Boolean decreaseDeliveryQty(String detailId, Integer qty);

    /**
     * 获取样品领用单分页数据（用于异步导出）
     * @author wuhaotian
     * @date: 2025-08-25
     * @param dto 分页参数
     * @return 分页结果
     */
    PagingVO<SampleRecipientDTO.ListDTO> getSampleRecipientPageData(PagingDTO<SampleRecipientDTO.ExportDTO> dto);

    /**
     * 处理导入成功的数据列表
     * @author wuhaotian
     * @date: 2025-08-25
     * @param successList 成功的数据列表
     * @param errorNoList 错误的序号列表
     * @param errorList2 错误数据列表2
     * @param importType 导入类型
     */
    void handleImportSuccessList(List<SampleRecipientExcelDTO> successList, List<String> errorNoList, List<SampleRecipientExcelDTO> errorList2, String importType);


    BatchResultDTO createOtherOutboundOrderBySourceId(String sourceId, List<SampleRecipientDTO.ViewGenerateOutboundOrderDTO> items);

    // ========== APP端专用方法 ==========

    /**
     * APP端标签页列表
     * @author wuhaotian
     * @date: 2025-09-15
     * @param dto
     * @return List<SampleRecipientDTO.TabListDTO>
     */
    List<SampleRecipientDTO.TabListDTO> tabListApp(PermissionsDTO dto);

    /**
     * APP端分页列表查询
     * @author wuhaotian
     * @date: 2025-09-15
     * @param pagingParamDTO
     * @return PagingVO<SampleRecipientDTO.ListDTO>
     */
    PagingVO<SampleRecipientDTO.ListDTO> pagingApp(PagingDTO<SampleRecipientDTO.PagingParamDTO> pagingParamDTO);



}
