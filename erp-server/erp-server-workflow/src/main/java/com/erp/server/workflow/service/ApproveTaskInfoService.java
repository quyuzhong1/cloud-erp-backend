package com.erp.server.workflow.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.ApproveTaskInfoDTO;
import com.erp.model.workflow.entity.ApproveTaskInfoEntity;

import java.util.List;

/**
 * <p>
 * 三方生成查询 服务类
 * </p>
 *
 * @author will
 * @since 2025-05-27
 */
public interface ApproveTaskInfoService extends SuperService<ApproveTaskInfoEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-05-27
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ApproveTaskInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2025-05-27
    * @param dto
    * @return
    */
    Boolean  update(ApproveTaskInfoDTO.UpdateDTO dto);

    /**
     * tab列表
     * @author will
     * @date 2025/5/27 10:03
     * @param dto
     * @return List<TabListDTO>
     */
    List<ApproveTaskInfoDTO.TabListDTO> tabList(PermissionsDTO dto);
    /**
     * 分页查询
     * @author will
     * @date 2025/5/27 10:04
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<ApproveTaskInfoDTO.ListDTO> paging(PagingDTO<ApproveTaskInfoDTO.PagingParamDTO> dto);
    /**
     * 查询详情
     * @author will
     * @date 2025/5/27 11:02
     * @param id
     * @return ViewDTO
     */
    ApproveTaskInfoDTO.ViewDTO view(String id);
    /**
     * 导出
     * @author will
     * @date 2025/5/27 11:07
     * @param dto
     * @return void
     */
    void exportList(ApproveTaskInfoDTO.PagingParamDTO dto);
    /**
     * 重新生成
     * @author will
     * @date 2025/5/27 16:11
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO afreshGenerate(String id);
    /**
     * 根据业务id和业务key查询
     * @author will
     * @date 2025/6/27 17:48
     * @param businessId
     * @param businessKey
     * @return ApproveTaskInfoEntity
     */
    ApproveTaskInfoEntity getByBusinessIdAndKey( String businessId,  String businessKey);
    /**
     * 删除
     * @author will
     * @date 2025/7/7 14:55
     * @param type
     * @param thirdInstanceId
     * @param thirdApprovalCode
     * @return Boolean
     */
    Boolean deleteByThird( String type, String thirdInstanceId, String thirdApprovalCode);
}
