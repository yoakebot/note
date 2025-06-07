
import java.util.HashMap;
import java.util.Map;

public class EnumUtil {

    private static final Map<Class<? extends CommonEnum>, Map<Integer, String>> ENUM_CACHE = new HashMap<>();

    public static <T extends CommonEnum> String getDesc(int code, Class<T> enumClass) {
        Map<Integer, String> map = ENUM_CACHE.computeIfAbsent(enumClass, cls -> {
            Map<Integer, String> inner = new HashMap<>();
            for (CommonEnum e : cls.getEnumConstants()) {
                inner.put(e.getCode(), e.getDesc());
            }
            return inner;
        });
        return map.get(code);
    }

    private EnumUtil() {
    }
}
