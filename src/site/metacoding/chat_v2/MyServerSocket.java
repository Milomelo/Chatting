package site.metacoding.chat_v2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

public class MyServerSocket {

    // 리스너(연결받기) - 메인스레드
    ServerSocket serverSocket;
    List<고객전담스레드> 고객리스트;

    // 서버는 메시지 받아서 보내기 (클라이언트 수마다)

    public MyServerSocket() {
        try {
            serverSocket = new ServerSocket(2000);
            고객리스트 = new Vector<>(); // 동기화가 처리된 ArrayList
            while (true) {
                Socket socket = serverSocket.accept();// main 스레드
                System.out.println("클라이언트 연결됨");
                고객전담스레드 t = new 고객전담스레드(socket);
                고객리스트.add(t);
                System.out.println("고객리스트 크기 : " + 고객리스트.size());
                new Thread(t).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 내부 클래스
    class 고객전담스레드 implements Runnable {

        Socket socket;
        BufferedReader reader;
        BufferedWriter writer;
        boolean isLogin = true;

        public 고객전담스레드(Socket socket) {
            this.socket = socket;
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                // 읽는 순간에 전송해서 다른 스레드가 필요 없음
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            try {
                while (isLogin) {
                    String inputData = reader.readLine();
                    System.out.println("from 클라이언트: " + inputData);
                    for (고객전담스레드 t : 고객리스트) { // 왼쪽:ㅣ 컬랙션 타입, 오른쪽: 컬랙션
                        t.writer.write(inputData + "\n");
                        t.writer.flush();
                    }

                }
                // 메세지를 받았으니까 List<고객전담스레드> 고객리시트 <== 여기에 담김
                // 모든 클라이언트에게 메세지를 전송 (for문 돌려)

                           // 파일 또는 db에 쓴다. 파일에 쓰면 select도 힘들고 관리하기 힘듬.
                // db에 저장하는 것이 관리하기 쉬움
                // 에러가 발생할 때마다 IO를 하면 서버가 과부하가 걸림 -> 한번에 모아서 INSERT 하는 것이 좋음 bulk
                // IO를 한번만 하므로 효율이 좋다
                // 하지만 통신과 갈비지 컬랙션중 통신이 부하가 더 심해서 바로바로 처리해주는 것이 효율이 좋음
            } catch (Exception e) {
                try {
                    System.out.println("통신실패: " + e.getMessage());
                    isLogin = false;
                    고객리스트.remove(this);

                    reader.close();
                    writer.close();
                    socket.close();

                } catch (Exception e1) {
                    System.out.println("연결해제 실패" + e1.getMessage());
                }

            }

        }

    }

    public static void main(String[] args) {
        new MyServerSocket();
    }

}