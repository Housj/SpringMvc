package com.hsj.sample;

/**
 * @author sergei
 * @create 2020-04-16
 */
public class IdentityHashCodeTest {

    public static void main(String[] args) {
        Object c,d;
        c = 2;
        d = 3;
//        System.out.println(c.getClass().getName().compareTo(d.getClass().getName()));
        System.out.println(System.identityHashCode(c));
        System.out.println(System.identityHashCode(d));
    }

    public static int tie(Object a,Object b){
        int d;
        if (a == null || b == null || (d = a.getClass().getName(). compareTo(b.getClass().getName())) == 0)
            d = (System.identityHashCode(a) <= System.identityHashCode(b) ? -1 : 1);
        return d;
    }
}
