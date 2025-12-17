package com.erp.model.oms.enums;

import com.alibaba.fastjson.JSONObject;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
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
import java.util.stream.Collectors;

@Getter
@ToString
@AllArgsConstructor
public enum SkuMappingRuleEnum implements EnumMessage{
    COMPLETE_SKU("completeSku","完整SKU",v-> new ArrayList<>(),(type,regex,v)-> v),
    IGNORE_PREFIXES_AND_SUFFIXES("ignorePrefixesAndSuffixes","忽略前缀/后缀",SkuMappingRuleEnum::getIgnorePrefixesAndSuffixesRegex,SkuMappingRuleEnum::handleRegex),
    IGNORE_FIRST_AND_LAST_DIGITS("ignoreFirstAndLastDigits","忽略前几位/后几位",SkuMappingRuleEnum::getIgnoreFirstAndLastDigitsRegex,SkuMappingRuleEnum::handleRegex),
    EXTRACT_FIRST_TO_LAST_DIGITS("extractFirstToLast","截取第几位到第几位",SkuMappingRuleEnum::getExtractFirstToLastRegex,SkuMappingRuleEnum::handleSubStrRegex),
    EXTRACT_BETWEEN_START_AND_END("extractBetweenStartAndEnd","截取两个字符之间的SKU",SkuMappingRuleEnum::getExtractBetweenStartAndEndRegex,SkuMappingRuleEnum::handleRegex),
    MATCH_COMBINE("matchCombine","捆绑商品智能拆分匹配",v-> new ArrayList<>(),SkuMappingRuleEnum::handleRegex),
    NO_MATCH("noMatch","无需匹配",v-> new ArrayList<>(),SkuMappingRuleEnum::handleRegex),
    ;
    private final String code;
    private final String name;
    //将参数处理成正则表达式
    private final Function<SkuMappingRuleDTO.RuleDTO, List<String>> regexMethod;
    //返回正则处理后的结果
    private final TriFunction<String,String,String,String> handleRegexMethod;

