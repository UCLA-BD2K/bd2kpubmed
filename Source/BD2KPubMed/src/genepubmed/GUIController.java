package genepubmed;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.tc33.jheatchart.HeatChart;

/**
 * This is the GUI controller associated with the GUI.fxml JavaFX file. All of the program logic is
 * implemented here, including data input, output and processing.
 * <p>
 * Contact: Tevfik U. Dincer - dincer@ucla.edu
 */
public class GUIController implements Initializable {

    @FXML
    private Font x1;
    @FXML
    private Insets x2;
    @FXML
    private TextField pmFileDirField;
    @FXML
    private Button pmDirButton;
    @FXML
    private TextField gpFileDirField;
    @FXML
    private Button gpDirButton;
    @FXML
    private TextField journalField;
    @FXML
    private Button journalChooseButtonnt;
    @FXML
    private TextField taxField;
    @FXML
    private Button runButton;
    @FXML
    private TextArea consoleField;

    private SimpleStringProperty console = new SimpleStringProperty("");
    String consoleStr = "";

    File pubmedFile = null;
    File geneFile = null;
    String publicationString = null;
    String taxonomy = null;

    HashMap<String, List<String>> testMap;

    @FXML
    private Button testButton;
    @FXML
    private CheckBox justTrends;
    @FXML
    private Insets aa;
    @FXML
    private Insets aa1;
    @FXML
    private Insets aa2;
    @FXML
    private Insets aa3;
    @FXML
    private Font x3;
    @FXML
    private ChoiceBox<String> rankingChoiceBox;
    @FXML
    private ChoiceBox<String> normalizationChoiceBox;
    @FXML
    private Insets aa5;

    /**
     * Setting the black background for the console on the Stage.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set console color, font and style
        consoleField.setStyle("-fx-text-fill: gray;"
                + "-fx-background-color: black;"
                + "-fx-font-family: monospaced;"
                + "-fx-font-size: 13;");
    }

    /**
     * Choose PubMed file button action.
     * @param event
     */
    @FXML
    private void pmDirButtonAction(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select the file from PubMed");
        File f = fc.showOpenDialog(pmDirButton.getScene().getWindow());
        if (f != null) {
            pmFileDirField.setText(f.toString());
        }
    }

    /**
     * Choose gene2pubmed file button action.
     * @param event
     */
    @FXML
    private void gpDirButtonAction(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select the gene2pubmed file");
        File f = fc.showOpenDialog(gpDirButton.getScene().getWindow());
        if (f != null) {
            gpFileDirField.setText(f.toString());
        }
    }

