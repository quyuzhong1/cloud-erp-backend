package com.erp.server.wms.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.TransferInfoDTO;
import com.erp.model.wms.dto.TransferInfoDetailDTO;
import com.erp.model.wms.entity.TransferInfoEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 直接调拨单主表
 *
 * @author will
 * @since 2023-05-10
 */
public interface TransferInfoService extends SuperService<TransferInfoEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/5/15 11:22
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<TransferInfoDTO.ListDTO> paging(PagingDTO<TransferInfoDTO.SearchParamDTO> dto);
    /**
     * @description: 查询数量
     * @author Will
     * @date: 2023/5/15 11:23
     * @param dto
     * @return List<ListStatusCountDTO>
     */
    List<TransferInfoDTO.ListStatusCountDTO> listCount(PermissionsDTO dto);
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/5/15 11:23
     * @param dto
     * @return String
     */
    String add(TransferInfoDTO.AddDTO dto);
    /**
     * @description: 新增并提交
     * @author Will
     * @date: 2023/5/15 11:23
     * @param dto
     * @return String
     */
    String addAndSubmit(TransferInfoDTO.AddDTO dto);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/5/15 11:25
     * @param dto
     * @return Boolean
     */
    Boolean update(TransferInfoDTO.UpdateDTO dto);
    /**
     * @description: 修改并提交
     * @author Will
     * @date: 2023/5/15 11:25
     * @param dto
     * @return Boolean
     */
    Boolean updateAndSubmit(TransferInfoDTO.UpdateDTO dto);
    /**
     * @description: 提交
     * @author Will
     * @date: 2023/5/15 11:25
     * @param ids
     * @return Boolean
     */
    Boolean submit(List<String> ids);
    /**
     * @description: 查看详情
     * @author Will
     * @date: 2023/5/15 11:25
     * @param id
     * @return ViewDTO
     */
    TransferInfoDTO.ViewDTO view(String id);
    /**
     * @description: 删除
     * @author Will
     * @date: 2023/5/15 11:25
     * @param ids
     * @return Boolean
     */
    Boolean delete(List<String> ids);
    /**
     * @description: 作废
     * @author Will
     * @date: 2023/5/15 11:25
     * @param ids
     * @param remark
     * @return Boolean
     */
    Boolean invalid(List<String> ids, String remark);
    /**
     * @description: 审核
     * @author Will
     * @date: 2023/5/15 11:25
     * @param baseApproveParamDTO
     */
    void approve(BaseApproveParamDTO baseApproveParamDTO,Boolean isSyncKingDee);
    /**
     * @description: 反审核
     * @author Will
     * @date: 2023/5/15 11:26
     * @param ids
     * @return Boolean
     */
    Boolean disApprove(List<String> ids, Boolean isPushKingDee);
    /**
     * @description: 取消流程
     * @author Will
     * @date: 2023/5/15 11:26
     * @param ids
     * @return Boolean
     */
    Boolean cancelProcess(List<String> ids);
    /**
     * @description: 导出
     * @author Will
     * @date: 2023/5/15 11:26
     * @param dto
     * @param response
     * @return Boolean
     */
    Boolean exportExcel(TransferInfoDTO.SearchParamDTO dto, HttpServletResponse response);
    /**
     * @description: 根据来源ids查询
     * @author Will
     * @date: 2023/5/15 11:26
     * @param sourceIds
     * @return List<TransferInfoEntity>
     */
    List<TransferInfoEntity> listBySourceIds(List<String> sourceIds);
    /**
     * @description: 更新金蝶状态等信息
     * @author Will
     * @date: 2023/5/23 17:41
     * @param id
     * @param syncKingdeeId
     * @return Boolean
     */
    Boolean updateSyncKingdeeId(String id,String syncKingdeeId);
    /**
     * @description: 根据编码查询有效直接直接调拨单
     * @author Will
     * @date: 2023/6/28 17:35
     * @param code
     * @return ViewDTO
     */
    TransferInfoDTO.ViewDTO viewTransferInfoByCode(String code);

    /**
     * 验证SKU是否缺货
     * @param dto
     * @return
     */
    String checkSkuInventory(TransferInfoDTO.CommonDTO dto, List<TransferInfoDetailDTO.AddDTO> detailList);

    /**
     * pda:列表查询
     * @Author Luo_WG
     * @Date 2023/8/24 15:36
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.TransferInfoDTO.PdaListDTO>
     **/
    PagingVO<TransferInfoDTO.PdaListDTO> pdaPaging(PagingDTO<TransferInfoDTO.PdaSearchParamDTO> dto);

    /**
     * PDA:列表数量
     * @Author Luo_WG
     * @Date 2023/8/24 15:51
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.TransferInfoDTO.PdaListStatusCountDTO>
     **/
    List<TransferInfoDTO.PdaListStatusCountDTO> pdaListCount(PermissionsDTO dto);
}
