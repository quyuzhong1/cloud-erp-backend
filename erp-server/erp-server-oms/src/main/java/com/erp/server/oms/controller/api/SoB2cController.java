package com.erp.server.oms.controller.api;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.base.*;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cLogisticsDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.oms.enums.SoB2cInvalidTypeEnum;
import com.erp.server.oms.service.SoB2cErrorService;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * B2C销售订单表
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@RestController
@RequestMapping("/soB2c")
public class SoB2cController extends BaseController {

    @Autowired
    private SoB2cService soB2cService;

    @Autowired
    private SoB2cErrorService soB2cErrorService;


    /**
     * 获取状态统计
     *
     * @return
     */
    @PostMapping("/tabList")
    public ApiResult<List<SoB2cDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(soB2cService.tabList(dto));
    }

    /**
     * 列表查询
     *
     * @param dto
     * @return ApiResult<PagingVO < SoB2cDTO.ListDTO>>
     * @author Will
     * @date: 2023-08-18
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<SoB2cDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SoB2cDTO.PagingParamDTO> dto) {
        return success(soB2cService.paging(dto));
    }

    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author Will
     * @date: 2023-08-18
     */
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Validated SoB2cDTO.AddDTO dto) {
        /**
         * 1,创建订单
         * 2,匹配订单规则
         * 3.匹配物流仓储规则
         * 4.创建物流运单
         * 5.创建发货单
         */
        SoB2cEntity add = soB2cService.add(dto, null);
        String id = add.getId();
        SoB2cDTO.RuleResultDTO orderRuleResult = soB2cService.orderRule(id);
        //匹配成功
        Boolean ruleMatch = orderRuleResult.getIsRuleMatch();
        Boolean isPass = orderRuleResult.getIsPass();
        //todo 可以优化
        if (ruleMatch && isPass) {
            //仓库规则
            SoB2cDTO.RuleResultDTO warehouseRuleResult = soB2cService.warehouseRule(orderRuleResult.getId(), orderRuleResult.getSoB2cDetailList(), orderRuleResult.getMap());
            Boolean warehouseRuleMatch = warehouseRuleResult.getIsRuleMatch();
            if (warehouseRuleMatch) {
                SoB2cDTO.RuleResultDTO logisticsRuleResult = soB2cService.logisticsRule(id, warehouseRuleResult.getMap());
                Boolean autoGetTrackNo = logisticsRuleResult.getAutoGetTrackNo();
                if (Objects.nonNull(autoGetTrackNo) && autoGetTrackNo) {
                    soB2cService.getLogisticsCode(id, autoGetTrackNo);
                }
            }

        }
        return success(add.getId());
    }

    /**
     * 修改
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2023-08-18
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated SoB2cDTO.UpdateDTO dto) {
        soB2cService.update(dto);
        return success();
    }

    /**
     * 提交审核
     *
     * @param dto
     * @return ApiResult<List < BatchResultDTO>>
     * @author Will
     * @date: 2023-08-18
     */
    @PostMapping("/submit")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = soB2cService.submit(id, Boolean.TRUE);
            } catch (Exception e) {
                log.error("B2C销售订单 提交审核失败", e);

                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "B2C销售订单不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(id, entity.getCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 审核
     *
     * @param dto
     * @return ApiResult<List < BatchResultDTO>>
     * @author Will
     * @date: 2023-08-18
     */
    @PostMapping("/approve")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = soB2cService.approve(new ApproveOneDTO(id, dto.getType(), dto.getComment()), null, "");
                SoB2cEntity entity = soB2cService.getById(id);
                if (Objects.nonNull(entity)) {
                    ApproveStatusEnum approveStatus = ApproveStatusEnum.APPROVE;
                    if (approveStatus.equals(entity.getApproveStatus())) {
                        //仓库规则
                        SoB2cDTO.RuleResultDTO warehouseRuleResult = soB2cService.warehouseRule(id, null, new HashMap<>());
                        Boolean warehouseRuleMatch = warehouseRuleResult.getIsRuleMatch();
                        if (warehouseRuleMatch) {
                            SoB2cDTO.RuleResultDTO logisticsRuleResult = soB2cService.logisticsRule(id, warehouseRuleResult.getMap());
                            Boolean autoGetTrackNo = logisticsRuleResult.getAutoGetTrackNo();
                            if (Objects.nonNull(autoGetTrackNo) && autoGetTrackNo) {
                                soB2cService.getLogisticsCode(id, autoGetTrackNo);
                            }
                        }

                    }
                }
            } catch (Exception e) {
                log.error("B2C销售订单审核失败", e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "B2C销售订单不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(id, entity.getCode(), e.getMessage());
            }
            resultDTOS.add(approveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 作废
     *
     * @param dto
     * @return ApiResult<List < BatchResultDTO>>
     * @author Will
     * @date: 2023-08-18
     */
    @PostMapping("/invalid")
    public ApiResult<List<BatchResultDTO>> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = soB2cService.invalid(id, dto.getRemark(), SoB2cInvalidTypeEnum.ENUM_MANUAL);
            } catch (Exception e) {
                log.error("B2C销售订单作废失败", e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, "B2C销售订单不存在, 作废失败");
                    resultDTOS.add(invalidResult);
                    continue;
                }
                invalidResult = BatchResultDTO.fail(id, entity.getCode(), e.getMessage());
            }
            resultDTOS.add(invalidResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 取消作废
     *
     * @param dto
     * @return ApiResult<List < BatchResultDTO>>
     * @author Will
     * @date: 2023/8/18 15:32
     */
    @PostMapping("/unInvalid")
    public ApiResult<List<BatchResultDTO>> unInvalid(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO unInvalidResult;
            try {
                unInvalidResult = soB2cService.unInvalid(id, SoB2cInvalidTypeEnum.ENUM_MANUAL);
            } catch (Exception e) {
                log.error("B2C销售订单取消作废失败", e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    unInvalidResult = BatchResultDTO.fail(id, id, "B2C销售订单不存在, 取消作废失败");
                    resultDTOS.add(unInvalidResult);
                    continue;
                }
                unInvalidResult = BatchResultDTO.fail(id, entity.getCode(), e.getMessage());
            }
            resultDTOS.add(unInvalidResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 详情
     *
     * @param id
     * @return ApiResult<SoB2cDTO.ViewDTO>>
     * @author Will
     * @date: 2023-08-18
     */
    @GetMapping("/view")
    public ApiResult<SoB2cDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(soB2cService.view(id));
    }

    /**
     * 查看财务信息
     *
     * @param dto
     * @return ApiResult<FinancialInfoDTO>
     * @author Will
     * @date: 2023/9/6 15:43
     */
    @PostMapping("/getFinancialInfo")
    public ApiResult<SoB2cDTO.FinancialInfoDTO> getFinancialInfo(@RequestBody @Validated SoB2cDTO.FinancialParamDTO dto) {
        return success(soB2cService.getFinancialInfoById(dto));
    }

    /**
     * 撤销流程
     * @param dto
     * @return
     * @description
     * @author Lambda
     * @create 2024-01-09 11:47
     */
    @PostMapping("/cancelProcess")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<String> ids = dto.getIds();
        for (String id : ids) {
            BatchResultDTO resultDTO;
            try {
                resultDTO=soB2cService.cancelProcess(id);
            }catch (Exception e){
                log.error("B2C销售订单撤销失败>>>>>{}",e.getMessage());
                SoB2cEntity entity=soB2cService.getById(id);
                if(Objects.isNull(entity)){
                    resultDTO=BatchResultDTO.fail(id, id, "B2c销售订单不存在, 撤销流程失败");
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
     * 反审核销售订单
     * @description
     * @param dto
     * @author Lambda
     * @return 
     * @create 2024-01-09 14:09
     */
    @PostMapping("/disApprove")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核B2C销售订单")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<String> ids = dto.getIds();
        for (String id : ids) {
            BatchResultDTO resultDTO;
            try {
                resultDTO=soB2cService.disApprove(id);
            }catch (Exception e){
                log.error("B2C销售订单反审核失败>>>>>{}",e.getMessage());
                SoB2cEntity entity=soB2cService.getById(id);
                if(Objects.isNull(entity)){
                    resultDTO=BatchResultDTO.fail(id, id, "B2c销售订单不存在, 反审核失败");
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
     * 修改订单备注
     *
     * @param dto
     * @return ApiResult<List < BatchResultDTO>>
     * @author Will
     * @date: 2023/8/18 15:37
     */
    @PostMapping("/updateRemark")
    public ApiResult<List<BatchResultDTO>> updateRemark(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO updateRemarkResult;
            try {
                updateRemarkResult = soB2cService.updateRemark(id, dto.getRemark());
            } catch (Exception e) {
                log.error("B2C销售订单修改订单备注失败", e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    updateRemarkResult = BatchResultDTO.fail(id, id, "B2C销售订单不存在, 修改订单备注失败");
                    resultDTOS.add(updateRemarkResult);
                    continue;
                }
                updateRemarkResult = BatchResultDTO.fail(id, entity.getCode(), e.getMessage());
            }
            resultDTOS.add(updateRemarkResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 编辑分类
     *
     * @param dto
     * @return ApiResult<List < BatchResultDTO>>
     * @author Will
     * @date: 2023/8/18 15:46
     */
    @PostMapping("/updateCategory")
    public ApiResult<List<BatchResultDTO>> updateCategory(@RequestBody @Validated SoB2cDTO.SoB2cAddCategoryDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO updateRemarkResult;
            try {
                updateRemarkResult = soB2cService.updateCategory(id, dto.getTypeEnum(), dto.getCategoryIdList());
            } catch (Exception e) {
                log.error("B2C销售订单修改分类失败", e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    updateRemarkResult = BatchResultDTO.fail(id, id, "B2C销售订单不存在, 修改分类失败");
                    resultDTOS.add(updateRemarkResult);
                    continue;
                }
                updateRemarkResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(updateRemarkResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 订单配货数据显示（前端手动配货）
     *
     * @param dto
     * @return ApiResult<List < ViewSoB2cDistributionDTO>>
     * @author Will
     * @date: 2023/8/18 16:36
     */
    @PostMapping("/viewSoB2cDistribution")
    public ApiResult<List<SoB2cDTO.ViewSoB2cDistributionDTO>> viewSoB2cDistribution(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<SoB2cDTO.ViewSoB2cDistributionDTO> list = soB2cService.viewSoB2cDistribution(dto);
        return success(list);
    }

    /**
     * 订单配货保存（前端手动配货）
     *
     * @param dto
     * @return ApiResult<List < BatchResultDTO>>
     * @author Will
     * @date: 2023/8/18 16:43
     */
    @PostMapping("/saveSoB2cDistribution")
    public ApiResult<List<BatchResultDTO>> saveSoB2cDistribution(@RequestBody @Validated SoB2cDTO.SaveSoB2cDistributionDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = soB2cService.saveSoB2cDistribution(id, dto);
            } catch (Exception e) {
                log.error("B2C销售订单配货失败", e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "B2C销售订单不存在, 配货失败");
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
     * 获取物流单号
     *
     * @param dto
     * @return ApiResult<List < BatchResultDTO>>
     * @author Will
     * @date: 2023/8/18 16:47
     */
    @PostMapping("/getLogisticsCode")
    public ApiResult<List<BatchResultDTO>> getLogisticsCode(@RequestBody @Validated SoB2cDTO.GetLogisticsCode dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = soB2cService.getLogisticsCode(id, dto.getIsDelivery());
            } catch (Exception e) {
                log.error("B2C销售订单获取物流单号失败", e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "B2C销售订单不存在, 获取物流单号失败");
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
     * 提交发货
     *
     * @param dto
     * @return ApiResult<List < BatchResultDTO>>
     * @author Will
     * @date: 2023/8/18 16:49
     */
    @PostMapping("/submitDelivery")
    public ApiResult<List<BatchResultDTO>> submitDelivery(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = soB2cService.submitDelivery(id);
            } catch (Exception e) {
                log.error("B2C销售订单提交发货失败", e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "B2C销售订单不存在, 提交发货失败");
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
     * 发货拦截
     *
     * @param dto
     * @return ApiResult<List < BatchResultDTO>>
     * @author Will
     * @date: 2023/8/18 16:51
     */
    @PostMapping("/deliveryIntercept")
    public ApiResult<List<BatchResultDTO>> deliveryIntercept(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = soB2cService.deliveryIntercept(id, dto.getRemark());
            } catch (Exception e) {
                log.error("B2C销售订单发货拦截失败", e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "B2C销售订单不存在, 发货拦截失败");
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
     * 取消发货拦截
     *
     * @param dto
     * @return ApiResult<List < BatchResultDTO>>
     * @author Will
     * @date: 2023/8/18 16:53
     */
    @PostMapping("/cancelDeliveryIntercept")
    public ApiResult<List<BatchResultDTO>> cancelDeliveryIntercept(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = soB2cService.cancelDeliveryIntercept(id);
            } catch (Exception e) {
                log.error("B2C销售订单取消发货拦截失败", e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "B2C销售订单不存在, 取消发货拦截失败");
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
     * 合并列表
     *
     * @param dto
     * @return ApiResult<PagingVO < MergeListDTO>>
     * @author Will
     * @date: 2023/8/18 18:31
     */
    @PostMapping("/mergePaging")
    public ApiResult<PagingVO<SoB2cDTO.MergeListDTO>> mergePaging(@RequestBody @Validated PagingDTO<SoB2cDTO.MergePagingParamDTO> dto) {
        return success(soB2cService.mergePaging(dto));
    }

    /**
     * 合并列表数量
     *
     * @param dto
     * @return ApiResult<Integer>
     * @author Will
     * @date: 2023/8/24 16:18
     */
    @PostMapping("/mergePagingCount")
    public ApiResult<Integer> mergePagingCount(@RequestBody @Validated SoB2cDTO.MergePagingParamDTO dto) {
        return success(soB2cService.mergePagingCount(dto));
    }

    /**
     * 合并保存
     *
     * @param dto
     * @return ApiResult<List < BatchResultDTO>>
     * @author Will
     * @date: 2023/8/18 18:35
     */
    @PostMapping("/mergeSave")
    public ApiResult<List<BatchResultDTO>> mergeSave(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soB2cService.mergeSave(dto.getIds());
        return flag.equals(Boolean.TRUE) ? success() : failure();
    }

    /**
     * 不合并
     *
     * @param dto
     * @return ApiResult<List < BatchResultDTO>>
     * @author Will
     * @date: 2023/9/11 9:23
     */
    @PostMapping("/isNotNeedMerge")
    public ApiResult<List<BatchResultDTO>> isNotNeedMerge(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = soB2cService.isNotNeedMerge(id);
            } catch (Exception e) {
                log.error("B2C销售订单无需合并标记失败", e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "B2C销售订单不存在, 无需合并标记失败");
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
     * 取消合并
     *
     * @param dto
     * @return ApiResult<List < BatchResultDTO>>
     * @author Will
     * @date: 2023/8/21 9:07
     */
    @PostMapping("/cancelMerge")
    public ApiResult<List<BatchResultDTO>> cancelMerge(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = soB2cService.cancelMerge(id);
            } catch (Exception e) {
                log.error("B2C销售订单取消合并失败", e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "B2C销售订单不存在, 取消合并失败");
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
     * 拆分数据显示
     *
     * @param dto
     * @return ApiResult<List < ViewSplitDTO>>
     * @author Will
     * @date: 2023/8/21 9:19
     */
    @PostMapping("/viewSplit")
    public ApiResult<SoB2cDTO.ViewSplitDTO> viewSplit(@RequestBody @Validated BaseIdDTO dto) {
        return success(soB2cService.viewSplit(dto.getId()));
    }

    /**
     * 拆分保存
     *
     * @param dto
     * @return ApiResult<List < BatchResultDTO>>
     * @author Will
     * @date: 2023/8/21 9:20
     */
    @PostMapping("/splitSave")
    public ApiResult<List<BatchResultDTO>> splitSave(@RequestBody @Validated SoB2cDTO.SplitSaveDTO dto) {
        Boolean flag = soB2cService.splitSave(dto);
        return flag.equals(Boolean.TRUE) ? success() : failure();
    }

    /**
     * 取消拆分前数据显示
     *
     * @param dto
     * @return ApiResult<List < CheckCancelSplitDTO>>
     * @author Will
     * @date: 2023/8/24 11:50
     */
    @PostMapping("/checkCancelSplit")
    public ApiResult<List<SoB2cDTO.CheckCancelSplitDTO>> checkCancelSplit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(soB2cService.checkCancelSplit(dto.getIds()));
    }

    /**
     * @param dto
     * @return ApiResult<List < BatchResultDTO>>
     * @description: 取消拆分
     * @author Will
     * @date: 2023/8/21 9:24
     */
    @PostMapping("/cancelSplit")
    public ApiResult<List<BatchResultDTO>> cancelSplit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = soB2cService.cancelSplit(id);
            } catch (Exception e) {
                log.error("B2C销售订单取消拆分失败", e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "B2C销售订单不存在, 取消拆分失败");
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
     * 匹配sku
     *
     * @return
     */
    @PostMapping("/matchSku")
    public ApiResult matchSku(@RequestBody @Validated SoB2cDTO.MatchSkuDTO dto) {
        Boolean result = soB2cService.matchSku(dto);
        return result ? success() : failure();
    }

    /**
     * 运费测算后选择渠道
     *
     * @param
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-15 12:22
     */
    @PostMapping("/selectLogisticsChannel")
    public ApiResult selectLogisticsChannel(@RequestBody SoB2cLogisticsDTO.SelectChannelDTO dto) {
        Boolean result = soB2cService.selectLogisticsChannel(dto);
        return result ? success() : failure();
    }

    @GetMapping("/getJson")
    public ApiResult<Map<String, Object>> getJson(@RequestParam("id") String id) {
        Map<String, Object> map = soB2cService.getJson(id);
        return success(map);

    }


    /**
     * 虚假发货
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.common.business.dto.base.BatchResultDTO>>
     * @Author Luo_WG
     * @Date 2023/12/13 19:29
     **/
    @PostMapping("/falseDelivery")
    public ApiResult<List<BatchResultDTO>> falseDelivery(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = soB2cService.falseDelivery(id);
            } catch (Exception e) {
                log.error("b2c订单 虚假发货失败", e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "b2c订单不存在, 虚假发货失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}