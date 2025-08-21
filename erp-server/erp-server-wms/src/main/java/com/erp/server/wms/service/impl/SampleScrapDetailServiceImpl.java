package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.dto.SampleScrapInfoDTO;
import com.erp.model.wms.entity.SampleScrapDetailEntity;
import com.erp.server.wms.mapper.SampleScrapDetailMapper;
import com.erp.server.wms.service.SampleScrapDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SampleScrapDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 样品报废单明细表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-08-20
 */
@Slf4j
@Service
public class SampleScrapDetailServiceImpl extends SuperServiceImpl<SampleScrapDetailMapper, SampleScrapDetailEntity> implements SampleScrapDetailService {


    @Override
    public SampleScrapDetailDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }

    @Override
    public List<SampleScrapDetailEntity> listByMainId(String id) {
        if(StringUtils.isBlank(id)){
            return Collections.emptyList();
        }
        return lambdaQuery().eq(SampleScrapDetailEntity::getMainId, id).list();
    }

}
