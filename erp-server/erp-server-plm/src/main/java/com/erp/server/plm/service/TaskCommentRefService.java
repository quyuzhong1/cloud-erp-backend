package com.erp.server.plm.service;

import com.common.business.dto.FindUserDTO;
import com.common.business.service.SuperService;
import com.erp.model.plm.dto.ProductMemberDTO;
import com.erp.model.plm.entity.TaskCommentRefEntity;

import java.util.List;


/**
 * <p>
 * 任务评论关联表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-09
 */
public interface TaskCommentRefService extends SuperService<TaskCommentRefEntity> {


    /**
     * 任务评论@后获取到对应人员的数据
     * @author yl
     * @date 2023-06-20 11:49
     * @param productId
     * @return java.util.List<com.erp.model.plm.dto.ProductMemberDTO.TaskRefDTO>
     */
    List<ProductMemberDTO.TaskRefDTO> listMemberByProductId(String productId);

    /**
     * 添加任务关注人西西呢
     * @param refUserIdList
     * @param taskId
     * @param userList
     */
    void addCommentRef(List<String> refUserIdList, String taskId,String commentId,List<FindUserDTO> userList);
}
