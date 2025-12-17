package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.PickingDetailDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.PickingListsEntity;
import com.erp.server.wms.service.PickingListsService;
import com.erp.server.wms.service.RequisitionApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
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
@LogSystemModule("拣货单")
@Slf4j
public class PickingListsController extends BaseController {

    @Resource
    private PickingListsService pickingListsService;
    @Resource
    private RequisitionApplicationService requisitionApplicationService;

    /**
     * 分页查询
     *
     * @param dto 分页查询条件
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "pl.warehouse_id",
            menuCode = "wms:picking-lists:paging",
            tableAlias = "pl"
    )
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
    @LogAction(value = LogActionEnum.UPDATE, desc = "拣货车类型修改")
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
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody BaseIdDTO dto) {
        PickingListsEntity pickingListsEntity=null;
        try {
            pickingListsEntity = pickingListsService.getById(dto.getId());
            pickingListsService.delete(dto.getId());
            requisitionApplicationService.writeBackRequisitionPickPushDownStatus(pickingListsEntity.getSourceId());
            return success(Collections.singletonList(BatchResultDTO.success(pickingListsEntity.getId(), pickingListsEntity.getCode(), "删除成功")));
        }catch (Exception e){
            log.error("删除拣货单失败", e);
            return failure(pickingListsEntity!=null? Collections.singletonList(BatchResultDTO.fail(pickingListsEntity.getId(), pickingListsEntity.getCode(), e.getMessage())):Collections.singletonList(BatchResultDTO.fail(dto.getId(), dto.getId(), e.getMessage())));
        }

    }

    /**
     * 批量打印B2B拣货单（组合/单品）
     *
     * @param idsDTO idsDTO
     **/
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "打印B2B拣货单")
    @PostMapping("/printCombination")
    public ApiResult<List<PickingListsDTO.PrintCombinationView>> printCombination(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        List<PickingListsDTO.PrintCombinationView> views = pickingListsService.printCombination(idsDTO.getIds().stream().distinct().collect(Collectors.toList()));
        return success(views);
    }
    /**
     * 批量打印拣货单（头程 拣货清单/发货清单）
     *
     * @param idsDTO idsDTO
     **/
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "打印头程拣货单")
    @PostMapping("/print")
    public ApiResult<List<PickingListsDTO.PrintCombinationView>> print(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        List<PickingListsDTO.PrintCombinationView> views = pickingListsService.print(idsDTO.getIds().stream().distinct().collect(Collectors.toList()));
        return success(views);
    }
    /**
     * 批量导出
     *
     * @param dto dto
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出拣货单")
    @PostMapping("/export")
    @WebAdvanceQuery
    public ApiResult<Boolean> export(@RequestBody @Validated PickingListsDTO.ExportDTO dto) {
        pickingListsService.export(dto);
        return success(true);
    }


    @PostMapping("/initDelivery")
    public void initDelivery(@RequestBody List<String> codes) {
        pickingListsService.initDelivery(codes);
    }

    /**
     * 确认打印
     *
     * @param idsDTO idsDTO
     **/
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "打印头程拣货单")
    @PostMapping("/printConfirm")
    public ApiResult<Boolean> printConfirm(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        pickingListsService.printConfirm(idsDTO.getIds().stream().distinct().collect(Collectors.toList()));
        return success(true);
    }
    /**
     * 取消打印
     *
     * @param idsDTO idsDTO
     **/
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "打印头程拣货单")
    @PostMapping("/printCancel")
    public ApiResult<Boolean> printCancel(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        pickingListsService.printCancel(idsDTO.getIds().stream().distinct().collect(Collectors.toList()));
        return success(true);
    }
}
