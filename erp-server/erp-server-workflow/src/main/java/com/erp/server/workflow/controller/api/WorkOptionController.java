package com.erp.server.workflow.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.workflow.dto.ApproveParamDTO;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.server.workflow.service.WorkOptionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 工作台
 * @author Luo_WG
 * @since 2023-04-11
 */
@RestController
@LogSystemModule("首页工作台")
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
    @LogAction(value = LogActionEnum.INSERT, desc = "新增工作台选项")
    @PostMapping("/addWaitDo")
    public ApiResult<Objects> addWaitDo(@RequestBody WorkOptionDTO.AddDTO dto) {
        Boolean flag = workOptionService.addWaitDo(dto);
        return Boolean.TRUE.equals(flag) ? success() : failure();
    }

    /**
     * 新增常用模块
     * @Author Luo_WG
     * @Date 2023/7/7 16:28
     * @param dto
     * @return java.lang.Boolean
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "新增工作台常用模块")
    @PostMapping("/addOften")
    public ApiResult<Objects> addOften(@RequestBody WorkOptionDTO.AddOftenDTO dto) {
        Boolean flag = workOptionService.addOften(dto);
        return Boolean.TRUE.equals(flag) ? success() : failure();
    }

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/20 19:45
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "修改工作台选项:id={id}")
    @PostMapping("/updateWaitDo")
    public ApiResult<Objects> updateWaitDo(@RequestBody WorkOptionDTO.UpdateDTO dto) {
        Boolean flag = workOptionService.updateWaitDo(dto);
        return Boolean.TRUE.equals(flag) ? success() : failure();
    }

    /**
     * 待办列表
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
    @LogAction(value = LogActionEnum.DELETE, desc = "删除工作台选项")
    @PostMapping("/delete")
    public ApiResult<Objects> delete(@RequestParam("id") String id) {
        Boolean flag = workOptionService.delete(id);
        return Boolean.TRUE.equals(flag) ? success() : failure();
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
    public ApiResult<PagingVO<WorkOptionDTO.ApproveViewDTO>> approveView(@RequestBody @Validated PagingDTO<WorkOptionDTO.ApproveViewParamDTO> dto) {
        PagingVO<WorkOptionDTO.ApproveViewDTO> approveViewDTO = workOptionService.approveView(dto);
        return success(approveViewDTO);
    }

    /**
     * 审批中心-审核按钮
     * @Author Luo_WG
     * @Date 2023/5/17 10:57
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.APPROVE, desc = "审批中心审核")
    @PostMapping("/approve")
    public ApiResult<Objects> approve(@RequestBody @Validated ApproveParamDTO dto) {
        Boolean approve = workOptionService.approve(dto);
        return approve == Boolean.TRUE ? success() : failure();
    }
}
