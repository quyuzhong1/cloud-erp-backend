package com.erp.server.oms.controller.api;


import com.common.business.validator.ValidList;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.CustomerGroupDTO;
import com.erp.server.oms.service.CustomerB2cGroupService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * B2C销售管理-B2C客户管理
 *
 * @author will
 * @since 2023-05-10
 */
@RestController
@RequestMapping("/customerB2c/group")
public class CustomerB2cGroupController extends BaseController {

    @Resource
    private CustomerB2cGroupService customerB2cGroupService;


    /**
     * 批量保存客户分组
     *
     * @param groupList
     * @return
     */
    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody @Valid ValidList<CustomerGroupDTO.AddOrUpdateDTO> groupList) {
        Boolean result = customerB2cGroupService.saveOrUpdateBatchGroup(groupList);
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
        List<CustomerGroupDTO.ListDTO> list = customerB2cGroupService.listGroup();
        return success(list);

    }

}
