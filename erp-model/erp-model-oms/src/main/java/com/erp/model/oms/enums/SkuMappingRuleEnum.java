package com.erp.model.oms.enums;

import com.alibaba.fastjson.JSONObject;
import com.common.core.constant.EnumMessage;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.SkuMappingRuleDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@ToString
@AllArgsConstructor
public enum SkuMappingRuleEnum implements EnumMessage{
    COMPLETE_SKU("completeSku","识别完整的SKU",v-> new ArrayList<>(),(regex,v)-> v),
    IGNORE_PREFIXES_AND_SUFFIXES("ignorePrefixesAndSuffixes","识别忽略前、后缀的SKU",SkuMappingRuleEnum::handleIgnorePrefixesAndSuffixes,SkuMappingRuleEnum::handleRegex),
    IGNORE_FIRST_AND_LAST_DIGITS("ignoreFirstAndLastDigits","识别忽略前、后位数的SKU",SkuMappingRuleEnum::handleIgnoreFirstAndLastDigits,SkuMappingRuleEnum::handleRegex),
    EXTRACT_FIRST_TO_LAST_DIGITS("extractFirstToLast","识别截取后的SKU",SkuMappingRuleEnum::handleExtractFirstToLast,SkuMappingRuleEnum::handleRegex),
    EXTRACT_BETWEEN_START_AND_END("extractBetweenStartAndEnd","截取SKU起始符与结束符之间的字符",SkuMappingRuleEnum::handleExtractBetweenStartAndEnd,SkuMappingRuleEnum::handleRegex),
    ;
    private final String code;
    private final String name;
    //将参数处理成正则表达式
    private final Function<SkuMappingRuleDTO.RuleDTO, List<String>> regexMethod;
    //返回正则处理后的结果
    private final BiFunction<String,String,String> handleRegexMethod;

    /**
     * 扩展规则枚举
     */
    @Getter
    @AllArgsConstructor
    public enum SkuMappingExtendRuleEnum implements EnumMessage{
        REPLACE_SPECIFIED_CHARACTERS("replaceSpecifiedCharacters","替换指定字符（配对前替换）",SkuMappingRuleEnum::handleReplaceSpecifiedCharacters,SkuMappingRuleEnum::handleReplace),
        ;
        private final String code;
        private final String name;
        private final Function<SkuMappingRuleDTO.ExtendRuleDTO, List<String>> regexMethod;
        private final BiFunction<String,String,String> handleRegexMethod;

    }
    /**
     * 起始符规则
     */
    @Getter
    @AllArgsConstructor
    public enum SkuMappingEnum implements EnumMessage{
        LEFTMOST_SIDE("leftmostSide","最左侧"),
        FAR_RIGHT("farRight","最右侧"),
        ;
        private final String code;
        private final String name;

    }

    /**
     * 符号枚举
     */
    @Getter
    @AllArgsConstructor
    public enum SkuMappingSymbolicEnum implements EnumMessage{
        LEFTMOST_SIDE("leftmostSide","最左侧"),
        FAR_RIGHT("farRight","最右侧"),
        ;
        private final String code;
        private final String name;

    }
    private static List<String> handleIgnorePrefixesAndSuffixes(SkuMappingRuleDTO.RuleDTO commonDTO){
        List<String> list = new ArrayList<>(commonDTO.getRuleContentList().size());
        for(SkuMappingRuleDTO.RuleConditionsDTO ruleConditionsDTO : commonDTO.getRuleContentList()){
            String prefix = Objects.isNull(ruleConditionsDTO.getIgnorePrefix())?"":ruleConditionsDTO.getIgnorePrefix();
            String suffixes = Objects.isNull(ruleConditionsDTO.getIgnoringSuffixes())?"":ruleConditionsDTO.getIgnoringSuffixes();
            String regex = "\"^"+ prefix+"(.*?)"+suffixes+"$\"";
            list.add(regex);
        }
        return list;
    }

    private static List<String> handleIgnoreFirstAndLastDigits(SkuMappingRuleDTO.RuleDTO commonDTO){
        List<String> list = new ArrayList<>(commonDTO.getRuleContentList().size());
        for(SkuMappingRuleDTO.RuleConditionsDTO ruleConditionsDTO : commonDTO.getRuleContentList()){
            int prefix = Objects.isNull(ruleConditionsDTO.getIgnoringBeforePosition())?0:ruleConditionsDTO.getIgnoringBeforePosition();
            int suffixes = Objects.isNull(ruleConditionsDTO.getIgnoringAfterPosition())?0:ruleConditionsDTO.getIgnoringAfterPosition();
            String regex = "^.{"+prefix+"}(.*).{"+suffixes+"}$";
            list.add(regex);
        }
        return list;
    }

