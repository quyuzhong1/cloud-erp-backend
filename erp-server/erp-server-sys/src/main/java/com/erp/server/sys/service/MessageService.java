package com.erp.server.sys.service;
import com.erp.model.sys.entity.MessageEntity;
import com.common.business.service.SuperService;

import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.MessageDTO;

 import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 消息通知表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-10
 */
public interface MessageService extends SuperService<MessageEntity> {

    /**
     * 查询未读消息数
     * @Author Luo_WG
     * @Date 2023/8/10 15:32
     * @return java.util.List<com.erp.model.sys.dto.MessageDTO.NotReadMessageNum>
     **/
    List<MessageDTO.NotReadMessageNum> listNotReadMessageNum();

    /**
     * 查询未读消息详情
     * @Author Luo_WG
     * @Date 2023/8/10 15:59
     * @param type
     * @return java.util.List<com.erp.model.sys.dto.MessageDTO.NotReadMessageNumDetail>
     **/
    List<MessageDTO.NotReadMessageNumDetail> listNotReadMessageDetail(String type);
}
