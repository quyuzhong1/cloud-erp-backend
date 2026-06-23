package com.erp.server.wms.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.MachineInfoDTO;
import com.erp.model.wms.dto.MachineSubComponentsDTO;
import com.erp.model.wms.entity.MachineInfoEntity;

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
    MachineInfoEntity add(MachineInfoDTO.AddDTO dto);
    /**
     * @description: 新增并提交
     * @author Will
     * @date: 2023/5/15 17:52
     * @param dto
     * @return String 
     */
    MachineInfoEntity addAndSubmit(MachineInfoDTO.AddDTO dto);
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
    BatchResultDTO updateAndSubmit(MachineInfoDTO.UpdateDTO dto);
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
     * @param entity
     * @param comment
     * @param type
     * @param isNeedProcess
     */
    BatchResultDTO approve(MachineInfoEntity entity, String type, String comment, Boolean isNeedProcess);
    /**
     * @description: 反审核
     * @author Will
     * @date: 2023/5/15 17:55
     * @param entity
     * @return Boolean
     */
    BatchResultDTO disApprove(MachineInfoEntity entity);
    /**
     * @description: 取消流程
     * @author Will
     * @date: 2023/5/15 17:55
     * @param dto
     * @return Boolean
     */
    Boolean cancelProcess(ApproveDTO.BatchCancelProcessDTO dto);
    /**
     * @param dto
     * @return Boolean
     * @description: 导出excel
     * @author Will
     * @date: 2023/5/15 17:55
     */
    Boolean exportExcel(MachineInfoDTO.SearchParamDTO dto);
    /**
     * 根据明细id查询子件明细数据
     * @author Will
     * @date: 2023/5/16 19:41
     * @param detailId
     * @return List<ViewDTO>
     */
    List<MachineSubComponentsDTO.ViewDTO> viewSubComponents(String detailId);
    /**
     * @description: 通过SKU查询BOM子集
     * @author Will
     * @date: 2023/5/18 17:03
     * @param dto
     * @return List<ViewDTO>
     */
    List<MachineSubComponentsDTO.ViewDTO> viewBomSubComponents(MachineSubComponentsDTO.ViewBomParamDTO dto);

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
     * 根据来源类型和来源id查找
     * @return
     */
    List<MachineInfoEntity> findBySourceTypeAndSourceCode(String sourceType, String sourceCode);

    /**
     * 根据来源单据ids查询
     * @Author Luo_WG
     * @Date 2023/7/10 16:43
     * @param ids ids
     * @return java.util.List<com.erp.model.wms.entity.MachineInfoEntity>
     **/
    List<MachineInfoEntity> listBySourceIds(List<String> ids);

    /**
     * 根据sku 信息查询加工单信息
     * @author yl
     * @date 2023-10-11 19:54
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.MachineInfoDTO.ListDTO>
     */
    List<MachineInfoDTO.ListDTO> listBySku(MachineInfoDTO.FindInfoBySkuDTO dto);

    /**
     * 导出
     */
    PagingVO<MachineInfoDTO.ListDTO> exportMachineInfo(PagingDTO<MachineInfoDTO.SearchParamDTO> dto);
    /**
     * 修复加工单明细id数据
     * @author will
     * @date 2024/11/27 14:28
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO handleErrorData(String id);


    /**
     * 单事务提交
     */
    BatchResultDTO submit(String id);

    /**
     * 单事务作废
     */
    BatchResultDTO invalid(String id, String reason);

    /**
     * 单事务提交
     */
    BatchResultDTO submitEntity(MachineInfoEntity entity);

    /**
     * 取消流程
     */
    BatchResultDTO cancelProcessEntity(MachineInfoEntity entity);

    List<List<MachineSubComponentsDTO.ViewDTO>> batchViewBomSubComponents(MachineSubComponentsDTO.BatchViewBomParamDTO dto);
}
