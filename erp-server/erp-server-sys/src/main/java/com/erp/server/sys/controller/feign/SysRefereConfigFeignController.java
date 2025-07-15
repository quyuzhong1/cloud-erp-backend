package com.erp.server.sys.controller.feign;

import com.common.business.annotation.DataIdempotent;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.constant.UserStateConstants;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.UserRequestPermissionsDTO;
import com.common.business.dto.UserSelectDto;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.RedisService;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.SysRefererConfigEntity;
import com.erp.model.sys.entity.SysRoleMenuEntity;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.model.sys.entity.SysUserWechatEntity;
import com.erp.model.sys.vo.SupplierUserVO;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.server.sys.constant.SysConstant;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeService;
import com.erp.server.sys.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Classname SysRefereConfigFeignController
 * @Date 2025-05-14
 * @Created by jack
 */
@RestController
@RequestMapping("feign/sysRefererConfig")
public class SysRefereConfigFeignController extends BaseController {

    @Resource
    private SysRefererConfigService sysRefererConfigService;

    @GetMapping("/getByReferer")
    public List<SysRefererConfigEntity> getByReferer(@RequestParam("referer") String referer) {
        return sysRefererConfigService.lambdaQuery().eq(SysRefererConfigEntity::getReferer, referer).list();
    }
}
