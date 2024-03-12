package op.wawa.utils;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

import java.util.HashMap;
import java.util.Map;

public class QQUtils {

    public static String QQNumber;

    /*******QQ窗口文本内容前缀****eg：qqexchangewnd_shortcut_prefix_123456(其中123456即为qq号)*****/
    private static final String QQ_WINDOW_TEXT_PRE = "qqexchangewnd_shortcut_prefix_";

    private static final  User32 user32 = User32.INSTANCE;

    public interface User32 extends StdCallLibrary {
        User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);
        interface WNDENUMPROC extends StdCallCallback {
            boolean callback(Pointer hWnd, Pointer arg);
        }
        boolean EnumWindows(WNDENUMPROC lpEnumFunc, Pointer arg);

        int GetWindowTextA(Pointer hWnd, byte[] lpString, int nMaxCount);
    }


    /**
     * 取两个文本之间的文本值
     * @param text 源文本 比如：欲取全文本为 12345
     * @param left 文本前面
     * @param right 后面文本
     * @return 返回 String
     */
    public static String getSubString(String text, String left, String right) {
        String result = "";
        int zLen;
        if (left == null || left.isEmpty()) {
            zLen = 0;
        } else {
            zLen = text.indexOf(left);
            if (zLen > -1) {
                zLen += left.length();
            } else {
                zLen = 0;
            }
        }
        int yLen = text.indexOf(right, zLen);
        if (yLen < 0 || right == null || right.isEmpty()) {
            yLen = text.length();
        }
        result = text.substring(zLen, yLen);
        return result;
    }


    /******
     * 获取当前登录qq的信息
     * @return map集合
     */
    public static Map<String,String> getLoginQQList(){
        final Map<String,String> map = new HashMap<>(5);

        user32.EnumWindows(new User32.WNDENUMPROC() {
            public boolean callback(Pointer hWnd, Pointer userData) {
                byte[] windowText = new byte[512];
                user32.GetWindowTextA(hWnd, windowText, 512);
                String wText = Native.toString(windowText);
                if(_filterQQInfo(wText)){
                    map.put(hWnd.toString(), wText.substring(wText.indexOf(QQ_WINDOW_TEXT_PRE) + QQ_WINDOW_TEXT_PRE.length()));
                }
                QQNumber = getSubString(String.valueOf(map),"=","}");
                return true;
            }
        }, null);
        return map;
    }

    /******
     * getLoginQQList但是返回QQ号
     * @return map集合
     */
    public static String getLoginQQNumber(){
        getLoginQQList();
        return QQNumber;
    }


    /****
     * 过滤有效qq窗体信息
     * @param windowText
     * @return 是否为qq窗体信息
     */
    private static boolean _filterQQInfo(String windowText){

        if(windowText.startsWith(QQ_WINDOW_TEXT_PRE))
            return true;
        return false;
    }

    public static void getQQ() {
        getLoginQQList();
    }
}
