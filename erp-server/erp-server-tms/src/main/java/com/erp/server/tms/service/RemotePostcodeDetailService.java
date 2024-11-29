package com.erp.server.tms.service;
import com.erp.model.tms.entity.RemotePostcodeDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.RemotePostcodeDetailDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 偏远邮编明细表 服务类
 * </p>
 *
 * @author jack
 * @since 2024-11-29
 */
public interface RemotePostcodeDetailService extends SuperService<RemotePostcodeDetailEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2024-11-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(RemotePostcodeDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2024-11-29
    * @param dto
    * @return
    */
    Boolean update(RemotePostcodeDetailDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author jack
    * @date: 2024-11-29
    * @param pagingParamDTO
    * @return PagingVO<RemotePostcodeDetailDTO.ListDTO>>
    */
    PagingVO<RemotePostcodeDetailDTO.ListDTO> paging(PagingDTO<RemotePostcodeDetailDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2024-11-29
    * @param dto
    * @return List<RemotePostcodeDetailDTO.TabListDTO>>
    */
    List<RemotePostcodeDetailDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author jack
    * @date: 2024-11-29
    * @param id
    * @return
    */
    RemotePostcodeDetailDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author jack
    * @date: 2024-11-29
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(RemotePostcodeDetailDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author jack
    * @date: 2024-11-29
    * @param dto
    * @return
    */
    void updateAndSubmit(RemotePostcodeDetailDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author jack
     * @date: 2024-11-29
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author jack
    * @date: 2024-11-29
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author jack
    * @date: 2024-11-29
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author jack
    * @date: 2024-11-29
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 撤销
    * @author jack
    * @date: 2024-11-29
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
    * 导出Excel
    * @author jack
    * @date: 2024-11-29
    * @param dto
    * @param response
    * @return
    */
    void exportList(RemotePostcodeDetailDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, RemotePostcodeDetailEntity entity);

}
