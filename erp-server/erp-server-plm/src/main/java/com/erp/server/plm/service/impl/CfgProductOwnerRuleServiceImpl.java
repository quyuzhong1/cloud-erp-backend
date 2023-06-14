package com.erp.server.plm.service.impl;

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.CfgProductOwnerRuleDTO;
import com.erp.model.plm.entity.CfgProductOwnerRuleEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.mapper.CfgProductOwnerRuleMapper;
import com.erp.server.plm.service.CfgProductOwnerRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

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
}
