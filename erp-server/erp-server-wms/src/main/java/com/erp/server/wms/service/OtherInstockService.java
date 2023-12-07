package com.erp.server.wms.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
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
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/5/17 15:13
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<OtherInstockDTO.ListDTO> paging(PagingDTO<OtherInstockDTO.SearchParamDTO> dto);
    /**
     * @description: 查寻数量
     * @author Will
     * @date: 2023/5/17 15:13
     * @param dto
     * @return List<ListStatusCountDTO>
     */
    List<OtherInstockDTO.ListStatusCountDTO> listCount(PermissionsDTO dto);
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/5/17 15:14
     * @param dto
     * @return String
     */
    String add(OtherInstockDTO.AddDTO dto);
    /**
     * @description: 新增并提交
     * @author Will
     * @date: 2023/5/17 15:14
     * @param dto
     * @return String
     */
    String addAndSubmit(OtherInstockDTO.AddDTO dto);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/5/17 15:14
     * @param dto
     * @return Boolean
     */
    Boolean update(OtherInstockDTO.UpdateDTO dto);
    /**
     * @description: 修改并提交
     * @author Will
     * @date: 2023/5/17 15:15
     * @param dto
     * @return Boolean
     */
    Boolean updateAndSubmit(OtherInstockDTO.UpdateDTO dto);
    /**
     * @description: 提交
     * @author Will
     * @date: 2023/5/17 15:15
     * @param ids
     * @return Boolean
     */
    Boolean submit(List<String> ids);
    /**
     * @description: 查询详情
     * @author Will
     * @date: 2023/5/17 15:15
     * @param id
     * @return ViewDTO
     */
    OtherInstockDTO.ViewDTO view(String id);
    /**
     * @description: 删除
     * @author Will
     * @date: 2023/5/17 15:15
     * @param ids
     * @return Boolean
     */
    Boolean delete(List<String> ids);
    /**
     * @description: 作废
     * @author Will
     * @date: 2023/5/17 15:16
     * @param ids
     * @param remark
     * @return Boolean
     */
    Boolean invalid(List<String> ids, String remark);
    /**
     * @description: 审核
     * @author Will
     * @date: 2023/12/5 11:58
     * @param id
     * @param type
     * @param comment
     */
    BatchResultDTO approve(String id, String type, String comment);
    /**
     * @description: 反审核
     * @author Will
     * @date: 2023/5/17 15:16
     * @param id
     * @return Boolean
     */
    BatchResultDTO disApprove(String id);
    /**
     * @description: 取消流程
     * @author Will
     * @date: 2023/5/17 15:16
     * @param ids
     * @return Boolean
     */
    Boolean cancelProcess(List<String> ids);
    /**
     * @description: 导出
     * @author Will
     * @date: 2023/5/17 15:17
     * @param dto
     * @param response
     * @return Boolean
     */
    Boolean exportExcel(OtherInstockDTO.SearchParamDTO dto, HttpServletResponse response);

    /**
     * @description: 更新金蝶状态等信息
     * @author Will
     * @date: 2023/5/23 17:41
     * @param id
     * @param syncKingdeeId
     * @return Boolean
     */
    Boolean updateSyncKingdeeId(String id, String syncKingdeeId);

    /**
     * PDA:列表查询
     * @Author Luo_WG
     * @Date 2023/8/23 9:58
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.OtherInstockDTO.PdaListDTO>
     **/
    PagingVO<OtherInstockDTO.PdaListDTO> PdaPaging(PagingDTO<OtherInstockDTO.PdaSearchParamDTO> dto);

    /**
     * PDA:列表数量
     * @Author Luo_WG
     * @Date 2023/8/23 10:35
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.OtherInstockDTO.PdaListStatusCountDTO>
     **/
    List<OtherInstockDTO.PdaListStatusCountDTO> pdaListCount(PermissionsDTO dto);
}
