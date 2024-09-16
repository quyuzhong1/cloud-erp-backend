package com.erp.server.wms.service;
import com.erp.model.wms.entity.SubcontractReturnEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SubcontractReturnDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 委外退料单 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-09-15
 */
public interface SubcontractReturnService extends SuperService<SubcontractReturnEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-09-15
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SubcontractReturnDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-09-15
    * @param dto
    * @return
    */
    Boolean update(SubcontractReturnDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author zdy
    * @date: 2024-09-15
    * @param pagingParamDTO
    * @return PagingVO<SubcontractReturnDTO.ListDTO>>
    */
    PagingVO<SubcontractReturnDTO.ListDTO> paging(PagingDTO<SubcontractReturnDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author zdy
    * @date: 2024-09-15
    * @param dto
    * @return List<SubcontractReturnDTO.TabListDTO>>
    */
    List<SubcontractReturnDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author zdy
    * @date: 2024-09-15
    * @param id
    * @return
    */
    SubcontractReturnDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author zdy
    * @date: 2024-09-15
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(SubcontractReturnDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author zdy
    * @date: 2024-09-15
    * @param dto
    * @return
    */
    void updateAndSubmit(SubcontractReturnDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author zdy
     * @date: 2024-09-15
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author zdy
    * @date: 2024-09-15
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author zdy
    * @date: 2024-09-15
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author zdy
    * @date: 2024-09-15
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);
    /**
    * 作废
    * @author zdy
    * @date: 2024-09-15
    * @param id
    * @param remark
    * @return
    */
    BatchResultDTO invalid(String id, String remark);

    /**
    * 撤销
    * @author zdy
    * @date: 2024-09-15
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
    * 导出Excel
    * @author zdy
    * @date: 2024-09-15
    * @param dto
    * @param response
    * @return
    */
    void exportList(SubcontractReturnDTO.PagingParamDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, SubcontractReturnEntity entity);

    /**
     * 添加产品
     * @param dto
     * @return
     */
    List<SubcontractReturnDTO.SubcontractDetailListDTO> listSubcontractDetail(SubcontractReturnDTO.DetailPagingParamDTO dto);
}
