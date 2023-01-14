package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.dto.AddChangeDTO;
import com.erp.model.plm.entity.ProductChangeEntity;
import com.erp.server.plm.mapper.ProductChangeMapper;
import com.erp.server.plm.service.ProductChangeService;
import org.springframework.stereotype.Service;

/**
 * 变更信息表(ProductChange)表服务实现类
 *
 * @author yl
 * @since 2023-01-11 14:05:03
 */
@Service
public class ProductChangeServiceImpl extends ServiceImpl<ProductChangeMapper, ProductChangeEntity> implements ProductChangeService {

    
    /**
     * 添加变更
     * @author yl
     * @date 2023-01-14 15:02
     * @param dto
     * @return java.lang.Boolean
     */
    @Override
    public Boolean add(AddChangeDTO dto) {
        return null;
    }
}
