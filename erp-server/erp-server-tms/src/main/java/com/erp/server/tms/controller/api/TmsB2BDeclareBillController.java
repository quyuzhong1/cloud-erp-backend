package com.erp.server.tms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.tms.entity.TmsDeclareBillEntity;
import com.erp.model.tms.enums.DeclareStatusEnum;
import com.erp.model.wms.enums.PackingTaskStatusEnum;
import com.erp.model.wms.enums.WmsDeclareStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.tms.query.TmsB2BDeclareQueryHandler;
import com.erp.server.tms.query.TmsFmDeclareQueryHandler;
import com.erp.server.tms.service.TmsDeclareBillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_TMS_B2B_DECLARE_BILL;

/**
 * B2B报关单
 *
 * @author lrp
 * @since 2024-03-27
 */
@Slf4j
@RestController
@LogSystemModule("B2B报关单")
@RequestMapping("/tmsB2BDeclareBill")
public class TmsB2BDeclareBillController extends BaseController {

    @Resource
    private TmsDeclareBillService tmsDeclareBillService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    /**
     * tabList
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @GetMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsB2BDeclareBill:paging",
            tableAlias = "db"
    )
    public ApiResult<List<TmsDeclareBillDTO.TabListDTO>> tabList(PermissionsDTO permissionsDTO) {
        return success(tmsDeclareBillService.tabList(SourceTypeEnum.B2B_DECLARE_BILL,permissionsDTO));
    }

    /**
     * 分页列表
     * @author lrp
     * @date:  2024-03-19
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = TmsB2BDeclareQueryHandler.class)
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsB2BDeclareBill:paging",
            tableAlias = "db"
    )
    public ApiResult<PagingVO<TmsDeclareBillDTO.PagingVO>> paging(@RequestBody @Valid PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto) {
        dto.getParams().setType(SourceTypeEnum.B2B_DECLARE_BILL.getCode());
        PagingVO<TmsDeclareBillDTO.PagingVO> pagingVO = tmsDeclareBillService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 列表统计
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @GetMapping("/statistics")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsB2BDeclareBill:paging",
            tableAlias = "db"
    )
    public ApiResult<TmsDeclareBillDTO.StatisticsVO> statistics(PermissionsDTO permissionsDTO) {
        return success(tmsDeclareBillService.statisticsBySoOut(permissionsDTO));
    }

    /**
    * 新增
    * @author lrp
    * @date:  2024-03-27
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "B2B报关单新增")
    public ApiResult<Boolean> add(@RequestBody @Validated TmsDeclareBillDTO.AddDTO dto) {
        return success(tmsDeclareBillService.addB2BDeclare(dto));
    }

    /**
    * 修改
    * @author lrp
    * @date:  2024-03-27
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "B2B报关单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:tmsB2BDeclareBill:update",
        serviceClass = TmsDeclareBillService.class,
        keyIdName = "id")
    public ApiResult<Object> update(@RequestBody @Validated TmsDeclareBillDTO.UpdateDTO dto) {
        tmsDeclareBillService.update(dto,SourceTypeEnum.B2B_DECLARE_BILL);
        return success();
    }

    /**
     * 查询符合生成条件的销售出库单
     * @author lrp
     * @date:  2024-03-27
     * @return ApiResult
     */
    @GetMapping("/getCanGenerateSoOut")
    public ApiResult<List<TmsDeclareBillDTO.SoOutDTO>> getCanGenerateSoOut() {
        TmsDeclareBillDTO.QuerySourceDTO querySourceDTO = TmsDeclareBillDTO.QuerySourceDTO.builder()
                .packingStatus(PackingTaskStatusEnum.PACKED.getCode())
                .declareStatus(WmsDeclareStatusEnum.WAIT.getCode())
                .build();
        return success(tmsDeclareBillService.getCanGenerateSoOut(querySourceDTO));
    }

    /**
     * 根据选中SKU查询报关表头信息
     * @author will
     * @date 2026/5/7 14:47
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.tms.dto.TmsDeclareBillDTO.SelectedSkuHeaderDTO>
     */
    @PostMapping("/querySelectedSkuHeader")
    public ApiResult<TmsDeclareBillDTO.SelectedSkuHeaderDTO> querySelectedSkuHeader(@RequestBody @Valid TmsDeclareBillDTO.SelectedSkuHeaderParamDTO dto) {
        return success(tmsDeclareBillService.querySelectedSkuHeader(dto, SourceTypeEnum.B2B_DECLARE_BILL));
    }

    /**
     * 详情
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @GetMapping("/view")
    public ApiResult<TmsDeclareBillDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(tmsDeclareBillService.view(id));
    }

    /**
     * 批量更新字段下拉配置
     */
    @GetMapping("/batchUpdateFieldDropDown")
    public ApiResult<List<TmsDeclareBillDTO.BatchUpdateFieldDropDownDTO>> batchUpdateFieldDropDown() {
        return success(tmsDeclareBillService.batchUpdateFieldDropDown());
    }

