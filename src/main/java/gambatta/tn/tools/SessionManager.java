package gambatta.tn.tools;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.event.EventHandler;

public class SessionManager {

    private static final int TIMEOUT_SECONDS = 15 * 60;
    private static final int WARNING_SECONDS  = 13 * 60;
    private static final double BANNER_WIDTH  = 720;

    private static Timeline                   timeline;
    private static Popup                      warningPopup;
    private static Scene                      managedScene;
    private static Runnable                   onTimeoutCallback;
    private static int                        elapsedSeconds = 0;

    private static EventHandler<MouseEvent>   mouseMoveHandler;
    private static EventHandler<MouseEvent>   mouseClickHandler;
    private static EventHandler<KeyEvent>     keyHandler;

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Start inactivity tracking on the given scene.
     * onTimeout is called on the JavaFX thread when 15 minutes of inactivity elapses.
     */
    public static void start(Scene scene, Runnable onTimeout) {
        stop();
        managedScene      = scene;
        onTimeoutCallback = onTimeout;
        elapsedSeconds    = 0;

        buildWarningPopup();
        attachActivityListeners(scene);
        startTimeline();
    }

    /** Cancel the session timeout and clean up all listeners. */
    public static void stop() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        if (warningPopup != null && warningPopup.isShowing()) {
            warningPopup.hide();
        }
        if (managedScene != null) {
            if (mouseMoveHandler  != null) managedScene.removeEventFilter(MouseEvent.MOUSE_MOVED,   mouseMoveHandler);
            if (mouseClickHandler != null) managedScene.removeEventFilter(MouseEvent.MOUSE_CLICKED,  mouseClickHandler);
            if (keyHandler        != null) managedScene.removeEventFilter(KeyEvent.KEY_PRESSED,      keyHandler);
        }
        managedScene      = null;
        onTimeoutCallback = null;
        elapsedSeconds    = 0;
        mouseMoveHandler  = null;
        mouseClickHandler = null;
        keyHandler        = null;
    }

    /** Reset the inactivity counter and hide the warning banner if showing. */
    public static void reset() {
        elapsedSeconds = 0;
        if (warningPopup != null && warningPopup.isShowing()) {
            warningPopup.hide();
        }
    }

    // ─── Timeline ─────────────────────────────────────────────────────────────

    private static void startTimeline() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            elapsedSeconds++;
            if (elapsedSeconds == WARNING_SECONDS) {
                showWarningBanner();
            }
            if (elapsedSeconds >= TIMEOUT_SECONDS) {
                doLogout();
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    // ─── Activity listeners ───────────────────────────────────────────────────

    private static void attachActivityListeners(Scene scene) {
        mouseMoveHandler  = e -> reset();
        mouseClickHandler = e -> reset();
        keyHandler        = e -> reset();

        scene.addEventFilter(MouseEvent.MOUSE_MOVED,  mouseMoveHandler);
        scene.addEventFilter(MouseEvent.MOUSE_CLICKED, mouseClickHandler);
        scene.addEventFilter(KeyEvent.KEY_PRESSED,    keyHandler);
    }

    // ─── Warning banner (Popup — not a Stage) ────────────────────────────────

    private static void buildWarningPopup() {
        HBox banner = new HBox(20);
        banner.setAlignment(Pos.CENTER);
        banner.setPadding(new Insets(14, 28, 14, 28));
        banner.setPrefWidth(BANNER_WIDTH);
        banner.setStyle(
            "-fx-background-color: #FFD700;" +
            "-fx-background-radius: 0 0 14 14;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 14, 0, 0, 6);"
        );

        Label msg = new Label("⚠  Votre session expire dans 2 minutes — Cliquez pour rester connecté");
        msg.setStyle(
            "-fx-text-fill: #08111f;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;"
        );

        Button stayBtn = new Button("Rester connecté");
        stayBtn.setStyle(
            "-fx-background-color: #08111f;" +
            "-fx-text-fill: #FFD700;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 20 8 20;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: rgba(0,0,0,0.2);" +
            "-fx-border-radius: 8;"
        );
        stayBtn.setOnAction(e -> reset());

        banner.getChildren().addAll(msg, stayBtn);

        warningPopup = new Popup();
        warningPopup.setAutoHide(false);
        warningPopup.getContent().add(banner);
    }

    private static void showWarningBanner() {
        if (managedScene == null) return;
        Stage stage = (Stage) managedScene.getWindow();
        if (stage == null || !stage.isShowing()) return;

        double sceneOffsetX = managedScene.getX();
        double sceneOffsetY = managedScene.getY();
        double sceneWidth   = managedScene.getWidth();
        double popupX = stage.getX() + sceneOffsetX + (sceneWidth - BANNER_WIDTH) / 2;
        double popupY = stage.getY() + sceneOffsetY;

        warningPopup.show(stage, popupX, popupY);

        HBox banner = (HBox) warningPopup.getContent().get(0);
        FadeTransition fade = new FadeTransition(Duration.millis(350), banner);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    // ─── Logout ───────────────────────────────────────────────────────────────

    private static void doLogout() {
        Runnable callback = onTimeoutCallback;
        stop();
        if (callback != null) {
            callback.run();
        }
    }
}
