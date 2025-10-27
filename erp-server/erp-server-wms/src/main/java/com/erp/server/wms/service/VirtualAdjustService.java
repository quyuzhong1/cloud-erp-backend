package com.erp.server.wms.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.VirtualAdjustDTO;
import com.erp.model.wms.dto.VirtualAdjustDetailDTO;
import com.erp.model.wms.entity.VirtualAdjustEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 虚拟仓调整单主表 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-06-09
 */
public interface VirtualAdjustService extends SuperService<VirtualAdjustEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-06-09
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(VirtualAdjustDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-06-09
    * @param dto
    * @return
    */
    Boolean update(VirtualAdjustDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author zdy
    * @date: 2025-06-09
    * @param pagingParamDTO
    * @return PagingVO<VirtualAdjustDTO.ListDTO>>
    */
    PagingVO<VirtualAdjustDTO.ListDTO> paging(PagingDTO<VirtualAdjustDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author zdy
    * @date: 2025-06-09
    * @param dto
    * @return List<VirtualAdjustDTO.TabListDTO>>
    */
    List<VirtualAdjustDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author zdy
    * @date: 2025-06-09
    * @param id
    * @return
    */
    VirtualAdjustDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author zdy
    * @date: 2025-06-09
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(VirtualAdjustDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author zdy
    * @date: 2025-06-09
    * @param dto
    * @return
    */
    void updateAndSubmit(VirtualAdjustDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author zdy
     * @date: 2025-06-09
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author zdy
    * @date: 2025-06-09
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author zdy
    * @date: 2025-06-09
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author zdy
    * @date: 2025-06-09
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);
    /**
    * 作废
    * @author zdy
    * @date: 2025-06-09
    * @param id
    * @param remark
    * @return
    */
    BatchResultDTO invalid(String id, String remark);

    /**
    * 撤销
    * @author zdy
    * @date: 2025-06-09
    * @param dto
    * @return
    */
   BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto);

    /**
    * 导出Excel
    * @author zdy
    * @date: 2025-06-09
    * @param dto
    * @param response
    * @return
    */
    Boolean exportList(VirtualAdjustDTO.PagingParamDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, VirtualAdjustEntity entity);

    void downloadTemplate(HttpServletResponse response);

    VirtualAdjustDTO.ImportDTO importFile(MultipartFile excelFile, List<VirtualAdjustDetailDTO.AddDTO> detailList, HttpServletResponse response);
}
