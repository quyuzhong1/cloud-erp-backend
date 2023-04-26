package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.service.SuperServiceImpl;
import com.erp.model.sys.dto.NoticeDTO;
import com.erp.model.sys.entity.NoticeInfoEntity;
import com.erp.server.sys.mapper.NoticeInfoMapper;
import com.erp.server.sys.service.NoticeReceivedService;
import com.erp.server.sys.service.NoticeInfoService;
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
public class NoticeInfoServiceImpl extends SuperServiceImpl<NoticeInfoMapper, NoticeInfoEntity> implements NoticeInfoService {


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
        NoticeInfoEntity notice = new NoticeInfoEntity();
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
