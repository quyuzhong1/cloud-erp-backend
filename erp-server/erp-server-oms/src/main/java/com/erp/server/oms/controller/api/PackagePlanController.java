package com.erp.server.oms.controller.api;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
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
import com.erp.model.oms.dto.PackagePlanDTO;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.server.oms.service.PackagePlanService;
import com.erp.server.oms.service.SoB2cErrorService;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 组包计划主表
 *
 * @author zdy
 * @since 2025-10-09
 */
@Slf4j
@RestController
@LogSystemModule("组包计划主表")
@RequestMapping("/packagePlan")
public class PackagePlanController extends BaseController {

    @Resource
    private PackagePlanService packagePlanService;
    @Resource
    private SoB2cService soB2cService;
    @Resource
    private SoB2cErrorService soB2cErrorService;

    /**
     * 生成组包计划预览
     *
     * @param dto
     * @return
     * @description
     * @author Lambda
     * @create 2024-01-09 14:09
     */
    @PostMapping("/preview")
    @LogAction(value = LogActionEnum.INSERT, desc = "生成组包计划预览")
    public ApiResult<List<PackagePlanDTO.SoB2cDTO>> preview(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<PackagePlanDTO.SoB2cDTO> resultDTOS = soB2cService.packagePlanPreview(dto.getIds());
        return success(resultDTOS);
    }
    /**
     * 批量生成组包计划
     *
     * @param dtoList
     * @return
     * @description
     * @author Lambda
     * @create 2024-01-09 14:09
     */
    @PostMapping("/batchAdd")
    @LogAction(value = LogActionEnum.INSERT, desc = "批量生成组包计划")
    public ApiResult<List<BatchResultDTO>> batchAdd(@RequestBody @Validated List<PackagePlanDTO.SoB2cDTO> dtoList) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dtoList.size());
        for (PackagePlanDTO.SoB2cDTO dto : dtoList) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = packagePlanService.addPlan(dto);
            } catch (Exception e) {
                log.error("B2C销售订单组包计划失败>>>>>{}", e.getMessage());
                SoB2cEntity entity = soB2cService.getById(dto.getSoId());
                if (Objects.isNull(entity)) {
                    resultDTO = BatchResultDTO.fail(dto.getSoId(), dto.getSoCode(), "B2c销售订单不存在, 组包计划失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(dto.getSoId(), dto.getSoCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 分页查询
     *
     * @param dto
     * @return PagingVO<PackagePlanDTO.PagingViewDTO>
     * @description 分页查询组包计划主表
     * @date 2024-01-26 17:45
     * @author zdy
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            shopTableField = "si.id",
            menuCode = "oms:packagePlan:paging",
            tableAlias = "pp"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<PackagePlanDTO.PagingViewDTO>> paging(@RequestBody PagingDTO<PackagePlanDTO.PagingParamDTO> dto) {
        PagingVO<PackagePlanDTO.PagingViewDTO> pagingView = packagePlanService.paging(dto);
        return success(pagingView);
    }

    /**
     * 导出
     *
     * @param dto
     * @author hyj
     * @date 2024/5/23
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出店铺")
    @PostMapping("/export")
    public ApiResult<Boolean> listExport(@RequestBody PackagePlanDTO.PagingParamDTO dto) {
        packagePlanService.listExport(dto);
        return success(true);
    }

    /**
     * 批量打印订单标签
     *
     * @param dto
     * @return
     */
    @PostMapping("/batchOrderPrint")
    public void batchOrderPrint(@RequestBody @Valid BaseIdsDTO.IdsDTO dto, HttpServletResponse response) {
        packagePlanService.batchOrderPrint(dto.getIds(), response);
    }

    /**
     * 1.创建大包号
     *
     * @param dto
     * @return
     */
    @PostMapping("/createSupply")
    public WorkflowTaskRecordDTO.MqResponseDTO createSupply(@RequestBody WorkflowTaskRecordDTO.MqRequestDTO dto) {
        WorkflowTaskRecordDTO.MqResponseDTO result = null;
        String soId = dto.getData().get("id").toString();
        String errorType = dto.getData().get("errorType").toString();
        try {
            result = packagePlanService.createSupply(dto);
            if (CharSequenceUtil.isNotBlank(result.getErrorMsg())) {
                //添加异常
                addException("创建大包号:" + result.getErrorMsg(), soId, errorType, dto);
            }
        } catch (Exception e) {
            log.error("创建大包号失败>>>>>{}", e.getMessage());
            //添加异常
            addException("创建大包号:" + e.getMessage(), soId, errorType, dto);
        }
        return result;
    }

    private void addException(String errorMsg, String soId, String errorType, WorkflowTaskRecordDTO.MqRequestDTO dto) {
        SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
        addError.setType(errorType);
        addError.setMainId(soId);
        addError.setMessage(errorMsg);
        addError.setParamJson(JSONUtil.toJsonStr(dto));
        soB2cErrorService.add(addError);
    }

    /**
     * 2.往大包中添加箱子
     *
     * @param dto
     * @return
     */
    @PostMapping("/addBoxToSupply")
    public WorkflowTaskRecordDTO.MqResponseDTO addBoxToSupply(@RequestBody WorkflowTaskRecordDTO.MqRequestDTO dto) {
        WorkflowTaskRecordDTO.MqResponseDTO result = null;
        String soId = dto.getData().get("id").toString();
        String errorType = dto.getData().get("errorType").toString();
        try {
            result = packagePlanService.addBoxToSupply(dto);
            if (CharSequenceUtil.isNotBlank(result.getErrorMsg())) {
                //添加异常
                addException("往大包中添加箱子:" + result.getErrorMsg(), soId, errorType, dto);
            }
        } catch (Exception e) {
            log.error("往大包中添加箱子失败>>>>>{}", e.getMessage());
            //添加异常
            addException("往大包中添加箱子:" + e.getMessage(), soId, errorType, dto);
        }
        return result;
    }

    /**
     * 3.往大包中添加订单
     *
     * @param dto
     * @return
     */
    @PostMapping("/addOrderToSupply")
    public WorkflowTaskRecordDTO.MqResponseDTO addOrderToSupply(@RequestBody WorkflowTaskRecordDTO.MqRequestDTO dto) {
        WorkflowTaskRecordDTO.MqResponseDTO result = null;
        String soId = dto.getData().get("id").toString();
        String errorType = dto.getData().get("errorType").toString();
        try {
            result = packagePlanService.addOrderToSupply(dto);
            if (CharSequenceUtil.isNotBlank(result.getErrorMsg())) {
                //添加异常
                addException("往大包中添加订单:" + result.getErrorMsg(), soId, errorType, dto);
            } else {
                soB2cErrorService.removeErrorOrder(soId, SoB2cErrorTypeEnum.PACKAGE_PLAN_GENERATE.getCode());
            }
        } catch (Exception e) {
            log.error("往大包中添加订单失败>>>>>{}", e.getMessage());
            //添加异常
            addException("往大包中添加订单:" + e.getMessage(), soId, errorType, dto);
        }
        return result;
    }

    /**
     * 4.获取跟踪号和订单标签
     *
     * @param dto
     * @return
     */
    @PostMapping("/getOrderSticker")
    public WorkflowTaskRecordDTO.MqResponseDTO getOrderSticker(@RequestBody WorkflowTaskRecordDTO.MqRequestDTO dto) {
        WorkflowTaskRecordDTO.MqResponseDTO result = null;
        String soId = dto.getData().get("id").toString();
        String errorType = dto.getData().get("errorType").toString();
        try {
            result = packagePlanService.getOrderSticker(dto);
            if (CharSequenceUtil.isNotBlank(result.getErrorMsg())) {
                //添加异常
                addException("获取跟踪号和订单标签:" + result.getErrorMsg(), soId, errorType, dto);
            }
        } catch (Exception e) {
            log.error("获取跟踪号和订单标签失败>>>>>{}", e.getMessage());
            //添加异常
            addException("获取跟踪号和订单标签:" + e.getMessage(), soId, errorType, dto);
        }
        return result;
    }

    /**
     * 5.将供货单转入已完成
     *
     * @param dto
     * @return
     */
    @PostMapping("/moveSupplyToDelivery")
    public WorkflowTaskRecordDTO.MqResponseDTO moveSupplyToDelivery(@RequestBody WorkflowTaskRecordDTO.MqRequestDTO dto) {
        WorkflowTaskRecordDTO.MqResponseDTO result = null;
        String soId = dto.getData().get("id").toString();
        String errorType = dto.getData().get("errorType").toString();
        try {
            result = packagePlanService.moveSupplyToDelivery(dto);
            if (CharSequenceUtil.isNotBlank(result.getErrorMsg())) {
                //添加异常
                addException("将供货单转入已完成:" + result.getErrorMsg(), soId, errorType, dto);
            }
        } catch (Exception e) {
            log.error("将供货单转入已完成失败>>>>>{}", e.getMessage());
            //添加异常
            addException("将供货单转入已完成:" + e.getMessage(), soId, errorType, dto);
        }
        return result;
    }

    /**
     * 6.获取跨境运输标签
     *
     * @param dto
     * @return
     */
    @PostMapping("/getCrossSticker")
    public WorkflowTaskRecordDTO.MqResponseDTO getCrossSticker(@RequestBody WorkflowTaskRecordDTO.MqRequestDTO dto) {
        WorkflowTaskRecordDTO.MqResponseDTO result = null;
        String soId = dto.getData().get("id").toString();
        String errorType = dto.getData().get("errorType").toString();
        try {
            result = packagePlanService.getCrossSticker(dto);
            if (CharSequenceUtil.isNotBlank(result.getErrorMsg())) {
                //添加异常
                addException("获取跨境运输标签:" + result.getErrorMsg(), soId, errorType, dto);
            } else {
                soB2cErrorService.removeErrorOrder(soId, SoB2cErrorTypeEnum.GET_LOGISTICS_CODE.getCode());
            }
        } catch (Exception e) {
            log.error("获取跨境运输标签失败>>>>>{}", e.getMessage());
            //添加异常
            addException("获取跨境运输标签:" + e.getMessage(), soId, errorType, dto);
        }
        return result;
    }
}
