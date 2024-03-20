package com.erp.server.sys.rocketmq.sync.kingdee.impl;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.ApiModuleTypeEnum;
import com.common.message.enums.AssistantDataEnum;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictGlobalAreaEntity;
import com.erp.model.sys.entity.ThirdpartyRefBusinessEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeCityService;
import com.erp.server.sys.service.DictCountryService;
import com.erp.server.sys.service.DictGlobalAreaService;
import com.erp.server.sys.service.ThirdpartyRefBusinessService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Lambda
 * @Classname SyncKingdeeCityServiceImpl
 * @Description TODO
 * @Date 2024-03-20 11:10
 * @Created by yl
 */
@Slf4j
@Service
public class SyncKingdeeCityServiceImpl implements SyncKingdeeCityService {

    @Resource
    private ThirdpartyRefBusinessService thirdpartyRefBusinessService;
    @Resource
    private DictCountryService dictCountryService;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void syncDataToKingdee(DictCityEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();
        Boolean isExistParent = true;
        resultMap.put("isExistParent", isExistParent);
        //业务id
        resultMap.put("id",entity.getId());
        //编码
        resultMap.put("code",entity.getKingdeeCode());
        //名称
        resultMap.put("name",entity.getName());
        ThirdpartyRefBusinessEntity thirdpartyRef=  thirdpartyRefBusinessService.getByBusinessId(entity.getId());
        String syncKingdeeId="";
        if (Objects.nonNull(thirdpartyRef)) {
            syncKingdeeId = thirdpartyRef.getThirdpartyId();
        }
        //金蝶id
        resultMap.put("syncKingdeeId",syncKingdeeId);
        resultMap.put("operate", operate);
        //模块类型
        Integer moduleType = ApiModuleTypeEnum.PROVINCE_CITY.getCode();
        //辅助资料类型编码
        String fNumber = AssistantDataEnum.PROVINCE.getCode();
        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            sendMqAndSaveTask(entity,operate,resultMap);
            return;
        }
        if(isExistParent){
            DictCountryEntity countryEntity = dictCountryService.getById(entity.getCountryCode());
            if (Objects.isNull(countryEntity) || StringUtils.isBlank(countryEntity.getKingdeeCode())) {
                throw new ServiceException(new ApiResult(1, "未找到上级国家或者国家未同步到金蝶"));
            }
            //上级编码
            resultMap.put("parentCode",countryEntity.getKingdeeCode());
        }
        resultMap.put("moduleType",moduleType);
        resultMap.put("fNumber", fNumber);
        sendMqAndSaveTask(entity,operate,resultMap);
    }

    private void sendMqAndSaveTask(DictCityEntity entity, String operate, Map<String, Object> resultMap) {
        //添加推送任务
        DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
        taskFeignDTO.setSourceId(entity.getId());
        taskFeignDTO.setSourceCode(entity.getCode());
        taskFeignDTO.setSourceType(SourceTypeEnum.PROVINCE_CITY.getCode());
        taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
        taskFeignDTO.setMqTag(RocketMqTagEnum.KINGDEE_ASSISTANT_DATA_TAG.getName());
        taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
        taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        taskFeignDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
        taskFeignDTO.setSyncOperate(operate);
        dmpMqFeign.sendMqAndSaveTask(taskFeignDTO);

    }
}
