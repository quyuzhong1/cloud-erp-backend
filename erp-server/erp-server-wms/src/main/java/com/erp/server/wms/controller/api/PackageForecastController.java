package com.erp.server.wms.controller.api;


import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.PackageForecastDetailDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.entity.PackageForecastEntity;
import com.erp.server.wms.service.PackageForecastDetailService;
import com.erp.server.wms.service.SoOutstockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.PackageForecastService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.PackageForecastDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 组包预报表
 *
 * @author Lambda
 * @since 2024-01-26
 */
@Slf4j
@RestController
@LogSystemModule("组包预报表")
@RequestMapping("/packageForecast")
public class PackageForecastController extends BaseController {

    @Resource
    private PackageForecastService packageForecastService;

    @Resource
    private PackageForecastDetailService  packageForecastDetailService;

    /**
     * 获取 tab列表
     *
     * @return
     */
    @GetMapping("/tabList")
    public ApiResult<List<PackageForecastDTO.TabListDTO>> tabList() {
        List<PackageForecastDTO.TabListDTO> tabList = packageForecastService.tabList();
        return success(tabList);
    }


    /**
     * 分页
     *
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:packageForecast:paging",
            tableAlias = "pf"
    )
    public ApiResult<PagingVO<PackageForecastDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<PackageForecastDTO.PagingParamDTO> dto) {
        PagingVO<PackageForecastDTO.PagingViewDTO> pagingVO = packageForecastService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 详情
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:packageForecast:view",
            serviceClass = PackageForecastService.class,
            keyIdName = "id")
    public ApiResult<PackageForecastDTO.ViewDTO> view(@RequestParam("id") String id) {
        PackageForecastDTO.ViewDTO viewDTO = packageForecastService.view(id);
        return success(viewDTO);
    }


    /**
     * 修改
     *
     * @param dto
     * @return ApiResult
     * @author Lambda
     * @date: 2024-01-26
     */
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:packageForecast:update",
            serviceClass = PackageForecastService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated PackageForecastDTO.UpdateDTO dto) {
        Boolean result = packageForecastService.update(dto);
        return result ? success() : failure();
    }


    /**
     * 删除
     *
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:packageForecast:delete",
            serviceClass = PackageForecastService.class,
            keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = packageForecastService.delete(id);
            } catch (Exception e) {
                log.error("组包预报单删除失败===>{}", e.getMessage());
                PackageForecastEntity entity = packageForecastService.getById(id);
                if (Objects.isNull(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "组包预报单不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 打印面单
     *
     * @param dto
     * @return
     */
    @PostMapping("/print")
    public ApiResult<String> print(@RequestBody @Valid BaseIdDTO dto) {
        String resultBase64 = packageForecastService.print(dto.getId());
        return success(resultBase64);

    }

    /**
     * 取消
     *
     * @param dto
     * @return
     */
    @PostMapping("/cancel")
    public ApiResult<List<BatchResultDTO>> cancel(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = packageForecastService.cancel(id);
            } catch (Exception e) {
                log.error("组包预报单取消失败===>{}", e.getMessage());
                PackageForecastEntity entity = packageForecastService.getById(id);
                if (Objects.isNull(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "组包预报单不存在, 取消失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 中转报关
     *
     * @param dto
     * @return
     */
    @PostMapping("/forecast")
    public ApiResult<List<BatchResultDTO>> forecast(@RequestBody @Valid PackageForecastDTO.TransferDeclareDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        String transferLogisticsChannelId = dto.getTransferLogisticsChannelId();
        String transferLogisticsSupplierId = dto.getTransferLogisticsSupplierId();
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = packageForecastService.forecast(id, transferLogisticsSupplierId, transferLogisticsChannelId);
            } catch (Exception e) {
                log.error("组包预报单 中转报关失败===>{}", e.getMessage());
                PackageForecastEntity entity = packageForecastService.getById(id);
                if (Objects.isNull(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "组包预报单不存在, 中转报关失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 上传组包预报
     *
     * @param dto
     * @return
     */
    @PostMapping("/upload")
    public ApiResult<List<BatchResultDTO>> upload(@RequestBody @Valid PackageForecastDTO.UploadDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = packageForecastService.upload(id, dto.getCollectMode(), dto.getCollectAddressId());
            } catch (Exception e) {
                log.error("组包预报上传消失败===>{}", e);
                PackageForecastEntity entity = packageForecastService.getById(id);
                if (Objects.isNull(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "组包预报单不存在, 上传失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 导出组包预报
     *
     * @param dto
     * @param response
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出组包预报")
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:packageForecast:paging",
            tableAlias = "pf"
    )
    public ApiResult export(@RequestBody @Valid PackageForecastDTO.ExportDTO dto, HttpServletResponse response) {
        Boolean result = packageForecastService.exportExcel(dto, response);
        return result ? success() : failure();
    }

    /**
     * 详情里面查询
     * @param dto
     * @return
     */
    @PostMapping("/detailQuery")
    public ApiResult<List<PackageForecastDetailDTO.ViewDTO>> detailQuery(@RequestBody @Valid PackageForecastDTO.DetailQueryParamDTO dto) {
        List<PackageForecastDetailDTO.ViewDTO> list = packageForecastDetailService.detailQuery(dto);
        return success(list);
    }



}
