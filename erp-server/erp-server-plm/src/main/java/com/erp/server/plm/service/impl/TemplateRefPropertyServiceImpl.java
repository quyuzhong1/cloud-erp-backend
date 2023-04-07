package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.SuperServiceImpl;
import com.erp.model.plm.dto.TemplatePropertyDTO;
import com.erp.model.plm.entity.TemplateRefPropertyEntity;
import com.erp.server.plm.mapper.TemplateRefPropertyMapper;
import com.erp.server.plm.service.TemplateRefPropertyService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 模板属性关系表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-06
 */
@Service
public class TemplateRefPropertyServiceImpl extends SuperServiceImpl<TemplateRefPropertyMapper, TemplateRefPropertyEntity> implements TemplateRefPropertyService {


    /**
     * 保存模板与产品属性的关系表
     *
     * @param templateId            模板id
     * @param productPropertyIdList
     * @return void
     * @author yl
     * @date 2023-03-06 14:13
     */
    @Override
    public void saveRef(String templateId, List<String> productPropertyIdList) {
        //先删除
        removeByTemplateId(templateId);
        if (CollectionUtils.isNotEmpty(productPropertyIdList)) {
            List<TemplateRefPropertyEntity> addList = new ArrayList<>(productPropertyIdList.size());
            for (String propertyId : productPropertyIdList) {
                TemplateRefPropertyEntity ref = new TemplateRefPropertyEntity();
                ref.setProductPropertyId(propertyId);
                ref.setTemplateId(templateId);
                addList.add(ref);
            }
            this.saveBatch(addList);
        }
    }

    /**
     * 根据模板id 集合获取到 属性信息
     *
     * @param templateIdList
     * @return java.util.List<com.erp.model.plm.dto.TemplatePropertyDTO>
     * @author yl
     * @date 2023-03-06 16:26
     */
    @Override
    public List<TemplatePropertyDTO> getByTemplateIds(List<String> templateIdList) {
        if (CollectionUtils.isEmpty(templateIdList)) {
            return new ArrayList<>(1);
        }
        return baseMapper.getByTemplateIds(templateIdList);
    }


    /**
     * 根据模板id 删除对应关系数据
     *
     * @param
     * @return void
     * @author yl
     * @date 2023-03-06 14:20
     */
    public void removeByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateRefPropertyEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateRefPropertyEntity::getTemplateId, templateId);
        this.remove(queryWrapper);

    }
}
