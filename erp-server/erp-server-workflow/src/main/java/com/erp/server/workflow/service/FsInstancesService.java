package com.erp.server.workflow.service;

import cn.hutool.json.JSONObject;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.workflow.dto.ApproveTaskDetailDTO;
import com.erp.model.workflow.entity.CfgThirdProcessEntity;

import java.util.List;
import java.util.Map;

/**
 * 飞书审批示例详情拉取接口服务
 * @author will
 * @date 2025/10/21 19:11
 */
public interface FsInstancesService {
    /**
     * 拉取新增数据
     * @author will
     * @date 2025/10/23 15:46
     * @param jsonObject
     * @return void
     */
    void handleAddInstance(JSONObject jsonObject);
    /**
     * 推送更新单据状态
     * @author will
     * @date 2025/10/23 15:46
     * @param jsonObject
     * @param sourcePlatform
     * @return void
     */
    void handleUpdateStatus(JSONObject jsonObject, String sourcePlatform);


    /**
     *
     * @author will
     * @date 2025/10/23 16:01
     * @param jsonObject
     * @param map
     * @param thirdProcessEntity
     * @param addDTOS
     * @return void
     */
    void addFreshGenerate(JSONObject jsonObject, Map<String, Object> map, CfgThirdProcessEntity thirdProcessEntity,
                          List<ApproveTaskDetailDTO.AddDTO> addDTOS);

    /**
     * 创建人
     * @author will
     * @date 2025/10/23 16:15
     * @param createUserId
     * @param addDTO
     * @return void
     */
    void addCreateUser (String createUserId, SupplierDTO.InsertDTO addDTO);
}
