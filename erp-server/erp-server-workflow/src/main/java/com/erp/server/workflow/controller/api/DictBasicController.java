package com.erp.server.workflow.controller.api;


import com.common.core.controller.vo.ApiResult;
import com.erp.model.workflow.dto.DictBasicDTO;
import com.erp.model.workflow.entity.DictBasicEntity;
import com.erp.server.workflow.service.DictBasicService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 字典服务实现类
 *
 * @author Cloud
 * @since 2023-04-21
 */
@RestController
@RequestMapping("/dict/basic")
public class DictBasicController extends BaseController {

    @Resource
    private DictBasicService dictBasicService;

    /**
     * 字典通用下拉列表
     * @param type moduleStatus，superiorOption 上级选项，approveOption 审批人选项，processCondition 流程网关条件 approverEmpty 审批人为空时 multiPersonReview 多人处理方式 timeoutHandling 超时处理方式 reviewSetting 审批设置
     * @param remark 备注  purchase_order 采购订单 warehouse_receive  仓库收货单 来自关联单据类型 流程网关条件获取需要填入此参数进行分类过滤
     * @return
     */
    @GetMapping("/drop/down")
    public ApiResult<List<DictBasicDTO.DropDownDTO>> dictDropDown(@RequestParam(value = "type")String type, @RequestParam(value = "remark", required = false) String remark) {
        List<DictBasicDTO.DropDownDTO> result =  dictBasicService.listByType(type, remark);
        return success(result);
    }

}
