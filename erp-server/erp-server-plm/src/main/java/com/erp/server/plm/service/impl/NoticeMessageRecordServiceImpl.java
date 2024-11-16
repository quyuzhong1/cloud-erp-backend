package com.erp.server.plm.service.impl;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.constant.ThirdConstants;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.entity.NoticeMessageRecordEntity;
import com.erp.server.plm.mapper.NoticeMessageRecordMapper;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.NoticeMessageRecordService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class NoticeMessageRecordServiceImpl extends ServiceImpl<NoticeMessageRecordMapper, NoticeMessageRecordEntity>
        implements NoticeMessageRecordService {

    @Autowired
    private CommonService commonService;

    @Override
    public PagingVO<NoticeMessageRecordEntity> paging(PagingDTO<BaseSearchDTO> dto) {

        BaseSearchDTO params = dto.getParams();
        String fsUnionId = params.getFlagId();
        if (StringUtils.isBlank(fsUnionId)) {
            throw new ServiceException(ApiError.ERROR_95062);
        }
        String userId = commonService.getUidByUnionId(ThirdConstants.FS_PLATFORM, fsUnionId);
        params.setFlagId(userId);
        Page<BaseSearchDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<NoticeMessageRecordEntity> pageData = baseMapper.paging(query, params);
        return new PagingVO<>(pageData);
    }
}




