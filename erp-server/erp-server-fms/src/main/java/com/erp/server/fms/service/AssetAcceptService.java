package com.erp.server.fms.service;
import com.erp.model.fms.entity.AssetAcceptEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.fms.dto.AssetAcceptDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 资产验收表 服务类
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
public interface AssetAcceptService extends SuperService<AssetAcceptEntity> {

    /**
    * 新增
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AssetAcceptDTO.AddDTO dto);

    /**
    * 修改
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    Boolean update(AssetAcceptDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author wuht
    * @date: 2025-10-11
    * @param pagingParamDTO
    * @return PagingVO<AssetAcceptDTO.ListDTO>>
    */
    PagingVO<AssetAcceptDTO.ListDTO> paging(PagingDTO<AssetAcceptDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return List<AssetAcceptDTO.TabListDTO>>
    */
    List<AssetAcceptDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author wuht
    * @date: 2025-10-11
    * @param id
    * @return
    */
    AssetAcceptDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(AssetAcceptDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    void updateAndSubmit(AssetAcceptDTO.UpdateDTO dto);

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
    void exportList(AssetAcceptDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, AssetAcceptEntity entity);

}
