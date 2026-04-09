package com.erp.server.oms.service.address.impl;

import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.AddressParseDTO;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.enums.DictValueEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.service.address.AddressParseService;
import com.erp.server.oms.service.address.parser.AddressParser;
import com.erp.server.oms.service.address.parser.NlpAddressParser;
import com.erp.server.oms.service.address.parser.model.ParsedAddress;
import com.erp.server.oms.service.address.parser.region.DictRegionLexicon;
import com.erp.server.oms.service.address.parser.region.RegionLexicon;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Shared address parse service implementation.
 */
@Service
public class AddressParseServiceImpl implements AddressParseService {

    private static final Pattern CHINESE_TEXT_PATTERN = Pattern.compile(".*[\\u4e00-\\u9fa5].*", Pattern.DOTALL);

    @Resource
    private SysUserFeign sysUserFeign;

    @Override
    public AddressParseDTO.ParseResultDTO parse(AddressParseDTO.ParseRequestDTO requestDTO) {
        String sourceText = requestDTO == null ? null : requestDTO.getFullAddress();
        if (StringUtils.isBlank(sourceText)) {
            throw new ServiceException(ApiError.SAMPLE_ADDRESS_PARSE_EMPTY_INPUT);
        }
        if (!CHINESE_TEXT_PATTERN.matcher(sourceText).matches()) {
            throw new ServiceException(ApiError.SAMPLE_ADDRESS_PARSE_ONLY_CN);
        }

        List<DictCityEntity> dictCityEntities = sysUserFeign.listByCountryCode(DictValueEnum.CN.getCode());
        RegionLexicon regionLexicon = new DictRegionLexicon(dictCityEntities);
        AddressParser parser = new NlpAddressParser(regionLexicon);
        ParsedAddress parsedAddress = parser.parse(sourceText);

        if (StringUtils.isBlank(parsedAddress.getProvince())
                && StringUtils.isBlank(parsedAddress.getCity())
                && StringUtils.isBlank(parsedAddress.getDistrict())) {
            throw new ServiceException(ApiError.SAMPLE_ADDRESS_PARSE_ONLY_CN);
        }

        AddressParseDTO.ParseResultDTO resultDTO = new AddressParseDTO.ParseResultDTO();
        resultDTO.setSourceText(sourceText);
        resultDTO.setCountryId(DictValueEnum.CN.getCode());
        resultDTO.setCountryName(DictValueEnum.CN.getName());

        resultDTO.setProvinceId(parsedAddress.getProvinceId());
        resultDTO.setProvince(parsedAddress.getProvince());
        resultDTO.setCityId(parsedAddress.getCityId());
        resultDTO.setCity(parsedAddress.getCity());
        resultDTO.setDistrictId(parsedAddress.getDistrictId());
        resultDTO.setDistrict(parsedAddress.getDistrict());

        resultDTO.setDetailAddress(parsedAddress.getDetailAddress());
        resultDTO.setContactName(parsedAddress.getContactName());
        resultDTO.setPhone(parsedAddress.getPhone());
        resultDTO.setZipCode(parsedAddress.getZipCode());
        resultDTO.setConfidence(BigDecimal.valueOf(parsedAddress.getConfidence()));
        return resultDTO;
    }
}