    @FunctionalInterface
    public interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }
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
     * 起始符结束符开始位置
     */
    @Getter
    @AllArgsConstructor
    public enum SkuMappingSymbolicSideEnum implements EnumMessage{
        LEFTMOST_SIDE("leftMostSide","最左侧"),
        FAR_RIGHT("rightMostSide","最右侧"),
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
        SYMBOLIC_1("~","~",false),
        SYMBOLIC_2("@","@",false),
        SYMBOLIC_3("#","#",false),
        SYMBOLIC_4("%","%",false),
        SYMBOLIC_5("^","^",false),
        SYMBOLIC_6("&","&",false),
        SYMBOLIC_7("+","+",true),
        SYMBOLIC_8("*","*",true),
        SYMBOLIC_9("×","×",false),
        SYMBOLIC_10("-","-",false),
        SYMBOLIC_11("_","_",false),
        SYMBOLIC_12("[","]",true),
        SYMBOLIC_13("]","]",true),
        SYMBOLIC_14("\\","\\",true),
        SYMBOLIC_15("/","/",false),
        SYMBOLIC_16("|","|",false),
        SYMBOLIC_17(".",".",true),
        ;
        private final String code;
        private final String name;
        //是否需要转义
        private final Boolean isEscape;
    }

    private final List<String> needEscepe = Arrays.asList("+","*","[","]","\\",".");

    private static List<String> getIgnorePrefixesAndSuffixesRegex(SkuMappingRuleDTO.RuleDTO commonDTO){
        List<String> list = new ArrayList<>(commonDTO.getRuleContentList().size());
        for(SkuMappingRuleDTO.RuleConditionsDTO ruleConditionsDTO : commonDTO.getRuleContentList()){
            if(StringUtils.isBlank(ruleConditionsDTO.getIgnorePrefix()) && StringUtils.isBlank(ruleConditionsDTO.getIgnoringSuffixes())){
                throw new ServiceException(ApiError.ERROR_SKU_MAPPING_RULE_NULL);
            }
            String prefix = StringUtils.isBlank(ruleConditionsDTO.getIgnorePrefix())?"":ruleConditionsDTO.getIgnorePrefix();
            String suffixes = StringUtils.isBlank(ruleConditionsDTO.getIgnoringSuffixes())?"":ruleConditionsDTO.getIgnoringSuffixes();
            prefix = escapeSpecialCharacters(prefix);
            suffixes = escapeSpecialCharacters(suffixes);
            String prefixRegex = StringUtils.isNotBlank(prefix)?"\"^"+ prefix+"(.*?)$\"" : "";
            list.add(prefixRegex);
            String suffixesRegex = StringUtils.isNotBlank(suffixes)?"\"^(.*?)"+suffixes+"$\"" : "";
            list.add(suffixesRegex);
        }
        return list;
    }

    private static List<String> getIgnoreFirstAndLastDigitsRegex(SkuMappingRuleDTO.RuleDTO commonDTO){
        List<String> list = new ArrayList<>(commonDTO.getRuleContentList().size());
        for(SkuMappingRuleDTO.RuleConditionsDTO ruleConditionsDTO : commonDTO.getRuleContentList()){
            if(Objects.isNull(ruleConditionsDTO.getIgnoringBeforePosition()) && Objects.isNull(ruleConditionsDTO.getIgnoringAfterPosition())){
                throw new ServiceException(ApiError.ERROR_SKU_MAPPING_RULE_NULL);
            }
            int prefix = Objects.isNull(ruleConditionsDTO.getIgnoringBeforePosition())?0:ruleConditionsDTO.getIgnoringBeforePosition();
            int suffixes = Objects.isNull(ruleConditionsDTO.getIgnoringAfterPosition())?0:ruleConditionsDTO.getIgnoringAfterPosition();
            String regex = "^.{"+prefix+"}(.*).{"+suffixes+"}$";
            list.add(regex);
        }
        return list;
    }

    private static List<String> getExtractFirstToLastRegex(SkuMappingRuleDTO.RuleDTO commonDTO){
        List<String> list = new ArrayList<>(commonDTO.getRuleContentList().size());
        for(SkuMappingRuleDTO.RuleConditionsDTO ruleConditionsDTO : commonDTO.getRuleContentList()){
            if(Objects.isNull(ruleConditionsDTO.getInterceptionFrontPosition()) && Objects.isNull(ruleConditionsDTO.getInterceptionBehindPosition())){
                throw new ServiceException(ApiError.ERROR_SKU_MAPPING_RULE_NULL);
            }
            int prefix = Objects.isNull(ruleConditionsDTO.getInterceptionFrontPosition())?1:ruleConditionsDTO.getInterceptionFrontPosition();
            int suffixes = Objects.isNull(ruleConditionsDTO.getInterceptionBehindPosition())?Integer.MAX_VALUE:ruleConditionsDTO.getInterceptionBehindPosition();
            if(prefix < 1 || suffixes < 1){
                throw new ServiceException("截取位数不能小于1");
            }
            if (suffixes<prefix){
                throw new ServiceException("后面位数不能小于前面位数");
            }
            prefix --;
//            String regex = ".{" + prefix + "}(.{" + (suffixes - prefix) + "}).*";
            //不使用正则，方便控制
            String regex = "{" + prefix + ":"+suffixes+"}";
            list.add(regex);
        }
        return list;
    }
    private static List<String> getExtractBetweenStartAndEndRegex(SkuMappingRuleDTO.RuleDTO commonDTO){
        List<String> list = new ArrayList<>(commonDTO.getRuleContentList().size());
        String waitHandleRegex;
        for(SkuMappingRuleDTO.RuleConditionsDTO ruleConditionsDTO : commonDTO.getRuleContentList()){
            /**
             分四种情况
             1.起始符取左侧，结束符取右侧
             2.起始符取右侧，结束符取右侧
             3.起始符取左侧，结束符取左侧
             4.起始符取右侧，结束符取左侧
             【#】替换起始符，【%】替换结束符
             */
            if(ruleConditionsDTO.getValidStartingSymbolPosition().equals(SkuMappingSymbolicSideEnum.LEFTMOST_SIDE.code) &&
                    ruleConditionsDTO.getValidEndSymbolPosition().equals(SkuMappingSymbolicSideEnum.FAR_RIGHT.code) ){
                waitHandleRegex = "【#】(.*?)【%】(?!.*【%】)";
            } else if (ruleConditionsDTO.getValidStartingSymbolPosition().equals(SkuMappingSymbolicSideEnum.FAR_RIGHT.code) &&
                    ruleConditionsDTO.getValidEndSymbolPosition().equals(SkuMappingSymbolicSideEnum.FAR_RIGHT.code) ) {
                waitHandleRegex = "【#】((?:(?!【#】).)*?)【%】(?!.*【%】)";
            } else if (ruleConditionsDTO.getValidStartingSymbolPosition().equals(SkuMappingSymbolicSideEnum.LEFTMOST_SIDE.code) &&
                    ruleConditionsDTO.getValidEndSymbolPosition().equals(SkuMappingSymbolicSideEnum.LEFTMOST_SIDE.code) ) {
                waitHandleRegex = "【#】(.*?)(?=【%】.*$)";
            } else if (ruleConditionsDTO.getValidStartingSymbolPosition().equals(SkuMappingSymbolicSideEnum.FAR_RIGHT.code) &&
                    ruleConditionsDTO.getValidEndSymbolPosition().equals(SkuMappingSymbolicSideEnum.LEFTMOST_SIDE.code) ) {
                waitHandleRegex = "【#】(?!.*【#】)(.*?)【%】";
            }else{
                throw new ServiceException("找不到起始符规则");
            }
            String finalRegex = waitHandleRegex;
            if(StringUtils.isBlank(ruleConditionsDTO.getStartingSymbol()) && StringUtils.isBlank(ruleConditionsDTO.getEndSymbol())){
                throw new ServiceException(ApiError.ERROR_SKU_MAPPING_RULE_NULL);
            }
            //起始符或终止符为空，正则都不一样
            if(StringUtils.isNotBlank(ruleConditionsDTO.getStartingSymbol()) && StringUtils.isBlank(ruleConditionsDTO.getEndSymbol())){
                if(ruleConditionsDTO.getValidStartingSymbolPosition().equals(SkuMappingSymbolicSideEnum.LEFTMOST_SIDE.code)){
                    finalRegex = "【#】(.*)";
                }else{
                    finalRegex = ".*【#】(.*)";
                }
            }
            if(StringUtils.isBlank(ruleConditionsDTO.getStartingSymbol()) && StringUtils.isNotBlank(ruleConditionsDTO.getEndSymbol())){
                if(ruleConditionsDTO.getValidEndSymbolPosition().equals(SkuMappingSymbolicSideEnum.LEFTMOST_SIDE.code)){
                    finalRegex = "([^【%】]*)【%】";
                }else{
                    finalRegex = "(.*)【%】";
                }
            }
            String startSymbol = ruleConditionsDTO.getStartingSymbol();
            String endSymbol =ruleConditionsDTO.getEndSymbol();
            //转义
            startSymbol = escapeSpecialCharacters(startSymbol);
            endSymbol = escapeSpecialCharacters(endSymbol);
            String regex = finalRegex.replaceAll("【#】",startSymbol).replaceAll("【%】",endSymbol);
            if(ruleConditionsDTO.getValidStartingSymbolPosition().equals(ruleConditionsDTO.getValidEndSymbolPosition())
            &&  StringUtils.isNotBlank(startSymbol) && StringUtils.isNotBlank(endSymbol) &&startSymbol.equals(endSymbol)){
                regex = "";
            }
            list.add(regex);
        }
        return list;
    }
    private static List<String> handleReplaceSpecifiedCharacters(SkuMappingRuleDTO.ExtendRuleDTO commonDTO){
        List<String> list = new ArrayList<>(commonDTO.getExtendRuleContentList().size());
        Set<String> valueSet = commonDTO.getExtendRuleContentList().stream().map(SkuMappingRuleDTO.ExtendRuleConditionsDTO::getBeforeReplacingCharacters).collect(Collectors.toSet());
        if(valueSet.size() != commonDTO.getExtendRuleContentList().size()){
            throw new ServiceException("不能有重复的待替换值");
        }
        //字符串替换，不需要正则，存放json key为需要替换的值，value为替换值
        for(SkuMappingRuleDTO.ExtendRuleConditionsDTO extendRuleConditionsDTO : commonDTO.getExtendRuleContentList()){
            if(StringUtils.isBlank(extendRuleConditionsDTO.getBeforeReplacingCharacters())){
                throw new ServiceException("待替换值不能为空");
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(extendRuleConditionsDTO.getBeforeReplacingCharacters(),extendRuleConditionsDTO.getAfterReplacingCharacters());
            list.add(jsonObject.toJSONString());
        }
        return list;
    }

    private static String handleSubStrRegex(String ruleType,String regex,String inputStr){
        SkuMappingRuleEnum skuMappingRuleEnum = EnumMessage.getByCode(SkuMappingRuleEnum.class, ruleType);
        if(EXTRACT_FIRST_TO_LAST_DIGITS!=skuMappingRuleEnum){
            return inputStr;
        }
        if(StringUtils.isBlank(regex)){
            return inputStr;
        }
        regex = regex.replaceAll("\"","");
        //提取数字
        Pattern pattern = Pattern.compile("\\{(\\d+):(\\d+)\\}");
        Matcher matcher = pattern.matcher(regex);
        if (matcher.find()) {
            int prefix = Integer.parseInt(matcher.group(1));
            int suffixes = Integer.parseInt(matcher.group(2));
            int strLength = inputStr.length();
            if(strLength == 0){
                return inputStr;
            }
            if(prefix >= strLength){
                return "";
            }
            suffixes = Math.min(suffixes, inputStr.length());
            inputStr = inputStr.substring(prefix, suffixes);
            return inputStr;
        } else {
            return inputStr;
        }
    }

    private static String handleRegex(String ruleType,String regex,String inputStr){
        SkuMappingRuleEnum skuMappingRuleEnum = EnumMessage.getByCode(SkuMappingRuleEnum.class, ruleType);
        if(StringUtils.isBlank(regex)){
            if(skuMappingRuleEnum.equals(SkuMappingRuleEnum.EXTRACT_BETWEEN_START_AND_END)){
                return "";
            }
            return inputStr;
        }
        regex = regex.replaceAll("\"","");
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.find()) {
            return matcher.group(1);
        }else{
            if(skuMappingRuleEnum.equals(SkuMappingRuleEnum.IGNORE_FIRST_AND_LAST_DIGITS) || skuMappingRuleEnum.equals(SkuMappingRuleEnum.EXTRACT_BETWEEN_START_AND_END)){
                return "";
            }
            return inputStr;
        }
    }

    private static String handleReplace(String json,String inputStr){
        JSONObject jsonObject = JSONObject.parseObject(json);
        String result = null;
        //正常就一对值
        for (String key : jsonObject.keySet()) {
            String value = jsonObject.getString(key);
            //转义
            SkuMappingSymbolicEnum keySymbolEnum = EnumMessage.getByCode(SkuMappingRuleEnum.SkuMappingSymbolicEnum.class,key);
            SkuMappingSymbolicEnum valueSymbolicEnum = EnumMessage.getByCode(SkuMappingRuleEnum.SkuMappingSymbolicEnum.class,value);
            if(Objects.nonNull(keySymbolEnum) && keySymbolEnum.isEscape){
                key = Pattern.quote(key);
            }
            if(Objects.nonNull(valueSymbolicEnum) && valueSymbolicEnum.isEscape){
                value = Matcher.quoteReplacement(value);
            }
            result = inputStr.replaceAll(key,value);
        }
        return result;
    }

    public static String escapeSpecialCharacters(String str) {
        if(StringUtils.isBlank(str)){
            return str;
        }
        StringBuilder escapedStr = new StringBuilder();
        // 需要转义的特殊字符
        String specialCharacters = ".$|()[{^?*+\\";
        for (char ch : str.toCharArray()) {
            // 如果字符是特殊字符，则进行转义
            if (specialCharacters.indexOf(ch) != -1) {
                escapedStr.append("\\").append(ch);
            } else {
                escapedStr.append(ch);
            }
        }
        return escapedStr.toString();
    }
}
