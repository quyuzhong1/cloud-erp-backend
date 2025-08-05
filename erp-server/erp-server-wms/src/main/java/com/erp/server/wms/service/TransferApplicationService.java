package com.erp.server.wms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.PickingDetailDTO;
import com.erp.model.wms.dto.SingleApproveParamDTO;
import com.erp.model.wms.dto.TransferApplicationDTO;
import com.erp.model.wms.entity.TransferApplicationEntity;

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
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/5/10 18:52
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<TransferApplicationDTO.ListDTO> paging(PagingDTO<TransferApplicationDTO.SearchParamDTO> dto);
    /**
     * @description: 列表数据查询
     * @author Will
     * @date: 2023/5/10 18:52
     * @param dto
     * @return List<ListStatusCountDTO>
     */
    List<TransferApplicationDTO.ListStatusCountDTO> listCount(PermissionsDTO dto);
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/5/10 18:55
     * @param dto 
     * @return String 
     */
    String add(TransferApplicationDTO.AddDTO dto);
    /**
     * @description: 新增并提交
     * @author Will
     * @date: 2023/5/10 18:55
     * @param dto 
     * @return String 
     */
    String addAndSubmit(TransferApplicationDTO.AddDTO dto);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/5/10 18:55
     * @param dto 
     * @return Boolean 
     */
    Boolean update(TransferApplicationDTO.UpdateDTO dto);
    /**
     * @description: 修改并提交
     * @author Will
     * @date: 2023/5/10 18:55
     * @param dto 
     * @return Boolean 
     */
    Boolean updateAndSubmit(TransferApplicationDTO.UpdateDTO dto);
    /**
     * @description: 提交
     * @author Will
     * @date: 2023/5/10 18:55
     * @param entity
     * @return Boolean 
     */
    BatchResultDTO submit(TransferApplicationEntity entity);
    /**
     * @description: 查看详情
     * @author Will
     * @date: 2023/5/10 18:56
     * @param id 
     * @return ViewDTO 
     */
    TransferApplicationDTO.ViewDTO view(String id);
    /**
     * @description: 删除
     * @author Will
     * @date: 2023/5/10 18:56
     * @param ids
     * @return Boolean 
     */
    Boolean delete(List<String> ids);
    /**
     * @description: 作废
     * @author Will
     * @date: 2023/5/10 18:56
     * @param ids
     * @param remark
     * @return Boolean
     */
    Boolean invalid(List<String> ids, String remark);
    /**
     * @description: 审核
     * @author Will
     * @date: 2023/5/10 18:56
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess
     */
    BatchResultDTO approve(TransferApplicationEntity entity, String type, String comment, Boolean isNeedProcess);
    /**
     * @description: 审核结束
     * @author Will
     * @date: 2023/8/2 15:09
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess
     * @return Boolean
     */
    Boolean approveEnd(TransferApplicationEntity entity, String type, String comment, Boolean isNeedProcess);
    /**
     * 单个单据的审核
     * @Author Luo_WG
     * @Date 2023/6/30 10:23
     * @param singleApproveParamDTO
     * @return void
     **/
    void singleApprove(SingleApproveParamDTO singleApproveParamDTO);
    /**
     * @description: 反审核
     * @author Will
     * @date: 2023/5/10 18:56
     * @param entity
     * @return Boolean
     */
    BatchResultDTO disApprove(TransferApplicationEntity entity);
    /**
     * @description: 取消流程
     * @author Will
     * @date: 2023/5/10 18:57
     * @param ids
     * @return Boolean
     */
    Boolean cancelProcess(List<String> ids);
    /**
     * @param dto
     * @return Boolean
     * @description: 导出
     * @author Will
     * @date: 2023/5/10 18:57
     */
    Boolean exportExcel(TransferApplicationDTO.SearchParamDTO dto);
    /**
     * @description: 下推直接调拨单
     * @author Will
     * @date: 2023/5/10 18:57
     * @param detailIdList
     * @return List<ViewGenerateTransferInfoDTO>
     */
    List<TransferApplicationDTO.ViewGenerateTransferInfoDTO> viewGenerateTransferInfo(List<String> detailIdList);
    /**
     * @description: 下推分布式调出
     * @author Will
     * @date: 2023/5/10 18:57
     * @param detailIdList
     * @return List<ViewGenerateTransferInfoDTO>
     */
    List<TransferApplicationDTO.ViewGenerateTransferInfoDTO> viewGenerateTransferOut(List<String> detailIdList);
    /**
     * @description: 下推直接调拨单保存
     * @author Will
     * @date: 2023/5/12 10:53
     * @param validList
     * @return Boolean
     */
    Boolean generateTransferInfo(ValidList<TransferApplicationDTO.GenerateTransferInfoDTO> validList);
    /**
     * @description: 下推分布式调出单保存
     * @author Will
     * @date: 2023/5/15 9:38
     * @param validList
     * @return Boolean
     */
    Boolean generateTransferOut(ValidList<TransferApplicationDTO.GenerateTransferInfoDTO> validList);

    /**
     * @description: 查询拣货名称
     * @author Will
     * @date: 2023/5/16 12:09
     * @param dto
     * @return List<ListDTO>
     */
    List<PickingDetailDTO.ListDTO> listPickingDetail(PickingDetailDTO.SearchParamDTO dto);

    /**
     * 下推加工单-列表查询
     * @Author Luo_WG
     * @Date 2023/6/29 17:50
     * @param ids
     * @param isAutoMachine 是否需要自动生成的
     * @param qty 审核通过填写的批准加工数量
     * @return java.util.List<com.erp.model.wms.dto.TransferApplicationDTO.ViewGenerateMachineInfo>
     **/
    List<TransferApplicationDTO.ViewGenerateMachineInfo> viewGenerateMachineInfo(List<String> ids, Boolean isAutoMachine, Integer qty);

    /**
     * 下推加工单-保存
     * @Author Luo_WG
     * @Date 2023/6/29 17:49
     * @param list
     * @return java.lang.Boolean
     **/
    Boolean saveGenerateMachineInfo(List<TransferApplicationDTO.ViewGenerateMachineInfo> list);

    PagingVO<TransferApplicationDTO.ListDTO> exportTransferApplication(PagingDTO<TransferApplicationDTO.SearchParamDTO> dto);

    List<TransferApplicationEntity> listByCodes(List<String> list);

    void updateApproveStatus(TransferApplicationDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO);
}