    /**
     * @deprecated @param event
     */
    @FXML
    private void journalChooseButtonAction(ActionEvent event) {
        ObservableList<JournalChooserController.JournalElement> journals = FXCollections.observableArrayList();
        //journals.add(new JournalChooserController.JournalElement("Nature", false));

        Parent root;
        try {
            URL fFile = getClass().getResource("journalChooser.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader();
            root = fxmlLoader.load(fFile);
            JournalChooserController controller = fxmlLoader.getController();
            controller.setJournalArray(journals);
            Scene scene = new Scene(root);

            Stage stage = new Stage();

            stage.setScene(scene);
            stage.setTitle("Choose journals");
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(GUIController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Starts the data processing on a new Thread.
     * @param event
     */
    @FXML
    private void runButtonAction(ActionEvent event) {

        Task<Void> task = new Task<Void>() {

            @Override
            protected Void call() {
                runWrap();
                return null;
            }
        };
        new Thread(task).start();

    }

    /**
     * Checks the validity of the inputs and runs the algorithm.
     */
    private void runWrap() {
        pubmedFile = new File(pmFileDirField.getText());
        geneFile = new File(gpFileDirField.getText());
        publicationString = journalField.getText();
        taxonomy = taxField.getText();

        if (pubmedFile.exists() == false) {
            cprintln("PubMed file does not exist!");
            pubmedFile = new File("C:\\gpm\\pubmed_result.csv");
        }
        if (geneFile.exists() == false) {
            cprintln("Gene2PubMed file does not exist!");
            geneFile = new File("C:\\gpm\\gene2pubmed");
        }
        if (publicationString.isEmpty()) {
            publicationString = "all";
            cprintln("Publication field is empty, defaulting to '" + publicationString + "'.");

        }
        if (taxonomy.isEmpty()) {
            cprintln("Taxonomy field is empty, defaulting to '10090'.");
            taxonomy = "10090";
        }

        String[] publications = publicationString.split(", ");

        try {
            run(pubmedFile, geneFile, publications, taxonomy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the PubMed file into a list and map.
     * @param pubmedFile File that points to the PubMed file.
     * @param pubs       (Hash)Map that maps journal names to a list of articles denoted by their
     *                   PubMed ID numbers.
     * @param years      Maps individual articles denoted by their PubMed ID numbers uniquely to
     *                   their publication year.
     */
    private void readPubmed(File pubmedFile, HashMap<String, ArrayList<String>> pubs, HashMap<String, String> years) {
        cprintln("Reading pubmed file");
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(pubmedFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String arr[] = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                    if (arr.length == 11) {
                        //Matcher m = pattern.matcher(arr[4]);
                        if (arr[4].contains(".")) {
                            String journalName = arr[4].substring(1, arr[4].indexOf('.'));
                            String year = arr[4].substring(arr[4].lastIndexOf('.') + 3, arr[4].lastIndexOf('.') + 7);
                            String pmID = arr[9].substring(1, arr[9].length() - 1);
                            if (!pubs.containsKey(journalName)) {
                                pubs.put(journalName, new ArrayList<String>());
                            }
                            pubs.get(journalName).add(pmID);
                           years.put(pmID, year);
                        }
                    } else {
//                        System.out.println("Line read error, probably due to quotes within article name.");
                    }
                }
            } catch (FileNotFoundException e) {
                cprintln("Please check if file exists");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("Error while reading pubmed file");
        }
        cprintln("Done reading pubmed file");
    }

    /**
     * Reads the gene2pubmed file into a Map.
     * @param geneFile File that points to gene2pubmed.
     * @param geneMap  Maps PubMed IDs of articles to the list of genes mentioned in the article.
     * @param taxonomy Only articles investigating this taxonomy will be included in the results.
     */
    private void readGene2PubmedFile(File geneFile, HashMap<String, ArrayList<String>> geneMap, String taxonomy) {
        cprintln("Reading gene2pubmed file");
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(geneFile))) {
                String line;
                boolean doneReading = false;
                boolean reachedTargetTax = false;
                while ((line = br.readLine()) != null && !doneReading) {
                    String arr[] = line.split("\t");
                    if (arr[0].equals(taxonomy)) {
                        String gene = arr[1];
                        String pmID = arr[2];
                        if (!geneMap.containsKey(pmID)) {
                            geneMap.put(pmID, new ArrayList<String>());
                        }
                        geneMap.get(pmID).add(gene);
                        reachedTargetTax = true;
                    } else {
                        doneReading = reachedTargetTax;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error while reading gene file");
        }
        cprintln("Done reading gene file");
    }

    /**
     * Runs the algorithm.
     * @param pubmedFile   File that points to the PubMed results CSV.
     * @param geneFile     File that points to gene2pubmed.
     * @param publications Text field input String.
     * @param taxonomy     Text field input taxonomy.
     */
    private void run(File pubmedFile, File geneFile, String[] publications,
            String taxonomy) {

        //Pattern pattern = Pattern.compile("(?<=\\\")[^\\.]*(?=\\.)");
        HashMap<String, ArrayList<String>> pubs = new HashMap<>();
        HashMap<String, String> years = new HashMap<>();
        readPubmed(pubmedFile, pubs, years);

        HashMap<String, ArrayList<String>> geneMap = new HashMap<>();
        readGene2PubmedFile(geneFile, geneMap, taxonomy);

        List<List<String>> allGenes = new ArrayList();
        List<String> allYears = new ArrayList<>();

        if (publications[0].equals("all")) {
            for (String publication : pubs.keySet()) {
                for (String pmID : pubs.get(publication)) {
                    if (geneMap.containsKey(pmID)) {
                        ArrayList<String> genes = geneMap.get(pmID);
                        allGenes.add(genes);
                        String year = years.get(pmID);
                        allYears.add(year);
                        System.out.println("Paper numbered " + pmID + " in publication "
                                + publication + " in the year " + year + " contains the genes:" + genes + " of taxonomy " + taxonomy);
                    }
                }
            }
        } else {
            for (String publication : publications) {
                for (String pmID : pubs.get(publication)) {
                    if (geneMap.containsKey(pmID)) {
                        ArrayList<String> genes = geneMap.get(pmID);
                        allGenes.add(genes);
                        String year = years.get(pmID);
                        allYears.add(year);
                        System.out.println("Paper numbered " + pmID + " in publication "
                                + publication + " in the year " + year + " contains the genes:" + genes + " of taxonomy " + taxonomy);
                    }
                }
            }
        }

        // Calculate gene frequencies
        HashMap<String, Integer> allGenesHM = new HashMap();

        for (List<String> genes : allGenes) {
            for (String gene : genes) {
                if (!allGenesHM.containsKey(gene)) {
                    allGenesHM.put(gene, 0);
                }
                allGenesHM.put(gene, allGenesHM.get(gene) + 1);
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
        String dateID = sdf.format(new Date());
        String trendFileName = (dateID + "_trends.txt");
        String normFileName = (dateID + "_normalization.txt");

        exportTrendsData(allGenes, allYears, allGenesHM, trendFileName);

        // Disabling geneID to Uniprot mapping
        justTrends.setSelected(true);

        if (!justTrends.isSelected()) {

            HashMap<String, List<String>> geneToUniprotMap
                    = getTabbedUniprots(allGenesHM);
            Map<String, String> geneToRecommendedNameMap
                    = getProteinNames2(geneToUniprotMap);

            printGeneFrequencies(allGenesHM, geneToUniprotMap);
            exportGeneFrequencies(allGenesHM, geneToUniprotMap,
                    geneToRecommendedNameMap);

            testMap = geneToUniprotMap;
        }

        Map<String, Map<String, Integer>> trendsRawData = getNumberPerYear(allGenes, allYears);
        Map<String, Map<String, Double>> normalizedTrends = null;

        if (normalizationChoiceBox.getValue().equals("No normalization")) {
            normalizedTrends = convertTrendsToDouble(trendsRawData);
        } //        else if (normalizationChoiceBox.getValue().equals("By publication number")){
        //            //normalizedTrends = convertTrendsToDouble(trendsRawData);
        //        }
        else if (normalizationChoiceBox.getValue().equals("By gene reference")) {
            normalizedTrends = normalizeTrends(trendsRawData);
        }

        Map<String, Map<String, Double>> sortedTrends = null;
        if (rankingChoiceBox.getValue().equals("All time gene popularity")) {
            sortedTrends = sortByPopularity(normalizedTrends, "alltime");
        } else if (rankingChoiceBox.getValue().equals("Last 5 years gene popularity")) {
            sortedTrends = sortByPopularity(normalizedTrends, "since2009");
        } else if (rankingChoiceBox.getValue().equals("Last 10 years gene popularity")) {
            sortedTrends = sortByPopularity(normalizedTrends, "since2004");
        }

        displayTrendsData(sortedTrends);

        Map<String, Integer> yearToTotalNum = generateGeneReferenceNormalizationData(trendsRawData);
        generateNormalizationData_geneReference(yearToTotalNum, normFileName);

        printTotalGeneFrequencies(sortedTrends);


//        Map<String, Map<String, Double>> normalizedTrends = normalizeTrends(trendsRawData);
//        Map<String, Map<String, Double>> sortedAllTime = sortByPopularity(normalizedTrends,"since2009");
//        displayTrendsData(sortedAllTime);
//        displayTrendsData(sortByPopularity(convertTrendsToDouble(trendsRawData),"alltime"));
//        displayTrendsData(sortByPopularity(convertTrendsToDouble(trendsRawData),"since2009"));
    }

    private void printTotalGeneFrequencies(Map<String, Map<String, Double>> trends){
        Map<String,Double> geneToTotalNum = generateGeneToTotalNum(trends);
        Map<String,Double> sortedMaps = sortByValue(geneToTotalNum);
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
        String filename = (sdf.format(new Date()) + "_gene_frequencies.txt");
        cprintln("Exporting to file:" + filename);

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(filename, "UTF-8");
            pw.println("PubMed Gene Search results dated "
                    + new SimpleDateFormat("yy-MM-dd HH:mm").format(new Date()));
            pw.println("PubMed filename: " + pmFileDirField.getText());
            pw.println("Selected publications: " + publicationString);
            pw.println("Taxonomy ID: " + taxonomy);
            pw.println("Including publications from all time" + "\n");
            pw.println("Gene \t Frequency");
            for (Entry<String,Double> geneEntry : sortedMaps.entrySet()) {
                String gene = geneEntry.getKey();
                Double number = geneEntry.getValue();
                String s = gene + "\t" + number.intValue();
                pw.println(s);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GUIController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(GUIController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            pw.close();
        }
    }

    /**
     * Sorts the genes by their popularity, for given metric (i.e. sortType).
     * @param trendsMap Maps a gene to another Map, which maps years to numbers. (Genes are mapped
     *                  to years, which are mapped to number of publications for that gene in that
     *                  year)
     * @param sortType  Indicates sorting metric. Possible values are "alltime", "since2009" and
     *                  "since2004".
     * @return The sorted Map.
     */
    private static Map<String, Map<String, Double>> sortByPopularity(Map<String, Map<String, Double>> trendsMap, String sortType) {

        if (sortType.equals("alltime")) {
            Map<String, Map<String, Double>> sortedOutput = new LinkedHashMap<>();
            Map<String, Double> geneTotalNum = generateGeneToTotalNum(trendsMap);
            Map<String, Double> sortedGeneTotalNum = sortByValue(geneTotalNum);
            for (String gene : sortedGeneTotalNum.keySet()) {
                //System.out.println("gene:" + gene);
                Map<String, Double> yearToNum = trendsMap.get(gene);
                sortedOutput.put(gene, yearToNum);
            }
            return sortedOutput;
        } else if (sortType.equals("since2009")) {
            Map<String, Map<String, Double>> sortedOutput = new LinkedHashMap<>();
            Map<String, Double> geneTotalNum = generateGeneToTotalNum(trendsMap, 2009);
            Map<String, Double> sortedGeneTotalNum = sortByValue(geneTotalNum);
            for (String gene : sortedGeneTotalNum.keySet()) {
                //System.out.println("gene:" + gene);
                Map<String, Double> yearToNum = trendsMap.get(gene);
                sortedOutput.put(gene, yearToNum);
            }
            return sortedOutput;
        } else if (sortType.equals("since2004")) {
            Map<String, Map<String, Double>> sortedOutput = new LinkedHashMap<>();
            Map<String, Double> geneTotalNum = generateGeneToTotalNum(trendsMap, 2004);
            Map<String, Double> sortedGeneTotalNum = sortByValue(geneTotalNum);
            for (String gene : sortedGeneTotalNum.keySet()) {
                //System.out.println("gene:" + gene);
                Map<String, Double> yearToNum = trendsMap.get(gene);
                sortedOutput.put(gene, yearToNum);
            }
            return sortedOutput;
        } else {
            System.out.println("Unknown sorting method");
            return null;
        }
    }

    /**
     * Sorts a given Map by value. Taken verbatim from:
     * http://stackoverflow.com/questions/109383/how-to-sort-a-mapkey-value-on-the-values-in-java
     * Author of the code states 'free to use'
     * @param <K> Key
     * @param <V> Value
     * @param map Input map
     * @return Sorted Map
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list
                = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Displays the HeatMap of the results.
     * @param trendsMap Map containing number of publications mentioning given GeneID at a given
     *                  year.
     */
    private void displayTrendsData(Map<String, Map<String, Double>> trendsMap) {

        int numberOfGenes = trendsMap.keySet().size();

        int MIN_YEAR = 1970;
        int MAX_YEAR = 2015;

        double[][] matrix = new double[numberOfGenes][(MAX_YEAR - MIN_YEAR + 1)];
        String[] years = new String[(MAX_YEAR - MIN_YEAR + 1)];
        String[] genes = new String[numberOfGenes];

        int i = 0;
        for (Entry<String, Map<String, Double>> entry : trendsMap.entrySet()) {
            String gene = entry.getKey();
            Map<String, Double> yearMap = entry.getValue();

            String s = "";
            int j = 0;
            for (int year = MIN_YEAR; year <= MAX_YEAR; year++) {
                if (yearMap.containsKey("" + year)) {
                    s += yearMap.get("" + year) + "\t";
                    matrix[i][j] = (double) yearMap.get("" + year);
                } else {
                    //s += "0\t";
                    matrix[i][j] = 0;
                }

                years[j] = "" + year;
                j++;
            }

            genes[i] = "" + gene;
            i++;
        }
        //System.out.println(Arrays.deepToString(genes));
        HeatChart map = new HeatChart(matrix);
        map.setCellSize(new Dimension(10, 10));
        map.setXValues(years);
        map.setYValues(genes);
        map.setXAxisLabel("Years");
        map.setYAxisLabel("GeneID");
        map.setLowValueColour(java.awt.Color.white);
        map.setHighValueColour(java.awt.Color.black);

        Image im = map.getChartImage();
        final WritableImage iim = SwingFXUtils.toFXImage((BufferedImage) im, null);

        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                ImageView iv = new ImageView();
                iv.setImage(iim);

                Stage stage = new Stage();

                BorderPane bp = new BorderPane();
                ScrollPane sp = new ScrollPane();
                sp.setStyle("-fx-background-color:white;");

                sp.setContent(iv);
                sp.setFitToWidth(true);
                bp.setCenter(sp);

                Scene scene = new Scene(bp);
                scene.setFill(Color.BLACK);
                stage.setTitle("Gene Popularity Heatmap");
                stage.setScene(scene);

                sp.setPrefSize(iim.getWidth() + 20, 800);
                sp.setPannable(true);
                stage.sizeToScene();
                stage.show();
            }
        });

    }

    /**
     * Writes normalization data by number of gene references for each year for given query to file.
     * @param years    Maps years to total number of publications for given query.
     * @param filename Output file name.
     */
    private void generateNormalizationData_geneReference(Map<String, Integer> years, String filename) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(filename, "UTF-8");
            pw.println("year \tn");
            for (String year : years.keySet()) {
                pw.println(year + "\t" + years.get(year));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GUIController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(GUIController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            pw.close();
        }

    }

    /**
     * Writes normalization data by total number of publications for each year for given query to
     * file (publications may be unrelated to genomics/proteomics).
     * @deprecated
     * @param years    Maps years to total number of publications for given query.
     * @param filename Output file name.
     */
    private void generateNormalizationData_pubmedOutput(HashMap<String, String> years, String filename) {
        // invert map
        Map<String, List<String>> yearToPubmed = new HashMap<>();
        for (Entry<String, String> entry : years.entrySet()) {
            String pmid = entry.getKey();
            String year = entry.getValue();
            if (!yearToPubmed.containsKey(year)) {
                yearToPubmed.put(year, new ArrayList<String>());
            }
            yearToPubmed.get(year).add(pmid);
        }

        for (String year : yearToPubmed.keySet()) {
            System.out.println(year + " " + yearToPubmed.get(year).size());
        }

//        String filename = "pubsPerYearForQuery.txt";
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(filename, "UTF-8");
            pw.println("year \tn");
            for (String year : yearToPubmed.keySet()) {
                pw.println(year + "\t" + yearToPubmed.get(year).size());
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GUIController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(GUIController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            pw.close();
        }

        cprintln("Normalization data generated: " + filename);
    }

    /**
     * Generates a map that maps years to total number of publications for given query.
     * @param trendsRawData Map containing number of publications mentioning given GeneID at a given
     *                      year.
     * @return Maps years to total number of publications for given query.
     */
    private Map<String, Integer> generateGeneReferenceNormalizationData(Map<String, Map<String, Integer>> trendsRawData) {
        Map<String, Integer> yearsTotalNum = new HashMap<>();

        for (Map<String, Integer> stringIntegerMap : trendsRawData.values()) {
            for (Entry<String, Integer> en : stringIntegerMap.entrySet()) {
                String key = en.getKey();
                Integer current = yearsTotalNum.get(key);
                if (current == null) {
                    current = 0;
                    yearsTotalNum.put(key, 0);
                }
                yearsTotalNum.put(key, current + en.getValue());
            }
        }
        return yearsTotalNum;
    }

    /**
     * Generates a Map that maps geneID to total number of publications, all time.
     * @param trendsRawData Map containing number of publications mentioning given GeneID at a given
     *                      year.
     * @return Maps geneID to total number of publications, all time.
     */
    private static Map<String, Double> generateGeneToTotalNum(Map<String, Map<String, Double>> trendsRawData) {
        Map<String, Double> genesTotalNum = new HashMap<>();

        for (Entry<String, Map<String, Double>> entrySet : trendsRawData.entrySet()) {
            String gene = entrySet.getKey();
            Map<String, Double> stringIntegerMap = entrySet.getValue();
            for (Entry<String, Double> en : stringIntegerMap.entrySet()) {
                Double current = genesTotalNum.get(gene);
                if (current == null) {
                    current = 0d;
                    genesTotalNum.put(gene, 0d);
                }
                genesTotalNum.put(gene, current + en.getValue());
            }
        }
        return genesTotalNum;
    }

    /**
     * Generates a Map that maps geneID to total number of publications, since lastYear.
     * @param trendsRawData Map containing number of publications mentioning given GeneID at a given
     *                      year.
     * @param lastYear      Last year to include (i.e. lastYear of 2004 only counts publications
     *                      published in 2004 and later)
     * @return Maps geneID to total number of publications, since lastYear.
     */
    private static Map<String, Double> generateGeneToTotalNum(Map<String, Map<String, Double>> trendsRawData, int lastYear) {
        Map<String, Double> genesTotalNum = new HashMap<>();

        for (Entry<String, Map<String, Double>> entrySet : trendsRawData.entrySet()) {
            String gene = entrySet.getKey();
            Map<String, Double> stringIntegerMap = entrySet.getValue();
            for (Entry<String, Double> en : stringIntegerMap.entrySet()) {
                String yearStr = en.getKey();
                double yearDouble = Double.parseDouble(yearStr);
                Double current = genesTotalNum.get(gene);
                if (current == null) {
                    current = 0d;
                }
                if (yearDouble < lastYear) {
                    genesTotalNum.put(gene, current + 0);
                }
                else{
                    genesTotalNum.put(gene, current + en.getValue());
                }
            }
        }
        return genesTotalNum;
    }

    /**
     * Writes the gene trends data to file.
     * @param allGenes   List of Lists that contains GeneID and years.
     * @param allYears   List of all years in the query.
     * @param allGenesHM Not utilized, leave null.
     * @param filename   File name for the output file.
     */
    private void exportTrendsData(List<List<String>> allGenes,
            List<String> allYears, Map<String, Integer> allGenesHM, String filename) {

        Map<String, Map<String, Integer>> trendsRawData
                = getNumberPerYear(allGenes, allYears);

        cprintln("Exporting trends data to file: " + filename);

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(filename, "UTF-8");
            pw.println("Gene \t Year \t Number of Publications");
            for (Entry<String, Map<String, Integer>> entry : trendsRawData.entrySet()) {
                String gene = entry.getKey();
                Map<String, Integer> yearMap = entry.getValue();
                for (Entry<String, Integer> yearEntry : yearMap.entrySet()) {
                    String year = yearEntry.getKey();
                    Integer number = yearEntry.getValue();
                    pw.println(gene + "\t" + year + "\t" + number);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GUIController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(GUIController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            pw.close();
        }

    }

    /**
     * Merges allGenes and allYears into a single Map.
     * @param allGenes
     * @param allYears
     * @return A single Map that Maps GeneID to Year to number of publications.
     */
    private Map<String, Map<String, Integer>> getNumberPerYear(List<List<String>> allGenes, List<String> allYears) {

        // gene - year - count in publications
        Map<String, Map<String, Integer>> numAllYears = new HashMap<>();

        if (allGenes.size() != allYears.size()) {
            System.out.println("Array sizes don't match!");
        }

        for (int i = 0; i < allGenes.size(); i++) {
            List<String> genes = allGenes.get(i);
            String year = allYears.get(i);
            for (String gene : genes) {
                if (!numAllYears.containsKey(gene)) {
                    numAllYears.put(gene, new HashMap<String, Integer>());
                }
                if (!numAllYears.get(gene).containsKey(year)) {
                    numAllYears.get(gene).put(year, 0);
                }
                int n = numAllYears.get(gene).get(year);
                numAllYears.get(gene).put(year, n + 1);
            }

        }

        return numAllYears;
    }

    /**
     * Not being used until UniprotID retrieval is reinstated.
     * @deprecated
     * @param allGenes
     * @return
     */
    private HashMap<String, List<String>> getTabbedUniprots(Map<String, Integer> allGenes) {
        cprintln("Contacting Uniprot database...");

        final int QUERY_SIZE = 500;
        HashMap<String, List<String>> entrezToUniprot = new HashMap<>();
        String[] genes = new String[allGenes.keySet().size()];

        int i = 0;
        for (String gene : allGenes.keySet()) {
            genes[i] = gene;
            i++;
        }

        List<String[]> splitUp = new ArrayList<>();
        try {
            double numberSplit = Math.ceil(((double) genes.length) / ((double) QUERY_SIZE));

            for (int j = 0; j < numberSplit; ++j) {
                int start = j * QUERY_SIZE;
                int length = (genes.length - start) > QUERY_SIZE
                        ? QUERY_SIZE : (genes.length - start);

                String[] temp = new String[length];
                System.arraycopy(genes, start, temp, 0, length);
                splitUp.add(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int j = 1;
        final int MAX_NUMBER_OF_TRIALS_TO_GET_UNIPROT = 5;
        for (String[] genesChunk : splitUp) {
            cprintln("Getting UniprotID batch " + j + " of " + splitUp.size());
            j++;
            String[] twoCol = new String[0];
            boolean gotTheData = false;
            int failTime = 0;
            while (!gotTheData && failTime < MAX_NUMBER_OF_TRIALS_TO_GET_UNIPROT) {
                try {
                    twoCol = geneMapper(genesChunk);
                    gotTheData = true;
                } catch (ConnectException ex) {
                    cprintln("Failed to connect, trying again.");
                    failTime++;
                    if (failTime > MAX_NUMBER_OF_TRIALS_TO_GET_UNIPROT - 1) {
                        cprintln("Maximum number of tries to access Uniprot DB failed. Skipping batch.");
                    }
                }
            }
            for (String s : twoCol) {
                String[] ss = s.split("\t");
                if (ss.length == 2 && !ss[0].equalsIgnoreCase("From")) {
                    if (!entrezToUniprot.containsKey(ss[0])) {
                        entrezToUniprot.put(ss[0], new ArrayList<String>());
                    }
                    entrezToUniprot.get(ss[0]).add(ss[1]);
                }
            }
        }

        for (String gene : genes) {
            if (!entrezToUniprot.containsKey(gene)) {
                entrezToUniprot.put(gene, new ArrayList<String>());
                entrezToUniprot.get(gene).add("No uniprot found");
            }
        }

        return entrezToUniprot;
    }

    /**
     * Writes gene frequencies into a file, not being used until UniprotID retrieval is reinstated.
     * @deprecated
     * @param allGenes
     * @param uniprots
     * @param geneToName
     */
    private void exportGeneFrequencies(Map<String, Integer> allGenes, HashMap<String, List<String>> uniprots, Map<String, String> geneToName) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
        String filename = (sdf.format(new Date()) + "_gene_frequencies.txt");
        cprintln("Exporting to file:" + filename);

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(filename, "UTF-8");
            pw.println("PubMed Gene Search results dated "
                    + new SimpleDateFormat("yy-MM-dd HH:mm").format(new Date()));
            pw.println("PubMed filename: " + pmFileDirField.getText());
            pw.println("Selected publications: " + publicationString);
            pw.println("Taxonomy ID: " + taxonomy + "\n");
            pw.println("Gene \t Frequency \t UniprotName \t Protein Name");
            for (String gene : allGenes.keySet()) {
                String name = geneToName.get(gene);
                name = name != null ? name : "Protein name unavailable";
                String s = gene + "\t" + allGenes.get(gene) + "\t" + uniprots.get(gene).toString() + "\t" + name;
                pw.println(s);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GUIController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(GUIController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            pw.close();
        }
    }

    /**
     * Prints the gene frequencies into the console.
     * @param allGenes
     * @param uniprots
     */
    private void printGeneFrequencies(Map<String, Integer> allGenes, HashMap<String, List<String>> uniprots) {
        cprintln("\n%%%%% RESULTS %%%%%\n");
        cprintln("Selected publications: " + publicationString + "\n");

        cprintln("Gene \t Frequency \t UniprotName \n");

        for (String gene : allGenes.keySet()) {
            String s = gene + "\t" + allGenes.get(gene) + "\t" + uniprots.get(gene).toString();
            cprintln(s);
        }
    }

    /**
     * Prints input to the console on the Stage.
     * @param s String to be printed.
     */
    private void cprintln(String s) {
        final String ss = s;

        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                consoleField.appendText(ss + "\n");
            }
        });
    }

    /**
     * For debugging purposes only.
     * @deprecated
     */
    public void test() {
        cprintln("Normalization data generation initiated");
        HashMap<String, ArrayList<String>> pubst = new HashMap<>();
        HashMap<String, String> yearst = new HashMap<>();

        File pmf = new File(pmFileDirField.getText());
        readPubmed(pmf, pubst, yearst);
        // invert map
        Map<String, List<String>> yearToPubmed = new HashMap<>();
        for (Entry<String, String> entry : yearst.entrySet()) {
            String pmid = entry.getKey();
            String year = entry.getValue();
            if (!yearToPubmed.containsKey(year)) {
                yearToPubmed.put(year, new ArrayList<String>());
            }
            yearToPubmed.get(year).add(pmid);
        }

        for (String year : yearToPubmed.keySet()) {
            System.out.println(year + " " + yearToPubmed.get(year).size());
        }

        String filename = "pubsPerYearForQuery.txt";
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(filename, "UTF-8");
            pw.println("year \tn");
            for (String year : yearToPubmed.keySet()) {
                pw.println(year + "\t" + yearToPubmed.get(year).size());
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GUIController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(GUIController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            pw.close();
        }

        cprintln("Process completed. See file: " + filename);
    }

    /**
     * This code is partly based on http://www.uniprot.org/faq/28
     * @deprecated
     * @param entrez
     * @return
     */
    public String[] geneMapper(String[] entrez) throws ConnectException {
        String[] uniprots = null;

        String urlString = "http://www.uniprot.org/mapping/?from=P_ENTREZGENEID&to=ACC&format=tab&query=";
        for (int i = 0; i < entrez.length; i++) {
            urlString += entrez[i] + "+";
        }
        urlString = urlString.substring(0, urlString.length() - 1);

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            HttpURLConnection.setFollowRedirects(true);
            conn.setDoInput(true);
            conn.connect();

            int status = conn.getResponseCode();
            while (true) {
                int wait = 0;
                String header = conn.getHeaderField("Retry-After");
                if (header != null) {
                    wait = Integer.valueOf(header);
                }
                if (wait == 0) {
                    break;
                }
                System.out.println("Waiting (" + wait + ")...");
                conn.disconnect();
                try {
                    Thread.sleep(wait * 1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(GUIController.class.getName()).log(Level.SEVERE, null, ex);
                }
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.connect();
                status = conn.getResponseCode();
            }
            if (status == HttpURLConnection.HTTP_OK) {
                cprintln("Uniprot database accepted query");
                InputStream reader = conn.getInputStream();
                URLConnection.guessContentTypeFromStream(reader);
                StringBuilder builder = new StringBuilder();
                int a = 0;
                while ((a = reader.read()) != -1) {
                    builder.append((char) a);
                }
                String outString = (builder.toString());
                uniprots = outString.split("\n");
            } else {
                cprintln("Uniprot database rejected query: " + conn.getResponseMessage() + " for "
                        + urlString);
                throw new ConnectException("uniprotRejected");
            }
            conn.disconnect();
        } catch (MalformedURLException e) {

        } catch (IOException e) {

        }

        return uniprots;
    }

    /**
     * Not used until UniprotID retrieval is reinstated.
     * @deprecated
     * @param allGenes
     * @return
     */
    private Map<String, String> getProteinNames2(Map<String, List<String>> allGenes) {
        Map<String, String> geneIDToProteinName = new HashMap();
        final int GENE_QUERY_SIZE = 100;

        Map<String, List<String>> filtered = new HashMap<>(allGenes);
        for (Entry<String, List<String>> entry : allGenes.entrySet()) {
            String key = entry.getKey();
            List<String> list = entry.getValue();
            if (list.get(0).equals("No uniprot found")) {
                filtered.remove(key);
            }
        }

        List<List<String>> splitUpUniprots = new ArrayList<>();
        splitUpUniprots.add(new ArrayList<String>());
        int i = 0;
        int j = 0;
        for (String key : filtered.keySet()) {
            List<String> uniprots = filtered.get(key);
            splitUpUniprots.get(i).addAll(uniprots);
            j++;
            if (j > GENE_QUERY_SIZE) {
                j = 0;
                i++;
                splitUpUniprots.add(new ArrayList<String>());
            }
        }

        List<String> allProteinNames = new ArrayList<>();
        int batch = 1;
        final int MAX_NUMBER_OF_TRIALS_TO_GET_PROTEIN_NAMES = 5;
        for (List<String> genesChunk : splitUpUniprots) {
            cprintln("Getting Protein name for batch " + batch + " of " + splitUpUniprots.size());
            j++;
            String[] proteinNames = new String[0];
            boolean gotTheData = false;
            int failTime = 0;
            while (!gotTheData && failTime < MAX_NUMBER_OF_TRIALS_TO_GET_PROTEIN_NAMES) {
                try {
                    String[] chunk = new String[0];
                    chunk = genesChunk.toArray(chunk);
                    proteinNames = rGeneNames(chunk);
                    gotTheData = true;
                    batch++;
                } catch (ConnectException ex) {
                    cprintln("Failed to connect, trying again.");
                    failTime++;
                    if (failTime > MAX_NUMBER_OF_TRIALS_TO_GET_PROTEIN_NAMES - 1) {
                        cprintln("Maximum number of tries to access Uniprot DB failed. Skipping batch.");
                    }
                }
            }
            List<String> pn = Arrays.asList(proteinNames);
            allProteinNames.addAll(pn);

        }

        HashMap<String, List<String>> multipleNamesForGenes = new HashMap<>();
        int index = 0;
        for (Entry<String, List<String>> entry : filtered.entrySet()) {
            String geneID = entry.getKey();
            List<String> uniprotList = entry.getValue();
            for (int k = 0; k < uniprotList.size(); k++) {
                String string = uniprotList.get(k);
                if (!multipleNamesForGenes.containsKey(geneID)) {
                    multipleNamesForGenes.put(geneID, new ArrayList<String>());
                }
                multipleNamesForGenes.get(geneID).add(allProteinNames.get(index));
                index++;
            }
        }

        for (Entry<String, List<String>> entry : multipleNamesForGenes.entrySet()) {
            String gene = entry.getKey();

            List<String> list = entry.getValue();
            if (list.isEmpty()) {
                list.add("List is empty? ERROR");
            }
            String recommendedName = null;
            String tempName = null;

            for (String name : list) {
                tempName = name;
                if (name == null) //TODO
                {
                    System.out.println("name is null");
                    continue;
                }
                if (name.charAt(name.length() - 1) == '*') {
                    recommendedName = name;
                }
            }

            if (recommendedName == null) {
                recommendedName = tempName;
                if (recommendedName == null) {
                    recommendedName = "temp name was null";
                }
            }

            geneIDToProteinName.put(gene, recommendedName);
        }

        return geneIDToProteinName;

    }

    /**
     * Not used until UniprotID retrieval is reinstated.
     * @param allGenes
     * @return
     */
    private Map<String, String> getProteinNames(Map<String, List<String>> allGenes) {
        cprintln("Contacting Uniprot database for protein names...");

        HashMap<String, String> geneIDToProteinName;
        final int QUERY_SIZE = 500;

        int totalSize = 0;
        for (Entry<String, List<String>> entry : allGenes.entrySet()) {
            totalSize += entry.getValue().size();
        }

        String[] genesT = new String[totalSize];
        String[] keysT = new String[totalSize];

        int i = 0;
        for (Entry<String, List<String>> geneEntry : allGenes.entrySet()) {
            List<String> singleProteinUniprots = geneEntry.getValue();
            for (String gene : singleProteinUniprots) {
                if (!gene.equalsIgnoreCase("No uniprot found")) {
                    genesT[i] = gene;
                    keysT[i] = geneEntry.getKey();
                } else {
                    genesT[i] = "INVALID";
                    keysT[i] = "INVALID";
                }

                i++;
            }
        }

        // remove all invalids
        List<String> genesList = new ArrayList<>();
        List<String> keysList = new ArrayList<>();

        for (int j = 0; j < keysT.length; j++) {
            String k = keysT[j];
            String g = genesT[j];
            if (!k.equals("INVALID")) {
                genesList.add(g);
                keysList.add(k);
            }
        }

        String[] keys = new String[keysList.size()];
        keys = keysList.toArray(keys);
        String[] genes = new String[genesList.size()];
        genes = genesList.toArray(genes);

        List<String[]> splitUp = new ArrayList<>();
        try {
            double numberSplit = Math.ceil(((double) genes.length) / ((double) QUERY_SIZE));

            for (int j = 0; j < numberSplit; ++j) {
                int start = j * QUERY_SIZE;
                int length = (genes.length - start) > QUERY_SIZE
                        ? QUERY_SIZE : (genes.length - start);

                String[] temp = new String[length];
                System.arraycopy(genes, start, temp, 0, length);
                splitUp.add(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> allProteinNames = new ArrayList<>();

        int j = 1;
        final int MAX_NUMBER_OF_TRIALS_TO_GET_PROTEIN_NAMES = 5;
        for (String[] genesChunk : splitUp) {
            cprintln("Getting Protein name for batch " + j + " of " + splitUp.size());
            j++;
            String[] proteinNames = new String[0];
            boolean gotTheData = false;
            int failTime = 0;
            while (!gotTheData && failTime < MAX_NUMBER_OF_TRIALS_TO_GET_PROTEIN_NAMES) {
                try {
                    proteinNames = rGeneNames(genesChunk);
                    gotTheData = true;
                } catch (ConnectException ex) {
                    cprintln("Failed to connect, trying again.");
                    failTime++;
                    if (failTime > MAX_NUMBER_OF_TRIALS_TO_GET_PROTEIN_NAMES - 1) {
                        cprintln("Maximum number of tries to access Uniprot DB failed. Skipping batch.");
                    }
                }
            }
            allProteinNames.addAll(Arrays.asList(proteinNames));

        }

        HashMap<String, List<String>> multipleNamesForGenes = new HashMap<>();
        int m = 0;
        for (int k = 0; k < keys.length; k++) {
            String key = keys[k];
            String gene = genes[k];
            String name = allProteinNames.get(k);
            if (name == null) {
                System.out.println("name is null? k=" + k);
            }

            if (!multipleNamesForGenes.containsKey(key)) {
                multipleNamesForGenes.put(key, new ArrayList<String>());
            }
            multipleNamesForGenes.get(key).add(name);
        }

        geneIDToProteinName = new HashMap<>();

        for (Entry<String, List<String>> entry : multipleNamesForGenes.entrySet()) {
            String gene = entry.getKey();
            if (gene == null) {
                continue;
            }
            List<String> list = entry.getValue();
            if (list.isEmpty()) {
                list.add("List is empty? ERROR");
            }
            String recommendedName = null;
            String tempName = null;

            for (String name : list) {
                tempName = name;
                if (name == null) //TODO
                {
                    System.out.println("name is null");
                    continue;
                }
                if (name.charAt(name.length() - 1) == '*') {
                    recommendedName = name;
                }
            }

            if (recommendedName == null) {
                recommendedName = tempName;
                if (recommendedName == null) {
                    recommendedName = "temp name was null";
                }
            }

            geneIDToProteinName.put(gene, recommendedName);
        }

        System.out.println("g" + geneIDToProteinName.toString());

        for (Entry<String, String> entry : geneIDToProteinName.entrySet()) {
            String key = entry.getKey();
            String name = entry.getValue();
            if (name == null) {
                name = "No protein name";
            }
        }

        return geneIDToProteinName;
    }

    /**
     * Not used until UniprotID retrieval is reinstated.
     * @param uniprots
     * @return
     * @throws ConnectException
     */
    public String[] rGeneNames(String[] uniprots) throws ConnectException {
        String[] geneNames = new String[uniprots.length];
        String[] entries = null;
        String urlString = "http://www.uniprot.org/batch/?format=fasta&query=";

        for (String uniprot : uniprots) {
            urlString += uniprot + "+";
        }
        urlString = urlString.substring(0, urlString.length() - 1);

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            HttpURLConnection.setFollowRedirects(true);
            conn.setDoInput(true);
            conn.connect();

            int status = conn.getResponseCode();
            while (true) {
                int wait = 0;
                String header = conn.getHeaderField("Retry-After");
                if (header != null) {
                    wait = Integer.valueOf(header);
                }
                if (wait == 0) {
                    break;
                }
                System.out.println("Waiting (" + wait + ")...");
                conn.disconnect();
                try {
                    Thread.sleep(wait * 1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(GUIController.class.getName()).log(Level.SEVERE, null, ex);
                }
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.connect();
                status = conn.getResponseCode();
            }
            if (status == HttpURLConnection.HTTP_OK) {
                cprintln("Uniprot database accepted query");
                InputStream reader = conn.getInputStream();
                URLConnection.guessContentTypeFromStream(reader);
                StringBuilder builder = new StringBuilder();
                int a = 0;
                while ((a = reader.read()) != -1) {
                    builder.append((char) a);
                }
                String outString = (builder.toString());
                entries = outString.split(">");
            } else {
                cprintln("Uniprot database rejected query: " + conn.getResponseMessage() + " for "
                        + urlString);
                throw new ConnectException("uniprotRejected");
            }
            conn.disconnect();
        } catch (MalformedURLException e) {

        } catch (IOException e) {

        }

        if (entries == null) {
            System.out.println("Entires null!");
        }

        // Disregard first index which is an empty String
        for (int i = 1; i < entries.length; i++) {
            String entry = entries[i];
            try {
                Pattern p = Pattern.compile("(\\s).*(?=OS=)");
                Matcher m = p.matcher(entry);
                if (m.find()) {
                    geneNames[i - 1] = m.group(0);
                    if (entry.substring(0, 2).equals("sp")) {
                        geneNames[i - 1] += "*";
                    }
                }

            } catch (Exception e) {
                System.out.println("Matching problem");
            }
        }

        return geneNames;
    }

    /**
     * Not used until UniprotID retrieval is reinstated. This code is partly based on
     * http://www.uniprot.org/faq/28
     * @deprecated
     * @param uniprots
     * @return
     */
    public List<String> retrieveGeneNames(HashMap<String, List<String>> uniprots) throws ConnectException {

        List<String> geneNames = new ArrayList<>();
        String[] entries = new String[0];

        for (String geneKey : uniprots.keySet()) {
            String urlString = "http://www.uniprot.org/batch/?format=fasta&query=";
            List<String> uniprotsForOneGene = uniprots.get(geneKey);
            for (String oneUniprot : uniprotsForOneGene) {
                urlString += oneUniprot + "+";
            }
            urlString = urlString.substring(0, urlString.length() - 1);

            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                HttpURLConnection.setFollowRedirects(true);
                conn.setDoInput(true);
                conn.connect();

                int status = conn.getResponseCode();
                while (true) {
                    int wait = 0;
                    String header = conn.getHeaderField("Retry-After");
                    if (header != null) {
                        wait = Integer.valueOf(header);
                    }
                    if (wait == 0) {
                        break;
                    }
                    System.out.println("Waiting (" + wait + ")...");
                    conn.disconnect();
                    try {
                        Thread.sleep(wait * 1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GUIController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    status = conn.getResponseCode();
                }
                if (status == HttpURLConnection.HTTP_OK) {
                    cprintln("Uniprot database accepted query");
                    InputStream reader = conn.getInputStream();
                    URLConnection.guessContentTypeFromStream(reader);
                    StringBuilder builder = new StringBuilder();
                    int a = 0;
                    while ((a = reader.read()) != -1) {
                        builder.append((char) a);
                    }
                    String outString = (builder.toString());
                    entries = outString.split(">");
                } else {
                    cprintln("Uniprot database rejected query: " + conn.getResponseMessage() + " for "
                            + urlString);
                    throw new ConnectException("uniprotRejected");
                }
                conn.disconnect();
            } catch (MalformedURLException e) {

            } catch (IOException e) {

            }

            String proteinName = null;
            String tempProteinName = null;
            boolean foundSP = false;
            for (String entry : entries) {
                String s = entry;
                try {
                    Pattern p = Pattern.compile("(\\s).*(?=OS=)");
                    Matcher m = p.matcher(s);
                    if (m.find()) {
                        tempProteinName = m.group(0);
                        if (s.substring(0, 2).equals("sp")) {
                            System.out.println("star*");
                            proteinName = tempProteinName;
                            foundSP = true;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error connecting to Uniprot");
                }
            }
            if (!foundSP) {
                proteinName = tempProteinName;
            }
            geneNames.add(proteinName);

            System.out.println("Recom name" + geneKey + ":" + proteinName);

        }

        return geneNames;
    }

    /**
     * @deprecated @param event
     */
    @FXML
    private void testButtonAction(ActionEvent event) {
        Task<Void> task = new Task<Void>() {

            @Override
            protected Void call() {
                test();
                return null;
            }
        };
        new Thread(task).start();
    }

    /**
     * @deprecated @param event
     */
    @FXML
    private void justTrendsButtonAction(ActionEvent event) {
//        Task<Void> task = new Task<Void>() {
//
//            @Override
//            protected Void call() {
//                runJustTrends();
//                return null;
//            }
//        };
//        new Thread(task).start();
    }

    /**
     * Normalizes the number of publications each year by the total number of gene references for
     * that year for given query.
     * @param trendsRawData Maps a gene to another Map, which maps years to numbers. (Genes are
     *                      mapped to years, which are mapped to number of publications for that
     *                      gene in that year)
     * @return Normalized trends data.
     */
    private Map<String, Map<String, Double>> normalizeTrends(Map<String, Map<String, Integer>> trendsRawData) {
        Map<String, Integer> yearToTotalNum = generateGeneReferenceNormalizationData(trendsRawData);
        Map<String, Map<String, Double>> normalizedTrends = new HashMap<>();

        for (Entry<String, Map<String, Integer>> entrySet : trendsRawData.entrySet()) {
            String gene = entrySet.getKey();
            Map<String, Integer> yearToNum = entrySet.getValue();
            Map<String, Double> yearToNumNormalized = new HashMap<>();
            for (Entry<String, Integer> e : yearToNum.entrySet()) {
                String year = e.getKey();
                Integer num = e.getValue();
                Integer norm = yearToTotalNum.get(year);
                yearToNumNormalized.put(year, num.doubleValue() / norm.doubleValue());
            }
            normalizedTrends.put(gene, yearToNumNormalized);
        }
        return normalizedTrends;
    }

    /**
     * Converts the Map into compatible format for later stages. No normalization is done.
     * @param trendsRawData Maps a gene to another Map, which maps years to numbers. (Genes are
     *                      mapped to years, which are mapped to number of publications for that
     *                      gene in that year)
     * @return Non-normalized trends data.
     */
    private Map<String, Map<String, Double>> convertTrendsToDouble(Map<String, Map<String, Integer>> trendsRawData) {
        Map<String, Map<String, Double>> normalizedTrends = new HashMap<>();

        for (Entry<String, Map<String, Integer>> entrySet : trendsRawData.entrySet()) {
            String gene = entrySet.getKey();
            Map<String, Integer> yearToNum = entrySet.getValue();
            Map<String, Double> yearToNumNormalized = new HashMap<>();
            for (Entry<String, Integer> e : yearToNum.entrySet()) {
                String year = e.getKey();
                Integer num = e.getValue();
                Integer norm = 1;
                yearToNumNormalized.put(year, num.doubleValue() / norm.doubleValue());
            }
            normalizedTrends.put(gene, yearToNumNormalized);
        }
        return normalizedTrends;

    }
}
