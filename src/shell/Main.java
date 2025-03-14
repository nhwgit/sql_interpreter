package shell;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("SQL 명령어를 입력하세요. 명령어 끝에는 ;이 필요합니다. (종료: 'exit' 입력)");

        StringBuilder inputBuilder = new StringBuilder();

        while (true) {
            System.out.print("SQL> ");
            String line = scanner.nextLine().trim();

            if (line.equalsIgnoreCase("exit")) {
                System.out.println("프로그램을 종료합니다.");
                scanner.close();
                return; // 프로그램 종료
            }

            inputBuilder.append(line).append(" ");

            // ';'이 입력되면 즉시 실행
            if (line.endsWith(";")) {
                String totalSql = inputBuilder.toString().trim();
                inputBuilder.setLength(0); // 버퍼 초기화

                // SQL 명령어 실행
                String[] partialSql = totalSql.split(";");
                for (String command : partialSql) {
                    command = command.trim();
                    if (!command.isEmpty()) {
                        Handler handler = new Handler(command);
                        handler.interpreter();
                    }
                }
            }
        }
    }
}
