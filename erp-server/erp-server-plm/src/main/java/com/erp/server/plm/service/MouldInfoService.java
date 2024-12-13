package com.erp.server.plm.service;

import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.MouldInfoDTO;
import com.erp.model.plm.dto.MouldInfoImportDTO;
import com.erp.model.plm.dto.MouldRefundVoucherDTO;
import com.erp.model.plm.entity.MouldInfoEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 模具主表 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
 */
public interface MouldInfoService extends SuperService<MouldInfoEntity> {
    /**
     * 分页
     * @param dto 参数
     */
    PagingVO<MouldInfoDTO.PagingViewDTO> paging(PagingDTO<MouldInfoDTO.PagingParamDTO> dto);
    /**
     * 详情
     * @param id 参数
     */
    MouldInfoDTO.ViewDTO view(String id);

    /**
     * 暂存
     * @param dto 参数
     */
    BatchResultDTO draft(MouldInfoDTO.CommonDTO dto);

    /**
     * 新增并提交
     * @param dto 参数
     */
    BatchResultDTO addAndSubmit(MouldInfoDTO.UpdateDTO dto);

    /**
     * 提交
     * @param id 主表id
     */
    BatchResultDTO submit(String id);

    /**
     * 取消流程
     * @param id 主表id
     */
    BatchResultDTO cancelProcess(String id);

    /**
     * 审核
     * @param dto 参数
     */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
     * 反审核
     * @param id 主表id
     */
    BatchResultDTO disApprove(String id);

    /**
     * 作废
     * @param id 主表id
     */
    BatchResultDTO invalid(String id, String remark);

    /**
     * 编辑备注
     * @param id 明细id
     * @param remark 备注
     */
    BatchResultDTO updateRemark(String id, String remark);

    /**
     * 编辑存放位置
     * @param id 明细id
     * @param dto 参数
     */
    BatchResultDTO updateStoreLocation(String id, MouldInfoDTO.StoreLocationDTO dto);

    /**
     * 批量编辑启用时间
     * @param id 明细id
     */
    BatchResultDTO updateEnableTime(String id, LocalDate enableTime);

    /**
     * 暂存
     * @param dto 参数
     */
    void export(MouldInfoDTO.PagingParamDTO dto);

    /**
     * 下单跟踪
     * @param dto 参数
     */
    PagingVO<MouldInfoDTO.OrderTrackingViewDTO> orderTracking(PagingDTO<MouldInfoDTO.PagingParamDTO> dto);

    /**
     * 下单跟踪
     * @param dto 参数
     */
    List<MouldInfoDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 费用返还确认
     * @param dto 参数
     */
    void returnConfirm(MouldInfoDTO.ReturnConfirmDTO dto);

    /**
     * 关联下单产品
     * @param dto 参数
     */
    void refProduct(MouldInfoDTO.RefProductDTO dto);

    /**
     * 下单跟踪明细
     * @param dto 参数
     */
    PagingVO<MouldInfoDTO.OrderTrackingDetailDTO> orderTrackingDetail(PagingDTO<MouldInfoDTO.OrderTrackingDetailParamDTO> dto);

    /**
     * 费用返还明细
     * @param detailId 明细id
     */
    MouldRefundVoucherDTO returnConfirmDetail(String detailId);

    /**
     * 下单跟踪导出
     * @param dto 参数
     */
    void orderTrackingExport(MouldInfoDTO.PagingParamDTO dto);

    /**
     * 下单跟踪合计
     * @param dto 参数
     */
    MouldInfoDTO.OrderTrackingTotalDTO orderTrackingTotal(MouldInfoDTO.PagingParamDTO dto);

    /**
     * 下单跟踪明细合计
     * @param dto 参数
     */
    MouldInfoDTO.OrderTrackingDetailTotalDTO orderTrackingDetailTotal(MouldInfoDTO.OrderTrackingDetailParamDTO dto);

    /**
     * 下单跟踪明细导出
     * @param dto 参数
     */
    void orderTrackingDetailExport(MouldInfoDTO.OrderTrackingDetailParamDTO dto);

    /**
     * 导出列表
     * @param dto 导出参数
     */
    PagingVO<MouldInfoDTO.MouldInfoExportDTO> exportMouldInfo(PagingDTO<MouldInfoDTO.PagingParamDTO> dto);

    /**
     * 导出下单跟踪
     * @param dto 参数
     */
    PagingVO<MouldInfoDTO.OrderTrackingExportDTO> exportOrderTracking(PagingDTO<MouldInfoDTO.PagingParamDTO> dto);

    /**
     * 导出下单跟踪明细
     * @param dto 参数
     */
    PagingVO<MouldInfoDTO.OrderTrackingDetailExportDTO> exportOrderTrackingDetail(PagingDTO<MouldInfoDTO.OrderTrackingDetailParamDTO> dto);

    /**
     * 导入
     * @param excelFile 文件
     */
    MouldInfoImportDTO importExcel(MultipartFile excelFile, HttpServletResponse response);
}
