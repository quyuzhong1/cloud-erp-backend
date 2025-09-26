package com.erp.server.srm.service;
import com.erp.model.srm.entity.PayableInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.srm.dto.PayableInfoDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author will
 * @since 2025-09-25
 */
public interface PayableInfoService extends SuperService<PayableInfoEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-09-25
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PayableInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2025-09-25
    * @param dto
    * @return
    */
    Boolean update(PayableInfoDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author will
    * @date: 2025-09-25
    * @param pagingParamDTO
    * @return PagingVO<PayableInfoDTO.ListDTO>>
    */
    PagingVO<PayableInfoDTO.ListDTO> paging(PagingDTO<PayableInfoDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author will
    * @date: 2025-09-25
    * @param dto
    * @return List<PayableInfoDTO.TabListDTO>>
    */
    List<PayableInfoDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author will
    * @date: 2025-09-25
    * @param id
    * @return
    */
    PayableInfoDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author will
    * @date: 2025-09-25
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(PayableInfoDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author will
    * @date: 2025-09-25
    * @param dto
    * @return
    */
    void updateAndSubmit(PayableInfoDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author will
     * @date: 2025-09-25
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author will
    * @date: 2025-09-25
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author will
    * @date: 2025-09-25
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author will
    * @date: 2025-09-25
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 撤销
    * @author will
    * @date: 2025-09-25
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);


    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, PayableInfoEntity entity);

}
