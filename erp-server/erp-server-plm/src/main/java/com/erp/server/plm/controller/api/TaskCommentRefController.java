package com.erp.server.plm.controller.api;

import com.common.core.controller.BaseController;
import com.erp.server.plm.service.TaskCommentRefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * <p>
 * 任务评论关联表
 * </p>
 *
 * @author Lambda
 * @since 2023-06-09
 */
@RestController
@RequestMapping("/taskCommentRef")
public class TaskCommentRefController extends BaseController {

    @Autowired
    private TaskCommentRefService taskCommentRefService;



}
