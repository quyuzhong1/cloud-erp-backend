package com.erp.server.workflow.service;

import com.common.business.service.SuperService;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.model.workflow.entity.WorkOptionEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * <p>
 *  工作台选项表服务类
 * </p>
 *
 * @author LUO_WG
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
     * 新增代办模块
     * @Author Luo_WG
     * @Date 2023/4/20 19:45
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    Boolean addWaitDo(WorkOptionDTO.AddDTO dto);

    /**
     * 编辑修改代办模块
     * @Author Luo_WG
     * @Date 2023/4/20 19:45
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    Boolean updateWaitDo(WorkOptionDTO.UpdateDTO dto);

    /**
     * 代办列表
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

}
