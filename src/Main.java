import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by Edsuns@qq.com on 2020/7/26.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("[抖音无水印视频解析]");
        DouyinGet douyinGet = new DouyinGet();
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入抖音分享口令或链接：");
        String input = scanner.nextLine();
        System.out.println("解析中...");
        String[] result = douyinGet.parse(input);
        System.out.println("解析结果：");
        System.out.println(Arrays.toString(result));
        scanner.close();
    }
}
