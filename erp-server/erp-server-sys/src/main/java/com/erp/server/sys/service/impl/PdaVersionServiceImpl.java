package com.erp.server.sys.service.impl;

import com.common.business.dto.FindUserDTO;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.dto.PdaVersionDTO;
import com.erp.model.sys.entity.MessageEntity;
import com.erp.model.sys.entity.MessageUserReadEntity;
import com.erp.model.sys.entity.PdaVersionEntity;
import com.erp.model.sys.enums.MessageTypeEnum;
import com.erp.server.sys.mapper.PdaVersionMapper;
import com.erp.server.sys.service.MessageService;
import com.erp.server.sys.service.MessageUserReadService;
import com.erp.server.sys.service.PdaVersionService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.sys.service.SysUserInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-14
 */
@Slf4j
@Service
public class PdaVersionServiceImpl extends SuperServiceImpl<PdaVersionMapper, PdaVersionEntity> implements PdaVersionService {
    @Resource
    private SysUserInfoService sysUserInfoService;

    @Resource
    private MessageService messageService;

    @Resource
    private MessageUserReadService messageUserReadService;

    @Override
    public PdaVersionEntity getPdaVersion() {
        return lambdaQuery().orderByDesc(PdaVersionEntity::getUpdateTime).last("LIMIT 1").one();
    }

    @Override
    public Boolean release(PdaVersionDTO.AddDTO dto) {
        PdaVersionEntity entity = new PdaVersionEntity();
        BeanMapper.copy(dto, entity);
        boolean flag = this.save(entity);
        if (flag) {
            List<FindUserDTO> allUserList = sysUserInfoService.getAllUserList();
            MessageEntity messageEntity = new MessageEntity();
            messageEntity.setType(MessageTypeEnum.sys.getCode());
            messageEntity.setRemark(dto.getRemark());
            LinkedHashMap<String, Object> map = new LinkedHashMap();
            map.put("version", dto.getPdaVersion());
            map.put("remark", dto.getRemark());
            messageEntity.setDataJson(map);
            messageService.save(messageEntity);
            List<MessageUserReadEntity> readEntityList = new ArrayList<>();
            for (FindUserDTO findUserDTO : allUserList) {
                MessageUserReadEntity readEntity = new MessageUserReadEntity();
                readEntity.setMessageId(messageEntity.getId());
                readEntity.setUserId(findUserDTO.getUserId());
                readEntity.setIsRead(Boolean.FALSE);
                readEntityList.add(readEntity);
            }
            messageUserReadService.saveBatch(readEntityList);
        }
        return flag;
    }
}
