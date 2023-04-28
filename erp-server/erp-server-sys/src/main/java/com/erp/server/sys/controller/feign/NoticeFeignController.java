package com.erp.server.sys.controller.feign;

import com.erp.model.sys.dto.NoticeReceiverDTO;
import com.erp.server.sys.service.NoticeReceiverService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lambda
 * @Classname NoticeFeignController
 * @Description TODO
 * @Date 2023-04-28 11:08
 * @Created by yl
 */

@RestController
@RequestMapping("feign/notice")
public class NoticeFeignController {

    @Resource
    private NoticeReceiverService noticeReceiverService;



    @PostMapping("/listNoticeReceiver")
    public List<NoticeReceiverDTO.InfoDTO> listNoticeReceiver(@RequestBody String nodeKey) {
        return noticeReceiverService.listNoticeReceiver(nodeKey);
    }

    /**
     * 获取到通知的人员
     * @param nodeKey
     * @return
     */
    @PostMapping("/listNoticeUser")
    public List<String> listNoticeUser(@RequestBody String nodeKey) {
        return noticeReceiverService.listNoticeUser(nodeKey);
    }


}
