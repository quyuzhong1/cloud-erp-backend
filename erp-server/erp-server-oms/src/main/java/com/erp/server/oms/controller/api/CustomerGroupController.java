package com.erp.server.oms.controller.api;


import com.common.business.validator.ValidList;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.CustomerGroupDTO;
import com.erp.server.oms.service.CustomerGroupService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 销售管理-客户管理
 *
 * @author lambda
 * @since 2023-05-10
 */
@RestController
@RequestMapping("/customer/group")
public class CustomerGroupController extends BaseController {

    @Resource
    private CustomerGroupService customerGroupService;


    /**
     * 批量保存客户分组
     *
     * @param groupList
     * @return
     */
    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody @Valid ValidList<CustomerGroupDTO.AddOrUpdateDTO> groupList) {
        Boolean result = customerGroupService.saveOrUpdateBatchGroup(groupList);
        return result == true ? success() : failure();
    }

    /**
     * 客户分组列表
     *
     * @param
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<CustomerGroupDTO.ListDTO>> list() {
        List<CustomerGroupDTO.ListDTO> list = customerGroupService.listCustomer();
        return success(list);

    }

}
