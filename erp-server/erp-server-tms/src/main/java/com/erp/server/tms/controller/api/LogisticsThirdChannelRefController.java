package com.erp.server.tms.controller.api;


import cn.hutool.core.util.ObjectUtil;
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
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.LogisticsThirdChannelRefDTO;
import com.erp.model.tms.entity.LogisticsThirdChannelRefEntity;
import com.erp.server.tms.query.LogisticsThirdChannelRefQueryHandler;
import com.erp.server.tms.service.LogisticsThirdChannelRefService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * 物流-第三方渠道关系表
 *
 * @author zdy
 * @since 2025-05-29
 */
@Slf4j
@RestController
@LogSystemModule("物流-第三方渠道关系表")
@RequestMapping("/logisticsThirdChannelRef")
public class LogisticsThirdChannelRefController extends BaseController {

    @Resource
    private LogisticsThirdChannelRefService logisticsThirdChannelRefService;

    /**
    * 新增
    * @author zdy
    * @date:  2025-05-29
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流-第三方渠道关系表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated LogisticsThirdChannelRefDTO.AddDTO dto) {
        return success(logisticsThirdChannelRefService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2025-05-29
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "物流-第三方渠道关系表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:logisticsThirdChannelRef:update",
        serviceClass = LogisticsThirdChannelRefService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated LogisticsThirdChannelRefDTO.UpdateDTO dto) {
        logisticsThirdChannelRefService.update(dto);
        return success();
    }


    /**
     * 分页
     *
     * @param dto
     * @author zdy
     * @date 2025-07-30
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            menuCode = "tms:logisticsThirdChannelRef:paging",
            tableAlias = "ltcr"
    )
    @WebAdvanceQuery(handler = LogisticsThirdChannelRefQueryHandler.class)
    public ApiResult<PagingVO<LogisticsThirdChannelRefDTO.PagingVO>> paging(@RequestBody @Valid PagingDTO<LogisticsThirdChannelRefDTO.PagingParamDTO> dto) {
        PagingVO<LogisticsThirdChannelRefDTO.PagingVO> pagingVO = logisticsThirdChannelRefService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 查看详情
     * @author zdy
     * @date: 2025/7/31 9:10
     * @param id
     * @return ApiResult<LogisticsThirdChannelRefDTO.ViewDTO>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<LogisticsThirdChannelRefDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(logisticsThirdChannelRefService.view(id));
    }

    /**
     * 批量删除
     * @author zdy
     * @date: 2025/7/31 9:10
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:logisticsThirdChannelRef:delete",
            serviceClass = LogisticsThirdChannelRefService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "物流-第三方渠道关系表删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = logisticsThirdChannelRefService.delete(id);
            }catch (Exception e){
                log.error("物流-第三方渠道关系表删除失败",e);
                LogisticsThirdChannelRefEntity entity = logisticsThirdChannelRefService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "物流-第三方渠道关系表数据不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 导出
     * @author zdy
     * @date: 2025/7/31 9:10
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "物流-第三方渠道关系表导出")
    public ApiResult<?> export(@RequestBody @Validated LogisticsThirdChannelRefDTO.PagingParamDTO dto) {
        return success(logisticsThirdChannelRefService.export(dto));
    }

    /**
     * 下载第三方渠道关系导入模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载第三方渠道关系导入模板")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        logisticsThirdChannelRefService.downloadTemplate(response);
        return success();
    }
    /**
     * 导入
     * @author zdy
     * @date: 2025/7/31 9:10
     * @param excelFile
     * @return ApiResult
     */
    @PostMapping("/importExcel")
    @LogAction(value = LogActionEnum.IMPORT, desc = "物流-第三方渠道关系表导入")
    public ApiResult<?> importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean b = logisticsThirdChannelRefService.importExcel(excelFile, response);
        return b ? success("导入成功") : failure("导入失败");
    }
    /**
     * 启用/停用
     * @author ZDY
     * @date: 2025/8/1 14:35
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "启用停用:idList={idList},状态值={disabled}(true=禁用,false=启用)")
    @PostMapping("/updateStatus")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:logisticsThirdChannelRef:updateStatus",
            serviceClass = LogisticsThirdChannelRefService.class,
            keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> updateStatus(@RequestBody @Validated LogisticsThirdChannelRefDTO.DisabledParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = logisticsThirdChannelRefService.updateStatus(id,dto.getDisabled());
            }catch (Exception e){
                log.error("物流-第三方渠道关系表 停用/启用失败",e);
                LogisticsThirdChannelRefEntity entity = logisticsThirdChannelRefService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "物流-第三方渠道关系表不存在, 停用/启用失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getLogisticsSupplierName(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
