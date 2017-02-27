package genepubmed;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class JournalChooserController implements Initializable {

    @FXML
    private TableColumn<JournalElement, String> col1;
    @FXML
    private TableColumn<JournalElement, Boolean> col2;
    @FXML
    private Button doneButton;
    @FXML
    private Button cancelButton;

    private ObservableList<JournalElement> journalNames;
    @FXML
    private TableView<JournalElement> table;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        journalNames = FXCollections.observableArrayList();
        journalNames.add(new JournalElement("hi", false));
        journalNames.add(new JournalElement("ho", true));
        journalNames.add(new JournalElement("ha", false));

//        col1.setCellValueFactory(new PropertyValueFactory<JournalElement, String>("name"));
//        col2.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<JournalElement, Boolean>, ObservableValue<Boolean>>() {
//
//            @Override
//            public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<JournalElement, Boolean> p) {
//                return p.getValue().getObservableSelected();
//            }
//        });
//        col2.setCellFactory(new Callback<TableColumn<JournalElement, Boolean>, TableCell<JournalElement, Boolean>>() {
//
//            @Override
//            public TableCell<JournalElement, Boolean> call(TableColumn<JournalElement, Boolean> p) {
//                return new CheckBoxTableCell<>(new Callback<Integer, ObservableValue<Boolean>>);
//            }
//        });
        
        col1.setCellValueFactory(new PropertyValueFactory<JournalElement, String>("name"));
        col2.setCellValueFactory(new PropertyValueFactory<JournalElement, Boolean>("selected"));
        
//        col2.setCellFactory(new Callback<TableColumn<JournalElement, Boolean>, TableCell<JournalElement, Boolean>>() {
//
//            @Override
//            public TableCell<JournalElement, Boolean> call(TableColumn<JournalElement, Boolean> p) {
//                return new CheckBoxCell(p.getCellObservableValue(p.getCellValueFactory().call(p)));
//            }
//        });
                
        table.setItems(journalNames);
    }
    
    private class CheckBoxCell extends TableCell<JournalElement, Boolean>{
        private CheckBox cb;
        private ObservableValue<Boolean> ov; 

        public CheckBoxCell(ObservableValue<Boolean> ovb) {
            ov = ovb;
            cb = new CheckBox();
            cb.setAlignment(Pos.CENTER);
            
            setAlignment(Pos.CENTER);
            setGraphic(cb);
        }

        @Override
        protected void updateItem(Boolean t, boolean bln) {
            super.updateItem(t, bln);
            ov = getTableColumn().getCellObservableValue(getIndex());
            cb.selectedProperty().bindBidirectional((BooleanProperty) ov);
        }
    }

    public void setJournalArray(ObservableList<JournalElement> list) {
        journalNames = list;
    }

    public class JournalElement {

        BooleanProperty selected = new SimpleBooleanProperty(false);
        StringProperty name = new SimpleStringProperty("");

        public BooleanProperty selectedProperty() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = new SimpleBooleanProperty(selected);
        }

        public StringProperty nameProperty() {
            return name;
        }

        public void setName(String name) {
            this.name = new SimpleStringProperty(name);
        }

        public JournalElement(String name, boolean selected) {
            this.name = new SimpleStringProperty(name);
            this.selected = new SimpleBooleanProperty(selected);
        }

    }

}
