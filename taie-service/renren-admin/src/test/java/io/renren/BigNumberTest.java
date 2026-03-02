package io.renren;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BigNumberTest {
    public static void main(String[] args) {

        //fromType=<SPOT>,toType=<CONTRACT>,userTransferDTO=<{"coin":"TRX","amount":28.500000000000000000,"fromWalletType":"SPOT","toWalletType":"CONTRACT"}>
//        BigDecimal amount = new BigDecimal("28.500000000000000000");
//        System.out.println(amount.scale());
//        amount = amount.setScale(18, RoundingMode.DOWN);
//        System.out.println(amount.toPlainString());
//        System.out.println(amount.compareTo(BigDecimal.ZERO) <= 0);
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("1");
        list.add("2");
        list.add("3");
        List<String> collect = list.stream().distinct().collect(Collectors.toList());
        System.out.println(collect);
    }
}
