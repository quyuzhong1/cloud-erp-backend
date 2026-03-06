package com.erp.server.oms.service.address.parser;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.erp.server.oms.service.address.parser.model.ParsedAddress;
import com.erp.server.oms.service.address.parser.region.RegionLexicon;
import com.erp.server.oms.service.address.parser.region.RegionMatchResult;
import com.erp.server.oms.service.address.parser.region.RegionMatcher;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Address parser implementation.
 */
public class NlpAddressParser implements AddressParser {

    private final PhoneExtractor phoneExtractor;
    private final NameExtractor nameExtractor;
    private final RegionMatcher regionMatcher;

    public NlpAddressParser(RegionLexicon regionLexicon) {
        this.phoneExtractor = new PhoneExtractor();
        this.nameExtractor = new NameExtractor();
        this.regionMatcher = new RegionMatcher(regionLexicon);
    }

    @Override
    public ParsedAddress parse(String input) {
        ParsedAddress result = new ParsedAddress();
        String normalized = TextCleaner.normalize(input);
        if (StringUtils.isBlank(normalized)) {
            result.setConfidence(0d);
            return result;
        }

        String zipCode = phoneExtractor.extractZipCode(normalized);
        String textNoZip = removeZip(normalized, zipCode);

        PhoneExtractor.ExtractedPhone phone = phoneExtractor.extractBest(textNoZip);
        String textWithoutPhone = phone.removeFrom(textNoZip);

        NameExtractor.ExtractedName extractedName = nameExtractor.extract(textWithoutPhone, phone.getStart());
        List<String> locationHints = extractLocationHints(textWithoutPhone);
        RegionMatchResult region = regionMatcher.match(locationHints, textWithoutPhone);

        String detailAddress = buildDetailAddress(textWithoutPhone, region, extractedName);

        result.setProvinceId(StringUtils.trimToNull(region.getProvinceId()));
        result.setProvince(StringUtils.trimToNull(region.getProvince()));
        result.setCityId(StringUtils.trimToNull(region.getCityId()));
        result.setCity(StringUtils.trimToNull(region.getCity()));
        result.setDistrictId(StringUtils.trimToNull(region.getDistrictId()));
        result.setDistrict(StringUtils.trimToNull(region.getDistrict()));
        fillFallbackRegionWhenIdMissing(result, textWithoutPhone);
        result.setDetailAddress(StringUtils.trimToNull(detailAddress));
        result.setContactName(extractedName.present() ? extractedName.getName() : null);
        result.setPhone(phone.present() ? phone.getPhone() : null);
        result.setZipCode(StringUtils.trimToNull(zipCode));
        result.setConfidence(calculateConfidence(result));
        return result;
    }

    private String removeZip(String text, String zipCode) {
        if (StringUtils.isBlank(text) || StringUtils.isBlank(zipCode)) {
            return text;
        }
        return text.replace(zipCode, " ");
    }

    private List<String> extractLocationHints(String text) {
        List<String> hints = new ArrayList<String>();
        if (StringUtils.isBlank(text)) {
            return hints;
        }
        try {
            List<Term> terms = HanLP.segment(text);
            for (Term term : terms) {
                if (term == null || term.word == null || term.nature == null) {
                    continue;
                }
                String nature = term.nature.toString();
                if (nature.startsWith("ns") || nature.startsWith("nt")) {
                    hints.add(term.word);
                }
            }
        } catch (Throwable ignored) {
        }
        if (hints.isEmpty()) {
            Matcher matcher = Pattern.compile("[\\u4e00-\\u9fa5]{2,12}(省|市|区|县|自治区|特别行政区|自治州|盟|旗)?").matcher(text);
            while (matcher.find()) {
                hints.add(matcher.group());
            }
        }
        return hints;
    }

    private String buildDetailAddress(String textWithoutPhone, RegionMatchResult region, NameExtractor.ExtractedName extractedName) {
        String detail = TextCleaner.extractDetailAddress(textWithoutPhone);
        if (StringUtils.isBlank(detail)) {
            detail = TextCleaner.removeLabelWords(textWithoutPhone);
        }
        detail = TextCleaner.removeToken(detail, region.getProvince());
        detail = TextCleaner.removeToken(detail, region.getCity());
        detail = TextCleaner.removeToken(detail, region.getDistrict());
        detail = TextCleaner.removeToken(detail, region.getMatchedProvinceToken());
        detail = TextCleaner.removeToken(detail, region.getMatchedCityToken());
        detail = TextCleaner.removeToken(detail, region.getMatchedDistrictToken());
        if (extractedName != null && extractedName.present()) {
            detail = TextCleaner.removeToken(detail, extractedName.getName());
        }
        detail = detail.replaceAll("\\b\\d{6}\\b", " ");
        detail = detail.replace(",", " ").replace(";", " ").replace(":", " ");
        detail = TextCleaner.cleanupDetailAddress(detail);
        return detail;
    }

    private void fillFallbackRegionWhenIdMissing(ParsedAddress result, String text) {
        if (StringUtils.isBlank(result.getProvince())) {
            result.setProvince(matchByRegex(text, "([\\u4e00-\\u9fa5]{2,12}(省|自治区|特别行政区|市))"));
        }
        if (StringUtils.isBlank(result.getCity())) {
            result.setCity(matchByRegex(text, "([\\u4e00-\\u9fa5]{2,12}(市|自治州|地区|盟))"));
        }
        if (StringUtils.isBlank(result.getDistrict())) {
            result.setDistrict(matchByRegex(text, "([\\u4e00-\\u9fa5]{2,12}(区|县|旗|市))"));
        }
    }

    private String matchByRegex(String text, String regex) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        Matcher matcher = Pattern.compile(regex).matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private double calculateConfidence(ParsedAddress parsedAddress) {
        double score = 0d;
        if (StringUtils.isNotBlank(parsedAddress.getProvince())) {
            score += 0.15d;
        }
        if (StringUtils.isNotBlank(parsedAddress.getCity())) {
            score += 0.15d;
        }
        if (StringUtils.isNotBlank(parsedAddress.getDistrict())) {
            score += 0.10d;
        }
        if (phoneExtractor.isValidMobile(parsedAddress.getPhone())) {
            score += 0.20d;
        }
        if (StringUtils.isNotBlank(parsedAddress.getContactName())) {
            score += 0.20d;
        }
        if (StringUtils.isNotBlank(parsedAddress.getDetailAddress())) {
            score += 0.10d;
        }
        if (StringUtils.isNotBlank(parsedAddress.getZipCode())) {
            score += 0.10d;
        }
        if (score > 1d) {
            score = 1d;
        }
        return Math.round(score * 100d) / 100d;
    }
}
