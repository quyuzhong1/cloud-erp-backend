package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
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
import com.erp.model.sys.dto.DictCityDTO;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.ThirdpartyRefBusinessEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.sys.mapper.DictCityMapper;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeCityService;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeProvinceService;
import com.erp.server.sys.service.DictCityService;
import com.erp.server.sys.service.DictCountryService;
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

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SYS_CITY;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_SYS_CITY_PROVINCE;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-21
 */
@Service
public class DictCityServiceImpl extends SuperServiceImpl<DictCityMapper, DictCityEntity> implements DictCityService {


    private String province = "province";

    private String city = "city";


    @Resource
    private SyncKingdeeProvinceService syncKingdeeProvinceService;

    @Resource
    private SyncKingdeeCityService syncKingdeeCityService;

    @Resource
    private DictCountryService dictCountryService;

    @Resource
    private ThirdpartyRefBusinessService thirdpartyRefBusinessService;

    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;



    /**
     * 获取省城市
     *
     * @param countryCode
     * @return java.util.List<com.erp.model.sys.dto.DictCityDTO.ListDTO>
     * @author yl
     * @date 2023-05-11 16:30
     */
    @Override
    public List<DictCityDTO.ListDTO> listCity(String countryCode) {

        //根据 国家code 获取城市信息
        List<DictCityEntity> allList = this.listByCountryCode(countryCode);
        List<DictCityDTO.ListDTO> flagList = BeanMapper.copyList(allList, DictCityDTO.ListDTO.class);
        List<DictCityDTO.ListDTO> treeList = flagList.stream().
                filter(item -> "0".equals(item.getParentId())).
                map(obj -> {
                    obj.setChildrenList(getChildren(obj, flagList));
                    return obj;
                }).collect(Collectors.toList());

        return treeList;
    }

