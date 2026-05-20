package com.erp.server.oms.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.AddressParseDTO;
import com.erp.model.oms.dto.KolB2cApplicationCancelCallbackDTO;
import com.erp.model.oms.dto.KolB2cApplicationDTO;
import com.erp.model.oms.dto.excel.KolB2cApplicationAddressImportExcelDTO;
import com.erp.model.oms.dto.excel.KolB2cApplicationDetailImportExcelDTO;
import com.erp.model.oms.dto.excel.KolB2cApplicationImportExcelDTO;
import com.erp.model.oms.entity.KolB2cApplicationEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * B2C寄样申请单 服务类
 * </p>
 *
 * @author jack
 * @since 2025-12-04
 */
public interface KolB2cApplicationService extends SuperService<KolB2cApplicationEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-12-04
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(KolB2cApplicationDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-12-04
    * @param dto
    * @return
    */
    Boolean update(KolB2cApplicationDTO.UpdateDTO dto);

    /**
     * 更新明细备注
     * @param id 主表id
     * @param detailId 明细id
     * @param remark 明细备注
     * @return 是否成功
     */
    Boolean updateDetailRemark(String id, String detailId, String remark);

    /**
    * 分页列表查询
    * @author jack
    * @date: 2025-12-04
    * @param pagingParamDTO
    * @return PagingVO<KolB2cApplicationDTO.ListDTO>>
    */
    PagingVO<KolB2cApplicationDTO.ListDTO> paging(PagingDTO<KolB2cApplicationDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2025-12-04
    * @param dto
    * @return List<KolB2cApplicationDTO.TabListDTO>>
    */
    List<KolB2cApplicationDTO.TabListDTO> tabList(PermissionsDTO dto);

    void updateKolSubStatus(String kolId);

    /**
    * 详情
    * @author jack
    * @date: 2025-12-04
    * @param id
    * @return
    */
    KolB2cApplicationDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author jack
    * @date: 2025-12-04
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(KolB2cApplicationDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author jack
    * @date: 2025-12-04
    * @param dto
    * @return
    */
    void updateAndSubmit(KolB2cApplicationDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author jack
     * @date: 2025-12-04
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author jack
    * @date: 2025-12-04
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author jack
    * @date: 2025-12-04
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author jack
    * @date: 2025-12-04
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);
    /**
    * 作废
    * @author jack
    * @date: 2025-12-04
    * @param id
    * @param remark
    * @return
    */
    BatchResultDTO invalid(String id, String remark);

    /**
     * 业务取消
     * @param id 主键
     * @return 结果
     */
    BatchResultDTO cancel(String id);

    /**
     * 根据拆分单回传刷新取消状态
     * @param mainId 主单id
     * @param failReason 取消失败原因
     */
    void refreshCancelStatusBySubOrder(String mainId, String failReason);

    /**
     * Handle domestic cancel success callback after DMP push success.
     * @param dto callback payload
     */
    void handleDomesticCancelPushSuccess(KolB2cApplicationCancelCallbackDTO dto);

    /**
     * Handle domestic cancel fail callback after DMP push fail.
     * @param dto callback payload
     */
    void handleDomesticCancelPushFail(KolB2cApplicationCancelCallbackDTO dto);

    /**
     * 撤销
     * @author jack
    * @date: 2025-12-04
    * @param dto
    * @return
    */
    BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto);

    /**
    * 导出Excel
    * @author jack
    * @date: 2025-12-04
    * @param dto
    * @param response
    * @return
    */
    void exportList(KolB2cApplicationDTO.PagingParamDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, KolB2cApplicationEntity entity);

    Boolean importFile(BaseDTO.ImportDTO dto);

    List<KolB2cApplicationDTO.DetailViewDTO> detailView(List<String> detailIdList);

    Boolean generateReturnPiece(List<KolB2cApplicationDTO.DetailViewDTO> list);

    void importKolB2cApplication(BaseDTO.ImportDTO dto);

    List<KolB2cApplicationImportExcelDTO> handleImportSuccessList(List<KolB2cApplicationImportExcelDTO> successList, List<KolB2cApplicationImportExcelDTO> errorList, List<KolB2cApplicationDetailImportExcelDTO> detailSuccessList, List<KolB2cApplicationAddressImportExcelDTO> addressSuccessList , String importType);

    AddressParseDTO.ParseResultDTO addressParse(AddressParseDTO.ParseRequestDTO dto);
}
