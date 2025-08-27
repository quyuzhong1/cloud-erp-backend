package com.erp.rpc.oms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.DmpSyncMqDTO;
import com.erp.model.oms.dto.SoMultiChannelDTO;
import com.erp.model.workflow.dto.WorkOptionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "erp-oms", contextId = "omsTaskFeign",configuration = {FeignErrorDecoder.class})
public interface OmsTaskFeign {
    /**
     * 更新业务单据状态
     */
    @PostMapping("feign/syncKingdee/updateBusinessSyncKingdeeStatus")
    void updateBusinessSyncKingdeeStatus(@RequestBody Map<String, Object> params);

    /**
     * 根据入参查询单据数量
     * @Author Luo_WG
     * @Date 2023/4/21 15:34
     **/
    @PostMapping("feign/omsWorkflow/getTableNum")
    List<WorkOptionDTO.MyWorkOptionDTO> getTableNum(@RequestBody List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionDTOList);

    /**
     * @description: 查询数据发送同步任务
     * @author Will
     * @date: 2023/10/30 11:41
     * @param syncParamDTO
     */
    @PostMapping("/feign/omsSyncTask/findDataSendSyncTask")
    void findDataSendSyncTask(@RequestBody DmpSyncMqDTO.SyncParamDTO syncParamDTO);

    /**
     * 更新多渠道订单创建状态
     * @param createResultDTO
     */
    @PostMapping("/feign/soMultiChannel/updateSoMultiChannel")
    void updateSoMultiChannel(@RequestBody SoMultiChannelDTO.CreateResultDTO createResultDTO);
}
