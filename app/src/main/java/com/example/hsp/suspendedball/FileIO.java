package com.example.hsp.suspendedball;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Created by hsp on 2016/12/16.
 */

public class FileIO {
    /**
     * 自用专门文件读写的类
     */
    private String fileDir = "/data/data/"+getClass().getPackage().getName();
    private String filepath = fileDir+"/data.txt";
    public boolean checkFileDir() {
        File file = new File(fileDir);
        if (file.exists()) {
            System.out.println(fileDir);
            return true;
        } else {
            return false;
        }
    }

    public boolean createFile() {
        File file = new File(filepath);
        if (!checkFile()) {
            try {
                return file.createNewFile();
            }catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public boolean checkFile() {
        File file = new File(filepath);
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean pushData(String[] data) {
        try {
            FileWriter fileWriter = new FileWriter(filepath);
            BufferedWriter writer = new BufferedWriter(fileWriter);
            for (int i = 0 ; i < data.length ; i ++ ) {
                writer.write(data[i]);
                writer.newLine();
            }
            writer.flush();
            writer.close();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String[] pullData() {
        String[] data = new String[]{"-1","-1","-1","-1","-1","-1","-1"};
        try {
            FileReader fileReader = new FileReader(filepath);
            BufferedReader reader = new BufferedReader(fileReader);
            String s = null;
            int i = 0;
            while ((s = reader.readLine()) != null) {
                data[i++] = s;
            }
            reader.close();
            fileReader.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return data;
    }

    public boolean clearData() {
        try {
            File file = new File(filepath);
            if (file.delete()) {
                return createFile();
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
