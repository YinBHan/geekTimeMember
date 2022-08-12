import lombok.Data;

@Data
public class WebResponse<T>{

    private Integer code;

    private String msg;

    private T data;


    public static <T>WebResponse<T> createSuccessResponse(T data){
        WebResponse<T> successResp = new WebResponse<T>();
        successResp.setCode(200);
        successResp.setMsg("成功");
        successResp.setData(data);
        return successResp;
    }


    public static <T>WebResponse<T> createErrorResponse(){
        WebResponse<T> successResp = new WebResponse<T>();
        successResp.setCode(500);
        successResp.setMsg("失敗");
        return successResp;
    }
}
