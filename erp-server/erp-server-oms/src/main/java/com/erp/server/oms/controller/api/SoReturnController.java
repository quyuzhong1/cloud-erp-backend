package com.erp.server.oms.controller.api;


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
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.oms.dto.listAddDetailViewDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.server.oms.query.SoReturnQueryHandler;
import com.erp.server.oms.service.SoReturnDetailService;
import com.erp.server.oms.service.SoReturnService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 销售退货单
 * @author Luo_WG
 * @since 2023-05-10
 */
@RestController
@LogSystemModule("销售退货订单")
@RequestMapping("/soReturn")
@Slf4j
public class SoReturnController extends BaseController {
    @Resource
    private SoReturnService soReturnService;

    @Resource
    private SoReturnDetailService soReturnDetailService;
    
    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoReturnDTO.PagingViewDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:soReturn:paging",
            tableAlias = "sr"
    )
    @WebAdvanceQuery(handler = SoReturnQueryHandler.class)
    public ApiResult<PagingVO<SoReturnDTO.PagingView>> paging(@RequestBody @Validated PagingDTO<SoReturnDTO.PagingParam> dto) {
        PagingVO<SoReturnDTO.PagingView> pagingVO = soReturnService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/4/17 13:14
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.SoReturnDTO.soReturnCountDTO>>
     **/
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:soReturn:paging",
            tableAlias = "sr"
    )
    public ApiResult<List<SoReturnDTO.StatusCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<SoReturnDTO.StatusCountDTO> soReturnCountDTOS = soReturnService.listCount(dto);
        return success(soReturnCountDTOS);
    }

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "新增销售退货订单")
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated SoReturnDTO.Add dto) {
        String id = soReturnService.add(dto);
        return StringUtils.isNotBlank(id) == true ? success() : failure();
    }

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改销售退货订单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soReturn:update",
            serviceClass = SoReturnService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated SoReturnDTO.Update dto) {
        Boolean flag = soReturnService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/4/6 18:57
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.SoReturnDTO.ViewDTO>
     **/
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soReturn:view",
            serviceClass = SoReturnService.class,
            keyIdName = "id")
    public ApiResult<SoReturnDTO.View> view(@RequestParam("id") String id) {
        SoReturnDTO.View dto = soReturnService.view(id);
        return success(dto);
    }

    /**
     * 提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交销售退货订单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soReturn:submit",
            serviceClass = SoReturnService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soReturnService.submit(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 新增提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交销售退货订单")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soReturn:add",
            serviceClass = SoReturnService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated SoReturnDTO.Add dto) {
        Boolean flag = soReturnService.addAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交销售退货订单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soReturn:update",
            serviceClass = SoReturnService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated SoReturnDTO.Update dto) {
        Boolean flag = soReturnService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param baseApproveParamDTO baseApproveParamDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.APPROVE, desc = "批量审核销售退货订单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soReturn:approve",
            serviceClass = SoReturnService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoReturnEntity> entityList = soReturnService.listByIds(ids);
        for (String id : ids) {
            SoReturnEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"销售退货订单不存在"));
                continue;
            }
            try {
                resultDTOS.add(soReturnService.approve(dto, entity));
            }catch (Exception e){
                log.error("销售退货订单审核失败",e);
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
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "批量反审核销售退货订单")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soReturn:disApprove",
            serviceClass = SoReturnService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoReturnEntity> entityList = soReturnService.listByIds(ids);
        for (String id : ids) {
            SoReturnEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"销售退货订单不存在"));
                continue;
            }
            try {
                resultDTOS.add(soReturnService.disApprove(entity));
            }catch (Exception e){
                log.error("反审核销售退货订单失败",e);
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
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销销售退货订单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soReturn:cancelProcess",
            serviceClass = SoReturnService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soReturnService.cancelProcess(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 批量作废
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param remarkDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INVALID, desc = "批量作废销售退货订单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soReturn:invalid",
            serviceClass = SoReturnService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO remarkDTO) {
        Boolean flag = soReturnService.invalid(remarkDTO.getIds(), remarkDTO.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 批量删除
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param idsDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除销售退货订单")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soReturn:delete",
            serviceClass = SoReturnService.class,
            keyIdName = "ids"
    )
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        Boolean flag = soReturnService.delete(idsDTO.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 导出
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出销售退货订单")
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody SoReturnDTO.PagingParam dto) {
        Boolean flag = soReturnService.exportExcel(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 下推退货通知单-列表查询
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping(value = "/generateSoReturnNoticeView")
    public ApiResult<List<SoReturnDTO.GenerateSoReturnNoticeView>> generateSoReturnNoticeView(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<SoReturnDTO.GenerateSoReturnNoticeView> generateSoReturnNoticeViews = soReturnService.generateSoReturnNoticeView(dto.getIds());
        return success(generateSoReturnNoticeViews);
    }

    /**
     * 获取所有已审核订单
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @return com.common.core.controller.vo.ApiResult
     **/
    @GetMapping(value = "/listSoReturnByApproveStatus")
    public ApiResult<List<SoReturnEntity>> listSoReturnByApproveStatus() {
        List<SoReturnEntity> entityList = soReturnService.listSoReturnByApproveStatus();
        return success(entityList);
    }

    /**
     * 根据退货单id查询退货单信息
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @return com.common.core.controller.vo.ApiResult
     **/
    @GetMapping(value = "/getSoReturnById")
    public ApiResult<SoReturnEntity> getSoReturnById(@RequestParam("id") String id) {
        SoReturnEntity entity = soReturnService.getSoReturnById(id);
        return success(entity);
    }

    /**
     * 添加详情按钮-列表查询
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.oms.dto.SoDetailDTO.AddDetailView>>
     * @Author Luo_WG
     * @Date 2023/5/16 18:43
     **/
    @PostMapping("/listAddDetailView")
    public ApiResult<List<SoDetailDTO.AddDetailView>> listAddDetailView(@RequestBody listAddDetailViewDTO dto) {
        List<SoDetailDTO.AddDetailView> addDetailViews = soReturnDetailService.listAddDetailView(dto);
        return success(addDetailViews);
    }

    /**
     * 下推销售退货订单-保存
     * @Author Luo_WG
     * @Date 2023/5/25 12:30
     * @param validList validList
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "下推销售退货订单-保存")
    @PostMapping("/generateSoReturnSave")
    public ApiResult generateSoReturnSave(@RequestBody @Validated ValidList<SoInfoDTO.GenerateSoReturnView> validList) {
        Boolean flag = soReturnService.generateSoReturnSave(validList.getList());
        return flag ? success() : failure();
    }

    /**
     * 销售单详情-单据关联-退货订单号列表
     * @Author Luo_WG
     * @Date 2023/5/25 16:19
     * @param id id
     * @return com.common.core.controller.vo.ApiResult
     **/
    @GetMapping("/listSoReturnDetail")
    public ApiResult<List<SoReturnDTO.PagingView>> listSoReturnDetailBySourceId(@RequestParam("soId") String id) {
        List<SoReturnDTO.PagingView> list = soReturnService.listSoReturnDetailBySourceId(id);
        return success(list);
    }

    /**
     * 快粘贴查询, 需要区分是否拆分套装BOM
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.oms.dto.SoDetailDTO.AddDetailView>>
     * @Author jack
     * @Date 2024-11-08
     **/
    @PostMapping("/listAddDetailWithNoBomView")
    public ApiResult<SoDetailDTO.ListAddDetailNoBomViewDTO> listAddDetailWithNoBomView(@RequestBody SoReturnDTO.PlatformSkuDTO dto) {
        return success(soReturnDetailService.listAddDetailWithNoBomView(dto));
    }
}
