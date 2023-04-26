package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.dto.NoticeDTO;
import com.erp.model.sys.dto.NoticeReceivedDTO;
import com.erp.model.sys.entity.NoticeInfoEntity;
import com.erp.server.sys.mapper.NoticeInfoMapper;
import com.erp.server.sys.service.NoticeInfoService;
import com.erp.server.sys.service.NoticeReceivedService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

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


    /**
     * 编辑通知
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-26 15:47
     */
    @Override
    public Boolean edit(NoticeDTO.UpdateDTO dto) {
        String id = dto.getId();
        NoticeInfoEntity notice = this.getById(id);
        if (Objects.isNull(notice)) {
            throw new ServiceException(ApiError.ERROR_9045);
        }
        notice.setNodeId(dto.getNodeId());
        notice.setSystem(dto.getSystem());
        //通知接收人
        List<NoticeReceivedDTO.UpdateDTO> receivedList = dto.getReceivedList();
        Boolean result = this.updateById(notice);
        if (result) {
            noticeReceivedService.edit(receivedList);
        }
        return result;
    }
}
