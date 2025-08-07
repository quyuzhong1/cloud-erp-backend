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
import com.erp.model.wms.dto.SoReturnNoticeDTO;
import com.erp.model.wms.dto.SoReturnReceiveDTO;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.model.wms.entity.SoReturnReceiveEntity;
import com.erp.server.wms.query.SoReturnReceiveQueryHandler;
import com.erp.server.wms.service.SoReturnReceiveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 销售退货签收单
 * @author Luo_WG
 * @since 2023-05-10
 */
@Slf4j
@RestController
@LogSystemModule("销售退货签收单")
@RequestMapping("/soReturnReceive")
public class SoReturnReceiveController extends BaseController {

    @Resource
    private SoReturnReceiveService soReturnReceiveService;

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoReturnReceiveDTO.PagingViewDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id,warehouse_keeper_id",
            warehouseTableField = "srr.warehouse_id",
            menuCode = "wms:soReturnReceive:paging",
            tableAlias = "srr"
    )
    @WebAdvanceQuery(handler = SoReturnReceiveQueryHandler.class)
    public ApiResult<PagingVO<SoReturnReceiveDTO.PagingView>> paging(@RequestBody @Validated PagingDTO<SoReturnReceiveDTO.PagingParam> dto) {
        PagingVO<SoReturnReceiveDTO.PagingView> pagingVO = soReturnReceiveService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/4/17 13:14
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.SoReturnReceiveDTO.soDeliveryNoticeCountDTO>>
     **/
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id,warehouse_keeper_id",
            warehouseTableField = "srr.warehouse_id",
            menuCode = "wms:soReturnReceive:paging",
            tableAlias = "srr"
    )
    public ApiResult<List<SoReturnReceiveDTO.StatusCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<SoReturnReceiveDTO.StatusCountDTO> soDeliveryNoticeCountDTOS = soReturnReceiveService.listCount(dto);
        return success(soDeliveryNoticeCountDTOS);
    }

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "新增销售退货签收单")
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated SoReturnReceiveDTO.Add dto) {
        String id = soReturnReceiveService.add(dto);
        return CharSequenceUtil.isNotBlank(id) == true ? success() : failure();
    }

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改销售退货签收单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnReceive:update",
            serviceClass = SoReturnReceiveService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated SoReturnReceiveDTO.Update dto) {
        Boolean flag = soReturnReceiveService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/4/6 18:57
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.SoReturnReceiveDTO.ViewDTO>
     **/
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnReceive:view",
            serviceClass = SoReturnReceiveService.class,
            keyIdName = "id")
    public ApiResult<SoReturnReceiveDTO.View> view(@RequestParam("id") String id) {
        SoReturnReceiveDTO.View dto = soReturnReceiveService.view(id);
        return success(dto);
    }

    /**
     * 提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交销售退货签收单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnReceive:submit",
            serviceClass = SoReturnReceiveService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soReturnReceiveService.submit(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 新增提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交销售退货签收单")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnReceive:add",
            serviceClass = SoReturnReceiveService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated SoReturnReceiveDTO.Add dto) {
        Boolean flag = soReturnReceiveService.addAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交销售退货签收单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnReceive:update",
            serviceClass = SoReturnReceiveService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated SoReturnReceiveDTO.Update dto) {
        Boolean flag = soReturnReceiveService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核销售退货签收单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnReceive:approve",
            serviceClass = SoReturnReceiveService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoReturnReceiveEntity> entityList = soReturnReceiveService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoReturnReceiveEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"退货签收单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(soReturnReceiveService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess()));
            }catch (Exception e){
                log.error("退货签收单审核失败",e);
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
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核销售退货签收单")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnReceive:disApprove",
            serviceClass = SoReturnReceiveService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
            List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
            List<String> ids = dto.getIds();
        List<SoReturnReceiveEntity> entityList = soReturnReceiveService.listByIds(dto.getIds());
            for (String id : ids) {
                BatchResultDTO submit;
                String flagCode = id;
                try {
                    SoReturnReceiveEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
                    if (Objects.isNull(entity)) {
                        submit = BatchResultDTO.fail(id,flagCode, "退货签收单不存在");
                    } else {
                        flagCode = entity.getCode();
                        submit = soReturnReceiveService.disApprove(entity);
                    }
                } catch (Exception e) {
                    log.error("退货签收单反审核失败>>>>{}", e);
                    submit = BatchResultDTO.fail(id,flagCode, e.getMessage());
                }
                resultDTOS.add(submit);
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
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销销售退货签收单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnReceive:cancelProcess",
            serviceClass = SoReturnReceiveService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soReturnReceiveService.cancelProcess(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 批量作废
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param remarkDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INVALID, desc = "作废销售退货签收单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnReceive:invalid",
            serviceClass = SoReturnReceiveService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO remarkDTO) {
        Boolean flag = soReturnReceiveService.invalid(remarkDTO.getIds(), remarkDTO.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 批量删除
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param idsDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "删除销售退货签收单 id为:{ids}")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnReceive:delete",
            serviceClass = SoReturnReceiveService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        try {
            List<BatchResultDTO> resultDTOS = soReturnReceiveService.deleteByIds(idsDTO.getIds(), true);
            return success(resultDTOS);
        } catch (Exception e) {
            log.error("批量删除销售退货签收单失败", e);
            return failure(e.getMessage());
        }
    }

    /**
     * 导出
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出销售退货签收单")
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody SoReturnReceiveDTO.PagingParam dto) {
        Boolean flag = soReturnReceiveService.exportExcel(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 下推退货签收单-保存
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param validList validList
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "下推退货签收单")
    @PostMapping(value = "/generateSoReturnReceiveSave")
    public ApiResult generateSoReturnReceiveSave(@RequestBody @Validated ValidList<SoReturnNoticeDTO.GenerateSoReturnReceiveView> validList) {
        Boolean flag = soReturnReceiveService.generateSoReturnReceiveSave(validList.getList());
        return flag == true ? success() : failure();
    }

    /**
     * 下推退货入库单-列表查询
     * @Author Luo_WG
     * @Date 2023/5/23 15:52
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.SoReturnInstockDTO.GenerateSoReturnInstockView>
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "下推退货入库单")
    @PostMapping(value = "/generateSoReturnInstockView")
    public ApiResult<List<SoReturnReceiveDTO.ReceiveGenerateSoReturnInstockView>> generateSoReturnInstockView(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<SoReturnReceiveDTO.ReceiveGenerateSoReturnInstockView> generateSoDeliveryViews = soReturnReceiveService.generateSoReturnInstockView(dto.getIds());
        return success(generateSoDeliveryViews);
    }
}
