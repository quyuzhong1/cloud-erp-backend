package com.erp.server.sys.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.dto.DictNodeDTO;
import com.erp.model.sys.entity.DictNodeEntity;
import com.erp.model.sys.entity.NoticeInfoEntity;
import com.erp.server.sys.mapper.DictNodeMapper;
import com.erp.server.sys.service.DictNodeService;
import com.erp.server.sys.service.NoticeInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 通知信息表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-26
 */
@Service
public class DictNodeServiceImpl extends SuperServiceImpl<DictNodeMapper, DictNodeEntity> implements DictNodeService {

    @Resource
    private NoticeInfoService noticeInfoService;

    /**
     * 添加节点
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-26 14:32
     */
    @Override
    public Boolean add(DictNodeDTO.AddDTO dto) {
        DictNodeEntity dictNode = new DictNodeEntity();
        BeanMapper.copy(dto, dictNode);
        return this.save(dictNode);
    }


    /**
     * 根据业务类型获取到通知节点
     *
     * @param module
     * @return java.util.List<com.erp.model.sys.dto.DictNodeDTO.ViewDTO>
     * @author yl
     * @date 2023-04-26 14:46
     */
    @Override
    public List<DictNodeDTO.ViewDTO> listByModule(String module) {
        List<DictNodeEntity> list = this.lambdaQuery().eq(DictNodeEntity::getModule, module).list();
        List<DictNodeDTO.ViewDTO> resultList = BeanMapper.copyList(list, DictNodeDTO.ViewDTO.class);
        List<String> nodeKeys = resultList.stream().map(DictNodeDTO.ViewDTO::getNodeKey).collect(Collectors.toList());
        List<NoticeInfoEntity> noticeInfoList = noticeInfoService.listByNodeKeys(nodeKeys);
        List<String> noticeNodeKeyList = noticeInfoList.stream().map(NoticeInfoEntity::getNodeKey).collect(Collectors.toList());
        for (DictNodeDTO.ViewDTO item : resultList) {
            String nodeKey = item.getNodeKey();
            Boolean isDeleted = noticeNodeKeyList.contains(nodeKey) ? true : false;
            item.setIsDeleted(isDeleted);
        }
        return resultList;
    }
}
