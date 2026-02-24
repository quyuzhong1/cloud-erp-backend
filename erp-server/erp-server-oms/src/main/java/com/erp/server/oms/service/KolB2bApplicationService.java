package com.erp.server.oms.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.KolB2bApplicationDTO;
import com.erp.model.oms.dto.excel.KolB2bApplicationImportExcelDTO;
import com.erp.model.oms.entity.KolB2bApplicationEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * B2B寄样申请主表 服务类
 * </p>
 *
 * @author will
 * @since 2025-12-01
 */
public interface KolB2bApplicationService extends SuperService<KolB2bApplicationEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-12-01
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(KolB2bApplicationDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2025-12-01
    * @param dto
    * @return
    */
    Boolean update(KolB2bApplicationDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author will
    * @date: 2025-12-01
    * @param pagingParamDTO
    * @return PagingVO<KolB2bApplicationDTO.ListDTO>>
    */
    PagingVO<KolB2bApplicationDTO.ListDTO> paging(PagingDTO<KolB2bApplicationDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author will
    * @date: 2025-12-01
    * @param dto
    * @return List<KolB2bApplicationDTO.TabListDTO>>
    */
    List<KolB2bApplicationDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author will
    * @date: 2025-12-01
    * @param id
    * @return
    */
    KolB2bApplicationDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author will
    * @date: 2025-12-01
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(KolB2bApplicationDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author will
    * @date: 2025-12-01
    * @param dto
    * @return
    */
    void updateAndSubmit(KolB2bApplicationDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author will
     * @date: 2025-12-01
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author will
    * @date: 2025-12-01
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author will
    * @date: 2025-12-01
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author will
    * @date: 2025-12-01
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);
    /**
    * 作废
    * @author will
    * @date: 2025-12-01
    * @param id
    * @param remark
    * @return
    */
    BatchResultDTO invalid(String id, String remark);

    /**
    * 撤销
    * @author will
    * @date: 2025-12-01
    * @param dto
    * @return
    */
    BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto);

    /**
    * 导出Excel
    * @author will
    * @date: 2025-12-01
    * @param dto
    * @param response
    * @return
    */
    void exportList(KolB2bApplicationDTO.PagingParamDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, KolB2bApplicationEntity entity);
    /**
     * 导入excel
     * @author will
     * @date 2025/12/1 16:25
     * @param excelFile
     * @return Boolean
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);
    /**
     * 查询关联单据
     * @author will
     * @date 2025/12/1 16:34
     * @param id
     * @return RefBillDTO
     */
    KolB2bApplicationDTO.RefBillDTO listRefBill(String id);
    /**
     * 生成销售订单保存
     * @author will
     * @date 2025/12/1 18:47
     * @param list
     * @return Boolean
     */
    Boolean generateSoInfo(ValidList<KolB2bApplicationDTO.GenerateSoInfoDTO> list);
    /**
     * 生成回片登记保存
     * @author will
     * @date 2025/12/1 18:48
     * @param list
     * @return Boolean
     */
    Boolean generateFeedback(ValidList<KolB2bApplicationDTO.GenerateFeedbackDTO> list);
    /**
     * 导入数据处理
     * @author will
     * @date 2025/12/3 10:39
     * @param successList
     * @param errorList
     * @return void
     */
    void handleImportSuccessList(List<KolB2bApplicationImportExcelDTO> successList, List<KolB2bApplicationImportExcelDTO> errorList);
}
