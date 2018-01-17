package org.kondrak.tangent;

import java.util.List;

public final class ListUtil {

    private ListUtil() { /* empty */}

    public static boolean contains(List<String> list, String val) {
        for(String s : list) {
            if(val.equals(s)) {
                return true;
            }
        }
        return false;
    }
}
