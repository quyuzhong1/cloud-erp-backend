package com.erp.server.sys.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictGlobalAreaEntity;
import com.erp.server.sys.mapper.DictCountryMapper;
import com.erp.server.sys.service.DictCityService;
import com.erp.server.sys.service.DictCountryService;
import com.erp.server.sys.service.DictGlobalAreaService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public List<DictCountryDTO.ListDTO> listCountry() {
        List<DictCountryDTO.ListDTO> list = baseMapper.listCountry();
        return list;
    }

    @Override
    public List<DictCountryDTO.ListDTO> listCountryByParam(DictCountryDTO.ListParamDTO dto) {
        List<DictCountryDTO.ListDTO> list = baseMapper.listCountryByParam(dto);
        return list;
    }

    public static void main(String[] args) {
        // 1. 读取resources文件夹下locList.xml文件
        ClassPathResource resource = new ClassPathResource("locList.xml");
        String xmlContent = resource.readUtf8Str();

        // 将XML转换为JSON
        JSON json = JSONUtil.parseFromXml(xmlContent);
        // 2. 解析文件
        JSONObject jsonObject = JSONUtil.parseObj(json.toString());
//        System.out.println(jsonObject);
        JSONArray jsonArray = jsonObject.getJSONObject("Location").getJSONArray("CountryRegion");
        List<Object> name = jsonArray.stream().map(o -> new JSONObject(o).get("Name")).collect(Collectors.toList());
        List<Object> code = jsonArray.stream().map(o -> new JSONObject(o).get("Code")).collect(Collectors.toList());
        System.out.println(name);
        System.out.println(name.size());
        System.out.println(code);
        System.out.println(code.size());
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
        countryList.stream().forEach(x -> {

            JSONObject temp = (JSONObject) x;
            // 3.1 生成国家sql
            String countryName = temp.getStr("Name");
            String countryCode = temp.getStr("Code");
            countryCode = "1".equals(countryCode) ? "CN" : countryCode;
            DictCountryEntity dictCountry = lambdaQuery()
                    .eq(DictCountryEntity::getId, countryCode)
                    .one();
            if (StrUtil.isBlank(country) && ObjectUtil.isEmpty(dictCountry)) {
                return;
            } else if (StrUtil.isNotBlank(country) && !country.equals(countryName)) {
                return;
            }
            // 3.2 生成省份sql
            addCity(temp, countryCode, 1, "0");
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
                .eq(DictGlobalAreaEntity::getRegionCode, dto.getRegionCode()).list();
        //国家数据
        List<DictCountryDTO.ListDTO> countryList = this.listCountryByParam(dto);
        List<DictCountryDTO.ListRegionDTO> resultList = new ArrayList<>();
        DictCountryDTO.ListRegionDTO allList = new DictCountryDTO.ListRegionDTO();
        allList.setRegionCode("");
        allList.setRegionName("全部");
        allList.setList(countryList);
        resultList.add(allList);
        for (DictGlobalAreaEntity areaEntity : list) {
            DictCountryDTO.ListRegionDTO listRegionDTO = new DictCountryDTO.ListRegionDTO();
            listRegionDTO.setRegionCode(areaEntity.getRegionCode());
            listRegionDTO.setRegionName(areaEntity.getRegionName());
            List<DictCountryDTO.ListDTO> detailList = countryList.stream().filter(obj -> obj.getRegionCode().equals(areaEntity.getRegionCode())).collect(Collectors.toList());
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


    /**
     * 根据data flag获取国家
     *
     * @param dataFlag
     * @return
     */
    public List<DictCountryEntity> listByDataFlag(String dataFlag) {
        return this.lambdaQuery().eq(DictCountryEntity::getDataFlag, dataFlag).list();
    }

    private boolean addCity(JSONObject temp, String countryCode, Integer levelCode, String parentId) {
        int level = 1;
        String type = "province";
        String key = "State";
        if (2 == levelCode) {
            level = 2;
            type = "city";
            key = "City";
        }
        if (3 == levelCode) {
            level = 3;
            type = "district";
            key = "Region";
        }
        JSONArray stateList = new JSONArray();
        String stateStr = temp.getStr(key);
        if (!JSONUtil.isTypeJSONArray(stateStr)) {
            stateList.add(JSONUtil.parse(stateStr));
        } else {
            stateList = temp.getJSONArray(key);
        }
        stateList = stateList.stream().filter(ObjectUtil::isNotEmpty).distinct().collect(JSONArray::new, JSONArray::add, JSONArray::add);
        if (CollectionUtil.isEmpty(stateList)) {
            return true;
        }
        Integer finalLevel = level;
        String finalType = type;
        stateList.stream().forEach(province -> {
            JSONObject provinceTemp = (JSONObject) province;
            String provinceName = provinceTemp.getStr("Name");
            String code = provinceTemp.getStr("Code");
            DictCityEntity provinceCity = dictCityService.lambdaQuery()
                    .eq(DictCityEntity::getCode, code)
                    .eq(DictCityEntity::getLevel, finalLevel)
                    .one();
            if (ObjectUtil.isEmpty(provinceCity)) {
                boolean isNum = code.chars().allMatch(Character::isDigit);
                provinceCity = new DictCityEntity(provinceName, countryCode, parentId, finalLevel, finalType, isNum ? Integer.parseInt(code) : 0, code);
                dictCityService.save(provinceCity);
            }

            addCity(provinceTemp, countryCode, levelCode + 1, provinceCity.getId());
        });
        return false;
    }
}
