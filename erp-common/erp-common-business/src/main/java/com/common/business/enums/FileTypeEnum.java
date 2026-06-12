package com.common.business.enums;

public enum FileTypeEnum {
    /**
     * 输出类型
     */
    PDF("pdf", "application/pdf", ".pdf"),
    PNG("png", "image/png", ".png"),
    HTML("html", "application/html", ".html"),
    XLS("xls", "application/vnd.ms-excel", ".xls"),
    XLSX("xlsx", "application/vnd.ms-excel", ".xlsx"),
    DOC("doc", "application/msword;charset=utf-8", ".doc"),
    DOCX("docx", "application/msword;charset=utf-8", ".docx"),
    XML("xml", "application/pdf", ".xml"),
    RTF("rtf", "application/pdf", ".rtf"),

    //非导出类型
    JRXML("jrxml", "", ".jrxml"),
    JASPER("jasper", "", ".jasper"),
    ;

    /**
     * 类型名称
     */
    private String code;
    /**
     * 类型内容
     */
    private String typeContent;
    /**
     * 类型后缀
     */
    private String typeSuffix;

    FileTypeEnum(String code) {
        this.code = code;
    }

    FileTypeEnum(String code, String typeContent, String typeSuffix) {
        this.code = code;
        this.typeContent = typeContent;
        this.typeSuffix = typeSuffix;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTypeContent() {
        return typeContent;
    }

    public void setTypeContent(String typeContent) {
        this.typeContent = typeContent;
    }

    public String getTypeSuffix() {
        return typeSuffix;
    }

    public void setTypeSuffix(String typeSuffix) {
        this.typeSuffix = typeSuffix;
    }

    /**
     * 根据类型的名称，返回类型的枚举实例。
     *
     * @param code 类型编码
     * @return 类型枚举
     */
    public static FileTypeEnum fromTypeName(String code) {
        for (FileTypeEnum docType : FileTypeEnum.values()) {
            if (docType.getCode().equals(code)) {
                return docType;
            }
        }
        return null;
    }
}
