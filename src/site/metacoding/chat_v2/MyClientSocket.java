package site.metacoding.chat_v2;

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
            socket = new Socket("127.0.0.1", 2000);

            sc = new Scanner(System.in);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 새로운 스레드(읽기전용)

            new Thread(new 읽기전담스레드()).start();

            // 메인 스레드 (쓰기전용)
            while (true) {
                String keyboardInputData = sc.nextLine();
                writer.write(keyboardInputData + "\n"); // 버퍼에 내용 담기
                writer.flush(); // 버버페 담긴 것을 stream으로 흘려보내기

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class 읽기전담스레드 implements Runnable {

        @Override
        public void run() {
            try {
                while (true) {
                    String inputData = reader.readLine();
                    System.out.println("받은 메세지:" + inputData);
                }

            } catch (Exception e) {
                // TODO: handle exception
            }

        }
    }

    public static void main(String[] args) {
        new MyClientSocket();
    }
}