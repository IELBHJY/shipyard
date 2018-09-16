package com.company;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by apple on 2018/3/30.
 */
public class SP_TimeConstraint {
    private int[][] cost;
    private int[][] t;
    final int INF=Integer.MAX_VALUE;
    IloNumVar[][] x;
    static int n=6;
    private void creatData(int n,String path) throws IOException{
        Scanner cin = new Scanner(new BufferedReader(new FileReader(path)));
        cost=new int[n][n];
        for(int i=0;i<n;i++) {
            String line = cin.nextLine();
            String[] lines=line.split(" ");
            for(int j=0;j<lines.length;j++){
                if(Integer.parseInt(lines[j])==0){
                    cost[i][j]=INF;
                }else{
                    cost[i][j]=Integer.parseInt(lines[j]);
                }
            }
        }
    }

    private void creatTime(int n,String path) throws IOException{
        Scanner cin = new Scanner(new BufferedReader(new FileReader(path)));
        t=new int[n][n];
        for(int i=0;i<n;i++) {
            String line = cin.nextLine();
            String[] lines=line.split(" ");
            for(int j=0;j<lines.length;j++){
                if(Integer.parseInt(lines[j])==0){
                    t[i][j]=INF;
                }else{
                    t[i][j]=Integer.parseInt(lines[j]);
                }
            }
        }
    }


    public void buildModel()throws IloException,IOException{
        creatData(n,"/Users/apple/Desktop/cost.txt");
        creatTime(n,"/Users/apple/Desktop/time.txt");
        IloCplex cplex=new IloCplex();
        x=new IloNumVar[n][n];
        //设定x的取值范围和类型
        for(int i=0;i<n;i++){
            for(int j=0;j<n;j++){
                x[i][j]=cplex.numVar(0,1, IloNumVarType.Int);
                x[i][j].setLB(0);
                x[i][j].setUB(0);
            }
        }
        //建立约束条件
        IloLinearNumExpr obj=cplex.linearNumExpr();
        for(int i=0;i<n-1;i++){
            for(int j=1;j<n;j++){
                if(i!=j){
                    obj.addTerm(cost[i][j],x[i][j]);
                }
            }
        }
        cplex.addMinimize(obj);
        //约束1 从1点出发的点   约束2 回到点6
        IloLinearNumExpr expr=cplex.linearNumExpr();
        for(int i=1;i<n;i++){
            expr.addTerm(1,x[0][i]);
        }
        cplex.eq(1,expr);
        IloLinearNumExpr expr1=cplex.linearNumExpr();
        for(int i=n-2;i>0;i--){
            expr1.addTerm(1,x[i][n-1]);
        }
        cplex.eq(1,expr1);
        //约束3 满足时间约束sum{t*x}<=14
        IloLinearNumExpr expr2=cplex.linearNumExpr();
        for(int i=0;i<n-1;i++){
            for(int j=1;j<n;j++){
                if(i!=j){
                    expr2.addTerm(t[i][j],x[i][j]);
                }
            }
        }
        cplex.le(expr2,14);
        IloLinearNumExpr expr3=cplex.linearNumExpr();
        IloLinearNumExpr expr4=cplex.linearNumExpr();
        for(int i=1;i<n-1;i++){
            for(int j=1;j<n-1;j++){
                if(i!=j){
                    expr3.addTerm(1,x[i][j]);
                    expr4.addTerm(-1,x[j][i]);
                }
            }
        }
        cplex.eq(expr3,expr4);
        if(cplex.solve()){
            for(int i=0;i<n;i++){
                for(int j=0;j<n;j++){
                    System.out.println(cplex.getObjValue());
                    //System.out.println(x[0][0].getLB()+" "+x[0][0].getUB());
                }
            }
        }
    }

}