    /**
     * 报关状态更新
     * @author lrp
     * @date:  2024-03-19
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/confirmDeclareStatus")
    @LogAction(value = LogActionEnum.CONFIRM, desc = "B2B报关单报关状态更新")
    public ApiResult<List<BatchResultDTO>> confirmDeclareStatus(@RequestBody @Validated TmsDeclareBillDTO.ConfirmDeclareStatusDTO dto) {
        List<BatchResultDTO> resultList = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            TmsDeclareBillDTO.ConfirmDeclareStatusDTO singleDTO = new TmsDeclareBillDTO.ConfirmDeclareStatusDTO();
            singleDTO.setIds(Collections.singletonList(id));
            singleDTO.setDeclareStatus(dto.getDeclareStatus());
            singleDTO.setDeclarConfirmDate(dto.getDeclarConfirmDate());
            singleDTO.setDeclarUserId(dto.getDeclarUserId());
            singleDTO.setDeclarUserName(dto.getDeclarUserName());
            resultList.add(tmsDeclareBillService.confirmDeclareStatus(singleDTO, SourceTypeEnum.B2B_DECLARE_BILL));
        }
        return resultList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultList) : failure(resultList);
    }


    /**
     * 删除报关单
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除记录")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsB2BDeclareBill:delete",
            serviceClass = TmsDeclareBillService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated TmsDeclareBillDTO.DeleteDTO dto) {
        List<BatchResultDTO> resultDTOList = tmsDeclareBillService.delete(dto);
        return resultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOList) : failure(resultDTOList);
    }


    /**
     * 导出B2B报关单列表
     */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出B2B报关单列表")
    @WebAdvanceQuery(handler = TmsB2BDeclareQueryHandler.class)
    public ApiResult<Object>export(@RequestBody @Valid TmsDeclareBillDTO.PagingParamDTO pagingParamDTO) {
        downloadTaskFeign.saveDownloadTask("B2B报关单列表", EXPORT_TMS_TMS_B2B_DECLARE_BILL.getCode(), pagingParamDTO);
        return success();
    }

//    /**
//     * 导出B2B报关单报关信息
//     */
//    @PostMapping("/exportDeclare")
//    @LogAction(value = LogActionEnum.EXPORT, desc = "导出B2B报关单报关信息")
//    @WebAdvanceQuery(handler = TmsB2BDeclareQueryHandler.class)
//    public ApiResult<Object>exportDeclare(@RequestBody @Valid TmsDeclareBillDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) throws IOException {
//        pagingParamDTO.setType(SourceTypeEnum.B2B_DECLARE_BILL.getCode());
//        tmsDeclareBillService.exportDeclare(pagingParamDTO,response);
//        return success();
//    }

    /**
     * 导出报关 - 多 sheet 版（报关单 + 合同；发票 / 装箱单 / 装箱明细 后续补充）
     *
     * <p>与 {@link #exportDeclare} 业务口径一致，仅产物形态不同：单条 → 含多 sheet 的 xlsx；多条 → ZIP 包，
     * 包内每个 xlsx 同样多 sheet。单次导出最多 100 条，超过抛业务异常。</p>
     */
    @PostMapping("/exportDeclare")
    @LogAction(value = LogActionEnum.EXPORT, desc = "多sheet导出B2B报关单报关信息")
    @WebAdvanceQuery(handler = TmsFmDeclareQueryHandler.class)
    public ApiResult<Object> exportDeclare(@RequestBody @Valid TmsDeclareBillDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) throws IOException {
        pagingParamDTO.setType(SourceTypeEnum.B2B_DECLARE_BILL.getCode());
        tmsDeclareBillService.exportDeclareMulti(pagingParamDTO, response);
        return success();
    }


