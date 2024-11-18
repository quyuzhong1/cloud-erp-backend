package com.erp.server.workflow.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.ApproveParamDTO;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.model.workflow.entity.WorkOptionEntity;

import java.util.List;

/**
 * <p>
 *  工作台选项表服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-04-11
 */
public interface WorkOptionService extends SuperService<WorkOptionEntity> {
    /**
     * 待办模块-模块分类下拉
     * @return
     */
    List<WorkOptionDTO.WaitDoMenu> listWaitDoMenu(String sysClassify);

    /**
     * 待办模块-模块分类下拉
     * @return
     */
    List<WorkOptionDTO.WaitDoMenu> listOftenMenu(String sysClassify);

    /**
     * 新增待办模块
     * @Author Luo_WG
     * @Date 2023/4/20 19:45
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    Boolean addWaitDo(WorkOptionDTO.AddDTO dto);

    /**
     * 新增常用模块
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/20 19:45
     **/
    Boolean addOften(WorkOptionDTO.AddOftenDTO dto);

    /**
     * 编辑修改待办模块
     * @Author Luo_WG
     * @Date 2023/4/20 19:45
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    Boolean updateWaitDo(WorkOptionDTO.UpdateDTO dto);

    /**
     * 待办列表
     * @Author Luo_WG
     * @Date 2023/4/11 18:48
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.PurchaseReturnOrderDTO.PagingViewDTO>>
     **/
    List<WorkOptionDTO.PendingViewDTO> listPendingView();

    /**
     * 常用列表
     * @Author Luo_WG
     * @Date 2023/4/11 18:50
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.frequentlyViewDTO>>
     **/
    List<WorkOptionDTO.FrequentlyViewDTO> listFrequentlyView();

    /**
     * 立项阶段列表
     * @Author Luo_WG
     * @Date 2023/4/12 9:33
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.stageViewDTO>>
     **/
    List<WorkOptionDTO.StageViewDTO> stageView();

    /**
     * 移除
     * @Author Luo_WG
     * @Date 2023/4/24 13:03
     * @param id id
     * @return java.lang.Boolean
     **/
    Boolean delete(String id);

    /**
     * 审批中心-下拉搜索选项
     * @Author Luo_WG
     * @Date 2023/4/12 11:58
     * @return java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.ApproveSearchOptionDTO>
     **/
    List<WorkOptionDTO.ApproveSearchOptionDTO> approveSearchOption();

    /**
     * 审批中心-列表
     * @Author Luo_WG
     * @Date 2023/5/11 15:32
     * @param dto dto
     * @return java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.ApproveViewDTO>
     **/
    PagingVO<WorkOptionDTO.ApproveViewDTO> approveView(PagingDTO<WorkOptionDTO.ApproveViewParamDTO> dto);

    /**
     * 审核
     * @Author Luo_WG
     * @Date 2023/5/17 8:59
     * @param dto
     * @return void
     **/
    Boolean approve(ApproveParamDTO dto);
}
