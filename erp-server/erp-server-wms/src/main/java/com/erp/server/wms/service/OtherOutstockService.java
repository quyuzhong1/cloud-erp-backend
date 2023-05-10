package com.erp.server.wms.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.OtherOutstockDTO;
import com.erp.model.wms.entity.OtherOutstockEntity;

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
public interface OtherOutstockService extends SuperService<OtherOutstockEntity> {

    PagingVO<OtherOutstockDTO.ListDTO> paging(PagingDTO<OtherOutstockDTO.SearchParamDTO> dto);

    List<OtherOutstockDTO.ListStatusCountDTO> listCount(PermissionsDTO dto);

    String add(OtherOutstockDTO.AddDTO dto);

    String addAndSubmit(OtherOutstockDTO.AddDTO dto);

    Boolean update(OtherOutstockDTO.UpdateDTO dto);

    Boolean updateAndSubmit(OtherOutstockDTO.UpdateDTO dto);

    Boolean submit(List<String> ids);

    OtherOutstockDTO.ViewDTO view(String id);

    Boolean delete(List<String> ids);

    Boolean invalid(List<String> ids, String remark);

    void approve(BaseApproveParamDTO baseApproveParamDTO);

    Boolean disApprove(List<String> ids);

    Boolean cancelProcess(List<String> ids);

    Boolean exportExcel(OtherOutstockDTO.SearchParamDTO dto, HttpServletResponse response);
}
