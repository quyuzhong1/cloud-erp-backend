package com.erp.server.wms.controller.api;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.StocktakingProfitLossDTO;
import com.erp.model.wms.entity.StocktakingProfitLossEntity;
import com.erp.server.wms.query.StocktakingProfitLossQueryHandler;
import com.erp.server.wms.service.StocktakingProfitLossService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
 * 盘点管理-盘盈盘亏单
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Slf4j
@RestController
@LogSystemModule("盘盈盘亏单")
@RequestMapping("/stocktakingProfitLoss")
public class StocktakingProfitLossController extends BaseController {

    @Resource
    private StocktakingProfitLossService stocktakingProfitLossService;


    /**
     * 获取 tab列表
     *
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingProfitLoss:paging",
            tableAlias = "spl"
    )
    public ApiResult<List<StocktakingProfitLossDTO.TabDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<StocktakingProfitLossDTO.TabDTO> tabList = stocktakingProfitLossService.tabList(dto);
        return success(tabList);
    }

    /**
     * 添加
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增盘盈盘亏单")
    @PostMapping("/add")
    public ApiResult add(@RequestBody StocktakingProfitLossDTO.AddDTO dto) {
        String id = stocktakingProfitLossService.add(dto);
        return CharSequenceUtil.isNotBlank(id) ? success() : failure();
    }


    /**
     * 修改
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改盘盈盘亏单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingProfitLoss:update",
            serviceClass = StocktakingProfitLossService.class,
            keyIdName = "id"
    )
    public ApiResult update(@RequestBody StocktakingProfitLossDTO.UpdateDTO dto) {
        String id = stocktakingProfitLossService.update(dto);
        return CharSequenceUtil.isNotBlank(id) ? success() : failure();
    }


    /**
     * 分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingProfitLoss:paging",
            tableAlias = "spl"
    )
    @WebAdvanceQuery(handler = StocktakingProfitLossQueryHandler.class)
    public ApiResult<PagingVO<StocktakingProfitLossDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<StocktakingProfitLossDTO.PagingParamDTO> dto) {
        PagingVO<StocktakingProfitLossDTO.PagingViewDTO> pagingVO = stocktakingProfitLossService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 导出
     * 数据
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出盘盈盘亏单")
    @PostMapping("/export")
    public ApiResult exportWarehouse(@RequestBody @Valid StocktakingProfitLossDTO.ExportDTO dto) {
        Boolean result = stocktakingProfitLossService.exportExcel(dto);
        return result ? success() : failure();
    }


    /**
     * 详情
     *
     * @param dto
     * @return
     */
    @LogViewService
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingProfitLoss:view",
            serviceClass = StocktakingProfitLossService.class,
            keyIdName = "id"
    )
    public ApiResult<StocktakingProfitLossDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        StocktakingProfitLossDTO.ViewDTO result = stocktakingProfitLossService.view(dto.getId());
        return success(result);
    }


    /**
     * 提交
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交盘盈盘亏单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingProfitLoss:submit",
            serviceClass = StocktakingProfitLossService.class,
            keyIdName = "ids"
    )
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<String> ids = dto.getIds();
        for (String id : ids) {
            BatchResultDTO submit;
            try {
                submit = stocktakingProfitLossService.submit(id);
            } catch (Exception e) {
                log.error("盘盈盘亏单 提交审核失败>>>>{}", e);
                StocktakingProfitLossEntity entity = stocktakingProfitLossService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "盘盈盘亏单不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }

        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 保存并提交
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "保存并提交盘盈盘亏单")
    @PostMapping("/addAndSubmit")
    public ApiResult addAndSubmit(@RequestBody @Validated StocktakingProfitLossDTO.AddDTO dto) {
        stocktakingProfitLossService.addAndSubmit(dto);
        return success();
    }

    /**
     * 修改并提交
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交盘盈盘亏单")
    @PostMapping("/updateAndSubmit")
    public ApiResult updateAndSubmit(@RequestBody @Validated StocktakingProfitLossDTO.UpdateDTO dto) {
        stocktakingProfitLossService.updateAndSubmit(dto);
        return success();
    }


    @LogAction(value = LogActionEnum.APPROVE, desc = "审核盘盈盘亏单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingProfitLoss:approve",
            serviceClass = StocktakingProfitLossService.class,
            keyIdName = "ids"
    )
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<String> ids = dto.getIds();
        for (String id : ids) {
            BatchResultDTO submit;
            try {
                submit = stocktakingProfitLossService.approve(id, new ApproveOneDTO(id, dto.getType(), dto.getComment()));
            } catch (Exception e) {
                log.error("盘盈盘亏单 审核失败>>>>{}", e);
                StocktakingProfitLossEntity entity = stocktakingProfitLossService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "盘盈盘亏单不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }

        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 撤销流程
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销盘盈盘亏单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingProfitLoss:cancelProcess",
            serviceClass = StocktakingProfitLossService.class,
            keyIdName = "ids"
    )
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<String> ids = dto.getIds();
        for (String id : ids) {
            BatchResultDTO submit;
            try {
                submit = stocktakingProfitLossService.cancelProcess(id);
            } catch (Exception e) {
                log.error("盘盈盘亏单 撤销流程失败>>>>{}", e);
                StocktakingProfitLossEntity entity = stocktakingProfitLossService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "盘盈盘亏单不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 删除盘盈盘亏单
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除盘盈盘亏单")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingProfitLoss:delete",
            serviceClass = StocktakingProfitLossService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = stocktakingProfitLossService.delete(id);
            } catch (Exception e) {
                log.error("盘盈盘亏单删除失败===>{}", e);
                StocktakingProfitLossEntity entity = stocktakingProfitLossService.getById(id);
                if(Objects.isNull(entity)){
                    deleteResult = BatchResultDTO.fail(id, id, "盘盈盘亏单不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
