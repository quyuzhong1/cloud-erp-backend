package com.erp.server.workflow.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.dto.ThirdNoticePushRecordDTO;
import com.erp.model.sys.entity.ThirdNoticePushRecordEntity;
import com.erp.model.sys.enums.ThirdNoticePushRecordNoticeTypeEnum;
import com.erp.model.sys.enums.ThirdNoticePushRecordStatusEnum;
import com.erp.model.workflow.entity.ApproveSyncRecordEntity;
import com.erp.model.workflow.enums.ApproveSyncRecordNoticeTypeEnum;
import com.erp.model.workflow.enums.ApproveSyncRecordStatusEnum;
import com.erp.model.workflow.enums.CfgApproveNoticeNoticeTypeEnum;
import com.erp.model.workflow.enums.CfgApproveSyncSyncPlatformEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.workflow.mapper.ApproveSyncRecordMapper;
import com.erp.server.workflow.service.ApproveSyncRecordService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.workflow.service.OperateLogService;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.ApproveSyncRecordDTO;
import java.util.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PROCESS_APPROVE_SYNC_RECORD;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_SYS_THIRD_NOTICE_RECORD;

/**
 * <p>
 * ERP审批同步-通知配置 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-05-12
 */
@Slf4j
@Service
public class ApproveSyncRecordServiceImpl extends SuperServiceImpl<ApproveSyncRecordMapper, ApproveSyncRecordEntity> implements ApproveSyncRecordService {


    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Override
    public List<ApproveSyncRecordDTO.TabListDTO> tabList(PermissionsDTO param) {
        ApproveSyncRecordDTO.PagingParamDTO searchParam = new ApproveSyncRecordDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<ApproveSyncRecordDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        List<ApproveSyncRecordDTO.TabListDTO> result = new ArrayList<>();
        ApproveSyncRecordDTO.TabListDTO enable = list.stream().filter(e -> e.getTabFlag().equals(ApproveSyncRecordStatusEnum.SUCCESS.getCode())).findFirst().orElse(null);
        ApproveSyncRecordDTO.TabListDTO disable = list.stream().filter(e -> e.getTabFlag().equals(ApproveSyncRecordStatusEnum.FAILED.getCode())).findFirst().orElse(null);
        result.add(new ApproveSyncRecordDTO.TabListDTO("all", "全部" , 0));
        result.add(new ApproveSyncRecordDTO.TabListDTO(ApproveSyncRecordStatusEnum.SUCCESS.getCode(), ApproveSyncRecordStatusEnum.SUCCESS.getName(), null == enable ? 0 : enable.getCount()));
        result.add(new ApproveSyncRecordDTO.TabListDTO(ApproveSyncRecordStatusEnum.FAILED.getCode(),ApproveSyncRecordStatusEnum.FAILED.getName(), null == disable ? 0 : disable.getCount()));
        return result;
    }

    @Override
    public PagingVO<ApproveSyncRecordDTO.ListDTO> paging(PagingDTO<ApproveSyncRecordDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<ApproveSyncRecordDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }
    private void fillList(List<ApproveSyncRecordDTO.ListDTO> records) {
        for (ApproveSyncRecordDTO.ListDTO record : records) {
            //单据类型
            String businessType = record.getBusinessType();
            record.setBusinessTypeName(SourceTypeEnum.getName(businessType));

            record.setNoticeTypeName(ApproveSyncRecordNoticeTypeEnum.getName(record.getNoticeType()));

            record.setNoticeMethodName(CfgApproveSyncSyncPlatformEnum.getName(record.getNoticeMethod()));

            record.setStatusName(ApproveSyncRecordStatusEnum.getName(record.getStatus()));

            record.setNoticeNodeName(CfgApproveNoticeNoticeTypeEnum.getName(record.getNoticeNode()));
        }
    }

    @Override
    public void exportList(ApproveSyncRecordDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("三方推送记录导出", EXPORT_PROCESS_APPROVE_SYNC_RECORD.getCode(), param);
    }

    @Override
    public BatchResultDTO repush(String id) {
        ApproveSyncRecordEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到三方推送记录数据"));
        return BatchResultDTO.success(entity.getId(), entity.getId(), "");
    }
}
