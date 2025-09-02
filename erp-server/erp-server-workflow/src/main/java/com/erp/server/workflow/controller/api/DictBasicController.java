package com.erp.server.workflow.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.workflow.dto.DictBasicDTO;
import com.erp.server.workflow.service.DictBasicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * @param type moduleStatus，superiorOption 上级选项，approveOption 审批人选项，processCondition 流程网关条件 approverEmpty 审批人为空时 multiPersonReview 多人处理方式 timeoutHandling 超时处理方式 reviewSetting 审批设置 processStatus 流程状态 processType 流程类型 operateType 生成/更新配置
     * @param remark 备注  purchase_order 采购订单 warehouse_receive  仓库收货单 来自关联单据类型 流程网关条件获取需要填入此参数进行分类过滤
     * @return
     */
    @GetMapping("/drop/down")
    public ApiResult<List<DictBasicDTO.DropDownDTO>> dictDropDown(@RequestParam(value = "type")String type, @RequestParam(value = "remark", required = false) String remark) {
        List<DictBasicDTO.DropDownDTO> result =  dictBasicService.listByType(type, remark);
        return success(result);
    }

    // 1. form_detail_url
    @GetMapping("/form_detail_url")
    public ResponseEntity<Map<String, Object>> getFormDetail() {
        Map<String, Object> response = new HashMap<>();

        List<Map<String, Object>> schema = Arrays.asList(
                new HashMap<String, Object>() {{
                    put("id", "widget1");
                    put("type", "input");
                    put("label", "申请理由");
                    put("required", true);
                    put("default_value", "请输入申请理由");
                }},
                new HashMap<String, Object>() {{
                    put("id", "widget2");
                    put("type", "date");
                    put("label", "申请日期");
                    put("required", true);
                    put("default_value", "2023-01-01");
                }}
        );

        response.put("schema", schema);
        return ResponseEntity.ok(response);
    }

    // 2. action_definition_url
    @GetMapping("/action_definition_url")
    public ResponseEntity<Map<String, Object>> getActionDefinition() {
        Map<String, Object> response = new HashMap<>();

        List<Map<String, Object>> actions = Arrays.asList(
                new HashMap<String, Object>() {{
                    put("action_type", "APPROVE");
                    put("action_name", "同意");
                    put("is_need_reason", true);
                    put("is_reason_required", true);
                    put("is_need_attachment", false);
                }},
                new HashMap<String, Object>() {{
                    put("action_type", "REJECT");
                    put("action_name", "拒绝");
                    put("is_need_reason", true);
                    put("is_reason_required", false);
                    put("is_need_attachment", false);
                }}
        );

        response.put("actions", actions);
        return ResponseEntity.ok(response);
    }

    // 3. approval_node_url
    @GetMapping("/approval_node_url")
    public ResponseEntity<Map<String, Object>> getApprovalNodes() {
        Map<String, Object> response = new HashMap<>();

        List<Map<String, Object>> nodes = Arrays.asList(
                new HashMap<String, Object>() {{
                    put("node_id", "node1");
                    put("node_name", "直属上级审批");
                    put("status", "APPROVED");
                    put("approver", new HashMap<String, Object>() {{
                        put("user_id", "ou_123456789");
                        put("user_name", "张三");
                    }});
                    put("update_time", "1688022600000");
                }},
                new HashMap<String, Object>() {{
                    put("node_id", "node2");
                    put("node_name", "财务审批");
                    put("status", "PENDING");
                    put("approver", new HashMap<String, Object>() {{
                        put("user_id", "ou_987654321");
                        put("user_name", "李四");
                    }});
                    put("update_time", "1688022700000");
                }}
        );

        response.put("nodes", nodes);
        return ResponseEntity.ok(response);
    }

    // 4. action_callback_url
    @GetMapping("/action_callback_url")
    public ResponseEntity<Map<String, Object>> getActionCallback() {
        Map<String, Object> response = new HashMap<>();

        response.put("action_type", "APPROVE");
        response.put("action_context", "12345");
        response.put("user_id", "ou_123456789");
        response.put("approval_code", "81D31358-93AF-92D6-7425-01A5D67C4E71");
        response.put("instance_id", "24492654");
        response.put("task_id", "112534");
        response.put("reason", "同意审批");

        List<Map<String, Object>> attachments = Arrays.asList(
                new HashMap<String, Object>() {{
                    put("file_type", "IMAGE");
                    put("file_name", "approval.png");
                    put("file_size", 12345);
                    put("file_url", "https://example.com/approval.png");
                }}
        );

        response.put("attachments", attachments);
        response.put("update_time", "1688022800000");

        return ResponseEntity.ok(response);
    }
}