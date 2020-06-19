package main;

public class Start {
    public static void main(String[] args) {
        int a = Integer.MAX_VALUE;
        int b = 1;
        int c = a + b;
        int d = 1;
        System.out.println(c);
        System.out.println(~(d << 28));
        System.out.println(268435456 >> 28);
        System.out.println();
        System.out.println(-268435454 & 0x7FFFFFFF);
        System.out.println(-268435455 & 0x7FFFFFFF);
        System.out.println(-268435456 & 0x7FFFFFFF);
        System.out.println(-268435457 & 0x7FFFFFFF);
        System.out.println(-268435458 & 0x7FFFFFFF);
    }
}
