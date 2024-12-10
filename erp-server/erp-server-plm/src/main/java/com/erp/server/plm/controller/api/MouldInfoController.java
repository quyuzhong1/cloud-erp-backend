package com.erp.server.plm.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.MouldInfoDTO;
import com.erp.model.plm.entity.MouldInfoEntity;
import com.erp.server.plm.query.MouldInfoQueryHandler;
import com.erp.server.plm.service.MouldInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 模具主表
 *
 * @author liaohui
 * @since 2024-12-03
 */
@Slf4j
@RestController
@LogSystemModule("模具主表")
@RequestMapping("/mouldInfo")
public class MouldInfoController extends BaseController {

    @Resource
    private MouldInfoService mouldInfoService;

    /**
     * 分页列表
     *
     * @param dto 参数
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = MouldInfoQueryHandler.class)
    public ApiResult<PagingVO<MouldInfoDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<MouldInfoDTO.PagingParamDTO> dto) {
        PagingVO<MouldInfoDTO.PagingViewDTO> pagingVO = mouldInfoService.paging(dto);
        return success(pagingVO);
    }
    /**
     * 详情
     *
     * @param dto 参数
     */
    @PostMapping("/view")
    public ApiResult<MouldInfoDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        MouldInfoDTO.ViewDTO view = mouldInfoService.view(dto.getId());
        return success(view);
    }



    /**
     * 暂存
     * @author liaohui
     * date:  2024-12-03
     * @return ApiResult<String>
     */
    @PostMapping("/draft")
    @LogAction(value = LogActionEnum.INSERT, desc = "暂存模具")
    public ApiResult<BatchResultDTO> draft(@RequestBody @Validated MouldInfoDTO.CommonDTO dto) {
        return success(mouldInfoService.draft(dto));
    }

    /**
     * 保存并提交
     * @author liaohui
     * date:  2024-12-03
     * @return ApiResult<String>
     */
    @PostMapping("/addAndSubmit")
    @LogAction(value = LogActionEnum.INSERT, desc = "模具保存并提交")
    public ApiResult<BatchResultDTO> addAndSubmit(@RequestBody @Validated MouldInfoDTO.UpdateDTO dto) {
        return success(mouldInfoService.addAndSubmit(dto));
    }


    /**
     * 提交审核
     * @author liaohui
     * date:  2024-12-03
     * @param dto 参数
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/submit")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "模具提交审核")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Set<String> ids = new HashSet<>(dto.getIds());
        for (String id : ids) {
            BatchResultDTO submit;
            try {
                submit = mouldInfoService.submit(id);
            }catch (Exception e){
                log.error("模具提交审核失败",e);
                MouldInfoEntity entity = mouldInfoService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "模具不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getName(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 撤销
     * @author liaohui
     * date:  2024-12-03
     * @param dto 参数
     */
    @PostMapping("/cancelProcess")
    @LogAction(value = LogActionEnum.CANCEL, desc = "模具撤销")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Set<String> ids = new HashSet<>(dto.getIds());
        for (String id : ids) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = mouldInfoService.cancelProcess(id);
            }catch (Exception e){
                log.error("模具撤回流程失败",e);
                MouldInfoEntity entity = mouldInfoService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "模具不存在, 撤回流程失败");
                    resultDTOS.add(cancelResult);
                    continue;
                }
                cancelResult = BatchResultDTO.fail(entity.getId(), entity.getName(), e.getMessage());
            }
            resultDTOS.add(cancelResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 审核
     * @author liaohui
     * date:  2024-12-03
     * @param dto 参数
     */
    @PostMapping("/approve")
    @LogAction(value = LogActionEnum.APPROVE, desc = "模具审核")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Set<String> ids = new HashSet<>(dto.getIds());
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = mouldInfoService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("模具审核失败",e);
                MouldInfoEntity entity = mouldInfoService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "模具不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getId(), entity.getName(), e.getMessage());
            }
            resultDTOS.add(approveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 反审核
     * @author liaohui
     * date:  2024-12-03
     * @param dto 参数
     */
    @PostMapping("/disApprove")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "模具反审核")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Set<String> ids = new HashSet<>(dto.getIds());
        for (String id : ids) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = mouldInfoService.disApprove(id);
            }catch (Exception e){
                log.error("模具反审核失败",e);
                MouldInfoEntity entity = mouldInfoService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "模具不存在, 反审核失败");
                    resultDTOS.add(disApproveResult);
                    continue;
                }
                disApproveResult = BatchResultDTO.fail(entity.getId(), entity.getName(), e.getMessage());
            }
            resultDTOS.add(disApproveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 作废
     * @author liaohui
     * date:  2024-12-03
     * @param dto 参数
     */
    @PostMapping("/invalid")
    @LogAction(value = LogActionEnum.INVALID, desc = "作废")
    public ApiResult<List<BatchResultDTO>> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Set<String> ids = new HashSet<>(dto.getIds());
        for (String id : ids) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = mouldInfoService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("模具作废失败",e);
                MouldInfoEntity entity = mouldInfoService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "模具作废失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getName(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 更新备注
     * @author liaohui
     * date:  2024-12-03
     * @param dto 参数
     */
    @PostMapping("/updateRemark")
    @LogAction(value = LogActionEnum.UPDATE, desc = "更新备注")
    public ApiResult<List<BatchResultDTO>> updateRemark(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = mouldInfoService.updateRemark(id, dto.getRemark());
            }catch (Exception e){
                log.error("模具更新备注失败",e);
                MouldInfoEntity entity = mouldInfoService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "模具不存在, 更新备注失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getName(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 更新存放位置
     * @author liaohui
     * date:  2024-12-03
     * @param dto 参数
     */
    @PostMapping("/updateStoreLocation")
    @LogAction(value = LogActionEnum.UPDATE, desc = "更新存放位置")
    public ApiResult<List<BatchResultDTO>> updateStoreLocation(@RequestBody @Validated MouldInfoDTO.StoreLocationDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getDetailId().size());
        for (String id : dto.getDetailId()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = mouldInfoService.updateStoreLocation(id, dto);
            }catch (Exception e){
                log.error("模具更新存放位置失败",e);
                MouldInfoEntity entity = mouldInfoService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "模具不存在, 更新存放位置失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getName(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 更新启用时间
     * @author liaohui
     * date:  2024-12-03
     * @param dto 参数
     */
    @PostMapping("/updateEnableTime")
    @LogAction(value = LogActionEnum.UPDATE, desc = "更新启用时间")
    public ApiResult<List<BatchResultDTO>> updateEnableTime(@RequestBody @Validated MouldInfoDTO.EnableTimeDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getDetailIdList().size());
        for (String id : dto.getDetailIdList()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = mouldInfoService.updateEnableTime(id, dto.getEnableTime());
            }catch (Exception e){
                log.error("模具更新启用时间失败",e);
                MouldInfoEntity entity = mouldInfoService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "模具不存在, 更新启用时间失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getName(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 导出
     * @author liaohui
     * date:  2024-12-03
     * @param dto 参数
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出")
    @PostMapping("/export")
    public void export(@RequestBody @Validated MouldInfoDTO.PagingParamDTO dto) {
        mouldInfoService.export(dto);
    }


    /**
     * 下单跟踪
     * @param dto 参数
     */
    @PostMapping("/orderTracking")
    @WebAdvanceQuery
    public ApiResult<PagingVO<MouldInfoDTO.OrderTrackingViewDTO>> orderTracking(@RequestBody @Validated PagingDTO<MouldInfoDTO.PagingParamDTO> dto) {
        PagingVO<MouldInfoDTO.OrderTrackingViewDTO> page = mouldInfoService.orderTracking(dto);
        return success(page);
    }


    /**
     * tab
     * @param dto 参数
     */
    @PostMapping("/tabList")
    public ApiResult<List<MouldInfoDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<MouldInfoDTO.TabListDTO> tabList = mouldInfoService.tabList(dto);
        return success(tabList);
    }

    /**
     * 费用返还确认
     * @param dto 参数
     */
    @PostMapping("/returnConfirm")
    public ApiResult<String> returnConfirm(@RequestBody @Validated MouldInfoDTO.ReturnConfirmDTO dto) {
        mouldInfoService.returnConfirm(dto);
        return success();
    }

    /**
     * 关联下单产品
     * @param dto 参数
     */
    @PostMapping("/refProduct")
    public ApiResult<String> refProduct(@RequestBody @Validated MouldInfoDTO.RefProductDTO dto) {
        mouldInfoService.refProduct(dto);
        return success();
    }

    /**
     * 下单明细
     * @param dto 参数
     */
    @PostMapping("/orderTrackingDetail")
    public ApiResult<PagingVO<MouldInfoDTO.OrderTrackingDetailDTO>> orderTrackingDetail(@RequestBody @Validated MouldInfoDTO.OrderTrackingDetailParamDTO dto) {
        PagingVO<MouldInfoDTO.OrderTrackingDetailDTO> page = mouldInfoService.orderTrackingDetail(dto);
        return success(page);
    }
}
