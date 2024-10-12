package com.erp.server.dmp.service.impl;


import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.*;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncMqDTO.SyncParamDetailDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.constant.DmpConstant;
import com.erp.model.dmp.dto.DmpCfgOutputBlackDTO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.DmpCfgOutputBlackCompareSignEnum;
import com.erp.model.dmp.enums.DmpCfgOutputBlackDataTypeEnum;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.model.dmp.enums.DmpPushMonitorTabEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.dmp.inout.handler.output.task.DmpOutputTaskHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private DmpCfgInputConvertService dmpCfgInputConvertService;

    @Resource
    private DmpCfgOutputService dmpCfgOutputService;
    
    @Autowired
	private DmpHandlerCache dmpHandlerCache;
    
    @Autowired
	private DmpPushMsgService dmpPushMsgService;

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
        result.add(all);

        //待推送
        DmpOutputTaskRecordDTO.TabListDTO inif = new DmpOutputTaskRecordDTO.TabListDTO();
        inif.setTabFlag(DmpPushMonitorTabEnum.INIT.getCode());
        int inifCount = countList.stream().filter(a -> a.getTabFlag().equals(DmpOutputTaskRecordStatusEnum.INIT.getCode())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        inif.setCount(inifCount);
        result.add(inif);


        //推送中
        DmpOutputTaskRecordDTO.TabListDTO pushIng = new DmpOutputTaskRecordDTO.TabListDTO();
        pushIng.setTabFlag(DmpPushMonitorTabEnum.PUSH_ING.getCode());
        int pushIngCount = countList.stream().filter(a -> a.getTabFlag().equals(DmpOutputTaskRecordStatusEnum.MQSUCCESS.getCode())
                || DmpOutputTaskRecordStatusEnum.MQERROR.getCode().equals(a.getTabFlag())
                || DmpOutputTaskRecordStatusEnum.COSUMERERROR.getCode().equals(a.getTabFlag())
        ).map(req -> req.getCount()).reduce(MathUtil.ZERO, Integer::sum);
        pushIng.setCount(pushIngCount);
        result.add(pushIng);

        //同步失败
        DmpOutputTaskRecordDTO.TabListDTO failed = new DmpOutputTaskRecordDTO.TabListDTO();
        failed.setTabFlag(DmpPushMonitorTabEnum.ERROR.getCode());
        int failedCount = countList.stream().filter(a -> a.getTabFlag().equals(DmpOutputTaskRecordStatusEnum.ERROR.getCode())
        ).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        failed.setCount(failedCount);
        result.add(failed);

        //同步成功
        DmpOutputTaskRecordDTO.TabListDTO finish = new DmpOutputTaskRecordDTO.TabListDTO();
        finish.setTabFlag(DmpPushMonitorTabEnum.FINISH.getCode());
        int finishCount = countList.stream().filter(a -> DmpOutputTaskRecordStatusEnum.FINISH.getCode().equals(a.getTabFlag())
        ).mapToInt(DmpOutputTaskRecordDTO.TabListDTO::getCount).sum();
        finish.setCount(finishCount);
        result.add(finish);

        //无需同步
        Integer count = this.lambdaQuery()
                .eq(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
                .eq(DmpOutputTaskRecordEntity::getIsNeedSync, Boolean.FALSE)
                .count();
        DmpOutputTaskRecordDTO.TabListDTO noNeedSync = new DmpOutputTaskRecordDTO.TabListDTO();
        noNeedSync.setTabFlag(DmpPushMonitorTabEnum.NO_NEED_SYNC.getCode());
        noNeedSync.setCount(count);
        result.add(noNeedSync);

        DmpOutputTaskRecordDTO.PagingParamDTO params = new DmpOutputTaskRecordDTO.PagingParamDTO();
        Map<String,String> sqlMap = new HashMap<>();
        sqlMap.put("default", "1 = 1");
        params.setSqlMap(sqlMap);
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(1, -1);
        IPage blackPaging = baseMapper.blackPaging(query, params);
        //黑名单
        Integer blackCount = blackPaging.getRecords().size();
        DmpOutputTaskRecordDTO.TabListDTO black = new DmpOutputTaskRecordDTO.TabListDTO();
        black.setTabFlag(DmpPushMonitorTabEnum.BLACK.getCode());
        black.setCount(blackCount);
        result.add(black);

        return result;
    }

    @Override
    public PagingVO<DmpOutputTaskRecordDTO.PagingDTO> paging(PagingDTO<DmpOutputTaskRecordDTO.PagingParamDTO> dto) {
        DmpOutputTaskRecordDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        List<AdvanceQueryDTO> advanceQueryDTOList = params.getAdvanceQueryDTOList();
        IPage pageData = null;
        if(CollUtil.isNotEmpty(advanceQueryDTOList)) {
        	if(advanceQueryDTOList.stream().anyMatch(a -> a.getField().equals("tab") && "black".equals(a.getValue()))) {
        		String sql = params.getSqlMap().get("default");
        		if(StringUtils.isNotBlank(sql)) {
        			params.getSqlMap().put("default", sql.replace("t.source_code", "dcob.field_value"));
        		}
        		pageData = baseMapper.blackPaging(query, params);
        	}else {
        		pageData = baseMapper.paging(query, params);
        	}
        }
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
            } else {
                listDTO.setStatusName(DmpOutputTaskRecordStatusEnum.getName(listDTO.getStatus()));
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

        //校验是否存在黑名单
//        checkExistsBlack(ids);

        return this.lambdaUpdate()
                .set(DmpOutputTaskRecordEntity::getIsNeedSync, Boolean.FALSE)
                .set(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
                .in(DmpOutputTaskRecordEntity::getId, ids)
                .update();
    }

    /**
     * 校验是否存在黑名单
     * @param ids
     */
    private void checkExistsBlack(List<String> ids) {
        List<DmpOutputTaskRecordEntity> list = this.lambdaQuery().in(DmpOutputTaskRecordEntity::getId, ids).list();

        List<String> outputTaskIds = list.stream().map(req -> req.getMainId()).distinct().collect(Collectors.toList());

        List<DmpCfgOutputBlackEntity> blackEntityList = dmpCfgOutputBlackService.lambdaQuery().in(DmpCfgOutputBlackEntity::getMainId, outputTaskIds).list();

        for (DmpOutputTaskRecordEntity recordEntity : list) {
            DmpCfgOutputBlackEntity dmpCfgOutputBlackEntity = blackEntityList.stream()
                    .filter(req -> req.getMainId().equals(recordEntity.getMainId())
                            && req.getFieldValue().equals(recordEntity.getSourceCode())
                    ).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(dmpCfgOutputBlackEntity)) {
                throw new ServiceException("单据【" + recordEntity.getSourceCode() + "】已存在黑名单，禁止操作!");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelOutputBlack(String id) {
    	DmpCfgOutputBlackEntity entity = dmpCfgOutputBlackService.getById(id);
        dmpCfgOutputBlackService.lambdaUpdate()
                    .eq(DmpCfgOutputBlackEntity::getId, id)
                    .remove();

        return BatchResultDTO.success(entity.getId(), entity.getFieldValue(), OperationTypeEnum.DELETE);

    }

    /**
     * 自定义条件方式添加黑名单
     *
     * @param dto
     */
    private void customizeBlack(DmpOutputTaskRecordDTO.AddOutputBlackDTO dto) {
        DmpOutputTaskRecordDTO.CustomizeBlackParam params = dto.getParams();

        //根据dmp_cfg_input表id查询输入配置转换表
        List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList = dmpCfgInputConvertService.lambdaQuery()
                .eq(DmpCfgInputConvertEntity::getMainId, params.getBillTypeId())
                .list();
        if(CollUtil.isEmpty(dmpCfgInputConvertEntityList)) {
        	return;
        }
        //根据系统id查询输出任务
        List<DmpCfgOutputEntity> dmpCfgOutputEntityList = dmpCfgOutputService.lambdaQuery()
        		.in(DmpCfgOutputEntity::getInputConvertId, dmpCfgInputConvertEntityList.stream().map(DmpCfgInputConvertEntity::getId).collect(Collectors.toList()))
                .eq(DmpCfgOutputEntity::getSystemId, params.getTargetPlatformCode())
                .list();
        for (DmpCfgOutputEntity dmpCfgOutputEntity : dmpCfgOutputEntityList) {

            //获取输出任务配置的主键字段
            List<String> codeKeyList = getSourceCodeKeys(dmpCfgOutputEntity.getOutputClass());
            if (CollectionUtils.isNotEmpty(codeKeyList)) {

                for (String sourceCode : params.getSourceCodeList()) {
                    //查询黑名单用于校验是否存在，避免重复添加
                    checkOutpuBlackExist(dmpCfgOutputEntity.getId(), codeKeyList, sourceCode);

                    //添加黑名单
                    DmpCfgOutputBlackDTO.AddDTO addDTO = new DmpCfgOutputBlackDTO.AddDTO();
                    addDTO.setCompareSign(QueryConditionEnum.EQ.getCompareCode());
                    addDTO.setDataType(DmpCfgOutputBlackDataTypeEnum.STRING.getCode());
                    addDTO.setFieldName(codeKeyList.get(0));
                    addDTO.setFieldValue(sourceCode);
                    addDTO.setMainId(dmpCfgOutputEntity.getId());
                    addDTO.setRemark(dto.getRemark());
                    dmpCfgOutputBlackService.add(addDTO);
                }

            }
        
        }
    }

    private List<String> getSourceCodeKeys(String outputClassName) {
        Object bean = ApplicationContextUtils.getBean(DmpHandlerUtils.dealBeanClass(outputClassName));
        if (bean != null) {
            try {
            	// 使用反射获取 getSourceCodeKeys 方法
            	Method method = ReflectUtil.getMethodByName(bean.getClass(), "getSourceCodeKeys");
                method.setAccessible(Boolean.TRUE);
                // 调用方法并获取返回值
                Object result = method.invoke(bean);
                return (List<String>) result;
            }catch (Exception e) {
            	log.error("{}没有找到方法: getSourceCodeKeys" , outputClassName , e);
                throw new ServiceException(outputClassName + "没有找到方法: getSourceCodeKeys");
            }
        } else {
            throw new ServiceException("没有找到" + outputClassName + "的Bean方法");
        }
    }

    /**
     * 勾选添加黑名单
     *
     * @param dto
     */
    private void checkAddBlack(DmpOutputTaskRecordDTO.AddOutputBlackDTO dto) {
        //获取到需要加入黑名单的任务记录
        List<DmpOutputTaskRecordEntity> recordEntityList = this.lambdaQuery().in(DmpOutputTaskRecordEntity::getId, dto.getIds()).list();

        //根据输出任务记录id获取cfgOutputId
        List<String> dmpOutputTaskIds = recordEntityList.stream().map(req -> req.getMainId()).distinct().collect(Collectors.toList());
        List<DmpOutputTaskEntity> dmpOutputTaskEntityList = dmpOutputTaskService.lambdaQuery()
                .in(DmpOutputTaskEntity::getId, dmpOutputTaskIds)
                .list();

        //获取输出配置
        List<String> cfgOutputIdList = dmpOutputTaskEntityList.stream().map(req -> req.getCfgOutputId()).distinct().collect(Collectors.toList());
        List<DmpCfgOutputEntity> cfgOutputEntityList = dmpCfgOutputService.lambdaQuery()
                .in(DmpCfgOutputEntity::getId, cfgOutputIdList)
                .list();

        for (DmpOutputTaskRecordEntity recordEntity : recordEntityList) {

            DmpOutputTaskEntity dmpOutputTaskEntity = dmpOutputTaskEntityList.stream().filter(req -> req.getId().equals(recordEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(dmpOutputTaskEntity)) {
                return;
            }

            String cfgOutputId = dmpOutputTaskEntity.getCfgOutputId();
            DmpCfgOutputEntity dmpCfgOutputEntity = cfgOutputEntityList.stream().filter(req -> req.getId().equals(cfgOutputId)).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(dmpCfgOutputEntity)) {
                continue;
            }

            //获取配置的主键名
            List<String> sourceCodeKeys = getSourceCodeKeys(dmpCfgOutputEntity.getOutputClass());
            if (CollectionUtils.isEmpty(sourceCodeKeys)) {
                throw new ServiceException("没有找到" + dmpCfgOutputEntity.getOutputClass() + " 类的sourceCodeKeys方法返回值");
            }

            //查询黑名单用于校验是否存在，避免重复添加
            checkOutpuBlackExist(cfgOutputId, sourceCodeKeys, recordEntity.getSourceCode());

            //添加黑名单
            DmpCfgOutputBlackDTO.AddDTO addDTO = new DmpCfgOutputBlackDTO.AddDTO();
            addDTO.setCompareSign(QueryConditionEnum.EQ.getCompareCode());
            addDTO.setDataType(DmpCfgOutputBlackDataTypeEnum.STRING.getCode());
            addDTO.setFieldName(sourceCodeKeys.get(0));
            addDTO.setFieldValue(recordEntity.getSourceCode());
            addDTO.setMainId(cfgOutputId);
            addDTO.setRemark(dto.getRemark());
            dmpCfgOutputBlackService.add(addDTO);

        }
    }

    /**
     * 校验黑名单是否存在
     *
     * @param cfgOutputId
     * @param sourceCodeKeys
     * @param sourceCode
     */
    private void checkOutpuBlackExist(String cfgOutputId, List<String> sourceCodeKeys, String sourceCode) {
        //查询黑名单用于校验是否存在，避免重复添加
        List<DmpCfgOutputBlackEntity> list = dmpCfgOutputBlackService.lambdaQuery()
                .eq(DmpCfgOutputBlackEntity::getMainId, cfgOutputId)
                .eq(DmpCfgOutputBlackEntity::getFieldName, sourceCodeKeys.get(0))
                .eq(DmpCfgOutputBlackEntity::getFieldValue, sourceCode)
                .list();
        if (CollectionUtils.isNotEmpty(list)) {
            throw new ServiceException("单据【"+sourceCode+"】已在黑名单存在，请不要重复添加！");
        }
    }

    @Override
    public PagingVO<DmpOutputTaskRecordDTO.PagingDTO> exportNewDmpPushTask(PagingDTO<DmpOutputTaskRecordDTO.ExpotParamDTO> dto) {
        Page<DmpOutputTaskRecordDTO.PagingDTO> page = baseMapper.listExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            //数据处理
            doOpHandleDmpPushTask(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    @Override
    public Boolean batchSync(List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList) {
        if (CollectionUtils.isEmpty(dmpOutputTaskRecordEntityList)) {
            return false;
        }

        Map<String, String> cfgOutputIdEntityMaps = dmpOutputTaskService.lambdaQuery()
                .in(DmpOutputTaskEntity::getId, dmpOutputTaskRecordEntityList.stream().map(DmpOutputTaskRecordEntity::getMainId).collect(Collectors.toSet()))
                .select(DmpOutputTaskEntity::getId, DmpOutputTaskEntity::getCfgOutputId)
                .list().stream().collect(Collectors.toMap(DmpOutputTaskEntity::getId, DmpOutputTaskEntity::getCfgOutputId));

        Map<String, DmpCfgOutputEntity> outputIdEntityMaps = dmpCfgOutputService.lambdaQuery().in(DmpCfgOutputEntity::getId, cfgOutputIdEntityMaps.values())
                .list().stream().collect(Collectors.toMap(DmpCfgOutputEntity::getId, d -> d));

        Map<String, List<DmpOutputTaskRecordEntity>> cfgOutputRecordEntityListMaps = new HashMap<>();
        for (DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity : dmpOutputTaskRecordEntityList) {
            String cfgOutputId = cfgOutputIdEntityMaps.get(dmpOutputTaskRecordEntity.getMainId());
            List<DmpOutputTaskRecordEntity> list = cfgOutputRecordEntityListMaps.get(cfgOutputId);
            if (CollUtil.isEmpty(list)) {
                list = new ArrayList<>();
            }
            list.add(dmpOutputTaskRecordEntity);
            cfgOutputRecordEntityListMaps.put(cfgOutputId, list);
        }
        for (Map.Entry<String, List<DmpOutputTaskRecordEntity>> cfgOutputRecordEntityListMap : cfgOutputRecordEntityListMaps.entrySet()) {
            DmpCfgOutputEntity dmpCfgOutputEntity = outputIdEntityMaps.get(cfgOutputRecordEntityListMap.getKey());
            DmpOutputTaskHandler dmpOutputTaskHandler = ApplicationContextUtils.getBean(DmpHandlerUtils.dealBeanClass(dmpCfgOutputEntity.getOutputClass()), DmpOutputTaskHandler.class);
            dmpOutputTaskHandler.dealDmpOutputTaskRecordEntityList(dmpCfgOutputEntity, cfgOutputRecordEntityListMap.getValue());
        }
        return Boolean.TRUE;
    }
    
    @Override
    public List<DmpOutputTaskRecordEntity> erpQuerySync(DmpCfgOutputEntity dmpCfgOutputEntity , List<DmpOutputTaskRecordEntity> list) {
    	String inputConvertId = dmpCfgOutputEntity.getInputConvertId();
		String cfgInputId = dmpHandlerCache.getDmpCfgInputConvertEntityList(d -> d.getId().equals(inputConvertId)).get(0).getMainId();
		DmpCfgInputEntity dmpCfgInputEntity = dmpHandlerCache.getDmpCfgInputEntityList(d -> d.getId().equals(cfgInputId)).get(0);
		
    	List<String> dataIds = list.stream().map(DmpOutputTaskRecordEntity::getDataId).collect(Collectors.toList());
		List<String> ids = list.stream().map(DmpOutputTaskRecordEntity::getId).collect(Collectors.toList());
    	String extendJson = dmpCfgInputEntity.getExtendJson();
		JSONObject parseObject = JSON.parseObject(extendJson);
		String system = parseObject.getString("system");
		String apiType = dmpHandlerCache.getDmpCfgApiEntityList(d -> d.getId().equals(dmpCfgInputEntity.getTypeId())).get(0).getApiType();
		
		List<DmpPushMsgEntity> dmpPushMsgEntityList = dmpPushMsgService.listByIds(dataIds);
		DmpSyncMqDTO.SyncParamDTO syncParamDTO = new DmpSyncMqDTO.SyncParamDTO();
		syncParamDTO.setSourceType(SourceTypeEnum.getEnum(apiType));
		List<SyncParamDetailDTO> sourceDetailList = new ArrayList<>();
		for(DmpPushMsgEntity dmpPushMsgEntity : dmpPushMsgEntityList) {
			SyncParamDetailDTO syncParamDetailDTO = new SyncParamDetailDTO();
			syncParamDetailDTO.setSourceId(dmpPushMsgEntity.getSourceId());
			syncParamDetailDTO.setSyncOperate(dmpPushMsgEntity.getSyncOperate());
			syncParamDetailDTO.setDataId(dmpPushMsgEntity.getId());
			sourceDetailList.add(syncParamDetailDTO);
		}
		syncParamDTO.setSourceDetailList(sourceDetailList);
		String outputSystemId = dmpCfgOutputEntity.getSystemId();
		String outputSystemCode = dmpHandlerCache.getDmpBasicSystemEntityList(d -> d.getId().equals(outputSystemId)).get(0).getCode();
		if(DmpBasicSystemCodeEnum.WDT.getCode().equals(outputSystemCode)) {
			try {
				FeignQuery.invoke("com.erp.server."+ system +".service.impl.SyncTaskServiceImpl", "findWdtDataSendSyncTask", Arrays.asList(syncParamDTO));
				this.lambdaUpdate()
					.in(DmpOutputTaskRecordEntity::getId, ids)
					.eq(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.ERROR.getCode())
					.set(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.FINISH.getCode())
					.set(DmpOutputTaskRecordEntity::getIsNeedSync, false)
					.update();
			} catch (Exception e) {
				log.error("查询同步调用erp服务报错" , e);
			}
		}else {
			Map<String, Map<String, Object>> invoke = FeignQuery.invoke(Map.class , "com.erp.server."+ system +".service.impl.SyncTaskServiceImpl", "newFindDataSendSyncTask", Arrays.asList(syncParamDTO));
			if(invoke != null) {
				Map<String, List<DmpOutputTaskRecordEntity>> dataIdOutputMaps = list.stream().collect(Collectors.groupingBy(DmpOutputTaskRecordEntity::getDataId));
				List<DmpOutputTaskRecordEntity> allUpdateList = new ArrayList<>();
				for(Map.Entry<String, Map<String, Object>> i : invoke.entrySet()) {
					List<DmpOutputTaskRecordEntity> updateList = dataIdOutputMaps.get(i.getKey());
					if(CollUtil.isNotEmpty(updateList)) {
						String requestData = JSON.toJSONString(i.getValue());
						this.lambdaUpdate()
							.in(DmpOutputTaskRecordEntity::getId, updateList.stream().map(DmpOutputTaskRecordEntity::getId).collect(Collectors.toList()))
							.set(DmpOutputTaskRecordEntity::getRequestData, requestData)
							.update();
						updateList.forEach(u -> u.setRequestData(requestData));
						allUpdateList.addAll(updateList);
					}
				}
				return allUpdateList;
			}
		}
		return new ArrayList<>();
    }
}
