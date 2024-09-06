package com.erp.server.dmp.service.impl;


import java.lang.reflect.Method;
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
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.constant.DmpConstant;
import com.erp.model.dmp.dto.DmpCfgOutputBlackDTO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.DmpCfgOutputBlackDataTypeEnum;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.server.dmp.mapper.DmpOutputTaskRecordMapper;

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

    @Resource
    private DmpCfgInputService dmpCfgInputService;

    @Resource
    private DmpCfgInputConvertService dmpCfgInputConvertService;

    @Resource
    private DmpCfgOutputService dmpCfgOutputService;

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
        countList.add(noNeedSync);
        return countList;
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
    public Boolean addOutputBlack(DmpOutputTaskRecordDTO.AddOutputBlackDTO dto) {
        //勾选方式添加黑名单
        if (CollectionUtils.isNotEmpty(dto.getIds())) {
            checkAddBlack(dto);
        }

        //自定义条件方式添加黑名单
        if (ObjectUtil.isNotEmpty(dto.getParams())) {
            customizeBlack(dto);
        }
        return Boolean.TRUE;
    }

    /**
     * 自定义条件方式添加黑名单
     * @param dto
     */
    private void customizeBlack(DmpOutputTaskRecordDTO.AddOutputBlackDTO dto) {
        DmpOutputTaskRecordDTO.CustomizeBlackParam params = dto.getParams();

        //根据dmp_cfg_input表id查询输入配置转换表
        List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList = dmpCfgInputConvertService.lambdaQuery()
                .eq(DmpCfgInputConvertEntity::getMainId, params.getBillTypeId())
                .list();

        //根据系统id查询输出任务
        List<DmpCfgOutputEntity> dmpCfgOutputEntityList = dmpCfgOutputService.lambdaQuery()
                .eq(DmpCfgOutputEntity::getSystemId, params.getTargetPlatformCode())
                .list();

        for (DmpCfgOutputEntity dmpCfgOutputEntity : dmpCfgOutputEntityList) {
            //匹配输入配置对应的输出任务
            DmpCfgInputConvertEntity dmpCfgInputConvertEntity = dmpCfgInputConvertEntityList.stream().filter(req -> req.getId().equals(dmpCfgOutputEntity.getInputConvertId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(dmpCfgInputConvertEntity)) {

                List<String> codeKeyList = getSourceCodeKeys(dmpCfgOutputEntity.getOutputClass());
                if (CollectionUtils.isNotEmpty(codeKeyList)) {
                    //添加黑名单
                    DmpCfgOutputBlackDTO.AddDTO addDTO = new DmpCfgOutputBlackDTO.AddDTO();
                    addDTO.setCompareSign(QueryConditionEnum.EQ.getCompareCode());
                    addDTO.setDataType(DmpCfgOutputBlackDataTypeEnum.STRING.getCode());
                    addDTO.setFieldName(codeKeyList.get(0));
                    addDTO.setFieldValue(String.join(",", params.getSourceCodeList()));
                    addDTO.setMainId(dmpCfgOutputEntity.getId());
                    addDTO.setRemark(dto.getRemark());
                    dmpCfgOutputBlackService.add(addDTO);
                }
            }
        }
    }

    private List<String> getSourceCodeKeys(String outputClassName) {
        try {
            Object bean = ApplicationContextUtils.getBean(DmpHandlerUtils.dealBeanClass(outputClassName));

            if (bean != null) {
                try {
                    // 使用反射获取 getSourceCodeKeys 方法
                    Method method = bean.getClass().getDeclaredMethod("getSourceCodeKeys");

                    // 调用方法并获取返回值
                    Object result = method.invoke(bean);

                    return (List<String>) result;

                } catch (NoSuchMethodException e) {
                    throw new ServiceException("没有找到方法: getSourceCodeKeys");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                throw new ServiceException("没有找到" + outputClassName + "的Bean方法");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * 勾选添加黑名单
     * @param dto
     */
    private void checkAddBlack(DmpOutputTaskRecordDTO.AddOutputBlackDTO dto) {
        //获取到需要加入黑名单的任务记录
        List<DmpOutputTaskRecordEntity> recordEntityList = this.lambdaQuery().in(DmpOutputTaskRecordEntity::getId, dto.getIds()).list();

        //根据输出任务记录id获取cfgOutputId
        List<DmpOutputTaskEntity> dmpOutputTaskEntityList = dmpOutputTaskService.lambdaQuery()
                .in(DmpOutputTaskEntity::getId, dto.getIds())
                .list();

        //获取输出配置
        List<String> cfgOutputIdList = dmpOutputTaskEntityList.stream().map(req -> req.getCfgOutputId()).distinct().collect(Collectors.toList());
        List<DmpCfgOutputEntity> cfgOutputEntityList = dmpCfgOutputService.lambdaQuery()
                .in(DmpCfgOutputEntity::getId, cfgOutputIdList)
                .list();

        //根据输出任务分组添加黑名单
        Map<String, List<DmpOutputTaskEntity>> outputTaskGroup = dmpOutputTaskEntityList.stream().collect(Collectors.groupingBy(DmpOutputTaskEntity::getCfgOutputId));
        for (Map.Entry<String, List<DmpOutputTaskEntity>> outputTaskGroupMap : outputTaskGroup.entrySet()) {

            //获取对应的输出配置
            String cfgOutputId = outputTaskGroupMap.getKey();
            DmpCfgOutputEntity dmpCfgOutputEntity = cfgOutputEntityList.stream().filter(req -> req.getId().equals(cfgOutputId)).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(dmpCfgOutputEntity)) {
                continue;
            }

            //获取配置的主键名
            List<String> sourceCodeKeys = getSourceCodeKeys(dmpCfgOutputEntity.getOutputClass());
            if (CollectionUtils.isEmpty(sourceCodeKeys)) {
                throw new ServiceException("没有找到" + dmpCfgOutputEntity.getOutputClass() + " 类的sourceCodeKeys方法返回值");
            }

            //获取到这个分组下的所有单号，根据单号添加黑名单
            List<String> sourceCodeList = recordEntityList.stream()
                    .filter(req -> req.getMainId().equals(outputTaskGroupMap.getKey()))
                    .map(DmpOutputTaskRecordEntity::getSourceCode)
                    .collect(Collectors.toList());

            //添加黑名单
            DmpCfgOutputBlackDTO.AddDTO addDTO = new DmpCfgOutputBlackDTO.AddDTO();
            addDTO.setCompareSign(QueryConditionEnum.EQ.getCompareCode());
            addDTO.setDataType(DmpCfgOutputBlackDataTypeEnum.STRING.getCode());
            addDTO.setFieldName(sourceCodeKeys.get(0));
            addDTO.setFieldValue(String.join(",", sourceCodeList));
            addDTO.setMainId(cfgOutputId);
            addDTO.setRemark(dto.getRemark());
            dmpCfgOutputBlackService.add(addDTO);
        }
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
