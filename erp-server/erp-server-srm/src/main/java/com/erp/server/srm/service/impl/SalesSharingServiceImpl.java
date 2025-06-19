package com.erp.server.srm.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.enums.SaleStateEnum;
import com.erp.model.srm.entity.SalesSharingEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.srm.mapper.SalesSharingMapper;
import com.erp.server.srm.service.SalesSharingService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.srm.dto.SalesSharingDTO;
import java.util.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SRM_SALES_SHARING_REPORT;

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
        return new PagingVO(pageData);
    }


    @Override
    public void exportList(SalesSharingDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("销量共享导出", EXPORT_SRM_SALES_SHARING_REPORT.getCode(), pagingParamDTO);
    }


}
