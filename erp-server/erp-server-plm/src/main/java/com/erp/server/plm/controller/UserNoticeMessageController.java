package com.erp.server.plm.controller;

import com.common.core.constant.ThirdConstants;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;

import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.dto.base.StateDTO;
import com.erp.common.dto.base.UpdateStateDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.modules.sys.dto.FindThirdUserDTO;
import com.erp.common.modules.sys.dto.FindUserByThirdDTO;
import com.erp.model.plm.dto.UpdateUserNoticeStateDTO;
import com.erp.model.plm.dto.UserNoticeNodeDTO;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.NoticeMessageService;
import com.erp.server.plm.service.UserCancelNoticeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 飞书应用-个人通知管理
 *
 * @Classname UserNoticeMessageController
 * @Description TODO
 * @Date 2022-11-10 14:35
 * @Created by yl
 */
@RestController
@RequestMapping("plm/app/user/notice/")
public class UserNoticeMessageController extends BaseController {

    @Autowired
    private NoticeMessageService noticeMessageService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private UserCancelNoticeService userCancelNoticeService;

    @Resource
    private FsService fsService;

    /**
     * 获取消息通知列表
     *
     * @param
     * @return com.erp.common.dto.base.ApiResult<java.util.List < com.erp.model.plm.dto.UserNoticeNodeDTO>>
     * @author yl
     * @date 2022-11-10 15:54
     */
    @GetMapping("/list")
    public ApiResult<List<UserNoticeNodeDTO>> userNoticeNodeList(@Validated @RequestBody BaseIdDTO dto) {
        String fsUnionId = dto.getId();
        //根据飞书的unionId 获取对应用户信息
        String userId = commonService.getUidByUnionId(ThirdConstants.FS_PLATFORM, fsUnionId);
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
    public ApiResult updateState(@Validated @RequestBody UpdateUserNoticeStateDTO dto) {
        String fsUnionId = dto.getUnionId();
        //根据飞书的unionId 获取对应用户信息
        String userId = commonService.getUidByUnionId(ThirdConstants.FS_PLATFORM, fsUnionId);
        Boolean flag = userCancelNoticeService.updateState(userId, dto.getId(), dto.getState());
        return flag == true ? success() : failure();
    }


    /**
     * 获取飞书的客户端id
     *
     * @param
     * @return java.lang.String
     * @author yl
     * @date 2022-11-11 16:38
     */
    @GetMapping("/getFsClientId")
    public ApiResult<String> getFsClientId() {
        String fsClientId = fsService.getFsClientId();
        return success(fsClientId);
    }


    /**
     * 检查是否有绑定plm 系统
     *
     * @param
     * @return com.erp.common.dto.base.ApiResult
     * @author yl
     * @date 2022-11-14 11:10
     */
    @PostMapping("/checkBinding")
    public ApiResult checkBinding(@RequestBody @Validated FindUserByThirdDTO dto) {
        String useId = commonService.getUidByUnionId(dto.getThirdPartyType(), dto.getThirdPartyUnionId());
        if (StringUtils.isBlank(useId)) {
            return failure(ApiError.ERROR_95056,null);
        }
        return success();
    }

    /**
     * 获取飞书的用户信息 根据code
     *
     * @param
     * @return java.lang.String
     * @author yl
     * @date 2022-11-11 16:38
     */
    @GetMapping("/getFsUserInfo")
    public ApiResult getFsUserInfo(String code) {
        Map<String, Object> fsMap = fsService.getFsUserByCode(code);
        if (fsMap != null) {
            if (fsMap.containsKey("code")) {
                int state = (int) fsMap.get("code");
                if (state == 0) {
                    return success(fsMap.get("data"));
                }
                return new ApiResult(state, fsMap.get("msg").toString());
            }
        } else {
            return failure();
        }
        return failure();
    }


}
