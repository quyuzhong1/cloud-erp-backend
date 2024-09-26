package com.common.core.utils;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collector;

/**
 * 计算工具类
 */
@UtilityClass
public class MathUtil {

    public final int scale = 4;
    public final BigDecimal BigDecimal_100 = new BigDecimal("100");
    public final BigDecimal BigDecimal_1000 = new BigDecimal("1000");
    public final BigDecimal BigDecimal__1 = new BigDecimal("-1");
    public final BigDecimal BigDecimal_1 = new BigDecimal("1");
    public final BigDecimal BigDecimal_2 = new BigDecimal("2");
    public final BigDecimal BigDecimal_0_1 = new BigDecimal("0.1");
    public final BigDecimal OTHER_FEE_DEFAULT = new BigDecimal("0.0001");


    public final Integer ZERO = 0;
    public final Integer ONE = 1;
    public final Integer ONE_NEGATIVE = -1;
    public final Integer TWO = 2;
    public final Integer THREE = 3;
    public final Integer FOUR = 4;
    public final Integer FIVE = 5;
    public final Integer SIX = 6;
    public final Integer NUMBER_100 = 100;
    public final Integer NUMBER_1000 = 1000;
    public final Integer CONSTANT_BYTE_SIZE = 1024;
    public final Integer EX_HK_NO_COUNT = 13;
    public final Integer CUSTOMS_CODE_COUNT = 18;
    public final Integer EXPORT_MAX_COUNT = 50000;


    /**
     * 正则验证：非负整数
     */
    public static Pattern P = Pattern.compile("^\\d+$");
    /**
     * 正则验证：保留2位小数，正数
     */
    public static Pattern P2 = Pattern.compile("^[0-9]+(\\.[0-9]{1,2})?$");
    /**
     * 保留6位小数，正数
     */
    public static Pattern P6 = Pattern.compile("^[0-9]+(\\.[0-9]{1,6})?$");
    /**
     * 正则校验日期
     */
    public static Pattern P_DATE = Pattern.compile("^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))");


    /**
     * 根据对象获取Integer
     *
     * @param val
     * @return
     */
    public Integer valueOfZero(Integer val) {
        if (val == null) {
            return 0;
        }
        return val;
    }

    public Integer valueOfInteger(String val) {
        if (val == null) {
            return null;
        }
        return Integer.valueOf(val);
    }

    /**
     * Integer如果为空，返回第二个对象
     *
     * @param val1
     * @param val2
     * @return
     */
    public Integer nvl(Integer val1, Integer val2) {
        if (val1 == null) {
            return val2;
        }
        return val1;
    }

    /**
     * 根据对象获取Long
     *
     * @param value
     * @return
     */
    public Long valueOf(Long value) {
        if (value == null) {
            return 0L;
        }
        return value;
    }

    public Long valueOfLong(String value) {
        if (value == null) {
            return null;
        }
        return Long.valueOf(value);
    }

    /**
     * bigDecimal如果为空，返回第二个对象
     *
     * @param val1
     * @param val2
     * @return
     */
    public BigDecimal nvl(BigDecimal val1, BigDecimal val2) {
        if (val1 == null) {
            return val2;
        }
        return val1;
    }

    /**
     * 根据对象获取 BigDecimal
     *
     * @param object
     * @return
     */
    public BigDecimal valueOf(Object object) {
        if (object == null) {
            return BigDecimal.ZERO;
        } else if (StringUtils.isBlank(object.toString())) {
            return BigDecimal.ZERO;
        }
        BigDecimal result;
        try {
            result = new BigDecimal(String.valueOf(object).replaceAll(",", ""));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("数据类型有误，请设置正确的数值：" + object);
        }
        return result;
    }

    /**
     * 判断第一个参数是否大于第二个参数，大于返回1，小于返回-1，等于返回0
     *
     * @param val1
     * @param val2
     * @return
     */
    public int compareTo(Object val1, Object val2) {
        return compareTo(valueOf(val1), valueOf(val2));
    }

    /**
     * 判断第一个参数是否大于第二个参数，大于返回1，小于返回-1，等于返回0
     *
     * @param val1
     * @param val2
     * @return
     */
    public int compareTo(Integer val1, Integer val2) {
        val1 = val1 == null ? 0 : val1;
        val2 = val2 == null ? 0 : val2;
        if (val1.intValue() == val2.intValue()) {
            return 0;
        }
        return val1 > val2 ? 1 : -1;
    }

