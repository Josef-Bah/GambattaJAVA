package gambatta.tn.ui.components;

import gambatta.tn.entites.tournois.rencontre;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class BracketMatchCard extends VBox {

    public BracketMatchCard(rencontre r) {
        this.setSpacing(10);
        this.setPadding(new Insets(15));
        this.getStyleClass().add("bracket-card");
        if (r.getEquipeA() == null && r.getEquipeB() == null) {
            this.getStyleClass().add("bracket-tbd");
        }
        this.setMinWidth(220);
        this.setMaxWidth(220);

        // Header Stage
        Label lblStage = new Label(r.getStage() != null ? r.getStage() : "MATCH");
        lblStage.getStyleClass().add("bracket-stage-label");
        this.getChildren().add(lblStage);

        // Team A Row
        HBox rowA = createTeamRow(
            r.getEquipeA() != null ? r.getEquipeA().getNom() : "TBD",
            r.getScoreA() != null ? r.getScoreA().toString() : "-"
        );

        // Team B Row
        HBox rowB = createTeamRow(
            r.getEquipeB() != null ? r.getEquipeB().getNom() : "TBD",
            r.getScoreB() != null ? r.getScoreB().toString() : "-"
        );

        this.getChildren().addAll(rowA, rowB);

        // Info footer (Date or Status)
        Label lblInfo = new Label(r.getPlayedAt() != null ? r.getPlayedAt().toLocalDate().toString() : "Date \u00e0 d\u00e9finir");
        lblInfo.getStyleClass().add("bracket-date");
        this.getChildren().add(lblInfo);
    }

    private HBox createTeamRow(String name, String score) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label lblName = new Label(name);
        lblName.getStyleClass().add("bracket-team-name");
        HBox.setHgrow(lblName, Priority.ALWAYS);
        lblName.setMaxWidth(Double.MAX_VALUE);

        Label lblScore = new Label(score);
        lblScore.getStyleClass().add("bracket-score");
        
        row.getChildren().addAll(lblName, lblScore);
        return row;
    }
}
