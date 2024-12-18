package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.PickingDetailDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.server.wms.service.PickingListsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 拣货单 前端控制器
 * </p>
 *
 * @author liaohui
 * @since 2024-06-07
 */
@RestController
@RequestMapping("/picking-lists")
public class PickingListsController extends BaseController {

    @Resource
    private PickingListsService pickingListsService;

    /**
     * 分页查询
     *
     * @param dto 分页查询条件
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<PickingListsDTO.PagingView>> paging(@RequestBody @Validated PagingDTO<PickingListsDTO.PagingParam> dto) {
        PagingVO<PickingListsDTO.PagingView> pagingVO = pickingListsService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 修改
     *
     * @param dto 编辑参数
     **/
    @PostMapping("/update")
    public ApiResult<String> update(@RequestBody @Validated PickingListsDTO.UpdateDTO dto) {
        pickingListsService.update(dto);
        return success();
    }


    /**
     * 修改数量弹窗
     *
     * @param dto 编辑参数
     **/
    @PostMapping("/changeQtyView")
    public ApiResult<List<PickingDetailDTO.ChangeQtyView>> generateRequisitionChange(@RequestBody @Validated PickingListsDTO.UpdateDTO dto) {
        return success(pickingListsService.generateRequisitionChange(dto));
    }

    /**
     * 查询详情
     *
     * @param id id
     **/
    @GetMapping("/view")
    public ApiResult<PickingListsDTO.View> view(@RequestParam("id") String id) {
        PickingListsDTO.View dto = pickingListsService.view(id);
        return success(dto);
    }

    /**
     * 删除
     *
     * @param dto dto
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "删除拣货单")
    @PostMapping("/delete")
    public ApiResult<String> delete(@RequestBody BaseIdDTO dto) {
        pickingListsService.delete(dto.getId());
        return success();
    }

    /**
     * 批量打印
     *
     * @param idsDTO idsDTO
     **/
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "打印拣货单")
    @PostMapping("/print")
    public ApiResult<List<PickingListsDTO.PrintView>> print(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        List<PickingListsDTO.PrintView> views = pickingListsService.print(idsDTO.getIds().stream().distinct().collect(Collectors.toList()));
        return success(views);
    }

    /**
     * 批量导出
     *
     * @param dto dto
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出拣货单")
    @PostMapping("/export")
    public ApiResult<Boolean> export(@RequestBody @Validated PickingListsDTO.ExportDTO dto) {
        pickingListsService.export(dto);
        return success(true);
    }


    @PostMapping("/initDelivery")
    public void initDelivery(@RequestBody List<String> codes) {
        pickingListsService.initDelivery(codes);
    }

}
