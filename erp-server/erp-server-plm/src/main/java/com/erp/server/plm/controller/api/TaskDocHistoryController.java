package com.erp.server.plm.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.DocHistoryDTO;
import com.erp.server.plm.service.TaskDocHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 任务详情
 *
 * @author Lambda
 * @since 2023-06-09
 */
@RestController
@RequestMapping("/docHistory")
public class TaskDocHistoryController extends BaseController {

    @Autowired
    private TaskDocHistoryService taskDocHistoryService;

    /**
     * 文档历史记录【PLM1.3】
     * @param finishDocsId
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<DocHistoryDTO.InfoDTO>> getDocHistory(@RequestParam("finishDocsId") String finishDocsId) {
        List<DocHistoryDTO.InfoDTO> list = taskDocHistoryService.historyList(finishDocsId);
        return success(list);
    }


}
