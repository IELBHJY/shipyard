package shipyard;


import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

public class Data {
    public final static double E=0;
    public final static double L=1000;
    //个数
    public int n;
    public int t;
    public int r;
    public int p;
    // information about cars
    public int[] truckIDs;
    public int[] truckCaptitys;

    // infromation about tasks
    public int[] taskIDs;
    public String[] taskStartPlace;
    public String[] taskEndPlace;
    public int[] taskWeights;
    public int[] earlyTime;
    public int[] lateTime;
    public double[] carryTaskTime;
    public double[][] w;
    public int[] prior;
    // information about roads
    public int[] roadIDs;
    public int[] x;
    public int[] y;

    //information about places
    public int[] placeIDs;
    public double[] placeX;
    public double[] placeY;
    public String[] placeNames;
    public static DecimalFormat df=new DecimalFormat("#.0000");

    public Data(int n,int t,int r,int p) {
        this.n=n+1;
        this.t=t+1;
        this.r=r+1;
        this.p=p+1;
        truckIDs=new int[t+1];
        truckCaptitys=new int[t+1];
        roadIDs=new int[r+1];
        x=new int[r+1];
        y=new int[r+1];
        taskIDs=new int[n+1];
        taskWeights=new int[n+1];
        w=new double[n+1][n+1];
        prior=new int[n+1];
        taskStartPlace=new String[n+1];
        taskEndPlace=new String[n+1];
        earlyTime=new int[n+1];
        lateTime=new int[n+1];
        carryTaskTime=new double[n+1];
        placeIDs=new int[p+1];
        placeX=new double[p+1];
        placeY=new double[p+1];
        placeNames=new String[p+1];
    }

    public void readData(String filePath) throws IOException, BiffException {
        try {
            InputStream stream = new FileInputStream(filePath);
            //获取Excel文件对象
            Workbook rwb = Workbook.getWorkbook(stream);
            Sheet sheet = rwb.getSheet("car"+Parameter.truck);
            //行数(表头的目录不需要，从1开始)
            Cell cell=null;
            for(int i=1; i<t; i++){
                //for(int j=0; j<sheet.getColumns(); j++){
                    //获取第i行，第j列的值
                cell = sheet.getCell(0,i);
                String temp=cell.getContents();
                truckIDs[i]=Integer.parseInt(temp);
                cell =sheet.getCell(1,i);
                temp=cell.getContents();
                truckCaptitys[i]=Integer.parseInt(temp);
            }

            sheet=rwb.getSheet("road");
            for(int i=1;i<sheet.getRows();i++){
                cell=sheet.getCell(0,i);
                String temp=cell.getContents();
                roadIDs[i]=Integer.parseInt(temp);
                cell=sheet.getCell(1,i);
                temp=cell.getContents();
                x[i]=Integer.parseInt(temp);
                cell=sheet.getCell(2,i);
                temp=cell.getContents();
                y[i]=Integer.parseInt(temp);
            }
            sheet=rwb.getSheet("task"+Parameter.task);
            for(int i=1;i<n;i++){
                cell=sheet.getCell(0,i);
                String temp=cell.getContents();
                taskIDs[i]=Integer.parseInt(temp);
                cell=sheet.getCell(1,i);
                temp=cell.getContents();
                taskStartPlace[i]=temp;
                cell=sheet.getCell(2,i);
                temp=cell.getContents();
                taskEndPlace[i]=temp;
                cell=sheet.getCell(3,i);
                temp=cell.getContents();
                earlyTime[i]=Integer.parseInt(temp);
                cell=sheet.getCell(4,i);
                temp=cell.getContents();
                lateTime[i]=Integer.parseInt(temp);
                cell=sheet.getCell(5,i);
                temp=cell.getContents();
                taskWeights[i]=Integer.parseInt(temp);
            }
            sheet=rwb.getSheet("place");
            for(int i=1;i<sheet.getRows();i++){
                placeIDs[i]=i;
                cell=sheet.getCell(0,i);
                String temp=cell.getContents();
                placeNames[i]=temp;
                cell=sheet.getCell(1,i);
                temp=cell.getContents();
                placeX[i]=Double.parseDouble(temp);
                cell=sheet.getCell(2,i);
                temp=cell.getContents();
                placeY[i]=Double.parseDouble(temp);
            }
            stream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        calTaskTime();
        calTwoTasksTime();
    }

    private void calTaskTime(){
        for(int i=1;i<n;i++){
            carryTaskTime[i]=double_truncate(calPathTime(taskStartPlace[i],taskEndPlace[i])/2);
        }
    }

    private void calTwoTasksTime(){
        for(int i=1;i<n;i++){
            for(int j=i+1;j<n;j++){
                 w[i][j]=double_truncate(calPathTime(taskEndPlace[i],taskStartPlace[j])/2);
                 w[j][i]=w[i][j];
            }
        }
        for(int i=1;i<n;i++){
            w[0][i]=calPathTime("车场",taskStartPlace[i])/2;
            w[i][0]=w[0][i];
        }
    }

    public double calPathTime(String p1,String p2){
        int first=0,last=0;
        for(int i=1;i<placeNames.length;i++){
            if(placeNames[i].equals(p1))
                first=i;
            else if(placeNames[i].equals(p2))
                last=i;
        }
        double b= Math.sqrt(Math.pow((placeX[first]-placeX[last]),2)+Math.pow((placeY[first]-placeY[last]),2));
        return double_truncate(b);
    }

    public static double double_truncate(double v){
        int iv = (int) v;
        if(iv+1 - v <= 0.0001)
            return iv+1;
        double dv = (v - iv) * 10;
        int idv = (int) dv;
        double rv = iv + idv / 10.0;
        return rv;
    }
}
