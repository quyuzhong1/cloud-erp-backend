package com.erp.server.plm.controller;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.NoticeNodeDTO;
import com.erp.server.plm.service.NoticeNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 产品系统通用设置
 * @Classname NoticeNodeController
 * @Description TODO
 * @Date 2022-11-07 10:31
 * @Created by yl
 */
@RestController
@RequestMapping("plm/notice/node")
public class NoticeNodeController extends BaseController {

    @Autowired
    private NoticeNodeService noticeNodeService;

    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated NoticeNodeDTO dto) {
        boolean flag = noticeNodeService.addNoticeNode(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 通知管理-新增通知-获取节点列表
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<Map<String, Object>>> getList() {
        List<Map<String, Object>> list = noticeNodeService.getList();
        return success(list);
    }
}
