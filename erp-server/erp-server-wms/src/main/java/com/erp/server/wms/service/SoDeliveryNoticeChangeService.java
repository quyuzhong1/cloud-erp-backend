package com.erp.server.wms.service;
import com.erp.model.wms.entity.SoDeliveryNoticeChangeEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SoDeliveryNoticeChangeDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 发货通知变更单 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-10-23
 */
public interface SoDeliveryNoticeChangeService extends SuperService<SoDeliveryNoticeChangeEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-10-23
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SoDeliveryNoticeChangeDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2024-10-23
    * @param dto
    * @return
    */
    Boolean update(SoDeliveryNoticeChangeDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author lrp
    * @date: 2024-10-23
    * @param pagingParamDTO
    * @return PagingVO<SoDeliveryNoticeChangeDTO.ListDTO>>
    */
    PagingVO<SoDeliveryNoticeChangeDTO.ListDTO> paging(PagingDTO<SoDeliveryNoticeChangeDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author lrp
    * @date: 2024-10-23
    * @param dto
    * @return List<SoDeliveryNoticeChangeDTO.TabListDTO>>
    */
    List<SoDeliveryNoticeChangeDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 详情
     *
     * @param viewIdDTO
     * @return
     * @author lrp
     * @date: 2024-10-23
     */
    SoDeliveryNoticeChangeDTO.ViewDTO view(SoDeliveryNoticeChangeDTO.ViewIdDTO viewIdDTO);

    /**
    * 新增并提交审核
    * @author lrp
    * @date: 2024-10-23
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(SoDeliveryNoticeChangeDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author lrp
    * @date: 2024-10-23
    * @param dto
    * @return
    */
    void updateAndSubmit(SoDeliveryNoticeChangeDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author lrp
     * @date: 2024-10-23
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author lrp
    * @date: 2024-10-23
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author lrp
    * @date: 2024-10-23
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author lrp
    * @date: 2024-10-23
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 撤销
    * @author lrp
    * @date: 2024-10-23
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
    * 导出Excel
    * @author lrp
    * @date: 2024-10-23
    * @param dto
    * @param response
    * @return
    */
    void exportList(SoDeliveryNoticeChangeDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, SoDeliveryNoticeChangeEntity entity);

    PagingVO<SoDeliveryNoticeChangeDTO.ProductDTO> addProductPaging(PagingDTO<SoDeliveryNoticeChangeDTO.ProductAddDTO> dto);
}
