package com.erp.server.sys.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.sys.dto.SysLogMqDTO;
import com.erp.model.sys.dto.SysLogRecordDTO;
import com.erp.model.sys.entity.SysLogRecordEntity;
import com.erp.server.sys.mapper.LogRecordMapper;
import com.erp.server.sys.service.SysLogRecordService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 操作日志 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-08-25
 */
@Slf4j
@Service
public class SysLogRecordServiceImpl extends SuperServiceImpl<LogRecordMapper, SysLogRecordEntity> implements SysLogRecordService {

    @Resource
    private MQProducerService mQProducerService;

    @Override
    public void mqBatchSend(List<SysLogRecordDTO.AddDTO> listDto) {
        LoginUser loginUser = UserContext.getNonLoginUser();
        String userId = loginUser.getUid();
        String userName = loginUser.getUserName();
        List<SysLogMqDTO> listMqDto = listDto.stream().map(dto -> SysLogMqDTO.init(userId, userName, dto)).collect(Collectors.toList());

        // 异步推送MQ
        listMqDto.parallelStream().forEach(mqDto -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_ERP_LOG_TO_SYS_TOPIC, RocketMqTagEnum.SYNC_ERP_LOG_TO_SYS_TAG.getName(), mqDto, StrUtil.uuid().toLowerCase());
            if (!result.getSendStatus().equals(SendStatus.SEND_OK)) {
                throw new RuntimeException(StrUtil.format("发送系统日志MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        });
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void consumerAndAdd(SysLogMqDTO mqDTO) {
        SysLogRecordEntity sysLogRecordEntity = new SysLogRecordEntity();
        BeanMapperUtils.copy(mqDTO, sysLogRecordEntity);
        sysLogRecordEntity.setCreateUserId(mqDTO.getOperationUserId());
        sysLogRecordEntity.setCreateUserName(mqDTO.getOperationUserName());

        // 数据处理
        handleData(sysLogRecordEntity);

        boolean save = super.save(sysLogRecordEntity);
        if (!save) {
            throw new ServiceException("操作日志保存失败");
        }
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(SysLogRecordEntity sysLogRecordEntity) {
        // TODO 验证数据 & 数据赋值
    }

    @Override
    public PagingVO<SysLogRecordDTO.ListDTO> paging(PagingDTO<SysLogRecordDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<?> query = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SysLogRecordDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<SysLogRecordDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        // 其他如需要显示名称的字段赋值
        list.forEach(SysLogRecordDTO.ListDTO::setActionNameByAction);

    }
}
