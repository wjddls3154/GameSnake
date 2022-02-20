import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.Random;

public class SnakeGame {

    // 클래스 생성
    static class MyFrame extends JFrame {

        // x와 y 위치 가지고 있다가, 전달해주는 역할
        static class XY {
            int x;
            int y;
            public XY(int x, int y) {
                this.x = x;
                this.y = y;
            }
        }

        static JPanel panelNorth; // 북쪽 메시지 영역
        static JPanel panelCenter; // 바둑판 모양의 뱀이 지나다니는 게임판 영역
        static JLabel labelTitle; // 타이틀 영역
        static JLabel labelMessage; // 메시지 영역
        static JPanel[][] panels = new JPanel[20][20]; // 2차 배열, UI 바둑판 모양 만들어주는
        static  int[][] map = new int[20][20]; // 실질적 데이터를 가지고 있는 2차 배열, 과일을 9 폭탄을 8 로 표현 0이면 Blank
        static LinkedList<XY> snake = new LinkedList<XY>();
        static int dir = 3; // move direction, 0 - up, 1 - down, 2 - left, 3 - right 방향
        static int score = 0; // 점수판
        static int time = 0; // 현재 게임 시간 (1초 단위)
        static int timeTickCount = 0; // per 200ms, 뱀 움직이는 시간
        static  Timer timer = null;


        // 생성자
        public MyFrame(String title) {
            super(title);
            this.setSize(400,500);
            this.setVisible(true);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            initUI(); // UI 초기화
            makeSnakeList(); // 스네이크 몸 만들어서 넣을 리스트
            startTimer(); // start timer
            setKeyListener(); // 키보드 입력 받을 것
            makeFruit(); // 과일을 만드는 코드 (뱀이 과일을 먹으면 꼬리가 길어짐)

        }

        // 과일 만드는 메소드
        public void  makeFruit() {
            Random rand = new Random();
            // x 0~19 값, y 0~19 값 랜덤
            int randX = rand.nextInt(19);
            int randY = rand.nextInt(19);
            map[randX][randY] = 9; // 과일은 9
        }

