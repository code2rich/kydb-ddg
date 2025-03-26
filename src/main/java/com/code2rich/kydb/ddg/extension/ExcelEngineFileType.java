package com.code2rich.kydb.ddg.extension;

import cn.smallbun.screw.core.engine.EngineFileType;

/**
 * Excel引擎文件类型扩展
 */
public enum ExcelEngineFileType {
    /**
     * Excel文件类型
     */
    EXCEL;

    /**
     * 转换为screw引擎文件类型
     * @return 引擎文件类型
     */
    public static EngineFileType toEngineFileType() {
        // 由于screw库本身不支持Excel，我们这里扩展一个自定义类型
        // 在实际处理时会在DatabaseDocumentGenerator中特殊处理
        return EngineFileType.HTML; // 临时返回HTML类型，实际上会被覆盖
    }
}