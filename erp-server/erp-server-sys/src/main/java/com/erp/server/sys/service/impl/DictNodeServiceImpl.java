package com.erp.server.sys.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.dto.DictNodeDTO;
import com.erp.model.sys.entity.DictNodeEntity;
import com.erp.server.sys.mapper.DictNodeMapper;
import com.erp.server.sys.service.DictNodeService;
import org.springframework.stereotype.Service;

import java.util.List;

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
     * @param businessType
     * @return java.util.List<com.erp.model.sys.dto.DictNodeDTO.ViewDTO>
     * @author yl
     * @date 2023-04-26 14:46
     */
    @Override
    public List<DictNodeDTO.ViewDTO> listByBusinessType(String businessType) {
        List<DictNodeEntity> list = this.lambdaQuery().eq(DictNodeEntity::getBusinessType, businessType).list();
        return BeanMapper.copyList(list,DictNodeDTO.ViewDTO.class);
    }
}
