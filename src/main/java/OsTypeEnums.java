import cn.hutool.core.util.StrUtil;

public enum OsTypeEnums {

    WIN(),
    MAC(),
    LINUX();


    public  String processCommondResult(String commondResult){
        if (this.equals(OsTypeEnums.WIN)){
            return  commondResult.split("REG_SZ")[1].trim();
        }
        return null;
    }


    public String getSavePath(String savePath){
        if (this.equals(WIN)){
            return savePath+"chromedriver.zip";
        }
        return StrUtil.EMPTY;
    }

    public String getDirverName(){
        if (this.equals(WIN)){
           return "chromedriver.exe";
        }
        return StrUtil.EMPTY;
    }


    public static OsTypeEnums getOsType(String name){
        if (name.toLowerCase().contains("window")){
            return WIN;
        }else if (name.toLowerCase().contains("linux")){
            return LINUX;
        }else if (name.toLowerCase().contains("mac")){
            return MAC;
        }
        throw new RuntimeException("系统"+name+"暂不支持");
    }
}
