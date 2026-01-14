package com.erp.server.sys.rocketmq.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.constant.DmpPullConstant;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.PlatformCityDictDTO;
import com.common.business.enums.*;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.MongoDBUpdateDTO;
import com.erp.model.dmp.entity.DmpFeishuUserInfoEntity;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.sys.dto.SysUserInfoDTO;
import com.erp.model.sys.dto.UpdateUserStateDTO;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.DictThirdCity;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.model.sys.entity.SysUserThirdEntity;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.sys.convert.CityDictConvert;
import com.erp.server.sys.service.DictCityService;
import com.erp.server.sys.service.DictThirdCityService;
import com.erp.server.sys.service.SysUserInfoService;
import com.erp.server.sys.service.SysUserThirdService;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 下载第三方城市字典消费类
 */
@Service
@Slf4j
public class FsUseInfoConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private MQProducerService mqProducerService;

    @Resource
    private DmpMongoDbFeign dmpMongoDbFeign;

    @Resource
    private SysUserInfoService sysUserInfoService;

    @Resource
    private SysUserThirdService sysUserThirdService;

    @Override
    public void updateMongodbData(String platform, String uniqueId, Integer isClean) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(uniqueId) || org.apache.commons.lang3.StringUtils.isEmpty(platform) || Objects.isNull(isClean)){
            return;
        }
        MongoDBUpdateDTO dto = MongoDBUpdateDTO.builder()
                .tableName(getTableName(platform))
                .uniqueId(uniqueId)
                .isClean(isClean)
                .build();
        dmpMongoDbFeign.updateMongoDbData(dto);
    }

    /**
     * 根据平台组装表名
     * @param platform
     * @return
     */
    private String getTableName(String platform){
        return StrUtil.format("{}_{}_{}", PlatformCategoryEnum.THIRD_SYSTEM.getCode(),
                platform, BusinessTypeEnum.INBOUND.getCode());
    }

    @Override
    public void updateSyncTaskStatus(DmpSyncMqDTO.ParamDTO paramDTO) {
        dmpTaskFeign.updateSyncInfo(paramDTO);
    }

    @Override
    public void sendWarnMsg(String syncTaskId, String msg) {
        DmpPullTaskEntity dmpPullTaskEntity = dmpTaskFeign.getPullTaskById(syncTaskId);
        WarnMsgInfoDTO msgInfoDTO = this.buildWarnMsgInfoDTO(dmpPullTaskEntity,msg);
        mqProducerService.sendWarnMsg(msgInfoDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> handle(Object ext) {
        DmpFeishuUserInfoEntity dto = JSONUtil.toBean(ext.toString(), DmpFeishuUserInfoEntity.class);

        String eventType = dto.getEventType();
        String userId = dto.getUserId();
        String name = dto.getName();
        Boolean isResigned = dto.getIsResigned();
        if(Objects.equals(DmpPullConstant.USER_DELETED,eventType)){ //离职
            LambdaQueryWrapper<SysUserThirdEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUserThirdEntity::getThirdUserId, userId);
            List<SysUserThirdEntity> list = sysUserThirdService.list(queryWrapper);
            if(CollUtil.isEmpty(list)){
                log.error(ApiError.COMMON_FS_USER_NOT_BIND.getMsg(),eventType,name);
                throw new ServiceException(ApiError.COMMON_FS_USER_NOT_BIND,eventType,name);
            }
            if(isResigned){
                UpdateUserStateDTO stateDTO = new UpdateUserStateDTO();
                stateDTO.setState(0);
                stateDTO.setIds(list.stream().map(SysUserThirdEntity::getUserId).collect(Collectors.toList()));
                sysUserInfoService.updateState(stateDTO);
            }
        }else if(Objects.equals(DmpPullConstant.USER_CREATED,eventType)){//入职
            SysUserInfoDTO sysUserInfoDTO = new SysUserInfoDTO();
            sysUserInfoDTO.setRealName(name);
            sysUserInfoDTO.setUserName(name);
            sysUserInfoDTO.setEmail(dto.getEmail());
            sysUserInfoDTO.setMobile(dto.getMobile());
            sysUserInfoDTO.setUserType(UserTypeEnum.ERP.code);
            sysUserInfoDTO.setUserState(0);
            sysUserInfoDTO.setCreatePasswordType(0);
            sysUserInfoDTO.setNeedChangePwd(Boolean.TRUE);
            sysUserInfoDTO.setIsSuper(Boolean.FALSE);
            sysUserInfoService.add(sysUserInfoDTO);
        }else {
            log.error(ApiError.COMMON_FS_USER_NOT_BIND.getMsg(),eventType,name);
            throw new ServiceException(ApiError.COMMON_FS_USER_NOT_BIND,eventType,name);
        }
        return ApiResult.success();
    }

    private WarnMsgInfoDTO buildWarnMsgInfoDTO(DmpPullTaskEntity dmpPullTaskEntity,String msg) {
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName(SourceTypeEnum.getName(dmpPullTaskEntity.getSourceType()));
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_SYS);
        warnMsgInfo.setTitle(StrUtil.format("获取飞书离职员工消息消费失败，来源平台:{},目标平台:{}",dmpPullTaskEntity.getSourcePlatformName(),dmpPullTaskEntity.getTargetPlatformName()));
        warnMsgInfo.setTableName(SourceTypeEnum.getTableName(dmpPullTaskEntity.getSourceType()));
        warnMsgInfo.setTableId(dmpPullTaskEntity.getId());
        warnMsgInfo.setKeyInfo(StringUtils.isBlank(msg)?"":msg);
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.IMPLEMENT_GROUP_NOTICE);
        return warnMsgInfo;
    }
}
