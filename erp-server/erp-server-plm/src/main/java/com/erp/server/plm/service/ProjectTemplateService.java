package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.StartItemSourceDTO;
import com.erp.model.plm.entity.ProjectTemplateEntity;

import java.util.List;

/**
 * <p>
 * 项目模板信息 服务类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
public interface ProjectTemplateService extends IService<ProjectTemplateEntity> {

    String saveTemplate(String templateName);

    List<StartItemSourceDTO> startItemSource(Integer sourceType);
}
