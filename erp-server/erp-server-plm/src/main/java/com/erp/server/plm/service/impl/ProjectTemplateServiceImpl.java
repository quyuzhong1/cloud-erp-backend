package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.dto.StartItemSourceDTO;
import com.erp.model.plm.entity.ProjectTemplateEntity;
import com.erp.server.plm.mapper.ProjectTemplateMapper;
import com.erp.server.plm.service.ProjectTemplateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 项目模板信息 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Service
public class ProjectTemplateServiceImpl extends ServiceImpl<ProjectTemplateMapper, ProjectTemplateEntity> implements ProjectTemplateService {


    /**
     * 保存模板 返回模板id
     *
     * @param templateName
     * @return java.lang.String
     * @author yl
     * @date 2022-09-20 14:30
     */
    @Override
    public String saveTemplate(String templateName) {
        checkTemplateName(templateName);
        ProjectTemplateEntity entity = new ProjectTemplateEntity();
        entity.setName(templateName);
        if (this.save(entity)) {
            return entity.getId();
        }
        return "";

    }

    @Override
    public List<StartItemSourceDTO> startItemSource(Integer sourceType) {
        return baseMapper.getStartItemSource(sourceType);
    }


    /**
     * 检查模板名
     *
     * @return
     * @author yl
     * @date 2022-09-20 14:34
     */
    private void checkTemplateName(String templateName) {
        LambdaQueryWrapper<ProjectTemplateEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTemplateEntity::getName, templateName);
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95011);
        }
    }
}
