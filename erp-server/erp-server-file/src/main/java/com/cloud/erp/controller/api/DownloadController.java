package com.cloud.erp.controller.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloud.erp.context.FileTaskContext;
import com.cloud.erp.dto.FileTaskDTO;
import com.cloud.erp.dto.FileTaskParamsDTO;
import com.cloud.erp.vo.FileTaskVO;
import com.common.business.dto.base.PagingDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/download")
public class DownloadController extends BaseController {

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

    @PostMapping("/paging")
    public ApiResult<IPage<FileTaskVO>> paging(@RequestBody PagingDTO<FileTaskParamsDTO> dto) {
        IPage<FileTaskVO> fileTasks = fileTaskContext.paging(dto);
        return ApiResult.success(fileTasks);
    }

}
