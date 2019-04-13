import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;


/**
 * @Author: qq979249745
 * @Date: 2019/3/22 21:41
 * @Version 1.0
 */
public class AttackPlane extends Application {
    private Random random=new Random();
    private final int width=600;
    private final int height=800;
    private boolean start=true;
    private int score,high=100;
    private Pane pane;
    private MediaPlayer mediaPlayer, boomP;
    private TranslateTransition tt;

    public static void main(String[] args) {
        launch(args);
    }

    class Bullet extends ImageView {
        Bullet(double x,double y) {
            super("bullet.png");
            setY(y-10);
            setX(x-5);
            setFitHeight(20);
            setFitWidth(10);
            tt=new TranslateTransition(Duration.millis(2000),this);
            tt.setToY(-height);
            tt.setAutoReverse(false);
            tt.play();
        }

    }
    class Enemy extends ImageView {
        Enemy(){
            super("enemy.png");
            setX(random.nextInt(width-40));
            setFitHeight(60);
            setFitWidth(40);
            tt=new TranslateTransition(Duration.millis(5000),this);
            tt.setToY(height+20);
            tt.setFromY(-60);
            tt.setAutoReverse(false);
            tt.play();
        }

    }
    @Override
    public void start(Stage primaryStage) {
        pane = new Pane();
        pane.setBackground(new Background(new BackgroundImage(new Image("bg.jpg"), BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,new BackgroundSize(width,height,true,true,false,true)
        )));
        mediaPlayer=new MediaPlayer(new Media(AttackPlane.class.getResource("bgm.mp3").toString()));
        //bulletP=new MediaPlayer(new Media(AttackPlane.class.getResource("bullet.mp3").toString()));


        mediaPlayer.setAutoPlay(true);
        mediaPlayer.setCycleCount(1000);
        mediaPlayer.play();

        start();

        Scene scene = new Scene(pane, width, height);
        primaryStage.setScene(scene);
        primaryStage.setTitle("打飞机1.0 作者：16201533 ");
        //primaryStage.setResizable(false);
        primaryStage.show();
    }
    private void start(){
        pane.getChildren().clear();
        start=true;score=0;
        ImageView plane=new ImageView("hero.png");
        plane.setFitHeight(80);
        plane.setFitWidth(65);
        plane.setX(width/2.0+40);
        plane.setY(height-80*2);
        plane.setOnMouseDragged(event -> {
            plane.setX(event.getSceneX()-32);
            plane.setY(event.getSceneY()-20);
        });
        pane.getChildren().add(plane);
        List<Bullet> bullets=new ArrayList<>();
        List<Enemy> enemys=new ArrayList<>();

        new Thread(() -> {
            while (start) {
                if (score/high==1){
                    bullets.add(new Bullet(plane.getX() + 17, plane.getY()));
                    bullets.add(new Bullet(plane.getX() + 45, plane.getY()));
                }else if (score/high>1){
                    bullets.add(new Bullet(plane.getX() + 32, plane.getY()));
                    bullets.add(new Bullet(plane.getX() + 17, plane.getY()));
                    bullets.add(new Bullet(plane.getX() + 45, plane.getY()));
                }else
                    bullets.add(new Bullet(plane.getX() + 32, plane.getY()));
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        new Thread(() -> {
            while (start) {
                for(int i=0;i<=score/high;++i){
                    Enemy enemy=new Enemy();
                    enemys.add(enemy);
                    TranslateTransition tt=new TranslateTransition(Duration.millis(5000),enemy);
                    tt.setToY(height+20);
                    tt.setFromY(-60);
                    tt.setAutoReverse(false);
                    tt.play();
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        new Thread(() -> {
            while (start) {
                Platform.runLater(()->{
                    pane.getChildren().removeAll(bullets);
                    pane.getChildren().removeAll(enemys);
                    for (int i=0;i<enemys.size();++i) {
                        Enemy enemy=enemys.get(i);
                        double enemyY=enemy.getY()+enemy.getTranslateY(),enemyX=enemy.getX();
                        if (enemyY>height){
                            enemys.clear();
                            start=false;dialog();break;
                        }else if(enemyY+enemy.getFitHeight()>plane.getY()&&enemyY<plane.getY()
                                &&enemyX+enemy.getFitWidth()>plane.getX()&&enemyX<plane.getX()){
                            enemys.clear();start=false;dialog();break;
                        }
                        else {
                            for (int j=0;j<bullets.size();++j) {
                                Bullet bullet=bullets.get(j);
                                double bulletY=bullet.getY()+bullet.getTranslateY(),bulletX=bullet.getX();
                                //System.out.println(bulletY);
                                if (bulletY<0)
                                    bullets.remove(bullet);
                                else if (enemyY+enemy.getFitHeight()>bulletY&&enemyY<bulletY
                                        &&enemyX+enemy.getFitWidth()>bulletX&&enemyX<bulletX){
                                    bullets.remove(bullet);
                                    enemys.remove(enemy);
                                    boomP =new MediaPlayer(new Media(AttackPlane.class.getResource("boom.mp3").toString()));
                                    boomP.play();
                                    ++score;
                                }
                            }
                        }
                    }
                    pane.getChildren().addAll(bullets);
                    pane.getChildren().addAll(enemys);
                });
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public boolean dialog(){
        Alert _alert = new Alert(Alert.AlertType.CONFIRMATION,"你打死了"+score+"架飞机",new ButtonType("取消", ButtonBar.ButtonData.NO),
                new ButtonType("确定", ButtonBar.ButtonData.YES));
        _alert.setTitle("确认");
        _alert.setHeaderText("你个辣鸡，你死了！");
        Optional<ButtonType> _buttonType = _alert.showAndWait();
        if(_buttonType.get().getButtonData().equals(ButtonBar.ButtonData.YES)){
            start();
            return true;
        }
        else {
            start();
            return true;
        }
    }
}
