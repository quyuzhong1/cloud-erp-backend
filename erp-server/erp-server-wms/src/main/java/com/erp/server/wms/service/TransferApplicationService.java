package com.erp.server.wms.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.TransferApplicationDTO;
import com.erp.model.wms.entity.TransferApplicationEntity;

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
public interface TransferApplicationService extends SuperService<TransferApplicationEntity> {

    PagingVO<TransferApplicationDTO.ListDTO> paging(PagingDTO<TransferApplicationDTO.SearchParamDTO> dto);

    List<TransferApplicationDTO.ListStatusCountDTO> listCount(PermissionsDTO dto);

    String add(TransferApplicationDTO.AddDTO dto);

    String addAndSubmit(TransferApplicationDTO.AddDTO dto);

    Boolean update(TransferApplicationDTO.UpdateDTO dto);

    Boolean updateAndSubmit(TransferApplicationDTO.UpdateDTO dto);

    Boolean submit(List<String> ids);

    TransferApplicationDTO.ViewDTO view(String id);

    Boolean delete(List<String> ids);

    Boolean invalid(List<String> ids, String remark);

    void approve(BaseApproveParamDTO baseApproveParamDTO);

    Boolean disApprove(List<String> ids);

    Boolean cancelProcess(List<String> ids);

    Boolean exportExcel(TransferApplicationDTO.SearchParamDTO dto, HttpServletResponse response);
}
