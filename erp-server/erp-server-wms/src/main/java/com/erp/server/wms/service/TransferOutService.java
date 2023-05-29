package com.erp.server.wms.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.TransferOutDTO;
import com.erp.model.wms.entity.TransferOutEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
     * @param param
     * @param response
     */
    void exportList(TransferOutDTO.ExportDTO param, HttpServletResponse response);

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
     * 新增并提交审核
     * @param dto
     * @return
     */
    void addAndSubmit(TransferOutDTO.AddDTO dto);

    /**
     * 修改并提交审核
     * @param dto
     */
    void updateAndSubmit(TransferOutDTO.UpdateDTO dto);

    /**
     * 审核
     * @param baseApproveParamDTO
     */
    void approve(BaseApproveParamDTO baseApproveParamDTO);

    /**
     * 删除
     * @param ids
     */
    void delete(List<String> ids);

}