    /**
     * 判断第一个参数是否大于第二个参数，大于返回1，小于返回-1，等于返回0
     *
     * @param val1
     * @param val2
     * @return
     */
    public int compareTo(BigDecimal val1, BigDecimal val2) {
        if (val1 == null) {
            val1 = BigDecimal.ZERO;
        }
        if (val2 == null) {
            val2 = BigDecimal.ZERO;
        }
        return val1.compareTo(val2);
    }

    /**
     * 小数位舍入
     *
     * @param d1
     * @param scale
     * @return
     */
    public BigDecimal setScale(BigDecimal d1, int scale) {
        return d1.setScale(scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 两位小数舍入,默认两位小数
     *
     * @param d1
     * @return
     */
    public BigDecimal setScale(BigDecimal d1) {
        return MathUtil.setScale(d1, scale);
    }

    /**
     * 相加
     *
     * @param d1
     * @param d2
     * @return
     */
    public BigDecimal add(BigDecimal d1, BigDecimal d2) {
        if (d1 == null && d2 == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal newd1 = d1;
        if (newd1 == null) {
            newd1 = BigDecimal.ZERO;
        }
        BigDecimal newd2 = d2;
        if (newd2 == null) {
            newd2 = BigDecimal.ZERO;
        }
        return newd1.add(newd2);
    }

    /**
     * 相减
     *
     * @param d1
     * @param d2
     * @return
     */
    public BigDecimal subtract(BigDecimal d1, BigDecimal d2) {
        if (d1 == null && d2 == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal newd1 = d1;
        if (newd1 == null) {
            newd1 = BigDecimal.ZERO;
        }
        BigDecimal newd2 = d2;
        if (newd2 == null) {
            newd2 = BigDecimal.ZERO;
        }
        return newd1.subtract(newd2);
    }

    /**
     * 两数相乘，得出结果，该结果未四舍五入，请注意, 默认保留两位小数
     *
     * @param d1
     * @param d2
     * @return
     */
    public BigDecimal multiply(BigDecimal d1, BigDecimal d2) {
        return multiply(d1, d2, 2);
    }

    /**
     * 两数相乘，得出结果
     *
     * @param d1
     * @param d2
     * @return
     */
    public BigDecimal multiply(BigDecimal d1, BigDecimal d2, int scale) {
        if (d1 == null && d2 == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal newd1 = d1;
        if (newd1 == null) {
            newd1 = BigDecimal.ZERO;
        }
        BigDecimal newd2 = d2;
        if (newd2 == null) {
            newd2 = BigDecimal.ZERO;
        }
        BigDecimal multiply = newd2.multiply(newd1);
        return multiply.setScale(scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 乘法
     *
     * @param d1
     * @param d2
     * @return
     */
    public BigDecimal multiply(BigDecimal d1, Integer d2) {
        if (d1 == null && d2 == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal newd1 = d1;
        if (newd1 == null) {
            newd1 = BigDecimal.ZERO;
        }
        BigDecimal newd2 = null;
        if (d2 == null) {
            newd2 = BigDecimal.ZERO;
        } else {
            newd2 = new BigDecimal(d2);
        }
        return newd1.multiply(newd2);
    }

    /**
     * 除法
     *
     * @param d1
     * @param d2
     * @param scale
     * @return
     */
    public BigDecimal divide(BigDecimal d1, BigDecimal d2, int scale) {
        if (d2 == null || d2.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal newd1 = d1;
        if (newd1 == null) {
            newd1 = BigDecimal.ZERO;
        }
        return newd1.divide(d2, scale, BigDecimal.ROUND_HALF_UP);

    }

    /**
     * 除法
     *
     * @param d1
     * @param d2
     * @param scale
     * @return
     */
    public BigDecimal divide(BigDecimal d1, BigDecimal d2, int scale, int roundingMode) {
        if (d2 == null || d2.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal newd1 = d1;
        if (newd1 == null) {
            newd1 = BigDecimal.ZERO;
        }
        return newd1.divide(d2, scale, roundingMode);

    }

    /**
     * 除法（默认保留两位小数）
     *
     * @param d1
     * @param d2
     * @return
     */
    public BigDecimal divide(BigDecimal d1, BigDecimal d2) {
        return MathUtil.divide(d1, d2, scale);
    }


    /**
     * 相加
     *
     * @param d1
     * @param d2
     * @return
     */
    public Integer add(Integer d1, Integer d2) {
        if (d1 == null && d2 == null) {
            return 0;
        }
        Integer newd1 = d1;
        if (newd1 == null) {
            newd1 = 0;
        }
        Integer newd2 = d2;
        if (newd2 == null) {
            newd2 = 0;
        }
        return newd1 + newd2;
    }

    /**
     * 是否超出误差范围
     *
     * @param param1
     * @param param2
     * @param objParam 误差范围
     * @return true 超出误差范围
     */
    public Boolean diffGreaterThan(BigDecimal param1, BigDecimal param2, BigDecimal objParam) {
        return compareTo(subtract(param1, param2).abs(), objParam) > 0;
    }

    /**
     * 获取BigDecimal，如果为空时返回0
     *
     * @param value
     * @return
     */
    public static BigDecimal getValue(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 获取BigDecimal 随机数
     *
     * @param min
     * @param max
     * @return
     */
    public static BigDecimal getRandom(Integer min, Integer max, int scale) {
        float minF = min.floatValue();
        float maxF = max.floatValue();
        //生成随机数
        BigDecimal db = new BigDecimal(Math.random() * (maxF - minF) + minF);
        //返回保留两位小数的随机数。不进行四舍五入
        return db.setScale(scale, BigDecimal.ROUND_DOWN);
    }

    /**
     * 获取到未含税的值
     *
     * @param taxPrice 含税的价格
     * @param taxRate  税率 除以过100 的
     * @return java.math.BigDecimal
     * @author yl
     * @date 2023-10-23 11:03
     */
    public static BigDecimal getUntaxed(BigDecimal taxPrice, BigDecimal taxRate, int scale) {
        if (Objects.isNull(taxPrice)) {
            return BigDecimal.ZERO;
        }
        if (Objects.isNull(taxRate)) {
            taxRate = BigDecimal.ZERO;
        }
        BigDecimal divValue = taxRate.add(BigDecimal_1);
        return MathUtil.divide(taxPrice, divValue, scale);

    }

    /**
     * 获取含税值
     *
     * @param price   未税的
     * @param taxRate taxRate  税率 除以过100 的
     * @param i
     * @return
     */
    public static BigDecimal getTaxValue(BigDecimal price, BigDecimal taxRate, int scale) {
        BigDecimal multiplyTax = MathUtil.add(taxRate, MathUtil.BigDecimal_1);
        return MathUtil.multiply(price, multiplyTax,scale);
    }

    public static BigDecimal getBigDecimalByStr(String priceStr) {
        if (StringUtils.isBlank(priceStr)) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(priceStr);
    }

    private static final Set<Collector.Characteristics> CHARACTERISTICS = Collections.emptySet();


    @SuppressWarnings("unchecked")
    private static <I, R> Function<I, R> check() {
        return i -> (R) i;
    }


    @SuppressWarnings("hiding")
    static class CollectorImpl<T, A, R> implements Collector<T, A, R> {

        private final Supplier<A> supplier;
        private final BiConsumer<A, T> accumulator;
        private final BinaryOperator<A> combiner;
        private final Function<A, R> finisher;
        private final Set<Characteristics> characteristics;

        CollectorImpl(Supplier<A> supplier, BiConsumer<A, T> accumulator, BinaryOperator<A> combiner, Function<A, R> finisher, Set<Characteristics> characteristics) {
            this.supplier = supplier;
            this.accumulator = accumulator;
            this.combiner = combiner;
            this.finisher = finisher;
            this.characteristics = characteristics;
        }

        CollectorImpl(Supplier<A> supplier, BiConsumer<A, T> accumulator, BinaryOperator<A> combiner, Set<Characteristics> characteristics) {
            this(supplier, accumulator, combiner, check(), characteristics);
        }

        @Override
        public Supplier<A> supplier() {
            return supplier;
        }

        @Override
        public BiConsumer<A, T> accumulator() {
            return accumulator;
        }

        @Override
        public BinaryOperator<A> combiner() {
            return combiner;
        }

        @Override
        public Function<A, R> finisher() {
            return finisher;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return characteristics;
        }
    }

    /**
     * 求和方法
     *
     * @param mapper
     * @param <T>
     * @return
     */
    public static <T> Collector<T, ?, BigDecimal> summingBigDecimal(MathUtil.ToBigDecimalFunction<? super T> mapper) {
        return new MathUtil.CollectorImpl<>(
                () -> new BigDecimal[]{new BigDecimal(0)},
                (a, t) -> {
                    a[0] = a[0].add(mapper.applyAsBigDecimal(t), MathContext.DECIMAL32);
                },
                (a, b) -> {
                    a[0] = a[0].add(b[0], MathContext.DECIMAL32);
                    return a;
                },
                a -> a[0], CHARACTERISTICS
        );
    }

    /**
     * 求最大值，这里的最小MIN值，作为初始条件判断值，如果某些数据范围超过百亿，可以根据需求换成 Long.MIN_VALUE 或者 Double.MIN_VALUE
     *
     * @param mapper
     * @param <T>
     * @return
     */
    public static <T> Collector<T, ?, BigDecimal> maxBy(MathUtil.ToBigDecimalFunction<? super T> mapper) {
        return new MathUtil.CollectorImpl<>(
                () -> new BigDecimal[]{new BigDecimal(Integer.MIN_VALUE)},
                (a, t) -> {
                    a[0] = a[0].max(mapper.applyAsBigDecimal(t));
                },
                (a, b) -> {
                    a[0] = a[0].max(b[0]);
                    return a;
                },
                a -> a[0], CHARACTERISTICS
        );
    }

    /**
     * 求多个数中最大值，为空返回0
     * @param values
     * @return
     */
    public static BigDecimal findMax(BigDecimal... values) {
        if (values == null || values.length == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal max = values[0];
        for (BigDecimal value : values) {
            if (value.compareTo(max) > 0) {
                max = value;
            }
        }
        return max;
    }
    /**
     * 求最小值，这里的最大MAX值，作为初始条件判断值，如果某些数据范围超过百亿，可以根据需求换成 Long.MAX_VALUE 或者 Double.MAX_VALUE
     *
     * @param mapper
     * @param <T>
     * @return
     */
    public static <T> Collector<T, ?, BigDecimal> minBy(MathUtil.ToBigDecimalFunction<? super T> mapper) {
        return new MathUtil.CollectorImpl<>(
                () -> new BigDecimal[]{new BigDecimal(Integer.MAX_VALUE)},
                (a, t) -> {
                    a[0] = a[0].min(mapper.applyAsBigDecimal(t));
                },
                (a, b) -> {
                    a[0] = a[0].min(b[0]);
                    return a;
                },
                a -> a[0], CHARACTERISTICS
        );
    }

    /**
     * 求平均，并且保留小数，返回一个平均值
     *
     * @param mapper
     * @param newScale
     * @param roundingMode
     * @param <T>
     * @return
     */
    public static <T> Collector<T, ?, BigDecimal> averagingBigDecimal(MathUtil.ToBigDecimalFunction<? super T> mapper,
                                                                      int newScale, int roundingMode) {
        return new MathUtil.CollectorImpl<>(
                () -> new BigDecimal[]{new BigDecimal(0), new BigDecimal(0)},
                (a, t) -> {
                    a[0] = a[0].add(mapper.applyAsBigDecimal(t));
                    a[1] = a[1].add(BigDecimal.ONE);
                },
                (a, b) -> {
                    a[0] = a[0].min(b[0]);
                    return a;
                },
                a -> a[0].divide(a[1], MathContext.DECIMAL32).setScale(newScale, roundingMode), CHARACTERISTICS
        );
    }

    // 自定义函数
    @FunctionalInterface
    public interface ToBigDecimalFunction<T> {

        BigDecimal applyAsBigDecimal(T t);

    }

    /**
     * 校验 BigDecimal位数
     * @param number
     * @param integerNum 整数位数
     * @return
     */
    public static boolean isValidNumber(BigDecimal number,int integerNum) {
        String numberStr = number.toPlainString();
        int dotIndex = numberStr.indexOf('.');
        if (dotIndex == -1) {
            return numberStr.length() <= integerNum;
        } else {
            return dotIndex <= integerNum;
        }
    }

}
