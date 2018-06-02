package io.xiwi.junny;


import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.MultimediaInfo;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.*;
import java.util.Properties;

/**
 * @author <a href="mailto:simling82@gmail.com">Simling</a>
 * @version v1.0 on 2018/5/31
 */


public class MediaInfo {

    private Properties prop;

    public void getVideoDuration(String path) {
        // get all files in specified "path"
        File[] files = new File(path).listFiles();

        Encoder encoder = new Encoder();
        MultimediaInfo multimediaInfo;

        long totalTime = 0L;
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");
        int rowindex = 0;

        Row row = sheet.createRow(0);
        int cellIndex = 0;
        for (String key : prop.stringPropertyNames()) {
            cellIndex = Integer.valueOf(key.split("\\.")[1]);
            Cell cell = row.createCell(cellIndex);
            cell.setCellValue(prop.get(key).toString());
        }
        rowindex++;

        for (int i = 0; i < files.length; i++) {

            // here, the format of video can be changed, JAVE upports dozens of formats
            if (!files[i].isDirectory()) {
                try {
                    multimediaInfo = encoder.getInfo(files[i]);
                    long duration = multimediaInfo.getDuration();
                    totalTime = duration;

                    System.out.println(files[i].getName() + "\t" + multimediaInfo);
                    row = sheet.createRow(rowindex);

                    for (String key : prop.stringPropertyNames()) {
                        String v = prop.get(key).toString();
                        String value = null;
                        if("filename".equals(v)){
                            value = files[i].getName();
                        }else {
                            value = PropertyUtils.getNestedProperty(multimediaInfo, v).toString();
                            if("duration".equals(v)) {
                                value = DurationFormatUtils.formatDuration(totalTime, "HH:mm:ss");
                            }
                        }

                        System.out.println("key: "+key+ ", value: "+value);
                        cellIndex = Integer.valueOf(key.split("\\.")[1]);
                        Cell cell = row.createCell(cellIndex);
                        cell.setCellValue(value);
                    }
                    rowindex++;
                } catch (Exception e) {
                    System.out.println(files[i].getName()+" is not video file.");
                }
            }else {
                getVideoDuration(files[i].getPath());
            }
        }
        path = path.replaceAll("\\\\", "/");
        String[] temp = path.split("/");
        String filename = temp[temp.length - 1];
        System.out.println("filename: "+filename);
        try {
            workbook.write(new FileOutputStream(path + "/"+filename+".xls"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadProperties(String path){
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(
                    new File(path)));
            prop = new Properties();

            prop.load(in);
            System.out.println("prop:"+prop);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        if(args == null || args.length == 0){
            System.out.println("Usage: java -jar junny-tools.jar [media source file path] [property file]");
            return;
        }
        String filePath = args[0]; //"C:/BaiduNetdiskDownload/";;

        MediaInfo mediaInfo = new MediaInfo();
        String path = null;
        if(args.length == 2){
            path = args[1];
        }else {
            path = mediaInfo.getClass().getResource("/").getPath()+"media.properties";
        }

        System.out.println("path:"+path);
        mediaInfo.loadProperties(path);
        mediaInfo.getVideoDuration(filePath);
    }
}
