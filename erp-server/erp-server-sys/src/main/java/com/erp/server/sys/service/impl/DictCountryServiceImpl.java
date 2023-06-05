package com.erp.server.sys.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.service.SuperServiceImpl;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.server.sys.mapper.DictCountryMapper;
import com.erp.server.sys.service.DictCityService;
import com.erp.server.sys.service.DictCountryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
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

    @Override
    public List<DictCountryDTO.ListDTO> listCountry() {
        List<DictCountryDTO.ListDTO> list = baseMapper.listCountry();
        return list;
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
            // 3.2 生成省份sql
            addCity(temp,countryCode, 1, "0");
        });
    }

    private boolean addCity(JSONObject temp, String countryCode,Integer levelCode,String parentId) {
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
            if(ObjectUtil.isEmpty(provinceCity)){
                boolean isNum = code.chars().allMatch(Character::isDigit);
                provinceCity = new DictCityEntity(provinceName, countryCode, parentId, finalLevel, finalType, isNum ? Integer.parseInt(code) : 0, code);
                dictCityService.save(provinceCity);
            }

            addCity(provinceTemp, countryCode, levelCode + 1, provinceCity.getId());
        });
        return false;
    }
}
