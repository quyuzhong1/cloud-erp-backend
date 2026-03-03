package com.erp.server.tms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.server.tms.query.TmsAsyncTaskRecordQueryHandler;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogSystemModule;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;
import com.erp.server.tms.service.TmsAsyncTaskRecordService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 异步任务记录
 *
 * @author jack
 * @since 2026-01-28
 */
@Slf4j
@RestController
@LogSystemModule("异步任务记录")
@RequestMapping("/tmsAsyncTaskRecord")
public class TmsAsyncTaskRecordController extends BaseController {

    @Resource
    private TmsAsyncTaskRecordService tmsAsyncTaskRecordService;

    /**
     * 获取状态统计
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsAsyncTaskRecord:paging",
            tableAlias = "tatr"
    )
    public ApiResult<List<TmsAsyncTaskRecordDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(tmsAsyncTaskRecordService.tabList(dto));
    }

    /**
     * 列表查询
     * @author jack
     * @date: 2026-02-26
     * @param dto
     * @return ApiResult<PagingVO<TmsAsyncTaskRecordDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsAsyncTaskRecord:paging",
            tableAlias = "tatr"
    )
    @WebAdvanceQuery(handler = TmsAsyncTaskRecordQueryHandler.class)
    public ApiResult<PagingVO<TmsAsyncTaskRecordDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<TmsAsyncTaskRecordDTO.PagingParamDTO> dto) {
        return success(tmsAsyncTaskRecordService.paging(dto));
    }

    /**
     * 导出
     * @author jack
     * @date: 2026-02-26
     * @param dto
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsAsyncTaskRecord:export",
            tableAlias = "tatr"
    )
    @WebAdvanceQuery(handler = TmsAsyncTaskRecordQueryHandler.class)
    public  ApiResult<Object> exportList(@RequestBody @Validated TmsAsyncTaskRecordDTO.PagingParamDTO dto, HttpServletResponse response) {
        tmsAsyncTaskRecordService.exportList(dto, response);
        return success();
    }


    /**
     * 列表查询
     * @author jack
     * @date: 2026-02-26
     * @param dto
     * @return ApiResult<PagingVO<TmsAsyncTaskRecordDTO.ListDTO>>
     */
    @PostMapping("/pagingError")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsAsyncTaskRecord:pagingError",
            tableAlias = "tatr"
    )
    public ApiResult<PagingVO<TmsAsyncTaskRecordDTO.DetailListDTO>> pagingError(@RequestBody @Validated PagingDTO<TmsAsyncTaskRecordDTO.PagingDetailParamDTO> dto) {
        return success(tmsAsyncTaskRecordService.pagingError(dto));
    }

    /**
     * 导出
     * @author jack
     * @date: 2026-02-26
     * @param dto
     */
    @PostMapping("/exportError")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsAsyncTaskRecord:exportError",
            tableAlias = "tatr"
    )
    public  ApiResult<Object> exportError(@RequestBody @Validated TmsAsyncTaskRecordDTO.PagingDetailParamDTO dto, HttpServletResponse response) {
        tmsAsyncTaskRecordService.exportError(dto, response);
        return success();
    }

    /**
     * 更新执行时间
     * @author jack
     * @date: 2026-02-26
     * @param dto
     */
    @PostMapping("/updateStartTime")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsAsyncTaskRecord:update",
            serviceClass = TmsAsyncTaskRecordService.class,
            keyIdName = "id")
    public ApiResult<?> updateStartTime(@RequestBody @Validated TmsAsyncTaskRecordDTO.UpdateDTO dto) {
        tmsAsyncTaskRecordService.updateStartTime(dto);
        return success();
    }

    /**
     * 批量重试
     * @author jack
     * @date: 2026-02-26
     * @param dto
     */
    @PostMapping("/batchRetry")
    public ApiResult<List<BatchResultDTO>> batchRetry(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<TmsAsyncTaskRecordEntity> list = tmsAsyncTaskRecordService.lambdaQuery().in(TmsAsyncTaskRecordEntity::getId, ids).list();
        Map<String, TmsAsyncTaskRecordEntity> idEntityMap = list.stream().collect(Collectors.toMap(TmsAsyncTaskRecordEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO batchResultDTO;
            try {
                batchResultDTO = tmsAsyncTaskRecordService.retry(id);
            }catch (Exception e){
                TmsAsyncTaskRecordEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    batchResultDTO = BatchResultDTO.fail(id, id, "异步任务记录不存在, 重试失败");
                    resultDTOS.add(batchResultDTO);
                    continue;
                }
                batchResultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(batchResultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 错误重试
     * @author jack
     * @date: 2026-02-26
     * @param dto
     */
    @PostMapping("/batchErrorRetry")
    public ApiResult<List<BatchResultDTO>> batchErrorRetry(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<TmsAsyncTaskRecordEntity> list = tmsAsyncTaskRecordService.lambdaQuery().in(TmsAsyncTaskRecordEntity::getId, ids).list();
        Map<String, TmsAsyncTaskRecordEntity> idEntityMap = list.stream().collect(Collectors.toMap(TmsAsyncTaskRecordEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO batchResultDTO;
            try {
                batchResultDTO = tmsAsyncTaskRecordService.errorRetry(id);
            }catch (Exception e){
                TmsAsyncTaskRecordEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    batchResultDTO = BatchResultDTO.fail(id, id, "异步任务记录不存在, 重试失败");
                    resultDTOS.add(batchResultDTO);
                    continue;
                }
                batchResultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(batchResultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 测试 - 创建所有的自动任务
     */
    @GetMapping("/genAutoTask")
    public void genAutoTask() {
        tmsAsyncTaskRecordService.genAutoTask();
    }



}
