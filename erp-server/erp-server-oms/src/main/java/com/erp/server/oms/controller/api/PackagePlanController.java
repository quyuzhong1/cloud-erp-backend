package com.erp.server.oms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.PackageDTO;
import com.erp.model.oms.dto.PackagePlanDTO;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.server.oms.service.PackagePlanService;
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
     * @author hyj
     * @date 2024/5/23
     * @param dto
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出店铺")
    @PostMapping("/export")
    public ApiResult<Boolean> listExport(@RequestBody PackagePlanDTO.PagingParamDTO dto) {
        packagePlanService.listExport(dto);
        return success(true);
    }
    /**
     * 批量打印面单
     *
     * @param dto
     * @return
     */
    @PostMapping("/batchPrint")
    public void batchPrint(@RequestBody @Valid BaseIdsDTO.IdsDTO dto, HttpServletResponse response) {
        packagePlanService.batchPrint(dto.getIds(),response);
    }

    /**
     * 1.创建大包号
     * @param dto
     * @return
     */
    @PostMapping("/createSupply")
    public WorkflowTaskRecordDTO.MqResponseDTO createSupply(@RequestBody WorkflowTaskRecordDTO.MqRequestDTO dto){
        return packagePlanService.createSupply(dto);
    }

    /**
     * 2.往大包中添加箱子
     * @param dto
     * @return
     */
    @PostMapping("/addBoxToSupply")
    public WorkflowTaskRecordDTO.MqResponseDTO addBoxToSupply(@RequestBody WorkflowTaskRecordDTO.MqRequestDTO dto){
        return packagePlanService.addBoxToSupply(dto);
    }
    /**
     * 3.往大包中添加订单
     * @param dto
     * @return
     */
    @PostMapping("/addOrderToSupply")
    public WorkflowTaskRecordDTO.MqResponseDTO addOrderToSupply(@RequestBody WorkflowTaskRecordDTO.MqRequestDTO dto){
        return packagePlanService.addOrderToSupply(dto);
    }
    /**
     * 4.获取跟踪号和订单标签
     * @param dto
     * @return
     */
    @PostMapping("/getOrderSticker")
    public WorkflowTaskRecordDTO.MqResponseDTO getOrderSticker(@RequestBody WorkflowTaskRecordDTO.MqRequestDTO dto){
        return packagePlanService.getOrderSticker(dto);
    }

    /**
     * 5.将供货单转入已完成
     * @param dto
     * @return
     */
    @PostMapping("/moveSupplyToDelivery")
    public WorkflowTaskRecordDTO.MqResponseDTO moveSupplyToDelivery(@RequestBody WorkflowTaskRecordDTO.MqRequestDTO dto){
        return packagePlanService.moveSupplyToDelivery(dto);
    }
    /**
     * 6.获取跨境运输标签
     * @param dto
     * @return
     */
    @PostMapping("/getCrossSticker")
    public WorkflowTaskRecordDTO.MqResponseDTO getCrossSticker(@RequestBody WorkflowTaskRecordDTO.MqRequestDTO dto){
        return packagePlanService.getCrossSticker(dto);
    }
}
