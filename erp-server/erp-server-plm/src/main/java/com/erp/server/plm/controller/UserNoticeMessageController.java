package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;

import com.erp.common.dto.base.StateDTO;
import com.erp.common.dto.base.UpdateStateDTO;
import com.erp.model.plm.dto.UserNoticeNodeDTO;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.NoticeMessageService;
import com.erp.server.plm.service.UserCancelNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 飞书应用-个人通知管理
 *
 * @Classname UserNoticeMessageController
 * @Description TODO
 * @Date 2022-11-10 14:35
 * @Created by yl
 */
@RestController
@RequestMapping("plm/user/notice/message")
public class UserNoticeMessageController extends BaseController {

    @Autowired
    private NoticeMessageService noticeMessageService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private UserCancelNoticeService userCancelNoticeService;

    /**
     * 获取消息通知列表
     *
     * @param
     * @return com.erp.common.dto.base.ApiResult<java.util.List < com.erp.model.plm.dto.UserNoticeNodeDTO>>
     * @author yl
     * @date 2022-11-10 15:54
     */
    @GetMapping("/list")
    public ApiResult<List<UserNoticeNodeDTO>> userNoticeNodeList() {
        String userId = commonService.getUserInfo().getUid();
        List<UserNoticeNodeDTO> resultList = noticeMessageService.getUserNoticeNode(userId);
        return success(resultList);
    }


    /**
     * 更改状态
     *
     * @param
     * @return com.erp.common.dto.base.ApiResult<java.util.List < com.erp.model.plm.dto.UserNoticeNodeDTO>>
     * @author yl
     * @date 2022-11-10 15:55
     */
    @PostMapping("/updateState")
    public ApiResult updateState(@Validated @RequestBody UpdateStateDTO dto) {
        String userId = commonService.getUserInfo().getUid();
        Boolean flag = userCancelNoticeService.updateState(userId, dto.getId(), dto.getState());
        return flag == true ? success() : failure();
    }
}
