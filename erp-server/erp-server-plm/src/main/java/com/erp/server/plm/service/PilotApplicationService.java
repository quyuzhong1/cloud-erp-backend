package com.erp.server.plm.service;
import com.common.business.dto.FindUserDTO;
import com.erp.model.plm.entity.PilotApplicationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.PilotApplicationDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 试产/量产申请 服务类
 * </p>
 *
 * @author tmj
 * @since 2024-08-27
 */
public interface PilotApplicationService extends SuperService<PilotApplicationEntity> {

    /**
    * 新增
    * @author tmj
    * @date: 2024-08-27
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PilotApplicationDTO.AddDTO dto);

    /**
    * 修改
    * @author tmj
    * @date: 2024-08-27
    * @param dto
    * @return
    */
    Boolean update(PilotApplicationDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author tmj
    * @date: 2024-08-27
    * @param pagingParamDTO
    * @return PagingVO<PilotApplicationDTO.ListDTO>>
    */
    PagingVO<PilotApplicationDTO.ListDTO> paging(PagingDTO<PilotApplicationDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author tmj
    * @date: 2024-08-27
    * @param dto
    * @return List<PilotApplicationDTO.TabListDTO>>
    */
    List<PilotApplicationDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author tmj
    * @date: 2024-08-27
    * @param id
    * @return
    */
    PilotApplicationDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author tmj
    * @date: 2024-08-27
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(PilotApplicationDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author tmj
    * @date: 2024-08-27
    * @param dto
    * @return
    */
    void updateAndSubmit(PilotApplicationDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author tmj
     * @date: 2024-08-27
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author tmj
    * @date: 2024-08-27
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author tmj
    * @date: 2024-08-27
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author tmj
    * @date: 2024-08-27
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 撤销
    * @author tmj
    * @date: 2024-08-27
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
    * 导出Excel
    * @author tmj
    * @date: 2024-08-27
    * @param dto
    * @param response
    * @return
    */
    void exportList(PilotApplicationDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, PilotApplicationEntity entity);

    /**
     * 下推采购申请
     */
    BatchResultDTO pushPurchaseApplication(PilotApplicationDTO.PushPurchaseApplicationDTO dto, FindUserDTO userInfo);
}
