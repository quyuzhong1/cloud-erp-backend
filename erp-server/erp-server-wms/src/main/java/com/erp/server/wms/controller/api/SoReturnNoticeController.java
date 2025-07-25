package com.erp.server.wms.controller.api;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.wms.dto.SoReturnNoticeDTO;
import com.erp.model.wms.entity.SoReturnNoticeEntity;
import com.erp.server.wms.query.SoReturnNoticeQueryHandler;
import com.erp.server.wms.service.SoReturnNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 销售退货通知单
 * @author Luo_WG
 * @since 2023-05-10
 */
@Slf4j
@RestController
@LogSystemModule("销售退货通知单")
@RequestMapping("/soReturnNotice")
public class SoReturnNoticeController extends BaseController {
    @Resource
    private SoReturnNoticeService soReturnNoticeService;

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoReturnNoticeDTO.PagingViewDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id,warehouse_keeper_id",
            warehouseTableField = "srn.warehouse_id",
            menuCode = "wms:soReturnNotice:paging",
            tableAlias = "srn"
    )
    @WebAdvanceQuery(handler = SoReturnNoticeQueryHandler.class)
    public ApiResult<PagingVO<SoReturnNoticeDTO.PagingView>> paging(@RequestBody @Validated PagingDTO<SoReturnNoticeDTO.PagingParam> dto) {
        PagingVO<SoReturnNoticeDTO.PagingView> pagingVO = soReturnNoticeService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/4/17 13:14
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.SoReturnNoticeDTO.soDeliveryNoticeCountDTO>>
     **/
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id,warehouse_keeper_id",
            warehouseTableField = "srn.warehouse_id",
            menuCode = "wms:soReturnNotice:paging",
            tableAlias = "srn"
    )
    public ApiResult<List<SoReturnNoticeDTO.StatusCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<SoReturnNoticeDTO.StatusCountDTO> soDeliveryNoticeCountDTOS = soReturnNoticeService.listCount(dto);
        return success(soDeliveryNoticeCountDTOS);
    }

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "新增销售退货通知单")
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated SoReturnNoticeDTO.Add dto) {
        String id = soReturnNoticeService.add(dto);
        return CharSequenceUtil.isNotBlank(id) == true ? success() : failure();
    }

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改销售退货通知单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnNotice:update",
            serviceClass = SoReturnNoticeService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated SoReturnNoticeDTO.Update dto) {
        Boolean flag = soReturnNoticeService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/4/6 18:57
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.SoReturnNoticeDTO.ViewDTO>
     **/
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnNotice:view",
            serviceClass = SoReturnNoticeService.class,
            keyIdName = "id")
    public ApiResult<SoReturnNoticeDTO.View> view(@RequestParam("id") String id) {
        SoReturnNoticeDTO.View dto = soReturnNoticeService.view(id);
        return success(dto);
    }

    /**
     * 提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交销售退货通知单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnNotice:submit",
            serviceClass = SoReturnNoticeService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soReturnNoticeService.submit(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 新增提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交销售退货通知单")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnNotice:add",
            serviceClass = SoReturnNoticeService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated SoReturnNoticeDTO.Add dto) {
        Boolean flag = soReturnNoticeService.addAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交销售退货通知单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnNotice:update",
            serviceClass = SoReturnNoticeService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated SoReturnNoticeDTO.Update dto) {
        Boolean flag = soReturnNoticeService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核销售退货通知单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnNotice:approve",
            serviceClass = SoReturnNoticeService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoReturnNoticeEntity> entityList = soReturnNoticeService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoReturnNoticeEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"退货通知单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(soReturnNoticeService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess()));
            }catch (Exception e){
                log.error("退货通知单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量反审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核销售退货通知单")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnNotice:disApprove",
            serviceClass = SoReturnNoticeService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoReturnNoticeEntity> entityList = soReturnNoticeService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoReturnNoticeEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购收货单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(soReturnNoticeService.disApprove(entity));
            }catch (Exception e){
                log.error("采购收货单反审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 取消流程
     * @Author Luo_WG
     * @Date 2023/4/13 18:58
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销销售退货通知单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnNotice:cancelProcess",
            serviceClass = SoReturnNoticeService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soReturnNoticeService.cancelProcess(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 批量作废
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param remarkDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INVALID, desc = "作废销售退货通知单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnNotice:invalid",
            serviceClass = SoReturnNoticeService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO remarkDTO) {
        Boolean flag = soReturnNoticeService.invalid(remarkDTO.getIds(), remarkDTO.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 批量删除
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param idsDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "删除销售退货通知单")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnNotice:delete",
            serviceClass = SoReturnNoticeService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        Boolean flag = soReturnNoticeService.delete(idsDTO.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 导出
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出销售退货通知单")
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody SoReturnNoticeDTO.PagingParam dto) {
        Boolean flag = soReturnNoticeService.exportExcel(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 下推退货通知单-保存
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param validList validList
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "下推退货通知单")
    @PostMapping(value = "/generateSoReturnNoticeSave")
    public ApiResult generateSoReturnNoticeSave(@RequestBody ValidList<SoReturnDTO.GenerateSoReturnNoticeView> validList) {
        Boolean flag = soReturnNoticeService.generateSoReturnNoticeSave(validList.getList());
        return flag == true ? success() : failure();
    }

    /**
     * 下推退货签收单-列表查询
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "下推退货签收单")
    @PostMapping(value = "/generateSoDeliveryView")
    public ApiResult<List<SoReturnNoticeDTO.GenerateSoReturnReceiveView>> generateSoDeliveryView(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<SoReturnNoticeDTO.GenerateSoReturnReceiveView> generateSoDeliveryViews = soReturnNoticeService.generateSoDeliveryView(dto.getIds());
        return success(generateSoDeliveryViews);
    }
}
