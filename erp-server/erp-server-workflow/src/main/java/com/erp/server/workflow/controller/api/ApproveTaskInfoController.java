package com.erp.server.workflow.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.workflow.dto.ApproveTaskInfoDTO;
import com.erp.model.workflow.entity.ApproveTaskInfoEntity;
import com.erp.server.workflow.query.ApproveTaskInfoQueryHandler;
import com.erp.server.workflow.service.ApproveTaskInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 三方生成查询
 *
 * @author will
 * @since 2025-05-27
 */
@Slf4j
@RestController
@LogSystemModule("三方生成查询")
@RequestMapping("/approveTaskInfo")
public class ApproveTaskInfoController extends BaseController {

    @Resource
    private ApproveTaskInfoService approveTaskInfoService;

    /**
     * 获取状态统计
     * @author will
     * @date 2025/5/12 16:10
     * @param dto
     * @return ApiResult<List<TabListDTO>>
     */
    @PostMapping("/tabList")
    public ApiResult<List<ApproveTaskInfoDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(approveTaskInfoService.tabList(dto));
    }

    /**
     * 列表查询
     * @author will
     * @date: 2025-05-12
     * @param dto
     * @return ApiResult<PagingVO<ProcessDelegateDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = ApproveTaskInfoQueryHandler.class)
    public ApiResult<PagingVO<ApproveTaskInfoDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<ApproveTaskInfoDTO.PagingParamDTO> dto) {
        return success(approveTaskInfoService.paging(dto));
    }


    /**
    * 修改
    * @author will
    * @date:  2025-05-27
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "三方生成查询修改")
    public ApiResult<?> update(@RequestBody @Validated ApproveTaskInfoDTO.UpdateDTO dto) {
        approveTaskInfoService.update(dto);
        return success();
    }

    /**
     * 查看详情
     * @author will
     * @date 2025/5/27 11:02
     * @param id
     * @return ApiResult<ViewDTO>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<ApproveTaskInfoDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(approveTaskInfoService.view(id));
    }


    /**
     * 导出
     * @author will
     * @date 2025/5/27 11:04
     * @param dto
     * @return ApiResult<Boolean>
     */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "第三方生成查询导出Excel数据")
    @WebAdvanceQuery(handler = ApproveTaskInfoQueryHandler.class)
    public ApiResult<Boolean> exportList(@RequestBody @Validated ApproveTaskInfoDTO.PagingParamDTO dto) {
        approveTaskInfoService.exportList(dto);
        return success();
    }

    /**
     * 重新生成
     * @author will
     * @date 2025/5/27 16:10
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/afreshGenerate")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "第三方生成查询重新生成")
    public ApiResult<List<BatchResultDTO>> afreshGenerate(@RequestBody @Validated ApproveTaskInfoDTO.AfreshGenerateTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = approveTaskInfoService.afreshGenerate(id);
            }catch (Exception e){
                log.error("三方生成查询重新生成失败",e);
                ApproveTaskInfoEntity entity = approveTaskInfoService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "三方生成查询数据不存在, 重新生成失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getBussinessCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
