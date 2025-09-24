package com.erp.server.plm.controller.feign;

import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.rpc.sys.feign.SysUserFeign;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * PLM 服务 Feign 控制器
 * @author wuhaotian
 * @since 2025-09-24
 */
@RestController
@RequestMapping("/feign")
public class PlmFeignController extends BaseController {

    @Resource
    private SysUserFeign sysUserFeign;

    /**
     * 查找用户列表
     */
    @PostMapping("/common/findUserList")
    public ApiResult<List<FindUserDTO>> findUserList(@RequestBody BaseSearchDTO dto) {
        return sysUserFeign.userList(dto);
    }
}
