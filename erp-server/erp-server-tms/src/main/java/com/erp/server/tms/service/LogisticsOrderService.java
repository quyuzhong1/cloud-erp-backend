package com.erp.server.tms.service;

import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.tms.dto.LogisticsOrderDTO;
import com.erp.model.tms.entity.LogisticsOrderEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 物流下单表 服务类
 * </p>
 *
 * @author lei.nie
 * @since 2026-04-20
 */
public interface LogisticsOrderService extends SuperService<LogisticsOrderEntity> {

    /**
     * 新增
     *
     * @param dto LogisticsOrderDTO.AddDTO
     * @return BaseResultDTO.AddDTO
     * @author lei.nie
     * @date: 2026-04-20
     */
    BaseResultDTO.AddDTO add(LogisticsOrderDTO.AddDTO dto);

    /**
     * 修改
     *
     * @param dto LogisticsOrderDTO.UpdateDTO
     * @return Boolean
     * @author lei.nie
     * @date: 2026-04-20
     */
    Boolean update(LogisticsOrderDTO.UpdateDTO dto);

    /**
     * 分页列表查询
     *
     * @param pagingParamDTO PagingDTO<LogisticsOrderDTO.PagingParamDTO>
     * @return PagingVO<LogisticsOrderDTO.ListDTO>>
     * @author lei.nie
     * @date: 2026-04-20
     */
    PagingVO<LogisticsOrderDTO.ListDTO> paging(PagingDTO<LogisticsOrderDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 状态统计
     *
     * @param dto PermissionsDTO
     * @return List<LogisticsOrderDTO.TabListDTO>>
     * @author lei.nie
     * @date: 2026-04-20
     */
    List<LogisticsOrderDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 详情
     *
     * @param id String
     * @return LogisticsOrderDTO.ViewDTO
     * @author lei.nie
     * @date: 2026-04-20
     */
    LogisticsOrderDTO.ViewDTO view(String id);

    /**
     * 导出Excel
     *
     * @param dto      LogisticsOrderDTO.ExportDTO
     * @param response HttpServletResponse
     * @author lei.nie
     * @date: 2026-04-20
     */
    void exportList(LogisticsOrderDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 批量添加 用于寄修申请 物流下单
     *
     * @param entityList List<LogisticsOrderEntity>
     * @return List<AfterSaleDTO.LogisticsOrderResultDTO>
     */
    List<AfterSaleDTO.LogisticsOrderResultDTO> addBatch(List<LogisticsOrderEntity> entityList);

    /**
     * 删除
     *
     * @param id String
     * @return BatchResultDTO
     */
    BatchResultDTO delete(String id);

    /**
     * 取消
     *
     * @param id String
     * @return BatchResultDTO
     */
    BatchResultDTO cancel(String id);

    /**
     * 根据物流跟踪号查询物流下单信息
     *
     * @param trackNoList List<String>
     * @return List<LogisticsOrderDTO.ListDTO>
     */
    List<LogisticsOrderDTO.ListDTO> getLogisticsOrderListByTrackNo(List<String> trackNoList);

    /**
     * 批量取消
     *
     * @param codeList List<String>
     * @return List<AfterSaleDTO.LogisticsOrderResultDTO>
     */
    List<AfterSaleDTO.LogisticsOrderResultDTO> batchCancel(List<String> codeList);

    /**
     * 打印物流面单
     *
     * @param dto BaseIdsDTO.IdsDTO
     * @return LogisticsOrderDTO.LogisticsLabelPreviewDTO
     */
    LogisticsOrderDTO.LogisticsLabelPreviewDTO printLogisticsLabelPreview(BaseIdsDTO.IdsDTO dto);

    /**
     * 上传物流面单
     *
     * @param dto LogisticsOrderDTO.UploadFileDTO
     * @return String
     */
    String uploadLogisticLabel(LogisticsOrderDTO.UploadFileDTO dto);

    /**
     * 打印物流面单确认
     *
     * @param dto BaseIdsDTO.IdsDTO
     * @return String
     */
    String printLogisticsLabelConfirm(BaseIdsDTO.IdsDTO dto);

    /**
     * 获取物流面单
     *
     * @param dtoList LogisticsOrderDTO.LogisticsLabelDTO
     */
    List<BatchResultDTO> getLogisticsOrderLabel(List<LogisticsOrderDTO.LogisticsLabelDTO> dtoList);

    /**
     * 手动批量获取面单
     *
     * @param dto BaseIdsDTO.IdsDTO
     * @return List<BatchResultDTO>
     */
    List<BatchResultDTO> batchLogisticsLabel(BaseIdsDTO.IdsDTO dto);
}
