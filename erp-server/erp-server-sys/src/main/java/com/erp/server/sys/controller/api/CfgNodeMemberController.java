package com.erp.server.sys.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.CfgNodeMemberDTO;
import com.erp.server.sys.service.CfgNodeMemberService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 节点接收配置表 前端控制器
 * </p>
 *
 * @author lambda
 * @since 2023-04-26
 */
@RestController
@RequestMapping("/cfgNodeMember")
public class CfgNodeMemberController extends BaseController {

    @Resource
    private CfgNodeMemberService cfgNodeMemberService;

    /**
     * 添加或者修改
     *
     * @param list
     * @return
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated List<CfgNodeMemberDTO.AddOrUpdateDTO> list) {
        Boolean result = cfgNodeMemberService.addOrUpdate(list);
        return result ? success() : failure();
    }

}