    private static List<String> handleExtractFirstToLast(SkuMappingRuleDTO.RuleDTO commonDTO){
        List<String> list = new ArrayList<>(commonDTO.getRuleContentList().size());
        for(SkuMappingRuleDTO.RuleConditionsDTO ruleConditionsDTO : commonDTO.getRuleContentList()){
            String regex = ".{"+ruleConditionsDTO.getInterceptionFrontPosition()+"}(.{"+ruleConditionsDTO.getInterceptionBehindPosition()+"}).*";
            list.add(regex);
        }
        return list;
    }

    private static List<String> handleExtractBetweenStartAndEnd(SkuMappingRuleDTO.RuleDTO commonDTO){
        List<String> list = new ArrayList<>(commonDTO.getRuleContentList().size());
        String waitHandleRegex;
        /**
         分四种情况
         1.起始符取左侧，结束符取右侧
         2.起始符取右侧，结束符取右侧
         3.起始符取左侧，结束符取左侧
         4.起始符取右侧，结束符取左侧
         【#】替换起始符，【%】替换结束符
         */
        if(commonDTO.getValidStartingSymbolPosition().equals(SkuMappingEnum.LEFTMOST_SIDE.code) &&
                commonDTO.getValidEndSymbolPosition().equals(SkuMappingEnum.FAR_RIGHT.code) ){
            waitHandleRegex = "【#】(.*?)【%】(?!.*【%】)";
        } else if (commonDTO.getValidStartingSymbolPosition().equals(SkuMappingEnum.FAR_RIGHT.code) &&
                commonDTO.getValidEndSymbolPosition().equals(SkuMappingEnum.FAR_RIGHT.code) ) {
            waitHandleRegex = "【#】((?:(?!【#】).)*?)【%】(?!.*【%】)";
        } else if (commonDTO.getValidStartingSymbolPosition().equals(SkuMappingEnum.LEFTMOST_SIDE.code) &&
                commonDTO.getValidEndSymbolPosition().equals(SkuMappingEnum.LEFTMOST_SIDE.code) ) {
            waitHandleRegex = "【#】(.*?)(?=【%】.*$)";
        } else if (commonDTO.getValidStartingSymbolPosition().equals(SkuMappingEnum.FAR_RIGHT.code) &&
                commonDTO.getValidEndSymbolPosition().equals(SkuMappingEnum.LEFTMOST_SIDE.code) ) {
            waitHandleRegex = "【#】(?!.*【#】)(.*?)【%】";
        }else{
            throw new ServiceException("找不到起始符规则");
        }
        for(SkuMappingRuleDTO.RuleConditionsDTO ruleConditionsDTO : commonDTO.getRuleContentList()){
            String regex = waitHandleRegex.replaceAll("【#】",ruleConditionsDTO.getStartingSymbol()).replaceAll("【%】",ruleConditionsDTO.getEndSymbol());
            list.add(regex);
        }
        return list;
    }

    private static List<String> handleReplaceSpecifiedCharacters(SkuMappingRuleDTO.ExtendRuleDTO commonDTO){
        List<String> list = new ArrayList<>(commonDTO.getExtendRuleContentList().size());
        //字符串替换，不需要正则，存放json key为需要替换的值，value为替换值
        JSONObject jsonObject = new JSONObject();
        for(SkuMappingRuleDTO.ExtendRuleConditionsDTO extendRuleConditionsDTO : commonDTO.getExtendRuleContentList()){
            jsonObject.put(extendRuleConditionsDTO.getBeforeReplacingCharacters(),extendRuleConditionsDTO.getAfterReplacingCharacters());
            list.add(jsonObject.toJSONString());
        }
        return list;
    }

    private static String handleRegex(String regex,String inputStr){
        regex = regex.replaceAll("\"","");
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return inputStr;
    }

    private static String handleReplace(String json,String inputStr){
        JSONObject jsonObject = JSONObject.parseObject(json);
        String result = null;
        //正常就一对值
        for (String key : jsonObject.keySet()) {
            String value = jsonObject.getString(key);
            result = inputStr.replaceAll(key,value);
        }
        return result;
    }
}
