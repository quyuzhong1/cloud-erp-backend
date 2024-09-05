package com.erp.server.dmp.service.impl;


import java.util.*;
import java.util.stream.Collectors;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.constant.DmpConstant;
import com.erp.model.dmp.dto.DmpCfgOutputBlackDTO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.erp.model.dmp.enums.DmpCfgOutputBlackDataTypeEnum;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.dmp.service.DmpCfgOutputBlackService;
import com.erp.server.dmp.service.DmpCfgOutputService;
import com.erp.server.dmp.service.DmpOutputTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.server.dmp.mapper.DmpOutputTaskRecordMapper;
import com.erp.server.dmp.service.DmpOutputTaskRecordService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_NEW_DMP_PUSH_TASK;

/**
 * <p>
 * 推送任务记录 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@Service
public class DmpOutputTaskRecordServiceImpl extends SuperServiceImpl<DmpOutputTaskRecordMapper, DmpOutputTaskRecordEntity> implements DmpOutputTaskRecordService {
    @Resource
    private DmpOutputTaskService dmpOutputTaskService;

    @Resource
    private DmpCfgOutputBlackService dmpCfgOutputBlackService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpOutputTaskRecordDTO.AddDTO addDTO) {
        DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity = new DmpOutputTaskRecordEntity();
        BeanMapperUtils.copy(addDTO, dmpOutputTaskRecordEntity);

        // 数据处理
        handleData(dmpOutputTaskRecordEntity);

        log.info("开始新增推送任务记录");
        boolean save = super.save(dmpOutputTaskRecordEntity);
        if (!save) {
            throw new ServiceException("推送任务记录保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "推送任务记录", dmpOutputTaskRecordEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpOutputTaskRecordEntity.getId(), dmpOutputTaskRecordEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpOutputTaskRecordDTO.UpdateDTO updateDTO) {
        DmpOutputTaskRecordEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "推送任务记录"));
        DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity = BeanMapperUtils.map(DmpOutputTaskRecordEntity.class, updateDTO);

        // 数据处理
        handleData(dmpOutputTaskRecordEntity);
        log.info("编辑 开始修改推送任务记录数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpOutputTaskRecordEntity);
        if (!save) {
            throw new ServiceException("推送任务记录保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录推送任务记录日志数据，id：【{}】", dmpOutputTaskRecordEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpOutputTaskRecordEntity.getId(), "推送任务记录");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity) {
        // TODO 验证数据 & 数据赋值
    }

    @Override
    public List<DmpOutputTaskRecordDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<DmpOutputTaskRecordDTO.TabListDTO> result = new ArrayList<>(4);
        List<DmpOutputTaskRecordDTO.TabListDTO> countList = baseMapper.listStatusCount(dto.getPermissionSql());
        //全部
        int allCount = countList.stream().mapToInt(DmpOutputTaskRecordDTO.TabListDTO::getCount).sum();
        DmpOutputTaskRecordDTO.TabListDTO all = new DmpOutputTaskRecordDTO.TabListDTO();
        all.setCount(allCount);
        all.setTabFlag(DmpConstant.ALL);
        countList.add(all);

        //无需同步
        Integer count = this.lambdaQuery()
                .eq(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
                .eq(DmpOutputTaskRecordEntity::getIsNeedSync, Boolean.FALSE)
                .count();
        DmpOutputTaskRecordDTO.TabListDTO noNeedSync = new DmpOutputTaskRecordDTO.TabListDTO();
        noNeedSync.setTabFlag(SyncStatusEnum.NO_NEED_SYNC.getCode());
        noNeedSync.setCount(count);
        result.add(noNeedSync);
        return result;
    }

    @Override
    public PagingVO<DmpOutputTaskRecordDTO.PagingDTO> paging(PagingDTO<DmpOutputTaskRecordDTO.PagingParamDTO> dto) {
        DmpOutputTaskRecordDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<DmpOutputTaskRecordDTO.PagingDTO> records = pageData.getRecords();
        //数据处理
        doOpHandleDmpPushTask(records);
        return new PagingVO<>(pageData);
    }

    private void doOpHandleDmpPushTask(List<DmpOutputTaskRecordDTO.PagingDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (DmpOutputTaskRecordDTO.PagingDTO listDTO : list) {
            listDTO.setSyncTypeName("推送");

            //如果是推送成功，可能是无需推送状态
            if (DmpOutputTaskRecordStatusEnum.FINISH.getCode().equals(listDTO.getStatus()) && !listDTO.getIsNeedSync()) {
                listDTO.setStatusName(SyncStatusEnum.NO_NEED_SYNC.getName());
            }

        }
    }

    @Override
    public Boolean exportExcel(DmpOutputTaskRecordDTO.ExpotParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("中台推送任务表", EXPORT_NEW_DMP_PUSH_TASK.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public Boolean batchNoNeedSync(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Boolean.FALSE;
        }

        return this.lambdaUpdate()
                .set(DmpOutputTaskRecordEntity::getIsNeedSync, Boolean.FALSE)
                .set(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
                .in(DmpOutputTaskRecordEntity::getId, ids)
                .update();
    }

    @Override
    public Boolean addOutputBlack(BaseIdsDTO.RemarkDTO dto) {
        //获取到需要加入黑名单的任务记录
        List<DmpOutputTaskRecordEntity> recordEntityList = this.lambdaQuery().in(DmpOutputTaskRecordEntity::getId, dto.getIds()).list();

        //根据输出任务记录id获取cfgOutputId
        List<DmpOutputTaskEntity> dmpOutputTaskEntityList = dmpOutputTaskService.lambdaQuery()
                .in(DmpOutputTaskEntity::getId, dto.getIds())
                .list();

        //根据输出任务分组添加黑名单
        Map<String, List<DmpOutputTaskEntity>> outputTaskGroup = dmpOutputTaskEntityList.stream().collect(Collectors.groupingBy(DmpOutputTaskEntity::getCfgOutputId));
        for (Map.Entry<String, List<DmpOutputTaskEntity>> outputTaskGroupMap : outputTaskGroup.entrySet()) {

            //获取到这个分组下的所有单号，根据单号添加黑名单
            List<String> sourceCodeList = recordEntityList.stream()
                    .filter(req -> req.getMainId().equals(outputTaskGroupMap.getKey()))
                    .map(DmpOutputTaskRecordEntity::getSourceCode)
                    .collect(Collectors.toList());

            //添加黑名单
            DmpCfgOutputBlackDTO.AddDTO addDTO = new DmpCfgOutputBlackDTO.AddDTO();
            addDTO.setCompareSign(QueryConditionEnum.EQ.getCompareCode());
            addDTO.setDataType(DmpCfgOutputBlackDataTypeEnum.STRING.getCode());
            addDTO.setFieldName("thirdCode");
            addDTO.setFieldValue(String.join(",", sourceCodeList));
            addDTO.setMainId(outputTaskGroupMap.getKey());
            addDTO.setRemark(dto.getRemark());
            dmpCfgOutputBlackService.add(addDTO);
        }

        return Boolean.TRUE;
    }

    @Override
    public PagingVO<DmpOutputTaskRecordDTO.PagingDTO> exportNewDmpPushTask(PagingDTO<DmpOutputTaskRecordDTO.ExpotParamDTO> dto) {
        Page<DmpOutputTaskRecordDTO.PagingDTO> page = baseMapper.listExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            //数据处理
            doOpHandleDmpPushTask(page.getRecords());
        }
        return new PagingVO<>(page);
    }
}
