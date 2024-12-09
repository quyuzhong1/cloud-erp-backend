package com.erp.server.tms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.RemotePostcodeDTO;
import com.erp.model.tms.entity.RemotePostcodeEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 偏远邮编组 服务类
 * </p>
 *
 * @author jack
 * @since 2024-11-29
 */
public interface RemotePostcodeService extends SuperService<RemotePostcodeEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2024-11-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(RemotePostcodeDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2024-11-29
    * @param dto
    * @return
    */
    Boolean update(RemotePostcodeDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author jack
    * @date: 2024-11-29
    * @param pagingParamDTO
    * @return PagingVO<RemotePostcodeDTO.ListDTO>>
    */
    PagingVO<RemotePostcodeDTO.ListDTO> paging(PagingDTO<RemotePostcodeDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2024-11-29
    * @param dto
    * @return List<RemotePostcodeDTO.TabListDTO>>
    */
    List<RemotePostcodeDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author jack
    * @date: 2024-11-29
    * @param id
    * @return
    */
    RemotePostcodeDTO.ViewDTO view(String id);

    /**
    * 删除
    * @author jack
    * @date: 2024-11-29
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 导出Excel
    * @author jack
    * @date: 2024-11-29
    * @param dto
    * @param response
    * @return
    */
    Boolean exportList(RemotePostcodeDTO.ExportDTO dto, HttpServletResponse response);
    /**
     * 导出Excel的查询
     * @author jack
     * @date: 2024-11-29
     * @param pagingParamDTO
     * @return
     */
    PagingVO<RemotePostcodeDTO.ExportListDTO> listExport(PagingDTO<RemotePostcodeDTO.PagingParamDTO> pagingParamDTO);
    /**
     * 邮编远程查询（分页型）
     * @author jack
     * @date:  2024-11-29
     * @param dto
     * @return ApiResult<PagingVO<RemotePostcodeDTO.ListDTO>>
     */
    PagingVO<RemotePostcodeDTO.ListDTO> pagingSelect(PagingDTO<RemotePostcodeDTO.SelectDTO> dto);
    /**
     * 邮编下拉值
     * @author jack
     * @date:  2024-11-29
     * @return ApiResult<List<RemotePostcodeDTO.ListDTO>>
     */
    List<RemotePostcodeDTO.ListDTO> select(RemotePostcodeDTO.SelectDTO dto);
}
