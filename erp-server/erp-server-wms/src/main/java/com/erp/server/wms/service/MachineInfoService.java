package com.erp.server.wms.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.MachineInfoDTO;
import com.erp.model.wms.entity.MachineInfoEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 加工单 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface MachineInfoService extends SuperService<MachineInfoEntity> {

    PagingVO<MachineInfoDTO.ListDTO> paging(PagingDTO<MachineInfoDTO.SearchParamDTO> dto);

    List<MachineInfoDTO.ListStatusCountDTO> listCount(PermissionsDTO dto);

    String add(MachineInfoDTO.AddDTO dto);

    String addAndSubmit(MachineInfoDTO.AddDTO dto);

    Boolean update(MachineInfoDTO.UpdateDTO dto);

    Boolean updateAndSubmit(MachineInfoDTO.UpdateDTO dto);

    Boolean submit(List<String> ids);

    MachineInfoDTO.ViewDTO view(String id);

    Boolean delete(List<String> ids);

    Boolean invalid(List<String> ids, String remark);

    void approve(BaseApproveParamDTO baseApproveParamDTO);

    Boolean disApprove(List<String> ids);

    Boolean cancelProcess(List<String> ids);

    Boolean exportExcel(MachineInfoDTO.SearchParamDTO dto, HttpServletResponse response);
}
