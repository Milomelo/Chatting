package site.metacoding.chat_v3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

// JWP = 재원프로토콜
// 1. 최초 메시지는 username으로 체킹

// 2. 구분자 : 
// 3. ALL:메시지
// 4. CHAT:아이디:메시지

public class MyServerSocket {

    // 리스너 (연결받기) - 메인스레드
    ServerSocket serverSocket;
    List<고객전담스레드> 고객리스트;

    // 서버는 메시지 받아서 보내기 (클라이언트 수마다)

    public MyServerSocket() {
        try {
            serverSocket = new ServerSocket(2000);
            고객리스트 = new Vector<>(); // 동기화가 처리된 ArrayList
            while (true) {
                Socket socket = serverSocket.accept(); // main 스레드
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

        String username;
        Socket socket;
        BufferedReader reader;
        BufferedWriter writer;
        boolean isLogin;

        public 고객전담스레드(Socket socket) {
            this.socket = socket;

            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ALL:머시기
        public void chatPublic(String msg) {
            try {
                for (고객전담스레드 t : 고객리스트) { // 왼쪽 : 컬렉션 타입, 오른쪽 : 컬렉션
                    if (t != this) {
                        t.writer.write(t.socket.getRemoteSocketAddress() + username + ":" + msg + "\n");
                        t.writer.flush();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // CHAT:최주호:안녕
        public void chatPrivate(String receiver, String msg) {
            try {
                for (고객전담스레드 t : 고객리스트) { // 왼쪽 : 컬렉션 타입, 오른쪽 : 컬렉션
                    if (t.username.equals(receiver)) {
                        t.writer.write(t.socket.getRemoteSocketAddress() + "[귓속말]" + username + ":" + msg + "\n");
                        t.writer.flush();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // 재원 프로토콜 검사기
        // ALL:안녕
        // CHAT:재원:안녕
        public void jwp(String inputData) {
            // 1. 프로토콜 분리
            String[] token = inputData.split(":");
            String protocol = token[0];
            if (protocol.equals("ALL")) {
                String msg = token[1];
                chatPublic(msg);
            } else if (protocol.equals("CHAT")) {
                String receiver = token[1];
                String msg = token[2];
                chatPrivate(receiver, msg);
            } else { // 프로토콜 통과 못함.
                System.out.println("프로토콜 없음");
            }
        }

        @Override
        public void run() {

            // 최초 메시지는 username이다.
            try {
                username = reader.readLine();
                isLogin = true;
            } catch (Exception e) {
                isLogin = false;
                System.out.println("username을 받지 못했습니다.");
            }

            while (isLogin) {
                try {
                    String inputData = reader.readLine();

                    // 메시지 받았으니까 List<고객전담스레드> 고객리스트 <== 여기에 담긴
                    // 모든 클라이언트에게 메시지 전송 (for문 돌려서!!)
                    jwp(inputData);
                } catch (Exception e) {
                    try {
                        System.out.println("통신 실패 : " + e.getMessage());
                        isLogin = false;
                        고객리스트.remove(this);
                        reader.close();
                        writer.close();
                        socket.close();
                    } catch (Exception e1) {
                        System.out.println("연결해제 프로세스 실패 " + e1.getMessage());
                    }
                }

            }
        }

    }

    // 192.168.0.132
    public static void main(String[] args) {
        new MyServerSocket();
    }
}