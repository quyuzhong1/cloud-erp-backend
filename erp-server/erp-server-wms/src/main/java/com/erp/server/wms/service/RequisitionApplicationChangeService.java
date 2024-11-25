package com.erp.server.wms.service;

import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.RequisitionApplicationChangeDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.PickingListsEntity;
import com.erp.model.wms.entity.RequisitionApplicationChangeDetailEntity;
import com.erp.model.wms.entity.RequisitionApplicationChangeEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 要货申请变更单 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-11-18
 */
public interface RequisitionApplicationChangeService extends SuperService<RequisitionApplicationChangeEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-11-18
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(RequisitionApplicationChangeDTO.ViewDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2024-11-18
    * @param dto
    * @return
    */
    Boolean update(RequisitionApplicationChangeDTO.ViewDTO dto);

    /**
    * 分页列表查询
    * @author lrp
    * @date: 2024-11-18
    * @param pagingParamDTO
    * @return PagingVO<RequisitionApplicationChangeDTO.ListDTO>>
    */
    PagingVO<RequisitionApplicationChangeDTO.ListDTO> paging(PagingDTO<RequisitionApplicationChangeDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author lrp
    * @date: 2024-11-18
    * @param dto
    * @return List<RequisitionApplicationChangeDTO.TabListDTO>>
    */
    List<RequisitionApplicationChangeDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 详情
     *
     * @param dto
     * @return
     * @author lrp
     * @date: 2024-11-18
     */
    RequisitionApplicationChangeDTO.ViewDTO view(RequisitionApplicationChangeDTO.ViewIdDTO dto);

    /**
    * 新增并提交审核
    * @author lrp
    * @date: 2024-11-18
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(RequisitionApplicationChangeDTO.ViewDTO dto);

    /**
    * 修改并提交审核
    * @author lrp
    * @date: 2024-11-18
    * @param dto
    * @return
    */
    void updateAndSubmit(RequisitionApplicationChangeDTO.ViewDTO dto);

     /**
     * 提交审核
     * @author lrp
     * @date: 2024-11-18
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author lrp
    * @date: 2024-11-18
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author lrp
    * @date: 2024-11-18
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author lrp
    * @date: 2024-11-18
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 撤销
    * @author lrp
    * @date: 2024-11-18
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
    * 导出Excel
    * @author lrp
    * @date: 2024-11-18
    * @param dto
    * @param response
    * @return
    */
    void exportList(RequisitionApplicationChangeDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, RequisitionApplicationChangeEntity entity);

    PagingVO<RequisitionApplicationChangeDTO.ProductDTO> addProductPaging(PagingDTO<RequisitionApplicationChangeDTO.ProductAddDTO> dto);

    BatchResultDTO invalid(String id, String remark);

    List<RequisitionApplicationChangeEntity> listNotHandleByBusinessIds(List<String> ids);

    List<RequisitionApplicationChangeDetailEntity> listNotHandleDetailByBusinessDetailIds(List<String> detailIds);

    void generateByPickingList(PickingListsDTO.AddChangeDTO addChangeDTO, PickingListsDTO.UpdateDTO dto, PickingListsEntity entity);
}
