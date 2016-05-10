package massim.competition2015.monitor;

import massim.competition2015.monitor.data.Util;
import massim.competition2015.monitor.data.WorldInfo;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

/**
 * Created by ta10 on 125.04.15.
 *
 * Creates statistics from the specified xmls-directory/-ies.
 * Based on *ReportGenerator (up to 2014).
 */
public class StatisticsGenerator {

    private File dir;
    private Vector<Integer> fileNumbers;

    public static void main(String args[]) {

        String aDir = null;
        String aDirs = null;

        String usage = "(multiple dirs) StatisticsGenerator -dirs <directrory containing directrories of the xmls> \n"
                +"or (single dir): StatisticsGenerator -dir <directrory containing the xmls>";

        for(int i = 0; i<args.length;i++){
            if(args[i].equalsIgnoreCase("-dirs")){
                aDirs = args[++i];
            }
            else if(args[i].equalsIgnoreCase("-dir")){
                aDir = args[++i];
            }
        }

        if (aDir != null && aDirs != null){
            System.out.println(usage);
            System.exit(-1);
        }
        else if(aDir != null){
            generateStatistics(aDir, false);
            System.exit(0);
        }
        else if(aDirs != null){
            generateStatistics(aDirs, true);
            System.exit(0);
        }
        else{
            System.out.println(usage);
            System.exit(-1);
        }


    }

    protected static void generateStatistics(String directory, boolean multi) {
        File dir = new File(directory);
        if (!dir.isDirectory()){
            System.err.println(dir+" is not a directory.");
            return;
        }

        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()){
                    return true;
                }
                return false;
            }

        };

        if (multi){
            File [] files = dir.listFiles(filter);
            for (int i = 0; i < files.length; i++){
                new StatisticsGenerator(directory+File.separator+files[i].getName());
            }
        }
        else{
            new StatisticsGenerator(directory);
        }
    }

    /**
     *
     * @param xmlDir path to the xmls
     */
    protected StatisticsGenerator(String xmlDir){

        if(!initFile(xmlDir)){
            System.err.println("Could not initialize files for directory " + xmlDir);
            return;
        }


        if (fileNumbers.size() == 0){
            System.err.println("No viable files in directory " + xmlDir);
            return;
        }

        for (int fileId = 0;fileId < fileNumbers.size(); fileId++){
            WorldInfo world = parseFile(fileId);

            handleWorldInfo(world);
        }

        writeStatistics();
    }

    private boolean initFile(String directory) {
        dir = new File(directory);
        if (!dir.isDirectory()){
            return false;
        }

        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()){
                    return false;
                }
                try {
                    String name = f.getName();
                    String firstPart = name.substring(0, dir.getName().length());
                    String extension = name.substring(name.lastIndexOf("."));
                    if (firstPart.equalsIgnoreCase(dir.getName())
                            && extension.equalsIgnoreCase(".xml")) {
                        return true;
                    }
                    return false;
                } catch (IndexOutOfBoundsException e) {
                    return false;
                }
            }

        };
        File [] files = dir.listFiles(filter);
        fileNumbers = new Vector<Integer>(files.length);
        for (int i = 0; i < files.length; i++){
            try {
                String name = files[i].getName();
                String nr = name.substring(dir.getName().length() + 1, name.lastIndexOf("."));
                fileNumbers.add(new Integer(nr));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Collections.sort(fileNumbers);
        return true;
    }

    /**
     * Parses file with given id into {@link WorldInfo} object
     * @param id ID of the file to parse
     * @return A {@link WorldInfo} object or null if parsing didn't work out
     */
    private WorldInfo parseFile(int id) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            File currFile = new File(dir, dir.getName() + "_" + fileNumbers.get(id).toString()+".xml");
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(currFile);
            return Util.parseXML(document);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Accumulate relevant information here
     * @param world one of the WorldInfo objects generated from a single xml-File
     */
    private void handleWorldInfo(WorldInfo world) {
        //TODO 2015
    }

    /**
     * Use accumulated data to generate charts and stuff.
     */
    private void writeStatistics() {
        //TODO 2015
    }
}
