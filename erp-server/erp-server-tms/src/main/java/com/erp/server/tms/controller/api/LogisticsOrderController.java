package com.erp.server.tms.controller.api;

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
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.LogisticsOrderDTO;
import com.erp.model.tms.entity.LogisticsOrderEntity;
import com.erp.server.tms.query.LogisticsOrderQueryHandler;
import com.erp.server.tms.service.LogisticsOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 物流下单表
 *
 * @author lei.nie
 * @since 2026-04-20
 */
@Slf4j
@RestController
@LogSystemModule("物流下单表")
@RequestMapping("/logisticsOrder")
public class LogisticsOrderController extends BaseController {

    @Resource
    private LogisticsOrderService logisticsOrderService;

    /**
     * 新增
     *
     * @param dto LogisticsOrderDTO.AddDTO
     * @return ApiResult<String>
     * @author lei.nie
     * @date: 2026-04-20
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流下单表新增")
    public ApiResult<Object> add(@RequestBody @Validated LogisticsOrderDTO.AddDTO dto) {
        logisticsOrderService.add(dto);
        return success();
    }

    /**
     * 修改
     *
     * @param dto LogisticsOrderDTO.UpdateDTO
     * @return ApiResult
     * @author lei.nie
     * @date: 2026-04-20
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "物流下单表修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:logisticsOrder:update",
            serviceClass = LogisticsOrderService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated LogisticsOrderDTO.UpdateDTO dto) {
        logisticsOrderService.update(dto);
        return success();
    }

    /**
     * 获取状态统计
     *
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:logisticsOrder:paging",
            tableAlias = ""
    )
    public ApiResult<List<LogisticsOrderDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(logisticsOrderService.tabList(dto));
    }

    /**
     * 列表查询
     *
     * @param dto PagingDTO<LogisticsOrderDTO.PagingParamDTO>
     * @return ApiResult<PagingVO < LogisticsOrderDTO.ListDTO>>
     * @author lei.nie
     * @date: 2026-04-20
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = LogisticsOrderQueryHandler.class)
    public ApiResult<PagingVO<LogisticsOrderDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<LogisticsOrderDTO.PagingParamDTO> dto) {
        return success(logisticsOrderService.paging(dto));
    }

    /**
     * 详情
     *
     * @param id String
     * @return ApiResult<LogisticsOrderDTO.ViewDTO>>
     * @author lei.nie
     * @date: 2026-04-20
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:logisticsOrder:view",
            serviceClass = LogisticsOrderService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<LogisticsOrderDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(logisticsOrderService.view(id));
    }

    /**
     * 导出Excel数据
     *
     * @param dto
     * @param response
     * @return
     * @author lei.nie
     * @date: 2026-04-20
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:logisticsOrder:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "物流下单表导出Excel数据")
    @WebAdvanceQuery(handler = LogisticsOrderQueryHandler.class)
    public ApiResult<Object> exportList(@RequestBody @Validated LogisticsOrderDTO.ExportDTO dto, HttpServletResponse response) {
        logisticsOrderService.exportList(dto, response);
        return success();
    }

    @LogAction(value = LogActionEnum.DELETE, desc = "删除:ids={ids}")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:logisticsOrder:delete",
            serviceClass = LogisticsOrderService.class,
            keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO delete;
            try {
                delete = logisticsOrderService.delete(id);
            } catch (Exception e) {
                log.error("物流下单状态变更", e);
                LogisticsOrderEntity entity = logisticsOrderService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    delete = BatchResultDTO.fail(id, id, "物流下单不存在, 状态变更");
                    resultDTOS.add(delete);
                    continue;
                }
                delete = BatchResultDTO.fail(entity.getId(), entity.getTrackNo(), e.getMessage());
            }
            resultDTOS.add(delete);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    @LogAction(value = LogActionEnum.CANCEL, desc = "取消:ids={ids}")
    @PostMapping("/cancel")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:logisticsOrder:delete",
            serviceClass = LogisticsOrderService.class,
            keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> cancel(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO cancel;
            try {
                cancel = logisticsOrderService.cancel(id);
            } catch (Exception e) {
                log.error("物流下单状态变更", e);
                LogisticsOrderEntity entity = logisticsOrderService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancel = BatchResultDTO.fail(id, id, "物流下单不存在, 状态变更");
                    resultDTOS.add(cancel);
                    continue;
                }
                cancel = BatchResultDTO.fail(entity.getId(), entity.getTrackNo(), e.getMessage());
            }
            resultDTOS.add(cancel);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 打印物流面单
     *
     * @param dto BaseIdsDTO.IdsDTO
     * @return
     */
    @PostMapping("/printLogisticsWaybill")
    @LogAction(value = LogActionEnum.GET_LOGISTICS_LABEL, desc = "打印物流面单")
    public ApiResult<List<BatchResultDTO>> printLogisticsWaybill(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(logisticsOrderService.printLogisticsWaybill(dto));
    }

    /**
     * 上传物流面单
     *
     * @param dto LogisticsOrderDTO.UploadFileDTO
     * @return String
     */
    @PostMapping("/uploadLogisticLabel")
    public ApiResult<String> uploadLogisticLabel(@ModelAttribute @Validated LogisticsOrderDTO.UploadFileDTO dto) throws IOException {
        return success(logisticsOrderService.uploadLogisticLabel(dto));
    }

}
