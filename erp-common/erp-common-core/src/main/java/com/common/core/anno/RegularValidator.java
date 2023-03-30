package com.common.core.anno;

import com.common.core.enums.FieldFormatPatternTypeEnum;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 正则的实现
 *
 * @author Administrator
 * @Classname RegularValidator
 * @Description TODO
 * @Date 2023-03-14 15:00
 * @Created by yl
 */
public class RegularValidator implements ConstraintValidator<RegularValid, Object> {

    private String formatPattern;
    private final static Pattern bank_reg = Pattern.compile("^\\d{16,19}$");


    @Override
    public void initialize(RegularValid constraintAnnotation) {
        formatPattern = constraintAnnotation.formatPattern();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
        if (Objects.isNull(value) || StringUtils.isBlank(value.toString())) {
            return true;
        }
        switch (formatPattern) {
            //手机号
            case FieldFormatPatternTypeEnum.MOBILE:
                String mobileRegex = FieldFormatPatternTypeEnum.getRegularByCode(FieldFormatPatternTypeEnum.MOBILE);
                return Pattern.matches(mobileRegex, value.toString());
            //网址
            case FieldFormatPatternTypeEnum.URL:
                String urlRegex = FieldFormatPatternTypeEnum.getRegularByCode(FieldFormatPatternTypeEnum.URL);
                return Pattern.matches(urlRegex, value.toString());
            //邮箱
            case FieldFormatPatternTypeEnum.MAILBOX:
                if (value.toString().length() > 30) {
                    return false;
                }
                String mailRegex = FieldFormatPatternTypeEnum.getRegularByCode(FieldFormatPatternTypeEnum.MAILBOX);
                return Pattern.matches(mailRegex, value.toString());

            //银行卡号
            case FieldFormatPatternTypeEnum.BANK_CARD_NO:
                if (value != null) {
                    String card = value.toString();
                    int length = card.length();
                    return isBankCard(card) && getBankCardCheckCode(card.substring(0, length - 1), card.charAt(length - 1));
                }
                return false;
        }
        return false;
    }


    public static boolean isBankCard(String dataString) {
        return bank_reg.matcher(dataString).matches();
    }

    /**
     * 银行卡校验位计算，并验证
     *
     * @param nonCheckCodeCardId
     * @param c
     * @return
     */
    private static boolean getBankCardCheckCode(String nonCheckCodeCardId, char c) {
        char[] chs = nonCheckCodeCardId.trim().toCharArray();
        int luhmSum = 0;
        for (int i = chs.length - 1, j = 0; i >= 0; i--, j++) {
            int k = chs[i] - '0';
            if (j % 2 == 0) {
                k *= 2;
                k = k / 10 + k % 10;
            }
            luhmSum += k;
        }

        return ((luhmSum % 10 == 0) ? '0' : (char) ((10 - luhmSum % 10) + '0')) == c;
    }

}
