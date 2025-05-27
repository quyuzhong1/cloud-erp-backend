package com.erp.server.sys.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CfgThirdNoticeDTO;
import com.erp.model.sys.dto.ThirdNoticePushRecordDTO;
import com.erp.model.sys.entity.CfgApproveSyncFieldMapEntity;
import com.erp.model.sys.entity.CfgThirdNoticeEntity;
import com.erp.model.sys.entity.ThirdNoticePushRecordEntity;
import com.erp.model.sys.enums.ThirdNoticePushRecordNoticeTypeEnum;
import com.erp.model.sys.enums.ThirdNoticePushRecordStatusEnum;
import com.erp.model.workflow.enums.CfgApproveSyncSyncPlatformEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.sys.mapper.ThirdNoticePushRecordMapper;
import com.erp.server.sys.service.ThirdNoticePushRecordService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.sys.service.OperateLogService;
import com.erp.server.sys.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.ThirdNoticePushRecordDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SYS_THIRD_NOTICE;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_SYS_THIRD_NOTICE_RECORD;

/**
 * <p>
 * 三方通知推送记录 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-05-26
 */
@Slf4j
@Service
public class ThirdNoticePushRecordServiceImpl extends SuperServiceImpl<ThirdNoticePushRecordMapper, ThirdNoticePushRecordEntity> implements ThirdNoticePushRecordService {

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Override
    public List<ThirdNoticePushRecordDTO.TabListDTO> tabList(PermissionsDTO param) {
        ThirdNoticePushRecordDTO.PagingParamDTO searchParam = new ThirdNoticePushRecordDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<ThirdNoticePushRecordDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        List<ThirdNoticePushRecordDTO.TabListDTO> result = new ArrayList<>();
        ThirdNoticePushRecordDTO.TabListDTO enable = list.stream().filter(e -> e.getTabFlag().equals(ThirdNoticePushRecordStatusEnum.SUCCESS.getCode())).findFirst().orElse(null);
        ThirdNoticePushRecordDTO.TabListDTO disable = list.stream().filter(e -> e.getTabFlag().equals(ThirdNoticePushRecordStatusEnum.FAILED.getCode())).findFirst().orElse(null);
        result.add(new ThirdNoticePushRecordDTO.TabListDTO("all", "全部" , 0));
        result.add(new ThirdNoticePushRecordDTO.TabListDTO(ThirdNoticePushRecordStatusEnum.SUCCESS.getCode(),ThirdNoticePushRecordStatusEnum.SUCCESS.getName(), null == enable ? 0 : enable.getCount()));
        result.add(new ThirdNoticePushRecordDTO.TabListDTO(ThirdNoticePushRecordStatusEnum.FAILED.getCode(),ThirdNoticePushRecordStatusEnum.FAILED.getName(), null == disable ? 0 : disable.getCount()));
        return result;
    }


    @Override
    public PagingVO<ThirdNoticePushRecordDTO.ListDTO> paging(PagingDTO<ThirdNoticePushRecordDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<ThirdNoticePushRecordDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    private void fillList(List<ThirdNoticePushRecordDTO.ListDTO> records) {
        for (ThirdNoticePushRecordDTO.ListDTO record : records) {

            //单据类型
            String businessType = record.getBusinessType();
            record.setBusinessTypeName(SourceTypeEnum.getName(businessType));

            record.setNoticeTypeName(ThirdNoticePushRecordNoticeTypeEnum.getName(record.getNoticeType()));

            record.setNoticeMethodName(CfgApproveSyncSyncPlatformEnum.getName(record.getNoticeMethod()));

            record.setStatusName(ThirdNoticePushRecordStatusEnum.getName(record.getStatus()));
        }
    }


    @Override
    public void exportList(ThirdNoticePushRecordDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("三方通知推送记录导出", EXPORT_SYS_THIRD_NOTICE_RECORD.getCode(), param);
    }


    @Override
    public BatchResultDTO repush(String id) {
        ThirdNoticePushRecordEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到三方通知推送记录数据"));


        return BatchResultDTO.success(entity.getId(), entity.getId(), "");
    }

}
