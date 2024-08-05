package com.erp.server.file.controller.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.DataAttributeEnum;
import com.erp.server.file.context.FileTaskContext;
import com.erp.server.file.dto.FileTaskDTO;
import com.erp.server.file.dto.FileTaskParamsDTO;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.vo.FileTaskVO;
import com.common.business.dto.base.PagingDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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
        String id = fileTaskContext.add(fileTaskDTO);
        return success(new BaseResultDTO.AddDTO(id, ""));
    }

    @GetMapping
    public ApiResult<FileTask> view(@RequestParam String id){
        FileTask fileTask = fileTaskContext.view(id);
        return success(fileTask);
    }

    /**
     *  删除下载任务
     */
    @DeleteMapping
    public ApiResult<String> delete(@RequestParam String id){
        fileTaskContext.delete(id);
        return success();
    }

    /**
     * 分页查询下载任务
     * @param dto 参数
     * @return {@link FileTaskVO} 下载任务
     */
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "file:task:paging"
    )
    @PostMapping("/paging")
    public ApiResult<IPage<FileTaskVO>> paging(@RequestBody PagingDTO<FileTaskParamsDTO> dto) {
        IPage<FileTaskVO> fileTasks = fileTaskContext.paging(dto);
        return ApiResult.success(fileTasks);
    }

}
