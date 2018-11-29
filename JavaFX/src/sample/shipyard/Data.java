package sample.shipyard;


import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import sample.Algorithm.ConnectDB;
import sample.Algorithm.FindBestPath;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

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
    public String[] truckNames;
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
    //shortest path
    private HashMap<String,String> paths;
    private List<Integer> trucks;

    public static DecimalFormat df=new DecimalFormat("#.0000");

    int[][] adj={{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,1,1,0,1,1,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,1,0,0,1,0,0,0,1,0,0,0,0,0},
            {0,0,0,0,0,1,1,0,0,0,0,0,1,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0},
            {0,0,0,0,0,0,0,0,1,0,1,0,0,0,1,0,0},
            {0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,1,0},
            {0,0,0,0,0,0,1,0,0,0,1,0,1,0,0,0,1},
            {0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0},
            {0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,1,0},
            {0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,1},
            {0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0},
    };

    private ConnectDB db;

    public Data(int n,int t,int r,int p,List<Integer> trucks) {
        this.n=n+1;
        this.t=t+1;
        this.r=r+1;
        this.p=p+1;
        truckIDs=new int[t+1];
        truckNames=new String[t+1];
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
        paths=new HashMap<String, String>();
        this.trucks=trucks;
    }

    public void readData1(String filePath) throws IOException, BiffException {
        try {
            InputStream stream = new FileInputStream(filePath);
            //获取Excel文件对象
            Workbook rwb = Workbook.getWorkbook(stream);
            Sheet sheet = rwb.getSheet("test");
            //行数(表头的目录不需要，从1开始)
            Cell cell;
            int j=1;
            for(int i=1; i<=8; i++){
                cell = sheet.getCell(0,i);
                String temp=cell.getContents();
                if(trucks.contains(Integer.parseInt(temp))) {
                    truckIDs[j] = Integer.parseInt(temp);
                    cell = sheet.getCell(1, i);
                    temp = cell.getContents();
                    truckCaptitys[j++] = Integer.parseInt(temp);
                }
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
            sheet=rwb.getSheet("test"+Parameter.task);
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

    public void readData(String filePath) throws Exception {
        try {
            InputStream stream = new FileInputStream(filePath);
            //获取Excel文件对象
            Workbook rwb = Workbook.getWorkbook(stream);
            Sheet sheet=rwb.getSheet("test"+Parameter.task);
            Cell cell;
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
            stream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        readDatabyMySQL();
        calTaskTime();
        calTwoTasksTime();
    }

    public void readDatabyMySQL() throws Exception{
        db=new ConnectDB();
        db.connection();
        ResultSet flatcars=db.query("flatcars");
        int j=1;
        while (flatcars.next()){
             if(trucks.contains(Integer.parseInt(flatcars.getString("ID")))){
                 truckIDs[j]=Integer.parseInt(flatcars.getString("ID"));
                 truckCaptitys[j]=(int) Double.parseDouble(flatcars.getString("额定载重"));
                 j++;
             }
        }
        ResultSet roads=db.query("roads");
        j=1;
        while (roads.next()){
            roadIDs[j]=Integer.parseInt(roads.getString("ID"));
            x[j]=(int) Double.parseDouble(roads.getString("横坐标"));
            y[j]=(int) Double.parseDouble(roads.getString("纵坐标"));
            j++;
        }
        ResultSet plate=db.query("storage");
        j=1;
        while (plate.next()){
            placeIDs[j]=Integer.parseInt(plate.getString("ID"));
            placeNames[j]=plate.getString("storage_name");
            placeX[j]=Double.parseDouble(plate.getString("xcoordinate"));
            placeY[j]=Double.parseDouble(plate.getString("ycoordinate"));
            j++;
        }
        db.close();
    }

    private void calTaskTime(){
        for(int i=1;i<n;i++){
            //carryTaskTime[i]=calPathTime(taskStartPlace[i],taskEndPlace[i])/2;
            carryTaskTime[i]=cal(taskStartPlace[i],taskEndPlace[i])/6;
        }
    }

    private double cal(String p1,String p2){
        double[] cost={0.0,0.0};
        try {
            FindBestPath findBestPath = new FindBestPath(16, adj);
            List<Integer> path=findBestPath.findShortPath(p1,p2,cost);
            String result=p1+"->";
            for(Integer i:path){
                result+=i.toString()+"->";
            }
            result+=p2;
            paths.put(p1+"-"+p2,result);
            paths.put(p2+"-"+p1,result);
        }catch (Exception e){
            e.printStackTrace();
        }
        return cost[0];
    }

    private void calTwoTasksTime(){
        for(int i=1;i<n;i++){
            for(int j=i+1;j<n;j++){
                 //w[i][j]=calPathTime(taskEndPlace[i],taskStartPlace[j])/2;
                 w[i][j]=cal(taskEndPlace[i],taskStartPlace[j])/10;
                 w[j][i]=w[i][j];
            }
        }
        for(int i=1;i<n;i++){
            //w[0][i]=calPathTime("车场",taskStartPlace[i])/2;
            w[0][i]=cal("车场",taskStartPlace[i]);
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

    public String getPaths(String p1,String p2){
        return this.paths.get(p1+"-"+p2);
    }
}
