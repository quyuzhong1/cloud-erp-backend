package com.erp.server.file.controller.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.erp.server.file.context.FileTaskContext;
import com.erp.server.file.dto.FileTaskDTO;
import com.erp.server.file.dto.FileTaskParamsDTO;
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
    public ApiResult<String> add(@RequestBody FileTaskDTO fileTaskDTO){
        fileTaskContext.add(fileTaskDTO);
        return success();
    }

    /**
     *  删除下载任务
     */
    @DeleteMapping("/{id}")
    public ApiResult<String> delete(@PathVariable String id){
        fileTaskContext.delete(id);
        return success();
    }

    /**
     * 分页查询下载任务
     * @param dto 参数
     * @return {@link FileTaskVO} 下载任务
     */
    @PostMapping("/paging")
    public ApiResult<IPage<FileTaskVO>> paging(@RequestBody PagingDTO<FileTaskParamsDTO> dto) {
        IPage<FileTaskVO> fileTasks = fileTaskContext.paging(dto);
        return ApiResult.success(fileTasks);
    }

}
