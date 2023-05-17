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
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/5/17 16:14
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<OtherOutstockDTO.ListDTO> paging(PagingDTO<OtherOutstockDTO.SearchParamDTO> dto);
    /**
     * @description: 查询数量
     * @author Will
     * @date: 2023/5/17 16:14
     * @param dto
     * @return List<ListStatusCountDTO>
     */
    List<OtherOutstockDTO.ListStatusCountDTO> listCount(PermissionsDTO dto);
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/5/17 16:14
     * @param dto
     * @return String
     */
    String add(OtherOutstockDTO.AddDTO dto);
    /**
     * @description: 新增并提交
     * @author Will
     * @date: 2023/5/17 16:15
     * @param dto
     * @return String
     */
    String addAndSubmit(OtherOutstockDTO.AddDTO dto);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/5/17 16:15
     * @param dto
     * @return Boolean
     */
    Boolean update(OtherOutstockDTO.UpdateDTO dto);
    /**
     * @description: 修改并提交
     * @author Will
     * @date: 2023/5/17 16:15
     * @param dto
     * @return Boolean
     */
    Boolean updateAndSubmit(OtherOutstockDTO.UpdateDTO dto);
    /**
     * @description: 提交
     * @author Will
     * @date: 2023/5/17 16:15
     * @param ids
     * @return Boolean
     */
    Boolean submit(List<String> ids);
    /**
     * @description: 查看详情
     * @author Will
     * @date: 2023/5/17 16:16
     * @param id
     * @return ViewDTO
     */
    OtherOutstockDTO.ViewDTO view(String id);

    Boolean delete(List<String> ids);

    Boolean invalid(List<String> ids, String remark);

    void approve(BaseApproveParamDTO baseApproveParamDTO);

    Boolean disApprove(List<String> ids);

    Boolean cancelProcess(List<String> ids);

    Boolean exportExcel(OtherOutstockDTO.SearchParamDTO dto, HttpServletResponse response);
}
