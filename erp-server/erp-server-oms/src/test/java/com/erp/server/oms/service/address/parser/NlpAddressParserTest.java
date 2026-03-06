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
}
