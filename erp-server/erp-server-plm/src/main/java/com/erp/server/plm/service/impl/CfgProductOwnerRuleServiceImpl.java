package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.CfgProductOwnerRuleDTO;
import com.erp.model.plm.entity.CfgProductOwnerRuleEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.mapper.CfgProductOwnerRuleMapper;
import com.erp.server.plm.service.CfgProductOwnerRuleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 产品归属规则配置表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-09
 */
@Slf4j
@Service
public class CfgProductOwnerRuleServiceImpl extends SuperServiceImpl<CfgProductOwnerRuleMapper, CfgProductOwnerRuleEntity> implements CfgProductOwnerRuleService {

    @Resource
    private SysUserFeign sysUserFeign;

    /**
     * 添加规则
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-06-13 16:56
     */
    @Override
    public Boolean add(CfgProductOwnerRuleDTO.AddDTO dto) {
        CfgProductOwnerRuleEntity entity = new CfgProductOwnerRuleEntity();
        BeanMapper.copy(dto, entity);
        String orgId = dto.getOrgId();
        List<BaseIdDTO.CodeDTO> codeList = sysUserFeign.getAccountingCompanyList(Arrays.asList("orgId"));
        String orgName = codeList.stream().filter(c -> c.getId().equals(orgId)).findFirst().map(BaseIdDTO.CodeDTO::getName).orElse("");
        entity.setOrgName(orgName);
        return this.save(entity);
    }


    /**
     * 根据分类id获取到配置信息
     * 因为 对应一个分类id 会有父 子
     *
     * @param categoryIdList
     * @return com.erp.model.plm.entity.CfgProductOwnerRuleEntity
     * @author yl
     * @date 2023-06-15 11:34
     */
    @Override
    public CfgProductOwnerRuleEntity getByCategoryIdList(List<String> categoryIdList) {
        if (CollectionUtils.isEmpty(categoryIdList)) {
            return getBase();
        }
        LambdaQueryWrapper<CfgProductOwnerRuleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CfgProductOwnerRuleEntity::getCategoryId, categoryIdList);
        queryWrapper.last("LIMIT 1");
        CfgProductOwnerRuleEntity entity = this.getOne(queryWrapper);
        if (!Objects.isNull(entity)) {
            return entity;
        }
        return getBase();
    }

    private CfgProductOwnerRuleEntity getBase() {
        CfgProductOwnerRuleEntity entity = new CfgProductOwnerRuleEntity();
        entity.setOrgId("");
        entity.setOrgName("深圳市唯迹科技有限公司");
        return entity;
    }
}
