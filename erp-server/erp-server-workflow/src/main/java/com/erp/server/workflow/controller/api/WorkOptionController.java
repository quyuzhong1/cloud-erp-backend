package com.erp.server.workflow.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.server.workflow.service.WorkOptionService;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 工作台
 * @author LUO_WG
 * @since 2023-04-11
 */
@RestController
@RequestMapping("/workOption")
public class WorkOptionController extends BaseController {
    @Resource
    private WorkOptionService workOptionService;

    /**
     * 待办模块-模块分类下拉
     * @return
     */
    @GetMapping("/listWaitDoMenu")
    public ApiResult<List<WorkOptionDTO.WaitDoMenu>> listWaitDoMenu(@RequestParam("sysClassify") String sysClassify) {
        List<WorkOptionDTO.WaitDoMenu> waitDoMenus = workOptionService.listWaitDoMenu(sysClassify);
        return success(waitDoMenus);
    }

    /**
     * 常用模块-模块分类下拉
     * @return
     */
    @GetMapping("/listOftenMenu")
    public ApiResult<List<WorkOptionDTO.WaitDoMenu>> listOftenMenu(@RequestParam("sysClassify") String sysClassify) {
        List<WorkOptionDTO.WaitDoMenu> waitDoMenus = workOptionService.listOftenMenu(sysClassify);
        return success(waitDoMenus);
    }

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/20 19:45
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/addWaitDo")
    public ApiResult addWaitDo(@RequestBody WorkOptionDTO.AddDTO dto) {
        Boolean flag = workOptionService.addWaitDo(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/20 19:45
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/updateWaitDo")
    public ApiResult updateWaitDo(@RequestBody WorkOptionDTO.UpdateDTO dto) {
        Boolean flag = workOptionService.updateWaitDo(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 代办列表
     * @Author Luo_WG
     * @Date 2023/4/11 18:48
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.PurchaseReturnOrderDTO.PagingViewDTO>>
     **/
    @GetMapping("/listPendingView")
    public ApiResult<List<WorkOptionDTO.PendingViewDTO>> listPendingView() {
        List<WorkOptionDTO.PendingViewDTO> pendingViewList = workOptionService.listPendingView();
        return success(pendingViewList);
    }

    /**
     * 常用列表
     * @Author Luo_WG
     * @Date 2023/4/11 18:50
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.frequentlyViewDTO>>
     **/
    @GetMapping("/listFrequentlyView")
    public ApiResult<List<WorkOptionDTO.FrequentlyViewDTO>> listFrequentlyView() {
        List<WorkOptionDTO.FrequentlyViewDTO> frequentlyViewDTO = workOptionService.listFrequentlyView();
        return success(frequentlyViewDTO);
    }

    /**
     * 立项阶段列表
     * @Author Luo_WG
     * @Date 2023/4/12 9:33
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.stageViewDTO>>
     **/
    @GetMapping("/stageView")
    public ApiResult<List<WorkOptionDTO.StageViewDTO>> stageView() {
        List<WorkOptionDTO.StageViewDTO> stageViewDTOList = workOptionService.stageView();
        return success(stageViewDTOList);
    }

    /**
     * 删除
     * @Author Luo_WG
     * @Date 2023/4/24 13:03
     * @param id id
     * @return java.lang.Boolean
     **/
    @PostMapping("/delete")
    public ApiResult<List<WorkOptionDTO.ApproveViewDTO>> delete(@RequestParam("id") String id) {
        Boolean flag = workOptionService.delete(id);
        return flag == true ? success() : failure();
    }

    /**
     * 审批中心-下拉搜索选项
     * @Author Luo_WG
     * @Date 2023/4/12 9:35
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.approveViewDTO>>
     **/
    @GetMapping("/approveSearchOption")
    public ApiResult<List<WorkOptionDTO.ApproveSearchOptionDTO>> approveSearchOption() {
        List<WorkOptionDTO.ApproveSearchOptionDTO> approveSearchOptionDTO = workOptionService.approveSearchOption();
        return success(approveSearchOptionDTO);
    }

    /**
     * 审批中心-列表
     * @Author Luo_WG
     * @Date 2023/4/12 9:35
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.approveViewDTO>>
     **/
    @PostMapping("/approveView")
    public ApiResult<PagingVO<List<WorkOptionDTO.ApproveViewDTO>>> approveView(@RequestBody @Validated PagingDTO<WorkOptionDTO.ApproveViewParamDTO> dto) {
        PagingVO<List<WorkOptionDTO.ApproveViewDTO>> approveViewDTO = workOptionService.approveView(dto);
        return success(approveViewDTO);
    }
}
