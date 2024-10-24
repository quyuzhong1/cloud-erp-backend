package com.erp.model.plm.dto;

import com.erp.model.plm.enums.ProductContentEnum;
import com.itextpdf.text.Element;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Getter
@Setter
public class PrintEanDTO {

    /**
     * 宽
     */
    @NotNull(message = "宽度不能为空")
    private Integer width;
    /**
     * 高
     */
    @NotNull(message = "高度不能为空")
    private Integer height;

    /**
     * 打印格式
     */
    @NotBlank(message = "打印格式不能为空")
    private String printFormat;

    /**
     * 是否打印文本
     */
    @NotNull(message = "是否打印文本不能为空")
    private Boolean isPrintText;

    /**
     * 文本位置
     * @see Element
     */
    private Integer textPosition;

    /**
     * 文本内容
     */
    private List<String> textContent;

    /**
     * 自定义文本
     */
    @Length(max = 20, message = "仅限大小写字母与汉字 20字")
    private String customText;

    /**
     * sku数据
     */
    private List<PrintSkuEanDTO> printSkuEanList;

    public void setTextContent(List<String> textContent) {
        this.textContent = textContent.stream()
                .map(ProductContentEnum::ofCode) // 将字符串转换为枚举常量
                .sorted(Comparator.comparing(ProductContentEnum::getOrder)) // 根据 order 排序
                .map(ProductContentEnum::getCode) // 转换回字符串
                .collect(Collectors.toList());
    }

    /**
     * 打印sku参数
     */
    @Getter
    @Setter
    public static class PrintSkuEanDTO {

        /**
         * skuNo
         */
        private String skuNo;
        /**
         * ean
         */
        private String ean;
        /**
         * 品名
         */
        private String productName;
        /**
         * 数量
         */
        private Integer qty;
    }

}
