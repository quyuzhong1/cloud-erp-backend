package com.erp.server.msg.controller;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.enums.NoticeTypeEnum;
import com.erp.server.msg.config.MsgContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * @Classname: TestController
 * @Description: TODO
 * @CreateTime: 2023-04-20  19:21
 * @Author: zhangchunlin
 */
@RestController
@RequestMapping(value = "/test")
public class TestController extends BaseController {

    @Autowired
    private MsgContext msgContext;

    /**
     * 发送消息
     */
    @RequestMapping("/sendMsg")
    public ApiResult sendMsg() {
        NoticeMsgInfoDTO noticeMsgInfoDTO = new NoticeMsgInfoDTO();
        noticeMsgInfoDTO.setReceiverUserIds(new ArrayList<String>(Arrays.asList("1645710077245652993")));
        noticeMsgInfoDTO.setTitle("产品提醒: 张三 新建产品名称【iphone14】");
        noticeMsgInfoDTO.setContent("**产品名称: **iphone14\n**产品日期：**2023-04-20");
        noticeMsgInfoDTO.setUrgent(true);
        noticeMsgInfoDTO.setNoticeTypeEnum(NoticeTypeEnum.SCM_TASK);
        msgContext.routeSend(noticeMsgInfoDTO);
        return success();
    }


}