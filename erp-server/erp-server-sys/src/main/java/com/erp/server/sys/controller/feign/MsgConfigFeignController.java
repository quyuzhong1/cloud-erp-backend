package com.erp.server.sys.controller.feign;

import com.common.core.controller.BaseController;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.sys.entity.MsgConfig;
import com.erp.model.sys.vo.MsgConfigDTO;
import com.erp.server.sys.service.MsgConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Classname: MsgConfigFeignController
 * @Description: TODO
 * @CreateTime: 2023-04-21  14:19
 * @Author: zhangchunlin
 */
@RestController
@RequestMapping("feign/msgConfig")
public class MsgConfigFeignController extends BaseController {

    @Autowired
    private MsgConfigService msgConfigService;

    /**
     * 根据主键获取消息配置信息
     * @param id
     * @return
     */
    @GetMapping("/getById")
    public MsgConfigDTO getMsgConfigById(@RequestParam(value = "id")String id) {
        MsgConfig msgConfig = msgConfigService.getById(id);
        return BeanMapperUtils.map(MsgConfigDTO.class, msgConfig);
    }

}