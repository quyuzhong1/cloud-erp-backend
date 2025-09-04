package com.erp.server.oms.service;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.entity.SoReceiptEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.SoReceiptDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 收款单 服务类
 * </p>
 *
 * @author lrp
 * @since 2025-08-28
 */
public interface SoReceiptService extends SuperService<SoReceiptEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2025-08-28
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SoReceiptDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2025-08-28
    * @param dto
    * @return
    */
    Boolean update(SoReceiptDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author lrp
    * @date: 2025-08-28
    * @param pagingParamDTO
    * @return PagingVO<SoReceiptDTO.ListDTO>>
    */
    PagingVO<SoReceiptDTO.ListDTO> paging(PagingDTO<SoReceiptDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author lrp
    * @date: 2025-08-28
    * @param dto
    * @return List<SoReceiptDTO.TabListDTO>>
    */
    List<SoReceiptDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author lrp
    * @date: 2025-08-28
    * @param id
    * @return
    */
    SoReceiptDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author lrp
    * @date: 2025-08-28
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(SoReceiptDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author lrp
    * @date: 2025-08-28
    * @param dto
    * @return
    */
    void updateAndSubmit(SoReceiptDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author lrp
     * @date: 2025-08-28
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author lrp
    * @date: 2025-08-28
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author lrp
    * @date: 2025-08-28
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author lrp
    * @date: 2025-08-28
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 撤销
    * @author lrp
    * @date: 2025-08-28
    * @param id
    * @return
    */
    Boolean cancelProcess(String id);

    /**
    * 导出Excel
    * @author lrp
    * @date: 2025-08-28
    * @param dto
    * @param response
    * @return
    */
    boolean exportList(SoReceiptDTO.PagingParamDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, SoReceiptEntity entity);

    List<SoReceiptDTO.SoInfoAndReceiptDTO> listSoReceiptBySoCode(SoReceiptDTO.SoSearchDTO dto);

    /**
     * 通过销售订单id查询收款金额
     * @param soIds
     * @return
     */
    List<SoReceiptDTO.AmountDTO> queryAmountBySoIds(List<String> soIds);

    List<SoReceiptDTO.SoViewDTO> getSoViewDTO(SoInfoEntity soInfo);

    void addOrUpdateBySo(SoInfoEntity soInfo, String customerId, List<SoReceiptDTO.SoViewDTO> soReceiptDTOList);

    void autoSubmitBySo(SoInfoEntity entity);

    void autoApproveBySo(SoInfoEntity entity);
}
