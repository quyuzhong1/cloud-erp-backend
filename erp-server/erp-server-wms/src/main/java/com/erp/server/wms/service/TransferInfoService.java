package com.erp.server.wms.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.TransferInfoDTO;
import com.erp.model.wms.entity.TransferInfoEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 直接调拨单主表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface TransferInfoService extends SuperService<TransferInfoEntity> {

    PagingVO<TransferInfoDTO.ListDTO> paging(PagingDTO<TransferInfoDTO.SearchParamDTO> dto);

    List<TransferInfoDTO.ListStatusCountDTO> listCount(PermissionsDTO dto);

    String add(TransferInfoDTO.AddDTO dto);

    String addAndSubmit(TransferInfoDTO.AddDTO dto);

    Boolean update(TransferInfoDTO.UpdateDTO dto);

    Boolean updateAndSubmit(TransferInfoDTO.UpdateDTO dto);

    Boolean submit(List<String> ids);

    TransferInfoDTO.ViewDTO view(String id);

    Boolean delete(List<String> ids);

    Boolean invalid(List<String> ids, String remark);

    void approve(BaseApproveParamDTO baseApproveParamDTO);

    Boolean disApprove(List<String> ids);

    Boolean cancelProcess(List<String> ids);

    Boolean exportExcel(TransferInfoDTO.SearchParamDTO dto, HttpServletResponse response);

    List<TransferInfoEntity> listBySourceIds(List<String> sourceIds);
}
