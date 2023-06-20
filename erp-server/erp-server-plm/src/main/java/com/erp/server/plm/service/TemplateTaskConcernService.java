package com.erp.server.plm.service;
import com.erp.model.plm.entity.TemplateTaskConcernEntity;
import com.common.business.service.SuperService;

import java.util.List;


/**
 * <p>
 * 任务关注的人 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-06-20
 */
public interface TemplateTaskConcernService extends SuperService<TemplateTaskConcernEntity> {

    /**
     * 模板任务编辑关注人
     * @Author Luo_WG
     * @Date 2023/6/20 17:17
     * @param templateId
     * @param taskId
     * @param concernUserIdList
     * @return void
     **/
    Boolean saveTemplateConcernList(String templateId, String taskId, List<String> concernUserIdList);

    /**
     * 删除模板关注人
     * @Author Luo_WG
     * @Date 2023/6/20 17:17
     * @param templateId
     * @param taskIds
     * @return void
     **/
    Boolean deleteByTemplateIdAndTaskIds(String templateId,  List<String> taskIds);

    /**
     * 查询任务关注人
     * @Author Luo_WG
     * @Date 2023/6/20 18:12
     * @param templateId
     * @param taskIds
     * @return java.util.List<com.erp.model.plm.entity.TemplateTaskConcernEntity>
     **/
    List<TemplateTaskConcernEntity> listTemplateConcern(String templateId, List<String> taskIds);
}
