package com.erp.server.sys.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.sys.vo.MsgChannelConfigDTO;
import com.erp.server.sys.service.MsgChannelConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Classname: MsgConfigFeignController
 * @Description: TODO
 * @CreateTime: 2023-04-21  14:19
 * @Author: zhangchunlin
 */
@RestController
@RequestMapping("feign/msgChannelConfig")
public class MsgChannelConfigFeignController extends BaseController {

    @Autowired
    private MsgChannelConfigService msgChannelConfigService;

    /**
     * 根据消息配置id获取消息渠道配置信息
     * @param msgConfigId
     * @return
     */
    @GetMapping("/findByMsgConfigId")
    public List<MsgChannelConfigDTO> findByMsgConfigId(@RequestParam(value = "msgConfigId")String msgConfigId) {
        return msgChannelConfigService.findByMsgConfigId(msgConfigId);
    }

}