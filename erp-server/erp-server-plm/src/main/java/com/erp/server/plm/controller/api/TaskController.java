package com.erp.server.plm.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.TaskDTO;
import com.erp.model.plm.dto.TaskPagingShowDTO;
import com.erp.server.plm.service.TaskService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 任务列表
 *
 * @author
 * @Classname TaskController

 * @Date 2023-06-20 19:46
 * @Created by yl
 */
@RestController
@RequestMapping("productTask")
public class TaskController extends BaseController {

    @Resource
    private TaskService taskService;


    /**
     * 可执行全部任务
     *
     * @return
     */
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id,charge_ids",
            menuCode = "plm:productTask:all:paging",
            tableAlias = "pt,tcd"
    )
    @PostMapping("/allExecutable/paging")
    public ApiResult<PagingVO<List<TaskPagingShowDTO>>> allExecutablePaging(@Validated @RequestBody PagingDTO<TaskDTO.TaskPagingParamDTO> searchParamDTO) {
        PagingVO<List<TaskPagingShowDTO>> pagingVO = taskService.allExecutablePaging(searchParamDTO);
        return success(pagingVO);
    }


}
