package com.erp.server.oms.service.address.parser;

import com.common.core.enums.DictCityTypeEnum;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.server.oms.service.address.parser.model.ParsedAddress;
import com.erp.server.oms.service.address.parser.region.DictRegionLexicon;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class NlpAddressParserTest {

    @Test
    public void parse_basic_cn_address_should_extract_fields() {
        NlpAddressParser parser = new NlpAddressParser(new DictRegionLexicon(mockDictCityList()));
        ParsedAddress parsed = parser.parse("广东省深圳市龙华区测试大道001号，张三18289377326,526458");

        Assert.assertEquals("440000", parsed.getProvinceId());
        Assert.assertEquals("广东省", parsed.getProvince());
        Assert.assertEquals("440300", parsed.getCityId());
        Assert.assertEquals("深圳市", parsed.getCity());
        Assert.assertEquals("440309", parsed.getDistrictId());
        Assert.assertEquals("龙华区", parsed.getDistrict());
        Assert.assertEquals("张三", parsed.getContactName());
        Assert.assertEquals("18289377326", parsed.getPhone());
        Assert.assertEquals("526458", parsed.getZipCode());
        Assert.assertTrue(parsed.getDetailAddress().contains("测试大道001号"));
    }

    @Test
    public void parse_unknown_district_should_keep_text_and_empty_id() {
        NlpAddressParser parser = new NlpAddressParser(new DictRegionLexicon(mockDictCityList()));
        ParsedAddress parsed = parser.parse("广东省深圳市未来区创新路88号，李四,13900001111");

        Assert.assertEquals("440000", parsed.getProvinceId());
        Assert.assertEquals("440300", parsed.getCityId());
        Assert.assertNull(parsed.getDistrictId());
        Assert.assertEquals("未来区", parsed.getDistrict());
        Assert.assertEquals("13900001111", parsed.getPhone());
    }

    @Test
    public void parse_short_district_alias_should_not_override_real_region_or_strip_building_name() {
        NlpAddressParser parser = new NlpAddressParser(new DictRegionLexicon(mockAmbiguousDictCityList()));
        ParsedAddress parsed = parser.parse("广东深圳龙岗星河12楼11号");

        Assert.assertEquals("440000", parsed.getProvinceId());
        Assert.assertEquals("广东省", parsed.getProvince());
        Assert.assertEquals("440300", parsed.getCityId());
        Assert.assertEquals("深圳市", parsed.getCity());
        Assert.assertEquals("440307", parsed.getDistrictId());
        Assert.assertEquals("龙岗区", parsed.getDistrict());
        Assert.assertNull(parsed.getContactName());
        Assert.assertEquals("星河12楼11号", parsed.getDetailAddress());
    }

    @Test
    public void parse_same_name_district_should_prefer_city_and_province_in_source_text() {
        NlpAddressParser parser = new NlpAddressParser(new DictRegionLexicon(mockDuplicateDistrictDictCityList()));
        ParsedAddress parsed = parser.parse("广东省深圳市南山区粤海街道科技园社区科苑路15号科兴科学园A栋2单元302室");

        Assert.assertEquals("440000", parsed.getProvinceId());
        Assert.assertEquals("广东省", parsed.getProvince());
        Assert.assertEquals("440300", parsed.getCityId());
        Assert.assertEquals("深圳市", parsed.getCity());
        Assert.assertEquals("440305", parsed.getDistrictId());
        Assert.assertEquals("南山区", parsed.getDistrict());
    }

    @Test
    public void parse_municipality_district_should_prefer_matching_province_context() {
        NlpAddressParser parser = new NlpAddressParser(new DictRegionLexicon(mockDuplicateDistrictDictCityList()));
        ParsedAddress parsed = parser.parse("北京市朝阳区建国门外大街1号国贸大厦A座3201室");

        Assert.assertEquals("110000", parsed.getProvinceId());
        Assert.assertEquals("北京市", parsed.getProvince());
        Assert.assertEquals("110100", parsed.getCityId());
        Assert.assertEquals("北京市", parsed.getCity());
        Assert.assertEquals("110105", parsed.getDistrictId());
        Assert.assertEquals("朝阳区", parsed.getDistrict());
    }

    @Test
    public void parse_address_with_street_name_should_not_extract_street_prefix_as_contact() {
        NlpAddressParser parser = new NlpAddressParser(new DictRegionLexicon(mockGuangzhouDictCityList()));
        ParsedAddress parsed = parser.parse("广东省广州市荔湾区金达街36号快递柜,陈先生,15913149053");

        Assert.assertEquals("440000", parsed.getProvinceId());
        Assert.assertEquals("广东省", parsed.getProvince());
        Assert.assertEquals("440100", parsed.getCityId());
        Assert.assertEquals("广州市", parsed.getCity());
        Assert.assertEquals("440103", parsed.getDistrictId());
        Assert.assertEquals("荔湾区", parsed.getDistrict());
        Assert.assertEquals("陈先生", parsed.getContactName());
        Assert.assertEquals("15913149053", parsed.getPhone());
        Assert.assertEquals("金达街36号快递柜", parsed.getDetailAddress());
    }

    private List<DictCityEntity> mockDictCityList() {
        List<DictCityEntity> list = new ArrayList<DictCityEntity>();
        list.add(create("440000", "广东省", DictCityTypeEnum.PROVINCE.getCode(), null));
        list.add(create("440300", "深圳市", DictCityTypeEnum.CITY.getCode(), "440000"));
        list.add(create("440309", "龙华区", DictCityTypeEnum.DISTRICT.getCode(), "440300"));
        return list;
    }

    private DictCityEntity create(String id, String name, String type, String parentId) {
        DictCityEntity entity = new DictCityEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setType(type);
        entity.setParentId(parentId);
        entity.setDisabled(false);
        return entity;
    }

    private List<DictCityEntity> mockAmbiguousDictCityList() {
        List<DictCityEntity> list = new ArrayList<DictCityEntity>();
        list.add(create("440000", "广东省", DictCityTypeEnum.PROVINCE.getCode(), null));
        list.add(create("440300", "深圳市", DictCityTypeEnum.CITY.getCode(), "440000"));
        list.add(create("440307", "龙岗区", DictCityTypeEnum.DISTRICT.getCode(), "440300"));
        list.add(create("510000", "四川省", DictCityTypeEnum.PROVINCE.getCode(), null));
        list.add(create("510400", "攀枝花市", DictCityTypeEnum.CITY.getCode(), "510000"));
        list.add(create("510402", "东区", DictCityTypeEnum.DISTRICT.getCode(), "510400"));
        return list;
    }

    private List<DictCityEntity> mockDuplicateDistrictDictCityList() {
        List<DictCityEntity> list = new ArrayList<DictCityEntity>();
        list.add(create("440000", "广东省", DictCityTypeEnum.PROVINCE.getCode(), null));
        list.add(create("440300", "深圳市", DictCityTypeEnum.CITY.getCode(), "440000"));
        list.add(create("440305", "南山区", DictCityTypeEnum.DISTRICT.getCode(), "440300"));
        list.add(create("230000", "黑龙江省", DictCityTypeEnum.PROVINCE.getCode(), null));
        list.add(create("230400", "鹤岗市", DictCityTypeEnum.CITY.getCode(), "230000"));
        list.add(create("230404", "南山区", DictCityTypeEnum.DISTRICT.getCode(), "230400"));
        list.add(create("110000", "北京市", DictCityTypeEnum.PROVINCE.getCode(), null));
        list.add(create("110100", "北京市", DictCityTypeEnum.CITY.getCode(), "110000"));
        list.add(create("110105", "朝阳区", DictCityTypeEnum.DISTRICT.getCode(), "110100"));
        list.add(create("220000", "吉林省", DictCityTypeEnum.PROVINCE.getCode(), null));
        list.add(create("220100", "长春市", DictCityTypeEnum.CITY.getCode(), "220000"));
        list.add(create("220104", "朝阳区", DictCityTypeEnum.DISTRICT.getCode(), "220100"));
        return list;
    }

    private List<DictCityEntity> mockGuangzhouDictCityList() {
        List<DictCityEntity> list = new ArrayList<DictCityEntity>();
        list.add(create("440000", "广东省", DictCityTypeEnum.PROVINCE.getCode(), null));
        list.add(create("440100", "广州市", DictCityTypeEnum.CITY.getCode(), "440000"));
        list.add(create("440103", "荔湾区", DictCityTypeEnum.DISTRICT.getCode(), "440100"));
        return list;
    }
}
