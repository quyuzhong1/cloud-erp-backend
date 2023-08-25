package com.erp.server.oms.controller.api;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.SoB2cInvalidTypeEnum;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:paging",
            tableAlias = ""
    )
    public ApiResult<List<SoB2cDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(soB2cService.tabList(dto));
    }

    /**
    * 列表查询
    * @author Will
    * @date: 2023-08-18
    * @param dto
    * @return ApiResult<PagingVO<SoB2cDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<SoB2cDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SoB2cDTO.PagingParamDTO> dto) {
        return success(soB2cService.paging(dto));
    }

   /**
   * 新增
   * @author Will
   * @date:  2023-08-18
   * @param dto
   * @return ApiResult<String>
   */
   @PostMapping("/add")
   @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
           tableField = "create_user_id",
           menuCode = "oms:soB2c:add",
           serviceClass = SoB2cService.class,
           keyIdName = "id")
   public ApiResult<String> add(@RequestBody @Validated SoB2cDTO.AddDTO dto) {
      return success(soB2cService.add(dto));
   }

    /**
    * 修改
    * @author Will
    * @date:  2023-08-18
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:update",
            serviceClass = SoB2cService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated SoB2cDTO.UpdateDTO dto) {
        soB2cService.update(dto);
        return success();
    }

    /**
    * 提交审核
    * @author Will
    * @date:  2023-08-18
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:submit",
            serviceClass = SoB2cService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = soB2cService.submit(id,Boolean.TRUE);
            } catch (Exception e){
                log.error("B2C销售订单 提交审核失败",e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, "B2C销售订单不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return success(resultDTOS);
    }

    /**
    * 审核
    * @author Will
    * @date:  2023-08-18
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:approve",
            serviceClass = SoB2cService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = soB2cService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            } catch (Exception e){
                log.error("B2C销售订单审核失败",e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(entity.getCode(), "B2C销售订单不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(approveResult);
        }
        return success(resultDTOS);
    }

    /**
    * 作废
    * @author Will
    * @date:  2023-08-18
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:invalid",
            serviceClass = SoB2cService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = soB2cService.invalid(id,dto.getRemark(), SoB2cInvalidTypeEnum.ENUM_MANUAL);
            } catch (Exception e){
                log.error("B2C销售订单作废失败",e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(entity.getCode(), "B2C销售订单不存在, 作废失败");
                    resultDTOS.add(invalidResult);
                    continue;
                }
                invalidResult = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(invalidResult);
        }
        return success(resultDTOS);
    }

    /**
     * 取消作废
     * @author Will
     * @date: 2023/8/18 15:32
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/unInvalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:unInvalid",
            serviceClass = SoB2cService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> unInvalid(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO unInvalidResult;
            try {
                unInvalidResult = soB2cService.unInvalid(id, SoB2cInvalidTypeEnum.ENUM_MANUAL);
            } catch (Exception e){
                log.error("B2C销售订单取消作废失败",e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    unInvalidResult = BatchResultDTO.fail(entity.getCode(), "B2C销售订单不存在, 取消作废失败");
                    resultDTOS.add(unInvalidResult);
                    continue;
                }
                unInvalidResult = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(unInvalidResult);
        }
        return success(resultDTOS);
    }



    /**
    * 详情
    * @author Will
    * @date:  2023-08-18
    * @param id
    * @return ApiResult<SoB2cDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:view",
            serviceClass = SoB2cService.class,
            keyIdName = "id")
    public ApiResult<SoB2cDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(soB2cService.view(id));
    }

    /**
     * 修改订单备注
     * @author Will
     * @date: 2023/8/18 15:37
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/updateRemark")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:updateRemark",
            serviceClass = SoB2cService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> updateRemark(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO updateRemarkResult;
            try {
                updateRemarkResult = soB2cService.updateRemark(id,dto.getRemark());
            } catch (Exception e){
                log.error("B2C销售订单修改订单备注失败",e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    updateRemarkResult = BatchResultDTO.fail(entity.getCode(), "B2C销售订单不存在, 修改订单备注失败");
                    resultDTOS.add(updateRemarkResult);
                    continue;
                }
                updateRemarkResult = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(updateRemarkResult);
        }
        return success(resultDTOS);
    }

    /**
     * 编辑分类
     * @author Will
     * @date: 2023/8/18 15:46
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/updateCategory")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:updateCategory",
            serviceClass = SoB2cService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> updateCategory(@RequestBody @Validated SoB2cDTO.SoB2cAddCategoryDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO updateRemarkResult;
            try {
                updateRemarkResult = soB2cService.updateCategory(id,dto.getTypeEnum(),dto.getCategoryIdList());
            } catch (Exception e){
                log.error("B2C销售订单修改分类失败",e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    updateRemarkResult = BatchResultDTO.fail(entity.getCode(), "B2C销售订单不存在, 修改分类失败");
                    resultDTOS.add(updateRemarkResult);
                    continue;
                }
                updateRemarkResult = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(updateRemarkResult);
        }
        return success(resultDTOS);
    }

    /**
     * 订单配货数据显示
     * @author Will
     * @date: 2023/8/18 16:36
     * @param dto 
     * @return ApiResult<List<ViewSoB2cDistributionDTO>> 
     */
    @PostMapping("/viewSoB2cDistribution")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:viewSoB2cDistribution",
            serviceClass = SoB2cService.class,
            keyIdName = "ids")
    public ApiResult<List<SoB2cDTO.ViewSoB2cDistributionDTO>> viewSoB2cDistribution(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
       List<SoB2cDTO.ViewSoB2cDistributionDTO> list = soB2cService.viewSoB2cDistribution(dto);
        return success(list);
    }

    /**
     * 订单配货保存
     * @author Will
     * @date: 2023/8/18 16:43
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/saveSoB2cDistribution")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:saveSoB2cDistribution",
            serviceClass = SoB2cService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> saveSoB2cDistribution(@RequestBody @Validated SoB2cDTO.SaveSoB2cDistributionDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = soB2cService.saveSoB2cDistribution(id,dto);
            } catch (Exception e){
                log.error("B2C销售订单配货失败",e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(entity.getCode(), "B2C销售订单不存在, 配货失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return success(resultDTOS);
    }

    /**
     * 获取物流单号
     * @author Will
     * @date: 2023/8/18 16:47
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/getLogisticsCode")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:getLogisticsCode",
            serviceClass = SoB2cService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> getLogisticsCode(@RequestBody @Validated SoB2cDTO.GetLogisticsCode dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = soB2cService.getLogisticsCode(id,dto.getIsDelivery());
            } catch (Exception e){
                log.error("B2C销售订单获取物流单号失败",e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(entity.getCode(), "B2C销售订单不存在, 获取物流单号失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return success(resultDTOS);
    }

    /**
     * 提交发货
     * @author Will
     * @date: 2023/8/18 16:49
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/submitDelivery")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:submitDelivery",
            serviceClass = SoB2cService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> submitDelivery(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = soB2cService.submitDelivery(id);
            } catch (Exception e){
                log.error("B2C销售订单提交发货失败",e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(entity.getCode(), "B2C销售订单不存在, 提交发货失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return success(resultDTOS);
    }

    /**
     * 发货拦截
     * @author Will
     * @date: 2023/8/18 16:51
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/deliveryIntercept")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:deliveryIntercept",
            serviceClass = SoB2cService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> deliveryIntercept(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = soB2cService.deliveryIntercept(id,dto.getRemark());
            } catch (Exception e){
                log.error("B2C销售订单发货拦截失败",e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(entity.getCode(), "B2C销售订单不存在, 发货拦截失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return success(resultDTOS);
    }

    /**
     * 取消发货拦截
     * @author Will
     * @date: 2023/8/18 16:53
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/cancelDeliveryIntercept")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:cancelDeliveryIntercept",
            serviceClass = SoB2cService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> cancelDeliveryIntercept(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = soB2cService.cancelDeliveryIntercept(id);
            } catch (Exception e){
                log.error("B2C销售订单取消发货拦截失败",e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(entity.getCode(), "B2C销售订单不存在, 取消发货拦截失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return success(resultDTOS);
    }

    /**
     * 合并列表
     * @author Will
     * @date: 2023/8/18 18:31
     * @param dto
     * @return ApiResult<PagingVO<MergeListDTO>>
     */
    @PostMapping("/mergePaging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:mergePaging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<SoB2cDTO.MergeListDTO>> mergePaging(@RequestBody @Validated PagingDTO<SoB2cDTO.MergePagingParamDTO> dto) {
        return success(soB2cService.mergePaging(dto));
    }

    /**
     * 合并列表数量
     * @author Will
     * @date: 2023/8/24 16:18
     * @param dto
     * @return ApiResult<Integer>
     */
    @PostMapping("/mergePagingCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:mergePaging",
            tableAlias = ""
    )
    public ApiResult<Integer> mergePagingCount(@RequestBody @Validated SoB2cDTO.MergePagingParamDTO dto) {
        return success(soB2cService.mergePagingCount(dto));
    }

    /**
     * 合并保存
     * @author Will
     * @date: 2023/8/18 18:35
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/mergeSave")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:mergeSave",
            serviceClass = SoB2cService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> mergeSave(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soB2cService.mergeSave(dto.getIds());
        return flag.equals(Boolean.TRUE) ? success() : failure();
    }

    /**
     * 取消合并
     * @author Will
     * @date: 2023/8/21 9:07
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/cancelMerge")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:cancelMerge",
            serviceClass = SoB2cService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> cancelMerge(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = soB2cService.cancelMerge(id);
            } catch (Exception e){
                log.error("B2C销售订单取消合并失败",e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(entity.getCode(), "B2C销售订单不存在, 取消合并失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return success(resultDTOS);
    }

    /**
     * 拆分数据显示
     * @author Will
     * @date: 2023/8/21 9:19
     * @param dto
     * @return ApiResult<List<ViewSplitDTO>>
     */
    @PostMapping("/viewSplit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:viewSplit",
            serviceClass = SoB2cService.class,
            keyIdName = "id")
    public ApiResult<SoB2cDTO.ViewSplitDTO> viewSplit(@RequestBody @Validated BaseIdDTO dto) {
        return success(soB2cService.viewSplit(dto.getId()));
    }

    /**
     * 拆分保存
     * @author Will
     * @date: 2023/8/21 9:20
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/splitSave")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:splitSave",
            serviceClass = SoB2cService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> splitSave(@RequestBody @Validated SoB2cDTO.SplitSaveDTO dto) {
        Boolean flag = soB2cService.splitSave(dto);
        return flag.equals(Boolean.TRUE) ? success() : failure();
    }

    /**
     * 取消拆分前数据显示
     * @author Will
     * @date: 2023/8/24 11:50
     * @param dto
     * @return ApiResult<List<CheckCancelSplitDTO>>
     */
    @PostMapping("/checkCancelSplit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:checkCancelSplit",
            serviceClass = SoB2cService.class,
            keyIdName = "ids")
    public ApiResult<List<SoB2cDTO.CheckCancelSplitDTO>> checkCancelSplit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(soB2cService.checkCancelSplit(dto.getIds()));
    }

    /**
     * @description: 取消拆分
     * @author Will
     * @date: 2023/8/21 9:24
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/cancelSplit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2c:cancelSplit",
            serviceClass = SoB2cService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> cancelSplit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = soB2cService.cancelSplit(id);
            } catch (Exception e){
                log.error("B2C销售订单取消拆分失败",e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(entity.getCode(), "B2C销售订单不存在, 取消拆分失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return success(resultDTOS);
    }

}
