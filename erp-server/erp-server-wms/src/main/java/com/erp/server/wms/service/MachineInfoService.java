package com.erp.server.wms.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.MachineInfoDTO;
import com.erp.model.wms.dto.MachineSubComponentsDTO;
import com.erp.model.wms.entity.MachineInfoEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 加工单
 *
 * @author will
 * @since 2023-05-10
 */
public interface MachineInfoService extends SuperService<MachineInfoEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/5/15 17:51
     * @param dto 
     * @return PagingVO<ListDTO> 
     */
    PagingVO<MachineInfoDTO.ListDTO> paging(PagingDTO<MachineInfoDTO.SearchParamDTO> dto);
    /**
     * @description: 查询数量
     * @author Will
     * @date: 2023/5/15 17:52
     * @param dto 
     * @return List<ListStatusCountDTO> 
     */
    List<MachineInfoDTO.ListStatusCountDTO> listCount(PermissionsDTO dto);
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/5/15 17:52
     * @param dto 
     * @return String 
     */
    String add(MachineInfoDTO.AddDTO dto);
    /**
     * @description: 新增并提交
     * @author Will
     * @date: 2023/5/15 17:52
     * @param dto
     * @return String 
     */
    String addAndSubmit(MachineInfoDTO.AddDTO dto);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/5/15 17:53
     * @param dto
     * @return Boolean
     */
    Boolean update(MachineInfoDTO.UpdateDTO dto);
    /**
     * @description: 修改并提交
     * @author Will
     * @date: 2023/5/15 17:53
     * @param dto
     * @return Boolean
     */
    Boolean updateAndSubmit(MachineInfoDTO.UpdateDTO dto);
    /**
     * @description: 提交
     * @author Will
     * @date: 2023/5/15 17:53
     * @param ids
     * @return Boolean
     */
    Boolean submit(List<String> ids);
    /**
     * @description: 查看详情
     * @author Will
     * @date: 2023/5/15 17:54
     * @param id 
     * @return ViewDTO 
     */
    MachineInfoDTO.ViewDTO view(String id);
    /**
     * @description: 删除
     * @author Will
     * @date: 2023/5/15 17:54
     * @param ids 
     * @return Boolean 
     */
    Boolean delete(List<String> ids);
    /**
     * @description: 作废
     * @author Will
     * @date: 2023/5/15 17:55
     * @param ids
     * @param remark
     * @return Boolean
     */
    Boolean invalid(List<String> ids, String remark);
    /**
     * @description: 审核
     * @author Will
     * @date: 2023/5/15 17:55
     * @param baseApproveParamDTO
     */
    void approve(BaseApproveParamDTO baseApproveParamDTO);
    /**
     * @description: 反审核
     * @author Will
     * @date: 2023/5/15 17:55
     * @param ids
     * @return Boolean
     */
    Boolean disApprove(List<String> ids);
    /**
     * @description: 取消流程
     * @author Will
     * @date: 2023/5/15 17:55
     * @param ids
     * @return Boolean
     */
    Boolean cancelProcess(List<String> ids);
    /**
     * @description: 导出excel
     * @author Will
     * @date: 2023/5/15 17:55
     * @param dto
     * @param response
     * @return Boolean
     */
    Boolean exportExcel(MachineInfoDTO.SearchParamDTO dto, HttpServletResponse response);
    /**
     * 子件明细数据查询
     * @author Will
     * @date: 2023/5/16 19:41
     * @param skuId
     * @return List<ViewDTO>
     */
    List<MachineSubComponentsDTO.ViewDTO> viewSubComponents(String skuId);
}
