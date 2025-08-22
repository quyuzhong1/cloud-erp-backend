package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.SampleBorrowDetailEntity;
import com.erp.server.wms.mapper.SampleBorrowDetailMapper;
import com.erp.server.wms.service.SampleBorrowDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SampleBorrowDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 样品借用单明细表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-08-20
 */
@Slf4j
@Service
public class SampleBorrowDetailServiceImpl extends SuperServiceImpl<SampleBorrowDetailMapper, SampleBorrowDetailEntity> implements SampleBorrowDetailService {

    @Override
    public SampleBorrowDetailDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }
}
