package com.erp.server.plm.service;
import com.erp.model.plm.dto.ProductMemberDTO;
import com.erp.model.plm.entity.TaskCommentRefEntity;
import com.common.business.service.SuperService;

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
     * 任务评论@后获取到对应的数据
     * @author yl
     * @date 2023-06-20 11:49
     * @param productId
     * @return java.util.List<com.erp.model.plm.dto.ProductMemberDTO.TaskRefDTO>
     */
    List<ProductMemberDTO.TaskRefDTO> listMemberByProductId(String productId);
}
