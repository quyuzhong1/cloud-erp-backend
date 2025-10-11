package com.erp.server.fms.service;
import com.erp.model.fms.entity.AssetDisposalEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.fms.dto.AssetDisposalDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 资产处置单主表 服务类
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
public interface AssetDisposalService extends SuperService<AssetDisposalEntity> {

    /**
    * 新增
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AssetDisposalDTO.AddDTO dto);

    /**
    * 修改
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    Boolean update(AssetDisposalDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author wuht
    * @date: 2025-10-11
    * @param pagingParamDTO
    * @return PagingVO<AssetDisposalDTO.ListDTO>>
    */
    PagingVO<AssetDisposalDTO.ListDTO> paging(PagingDTO<AssetDisposalDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return List<AssetDisposalDTO.TabListDTO>>
    */
    List<AssetDisposalDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author wuht
    * @date: 2025-10-11
    * @param id
    * @return
    */
    AssetDisposalDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(AssetDisposalDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    void updateAndSubmit(AssetDisposalDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author wuht
     * @date: 2025-10-11
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author wuht
    * @date: 2025-10-11
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author wuht
    * @date: 2025-10-11
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);
    /**
    * 作废
    * @author wuht
    * @date: 2025-10-11
    * @param id
    * @param remark
    * @return
    */
    BatchResultDTO invalid(String id, String remark);

    /**
    * 撤销
    * @author wuht
    * @date: 2025-10-11
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
    * 导出Excel
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @param response
    * @return
    */
    void exportList(AssetDisposalDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, AssetDisposalEntity entity);

}