        // 키보드 입력을 받아서 방향을 바꾸는 메소드
        public void setKeyListener() {
            this.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_UP) { // move direction, 0 - up, 1 - down, 2 - left, 3 - right 방향
                        if(dir != 1)
                        dir = 0;
                    } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        if(dir != 0)
                        dir = 1;
                    } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        if(dir != 3)
                        dir = 2;
                    } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        if(dir != 2)
                        dir = 3;
                    }
                }
            });
        }


        // 타이머 메소드
        public void startTimer() {
            timer = new Timer(200, new ActionListener() { // 1초에 200ms 간격
                @Override
                public void actionPerformed(ActionEvent e) {
                    timeTickCount += 1;

                    if(timeTickCount % 5 == 0) { // 5개가 모이면, 1초 올라간다.
                        time ++; // 1초 증가
                    }
                        moveSnake(); // move snake
                        updateUI(); // UI 갱신
                }
            });
            timer.start(); // 진짜 시작
        }

        // 뱀을 움직이는 메소드
        public void moveSnake() {
            XY headXY = snake.get(0); // 뱀의 head get
            int headX = headXY.x; // 뱀의 몸
            int headY = headXY.y; // 뱀의 꼬리

            if(dir == 0) { // 0 - up, 1 - down, 2 - left, 3 - right 방향
                boolean isColl = checkCollision( headX, headY-1);
                if(isColl == true) {
                    labelMessage.setText("Game Over !");
                    timer.stop();
                    return;
                }
                snake.add(0, new XY(headX, headY-1)); // add 머리 remove 꼬리 하면 뱀이 이동하는 것 처럼 보인다.
                snake.remove(snake.size() - 1);
            }
            else if ( dir == 1) {
                boolean isColl = checkCollision( headX, headY+1);
                if(isColl == true) {
                    labelMessage.setText("Game Over !");
                    timer.stop();
                    return;
                }
                snake.add(0, new XY(headX, headY+1));
                snake.remove(snake.size() - 1);
            } else if (dir == 2) {
                boolean isColl = checkCollision( headX-1, headY);
                if(isColl == true) {
                    labelMessage.setText("Game Over !");
                    timer.stop();
                    return;
                }
                snake.add(0, new XY(headX-1, headY));
                snake.remove(snake.size() - 1);
            } else if (dir == 3) {
                boolean isColl = checkCollision( headX+1, headY);
                if(isColl == true) {
                    labelMessage.setText("Game Over !");
                    timer.stop();
                    return;
                }
                snake.add(0, new XY(headX+1, headY));
                snake.remove(snake.size() - 1);
            }

        }

        // 충돌 체크
        public boolean checkCollision(int headX, int headY) {
            if( headX<0 || headX>19 || headY<0 || headY>19) { // 벽에 충돌한것
                return true;
            }

            // 뱀이 자기 몸에 부딪혀도 게임오버
            for( XY xy : snake) {
                if(headX == xy.x && headY == xy.y) {
                    return true;
                }
            }

            // 과일에 충돌하면
            if(map[headY][headX] == 9) { // 배열은 행과 열이 다르다. 그래서 Y,X, UI는 X,Y
                map[headY][headX] = 0; // 0으로 초기화
                addTail(); // 뱀의 꼬리 길이 증가
                makeFruit(); // 다시 랜덤한 과일 생성
                score += 100; // 100점 점수 추가
            }
            return false;
        }

        // 꼬리 추가하는 메소드
        public void addTail() {
            int tailX = snake.get(snake.size()-1).x;
            int tailY = snake.get(snake.size()-1).y;
            int tailX2 = snake.get(snake.size()-2).x;
            int tailY2 = snake.get(snake.size()-2).y;

            if (tailX<tailX2) { // 오른쪽으로 가는 경우 : 왼쪽에 꼬리가 있어서 왼쪽에 추가해야한다.
                snake.add( new XY(tailX-1,tailY));
            } else if (tailX>tailX2) { // 왼쪽으로 가는 경우 : 오른쪽에 꼬리가 있어서 오른쪽에 추가
                snake.add( new XY(tailX+1,tailY));
            } else if (tailY<tailY2) { // 위쪽으로 가는 경우 : 아래쪽에 꼬리가 있기에 아래쪽에 추가
                snake.add( new XY(tailX,tailY-1));
            } else if (tailY>tailY2) { // 아래쪽으로 가는 경우 : 위쪽에 꼬리가 있기에 위쪽에 추가
                snake.add( new XY(tailX,tailY+1));
            }
        }


        // UI 갱신해주는 메소드
        public void updateUI() {
            labelTitle.setText("Score: " + score + " Time: " + time);

            // Clear tile (panel)
            for (int i = 0; i < 20; i++) {
                for (int j = 0; j < 20; j++) {
                    if(map[i][j] == 0 ) { // Blank
                        panels[i][j].setBackground(Color.GRAY);
                    }
                    else if (map[i][j] == 9) {
                        panels[i][j].setBackground(Color.GREEN); // 열매
                    }
                }
            }
            // 뱀 그려주기
            int index = 0;
            for( XY xy : snake ) {
                if(index == 0 ) { // 뱀의 머리
                    panels[xy.y][xy.x].setBackground(Color.RED);
                } else { // 뱀의 body(s), tail
                    panels[xy.y][xy.x].setBackground(Color.BLUE);
                }

                index++;
            }

        }


        // 뱀의 몸 만드는 메소드
        public void makeSnakeList() {
            snake.add(new XY(10,10)); // 뱀의 머리 영역
            snake.add(new XY(9,10)); // 뱀의 몸 영역
            snake.add(new XY(8,10)); // 뱀의 꼬리 영역
        }

        // UI 초기화 메소드
        public void initUI() {
            this.setLayout(new BorderLayout());

            panelNorth = new JPanel();
            panelNorth.setPreferredSize(new Dimension(400,100));
            panelNorth.setBackground(Color.BLACK);
            panelNorth.setLayout(new FlowLayout());

            // North 영역
            labelTitle = new JLabel("Score : 0, Time : 0sec");
            labelTitle.setPreferredSize(new Dimension(400,50));
            labelTitle.setFont(new Font("TimesRoman",Font.BOLD,20));
            labelTitle.setForeground(Color.WHITE);
            labelTitle.setHorizontalAlignment(JLabel.CENTER);
            panelNorth.add(labelTitle);

            labelMessage = new JLabel("Eat Fruit ! ");
            labelMessage.setPreferredSize(new Dimension(400,20));
            labelMessage.setFont(new Font("TimesRoman",Font.BOLD,20));
            labelMessage.setForeground(Color.YELLOW);
            labelMessage.setHorizontalAlignment(JLabel.CENTER);
            panelNorth.add(labelMessage);

            this.add("North",panelNorth);

            // Center 영역
            panelCenter = new JPanel();
            panelCenter.setLayout(new GridLayout(20,20)); // 바둑판 모양은 GridLayout 사용이 편함
            for (int i = 0; i < 20; i++) { // 열(가로)
                for (int j = 0; j < 20; j++) { // 행(세로)
                    map[i][j] = 0; // 0 = blank(빈공간)

                    // 패널 UI 넣기
                    panels[i][j] = new JPanel(); // 실제 패널 만들어짐
                    panels[i][j].setPreferredSize(new Dimension(20,20)); // 사이즈 조정
                    panels[i][j].setBackground(Color.GRAY); // 색상 조정
                    panelCenter.add(panels[i][j]); // 패널을 패널센터에 하나씩 넣어준다.
                }
            }
                this.add("Center",panelCenter); // 패널센터 만들어진걸 add 해준다.
                this.pack(); // 빈공간 없애줌
        }

    }

    public static void main(String[] args) {
        new MyFrame(" Snake Game ");
    }

}
