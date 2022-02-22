package site.metacoding.chat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class MyClientSocket {

    Socket socket;
    BufferedWriter writer;
    Scanner sc;
    BufferedReader reader;

    public MyClientSocket() {
        try {
            socket = new Socket("localhost", 2000);
            writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream()));

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 2. 이 부분 추가 (클라이언트 소켓쪽)
            new Thread(() -> {
                while (true) {
                    try {
                        String inputData = reader.readLine();
                        System.out.println("받은 메시지 : " + inputData);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            // 스캐너 달고 (반복x)
            sc = new Scanner(System.in);
            // 키보드로부터 입력 받는 부분 (반복)
            while (true) {
                String inputData = sc.nextLine();
                writer.write(inputData + "\n");
                writer.flush();

                break;
            }
            System.out.println("끝");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new MyClientSocket();
    }
}