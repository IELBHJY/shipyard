package com.company;

import com.csvreader.CsvReader;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by apple on 2018/3/17.
 */
public class TSP {
    private  double[] xPos;
    private double[] yPos;
    public void solveModel(int n){
        //random data
        xPos=new double[n];
        yPos=new double[n];
        readData(n);
        double[][] c=new double[n][n];
        for(int i=0;i<n;i++){
            for(int j=0;j<n;j++){
                c[i][j]=Math.sqrt(Math.pow(xPos[i]-xPos[j],2)+Math.pow(yPos[i]-yPos[j],2));
            }
        }
        //build model
        try{
            IloCplex model=new IloCplex();
            model.setOut(null);
            // variable;
            IloNumVar[][] x=new IloNumVar[n][];
            for (int i=0;i<n;i++){
                x[i]=model.boolVarArray(n);
            }
            IloNumVar[] u=model.numVarArray(n,0,Double.MAX_VALUE);
            //object
            IloLinearNumExpr obj=model.linearNumExpr();
            for(int i=0;i<n;i++){
                for(int j=0;j<n;j++){
                    if(j!=i){
                        obj.addTerm(c[i][j],x[i][j]);
                    }
                }
            }
            model.addMinimize(obj);
            //constraints
            for(int j=0;j<n;j++){
                IloLinearNumExpr expr=model.linearNumExpr();
                for(int i=0;i<n;i++){
                    if(i!=j){
                        expr.addTerm(1.0,x[i][j]);
                    }
                }
                model.addEq(expr,1.0);
            }
            for(int i=0;i<n;i++){
                IloLinearNumExpr expr=model.linearNumExpr();
                for(int j=0;j<n;j++){
                    if(j!=i){
                        expr.addTerm(1.0,x[i][j]);
                    }
                }
                model.addEq(expr,1.0);
            }
            for(int i=1;i<n;i++){
                for(int j=1;j<n;j++){
                    if(i!=j){
                        IloLinearNumExpr expr=model.linearNumExpr();
                        expr.addTerm(1.0,u[i]);
                        expr.addTerm(-1.0,u[j]);
                        expr.addTerm(n-1,x[i][j]);
                        model.addLe(expr,n-2);
                    }
                }
            }
            //solve model
            model.solve();
            System.out.println(model.getObjValue());
            int[] path=new int[n];
            for(int i=0;i<n;i++){
               for(int j=0;j<n;j++){
                   if(j!=i && model.getValue(x[i][j])==1){
                       path[i]=j;
                   }
               }
            }
            int i=0;
            int count=0;
            while(count<n){
                System.out.print(i+"->"+path[i]+" ");
                i=path[i];
                count++;
            }
            System.out.println();
            model.end();
        }catch (IloException e){
            e.printStackTrace();
        }
    }

    private  void readData(int n)
    {
        try {
            ArrayList<String[]> csvFileList = new ArrayList<>();
            String csvFilePath = "/Users/apple/Github/ACO-for-tsp/ACOForTSP/tsp/tsp40.csv";
            //String csvFilePath = "/Users/apple/Desktop/tsp.csv";
            CsvReader reader = new CsvReader(csvFilePath, ',', Charset.forName("UTF-8"));
            while (reader.readRecord()){
                csvFileList.add(reader.getValues());
            }
            reader.close();
            for (int i = 0; i<n; i++) {
                String[] strData = csvFileList.get(i);
                //number[i]=Double.valueOf(strData[0]).intValue();
                xPos[i]=Double.valueOf(strData[1]);
                yPos[i]=Double.valueOf(strData[2]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
