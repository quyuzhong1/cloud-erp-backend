package com.erp.server.scm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.CfgSupplierSalesDTO;
import com.erp.model.scm.entity.SalesSharingEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.scm.mapper.SalesSharingMapper;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.SalesSharingService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.scm.dto.SalesSharingDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_SALES_SHARING_REPORT;

/**
 * <p>
 * 销量共享表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-06-18
 */
@Slf4j
@Service
public class SalesSharingServiceImpl extends SuperServiceImpl<SalesSharingMapper, SalesSharingEntity> implements SalesSharingService {


    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Override
    public PagingVO<SalesSharingDTO.ListDTO> paging(PagingDTO<SalesSharingDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SalesSharingDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }
    private void fillList(List<SalesSharingDTO.ListDTO> records) {

    }

    @Override
    public void exportList(SalesSharingDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("销量共享导出", EXPORT_SCM_SALES_SHARING_REPORT.getCode(), pagingParamDTO);
    }


}
