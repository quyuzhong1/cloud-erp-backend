package com.erp.server.wms.controller.api;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.entity.PoReturnEntity;
import com.erp.server.wms.query.SupplierPoReturnQueryHandler;
import com.erp.server.wms.service.PoReturnService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * SRM供应商退货单
 * @author Luo_WG
 * @since 2023-04-07
 */
@Slf4j
@RestController
@LogSystemModule("SRM供应商退货单")
@RequestMapping("/supplierPoReturn")
public class SupplierPoReturnController extends BaseController {

    @Resource
    private PoReturnService poReturnService;

    /**
     * 列表分页查询
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.WarehouseReceiveDTO.PagingViewDTO>>
     **/
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = SupplierPoReturnQueryHandler.class)
    public ApiResult<PagingVO<PurchaseReturnOrderDTO.SupplierPagingViewDTO>> paging(@RequestBody @Validated PagingDTO<PurchaseReturnOrderDTO.SupplierPagingParamDTO> dto) {
        PagingVO<PurchaseReturnOrderDTO.SupplierPagingViewDTO> pagingVO = poReturnService.supplierPaging(dto);
        return success(pagingVO);
    }

    /**
     * tab列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/4/17 13:14
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.WarehouseReceiveDTO.WarehouseReceiveCountDTO>>
     **/
    @PostMapping("/listCount")
    public ApiResult<List<PurchaseReturnOrderDTO.SupplierTabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<PurchaseReturnOrderDTO.SupplierTabListDTO> warehouseReceiveCountDTOS = poReturnService.supplierTabList(dto);
        return success(warehouseReceiveCountDTOS);
    }

    /**
     * 退货确认
     * @Author Luo_WG
     * @Date 2024/1/11 18:23
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/returnConfirm")
    @LogAction(value = LogActionEnum.CONFIRM, desc = "退货确认")
    public ApiResult<List<BatchResultDTO>> returnConfirm(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = poReturnService.returnConfirm(id);
            } catch (Exception e) {
                log.error("退货单 退货确认失败", e);
                PoReturnEntity entity = poReturnService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "退货单不存在, 退货确认失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 异常反馈
     * @Author Luo_WG
     * @Date 2024/1/11 18:48
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/unusualFeedback")
    public ApiResult unusualFeedback(@RequestBody @Validated PurchaseReturnOrderDTO.UnusualFeedbackParamDTO dto) {
        Boolean flag = poReturnService.unusualFeedback(dto);
        return flag ? success() : failure();
    }

    /**
     * 异常反馈详情页
     * @Author Luo_WG
     * @Date 2024/1/12 12:35
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.PurchaseReturnOrderDTO.UnusualFeedbackParamDTO>
     **/
    @GetMapping("/unusualFeedbackView")
    public ApiResult<PurchaseReturnOrderDTO.UnusualFeedbackView> unusualFeedbackView(@RequestParam("id") String id) {
        PurchaseReturnOrderDTO.UnusualFeedbackView result = poReturnService.unusualFeedbackView(id);
        return success(result);
    }

}
