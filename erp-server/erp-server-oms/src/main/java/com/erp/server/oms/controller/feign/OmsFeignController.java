package com.erp.server.oms.controller.feign;

import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.server.oms.service.CustomerInfoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OMS 服务 Feign 控制器
 * @author wuhaotian
 * @since 2025-09-24
 */
@RestController
@RequestMapping("/feign")
public class OmsFeignController extends BaseController {

    @Resource
    private CustomerInfoService customerInfoService;

    /**
     * 启用客户列表
     */
    @PostMapping("/customer/listEnable")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> customerListEnable(@RequestBody PermissionsDTO dto) {
        List<CustomerDTO.InfoDTO> customerList = customerInfoService.listEnable(dto.getPermissionSql());
        return success(customerList.stream()
                .map(customer -> new BaseDropDownDTO.CommonDTO(customer.getId(), customer.getName()))
                .collect(Collectors.toList()));
    }
}
