package com.erp.server.sys.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
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
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.model.sys.dto.DictCityDTO;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.dto.KingdeeDTO;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictGlobalAreaEntity;
import com.erp.model.sys.entity.ThirdpartyRefBusinessEntity;
import com.erp.model.sys.enums.KingdeeAssistDataTypeEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.server.sys.mapper.DictCountryMapper;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeCountryService;
import com.erp.server.sys.service.DictCityService;
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SYS_COUNTRY;

/**
 * <p>
 * 国家字典表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-21
 */
@Service
public class DictCountryServiceImpl extends SuperServiceImpl<DictCountryMapper, DictCountryEntity> implements DictCountryService {

    @Resource
    private DictCityService dictCityService;

    @Resource
    private DictGlobalAreaService dictGlobalAreaService;

    @Resource
    private ThirdpartyRefBusinessService thirdpartyRefBusinessService;

    @Resource
    private SyncKingdeeCountryService syncKingdeeCountryService;

    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;



    @Override
    public List<DictCountryDTO.ListDTO> listCountry() {
        List<DictCountryDTO.ListDTO> list = baseMapper.listCountry();
        return list;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(DictCountryDTO.AddDTO dto) {
        DictCountryEntity entity = new DictCountryEntity();
        entity.setNameCn(dto.getName());
        entity.setRegionCode(dto.getParentRegionId());
        entity.setKingdeeCode(dto.getCode());
        handleData(entity);
        entity.setId(dto.getCode());
        Boolean addResult = this.save(entity);
        if (addResult) {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeCountryService.syncDataToKingdee(entity, SyncOperateEnum.OPERATE_APPROVE.getCode());
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
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(DictCountryDTO.UpdateDTO dto) {
        String id=dto.getId();
        DictCountryEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException("国家不存在");
        }
        String code = dto.getCode();
        entity.setNameCn(dto.getName());
        entity.setRegionCode(dto.getParentRegionId());
        entity.setKingdeeCode(code);
        handleData(entity);
        Boolean updateResult = this.updateById(entity);
        if (updateResult) {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeCountryService.syncDataToKingdee(entity, SyncOperateEnum.OPERATE_APPROVE.getCode());
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

    public void handleData(DictCountryEntity entity) {
        String id = entity.getId();
        int codeCount = this.lambdaQuery().
                eq(DictCountryEntity::getKingdeeCode, entity.getKingdeeCode()).
                ne(StringUtils.isNotBlank(id), DictCountryEntity::getId,id).
                count();
        if (codeCount > 0) {
            throw new ServiceException("国家二字码已存在");
        }
        int nameCount = this.lambdaQuery().eq(DictCountryEntity::getNameCn, entity.getNameCn()).
                ne(StringUtils.isNotBlank(id), DictCountryEntity::getId,id).
                count();
        if (nameCount > 0) {
            throw new ServiceException("国家名已存在");
        }
        entity.setSubregionCode(entity.getRegionCode());
    }

    @Override
    public List<DictCountryDTO.ListDTO> listCountryByParam(DictCountryDTO.ListParamDTO dto) {
        List<DictCountryDTO.ListDTO> list = baseMapper.listCountryByParam(dto);
        if (CollectionUtils.isEmpty(dto.getNameCnList())) {
            return list;
        }
        List<DictCountryDTO.ListDTO> resultList = new ArrayList<>();
        for (String nameCn : dto.getNameCnList()) {
            DictCountryDTO.ListDTO listDTO = list.stream().filter(obj -> obj.getNameCn().equals(nameCn)).findFirst().orElse(new DictCountryDTO.ListDTO());
            resultList.add(listDTO);
        }
        return resultList;
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initRegionList(String country) {
        // 1. 读取resources文件夹下locList.xml文件
        ClassPathResource resource = new ClassPathResource("locList.xml");
        String xmlContent = resource.readUtf8Str();

        // 将XML转换为JSON
        JSON json = JSONUtil.parseFromXml(xmlContent);
        // 2. 解析文件
        JSONObject jsonObject = JSONUtil.parseObj(json.toString());
        // 3. 生成sql
        JSONArray countryList = jsonObject.getJSONObject("Location").getJSONArray("CountryRegion");
        countryList.stream().forEach( x -> {

            JSONObject temp = (JSONObject) x;
            // 3.1 生成国家sql
            String countryName = temp.getStr("Name");
            String countryCode = temp.getStr("Code");
            countryCode = "1".equals(countryCode) ? "CN" : countryCode;
            DictCountryEntity dictCountry = lambdaQuery()
                    .eq(DictCountryEntity::getId, countryCode)
                    .one();
            if(StrUtil.isBlank(country) && ObjectUtil.isEmpty(dictCountry)){
                return;
            }else if(StrUtil.isNotBlank(country) && !country.equals(countryName)){
                return;
            }
//            if(StrUtil.isBlank(code) && 1 == provinceTemp.size()){
//                JSONArray city = ((JSONObject) province).getJSONArray("City");
//                addCity(provinceTemp, countryCode, levelCode + 1, parentId);
//            }
            // 3.2 生成省份sql
            addCity(temp,countryCode, 1, "0" ,0);
        });
    }


    /**
     * 根据国家ids 获取信息
     *
     * @param ids
     * @return java.util.List<com.erp.model.sys.entity.DictCountryEntity>
     * @author yl
     * @date 2023-08-21 15:31
     */
    @Override
    public List<DictCountryEntity> listCountryByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(DictCountryEntity::getId, ids).list();
    }

    /**
     * 查询区域国家列表
     *
     * @param type
     * @return java.util.List<com.erp.model.sys.dto.DictCountryDTO.CascadeDTO>
     * @author yl
     * @date 2023-08-31 10:45
     */
    @Override
    public List<DictCountryDTO.CascadeDTO> areaCountryListByType(String type) {
        List<DictCountryEntity> dictCountryList = listByDataFlag(type);
        Map<String, List<DictCountryEntity>> map = dictCountryList.stream().collect(Collectors.groupingBy(DictCountryEntity::getAmazonArea));
        List<DictCountryDTO.CascadeDTO> resultList = new ArrayList<>(map.size());
        for (Map.Entry<String, List<DictCountryEntity>> item : map.entrySet()) {
            DictCountryDTO.CascadeDTO cascade = new DictCountryDTO.CascadeDTO();
            cascade.setDictAreaCode(item.getKey());
            List<DictCountryEntity> list = item.getValue();
            List<DictCountryDTO.ChildrenDTO> childrenList = new ArrayList<>(list.size());
            for (DictCountryEntity countryEntity : list) {
                DictCountryDTO.ChildrenDTO childrenDTO = new DictCountryDTO.ChildrenDTO();
                childrenDTO.setDictCountryCode(countryEntity.getId());
                childrenDTO.setDictCountryName(countryEntity.getNameCn());
                childrenList.add(childrenDTO);
            }
            cascade.setChildren(childrenList);
            resultList.add(cascade);
        }

        return resultList;
    }



    @Override
    public List<DictCountryDTO.ListRegionDTO> listAreaCountry(DictCountryDTO.ListParamDTO dto) {
        //区域数据
        List<DictGlobalAreaEntity> list = dictGlobalAreaService.lambdaQuery()
                .eq(StrUtil.isNotBlank(dto.getRegionCode()),DictGlobalAreaEntity::getRegionCode, dto.getRegionCode()).list();
        //国家数据
        List<DictCountryDTO.ListDTO> countryList = this.listCountryByParam(dto);
        List<DictCountryDTO.ListRegionDTO> resultList = new ArrayList<>();
        DictCountryDTO.ListRegionDTO allList = new DictCountryDTO.ListRegionDTO();
        allList.setRegionCode("");
        allList.setRegionName("全部");
        allList.setList(countryList);
        resultList.add(allList);
        List<DictCountryDTO.ListRegionDTO> regionList = list.stream().map(obj -> new DictCountryDTO.ListRegionDTO(obj.getRegionCode(), obj.getRegionName())).distinct().collect(Collectors.toList());
        for (DictCountryDTO.ListRegionDTO listRegionDTO : regionList) {
            List<DictCountryDTO.ListDTO> detailList = countryList.stream().filter(obj -> obj.getRegionCode().equals(listRegionDTO.getRegionCode())).collect(Collectors.toList());
            listRegionDTO.setList(detailList);
            resultList.add(listRegionDTO);
        }
        return resultList;
    }

    @Override
    public List<DictCountryEntity> listCountryByNames(List<String> names) {
        if(CollectionUtils.isEmpty(names)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(DictCountryEntity::getNameCn,names).list();
    }

    @Override
    public List<DictCountryEntity> listByRegionCode(String regionCode) {
        if(StringUtils.isEmpty(regionCode)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().eq(DictCountryEntity::getRegionCode,regionCode).list();
    }

    @Override
    public Boolean init() {
        Class<DictCountryEntity> AreaClass = DictCountryEntity.class;
        TableName tableName = AreaClass.getDeclaredAnnotation(TableName.class);
        //获取到表名
        String businessType = tableName.value();
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BOS_ASSISTANTDATA_DETAIL.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        String areaCode = KingdeeAssistDataTypeEnum.COUNTRY.getCode();
        //类别
        queryFilters.add(StrUtil.format("FId.FNumber = {}", "'" + areaCode + "'"));
        //查询
        String fieldKeys = "FEntryID,FNumber,FDataValue,FId.FNumber,FParentId,FSeq";
        String filterStr = String.join(" and ", queryFilters);
        // 当前页数
        Integer pageIndex = 0;
        // 每次最多获取100条
        Integer pageSize = 1000;
        List<KingdeeDTO.AssistDTO> countryList = new ArrayList<>(20);
        Boolean dataSign = true;
        while (dataSign) {
            List<Map<String, Object>> result = apiUtils.queryList(filterStr, fieldKeys, pageSize, pageIndex, 0);
            if (result.size() < pageSize) {
                dataSign = false;
            }
            List<KingdeeDTO.AssistDTO> entityList = result.stream().map(obj ->
                    BeanUtil.toBean(obj, KingdeeDTO.AssistDTO.class)).collect(Collectors.toList());
            countryList.addAll(entityList);
            pageIndex++;
        }
        List<DictCountryEntity> saveOrUpdateList = new ArrayList<>(20);
        List<ThirdpartyRefBusinessEntity> thirdpartySaveList = new ArrayList<>(20);
        //数据库存在的
        List<DictCountryEntity> dbList = this.list();
        List<String> idList = dbList.stream().map(DictCountryEntity::getId).collect(Collectors.toList());
        List<ThirdpartyRefBusinessEntity> thirdpartyDbList = thirdpartyRefBusinessService.listByBusinessIds(idList);
        for (KingdeeDTO.AssistDTO item : countryList) {
            String kingdeeId = item.getKingdeeId();
            String kingdeeCode = item.getKingdeeCode();
            String parentId = item.getParentId();
            String name = item.getName();
            DictCountryEntity dbEntity = dbList.stream().filter(entity ->
                            entity.getKingdeeCode().equals(kingdeeCode)
                                    || entity.getRegionCode().equals(kingdeeCode) ).
                    findFirst().orElse(null);
            //表示没有
            if (Objects.isNull(dbEntity)) {
                DictCountryEntity addEntity = new DictCountryEntity();
                String id = kingdeeCode;
                addEntity.setKingdeeCode(kingdeeCode);
                addEntity.setRegionCode(parentId);
                addEntity.setNameCn(name);
                addEntity.setId(id);
                saveOrUpdateList.add(addEntity);

                ThirdpartyRefBusinessEntity refEntity = new ThirdpartyRefBusinessEntity();
                refEntity.setBusinessType(businessType);
                refEntity.setBusinessId(id);
                refEntity.setThirdpartyId(kingdeeId);
                thirdpartySaveList.add(refEntity);
            } else {
                //表示有
                if (!dbEntity.getNameCn().equals(name)
                        || !dbEntity.getRegionCode().equals(parentId)
                        || !dbEntity.getKingdeeCode().equals(kingdeeCode)) {
                    dbEntity.setKingdeeCode(kingdeeCode);
                    dbEntity.setRegionCode(kingdeeCode);
                    dbEntity.setNameCn(name);
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



        return null;
    }

    @Override
    public PagingVO<DictCountryDTO.PagingViewDTO> paging(PagingDTO<DictCountryDTO.PagingParamDTO> dto) {
        DictCountryDTO.PagingParamDTO paramDTO = dto.getParams();
        paramDTO.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<DictCountryDTO.PagingViewDTO> pageData = this.baseMapper.paging(query, paramDTO);
        return new PagingVO(pageData);
    }

    @Override
    public DictCountryDTO.ViewDTO view(String id) {
        DictCountryDTO.ViewDTO result = new DictCountryDTO.ViewDTO();
        DictCountryEntity entity = this.getById(id);
        if(Objects.isNull(entity)){
            throw new ServiceException("国家不存在");
        }
        result.setCode(entity.getId());
        result.setName(entity.getNameCn());
        result.setParentRegionId(entity.getRegionCode());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId, String syncKingdeeCode) {
        Boolean result = false;
        if (StringUtils.isNotBlank(syncKingdeeCode)) {
            result = this.lambdaUpdate()
                    .eq(DictCountryEntity::getId, id)
                    .set(StringUtils.isNotBlank(syncKingdeeCode), DictCountryEntity::getKingdeeCode, syncKingdeeCode)
                    .update();
        }

        ThirdpartyRefBusinessEntity refBusinessEntity = thirdpartyRefBusinessService.getByBusinessId(id);
        if (Objects.isNull(refBusinessEntity)) {
            Class<DictCountryEntity> AreaClass = DictCountryEntity.class;
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
    public BatchResultDTO delete(String id) {
        DictCountryEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException("国家不存在");
        }
        List<DictCityDTO.ListDTO> cityList = dictCityService.listCity(id);
        if(CollectionUtils.isNotEmpty(cityList)){
            throw new ServiceException("国家下存在省市，无法删除");
        }
        baseMapper.deleteById(id);
        //金蝶推送
        DmpPushTaskEntity pushTaskEntity = syncKingdeeCountryService.syncDataToKingdee(entity, SyncOperateEnum.OPERATE_DELETE.getCode());
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(Arrays.asList(pushTaskEntity));
            }
        });
        thirdpartyRefBusinessService.removeByBusinessId(id);
        return BatchResultDTO.success(entity.getId(), entity.getNameCn(), OperationTypeEnum.DELETE);

    }

    @Override
    public void exportList(DictCountryDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("国家Excel导出", EXPORT_SYS_COUNTRY.getCode(), dto);
    }

    @Override
    public PagingVO<DictCountryDTO.PagingViewDTO> exportCountry(PagingDTO<DictCountryDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<DictCountryDTO.PagingViewDTO> page = this.baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        return new PagingVO<>(page);
    }

    @Override
    public PagingVO<DictCountryDTO.ListDTO> pagingSelect(PagingDTO<DictCountryDTO.SelectDTO> dto) {
        DictCountryDTO.SelectDTO params = dto.getParams();
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<DictCountryDTO.ListDTO> pagResult = baseMapper.pagingSelect(query, params);
        return new PagingVO<>(pagResult);
    }

    /**
     * 根据data flag获取国家
     *
     * @param dataFlag
     * @return
     */
    public List<DictCountryEntity> listByDataFlag(String dataFlag) {
        return this.lambdaQuery().eq(DictCountryEntity::getDataFlag, dataFlag).list();
    }

    private boolean addCity(JSONObject temp, String countryCode,Integer levelCode,String parentId, Integer skipLevel) {
        int level = 1;
        String type = "province";
        String key = "State";
        if(2 == levelCode){
            level = 2;
            type = "city";
            key = "City";
        }
        if(3 == levelCode){
            level = 3;
            type = "district";
            key = "Region";
        }
        JSONArray stateList = new JSONArray();
        String stateStr = temp.getStr(key);
        if(!JSONUtil.isTypeJSONArray(stateStr)){
            stateList.add(JSONUtil.parse(stateStr));
        }else {
            stateList = temp.getJSONArray(key);
        }
        stateList = stateList.stream().filter(ObjectUtil::isNotEmpty).distinct().collect(JSONArray::new, JSONArray::add, JSONArray::add);
        if(CollectionUtil.isEmpty(stateList)){
            return true;
        }
        Integer finalLevel = level - skipLevel;
        String finalType = type;
        stateList.stream().forEach(province -> {
            JSONObject provinceTemp = (JSONObject) province;
            String provinceName = provinceTemp.getStr("Name");
            String code = provinceTemp.getStr("Code");
            if(StrUtil.isBlank(code)){
                Integer curSkipLevel = skipLevel + 1;
                addCity(provinceTemp, countryCode, levelCode + 1, parentId, curSkipLevel);
                return;
            }
            DictCityEntity provinceCity = dictCityService.lambdaQuery()
                    .eq(DictCityEntity::getCode, code)
                    .eq(DictCityEntity::getLevel, finalLevel)
                    .eq(DictCityEntity::getName, provinceName)
                    .one();
            if(ObjectUtil.isEmpty(provinceCity)){
                boolean isNum = code.chars().allMatch(Character::isDigit);
                provinceCity = new DictCityEntity(provinceName, countryCode, parentId, finalLevel, finalType, isNum ? Integer.parseInt(code) : 0, code);
                dictCityService.save(provinceCity);
            }

            addCity(provinceTemp, countryCode, levelCode + 1, provinceCity.getId(), skipLevel);
        });
        return false;
    }

    @Override
    public List<DictCountryEntity> listCountryByIdsOrAlpha3(List<String> codeList) {
        return lambdaQuery()
                .in(DictCountryEntity::getId, codeList)
                .or()
                .in(DictCountryEntity::getAlpha3, codeList)
                .list();
    }

    @Override
    public void renewCountryImg() {
        List<DictCountryEntity> list = list();
        String baseUrl = "https://flagcdn.com/w320/";
        for (DictCountryEntity dictCountry : list) {
            String imgName =dictCountry.getId().toLowerCase() + ".png";
            Path tempFile = Paths.get(imgName);
            try {
                tempFile = Files.createTempFile(dictCountry.getId().toLowerCase(), ".png");
                URL url = new URL(baseUrl + imgName);
                try (InputStream inputStream = url.openStream()) {
                    Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
                }
                byte[] imageData = Files.readAllBytes(tempFile);
                String s = FastDFSClientUtil.uploadFile(imageData, imgName, null);
                dictCountry.setFlagUrl(s);
            } catch (IOException e) {
                log.error("获取失败");
            } finally {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    log.error("删除失败");
                }
            }
        }
        updateBatchById(list);
    }

    @Override
    public List<DictCountryEntity> listCountryByNamesOrIds(List<String> codeList) {
        if(CollectionUtils.isEmpty(codeList)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(DictCountryEntity::getNameCn,codeList).or().in(DictCountryEntity::getId, codeList).list();
    }
}
