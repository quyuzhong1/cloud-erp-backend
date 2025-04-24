package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.dto.PdaVersionDTO;
import com.erp.model.sys.entity.MessageEntity;
import com.erp.model.sys.entity.MessageUserReadEntity;
import com.erp.model.sys.entity.PdaUserSkipVersionEntity;
import com.erp.model.sys.entity.PdaVersionEntity;
import com.erp.model.sys.enums.MessageTypeEnum;
import com.erp.model.sys.enums.SysTypeEnum;
import com.erp.server.sys.mapper.PdaVersionMapper;
import com.erp.server.sys.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

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

    @Resource
    private PdaUserSkipVersionService pdaUserSkipVersionService;

    @Override
    public PagingVO<PdaVersionDTO.PagingDTO> paging(PagingDTO<PdaVersionDTO.PagingParamDTO> dto) {
        PdaVersionDTO.PagingParamDTO params = dto.getParams();
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<PdaVersionDTO.PagingDTO> pageData = baseMapper.paging(query, params);
        List<PdaVersionDTO.PagingDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO(pageData);
        }
        list.forEach(req -> req.setTypeName(SysTypeEnum.getName(req.getType())));
        return new PagingVO(pageData);
    }

    @Override
    public PdaVersionEntity getPdaVersion() {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        return baseMapper.getPdaVersion(userInfo.getUid());
    }

    @Override
    public Boolean release(PdaVersionDTO.AddDTO dto) {
        PdaVersionEntity entity = new PdaVersionEntity();
        BeanMapper.copy(dto, entity);
        boolean flag = this.save(entity);
        if (flag) {
            List<FindUserDTO> allUserList = sysUserInfoService.getAllUserList();
            MessageEntity messageEntity = new MessageEntity();
            messageEntity.setType(MessageTypeEnum.SYS.getCode());
            messageEntity.setRemark(dto.getRemark());
            LinkedHashMap<String, Object> map = new LinkedHashMap();
            map.put("version", dto.getPdaVersion());
            map.put("remark", dto.getRemark());
            messageEntity.setDataJson(map);
            messageEntity.setApplication(dto.getType());
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

    @Override
    public Boolean skipVersion(String versionId) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        PdaUserSkipVersionEntity entity = new PdaUserSkipVersionEntity();
        entity.setUserId(userInfo.getUid());
        entity.setUserName(userInfo.getUserName());
        entity.setVersionId(versionId);
        return pdaUserSkipVersionService.save(entity);
    }
}
