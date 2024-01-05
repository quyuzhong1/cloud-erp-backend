package com.erp.server.oms.rocketmq.producer;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
@Slf4j
public class SyncSoB2cToDmpProducer {
    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;

    /**
     * 推送B2c订单到dmp
     * @Author Luo_WG
     * @Date 2024/1/3 18:48
     * @param viewDTO
     * @return void
     **/
    public void syncOrderToDmp(SoB2cDTO.ViewDTO viewDTO) {
        //判断是否需要推送记录
        if (!dmpTaskFeign.needPushMQ(LocalDateTime.now())){
            return;
        }
        //添加推送任务
        DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
        taskFeignDTO.setSourceId(viewDTO.getId());
        taskFeignDTO.setSourceCode(viewDTO.getCode());
        taskFeignDTO.setSourceType(SourceTypeEnum.CUSTOMER_INFO.getCode());
        taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_SO_B2C_ORDER_TO_DMP_TOPIC);
        taskFeignDTO.setMqTag(RocketMqTagEnum.SO_B2C_TO_DMP_TAG.getName());
        taskFeignDTO.setMqData(JSONUtil.toJsonStr(viewDTO));
        taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        taskFeignDTO.setTargetPlatformName(PlatformEnum.ERP_DMP.getDesc());
        taskFeignDTO.setSyncOperate(viewDTO.getApproveStatus().getCode());
        dmpMqFeign.sendMqAndSaveTask(taskFeignDTO);
    }
}
