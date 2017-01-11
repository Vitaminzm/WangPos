package com.symboltech.wangpos.print;

public class KposPrinterManager {
    //字体属性定义
    //支持异或加权 比如0x1000|0x0100|0x0010|0x0001，表示默认字体，普通大小，基本形态，未缩放
  
   /**
    * 字体定义    
    * FONT_FAMILY_DEFAULT ：默认字体
    * FONT_FAMILY_1 ：其他字体（如无，可以不实现）
    * FONT_FAMILY_2 ：其他字体（如无，可以不实现）
    */
    public static final int FONT_FAMILY_DEFAULT = 0x1000;
    public static final int FONT_FAMILY_1 = 0x2000;
    public static final int FONT_FAMILY_2 = 0x4000;    
    
   /**
    * 字大小定义    
    * FONT_SIZE_NOMAL ：普通  单位为2x
    * FONT_SIZE_SMALL ：小号  单位为1x（如SMALL为字号为x,其他字号等比放大）
    * FONT_SIZE_BIG   ：大号  单位为4x
    */
    public static final int FONT_SIZE_NORMAL = 0x0100;
    public static final int FONT_SIZE_SMALL = 0x0200;
    public static final int FONT_SIZE_BIG = 0x0400; 
    
   /**
    * 字样式定义    
    * FONT_STYLE_BASIC : 基本
    * FONT_STYLE_BOLD : 加粗
    * FONT_STYLE_ITALIC : 斜体
    * 注：加粗和斜体可以异或存在
    */
    public static final int FONT_STYLE_BASIC = 0x0010;
    public static final int FONT_STYLE_BOLD = 0x0020;
    public static final int FONT_STYLE_ITALIC = 0x0040; 

   /**
    * 字缩放定义    
    * FONT_SCALE_NORMAL : 未缩放
    * FONT_SCALE_DOUBLEHIGHT : 双倍高
    * FONT_SCALE_DOUBLEWIDTH : 双倍宽
    * 注：双倍高和双倍宽可以异或存在，有些打印机实现为双倍大小     
    */
    public static final int FONT_SCALE_NORMAL = 0x0001;
    public static final int FONT_SCALE_DOUBLEHIGHT = 0x0002;
    public static final int FONT_SCALE_DOUBLEWIDTH = 0x0004;     
     
    /**
     * 内容对齐定义   
     * CONTENT_ALIGN_LEFT : 左对齐
     * CONTENT_ALIGN_CENTER : 居中对齐
     * CONTENT_ALIGN_RIGHT : 右对齐
     */
     public static final int CONTENT_ALIGN_LEFT = 0xA001;
     public static final int CONTENT_ALIGN_CENTER = 0xA010;
     public static final int CONTENT_ALIGN_RIGHT = 0xA100;    

   /**
    * 切纸定义
    * PAPER_CUT_HALF : 半切
    * PAPER_CUT_ALL : 全切
    */
    public static final int PAPER_CUT_HALF = 0xC101;
    public static final int PAPER_CUT_ALL = 0xC111;  
}
