import java.util.Arrays;

public class Demo {

    public static void main(String[] args) {

        int[] shuzu = {1, 2, 8, 4, 5, 6, 7};

        Arrays.sort(shuzu);

        for(int i=0; i<shuzu.length; i++){

            System.out.println(" 数组下标为" + i + "的数字为： " + shuzu[i]);
        }


    }
}
