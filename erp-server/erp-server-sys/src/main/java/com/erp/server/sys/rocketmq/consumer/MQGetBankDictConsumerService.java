package com.erp.server.sys.rocketmq.consumer;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.sys.entity.DictBankEntity;
import com.erp.server.sys.service.DictBankService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author: wuht
 * @CreateTime: 2025-07-31
 * @Description: 消费获取收款银行
 * @Version: 1.0
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_KINGDEE_BANK_TO_SYS_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_KINGDEE_BANK_TO_SYS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_KINGDEE_BANK_TO_SYS_GROUP
)
public class MQGetBankDictConsumerService extends AbstractNewPlatformConsumerHandler {

    @Resource
    private DictBankService dictBankService;

    @Override
    public String getBizName() {
        return "金蝶收款银行";
    }

    @Override
    public void handle(String data) {
        try {
            log.info("接收到金蝶银行数据: {}", data);
            
            // 解析JSON数据
            DictBankEntity dictBankEntity = JSON.parseObject(data, DictBankEntity.class);
            if (dictBankEntity == null) {
                log.error("解析银行数据失败: {}", data);
                return;
            }
            
            // 处理银行数据
            processBankData(dictBankEntity);
            
        } catch (Exception e) {
            log.error("处理金蝶银行数据异常: {}", data, e);
        }
    }
    
    /**
     * 处理银行数据
     * 逻辑：如果查一遍当前数据的银行编号是否存在数据库
     * 不存在就查一遍名字，名字存在就更新，不存在就新增
     * 如果银行编号存在就更新名称
     */
    private void processBankData(DictBankEntity dictBankEntity) {
        try {
            // 1. 先根据银行编号查询
            LambdaQueryWrapper<DictBankEntity> bankNumberQuery = new LambdaQueryWrapper<>();
            bankNumberQuery.eq(DictBankEntity::getBankNo, dictBankEntity.getBankNo());
            DictBankEntity existingByNumber = dictBankService.getOne(bankNumberQuery);
            
            if (existingByNumber != null) {
                // 银行编号存在，更新银行名称
                log.info("银行编号 {} 已存在，更新银行名称: {} -> {}", 
                    dictBankEntity.getBankNo(), 
                    existingByNumber.getName(), 
                    dictBankEntity.getName());
                
                existingByNumber.setName(dictBankEntity.getName());
                existingByNumber.setServicesPhone(dictBankEntity.getServicesPhone());
                existingByNumber.setHeadquarterAddress(dictBankEntity.getHeadquarterAddress());
                existingByNumber.setDisabled(dictBankEntity.getDisabled());
                existingByNumber.setUpdateTime(dictBankEntity.getUpdateTime());
                
                dictBankService.updateById(existingByNumber);
                log.info("银行编号 {} 更新成功", dictBankEntity.getBankNo());
                
            } else {
                // 银行编号不存在，根据银行名称查询
                LambdaQueryWrapper<DictBankEntity> bankNameQuery = new LambdaQueryWrapper<>();
                bankNameQuery.eq(DictBankEntity::getName, dictBankEntity.getName());
                DictBankEntity existingByName = dictBankService.getOne(bankNameQuery);
                
                if (existingByName != null) {
                    // 银行名称存在，更新银行编号
                    log.info("银行名称 {} 已存在，更新银行编号: {} -> {}", 
                        dictBankEntity.getName(), 
                        existingByName.getBankNo(), 
                        dictBankEntity.getBankNo());
                    
                    existingByName.setBankNo(dictBankEntity.getBankNo());
                    existingByName.setServicesPhone(dictBankEntity.getServicesPhone());
                    existingByName.setHeadquarterAddress(dictBankEntity.getHeadquarterAddress());
                    existingByName.setDisabled(dictBankEntity.getDisabled());
                    existingByName.setUpdateTime(dictBankEntity.getUpdateTime());
                    
                    dictBankService.updateById(existingByName);
                    log.info("银行名称 {} 更新成功", dictBankEntity.getName());
                    
                } else {
                    // 银行编号和名称都不存在，新增
                    log.info("银行编号 {} 和名称 {} 都不存在，新增银行记录", 
                        dictBankEntity.getBankNo(), 
                        dictBankEntity.getName());
                    
                    dictBankService.save(dictBankEntity);
                    log.info("银行记录新增成功: {}", dictBankEntity.getName());
                }
            }
            
        } catch (Exception e) {
            log.error("处理银行数据异常: {}", dictBankEntity, e);
            throw e;
        }
    }
}
