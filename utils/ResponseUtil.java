
import java.util.Optional;

public class ResponseUtil {

    public static <T> T getDataOrNull(ResultVO<T> result) {
        if (isSuccess(result)) {
            return result.getData();
        }
        return null;
    }

    public static <T> T getDataOrThrow(ResultVO<T> result) {
        if (isSuccess(result)) {
            return result.getData();
        }
        throw new RuntimeException(Optional.ofNullable(result).map(ResultVO::getMsg).orElse("request fail"));
    }

    public static String getMsg(ResultVO<?> result) {
        return (result != null && result.getMsg() != null) ? result.getMsg() : "request fail";
    }

    public static boolean isSuccess(ResultVO<?> result) {
        return result != null && result.getCode() == 1 && result.getData() != null;
    }

    private ResponseUtil() {
    }
}
