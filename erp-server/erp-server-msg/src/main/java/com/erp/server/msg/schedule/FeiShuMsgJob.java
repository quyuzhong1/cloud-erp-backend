package com.erp.server.msg.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.server.msg.config.MsgContext;
import com.erp.server.msg.constant.MongoTableConstant;
import com.mongodb.client.result.UpdateResult;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SKU自动匹配JOB
 */
@Component
@Slf4j
@EnableScheduling
public class FeiShuMsgJob {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private MsgContext msgContext;
    /**
     * 发送飞书预警消息
     */
    @XxlJob("sendFeiShuWarnMsg")
    public void sendFeiShuWarnMsg() {
        XxlJobHelper.log("发送飞书预警消息:start");
        //获取mongo中未发送的飞书消息
        Query query = new Query();
        query.addCriteria(
                Criteria.where("isSend").in(MathUtil.ZERO)
        );
        List<WarnMsgInfoDTO> list = mongoTemplate.find(query, WarnMsgInfoDTO.class, MongoTableConstant.FEISHU_WARN_MSG);
        XxlJobHelper.log("待发送飞书预警消息数量:{}", list.size());
        //对全量数据进行分区
        if (list.size() > 100){
            List<List<WarnMsgInfoDTO>> partition = ListUtil.partition(list, MathUtil.NUMBER_100);
            partition.forEach(this::sendWarnMsg);
        }else {
            sendWarnMsg(list);
        }
        XxlJobHelper.log("发送飞书预警消息:end");
    }

    private void sendWarnMsg(List<WarnMsgInfoDTO> list){
        XxlJobHelper.log("批量发送飞书预警消息数量:{}", list.size());
        if (CollectionUtil.isEmpty(list)){
            return;
        }
        //批量发送异常提醒，并更新mongo数据记录状态
        list.forEach(msgContext::routeSendWarnMsg);
        List<String> msgIds = list.stream().map(WarnMsgInfoDTO::getMsgId).distinct().collect(Collectors.toList());
        if (CollectionUtil.isEmpty(msgIds)){
            return;
        }
        //mongodb更新状态
        Query query = new Query();
        query.addCriteria(Criteria.where("msgId").in(msgIds));
//        Update update = new Update();
//         设置需要更新的字段和值
//        update.set("isSend", MathUtil.TWO);
//        UpdateResult result = mongoTemplate.updateMulti(query,update,WarnMsgInfoDTO.class,MongoTableConstant.FEISHU_WARN_MSG);
        mongoTemplate.findAndRemove(query,WarnMsgInfoDTO.class,MongoTableConstant.FEISHU_WARN_MSG);
//        XxlJobHelper.log("批量发送飞书预警消息结果:{}", result.getMatchedCount());
        try {
            XxlJobHelper.log("发送飞书预警消息休眠 start:{}", System.currentTimeMillis());
            //增加休眠，避免飞书请求限制
            Thread.sleep(60000);
            XxlJobHelper.log("发送飞书预警消息休眠 end:{}", System.currentTimeMillis());
        } catch (InterruptedException e) {
            log.error("FeiShuMsgJob.sendWarnMsg：休眠异常");
        }
        XxlJobHelper.log("批量发送飞书预警消息完成:{}", list.size());
    }
}
