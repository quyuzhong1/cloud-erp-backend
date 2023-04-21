package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.service.SuperServiceImpl;
import com.erp.model.sys.dto.NoticeDTO;
import com.erp.model.sys.entity.NoticeEntity;
import com.erp.server.sys.mapper.NoticeMapper;
import com.erp.server.sys.service.NoticeReceivedService;
import com.erp.server.sys.service.NoticeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * <p>
 * 通知表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-20
 */
@Service
public class NoticeServiceImpl extends SuperServiceImpl<NoticeMapper, NoticeEntity> implements NoticeService {


    @Resource
    private NoticeReceivedService noticeReceivedService;

    /**
     * 添加通知
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-20 20:43
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(NoticeDTO.AddDTO dto) {
        NoticeEntity notice = new NoticeEntity();
        notice.setNodeCodeDict(dto.getNodeCodeDict());
        String id = IdWorker.getIdStr();
        notice.setId(id);
        Boolean result = this.save(notice);
        if (result) {
            //保存接收人信息
            noticeReceivedService.add(id, dto.getReceivedList());
        }

        return result;
    }
}
