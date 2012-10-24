import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import keggapi.KEGGLocator;
import keggapi.KEGGPortType;

public class getColoredPathwaysThread implements Runnable {

    private String genomeStr;
    private String suf;
    private int idx;
    private ArrayList<List<String>> clustersList;
    private String imgsDir;
    private String algo;

    public getColoredPathwaysThread(String algo, String genomeStr, String suf, int idx, ArrayList<List<String>> clustersList){
        this.genomeStr = genomeStr;
        this.idx = idx;
        this.clustersList = clustersList;
        this.suf = suf;
        this.imgsDir = GlobalInitializer.imgsDirForCurRun;
        this.algo = algo;
    }
    
    public void run() {
        MainClass.colorPathsThreadCnt++;

        int numTries = 5;
        while(true){
            try {
                KEGGLocator  locator;
                KEGGPortType serv;
                locator = new KEGGLocator();
                serv = locator.getKEGGPort();

                int clusterOffset = 0;
                int bgColor = 0;
                String fgColor = "#ffffff";

                String[] obj_list = new String[MainClass.totalGenesNumber];
                String[] fg_list = new String[MainClass.totalGenesNumber];
                String[] bg_list = new String[MainClass.totalGenesNumber];
                String[] bgColors = {"#0000ff", "#ff0000", "#f0f000", "#00f0f0", "#0f0f00", "000f0f"};

                for(int cl=0; cl<clustersList.size(); cl++){

                    for(int k=0; k<clustersList.get(cl).size(); k++){
                        obj_list[clusterOffset + k] = clustersList.get(cl).get(k);
                        fg_list[clusterOffset + k] = fgColor;
                        bg_list[clusterOffset + k] = bgColors[cl];
                    }

                    clusterOffset += clustersList.get(cl).size();
                    
                }

                String pathStr = "path:"+genomeStr+MainClass.pathId;
                String url = serv.color_pathway_by_objects(pathStr, obj_list, fg_list, bg_list);
                System.out.println(url);
                String imFileStr = imgsDir+"/"+(this.algo)+"/images/"+genomeStr+"Path"+suf+".png";
                saveImage(url, imFileStr);

                break;
            } catch(Exception e){
                        if (--numTries == 0){
                    try {
                        System.err.println("Error: " + e.getMessage());
                        throw e;
                    } catch (Exception ex) {
                        Logger.getLogger(getColoredPathwaysThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                        }
                    }
            }
        
        MainClass.colorPathsThreadCnt--;
    }

    
    public static void saveImage(String imageUrl, String destinationFile) throws IOException {
		URL url = new URL(imageUrl);
		InputStream is = url.openStream();
		OutputStream os = new FileOutputStream(destinationFile);

		byte[] b = new byte[2048];
		int length;

		while ((length = is.read(b)) != -1) {
			os.write(b, 0, length);
		}

		is.close();
		os.close();
    }

};