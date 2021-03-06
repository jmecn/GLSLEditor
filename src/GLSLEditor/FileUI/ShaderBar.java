package GLSLEditor.FileUI;

import GLSLEditor.Document;
import GLSLEditor.Editor;
import GLSLEditor.Project;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import java.lang.Boolean;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;



//A Class that manages the Bar at the bottom of the window. Class provides an easy interface for adding files to active project, and to select already existing files.
//The bar has the name of the project, and a label for every supported shader. When a label(non-name) is clicked, Two things can happen: First, if the shader exists
//in the currently active project, the shader gets selected(opened, if necessary). If the shader does not exist, a diologue opens with options to use add an existing file,
//create new file, or use currently selected file.
public class ShaderBar {
    private Editor editor;
    private HBox bar;
    private Map<String, Label> labels;

    //Constructs the ShaderBar, should be called form Editor. Bar is the JavaFX element in witch the Shaderbar is created
    public ShaderBar(Editor editor, HBox bar){
        this.editor = editor;
        this.bar = bar;
        labels = new HashMap<>();

        //Create labels and add the, to the bar
        labels.put("name", new Label());
        labels.put("vs", new Label("VS"));
        labels.put("tc", new Label("TC"));
        labels.put("ts", new Label("TS"));
        labels.put("gs", new Label("GS"));
        labels.put("fs", new Label("FS"));
        bar.getChildren().add(labels.get("name"));
        bar.getChildren().add(labels.get("vs"));
        bar.getChildren().add(labels.get("tc"));
        bar.getChildren().add(labels.get("ts"));
        bar.getChildren().add(labels.get("gs"));
        bar.getChildren().add(labels.get("fs"));

        //Loops through the labels
        for(String s : labels.keySet()){

            //Initial style
            labels.get(s).setId("ProjectTab_noproject");

            labels.get(s).setMaxWidth(1000);
            bar.setHgrow(labels.get(s), Priority.ALWAYS);

            //Don't do the rest of this stuff for name label
            if (s.equals("name")) continue;

            labels.get(s).setOnMouseEntered(e -> {
                if (editor.getProject() != null) labels.get(s).setId("ProjectTab_hover");
            });

            labels.get(s).setOnMouseExited(e -> {
                if (editor.getProject() == null) labels.get(s).setId("ProjectTab_noproject");
                else if (!editor.getProject().hasDocument(s)) labels.get(s).setId("ProjectTab_no");
                else labels.get(s).setId("ProjectTab_yes");
            });

            //When the label is clicked
            labels.get(s).setOnMouseClicked(e -> {
                if (editor.getProject() == null) return;


                if (editor.getProject().hasDocument(s)) {
                    //If the tab exists
                    if (editor.getFileBar().hasTab(editor.getProject().getDocument(s))) {
                        editor.selectTab(editor.getFileBar().getTab(editor.getProject().getDocument(s)));

                        //Otherwise open it
                    } else {
                        editor.getFileBar().addTab(new FileTab(editor.getProject().getDocument(s), editor));
                        editor.selectTab(editor.getFileBar().getTab(editor.getProject().getDocument(s)));


                    }

                    //Don't create a alert etc.
                    return;
                }

                //Create a alert box
                Alert a = new Alert(Alert.AlertType.CONFIRMATION);

                //Set the alertBox's texts
                a.setTitle("Add Shader");
                a.setHeaderText("Do you want to create new file, add currently active file, open file or remove this stage?");

                //Add the buttons
                ButtonType newFile = new ButtonType("New File");
                ButtonType openFile = new ButtonType("Open File");
                ButtonType thisFile = new ButtonType("This File");
                ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                a.getButtonTypes().setAll(newFile, openFile, thisFile, cancel);

                //Get the result
                Optional<ButtonType> result = a.showAndWait();

                //If the user wants to create a new file
                if (result.get() == newFile) {

                    //Show the file chooser and create the file
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Create new file");
                    fileChooser.setInitialDirectory(new File(editor.getProject().getWorkFolder()));
                    fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("GLSL shader", "*." + s));

                    File file = fileChooser.showSaveDialog(null);


                    if (file == null) return;
                    if (file.exists()) file.delete();
                    try {

                        file.createNewFile();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }


                    Document doc = new Document(file.getAbsolutePath().replace("\\", "/"));

                    //Add the new file to the filebar and select it
                    FileTab tab = new FileTab(doc, editor);
                    editor.getFileBar().addTab(tab);
                    editor.selectTab(tab);

                    editor.getProject().setDocument(s, doc);

                    //Set the style to indicate that the stage exists
                    labels.get(s).setId("ProjectTab_yes");
                }

                //If the user wants to set this file to the stage
                else if (result.get() == thisFile) {
                    //Get the selected document
                    Document doc = editor.getActiveDocument();
                    if (doc == null || !doc.isFile()) return;

                    //If the document is the wrong file type, just return
                    String docPath = doc.getAsFile().getAbsolutePath().replace("\\", "/");
                    if (!docPath.substring(docPath.lastIndexOf('.')).equals("." + s)) return;


                    editor.getProject().setDocument(s, doc);

                    //Set the style to indicate that the stage exists
                    labels.get(s).setId("ProjectTab_yes");
                }

                //If the user wants to open an existing file
                else if (result.get() == openFile){
                    //Show the file chooser to open a file
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Open file");
                    fileChooser.setInitialDirectory(new File(editor.getProject().getWorkFolder()));
                    fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("GLSL shader", "*." + s));

                    File file = fileChooser.showOpenDialog(null);


                    if (file == null) return;

                    Document doc = new Document(file.getAbsolutePath().replace("\\", "/"));

                    //Add the document to the filetab and select it
                    FileTab tab = new FileTab(doc, editor);
                    editor.getFileBar().addTab(tab);
                    editor.selectTab(tab);

                    editor.getProject().setDocument(s, doc);

                    //Set the style to indicate that the stage exists
                    labels.get(s).setId("ProjectTab_yes");



                }


            });



        };








    }


    //Updates the ShaderBar(style, mostly), should be called whenever the project is changed or closed
    public void updateProject(){
        Project p = editor.getProject();

        //If the project was closed
        if(p == null){
            labels.get("name").setText("");
            for(String s : labels.keySet()){
                labels.get(s).setId("ProjectTab_noproject");
            };
            return;
        }


        //Set the project's name
        labels.get("name").setText(p.getName());

        //Set the styles to indicate the files' existance
        if(!p.hasDocument("vs")) labels.get("vs").setId("ProjectTab_no");
        if(p.hasDocument("vs")) labels.get("vs").setId("ProjectTab_yes");

        if(!p.hasDocument("tc")) labels.get("tc").setId("ProjectTab_no");
        if(p.hasDocument("tc")) labels.get("tc").setId("ProjectTab_yes");

        if(!p.hasDocument("ts")) labels.get("ts").setId("ProjectTab_no");
        if(p.hasDocument("ts")) labels.get("ts").setId("ProjectTab_yes");

        if(!p.hasDocument("gs")) labels.get("gs").setId("ProjectTab_no");
        if(p.hasDocument("gs")) labels.get("gs").setId("ProjectTab_yes");

        if(!p.hasDocument("fs")) labels.get("fs").setId("ProjectTab_no");
        if(p.hasDocument("fs")) labels.get("fs").setId("ProjectTab_yes");

        //Add a listener for the project's saved-property(when the project is changed -> change the style)
        p.getSavedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) ->{
           labels.get("name").setId(newValue ? "ProjectTab_savedProject" : "ProjectTab_unsavedProject");

        });

    }











}
