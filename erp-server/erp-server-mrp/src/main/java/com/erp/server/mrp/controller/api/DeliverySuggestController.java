package com.erp.server.mrp.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.mrp.dto.DeliverySuggestDTO;
import com.erp.model.mrp.entity.DeliverySuggestEntity;
import com.erp.server.mrp.handler.ReplenishmentSuggestionQueryHandler;
import com.erp.server.mrp.service.DeliverySuggestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 建议发货
 *
 * @author will
 * @since 2024-08-27
 */
@Slf4j
@RestController
@LogSystemModule("补货计划")
@RequestMapping("/deliverySuggest")
public class DeliverySuggestController extends BaseController {

    @Resource
    private DeliverySuggestService deliverySuggestService;

    /**
     * 分页查询
     * @author will
     * @date 2024/10/16 10:36
     * @param dto 
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<DeliverySuggestDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<DeliverySuggestDTO.PagingParamDTO> dto) {
        PagingVO<DeliverySuggestDTO.ListDTO> pagingVO = deliverySuggestService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 列表查询
     * @author will
     * @date 2024/9/9 11:59
     * @param params
     * @return ApiResult<List<ListDTO>>
     */
    @PostMapping("/list")
    public ApiResult<List<DeliverySuggestDTO.ListDTO>> list(@RequestBody @Validated DeliverySuggestDTO.ListParamDTO params) {
        List<DeliverySuggestDTO.ListDTO> paging = deliverySuggestService.list(params);
        return success(paging);
    }

    /**
     * 新增
     * @author will
     * @date:  2024-08-27
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "发货计划新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DeliverySuggestDTO.AddDTO dto) {
        return success(deliverySuggestService.add(dto));
    }

    /**
     * 更新
     * @author will
     * @date 2024/10/16 14:24
     * @param updateDTO
     * @return ApiResult<?>
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "更新")
    public ApiResult<String> update(@RequestBody @Validated DeliverySuggestDTO.UpdateDTO updateDTO) {
        Boolean flag = deliverySuggestService.update(updateDTO);
        return Boolean.TRUE.equals(flag) ? success() : failure();
    }


    /**
     * 下载模板
     * @author will
     * @date 2024/10/16 10:51
     * @param response
     * @return ApiResult<?>
     */
    @GetMapping("/downloadTemplate")
    public ApiResult<String> downloadTemplate(HttpServletResponse response) {
        deliverySuggestService.downloadTemplate(response);
        return success();
    }

    /**
     * 导入补货计划
     * @author will
     * @date 2024/10/24 10:42
     * @param excelFile
     * @param platformType
     * @param response
     * @return ApiResult<?>
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入补货计划")
    @PostMapping("/importDeliverySuggest")
    public ApiResult<?> importDeliverySuggest(@RequestParam(value = "excelFile") MultipartFile excelFile,@RequestParam(value = "platformType") String platformType, HttpServletResponse response) {
        deliverySuggestService.importDeliverySuggest(excelFile,platformType, response);
        return success();
    }


    /**
     * 锁定
     * @author will
     * @date 2024/10/16 11:22
     * @param dto 
     * @return ApiResult<?>
     */
    @PostMapping("/locking")
    @LogAction(value = LogActionEnum.UPDATE, desc = "锁定")
    public ApiResult<List<BatchResultDTO>> locking(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = deliverySuggestService.locking(id);
            }catch (Exception e){
                log.error("发货建议计划 锁定失败",e);
                DeliverySuggestEntity entity = deliverySuggestService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "发货建议计划不存在, 锁定失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 确认
     * @author will
     * @date 2024/10/16 11:00
     * @param dto 
     * @return ApiResult<?>
     */
    @PostMapping("/confirm")
    @LogAction(value = LogActionEnum.CONFIRM, desc = "确认")
    public ApiResult<List<BatchResultDTO>> confirm(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = deliverySuggestService.confirm(id);
            }catch (Exception e){
                log.error("发货建议计划 确认失败",e);
                DeliverySuggestEntity entity = deliverySuggestService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "发货建议计划不存在, 确认失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 作废
     * @author will
     * @date 2024/10/16 11:29
     * @param dto 
     * @return ApiResult<?>
     */
    @PostMapping("/invalid")
    @LogAction(value = LogActionEnum.INVALID, desc = "作废")
    public ApiResult<List<BatchResultDTO>> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = deliverySuggestService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("发货建议计划 作废失败",e);
                DeliverySuggestEntity entity = deliverySuggestService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "发货建议计划不存在, 作废失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 更新备注
     * @author will
     * @date 2024/10/22 17:14
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/updateRemark")
    @LogAction(value = LogActionEnum.UPDATE, desc = "更新备注")
    public ApiResult<List<BatchResultDTO>> updateRemark(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = deliverySuggestService.updateRemark(id,dto.getRemark());
            }catch (Exception e){
                log.error("采购建议计划 更新备注失败",e);
                DeliverySuggestEntity entity = deliverySuggestService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "采购建议计划不存在, 更新备注失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 导出发货建议
     * @author will
     * @date 2024/10/16 12:12
     * @param pagingParamDTO
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出发货建议")
    @PostMapping(value = "/export")
    @WebAdvanceQuery(handler = ReplenishmentSuggestionQueryHandler.class)
    public ApiResult<String> export(@RequestBody DeliverySuggestDTO.PagingParamDTO pagingParamDTO) {
        Boolean flag = deliverySuggestService.export(pagingParamDTO);
        return Boolean.TRUE.equals(flag) ? success() : failure();
    }

    /**
     * 下推补货计划
     * @author will
     * @date 2024/10/17 18:22
     * @param paramDTO
     * @return ApiResult<PushDeliveryPlanDTO>
     */
    @PostMapping(value = "/viewPushDeliveryPlan")
    public ApiResult<DeliverySuggestDTO.ViewPushDeliveryPlanDTO> viewPushDeliveryPlan(@RequestBody DeliverySuggestDTO.PushDeliveryPlanParamDTO paramDTO) {
        DeliverySuggestDTO.ViewPushDeliveryPlanDTO dto = deliverySuggestService.viewPushDeliveryPlan(paramDTO.getIds(),paramDTO.getWarehouseId());
        return success(dto);
    }


    /**
     * 下推补货计划保存
     * @author will
     * @date 2024/10/18 10:09
     * @param deliveryPlanDTO
     * @return ApiResult<ViewPushDeliveryPlanDTO>
     */
    @PostMapping(value = "/pushDeliveryPlan")
    public ApiResult<String> pushDeliveryPlan(@RequestBody DeliverySuggestDTO.AddPushDeliveryPlanDTO deliveryPlanDTO) {
        Boolean flag = deliverySuggestService.pushDeliveryPlan(deliveryPlanDTO);
        return Boolean.TRUE.equals(flag) ? success() : failure();
    }


    /**
     * 海外仓下拉
     * @author will
     * @date 2024/11/13 12:04
     * @param dto
     * @return ApiResult<DeliverySuggestWarehouseDTO>
     */
    @PostMapping(value = "/listOverseasWarehouse")
    public ApiResult<List<DeliverySuggestDTO.DeliverySuggestWarehouseDTO>> listOverseasWarehouse(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<DeliverySuggestDTO.DeliverySuggestWarehouseDTO> list = deliverySuggestService.listOverseasWarehouse(dto.getIds());
        return success(list);
    }
}
