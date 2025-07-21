package com.erp.server.sys.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.model.sys.dto.DictGlobalAreaDTO;
import com.erp.model.sys.dto.KingdeeDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictGlobalAreaEntity;
import com.erp.model.sys.entity.ThirdpartyRefBusinessEntity;
import com.erp.model.sys.enums.KingdeeAssistDataTypeEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.server.sys.mapper.DictGlobalAreaMapper;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeGlobalAreaService;
import com.erp.server.sys.service.DictCountryService;
import com.erp.server.sys.service.DictGlobalAreaService;
import com.erp.server.sys.service.ThirdpartyRefBusinessService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SYS_GLOBAL_AREA;

/**
 * <p>
 * 区域表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-21
 */
@Service
public class DictGlobalAreaServiceImpl extends SuperServiceImpl<DictGlobalAreaMapper, DictGlobalAreaEntity> implements DictGlobalAreaService {

    @Resource
    private ThirdpartyRefBusinessService thirdpartyRefBusinessService;

    @Resource
    private SyncKingdeeGlobalAreaService syncKingdeeGlobalAreaService;

    @Resource
    private DictCountryService dictCountryService;

    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(DictGlobalAreaDTO.UpdateDTO dto) {
        String id = dto.getId();
        DictGlobalAreaEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException("区域不存在");
        }
        String regionName = dto.getRegionName();
        String oldRegionName=entity.getRegionName();
        if (regionName.equals(oldRegionName)) {
            return Boolean.TRUE;
        }
        entity.setRegionName(regionName);
        handleData(entity);
        Boolean updateResult = this.updateById(entity);
        if(updateResult){
            DmpPushTaskEntity pushTaskEntity = syncKingdeeGlobalAreaService.syncDataToKingdee(entity, SyncOperateEnum.OPERATE_APPROVE.getCode());
            //推送金蝶
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    dmpMqFeign.sendTask(Arrays.asList(pushTaskEntity));
                }
            });
        }

        return updateResult;
    }


    /**
     * 根据国家id获取地区信息
     *
     * @param countryIds
     * @return java.util.List<com.erp.model.sys.dto.DictGlobalAreaDTO.InfoDTO>
     * @author yl
     * @date 2023-05-15 11:39
     */
    @Override
    public List<DictGlobalAreaDTO.InfoDTO> listGlobalAreaByCountryIds(List<String> countryIds) {
        if (CollectionUtils.isEmpty(countryIds)) {
            return Collections.emptyList();
        }
        List<DictGlobalAreaDTO.InfoDTO> resultList = this.baseMapper.listByCountryIds(countryIds);
        return resultList;
    }

    /**
     * 根据区域ids获取地区列表
     *
     * @param ids
     * @return java.util.List<com.erp.model.sys.entity.DictGlobalAreaEntity>
     * @author yl
     * @date 2023-08-22 15:48
     */
    @Override
    public List<DictGlobalAreaEntity> listGlobalAreaByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(DictGlobalAreaEntity::getId, ids).list();
    }

    /**
     * 获取地区列表
     *
     * @return java.util.List<com.erp.model.sys.entity.DictGlobalAreaEntity>
     * @author Jim
     * @date 2023-09-19 09:48
     */
    @Override
    public List<DictGlobalAreaEntity> listGlobalArea() {
        return this.lambdaQuery().list();
    }

    /**
     * 初始化金蝶信息
     *
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean init() {
        Class<DictGlobalAreaEntity> AreaClass = DictGlobalAreaEntity.class;
        TableName tableName = AreaClass.getDeclaredAnnotation(TableName.class);
        //获取到表名
        String businessType = tableName.value();

        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BOS_ASSISTANTDATA_DETAIL.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        String areaCode = KingdeeAssistDataTypeEnum.AREA.getCode();
        //类别
        queryFilters.add(StrUtil.format(" FId.FNumber = {}", "'" + areaCode + "'"));
        //查询
        String fieldKeys = "FEntryID,FNumber,FDataValue,FId.FNumber,FParentId,FSeq";
        String filterStr = String.join(" and ", queryFilters);
        // 当前页数
        Integer pageIndex = 0;
        // 每次最多获取100条
        Integer pageSize = 1000;
        List<KingdeeDTO.AssistDTO> areaList = new ArrayList<>(20);
        Boolean dataSign = true;
        while (dataSign) {
            List<Map<String, Object>> result = apiUtils.queryList(filterStr, fieldKeys, pageSize, pageIndex, 0);
            if (result.size() < pageSize) {
                dataSign = false;
            }
            List<KingdeeDTO.AssistDTO> entityList = result.stream().map(obj ->
                    BeanUtil.toBean(obj, KingdeeDTO.AssistDTO.class)).collect(Collectors.toList());
            areaList.addAll(entityList);
            pageIndex++;
        }
        List<DictGlobalAreaEntity> saveOrUpdateList = new ArrayList<>(20);
        List<ThirdpartyRefBusinessEntity> thirdpartySaveList = new ArrayList<>(20);

        //数据库存在的
        List<DictGlobalAreaEntity> dbList = this.list();
        List<String> idList = dbList.stream().map(DictGlobalAreaEntity::getId).collect(Collectors.toList());

        List<ThirdpartyRefBusinessEntity> thirdpartyDbList = thirdpartyRefBusinessService.listByBusinessIds(idList);
        for (KingdeeDTO.AssistDTO item : areaList) {
            String kingdeeId = item.getKingdeeId();
            String kingdeeCode = item.getKingdeeCode();
            String name = item.getName();
            DictGlobalAreaEntity dbEntity = dbList.stream().filter(entity ->
                            entity.getKingdeeCode().equals(kingdeeCode)
                                    || entity.getRegionCode().equals(kingdeeCode) ).
                    findFirst().orElse(null);
            //表示没有
            if (Objects.isNull(dbEntity)) {
                DictGlobalAreaEntity addEntity = new DictGlobalAreaEntity();
                String id = kingdeeCode;
                addEntity.setKingdeeCode(kingdeeCode);
                addEntity.setRegionCode(kingdeeCode);
                addEntity.setRegionName(name);
                addEntity.setSubregionName(name);
                addEntity.setId(id);
                saveOrUpdateList.add(addEntity);
                ThirdpartyRefBusinessEntity refEntity = new ThirdpartyRefBusinessEntity();
                refEntity.setBusinessType(businessType);
                refEntity.setBusinessId(id);
                refEntity.setThirdpartyId(kingdeeId);
                thirdpartySaveList.add(refEntity);
            } else {
                //表示有
                if (!dbEntity.getRegionName().equals(name)
                        || !dbEntity.getRegionCode().equals(kingdeeCode)
                        || !dbEntity.getKingdeeCode().equals(kingdeeCode)) {
                    dbEntity.setKingdeeCode(kingdeeCode);
                    dbEntity.setRegionCode(kingdeeCode);
                    dbEntity.setRegionName(name);
                    dbEntity.setSubregionName(name);
                    saveOrUpdateList.add(dbEntity);
                }
                ThirdpartyRefBusinessEntity thirdpartyEntity = thirdpartyDbList.stream().filter(entity ->
                        entity.getBusinessId().equals(dbEntity.getId()) &&
                                kingdeeId.equals(entity.getThirdpartyId())
                ).findFirst().orElse(null);
                if (Objects.isNull(thirdpartyEntity)) {
                    ThirdpartyRefBusinessEntity refEntity = new ThirdpartyRefBusinessEntity();
                    refEntity.setBusinessType(businessType);
                    refEntity.setBusinessId(dbEntity.getId());
                    refEntity.setThirdpartyId(kingdeeId);
                    thirdpartySaveList.add(refEntity);
                }
            }
        }
        Boolean result = this.saveOrUpdateBatch(saveOrUpdateList);
        if (result) {
            thirdpartyRefBusinessService.saveBatch(thirdpartySaveList);
        }
        return result;
    }

    @Override
    public PagingVO<DictGlobalAreaDTO.PagingViewDTO> paging(PagingDTO<DictGlobalAreaDTO.PagingParamDTO> dto) {
        DictGlobalAreaDTO.PagingParamDTO paramDTO = dto.getParams();
        paramDTO.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<DictGlobalAreaDTO.PagingViewDTO> pageData = this.baseMapper.paging(query, paramDTO);
        return new PagingVO(pageData);

    }

    @Override
    public DictGlobalAreaDTO.ViewDTO view(String id) {
        DictGlobalAreaDTO.ViewDTO viewDTO=new DictGlobalAreaDTO.ViewDTO();
        DictGlobalAreaEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException("区域不存在");
        }
        BeanMapper.copy(entity,viewDTO);
        return viewDTO;
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        DictGlobalAreaEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException("区域不存在");
        }
        List<DictCountryEntity> countryList = dictCountryService.listByRegionCode(entity.getRegionCode());
        if(CollectionUtils.isNotEmpty(countryList)){
            throw new ServiceException("区域下存在国家，无法删除");
        }
        //删除记录
        baseMapper.deleteById(id);
        //金蝶推送
        DmpPushTaskEntity pushTaskEntity = syncKingdeeGlobalAreaService.syncDataToKingdee(entity, SyncOperateEnum.OPERATE_DELETE.getCode());
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(Arrays.asList(pushTaskEntity));
            }
        });
        thirdpartyRefBusinessService.removeByBusinessId(id);
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.DELETE);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addGlobalArea(DictGlobalAreaDTO.AddDTO dto) {
        DictGlobalAreaEntity entity = new DictGlobalAreaEntity();
        String regionName = dto.getRegionName();
        String code = dto.getKingdeeCode();
        entity.setRegionName(regionName);
        entity.setSubregionName(regionName);
        entity.setRegionCode(code);
        entity.setKingdeeCode(code);
        handleData(entity);
        entity.setId(code);
        Boolean addResult = this.save(entity);
        if(addResult){
            DmpPushTaskEntity pushTaskEntity = syncKingdeeGlobalAreaService.syncDataToKingdee(entity, SyncOperateEnum.OPERATE_APPROVE.getCode());
            //推送金蝶
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    dmpMqFeign.sendTask(Arrays.asList(pushTaskEntity));
                }
            });
        }

        return addResult;
    }

    private void handleData(DictGlobalAreaEntity entity) {
        String id = entity.getId();
        String kingdeeCode = entity.getKingdeeCode().trim();
        String name = entity.getRegionName().trim();
        int codeCount = this.lambdaQuery().ne(StringUtils.isNotBlank(id), DictGlobalAreaEntity::getId, id).
                eq(DictGlobalAreaEntity::getKingdeeCode, kingdeeCode).count();
        if (codeCount > 0) {
            throw new ServiceException("区域金蝶编码已存在");
        }
        int nameCount = this.lambdaQuery().ne(StringUtils.isNotBlank(id), DictGlobalAreaEntity::getId, id).
                eq(DictGlobalAreaEntity::getRegionName, name).count();
        if (nameCount > 0) {
            throw new ServiceException("区域名已存在");
        }
        entity.setKingdeeCode(kingdeeCode);
        entity.setRegionName(name);
    }

    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId, String syncKingdeeCode) {
        Boolean result= false;
        if(StringUtils.isNotBlank(syncKingdeeCode)){
            result= this.lambdaUpdate()
                    .eq(DictGlobalAreaEntity::getId, id)
                    .set(StringUtils.isNotBlank(syncKingdeeCode), DictGlobalAreaEntity::getKingdeeCode, syncKingdeeCode)
                    .update();
        }

        ThirdpartyRefBusinessEntity refBusinessEntity = thirdpartyRefBusinessService.getByBusinessId(id);
        if (Objects.isNull(refBusinessEntity)) {
            Class<DictGlobalAreaEntity> areaClass = DictGlobalAreaEntity.class;
            TableName tableName = areaClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String businessType = tableName.value();
            ThirdpartyRefBusinessEntity refEntity = new ThirdpartyRefBusinessEntity();
            refEntity.setBusinessType(businessType);
            refEntity.setBusinessId(id);
            refEntity.setThirdpartyId(syncKingdeeId);
            thirdpartyRefBusinessService.save(refEntity);
        }else{
            String thirdpartyId = refBusinessEntity.getThirdpartyId();
            if(!syncKingdeeId.equals(thirdpartyId)){
                refBusinessEntity.setThirdpartyId(syncKingdeeId);
                thirdpartyRefBusinessService.updateById(refBusinessEntity);
            }
        }

        return result;
    }

    @Override
    public void exportList(DictGlobalAreaDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("区域Excel导出", EXPORT_SYS_GLOBAL_AREA.getCode(), dto);
    }

    @Override
    public PagingVO<DictGlobalAreaDTO.PagingViewDTO> exportGlobalArea(PagingDTO<DictGlobalAreaDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<DictGlobalAreaDTO.PagingViewDTO> page = this.baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        return new PagingVO<>(page);
    }


}