    @Override
    public List<DictCityEntity> listByIdList(List<String> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return Collections.emptyList();
        }
        return baseMapper.listByIdList(idList);
    }

    @Override
    public DictCityEntity getReginByName(String reginName,Integer level) {
        List<DictCityEntity> dictCityEntities = this.lambdaQuery().eq(DictCityEntity::getName, reginName).eq(DictCityEntity :: getLevel ,level).list();
        if(dictCityEntities.size() > 1){
            //查到多个，特殊判断，白云区取广州的,其他的返回null，避免设置错误
            if(reginName.equals("白云区")){
                List<String> parentIds = dictCityEntities.stream().map(DictCityEntity :: getParentId).collect(Collectors.toList());
                List<DictCityEntity> parentEntiyList =  listByIdList(parentIds);
                Optional<DictCityEntity> guangzhouEntity = parentEntiyList.stream().filter(v->v.getName().equals("广州")).findFirst();
                if(guangzhouEntity.isPresent()){
                    String guangzhouId = guangzhouEntity.get().getId();
                    return dictCityEntities.stream().filter(v->v.getParentId().equals(guangzhouId)).findFirst().orElse(null);
                }
            }
            return null;
        }else {
            return dictCityEntities.isEmpty() ? null : dictCityEntities.get(0);
        }
    }


    @Override
    public PagingVO<DictCityDTO.PagingViewDTO> provincePaging(PagingDTO<DictCityDTO.ProvincePagingParamDTO> dto) {
        DictCityDTO.ProvincePagingParamDTO paramDTO = dto.getParams();
        paramDTO.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<DictCityDTO.PagingViewDTO> pageData = this.baseMapper.provincePaging(query, paramDTO);
        return new PagingVO(pageData);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addProvince(DictCityDTO.AddProvinceDTO dto) {
        DictCityEntity addEntity = new DictCityEntity();
        String code = dto.getCode();
        addEntity.setKingdeeCode(code);
        addEntity.setName(dto.getName());
        addEntity.setCode(code);
        addEntity.setType(province);
        addEntity.setCountryCode(dto.getParentId());
        addEntity.setParentId("0");
        addEntity.setLevel(1);
        handleData(addEntity);
        Boolean addResult = this.save(addEntity);
        if (addResult) {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeProvinceService.syncDataToKingdee(addEntity, SyncOperateEnum.OPERATE_APPROVE.getCode());
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

    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId, String syncKingdeeCode) {
        Boolean result = false;
        if (StringUtils.isNotBlank(syncKingdeeCode)) {
            result = this.lambdaUpdate()
                    .eq(DictCityEntity::getId, id)
                    .set(StringUtils.isNotBlank(syncKingdeeCode), DictCityEntity::getKingdeeCode, syncKingdeeCode)
                    .update();
        }
        ThirdpartyRefBusinessEntity refBusinessEntity = thirdpartyRefBusinessService.getByBusinessId(id);
        if (Objects.isNull(refBusinessEntity)) {
            Class<DictCityEntity> AreaClass = DictCityEntity.class;
            TableName tableName = AreaClass.getDeclaredAnnotation(TableName.class);
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
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateProvince(DictCityDTO.UpdateProvinceDTO dto) {
        String id = dto.getId();
        DictCityEntity entity=this.getById(id);
        if(Objects.isNull(entity)){
          throw new ServiceException("省份不存在");
        }
        entity.setName(dto.getName());
        entity.setCountryCode(dto.getParentId());
        handleData(entity);
        Boolean updateResult = this.updateById(entity);
        if (updateResult) {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeProvinceService.syncDataToKingdee(entity, SyncOperateEnum.OPERATE_APPROVE.getCode());
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

    @Override
    public BatchResultDTO delete(String id) {
        DictCityEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException("省份不存在");
        }
        int count = this.lambdaQuery().eq(DictCityEntity::getParentId, id).count();
        if (count > 0) {
            throw new ServiceException("存在下级，无法删除");
        }
        baseMapper.deleteById(id);
        //金蝶推送
        DmpPushTaskEntity pushTaskEntity = syncKingdeeCityService.syncDataToKingdee(entity, SyncOperateEnum.OPERATE_DELETE.getCode());
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
    public DictCityDTO.ViewDTO provinceView(String id) {
        DictCityDTO.ViewDTO viewDTO=new DictCityDTO.ViewDTO();
        DictCityEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException("省份不存在");
        }
        viewDTO.setId(id);
        viewDTO.setName(entity.getName());
        viewDTO.setParentId(entity.getCountryCode());
        viewDTO.setCode(entity.getKingdeeCode());
        DictCountryEntity country = dictCountryService.getById(entity.getCountryCode());
        if(Objects.nonNull(country)){
            viewDTO.setParentName(country.getNameCn());
        }
        return viewDTO;
    }

    /**
     * 城市分页
     * @param dto
     * @return
     */
    @Override
    public PagingVO<DictCityDTO.PagingViewDTO> cityPaging(PagingDTO<DictCityDTO.CityPagingParamDTO> dto) {
        DictCityDTO.CityPagingParamDTO paramDTO = dto.getParams();
        paramDTO.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<DictCityDTO.PagingViewDTO> pageData = this.baseMapper.cityPaging(query, paramDTO);
        return new PagingVO(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addCity(DictCityDTO.AddCityDTO dto) {
        DictCityEntity addEntity = new DictCityEntity();
        //省id
        String provinceId = dto.getParentId();
        DictCityEntity province = this.getById(provinceId);
        if (Objects.isNull(province)) {
            throw new ServiceException("上级省不存在");
        }
        String code = dto.getCode();
        addEntity.setKingdeeCode(code);
        addEntity.setName(dto.getName());
        addEntity.setCode(code);
        addEntity.setType(city);
        addEntity.setCountryCode(province.getCountryCode());
        addEntity.setParentId(provinceId);
        addEntity.setLevel(2);
        handleData(addEntity);
        Boolean addResult = this.save(addEntity);
        if (addResult) {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeCityService.syncDataToKingdee(addEntity, SyncOperateEnum.OPERATE_APPROVE.getCode());
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

    /**
     * 城市详情
     * @param id
     * @return
     */
    @Override
    public DictCityDTO.ViewDTO cityView(String id) {
        DictCityEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException("城市不存在");
        }
        //省id
        String provinceId = entity.getParentId();
        DictCityEntity province = this.getById(provinceId);
        if (Objects.isNull(province)) {
            throw new ServiceException("上级省不存在");
        }
        DictCityDTO.ViewDTO viewDTO=new DictCityDTO.ViewDTO();
        viewDTO.setId(id);
        viewDTO.setName(entity.getName());
        viewDTO.setParentId(provinceId);
        viewDTO.setParentName(province.getName());
        viewDTO.setCode(entity.getKingdeeCode());
        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateCity(DictCityDTO.UpdateCityDTO dto) {
        String id = dto.getId();
        DictCityEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException("城市不存在");
        }

        //省id
        String provinceId = dto.getParentId();
        DictCityEntity province = this.getById(provinceId);
        if (Objects.isNull(province)) {
            throw new ServiceException("上级省不存在");
        }

        entity.setName(dto.getName());
        entity.setParentId(provinceId);
        handleData(entity);
        Boolean updateResult = this.updateById(entity);
        if (updateResult) {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeCityService.syncDataToKingdee(entity, SyncOperateEnum.OPERATE_APPROVE.getCode());
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

    @Override
    public void provinceExport(DictCityDTO.ProvincePagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("省份Excel导出", EXPORT_SYS_CITY_PROVINCE.getCode(), dto);
    }

    @Override
    public void cityExport(DictCityDTO.ProvincePagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("城市Excel导出", EXPORT_SYS_CITY.getCode(), dto);
    }

    @Override
    public List<DictCityEntity> listProvince() {
        return this.lambdaQuery().eq(DictCityEntity::getType, province).
                eq(DictCityEntity::getParentId,"0")
                .list();
    }

    @Override
    public PagingVO<DictCityDTO.PagingViewDTO> exportCity(PagingDTO<DictCityDTO.ProvincePagingParamDTO> dto) {
        Page<DictCityDTO.PagingViewDTO> page = this.baseMapper.cityExport(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        return new PagingVO<>(page);
    }

    @Override
    public PagingVO<DictCityDTO.PagingViewDTO> exportCityProvince(PagingDTO<DictCityDTO.ProvincePagingParamDTO> dto) {
        Page<DictCityDTO.PagingViewDTO> page = this.baseMapper.provinceExport(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        return new PagingVO<>(page);
    }

    @Override
    public List<DictCityEntity> listByNames(List<String> names) {
        return lambdaQuery().in(DictCityEntity::getName,names).list();
    }

    public void handleData(DictCityEntity entity) {
        String id = entity.getId();
        Integer level = entity.getLevel();
        int codeCount = this.lambdaQuery().
                eq(DictCityEntity::getKingdeeCode, entity.getKingdeeCode()).
                eq(DictCityEntity::getLevel, level).
                ne(StringUtils.isNotBlank(id), DictCityEntity::getId,id).
                count();
        if (codeCount > 0) {
            throw new ServiceException("code已存在");
        }
        int nameCount = this.lambdaQuery().eq(DictCityEntity::getName, entity.getName()).
                eq(DictCityEntity::getLevel, level).
                ne(StringUtils.isNotBlank(id), DictCityEntity::getId,id).
                count();
        if (nameCount > 0) {
            throw new ServiceException("名称已存在");
        }
    }


    /**
     * 获取子集信息
     *
     * @param item
     * @param flagList
     * @return java.util.List<com.erp.model.sys.dto.DictCityDTO.ListDTO>
     * @author yl
     * @date 2023-05-11 17:26
     */
    private List<DictCityDTO.ListDTO> getChildren(DictCityDTO.ListDTO item, List<DictCityDTO.ListDTO> flagList) {
        List<DictCityDTO.ListDTO> collect = flagList.stream().filter(city -> item.getId().equals(city.getParentId())).
                map(c -> {
                    c.setChildrenList(getChildren(c, flagList));
                    return c;
                }).collect(Collectors.toList());
        return CollectionUtils.isEmpty(collect) ? null : collect;

    }

    private List<DictCityEntity> listByCountryCode(String countryCode) {
        List<DictCityEntity> list = this.lambdaQuery().eq(DictCityEntity::getCountryCode, countryCode).list();
        return list;
    }


    /**
     * 保存树结构
     *
     * @param parentId
     * @param batchList
     * @param dto
     * @return void
     * @author yl
     * @date 2023-05-11 15:33
     */
    private void getSaveTree(String parentId, List<DictCityEntity> batchList, DictCityDTO.AddDTO dto) {
        DictCityEntity entity = new DictCityEntity();
        BeanMapper.copy(dto, entity);
        String id = IdWorker.getIdStr();
        entity.setParentId(parentId);
        entity.setId(id);
        if (parentId.equals("0")) {
            entity.setType(province);
        } else {
            entity.setType(city);
        }
        batchList.add(entity);
        List<DictCityDTO.AddDTO> subList = dto.getChildrenList();
        if (CollectionUtils.isNotEmpty(subList)) {
            for (DictCityDTO.AddDTO item1 : subList) {
                this.getSaveTree(id, batchList, item1);
            }
        }
    }
}