    /**
     * 批量更新备注
     * @author will
     * @date 2026/4/21 14:42
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>
     */
    @PostMapping("/batchUpdateRemark")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "更新报关单备注")
    public ApiResult<List<BatchResultDTO>> batchUpdateRemark(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<TmsDeclareBillEntity> list = tmsDeclareBillService.lambdaQuery().in(TmsDeclareBillEntity::getId, ids).list();
        Map<String, TmsDeclareBillEntity> idEntityMap = list.stream().collect(Collectors.toMap(TmsDeclareBillEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = tmsDeclareBillService.updateRemark(id,dto.getRemark());
            }catch (Exception e){
                log.error("报关单信息 更新备注失败",e);
                TmsDeclareBillEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "报关单主单不存在, 提交失败");
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
     * 批量更新字段
     */
    @PostMapping("/updateBatchFiled")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "报关单批量更新字段:ids={ids},修改的字段名称编号={updateFiledCode}")
    public ApiResult<List<BatchResultDTO>> updateBatchFiled(@RequestBody @Validated TmsDeclareBillDTO.BatchUpdateFieldDTO dto) {
        if (ObjectUtil.isEmpty(dto.getIds())) {
            throw new ServiceException(ApiError.BILL_SELECTION_REQUIRED);
        }
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<TmsDeclareBillEntity> entityList = tmsDeclareBillService.lambdaQuery().in(TmsDeclareBillEntity::getId, dto.getIds()).list();
        Map<String, TmsDeclareBillEntity> idEntityMap = entityList.stream().collect(Collectors.toMap(TmsDeclareBillEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            TmsDeclareBillEntity entity = idEntityMap.get(id);
            if (ObjectUtil.isEmpty(entity)) {
                resultDTOS.add(BatchResultDTO.fail(id, id, "报关单主单不存在"));
                continue;
            }
            // 仅待确认状态支持批量更新字段
            if (!Objects.equals(entity.getDeclareStatus(), DeclareStatusEnum.WAIT.getCode())) {
                resultDTOS.add(BatchResultDTO.fail(id, id, "仅支持待确认的报关单"));
                continue;
            }
            try {
                TmsDeclareBillDTO.BatchUpdateFieldDTO updateDTO = new TmsDeclareBillDTO.BatchUpdateFieldDTO();
                updateDTO.setId(id);
                updateDTO.setFieldList(dto.getFieldList());
                Boolean flag = tmsDeclareBillService.updateBatchFiled(updateDTO, SourceTypeEnum.B2B_DECLARE_BILL);
                if (flag) {
                    resultDTOS.add(BatchResultDTO.success(id, entity.getCode(), "报关单批量更新成功"));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(id, entity.getCode(), "报关单批量更新失败"));
                }
            } catch (Exception e) {
                log.error("报关单批量更新失败", e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 拆分报关明细
     * @author will
     * @date 2026/4/23 15:21
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.SplitDeclareDTO>>
     */
    @PostMapping("/listSplitB2bDetail")
    public ApiResult<List<TmsDeclareBillDTO.SplitDeclareDTO>> listSplitB2bDetail(@RequestBody @Valid BaseIdDTO dto)  {
        List<TmsDeclareBillDTO.SplitDeclareDTO> list = tmsDeclareBillService.listSplitB2bDetail(dto.getId());
        return success(list);
    }


    /**
     * 批量添加拆分的报关明细
     * @author will
     * @date 2026/4/23 16:11
     * @param declareDTO
     * @return com.common.core.controller.vo.ApiResult<java.lang.Object>
     */
    @PostMapping("/batchAddSplitB2bDetail")
    @LogAction(value = LogActionEnum.INSERT, desc = "保存拆分数据")
    public ApiResult<Object> batchAddSplitB2bDetail(@RequestBody @Valid TmsDeclareBillDTO.AddSplitDeclareDTO declareDTO)  {
        return success( tmsDeclareBillService.batchAddSplitB2bDetail(declareDTO));
    }


    /**
     * 查询合并前报关明细信息
     * @author will
     * @date 2026/4/23 17:56
     * @param dto
     * @return ApiResult<java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.SourceDeliveryDetailDTO>>
     */
    @PostMapping("/listBeforeMergeDetail")
    public ApiResult<List<TmsDeclareBillDTO.SourceDeliveryDetailDTO>> listBeforeMergeDetail(@RequestBody @Valid BaseIdsDTO.IdsDTO dto)  {
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> list = tmsDeclareBillService.listBeforeMergeDetail(dto.getIds());
        return success(list);
    }

    /**
     * 查询合并后报关明细信息
     * @author will
     * @date 2026/4/23 17:56
     * @param dto
     * @return ApiResult<java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.SourceDeliveryDetailDTO>>
     */
    @PostMapping("/listAfterMergeDetail")
    public ApiResult<List<TmsDeclareBillDTO.MergeDeclareBillDTO>> listAfterMergeDetail(@RequestBody @Valid BaseIdsDTO.IdsDTO dto)  {
        return success(tmsDeclareBillService.listAfterMergeDetail(dto.getIds()));
    }


    /**
     * 批量添加合并的报关明细
     * @author will
     * @date 2026/4/23 18:00
     * @param list
     * @return com.common.core.controller.vo.ApiResult<java.lang.Object>
     */
    @PostMapping("/batchAddMergeDetail")
    public ApiResult<Object> batchAddMergeDetail(@RequestBody @Valid ValidList<TmsDeclareBillDTO.MergeDeclareBillDTO> list)  {
        return success(tmsDeclareBillService.batchAddMergeDetail(SourceTypeEnum.B2B_DECLARE_BILL.getCode(),list.getList()));
    }

}
