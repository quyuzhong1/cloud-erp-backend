package com.erp.server.plm.controller;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.entity.DocsChangeRecordEntity;
import com.erp.server.plm.service.DocsChangeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 产品开发管理
 *
 * @Classname DocsChangeRecordController
 * @Description TODO
 * @Date 2022-10-14 12:15
 * @Created by yl
 */

@RestController
@RequestMapping("docsChange")
public class DocsChangeRecordController extends BaseController {

    @Autowired
    private DocsChangeRecordService docsChangeRecordService;

    /**
     * 项目任务-任务详情-变更记录
     * @param taskId
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<DocsChangeRecordEntity>> list(String taskId) {
        List<DocsChangeRecordEntity> list = docsChangeRecordService.listByTaskId(taskId);
        return success(list);
    }


}
