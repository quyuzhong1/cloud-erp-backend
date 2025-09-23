package com.erp.server.wms.service;
import com.common.business.enums.ClientTypeEnum;
import com.common.business.validator.ValidList;
import com.erp.model.wms.dto.SampleBorrowInfoDTO;
import com.erp.model.wms.entity.SampleReturnInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SampleReturnInfoDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 样品归还单主表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-08-20
 */
public interface SampleReturnInfoService extends SuperService<SampleReturnInfoEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SampleReturnInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    Boolean update(SampleReturnInfoDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author jack
    * @date: 2025-08-20
    * @param pagingParamDTO
    * @return PagingVO<SampleReturnInfoDTO.ListDTO>>
    */
    PagingVO<SampleReturnInfoDTO.ListDTO> paging(PagingDTO<SampleReturnInfoDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return List<SampleReturnInfoDTO.TabListDTO>>
    */
    List<SampleReturnInfoDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author jack
    * @date: 2025-08-20
    * @param id
    * @return
    */
    SampleReturnInfoDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(SampleReturnInfoDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    void updateAndSubmit(SampleReturnInfoDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author jack
     * @date: 2025-08-20
     * @param id
     * @return
     */
    BatchResultDTO submit(String id, ClientTypeEnum clientType);

    /**
    * 审核
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto, ClientTypeEnum clientType);

    /**
    * 反审核
    * @author jack
    * @date: 2025-08-20
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id, ClientTypeEnum clientType);

    /**
    * 删除
    * @author jack
    * @date: 2025-08-20
    * @param id
    * @return
    */
    BatchResultDTO delete(String id, ClientTypeEnum clientType);

    /**
    * 撤销
    * @author jack
    * @date: 2025-08-20
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id, ClientTypeEnum clientType);

    /**
    * 导出Excel
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @param response
    * @return
    */
    void exportList(SampleReturnInfoDTO.PagingParamDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, SampleReturnInfoEntity entity);
    /**
     * 作废
     * @author jack
     * @date: 2025-08-20
     * @param id
     * @return
     */
    BatchResultDTO invalid(String id,String remark, ClientTypeEnum clientType);

    List<SampleReturnInfoDTO.ListDTO> listReturnBySourceId(String sourceId);

    List<BatchResultDTO> generateSampleReturn(ValidList<SampleBorrowInfoDTO.SampleReturnView> list);

    List<SampleReturnInfoDTO.TabListDTO> tabListApp(PermissionsDTO dto);

    PagingVO<SampleReturnInfoDTO.ListDTO> pagingApp(PagingDTO<SampleReturnInfoDTO.PagingParamDTO> dto);
}
