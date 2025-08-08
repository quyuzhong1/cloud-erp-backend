package com.erp.server.file.controller.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.file.context.FileTaskContext;
import com.erp.server.file.dto.FileTaskDTO;
import com.erp.server.file.dto.FileTaskParamsDTO;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.vo.FileTaskVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 下载中心
 */
@RestController
@RequestMapping("/task")
public class FileTaskController extends BaseController {

    @Resource
    private FileTaskContext fileTaskContext;

    /**
     *  创建下载任务
     */
    @PostMapping
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody FileTaskDTO fileTaskDTO){
        String id = fileTaskContext.addExport(fileTaskDTO);
        return success(new BaseResultDTO.AddDTO(id, ""));
    }

    /**
     *  下载任务详情
     */
    @GetMapping
    public ApiResult<FileTask> view(@RequestParam String id){
        FileTask fileTask = fileTaskContext.view(id);
        return success(fileTask);
    }

    /**
     *  删除下载任务
     */
    @PostMapping("/delete")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        for (String id : dto.getIds()) {
            try {
                fileTaskContext.delete(id);
                resultDTOS.add(BatchResultDTO.success(id,null, "删除成功"));
            }catch (Exception e){
                resultDTOS.add(BatchResultDTO.fail(id , null, e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     *  重新开始下载任务
     */
    @PostMapping("/restart")
    public ApiResult<List<BatchResultDTO>> restart(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        for (String id : dto.getIds()) {
            try {
                fileTaskContext.restart(id);
                resultDTOS.add(BatchResultDTO.success(id,null, "重新导入"));
            }catch (Exception e){
                resultDTOS.add(BatchResultDTO.fail(id , null, e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 分页查询下载任务
     * @param dto 参数
     */
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "file:task:paging",
            tableAlias = "ft"
    )
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<FileTaskVO>> paging(@RequestBody @Validated PagingDTO<FileTaskParamsDTO> dto) {
        IPage<FileTaskVO> fileTasks = fileTaskContext.paging(dto);
        return ApiResult.success(new PagingVO<>(fileTasks));
    }

}
