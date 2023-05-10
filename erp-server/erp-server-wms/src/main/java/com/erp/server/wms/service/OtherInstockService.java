package com.erp.server.wms.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.OtherInstockDTO;
import com.erp.model.wms.entity.OtherInstockEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface OtherInstockService extends SuperService<OtherInstockEntity> {

    PagingVO<OtherInstockDTO.ListDTO> paging(PagingDTO<OtherInstockDTO.SearchParamDTO> dto);

    List<OtherInstockDTO.ListStatusCountDTO> listCount(PermissionsDTO dto);

    String add(OtherInstockDTO.AddDTO dto);

    String addAndSubmit(OtherInstockDTO.AddDTO dto);

    Boolean update(OtherInstockDTO.UpdateDTO dto);

    Boolean updateAndSubmit(OtherInstockDTO.UpdateDTO dto);

    Boolean submit(List<String> ids);

    OtherInstockDTO.ViewDTO view(String id);

    Boolean delete(List<String> ids);

    Boolean invalid(List<String> ids, String remark);

    void approve(BaseApproveParamDTO baseApproveParamDTO);

    Boolean disApprove(List<String> ids);

    Boolean cancelProcess(List<String> ids);

    Boolean exportExcel(OtherInstockDTO.SearchParamDTO dto, HttpServletResponse response);
}
