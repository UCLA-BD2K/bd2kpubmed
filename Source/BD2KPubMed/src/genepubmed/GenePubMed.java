package genepubmed;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * This class
 * <p>
 * Contact: Tevfik U. Dincer - dincer@ucla.edu
 */
public class GenePubMed extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("GUI.fxml"));
        Scene scene = new Scene(root);
//        scene.getStylesheets().add("file:res/Modena.css");

        stage.setScene(scene);
        stage.setTitle("BD2KPubMed");
//        stage.getIcons().add(new Image("file:res/images/icon.png"));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private class Publication {

        String pmID = null;
        String gene = null;

        public Publication(String pmID, String gene) {
            this.pmID = pmID;
            this.gene = gene;
        }

        public String getPmID() {
            return pmID;
        }

        public String getGene() {
            return gene;
        }

        @Override
        public String toString() {
            return "gene: " + gene + " pmID: " + pmID;
        }

    }

}
